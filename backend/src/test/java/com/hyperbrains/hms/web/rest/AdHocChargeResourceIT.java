package com.hyperbrains.hms.web.rest;

import static com.hyperbrains.hms.domain.AdHocChargeAsserts.*;
import static com.hyperbrains.hms.web.rest.TestUtil.createUpdateProxyForBean;
import static com.hyperbrains.hms.web.rest.TestUtil.sameNumber;
import static org.assertj.core.api.Assertions.assertThat;
import static org.hamcrest.Matchers.hasItem;
import static org.mockito.Mockito.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.hyperbrains.hms.IntegrationTest;
import com.hyperbrains.hms.domain.AdHocCharge;
import com.hyperbrains.hms.domain.Admission;
import com.hyperbrains.hms.domain.User;
import com.hyperbrains.hms.repository.AdHocChargeRepository;
import com.hyperbrains.hms.repository.UserRepository;
import com.hyperbrains.hms.service.AdHocChargeService;
import com.hyperbrains.hms.service.dto.AdHocChargeDTO;
import com.hyperbrains.hms.service.mapper.AdHocChargeMapper;
import jakarta.persistence.EntityManager;
import java.math.BigDecimal;
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
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.transaction.annotation.Transactional;

/**
 * Integration tests for the {@link AdHocChargeResource} REST controller.
 */
@IntegrationTest
@ExtendWith(MockitoExtension.class)
@AutoConfigureMockMvc
@WithMockUser
class AdHocChargeResourceIT {

    private static final String DEFAULT_DESCRIPTION = "AAAAAAAAAA";
    private static final String UPDATED_DESCRIPTION = "BBBBBBBBBB";

    private static final BigDecimal DEFAULT_AMOUNT = new BigDecimal(0);
    private static final BigDecimal UPDATED_AMOUNT = new BigDecimal(1);

    private static final String DEFAULT_REASON = "AAAAAAAAAA";
    private static final String UPDATED_REASON = "BBBBBBBBBB";

    private static final Instant DEFAULT_ADDED_AT = Instant.ofEpochMilli(0L);
    private static final Instant UPDATED_ADDED_AT = Instant.ofEpochMilli(1790236635213L);

    private static final Instant DEFAULT_VOIDED_AT = Instant.ofEpochMilli(0L);
    private static final Instant UPDATED_VOIDED_AT = Instant.ofEpochMilli(1790236635213L);

    private static final String DEFAULT_VOID_REASON = "AAAAAAAAAA";
    private static final String UPDATED_VOID_REASON = "BBBBBBBBBB";

    private static final String ENTITY_API_URL = "/api/ad-hoc-charges";
    private static final String ENTITY_API_URL_ID = ENTITY_API_URL + "/{id}";

    private static final Random random = new Random();
    private static final AtomicLong longCount = new AtomicLong(random.nextInt() + 2L * Integer.MAX_VALUE);

    @Autowired
    private ObjectMapper om;

    @Autowired
    private AdHocChargeRepository adHocChargeRepository;

    @Autowired
    private UserRepository userRepository;

    @Mock
    private AdHocChargeRepository adHocChargeRepositoryMock;

    @Autowired
    private AdHocChargeMapper adHocChargeMapper;

    @Mock
    private AdHocChargeService adHocChargeServiceMock;

    @Autowired
    private EntityManager em;

    @Autowired
    private MockMvc restAdHocChargeMockMvc;

    private AdHocCharge adHocCharge;

    private AdHocCharge insertedAdHocCharge;

    /**
     * Create an entity for this test.
     *
     * This is a static method, as tests for other entities might also need it,
     * if they test an entity which requires the current entity.
     */
    public static AdHocCharge createEntity(EntityManager em) {
        AdHocCharge adHocCharge = new AdHocCharge()
            .description(DEFAULT_DESCRIPTION)
            .amount(DEFAULT_AMOUNT)
            .reason(DEFAULT_REASON)
            .addedAt(DEFAULT_ADDED_AT)
            .voidedAt(DEFAULT_VOIDED_AT)
            .voidReason(DEFAULT_VOID_REASON);
        // Add required entity
        Admission admission;
        if (TestUtil.findAll(em, Admission.class).isEmpty()) {
            admission = AdmissionResourceIT.createEntity(em);
            em.persist(admission);
            em.flush();
        } else {
            admission = TestUtil.findAll(em, Admission.class).get(0);
        }
        adHocCharge.setAdmission(admission);
        // Add required entity
        User user = UserResourceIT.createEntity();
        em.persist(user);
        em.flush();
        adHocCharge.setAddedBy(user);
        return adHocCharge;
    }

