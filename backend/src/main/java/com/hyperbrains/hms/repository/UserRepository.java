package com.hyperbrains.hms.repository;

import com.hyperbrains.hms.domain.User;
import java.time.Instant;
import java.util.List;
import java.util.Optional;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.data.domain.*;
import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;

/**
 * Spring Data JPA repository for the {@link User} entity.
 */
@Repository
public interface UserRepository extends JpaRepository<User, Long> {
    String USERS_BY_LOGIN_CACHE = "usersByLogin";

    String USERS_BY_EMAIL_CACHE = "usersByEmail";
    Optional<User> findOneByActivationKey(String activationKey);
    List<User> findAllByActivatedIsFalseAndActivationKeyIsNotNullAndCreatedDateBefore(Instant dateTime);
    Optional<User> findOneByResetKey(String resetKey);
    Optional<User> findOneByEmailIgnoreCase(String email);
    Optional<User> findOneByLogin(String login);

    @EntityGraph(attributePaths = "authorities")
    @Cacheable(cacheNames = USERS_BY_LOGIN_CACHE, unless = "#result == null")
    Optional<User> findOneWithAuthoritiesByLogin(String login);

    @EntityGraph(attributePaths = "authorities")
    @Cacheable(cacheNames = USERS_BY_EMAIL_CACHE, unless = "#result == null")
    Optional<User> findOneWithAuthoritiesByEmailIgnoreCase(String email);

    Page<User> findAllByIdNotNullAndActivatedIsTrue(Pageable pageable);

    /**
     * The account state that decides whether a sign-in is allowed, read straight from the row.
     *
     * <p>Deliberately not the account the rest of the sign-in path loads: that one is served from the
     * {@code usersByLogin} cache, and the only eviction in the application is in {@code UserService}. The counting
     * listener writes a lock through this repository, so the cached copy went on saying the account was open — the
     * lock did nothing at all until the cache expired, which is an hour, and the same in reverse for an
     * administrator releasing one. A scalar projection is not cached, so the controls see what was stored, by
     * whoever stored it.
     */
    interface SignInState {
        boolean isActivated();

        Instant getSessionsValidFrom();

        Instant getLockedAt();
    }

    @Query(
        "select user.activated as activated, user.sessionsValidFrom as sessionsValidFrom, user.lockedAt as lockedAt from User user where user.login = :login"
    )
    Optional<SignInState> findSignInStateByLogin(@Param("login") String login);

    /**
     * When a request last arrived on this account's session, read straight from the row.
     *
     * <p>Deliberately not the account the session filter otherwise loads: that one is served from the
     * {@code usersByLogin} cache, and this value is written on requests. A cached copy would be a value that
     * stopped moving, which would mean an idle timeout that never fires and, worse, an active session refused
     * once its token had been open long enough. A scalar projection is not cached.
     */
    @Query("select user.lastActivityAt from User user where user.login = :login")
    Optional<Instant> findLastActivityAtByLogin(@Param("login") String login);

    /**
     * Records that a request arrived on this account's session.
     *
     * <p>A targeted update rather than saving a loaded entity, so that nothing cached is left holding the old
     * value and the cost is one statement on whichever node served the request.
     */
    @Modifying
    @Transactional
    @Query("update User user set user.lastActivityAt = :at where user.login = :login")
    void recordActivityAt(@Param("login") String login, @Param("at") Instant at);

    /**
     * How many other active accounts hold this authority.
     *
     * <p>Counted in the database rather than by loading the user table, because the answer decides whether a
     * change is allowed at all and a stale or filtered list on the application side would silently answer
     * "none" — which is the answer that locks the hospital out.
     */
    @Query(
        "select count(u) from User u join u.authorities a where a.name = :authority and u.activated = true and u.id <> :excludedId"
    )
    long countOtherActiveUsersWithAuthority(@Param("authority") String authority, @Param("excludedId") Long excludedId);
}
