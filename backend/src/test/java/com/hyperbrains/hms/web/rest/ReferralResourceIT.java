package com.hyperbrains.hms.web.rest;

import static com.hyperbrains.hms.domain.ReferralAsserts.*;
import static com.hyperbrains.hms.web.rest.TestUtil.createUpdateProxyForBean;
import static org.assertj.core.api.Assertions.assertThat;
import static org.hamcrest.Matchers.hasItem;
import static org.mockito.Mockito.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.hyperbrains.hms.IntegrationTest;
import com.hyperbrains.hms.domain.Referral;
import com.hyperbrains.hms.domain.User;
import com.hyperbrains.hms.domain.Visit;
import com.hyperbrains.hms.domain.enumeration.ReferralStatus;
import com.hyperbrains.hms.domain.enumeration.ReferralType;
import com.hyperbrains.hms.repository.ReferralRepository;
import com.hyperbrains.hms.repository.UserRepository;
import com.hyperbrains.hms.service.ReferralService;
import com.hyperbrains.hms.service.dto.ReferralDTO;
import com.hyperbrains.hms.service.mapper.ReferralMapper;
import jakarta.persistence.EntityManager;
import java.time.Instant;
import java.util.ArrayList;
import java.util.Random;
import java.util.concurrent.atomic.AtomicLong;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.Pageable;
import org.springframework.http.MediaType;
import com.hyperbrains.hms.security.AuthoritiesConstants;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.transaction.annotation.Transactional;

/**
 * Integration tests for the {@link ReferralResource} REST controller.
 */
@IntegrationTest
@ExtendWith(MockitoExtension.class)
@AutoConfigureMockMvc
// A direct write records a referral without ending the visit's local journey, so it is closed to
// clinical roles. Referring goes through /api/visit-referrals/{visitId}/create; this test covers the
// super-admin escape hatch.
@WithMockUser(authorities = AuthoritiesConstants.SUPER_ADMIN)
class ReferralResourceIT {

    private static final ReferralType DEFAULT_TYPE = ReferralType.INTERNAL;
    private static final ReferralType UPDATED_TYPE = ReferralType.EXTERNAL;

    private static final String DEFAULT_DESTINATION = "AAAAAAAAAA";
    private static final String UPDATED_DESTINATION = "BBBBBBBBBB";

    private static final String DEFAULT_DESTINATION_EMAIL = "AAAAAAAAAA";
    private static final String UPDATED_DESTINATION_EMAIL = "BBBBBBBBBB";

    private static final String DEFAULT_REASON = "AAAAAAAAAA";
    private static final String UPDATED_REASON = "BBBBBBBBBB";

    private static final String DEFAULT_NOTES = "AAAAAAAAAA";
    private static final String UPDATED_NOTES = "BBBBBBBBBB";

    private static final ReferralStatus DEFAULT_STATUS = ReferralStatus.PENDING;
    private static final ReferralStatus UPDATED_STATUS = ReferralStatus.COMPLETED;

    private static final Instant DEFAULT_CREATED_AT = Instant.ofEpochMilli(0L);
    private static final Instant UPDATED_CREATED_AT = Instant.ofEpochMilli(1790236635213L);

    private static final String ENTITY_API_URL = "/api/referrals";
    private static final String ENTITY_API_URL_ID = ENTITY_API_URL + "/{id}";

    private static final Random random = new Random();
    private static final AtomicLong longCount = new AtomicLong(random.nextInt() + 2L * Integer.MAX_VALUE);

    @Autowired
    private ObjectMapper om;

    @Autowired
    private ReferralRepository referralRepository;

    @Autowired
    private UserRepository userRepository;

    @Mock
    private ReferralRepository referralRepositoryMock;

    @Autowired
    private ReferralMapper referralMapper;

    @Mock
    private ReferralService referralServiceMock;

    @Autowired
    private EntityManager em;

    @Autowired
    private MockMvc restReferralMockMvc;