    /**
     * Create an updated entity for this test.
     *
     * This is a static method, as tests for other entities might also need it,
     * if they test an entity which requires the current entity.
     */
    public static AdHocCharge createUpdatedEntity(EntityManager em) {
        AdHocCharge updatedAdHocCharge = new AdHocCharge()
            .description(UPDATED_DESCRIPTION)
            .amount(UPDATED_AMOUNT)
            .reason(UPDATED_REASON)
            .addedAt(UPDATED_ADDED_AT)
            .voidedAt(UPDATED_VOIDED_AT)
            .voidReason(UPDATED_VOID_REASON);
        // Add required entity
        Admission admission;
        if (TestUtil.findAll(em, Admission.class).isEmpty()) {
            admission = AdmissionResourceIT.createUpdatedEntity(em);
            em.persist(admission);
            em.flush();
        } else {
            admission = TestUtil.findAll(em, Admission.class).get(0);
        }
        updatedAdHocCharge.setAdmission(admission);
        // Add required entity
        User user = UserResourceIT.createEntity();
        em.persist(user);
        em.flush();
        updatedAdHocCharge.setAddedBy(user);
        return updatedAdHocCharge;
    }

    @BeforeEach
    void initTest() {
        adHocCharge = createEntity(em);
    }

    @AfterEach
    void cleanup() {
        if (insertedAdHocCharge != null) {
            adHocChargeRepository.delete(insertedAdHocCharge);
            insertedAdHocCharge = null;
        }
    }

    @Test
    @Transactional
    void createAdHocCharge() throws Exception {
        long databaseSizeBeforeCreate = getRepositoryCount();
        // Create the AdHocCharge
        AdHocChargeDTO adHocChargeDTO = adHocChargeMapper.toDto(adHocCharge);
        var returnedAdHocChargeDTO = om.readValue(
            restAdHocChargeMockMvc
                .perform(post(ENTITY_API_URL).contentType(MediaType.APPLICATION_JSON).content(om.writeValueAsBytes(adHocChargeDTO)))
                .andExpect(status().isCreated())
                .andReturn()
                .getResponse()
                .getContentAsString(),
            AdHocChargeDTO.class
        );

        // Validate the AdHocCharge in the database
        assertIncrementedRepositoryCount(databaseSizeBeforeCreate);
        var returnedAdHocCharge = adHocChargeMapper.toEntity(returnedAdHocChargeDTO);
        assertAdHocChargeUpdatableFieldsEquals(returnedAdHocCharge, getPersistedAdHocCharge(returnedAdHocCharge));

        insertedAdHocCharge = returnedAdHocCharge;
    }

    @Test
    @Transactional
    void createAdHocChargeWithExistingId() throws Exception {
        // Create the AdHocCharge with an existing ID
        adHocCharge.setId(1L);
        AdHocChargeDTO adHocChargeDTO = adHocChargeMapper.toDto(adHocCharge);

        long databaseSizeBeforeCreate = getRepositoryCount();

        // An entity with an existing ID cannot be created, so this API call must fail
        restAdHocChargeMockMvc
            .perform(post(ENTITY_API_URL).contentType(MediaType.APPLICATION_JSON).content(om.writeValueAsBytes(adHocChargeDTO)))
            .andExpect(status().isBadRequest());

        // Validate the AdHocCharge in the database
        assertSameRepositoryCount(databaseSizeBeforeCreate);
    }

    @Test
    @Transactional
    void checkDescriptionIsRequired() throws Exception {
        long databaseSizeBeforeTest = getRepositoryCount();
        // set the field null
        adHocCharge.setDescription(null);

        // Create the AdHocCharge, which fails.
        AdHocChargeDTO adHocChargeDTO = adHocChargeMapper.toDto(adHocCharge);

        restAdHocChargeMockMvc
            .perform(post(ENTITY_API_URL).contentType(MediaType.APPLICATION_JSON).content(om.writeValueAsBytes(adHocChargeDTO)))
            .andExpect(status().isBadRequest());

        assertSameRepositoryCount(databaseSizeBeforeTest);
    }

