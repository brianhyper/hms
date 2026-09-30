package com.hyperbrains.hms.security;

import static org.assertj.core.api.Assertions.assertThat;

import java.lang.reflect.Field;
import java.lang.reflect.Modifier;
import java.util.List;
import org.junit.jupiter.api.Test;

/**
 * The role model, held to Phase 3's nine roles.
 *
 * <p>Phase 3 fixes the role model: there is no permission engine and no editable role list, so a role that
 * exists in code is a role that can be attached to somebody and mapped to an endpoint. That makes "how many
 * roles are there" a security-relevant question rather than a housekeeping one, and it is the question this
 * test answers from the constants themselves instead of from a list somebody has to remember to update.
 */
class RoleModelTest {

    /** The nine roles Phase 3 names, each against the constant that carries it. */
    private static final List<String> PHASE_3_ROLES = List.of(
        AuthoritiesConstants.DOCTOR,
        AuthoritiesConstants.NURSE,
        AuthoritiesConstants.ADMIN,
        AuthoritiesConstants.RECEPTION,
        AuthoritiesConstants.SUPER_ADMIN,
        AuthoritiesConstants.LAB,
        AuthoritiesConstants.PHARMACY,
        AuthoritiesConstants.FINANCE,
        AuthoritiesConstants.HR
    );

    /**
     * The stock JHipster roles, which are not hospital roles but cannot be removed: {@code ROLE_USER} is what
     * the login flow needs to reach {@code /api/account}, and {@code ROLE_ANONYMOUS} is Spring's.
     */
    private static final List<String> PLATFORM_ROLES = List.of(AuthoritiesConstants.USER, AuthoritiesConstants.ANONYMOUS);

    @Test
    void everyRoleInTheModelIsItsOwnAuthority() {
        assertThat(PHASE_3_ROLES)
            .as("a role repeated in the model would be two names for one permission set")
            .doesNotHaveDuplicates()
            .allMatch(role -> role.startsWith("ROLE_"));
    }

    @Test
    void thereAreExactlyTheRolesPhase3NamesPlusTheOneStillAwaitingAnAnswer() {
        // RADIOLOGY is the exception and is named here on purpose. Phase 3's role list does not include it,
        // while the code has had its own queues and RBAC rows since Phase 1, so the question of whether Lab
        // covers radiology is open. Until it is answered both exist, and this test makes the exception
        // visible: adding any *other* role fails here rather than quietly widening the model.
        List<String> expected = new java.util.ArrayList<>(PHASE_3_ROLES);
        expected.add(AuthoritiesConstants.RADIOLOGY);
        expected.addAll(PLATFORM_ROLES);

        assertThat(declaredAuthorities()).containsExactlyInAnyOrderElementsOf(expected);
    }

    /** Every authority constant the class declares, read from the class rather than from a hand-kept list. */
    private static List<String> declaredAuthorities() {
        return java.util.Arrays
            .stream(AuthoritiesConstants.class.getDeclaredFields())
            .filter(field -> Modifier.isStatic(field.getModifiers()) && field.getType() == String.class)
            .map(RoleModelTest::valueOf)
            .toList();
    }

    private static String valueOf(Field field) {
        try {
            return (String) field.get(null);
        } catch (IllegalAccessException ex) {
            throw new IllegalStateException("Authority constants are public, so this cannot happen", ex);
        }
    }
}