    private Referral referral;

    private Referral insertedReferral;

    /**
     * Create an entity for this test.
     *
     * This is a static method, as tests for other entities might also need it,
     * if they test an entity which requires the current entity.
     */
    public static Referral createEntity(EntityManager em) {
        Referral referral = new Referral()
            .type(DEFAULT_TYPE)
            .destination(DEFAULT_DESTINATION)
            .destinationEmail(DEFAULT_DESTINATION_EMAIL)
            .reason(DEFAULT_REASON)
            .notes(DEFAULT_NOTES)
            .status(DEFAULT_STATUS)
            .createdAt(DEFAULT_CREATED_AT);
        // Add required entity
        Visit visit;
        if (TestUtil.findAll(em, Visit.class).isEmpty()) {
            visit = VisitResourceIT.createEntity(em);
            em.persist(visit);
            em.flush();
        } else {
            visit = TestUtil.findAll(em, Visit.class).get(0);
        }
        referral.setVisit(visit);
        // Add required entity
        User user = UserResourceIT.createEntity();
        em.persist(user);
        em.flush();
        referral.setReferredBy(user);
        return referral;
    }

    /**
     * Create an updated entity for this test.
     *
     * This is a static method, as tests for other entities might also need it,
     * if they test an entity which requires the current entity.
     */
    public static Referral createUpdatedEntity(EntityManager em) {
        Referral updatedReferral = new Referral()
            .type(UPDATED_TYPE)
            .destination(UPDATED_DESTINATION)
            .destinationEmail(UPDATED_DESTINATION_EMAIL)
            .reason(UPDATED_REASON)
            .notes(UPDATED_NOTES)
            .status(UPDATED_STATUS)
            .createdAt(UPDATED_CREATED_AT);
        // Add required entity
        Visit visit;
        if (TestUtil.findAll(em, Visit.class).isEmpty()) {
            visit = VisitResourceIT.createUpdatedEntity(em);
            em.persist(visit);
            em.flush();
        } else {
            visit = TestUtil.findAll(em, Visit.class).get(0);
        }
        updatedReferral.setVisit(visit);
        // Add required entity
        User user = UserResourceIT.createEntity();
        em.persist(user);
        em.flush();
        updatedReferral.setReferredBy(user);
        return updatedReferral;
    }

    @BeforeEach
    void initTest() {
        referral = createEntity(em);
    }

    @AfterEach
    void cleanup() {
        if (insertedReferral != null) {
            referralRepository.delete(insertedReferral);
            insertedReferral = null;
        }
    }

    @Test
    @Transactional
    void createReferral() throws Exception {
        long databaseSizeBeforeCreate = getRepositoryCount();
        // Create the Referral
        ReferralDTO referralDTO = referralMapper.toDto(referral);
        var returnedReferralDTO = om.readValue(
            restReferralMockMvc
                .perform(post(ENTITY_API_URL).contentType(MediaType.APPLICATION_JSON).content(om.writeValueAsBytes(referralDTO)))
                .andExpect(status().isCreated())
                .andReturn()
                .getResponse()
                .getContentAsString(),
            ReferralDTO.class
        );

        // Validate the Referral in the database
        assertIncrementedRepositoryCount(databaseSizeBeforeCreate);
        var returnedReferral = referralMapper.toEntity(returnedReferralDTO);
        assertReferralUpdatableFieldsEquals(returnedReferral, getPersistedReferral(returnedReferral));

        insertedReferral = returnedReferral;
    }

    @Test
    @Transactional
    void createReferralWithExistingId() throws Exception {
        // Create the Referral with an existing ID
        referral.setId(1L);
        ReferralDTO referralDTO = referralMapper.toDto(referral);

        long databaseSizeBeforeCreate = getRepositoryCount();

        // An entity with an existing ID cannot be created, so this API call must fail
        restReferralMockMvc
            .perform(post(ENTITY_API_URL).contentType(MediaType.APPLICATION_JSON).content(om.writeValueAsBytes(referralDTO)))
            .andExpect(status().isBadRequest());

        // Validate the Referral in the database
        assertSameRepositoryCount(databaseSizeBeforeCreate);
    }

