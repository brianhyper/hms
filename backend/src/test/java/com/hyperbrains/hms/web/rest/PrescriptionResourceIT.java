package com.hyperbrains.hms.web.rest;

import static com.hyperbrains.hms.domain.PrescriptionAsserts.*;
import static com.hyperbrains.hms.web.rest.TestUtil.createUpdateProxyForBean;
import static org.assertj.core.api.Assertions.assertThat;
import static org.hamcrest.Matchers.hasItem;
import static org.mockito.Mockito.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.hyperbrains.hms.IntegrationTest;
import com.hyperbrains.hms.domain.Prescription;
import com.hyperbrains.hms.domain.enumeration.PrescriptionSource;
import com.hyperbrains.hms.domain.enumeration.PrescriptionStatus;
import com.hyperbrains.hms.repository.PrescriptionRepository;
import com.hyperbrains.hms.repository.UserRepository;
import com.hyperbrains.hms.service.PrescriptionService;
import com.hyperbrains.hms.service.dto.PrescriptionDTO;
import com.hyperbrains.hms.service.mapper.PrescriptionMapper;
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
 * Integration tests for the {@link PrescriptionResource} REST controller.
 */
@IntegrationTest
@ExtendWith(MockitoExtension.class)
@AutoConfigureMockMvc
// Writing a prescription straight to this endpoint skips reserving stock and raising the charge, so
// it is closed to clinical roles. Prescribing goes through /api/visit-prescriptions/{visitId}/place;
// this test covers the super-admin escape hatch.
@WithMockUser(authorities = AuthoritiesConstants.SUPER_ADMIN)
class PrescriptionResourceIT {

    private static final PrescriptionSource DEFAULT_SOURCE = PrescriptionSource.INTERNAL;
    private static final PrescriptionSource UPDATED_SOURCE = PrescriptionSource.EXTERNAL;

    private static final String DEFAULT_PRESCRIBING_SOURCE = "AAAAAAAAAA";
    private static final String UPDATED_PRESCRIBING_SOURCE = "BBBBBBBBBB";

    private static final PrescriptionStatus DEFAULT_STATUS = PrescriptionStatus.PENDING;
    private static final PrescriptionStatus UPDATED_STATUS = PrescriptionStatus.READY_FOR_DISPENSE;

    private static final String ENTITY_API_URL = "/api/prescriptions";
    private static final String ENTITY_API_URL_ID = ENTITY_API_URL + "/{id}";

    private static final Random random = new Random();
    private static final AtomicLong longCount = new AtomicLong(random.nextInt() + 2L * Integer.MAX_VALUE);

    @Autowired
    private ObjectMapper om;

    @Autowired
    private PrescriptionRepository prescriptionRepository;

    @Autowired
    private UserRepository userRepository;

    @Mock
    private PrescriptionRepository prescriptionRepositoryMock;

    @Autowired
    private PrescriptionMapper prescriptionMapper;

    @Mock
    private PrescriptionService prescriptionServiceMock;

    @Autowired
    private EntityManager em;

    @Autowired
    private MockMvc restPrescriptionMockMvc;

    private Prescription prescription;

    private Prescription insertedPrescription;

    /**
     * Create an entity for this test.
     *
     * This is a static method, as tests for other entities might also need it,
     * if they test an entity which requires the current entity.
     */
    public static Prescription createEntity() {
        return new Prescription()
            .source(DEFAULT_SOURCE)
            .prescribingSource(DEFAULT_PRESCRIBING_SOURCE)
            .status(DEFAULT_STATUS)
            .createdAt(Instant.parse("2026-09-24T09:00:00Z"));
    }

    /**
     * Create an updated entity for this test.
     *
     * This is a static method, as tests for other entities might also need it,
     * if they test an entity which requires the current entity.
     */
    public static Prescription createUpdatedEntity() {
        return new Prescription()
            .source(UPDATED_SOURCE)
            .prescribingSource(UPDATED_PRESCRIBING_SOURCE)
            .status(UPDATED_STATUS)
            .createdAt(Instant.parse("2026-09-24T10:00:00Z"));
    }