    @Test
    @Transactional
    void checkAmountIsRequired() throws Exception {
        long databaseSizeBeforeTest = getRepositoryCount();
        // set the field null
        adHocCharge.setAmount(null);

        // Create the AdHocCharge, which fails.
        AdHocChargeDTO adHocChargeDTO = adHocChargeMapper.toDto(adHocCharge);

        restAdHocChargeMockMvc
            .perform(post(ENTITY_API_URL).contentType(MediaType.APPLICATION_JSON).content(om.writeValueAsBytes(adHocChargeDTO)))
            .andExpect(status().isBadRequest());

        assertSameRepositoryCount(databaseSizeBeforeTest);
    }

    @Test
    @Transactional
    void checkReasonIsRequired() throws Exception {
        long databaseSizeBeforeTest = getRepositoryCount();
        // set the field null
        adHocCharge.setReason(null);

        // Create the AdHocCharge, which fails.
        AdHocChargeDTO adHocChargeDTO = adHocChargeMapper.toDto(adHocCharge);

        restAdHocChargeMockMvc
            .perform(post(ENTITY_API_URL).contentType(MediaType.APPLICATION_JSON).content(om.writeValueAsBytes(adHocChargeDTO)))
            .andExpect(status().isBadRequest());

        assertSameRepositoryCount(databaseSizeBeforeTest);
    }

    @Test
    @Transactional
    void checkAddedAtIsRequired() throws Exception {
        long databaseSizeBeforeTest = getRepositoryCount();
        // set the field null
        adHocCharge.setAddedAt(null);

        // Create the AdHocCharge, which fails.
        AdHocChargeDTO adHocChargeDTO = adHocChargeMapper.toDto(adHocCharge);

        restAdHocChargeMockMvc
            .perform(post(ENTITY_API_URL).contentType(MediaType.APPLICATION_JSON).content(om.writeValueAsBytes(adHocChargeDTO)))
            .andExpect(status().isBadRequest());

        assertSameRepositoryCount(databaseSizeBeforeTest);
    }

    @Test
    @Transactional
    void getAllAdHocCharges() throws Exception {
        // Initialize the database
        insertedAdHocCharge = adHocChargeRepository.saveAndFlush(adHocCharge);

        // Get all the adHocChargeList
        restAdHocChargeMockMvc
            .perform(get(ENTITY_API_URL + "?sort=id,desc"))
            .andExpect(status().isOk())
            .andExpect(content().contentType(MediaType.APPLICATION_JSON_VALUE))
            .andExpect(jsonPath("$.[*].id").value(hasItem(adHocCharge.getId().intValue())))
            .andExpect(jsonPath("$.[*].description").value(hasItem(DEFAULT_DESCRIPTION)))
            .andExpect(jsonPath("$.[*].amount").value(hasItem(sameNumber(DEFAULT_AMOUNT))))
            .andExpect(jsonPath("$.[*].reason").value(hasItem(DEFAULT_REASON)))
            .andExpect(jsonPath("$.[*].addedAt").value(hasItem(DEFAULT_ADDED_AT.toString())))
            .andExpect(jsonPath("$.[*].voidedAt").value(hasItem(DEFAULT_VOIDED_AT.toString())))
            .andExpect(jsonPath("$.[*].voidReason").value(hasItem(DEFAULT_VOID_REASON)));
    }

    @SuppressWarnings({ "unchecked" })
    void getAllAdHocChargesWithEagerRelationshipsIsEnabled() throws Exception {
        when(adHocChargeServiceMock.findAllWithEagerRelationships(any())).thenReturn(new PageImpl(new ArrayList<>()));

        restAdHocChargeMockMvc.perform(get(ENTITY_API_URL + "?eagerload=true")).andExpect(status().isOk());

        verify(adHocChargeServiceMock, times(1)).findAllWithEagerRelationships(any());
    }