    @Test
    @Transactional
    void checkTypeIsRequired() throws Exception {
        long databaseSizeBeforeTest = getRepositoryCount();
        // set the field null
        referral.setType(null);

        // Create the Referral, which fails.
        ReferralDTO referralDTO = referralMapper.toDto(referral);

        restReferralMockMvc
            .perform(post(ENTITY_API_URL).contentType(MediaType.APPLICATION_JSON).content(om.writeValueAsBytes(referralDTO)))
            .andExpect(status().isBadRequest());

        assertSameRepositoryCount(databaseSizeBeforeTest);
    }

    @Test
    @Transactional
    void checkDestinationIsRequired() throws Exception {
        long databaseSizeBeforeTest = getRepositoryCount();
        // set the field null
        referral.setDestination(null);

        // Create the Referral, which fails.
        ReferralDTO referralDTO = referralMapper.toDto(referral);

        restReferralMockMvc
            .perform(post(ENTITY_API_URL).contentType(MediaType.APPLICATION_JSON).content(om.writeValueAsBytes(referralDTO)))
            .andExpect(status().isBadRequest());

        assertSameRepositoryCount(databaseSizeBeforeTest);
    }

    @Test
    @Transactional
    void checkStatusIsRequired() throws Exception {
        long databaseSizeBeforeTest = getRepositoryCount();
        // set the field null
        referral.setStatus(null);

        // Create the Referral, which fails.
        ReferralDTO referralDTO = referralMapper.toDto(referral);

        restReferralMockMvc
            .perform(post(ENTITY_API_URL).contentType(MediaType.APPLICATION_JSON).content(om.writeValueAsBytes(referralDTO)))
            .andExpect(status().isBadRequest());

        assertSameRepositoryCount(databaseSizeBeforeTest);
    }

    @Test
    @Transactional
    void checkCreatedAtIsRequired() throws Exception {
        long databaseSizeBeforeTest = getRepositoryCount();
        // set the field null
        referral.setCreatedAt(null);

        // Create the Referral, which fails.
        ReferralDTO referralDTO = referralMapper.toDto(referral);

        restReferralMockMvc
            .perform(post(ENTITY_API_URL).contentType(MediaType.APPLICATION_JSON).content(om.writeValueAsBytes(referralDTO)))
            .andExpect(status().isBadRequest());

        assertSameRepositoryCount(databaseSizeBeforeTest);
    }

    @Test
    @Transactional
    void getAllReferrals() throws Exception {
        // Initialize the database
        insertedReferral = referralRepository.saveAndFlush(referral);

        // Get all the referralList
        restReferralMockMvc
            .perform(get(ENTITY_API_URL + "?sort=id,desc"))
            .andExpect(status().isOk())
            .andExpect(content().contentType(MediaType.APPLICATION_JSON_VALUE))
            .andExpect(jsonPath("$.[*].id").value(hasItem(referral.getId().intValue())))
            .andExpect(jsonPath("$.[*].type").value(hasItem(DEFAULT_TYPE.toString())))
            .andExpect(jsonPath("$.[*].destination").value(hasItem(DEFAULT_DESTINATION)))
            .andExpect(jsonPath("$.[*].destinationEmail").value(hasItem(DEFAULT_DESTINATION_EMAIL)))
            .andExpect(jsonPath("$.[*].reason").value(hasItem(DEFAULT_REASON)))
            .andExpect(jsonPath("$.[*].notes").value(hasItem(DEFAULT_NOTES)))
            .andExpect(jsonPath("$.[*].status").value(hasItem(DEFAULT_STATUS.toString())))
            .andExpect(jsonPath("$.[*].createdAt").value(hasItem(DEFAULT_CREATED_AT.toString())));
    }