    @BeforeEach
    void initTest() {
        prescription = createEntity();
    }

    @AfterEach
    void cleanup() {
        if (insertedPrescription != null) {
            // deleteById re-reads the row first so the optimistic-locking check uses the current
            // version. The cached entity is stale whenever a test modified it (Prescription carries @Version).
            prescriptionRepository.deleteById(insertedPrescription.getId());
            insertedPrescription = null;
        }
    }

    @Test
    @Transactional
    void createPrescription() throws Exception {
        long databaseSizeBeforeCreate = getRepositoryCount();
        // Create the Prescription
        PrescriptionDTO prescriptionDTO = prescriptionMapper.toDto(prescription);
        var returnedPrescriptionDTO = om.readValue(
            restPrescriptionMockMvc
                .perform(post(ENTITY_API_URL).contentType(MediaType.APPLICATION_JSON).content(om.writeValueAsBytes(prescriptionDTO)))
                .andExpect(status().isCreated())
                .andReturn()
                .getResponse()
                .getContentAsString(),
            PrescriptionDTO.class
        );

        // Validate the Prescription in the database
        assertIncrementedRepositoryCount(databaseSizeBeforeCreate);
        var returnedPrescription = prescriptionMapper.toEntity(returnedPrescriptionDTO);
        assertPrescriptionUpdatableFieldsEquals(returnedPrescription, getPersistedPrescription(returnedPrescription));

        insertedPrescription = returnedPrescription;
    }

    @Test
    @Transactional
    void createPrescriptionWithExistingId() throws Exception {
        // Create the Prescription with an existing ID
        prescription.setId(1L);
        PrescriptionDTO prescriptionDTO = prescriptionMapper.toDto(prescription);

        long databaseSizeBeforeCreate = getRepositoryCount();

        // An entity with an existing ID cannot be created, so this API call must fail
        restPrescriptionMockMvc
            .perform(post(ENTITY_API_URL).contentType(MediaType.APPLICATION_JSON).content(om.writeValueAsBytes(prescriptionDTO)))
            .andExpect(status().isBadRequest());

        // Validate the Prescription in the database
        assertSameRepositoryCount(databaseSizeBeforeCreate);
    }

    @Test
    @Transactional
    void checkSourceIsRequired() throws Exception {
        long databaseSizeBeforeTest = getRepositoryCount();
        // set the field null
        prescription.setSource(null);

        // Create the Prescription, which fails.
        PrescriptionDTO prescriptionDTO = prescriptionMapper.toDto(prescription);

        restPrescriptionMockMvc
            .perform(post(ENTITY_API_URL).contentType(MediaType.APPLICATION_JSON).content(om.writeValueAsBytes(prescriptionDTO)))
            .andExpect(status().isBadRequest());

        assertSameRepositoryCount(databaseSizeBeforeTest);
    }

    @Test
    @Transactional
    void checkStatusIsRequired() throws Exception {
        long databaseSizeBeforeTest = getRepositoryCount();
        // set the field null
        prescription.setStatus(null);

        // Create the Prescription, which fails.
        PrescriptionDTO prescriptionDTO = prescriptionMapper.toDto(prescription);

        restPrescriptionMockMvc
            .perform(post(ENTITY_API_URL).contentType(MediaType.APPLICATION_JSON).content(om.writeValueAsBytes(prescriptionDTO)))
            .andExpect(status().isBadRequest());

        assertSameRepositoryCount(databaseSizeBeforeTest);
    }

    @Test
    @Transactional
    void getAllPrescriptions() throws Exception {
        // Initialize the database
        insertedPrescription = prescriptionRepository.saveAndFlush(prescription);

        // Get all the prescriptionList
        restPrescriptionMockMvc
            .perform(get(ENTITY_API_URL + "?sort=id,desc"))
            .andExpect(status().isOk())
            .andExpect(content().contentType(MediaType.APPLICATION_JSON_VALUE))
            .andExpect(jsonPath("$.[*].id").value(hasItem(prescription.getId().intValue())))
            .andExpect(jsonPath("$.[*].source").value(hasItem(DEFAULT_SOURCE.toString())))
            .andExpect(jsonPath("$.[*].prescribingSource").value(hasItem(DEFAULT_PRESCRIBING_SOURCE)))
            .andExpect(jsonPath("$.[*].status").value(hasItem(DEFAULT_STATUS.toString())));
    }