    @SuppressWarnings({ "unchecked" })
    void getAllAdHocChargesWithEagerRelationshipsIsNotEnabled() throws Exception {
        when(adHocChargeServiceMock.findAllWithEagerRelationships(any())).thenReturn(new PageImpl(new ArrayList<>()));

        restAdHocChargeMockMvc.perform(get(ENTITY_API_URL + "?eagerload=false")).andExpect(status().isOk());
        verify(adHocChargeRepositoryMock, times(1)).findAll(any(Pageable.class));
    }

    @Test
    @Transactional
    void getAdHocCharge() throws Exception {
        // Initialize the database
        insertedAdHocCharge = adHocChargeRepository.saveAndFlush(adHocCharge);

        // Get the adHocCharge
        restAdHocChargeMockMvc
            .perform(get(ENTITY_API_URL_ID, adHocCharge.getId()))
            .andExpect(status().isOk())
            .andExpect(content().contentType(MediaType.APPLICATION_JSON_VALUE))
            .andExpect(jsonPath("$.id").value(adHocCharge.getId().intValue()))
            .andExpect(jsonPath("$.description").value(DEFAULT_DESCRIPTION))
            .andExpect(jsonPath("$.amount").value(sameNumber(DEFAULT_AMOUNT)))
            .andExpect(jsonPath("$.reason").value(DEFAULT_REASON))
            .andExpect(jsonPath("$.addedAt").value(DEFAULT_ADDED_AT.toString()))
            .andExpect(jsonPath("$.voidedAt").value(DEFAULT_VOIDED_AT.toString()))
            .andExpect(jsonPath("$.voidReason").value(DEFAULT_VOID_REASON));
    }

    @Test
    @Transactional
    void getNonExistingAdHocCharge() throws Exception {
        // Get the adHocCharge
        restAdHocChargeMockMvc.perform(get(ENTITY_API_URL_ID, Long.MAX_VALUE)).andExpect(status().isNotFound());
    }

    @Test
    @Transactional
    void putExistingAdHocCharge() throws Exception {
        // Initialize the database
        insertedAdHocCharge = adHocChargeRepository.saveAndFlush(adHocCharge);

        long databaseSizeBeforeUpdate = getRepositoryCount();

        // Update the adHocCharge
        AdHocCharge updatedAdHocCharge = adHocChargeRepository.findById(adHocCharge.getId()).orElseThrow();
        // Disconnect from session so that the updates on updatedAdHocCharge are not directly saved in db
        em.detach(updatedAdHocCharge);
        updatedAdHocCharge
            .description(UPDATED_DESCRIPTION)
            .amount(UPDATED_AMOUNT)
            .reason(UPDATED_REASON)
            .addedAt(UPDATED_ADDED_AT)
            .voidedAt(UPDATED_VOIDED_AT)
            .voidReason(UPDATED_VOID_REASON);
        AdHocChargeDTO adHocChargeDTO = adHocChargeMapper.toDto(updatedAdHocCharge);

        restAdHocChargeMockMvc
            .perform(
                put(ENTITY_API_URL_ID, adHocChargeDTO.getId())
                    .contentType(MediaType.APPLICATION_JSON)
                    .content(om.writeValueAsBytes(adHocChargeDTO))
            )
            .andExpect(status().isOk());

        // Validate the AdHocCharge in the database
        assertSameRepositoryCount(databaseSizeBeforeUpdate);
        assertPersistedAdHocChargeToMatchAllProperties(updatedAdHocCharge);
    }

    @Test
    @Transactional
    void putNonExistingAdHocCharge() throws Exception {
        long databaseSizeBeforeUpdate = getRepositoryCount();
        adHocCharge.setId(longCount.incrementAndGet());

        // Create the AdHocCharge
        AdHocChargeDTO adHocChargeDTO = adHocChargeMapper.toDto(adHocCharge);

        // If the entity doesn't have an ID, it will throw BadRequestAlertException
        restAdHocChargeMockMvc
            .perform(
                put(ENTITY_API_URL_ID, adHocChargeDTO.getId())
                    .contentType(MediaType.APPLICATION_JSON)
                    .content(om.writeValueAsBytes(adHocChargeDTO))
            )
            .andExpect(status().isBadRequest());

        // Validate the AdHocCharge in the database
        assertSameRepositoryCount(databaseSizeBeforeUpdate);
    }