    @SuppressWarnings({ "unchecked" })
    void getAllReferralsWithEagerRelationshipsIsEnabled() throws Exception {
        when(referralServiceMock.findAllWithEagerRelationships(any())).thenReturn(new PageImpl(new ArrayList<>()));

        restReferralMockMvc.perform(get(ENTITY_API_URL + "?eagerload=true")).andExpect(status().isOk());

        verify(referralServiceMock, times(1)).findAllWithEagerRelationships(any());
    }

    @SuppressWarnings({ "unchecked" })
    void getAllReferralsWithEagerRelationshipsIsNotEnabled() throws Exception {
        when(referralServiceMock.findAllWithEagerRelationships(any())).thenReturn(new PageImpl(new ArrayList<>()));

        restReferralMockMvc.perform(get(ENTITY_API_URL + "?eagerload=false")).andExpect(status().isOk());
        verify(referralRepositoryMock, times(1)).findAll(any(Pageable.class));
    }

    @Test
    @Transactional
    void getReferral() throws Exception {
        // Initialize the database
        insertedReferral = referralRepository.saveAndFlush(referral);

        // Get the referral
        restReferralMockMvc
            .perform(get(ENTITY_API_URL_ID, referral.getId()))
            .andExpect(status().isOk())
            .andExpect(content().contentType(MediaType.APPLICATION_JSON_VALUE))
            .andExpect(jsonPath("$.id").value(referral.getId().intValue()))
            .andExpect(jsonPath("$.type").value(DEFAULT_TYPE.toString()))
            .andExpect(jsonPath("$.destination").value(DEFAULT_DESTINATION))
            .andExpect(jsonPath("$.destinationEmail").value(DEFAULT_DESTINATION_EMAIL))
            .andExpect(jsonPath("$.reason").value(DEFAULT_REASON))
            .andExpect(jsonPath("$.notes").value(DEFAULT_NOTES))
            .andExpect(jsonPath("$.status").value(DEFAULT_STATUS.toString()))
            .andExpect(jsonPath("$.createdAt").value(DEFAULT_CREATED_AT.toString()));
    }

    @Test
    @Transactional
    void getNonExistingReferral() throws Exception {
        // Get the referral
        restReferralMockMvc.perform(get(ENTITY_API_URL_ID, Long.MAX_VALUE)).andExpect(status().isNotFound());
    }

    @Test
    @Transactional
    void putExistingReferral() throws Exception {
        // Initialize the database
        insertedReferral = referralRepository.saveAndFlush(referral);

        long databaseSizeBeforeUpdate = getRepositoryCount();

        // Update the referral
        Referral updatedReferral = referralRepository.findById(referral.getId()).orElseThrow();
        // Disconnect from session so that the updates on updatedReferral are not directly saved in db
        em.detach(updatedReferral);
        updatedReferral
            .type(UPDATED_TYPE)
            .destination(UPDATED_DESTINATION)
            .destinationEmail(UPDATED_DESTINATION_EMAIL)
            .reason(UPDATED_REASON)
            .notes(UPDATED_NOTES)
            .status(UPDATED_STATUS)
            .createdAt(UPDATED_CREATED_AT);
        ReferralDTO referralDTO = referralMapper.toDto(updatedReferral);

        restReferralMockMvc
            .perform(
                put(ENTITY_API_URL_ID, referralDTO.getId())
                    .contentType(MediaType.APPLICATION_JSON)
                    .content(om.writeValueAsBytes(referralDTO))
            )
            .andExpect(status().isOk());

        // Validate the Referral in the database
        assertSameRepositoryCount(databaseSizeBeforeUpdate);
        assertPersistedReferralToMatchAllProperties(updatedReferral);
    }

