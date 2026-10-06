package com.hyperbrains.hms.web.rest;

import static com.hyperbrains.hms.domain.AuthorityAsserts.*;
import static org.assertj.core.api.Assertions.assertThat;
import static org.hamcrest.Matchers.hasItem;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.hyperbrains.hms.IntegrationTest;
import com.hyperbrains.hms.domain.Authority;
import com.hyperbrains.hms.repository.AuthorityRepository;
import com.hyperbrains.hms.security.AuthoritiesConstants;
import jakarta.persistence.EntityManager;
import java.util.UUID;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc;
import org.springframework.http.MediaType;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.transaction.annotation.Transactional;

/**
 * Integration tests for the {@link AuthorityResource} REST controller.
 *
 * <p>The role list is read here and never written: a role is seeded with the application and every one of them is a
 * string the RBAC table or an annotation matches against, so the write routes are refused for every role, Super Admin
 * included. The generated create and delete tests are kept for the routes they name and assert that refusal instead of
 * a write.
 */
@IntegrationTest
@AutoConfigureMockMvc
@WithMockUser(authorities = { AuthoritiesConstants.SUPER_ADMIN })
class AuthorityResourceIT {

    private static final String ENTITY_API_URL = "/api/authorities";
    private static final String ENTITY_API_URL_ID = ENTITY_API_URL + "/{name}";

    @Autowired
    private ObjectMapper om;

    @Autowired
    private AuthorityRepository authorityRepository;

    @Autowired
    private EntityManager em;

    @Autowired
    private MockMvc restAuthorityMockMvc;

    private Authority authority;

    private Authority insertedAuthority;

    /**
     * Create an entity for this test.
     *
     * This is a static method, as tests for other entities might also need it,
     * if they test an entity which requires the current entity.
     */
    public static Authority createEntity() {
        return new Authority().name(UUID.randomUUID().toString());
    }

    /**
     * Create an updated entity for this test.
     *
     * This is a static method, as tests for other entities might also need it,
     * if they test an entity which requires the current entity.
     */
    public static Authority createUpdatedEntity() {
        return new Authority().name(UUID.randomUUID().toString());
    }

    @BeforeEach
    void initTest() {
        authority = createEntity();
    }

    @AfterEach
    void cleanup() {
        if (insertedAuthority != null) {
            authorityRepository.delete(insertedAuthority);
            insertedAuthority = null;
        }
    }

    /**
     * A role is what the RBAC table and the method annotations match against, so creating one over HTTP grants
     * nothing and changes what the application is built around rather than what it records. Refused, and nothing is
     * written.
     */
    @Test
    @Transactional
    void createAuthorityIsRefused() throws Exception {
        long databaseSizeBeforeCreate = getRepositoryCount();

        restAuthorityMockMvc
            .perform(post(ENTITY_API_URL).contentType(MediaType.APPLICATION_JSON).content(om.writeValueAsBytes(authority)))
            .andExpect(status().isForbidden());

        // Validate the Authority in the database: nothing was written.
        assertSameRepositoryCount(databaseSizeBeforeCreate);
    }

    /**
     * A body that would otherwise be a duplicate is refused before it is read: authorization happens before dispatch,
     * so what is sent never reaches a validator. Kept from the generated test because that is what it shows.
     */
    @Test
    @Transactional
    void createAuthorityIsRefusedBeforeTheBodyIsRead() throws Exception {
        // Create the Authority with an existing ID
        insertedAuthority = authorityRepository.saveAndFlush(authority);

        long databaseSizeBeforeCreate = getRepositoryCount();

        restAuthorityMockMvc
            .perform(post(ENTITY_API_URL).contentType(MediaType.APPLICATION_JSON).content(om.writeValueAsBytes(authority)))
            .andExpect(status().isForbidden());

        // Validate the Authority in the database
        assertSameRepositoryCount(databaseSizeBeforeCreate);
    }

    @Test
    @Transactional
    void getAllAuthorities() throws Exception {
        // Initialize the database
        authority.setName(UUID.randomUUID().toString());
        insertedAuthority = authorityRepository.saveAndFlush(authority);

        // Get all the authorityList
        restAuthorityMockMvc
            .perform(get(ENTITY_API_URL + "?sort=name,desc"))
            .andExpect(status().isOk())
            .andExpect(content().contentType(MediaType.APPLICATION_JSON_VALUE))
            .andExpect(jsonPath("$.[*].name").value(hasItem(authority.getName())));
    }

    @Test
    @Transactional
    void getAuthority() throws Exception {
        // Initialize the database
        authority.setName(UUID.randomUUID().toString());
        insertedAuthority = authorityRepository.saveAndFlush(authority);

        // Get the authority
        restAuthorityMockMvc
            .perform(get(ENTITY_API_URL_ID, authority.getName()))
            .andExpect(status().isOk())
            .andExpect(content().contentType(MediaType.APPLICATION_JSON_VALUE))
            .andExpect(jsonPath("$.name").value(authority.getName()));
    }

    @Test
    @Transactional
    void getNonExistingAuthority() throws Exception {
        // Get the authority
        restAuthorityMockMvc.perform(get(ENTITY_API_URL_ID, Long.MAX_VALUE)).andExpect(status().isNotFound());
    }

    /**
     * Deleting a role is refused for the same reason it cannot be created: the code asks for the role by name, so
     * removing one over HTTP takes away a door key the application still expects to find.
     */
    @Test
    @Transactional
    void deleteAuthorityIsRefused() throws Exception {
        // Initialize the database
        authority.setName(UUID.randomUUID().toString());
        insertedAuthority = authorityRepository.saveAndFlush(authority);

        long databaseSizeBeforeDelete = getRepositoryCount();

        // Delete the authority
        restAuthorityMockMvc
            .perform(delete(ENTITY_API_URL_ID, authority.getName()).accept(MediaType.APPLICATION_JSON))
            .andExpect(status().isForbidden());

        // Validate the database still holds it
        assertSameRepositoryCount(databaseSizeBeforeDelete);
    }

    protected long getRepositoryCount() {
        return authorityRepository.count();
    }

    protected void assertIncrementedRepositoryCount(long countBefore) {
        assertThat(countBefore + 1).isEqualTo(getRepositoryCount());
    }

    protected void assertDecrementedRepositoryCount(long countBefore) {
        assertThat(countBefore - 1).isEqualTo(getRepositoryCount());
    }

    protected void assertSameRepositoryCount(long countBefore) {
        assertThat(countBefore).isEqualTo(getRepositoryCount());
    }

    protected Authority getPersistedAuthority(Authority authority) {
        return authorityRepository.findById(authority.getName()).orElseThrow();
    }

    protected void assertPersistedAuthorityToMatchAllProperties(Authority expectedAuthority) {
        assertAuthorityAllPropertiesEquals(expectedAuthority, getPersistedAuthority(expectedAuthority));
    }

    protected void assertPersistedAuthorityToMatchUpdatableProperties(Authority expectedAuthority) {
        assertAuthorityAllUpdatablePropertiesEquals(expectedAuthority, getPersistedAuthority(expectedAuthority));
    }
}