    @Test
    @Transactional
    void putWithIdMismatchAdHocCharge() throws Exception {
        long databaseSizeBeforeUpdate = getRepositoryCount();
        adHocCharge.setId(longCount.incrementAndGet());

        // Create the AdHocCharge
        AdHocChargeDTO adHocChargeDTO = adHocChargeMapper.toDto(adHocCharge);

        // If url ID doesn't match entity ID, it will throw BadRequestAlertException
        restAdHocChargeMockMvc
            .perform(
                put(ENTITY_API_URL_ID, longCount.incrementAndGet())
                    .contentType(MediaType.APPLICATION_JSON)
                    .content(om.writeValueAsBytes(adHocChargeDTO))
            )
            .andExpect(status().isBadRequest());

        // Validate the AdHocCharge in the database
        assertSameRepositoryCount(databaseSizeBeforeUpdate);
    }

    @Test
    @Transactional
    void putWithMissingIdPathParamAdHocCharge() throws Exception {
        long databaseSizeBeforeUpdate = getRepositoryCount();
        adHocCharge.setId(longCount.incrementAndGet());

        // Create the AdHocCharge
        AdHocChargeDTO adHocChargeDTO = adHocChargeMapper.toDto(adHocCharge);

        // If url ID doesn't match entity ID, it will throw BadRequestAlertException
        restAdHocChargeMockMvc
            .perform(put(ENTITY_API_URL).contentType(MediaType.APPLICATION_JSON).content(om.writeValueAsBytes(adHocChargeDTO)))
            .andExpect(status().isMethodNotAllowed());

        // Validate the AdHocCharge in the database
        assertSameRepositoryCount(databaseSizeBeforeUpdate);
    }

    @Test
    @Transactional
    void partialUpdateAdHocChargeWithPatch() throws Exception {
        // Initialize the database
        insertedAdHocCharge = adHocChargeRepository.saveAndFlush(adHocCharge);

        long databaseSizeBeforeUpdate = getRepositoryCount();

        // Update the adHocCharge using partial update
        AdHocCharge partialUpdatedAdHocCharge = new AdHocCharge();
        partialUpdatedAdHocCharge.setId(adHocCharge.getId());

        partialUpdatedAdHocCharge.amount(UPDATED_AMOUNT).addedAt(UPDATED_ADDED_AT).voidedAt(UPDATED_VOIDED_AT);

        restAdHocChargeMockMvc
            .perform(
                patch(ENTITY_API_URL_ID, partialUpdatedAdHocCharge.getId())
                    .contentType("application/merge-patch+json")
                    .content(om.writeValueAsBytes(partialUpdatedAdHocCharge))
            )
            .andExpect(status().isOk());

        // Validate the AdHocCharge in the database

        assertSameRepositoryCount(databaseSizeBeforeUpdate);
        assertAdHocChargeUpdatableFieldsEquals(
            createUpdateProxyForBean(partialUpdatedAdHocCharge, adHocCharge),
            getPersistedAdHocCharge(adHocCharge)
        );
    }

    @Test
    @Transactional
    void fullUpdateAdHocChargeWithPatch() throws Exception {
        // Initialize the database
        insertedAdHocCharge = adHocChargeRepository.saveAndFlush(adHocCharge);

        long databaseSizeBeforeUpdate = getRepositoryCount();

        // Update the adHocCharge using partial update
        AdHocCharge partialUpdatedAdHocCharge = new AdHocCharge();
        partialUpdatedAdHocCharge.setId(adHocCharge.getId());

        partialUpdatedAdHocCharge
            .description(UPDATED_DESCRIPTION)
            .amount(UPDATED_AMOUNT)
            .reason(UPDATED_REASON)
            .addedAt(UPDATED_ADDED_AT)
            .voidedAt(UPDATED_VOIDED_AT)
            .voidReason(UPDATED_VOID_REASON);

        restAdHocChargeMockMvc
            .perform(
                patch(ENTITY_API_URL_ID, partialUpdatedAdHocCharge.getId())
                    .contentType("application/merge-patch+json")
                    .content(om.writeValueAsBytes(partialUpdatedAdHocCharge))
            )
            .andExpect(status().isOk());

        // Validate the AdHocCharge in the database

        assertSameRepositoryCount(databaseSizeBeforeUpdate);
        assertAdHocChargeUpdatableFieldsEquals(partialUpdatedAdHocCharge, getPersistedAdHocCharge(partialUpdatedAdHocCharge));
    }