    @Test
    @Transactional
    void putNonExistingReferral() throws Exception {
        long databaseSizeBeforeUpdate = getRepositoryCount();
        referral.setId(longCount.incrementAndGet());

        // Create the Referral
        ReferralDTO referralDTO = referralMapper.toDto(referral);

        // If the entity doesn't have an ID, it will throw BadRequestAlertException
        restReferralMockMvc
            .perform(
                put(ENTITY_API_URL_ID, referralDTO.getId())
                    .contentType(MediaType.APPLICATION_JSON)
                    .content(om.writeValueAsBytes(referralDTO))
            )
            .andExpect(status().isBadRequest());

        // Validate the Referral in the database
        assertSameRepositoryCount(databaseSizeBeforeUpdate);
    }

    @Test
    @Transactional
    void putWithIdMismatchReferral() throws Exception {
        long databaseSizeBeforeUpdate = getRepositoryCount();
        referral.setId(longCount.incrementAndGet());

        // Create the Referral
        ReferralDTO referralDTO = referralMapper.toDto(referral);

        // If url ID doesn't match entity ID, it will throw BadRequestAlertException
        restReferralMockMvc
            .perform(
                put(ENTITY_API_URL_ID, longCount.incrementAndGet())
                    .contentType(MediaType.APPLICATION_JSON)
                    .content(om.writeValueAsBytes(referralDTO))
            )
            .andExpect(status().isBadRequest());

        // Validate the Referral in the database
        assertSameRepositoryCount(databaseSizeBeforeUpdate);
    }

    @Test
    @Transactional
    void putWithMissingIdPathParamReferral() throws Exception {
        long databaseSizeBeforeUpdate = getRepositoryCount();
        referral.setId(longCount.incrementAndGet());

        // Create the Referral
        ReferralDTO referralDTO = referralMapper.toDto(referral);

        // If url ID doesn't match entity ID, it will throw BadRequestAlertException
        restReferralMockMvc
            .perform(put(ENTITY_API_URL).contentType(MediaType.APPLICATION_JSON).content(om.writeValueAsBytes(referralDTO)))
            .andExpect(status().isMethodNotAllowed());

        // Validate the Referral in the database
        assertSameRepositoryCount(databaseSizeBeforeUpdate);
    }

    @Test
    @Transactional
    void partialUpdateReferralWithPatch() throws Exception {
        // Initialize the database
        insertedReferral = referralRepository.saveAndFlush(referral);

        long databaseSizeBeforeUpdate = getRepositoryCount();

        // Update the referral using partial update
        Referral partialUpdatedReferral = new Referral();
        partialUpdatedReferral.setId(referral.getId());

        partialUpdatedReferral.destination(UPDATED_DESTINATION).reason(UPDATED_REASON).notes(UPDATED_NOTES).status(UPDATED_STATUS);

        restReferralMockMvc
            .perform(
                patch(ENTITY_API_URL_ID, partialUpdatedReferral.getId())
                    .contentType("application/merge-patch+json")
                    .content(om.writeValueAsBytes(partialUpdatedReferral))
            )
            .andExpect(status().isOk());

        // Validate the Referral in the database

        assertSameRepositoryCount(databaseSizeBeforeUpdate);
        assertReferralUpdatableFieldsEquals(createUpdateProxyForBean(partialUpdatedReferral, referral), getPersistedReferral(referral));
    }

    @Test
    @Transactional
    void fullUpdateReferralWithPatch() throws Exception {
        // Initialize the database
        insertedReferral = referralRepository.saveAndFlush(referral);

        long databaseSizeBeforeUpdate = getRepositoryCount();

        // Update the referral using partial update
        Referral partialUpdatedReferral = new Referral();
        partialUpdatedReferral.setId(referral.getId());

        partialUpdatedReferral
            .type(UPDATED_TYPE)
            .destination(UPDATED_DESTINATION)
            .destinationEmail(UPDATED_DESTINATION_EMAIL)
            .reason(UPDATED_REASON)
            .notes(UPDATED_NOTES)
            .status(UPDATED_STATUS)
            .createdAt(UPDATED_CREATED_AT);

        restReferralMockMvc
            .perform(
                patch(ENTITY_API_URL_ID, partialUpdatedReferral.getId())
                    .contentType("application/merge-patch+json")
                    .content(om.writeValueAsBytes(partialUpdatedReferral))
            )
            .andExpect(status().isOk());

        // Validate the Referral in the database

        assertSameRepositoryCount(databaseSizeBeforeUpdate);
        assertReferralUpdatableFieldsEquals(partialUpdatedReferral, getPersistedReferral(partialUpdatedReferral));
    }