    @SuppressWarnings({ "unchecked" })
    void getAllPrescriptionsWithEagerRelationshipsIsEnabled() throws Exception {
        when(prescriptionServiceMock.findAllWithEagerRelationships(any())).thenReturn(new PageImpl(new ArrayList<>()));

        restPrescriptionMockMvc.perform(get(ENTITY_API_URL + "?eagerload=true")).andExpect(status().isOk());

        verify(prescriptionServiceMock, times(1)).findAllWithEagerRelationships(any());
    }

    @SuppressWarnings({ "unchecked" })
    void getAllPrescriptionsWithEagerRelationshipsIsNotEnabled() throws Exception {
        when(prescriptionServiceMock.findAllWithEagerRelationships(any())).thenReturn(new PageImpl(new ArrayList<>()));

        restPrescriptionMockMvc.perform(get(ENTITY_API_URL + "?eagerload=false")).andExpect(status().isOk());
        verify(prescriptionRepositoryMock, times(1)).findAll(any(Pageable.class));
    }

    @Test
    @Transactional
    void getPrescription() throws Exception {
        // Initialize the database
        insertedPrescription = prescriptionRepository.saveAndFlush(prescription);

        // Get the prescription
        restPrescriptionMockMvc
            .perform(get(ENTITY_API_URL_ID, prescription.getId()))
            .andExpect(status().isOk())
            .andExpect(content().contentType(MediaType.APPLICATION_JSON_VALUE))
            .andExpect(jsonPath("$.id").value(prescription.getId().intValue()))
            .andExpect(jsonPath("$.source").value(DEFAULT_SOURCE.toString()))
            .andExpect(jsonPath("$.prescribingSource").value(DEFAULT_PRESCRIBING_SOURCE))
            .andExpect(jsonPath("$.status").value(DEFAULT_STATUS.toString()));
    }

    @Test
    @Transactional
    void getNonExistingPrescription() throws Exception {
        // Get the prescription
        restPrescriptionMockMvc.perform(get(ENTITY_API_URL_ID, Long.MAX_VALUE)).andExpect(status().isNotFound());
    }

    @Test
    @Transactional
    void putExistingPrescription() throws Exception {
        // Initialize the database
        insertedPrescription = prescriptionRepository.saveAndFlush(prescription);

        long databaseSizeBeforeUpdate = getRepositoryCount();

        // Update the prescription
        Prescription updatedPrescription = prescriptionRepository.findById(prescription.getId()).orElseThrow();
        // Disconnect from session so that the updates on updatedPrescription are not directly saved in db
        em.detach(updatedPrescription);
        updatedPrescription.source(UPDATED_SOURCE).prescribingSource(UPDATED_PRESCRIBING_SOURCE).status(UPDATED_STATUS);
        PrescriptionDTO prescriptionDTO = prescriptionMapper.toDto(updatedPrescription);

        restPrescriptionMockMvc
            .perform(
                put(ENTITY_API_URL_ID, prescriptionDTO.getId())
                    .contentType(MediaType.APPLICATION_JSON)
                    .content(om.writeValueAsBytes(prescriptionDTO))
            )
            .andExpect(status().isOk());

        // Validate the Prescription in the database
        assertSameRepositoryCount(databaseSizeBeforeUpdate);
        assertPersistedPrescriptionToMatchAllProperties(updatedPrescription);
    }