    @Test
    @Transactional
    void patchNonExistingAdHocCharge() throws Exception {
        long databaseSizeBeforeUpdate = getRepositoryCount();
        adHocCharge.setId(longCount.incrementAndGet());

        // Create the AdHocCharge
        AdHocChargeDTO adHocChargeDTO = adHocChargeMapper.toDto(adHocCharge);

        // If the entity doesn't have an ID, it will throw BadRequestAlertException
        restAdHocChargeMockMvc
            .perform(
                patch(ENTITY_API_URL_ID, adHocChargeDTO.getId())
                    .contentType("application/merge-patch+json")
                    .content(om.writeValueAsBytes(adHocChargeDTO))
            )
            .andExpect(status().isBadRequest());

        // Validate the AdHocCharge in the database
        assertSameRepositoryCount(databaseSizeBeforeUpdate);
    }

    @Test
    @Transactional
    void patchWithIdMismatchAdHocCharge() throws Exception {
        long databaseSizeBeforeUpdate = getRepositoryCount();
        adHocCharge.setId(longCount.incrementAndGet());

        // Create the AdHocCharge
        AdHocChargeDTO adHocChargeDTO = adHocChargeMapper.toDto(adHocCharge);

        // If url ID doesn't match entity ID, it will throw BadRequestAlertException
        restAdHocChargeMockMvc
            .perform(
                patch(ENTITY_API_URL_ID, longCount.incrementAndGet())
                    .contentType("application/merge-patch+json")
                    .content(om.writeValueAsBytes(adHocChargeDTO))
            )
            .andExpect(status().isBadRequest());

        // Validate the AdHocCharge in the database
        assertSameRepositoryCount(databaseSizeBeforeUpdate);
    }

    @Test
    @Transactional
    void patchWithMissingIdPathParamAdHocCharge() throws Exception {
        long databaseSizeBeforeUpdate = getRepositoryCount();
        adHocCharge.setId(longCount.incrementAndGet());

        // Create the AdHocCharge
        AdHocChargeDTO adHocChargeDTO = adHocChargeMapper.toDto(adHocCharge);

        // If url ID doesn't match entity ID, it will throw BadRequestAlertException
        restAdHocChargeMockMvc
            .perform(patch(ENTITY_API_URL).contentType("application/merge-patch+json").content(om.writeValueAsBytes(adHocChargeDTO)))
            .andExpect(status().isMethodNotAllowed());

        // Validate the AdHocCharge in the database
        assertSameRepositoryCount(databaseSizeBeforeUpdate);
    }

    @Test
    @Transactional
    void deleteAdHocCharge() throws Exception {
        // Initialize the database
        insertedAdHocCharge = adHocChargeRepository.saveAndFlush(adHocCharge);

        long databaseSizeBeforeDelete = getRepositoryCount();

        // Delete the adHocCharge
        restAdHocChargeMockMvc
            .perform(delete(ENTITY_API_URL_ID, adHocCharge.getId()).accept(MediaType.APPLICATION_JSON))
            .andExpect(status().isNoContent());

        // Validate the database contains one less item
        assertDecrementedRepositoryCount(databaseSizeBeforeDelete);
    }

    protected long getRepositoryCount() {
        return adHocChargeRepository.count();
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

    protected AdHocCharge getPersistedAdHocCharge(AdHocCharge adHocCharge) {
        return adHocChargeRepository.findById(adHocCharge.getId()).orElseThrow();
    }

    protected void assertPersistedAdHocChargeToMatchAllProperties(AdHocCharge expectedAdHocCharge) {
        assertAdHocChargeAllPropertiesEquals(expectedAdHocCharge, getPersistedAdHocCharge(expectedAdHocCharge));
    }

    protected void assertPersistedAdHocChargeToMatchUpdatableProperties(AdHocCharge expectedAdHocCharge) {
        assertAdHocChargeAllUpdatablePropertiesEquals(expectedAdHocCharge, getPersistedAdHocCharge(expectedAdHocCharge));
    }
}