    @Test
    @Transactional
    void patchNonExistingReferral() throws Exception {
        long databaseSizeBeforeUpdate = getRepositoryCount();
        referral.setId(longCount.incrementAndGet());

        // Create the Referral
        ReferralDTO referralDTO = referralMapper.toDto(referral);

        // If the entity doesn't have an ID, it will throw BadRequestAlertException
        restReferralMockMvc
            .perform(
                patch(ENTITY_API_URL_ID, referralDTO.getId())
                    .contentType("application/merge-patch+json")
                    .content(om.writeValueAsBytes(referralDTO))
            )
            .andExpect(status().isBadRequest());

        // Validate the Referral in the database
        assertSameRepositoryCount(databaseSizeBeforeUpdate);
    }

    @Test
    @Transactional
    void patchWithIdMismatchReferral() throws Exception {
        long databaseSizeBeforeUpdate = getRepositoryCount();
        referral.setId(longCount.incrementAndGet());

        // Create the Referral
        ReferralDTO referralDTO = referralMapper.toDto(referral);

        // If url ID doesn't match entity ID, it will throw BadRequestAlertException
        restReferralMockMvc
            .perform(
                patch(ENTITY_API_URL_ID, longCount.incrementAndGet())
                    .contentType("application/merge-patch+json")
                    .content(om.writeValueAsBytes(referralDTO))
            )
            .andExpect(status().isBadRequest());

        // Validate the Referral in the database
        assertSameRepositoryCount(databaseSizeBeforeUpdate);
    }

    @Test
    @Transactional
    void patchWithMissingIdPathParamReferral() throws Exception {
        long databaseSizeBeforeUpdate = getRepositoryCount();
        referral.setId(longCount.incrementAndGet());

        // Create the Referral
        ReferralDTO referralDTO = referralMapper.toDto(referral);

        // If url ID doesn't match entity ID, it will throw BadRequestAlertException
        restReferralMockMvc
            .perform(patch(ENTITY_API_URL).contentType("application/merge-patch+json").content(om.writeValueAsBytes(referralDTO)))
            .andExpect(status().isMethodNotAllowed());

        // Validate the Referral in the database
        assertSameRepositoryCount(databaseSizeBeforeUpdate);
    }

    @Test
    @Transactional
    void deleteReferral() throws Exception {
        // Initialize the database
        insertedReferral = referralRepository.saveAndFlush(referral);

        long databaseSizeBeforeDelete = getRepositoryCount();

        // Delete the referral
        restReferralMockMvc
            .perform(delete(ENTITY_API_URL_ID, referral.getId()).accept(MediaType.APPLICATION_JSON))
            .andExpect(status().isNoContent());

        // Validate the database contains one less item
        assertDecrementedRepositoryCount(databaseSizeBeforeDelete);
    }

    protected long getRepositoryCount() {
        return referralRepository.count();
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

    protected Referral getPersistedReferral(Referral referral) {
        return referralRepository.findById(referral.getId()).orElseThrow();
    }

    protected void assertPersistedReferralToMatchAllProperties(Referral expectedReferral) {
        assertReferralAllPropertiesEquals(expectedReferral, getPersistedReferral(expectedReferral));
    }

    protected void assertPersistedReferralToMatchUpdatableProperties(Referral expectedReferral) {
        assertReferralAllUpdatablePropertiesEquals(expectedReferral, getPersistedReferral(expectedReferral));
    }
}