    @Test
    @Transactional
    void putNonExistingPrescription() throws Exception {
        long databaseSizeBeforeUpdate = getRepositoryCount();
        prescription.setId(longCount.incrementAndGet());

        // Create the Prescription
        PrescriptionDTO prescriptionDTO = prescriptionMapper.toDto(prescription);

        // If the entity doesn't have an ID, it will throw BadRequestAlertException
        restPrescriptionMockMvc
            .perform(
                put(ENTITY_API_URL_ID, prescriptionDTO.getId())
                    .contentType(MediaType.APPLICATION_JSON)
                    .content(om.writeValueAsBytes(prescriptionDTO))
            )
            .andExpect(status().isBadRequest());

        // Validate the Prescription in the database
        assertSameRepositoryCount(databaseSizeBeforeUpdate);
    }

    @Test
    @Transactional
    void putWithIdMismatchPrescription() throws Exception {
        long databaseSizeBeforeUpdate = getRepositoryCount();
        prescription.setId(longCount.incrementAndGet());

        // Create the Prescription
        PrescriptionDTO prescriptionDTO = prescriptionMapper.toDto(prescription);

        // If url ID doesn't match entity ID, it will throw BadRequestAlertException
        restPrescriptionMockMvc
            .perform(
                put(ENTITY_API_URL_ID, longCount.incrementAndGet())
                    .contentType(MediaType.APPLICATION_JSON)
                    .content(om.writeValueAsBytes(prescriptionDTO))
            )
            .andExpect(status().isBadRequest());

        // Validate the Prescription in the database
        assertSameRepositoryCount(databaseSizeBeforeUpdate);
    }

    @Test
    @Transactional
    void putWithMissingIdPathParamPrescription() throws Exception {
        long databaseSizeBeforeUpdate = getRepositoryCount();
        prescription.setId(longCount.incrementAndGet());

        // Create the Prescription
        PrescriptionDTO prescriptionDTO = prescriptionMapper.toDto(prescription);

        // If url ID doesn't match entity ID, it will throw BadRequestAlertException
        restPrescriptionMockMvc
            .perform(put(ENTITY_API_URL).contentType(MediaType.APPLICATION_JSON).content(om.writeValueAsBytes(prescriptionDTO)))
            .andExpect(status().isMethodNotAllowed());

        // Validate the Prescription in the database
        assertSameRepositoryCount(databaseSizeBeforeUpdate);
    }

    @Test
    @Transactional
    void partialUpdatePrescriptionWithPatch() throws Exception {
        // Initialize the database
        insertedPrescription = prescriptionRepository.saveAndFlush(prescription);

        long databaseSizeBeforeUpdate = getRepositoryCount();

        // Update the prescription using partial update
        Prescription partialUpdatedPrescription = new Prescription();
        partialUpdatedPrescription.setId(prescription.getId());

        partialUpdatedPrescription.status(UPDATED_STATUS);

        restPrescriptionMockMvc
            .perform(
                patch(ENTITY_API_URL_ID, partialUpdatedPrescription.getId())
                    .contentType("application/merge-patch+json")
                    .content(om.writeValueAsBytes(partialUpdatedPrescription))
            )
            .andExpect(status().isOk());

        // Validate the Prescription in the database

        assertSameRepositoryCount(databaseSizeBeforeUpdate);
        assertPrescriptionUpdatableFieldsEquals(
            createUpdateProxyForBean(partialUpdatedPrescription, prescription),
            getPersistedPrescription(prescription)
        );
    }

    @Test
    @Transactional
    void fullUpdatePrescriptionWithPatch() throws Exception {
        // Initialize the database
        insertedPrescription = prescriptionRepository.saveAndFlush(prescription);

        long databaseSizeBeforeUpdate = getRepositoryCount();

        // Update the prescription using partial update
        Prescription partialUpdatedPrescription = new Prescription();
        partialUpdatedPrescription.setId(prescription.getId());

        partialUpdatedPrescription.source(UPDATED_SOURCE).prescribingSource(UPDATED_PRESCRIBING_SOURCE).status(UPDATED_STATUS);

        restPrescriptionMockMvc
            .perform(
                patch(ENTITY_API_URL_ID, partialUpdatedPrescription.getId())
                    .contentType("application/merge-patch+json")
                    .content(om.writeValueAsBytes(partialUpdatedPrescription))
            )
            .andExpect(status().isOk());

        // Validate the Prescription in the database

        assertSameRepositoryCount(databaseSizeBeforeUpdate);
        assertPrescriptionUpdatableFieldsEquals(partialUpdatedPrescription, getPersistedPrescription(partialUpdatedPrescription));
    }

    @Test
    @Transactional
    void patchNonExistingPrescription() throws Exception {
        long databaseSizeBeforeUpdate = getRepositoryCount();
        prescription.setId(longCount.incrementAndGet());

        // Create the Prescription
        PrescriptionDTO prescriptionDTO = prescriptionMapper.toDto(prescription);

        // If the entity doesn't have an ID, it will throw BadRequestAlertException
        restPrescriptionMockMvc
            .perform(
                patch(ENTITY_API_URL_ID, prescriptionDTO.getId())
                    .contentType("application/merge-patch+json")
                    .content(om.writeValueAsBytes(prescriptionDTO))
            )
            .andExpect(status().isBadRequest());

        // Validate the Prescription in the database
        assertSameRepositoryCount(databaseSizeBeforeUpdate);
    }

    @Test
    @Transactional
    void patchWithIdMismatchPrescription() throws Exception {
        long databaseSizeBeforeUpdate = getRepositoryCount();
        prescription.setId(longCount.incrementAndGet());

        // Create the Prescription
        PrescriptionDTO prescriptionDTO = prescriptionMapper.toDto(prescription);

        // If url ID doesn't match entity ID, it will throw BadRequestAlertException
        restPrescriptionMockMvc
            .perform(
                patch(ENTITY_API_URL_ID, longCount.incrementAndGet())
                    .contentType("application/merge-patch+json")
                    .content(om.writeValueAsBytes(prescriptionDTO))
            )
            .andExpect(status().isBadRequest());

        // Validate the Prescription in the database
        assertSameRepositoryCount(databaseSizeBeforeUpdate);
    }

    @Test
    @Transactional
    void patchWithMissingIdPathParamPrescription() throws Exception {
        long databaseSizeBeforeUpdate = getRepositoryCount();
        prescription.setId(longCount.incrementAndGet());

        // Create the Prescription
        PrescriptionDTO prescriptionDTO = prescriptionMapper.toDto(prescription);

        // If url ID doesn't match entity ID, it will throw BadRequestAlertException
        restPrescriptionMockMvc
            .perform(patch(ENTITY_API_URL).contentType("application/merge-patch+json").content(om.writeValueAsBytes(prescriptionDTO)))
            .andExpect(status().isMethodNotAllowed());

        // Validate the Prescription in the database
        assertSameRepositoryCount(databaseSizeBeforeUpdate);
    }

    @Test
    @Transactional
    void deletePrescription() throws Exception {
        // Initialize the database
        insertedPrescription = prescriptionRepository.saveAndFlush(prescription);

        long databaseSizeBeforeDelete = getRepositoryCount();

        // Delete the prescription
        restPrescriptionMockMvc
            .perform(delete(ENTITY_API_URL_ID, prescription.getId()).accept(MediaType.APPLICATION_JSON))
            .andExpect(status().isNoContent());

        // Validate the database contains one less item
        assertDecrementedRepositoryCount(databaseSizeBeforeDelete);
    }

    protected long getRepositoryCount() {
        return prescriptionRepository.count();
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

    protected Prescription getPersistedPrescription(Prescription prescription) {
        return prescriptionRepository.findById(prescription.getId()).orElseThrow();
    }

    protected void assertPersistedPrescriptionToMatchAllProperties(Prescription expectedPrescription) {
        assertPrescriptionAllPropertiesEquals(expectedPrescription, getPersistedPrescription(expectedPrescription));
    }

    protected void assertPersistedPrescriptionToMatchUpdatableProperties(Prescription expectedPrescription) {
        assertPrescriptionAllUpdatablePropertiesEquals(expectedPrescription, getPersistedPrescription(expectedPrescription));
    }
}
