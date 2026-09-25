package com.hyperbrains.hms.web.rest;

import static com.hyperbrains.hms.domain.DiagnosisAsserts.*;
import static com.hyperbrains.hms.web.rest.TestUtil.createUpdateProxyForBean;
import static org.assertj.core.api.Assertions.assertThat;
import static org.hamcrest.Matchers.hasItem;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.hyperbrains.hms.IntegrationTest;
import com.hyperbrains.hms.domain.Diagnosis;
import com.hyperbrains.hms.repository.DiagnosisRepository;
import com.hyperbrains.hms.service.dto.DiagnosisDTO;
import com.hyperbrains.hms.service.mapper.DiagnosisMapper;
import jakarta.persistence.EntityManager;
import java.util.Random;
import java.util.concurrent.atomic.AtomicLong;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc;
import org.springframework.http.MediaType;
import com.hyperbrains.hms.security.AuthoritiesConstants;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.transaction.annotation.Transactional;

/**
 * Integration tests for the {@link DiagnosisResource} REST controller.
 */
@IntegrationTest
@AutoConfigureMockMvc
@WithMockUser(authorities = AuthoritiesConstants.ADMIN)
class DiagnosisResourceIT {

    private static final String DEFAULT_CODE = "AAAAAAAAAA";
    private static final String UPDATED_CODE = "BBBBBBBBBB";

    private static final String DEFAULT_NAME = "AAAAAAAAAA";
    private static final String UPDATED_NAME = "BBBBBBBBBB";

    private static final Boolean DEFAULT_ACTIVE = false;
    private static final Boolean UPDATED_ACTIVE = true;

    private static final String ENTITY_API_URL = "/api/diagnoses";
    private static final String ENTITY_API_URL_ID = ENTITY_API_URL + "/{id}";

    private static final Random random = new Random();
    private static final AtomicLong longCount = new AtomicLong(random.nextInt() + 2L * Integer.MAX_VALUE);

    @Autowired
    private ObjectMapper om;

    @Autowired
    private DiagnosisRepository diagnosisRepository;

    @Autowired
    private DiagnosisMapper diagnosisMapper;

    @Autowired
    private EntityManager em;

    @Autowired
    private MockMvc restDiagnosisMockMvc;

    private Diagnosis diagnosis;

    private Diagnosis insertedDiagnosis;

    /**
     * Create an entity for this test.
     *
     * This is a static method, as tests for other entities might also need it,
     * if they test an entity which requires the current entity.
     */
    public static Diagnosis createEntity() {
        return new Diagnosis().code(DEFAULT_CODE).name(DEFAULT_NAME).active(DEFAULT_ACTIVE);
    }

    /**
     * Create an updated entity for this test.
     *
     * This is a static method, as tests for other entities might also need it,
     * if they test an entity which requires the current entity.
     */
    public static Diagnosis createUpdatedEntity() {
        return new Diagnosis().code(UPDATED_CODE).name(UPDATED_NAME).active(UPDATED_ACTIVE);
    }

    @BeforeEach
    void initTest() {
        diagnosis = createEntity();
    }

    @AfterEach
    void cleanup() {
        if (insertedDiagnosis != null) {
            diagnosisRepository.delete(insertedDiagnosis);
            insertedDiagnosis = null;
        }
    }

    @Test
    @Transactional
    void createDiagnosis() throws Exception {
        long databaseSizeBeforeCreate = getRepositoryCount();
        // Create the Diagnosis
        DiagnosisDTO diagnosisDTO = diagnosisMapper.toDto(diagnosis);
        var returnedDiagnosisDTO = om.readValue(
            restDiagnosisMockMvc
                .perform(post(ENTITY_API_URL).contentType(MediaType.APPLICATION_JSON).content(om.writeValueAsBytes(diagnosisDTO)))
                .andExpect(status().isCreated())
                .andReturn()
                .getResponse()
                .getContentAsString(),
            DiagnosisDTO.class
        );

        // Validate the Diagnosis in the database
        assertIncrementedRepositoryCount(databaseSizeBeforeCreate);
        var returnedDiagnosis = diagnosisMapper.toEntity(returnedDiagnosisDTO);
        assertDiagnosisUpdatableFieldsEquals(returnedDiagnosis, getPersistedDiagnosis(returnedDiagnosis));

        insertedDiagnosis = returnedDiagnosis;
    }

    @Test
    @Transactional
    void createDiagnosisWithExistingId() throws Exception {
        // Create the Diagnosis with an existing ID
        diagnosis.setId(1L);
        DiagnosisDTO diagnosisDTO = diagnosisMapper.toDto(diagnosis);

        long databaseSizeBeforeCreate = getRepositoryCount();

        // An entity with an existing ID cannot be created, so this API call must fail
        restDiagnosisMockMvc
            .perform(post(ENTITY_API_URL).contentType(MediaType.APPLICATION_JSON).content(om.writeValueAsBytes(diagnosisDTO)))
            .andExpect(status().isBadRequest());

        // Validate the Diagnosis in the database
        assertSameRepositoryCount(databaseSizeBeforeCreate);
    }

    @Test
    @Transactional
    void checkCodeIsRequired() throws Exception {
        long databaseSizeBeforeTest = getRepositoryCount();
        // set the field null
        diagnosis.setCode(null);

        // Create the Diagnosis, which fails.
        DiagnosisDTO diagnosisDTO = diagnosisMapper.toDto(diagnosis);

        restDiagnosisMockMvc
            .perform(post(ENTITY_API_URL).contentType(MediaType.APPLICATION_JSON).content(om.writeValueAsBytes(diagnosisDTO)))
            .andExpect(status().isBadRequest());

        assertSameRepositoryCount(databaseSizeBeforeTest);
    }

    @Test
    @Transactional
    void checkNameIsRequired() throws Exception {
        long databaseSizeBeforeTest = getRepositoryCount();
        // set the field null
        diagnosis.setName(null);

        // Create the Diagnosis, which fails.
        DiagnosisDTO diagnosisDTO = diagnosisMapper.toDto(diagnosis);

        restDiagnosisMockMvc
            .perform(post(ENTITY_API_URL).contentType(MediaType.APPLICATION_JSON).content(om.writeValueAsBytes(diagnosisDTO)))
            .andExpect(status().isBadRequest());

        assertSameRepositoryCount(databaseSizeBeforeTest);
    }

    @Test
    @Transactional
    void checkActiveIsRequired() throws Exception {
        long databaseSizeBeforeTest = getRepositoryCount();
        // set the field null
        diagnosis.setActive(null);

        // Create the Diagnosis, which fails.
        DiagnosisDTO diagnosisDTO = diagnosisMapper.toDto(diagnosis);

        restDiagnosisMockMvc
            .perform(post(ENTITY_API_URL).contentType(MediaType.APPLICATION_JSON).content(om.writeValueAsBytes(diagnosisDTO)))
            .andExpect(status().isBadRequest());

        assertSameRepositoryCount(databaseSizeBeforeTest);
    }

    @Test
    @Transactional
    void getAllDiagnoses() throws Exception {
        // Initialize the database
        insertedDiagnosis = diagnosisRepository.saveAndFlush(diagnosis);

        // Get all the diagnosisList
        restDiagnosisMockMvc
            .perform(get(ENTITY_API_URL + "?sort=id,desc"))
            .andExpect(status().isOk())
            .andExpect(content().contentType(MediaType.APPLICATION_JSON_VALUE))
            .andExpect(jsonPath("$.[*].id").value(hasItem(diagnosis.getId().intValue())))
            .andExpect(jsonPath("$.[*].code").value(hasItem(DEFAULT_CODE)))
            .andExpect(jsonPath("$.[*].name").value(hasItem(DEFAULT_NAME)))
            .andExpect(jsonPath("$.[*].active").value(hasItem(DEFAULT_ACTIVE)));
    }

    @Test
    @Transactional
    void getDiagnosis() throws Exception {
        // Initialize the database
        insertedDiagnosis = diagnosisRepository.saveAndFlush(diagnosis);

        // Get the diagnosis
        restDiagnosisMockMvc
            .perform(get(ENTITY_API_URL_ID, diagnosis.getId()))
            .andExpect(status().isOk())
            .andExpect(content().contentType(MediaType.APPLICATION_JSON_VALUE))
            .andExpect(jsonPath("$.id").value(diagnosis.getId().intValue()))
            .andExpect(jsonPath("$.code").value(DEFAULT_CODE))
            .andExpect(jsonPath("$.name").value(DEFAULT_NAME))
            .andExpect(jsonPath("$.active").value(DEFAULT_ACTIVE));
    }

    @Test
    @Transactional
    void getNonExistingDiagnosis() throws Exception {
        // Get the diagnosis
        restDiagnosisMockMvc.perform(get(ENTITY_API_URL_ID, Long.MAX_VALUE)).andExpect(status().isNotFound());
    }

    @Test
    @Transactional
    void putExistingDiagnosis() throws Exception {
        // Initialize the database
        insertedDiagnosis = diagnosisRepository.saveAndFlush(diagnosis);

        long databaseSizeBeforeUpdate = getRepositoryCount();

        // Update the diagnosis
        Diagnosis updatedDiagnosis = diagnosisRepository.findById(diagnosis.getId()).orElseThrow();
        // Disconnect from session so that the updates on updatedDiagnosis are not directly saved in db
        em.detach(updatedDiagnosis);
        updatedDiagnosis.code(UPDATED_CODE).name(UPDATED_NAME).active(UPDATED_ACTIVE);
        DiagnosisDTO diagnosisDTO = diagnosisMapper.toDto(updatedDiagnosis);

        restDiagnosisMockMvc
            .perform(
                put(ENTITY_API_URL_ID, diagnosisDTO.getId())
                    .contentType(MediaType.APPLICATION_JSON)
                    .content(om.writeValueAsBytes(diagnosisDTO))
            )
            .andExpect(status().isOk());

        // Validate the Diagnosis in the database
        assertSameRepositoryCount(databaseSizeBeforeUpdate);
        assertPersistedDiagnosisToMatchAllProperties(updatedDiagnosis);
    }

    @Test
    @Transactional
    void putNonExistingDiagnosis() throws Exception {
        long databaseSizeBeforeUpdate = getRepositoryCount();
        diagnosis.setId(longCount.incrementAndGet());

        // Create the Diagnosis
        DiagnosisDTO diagnosisDTO = diagnosisMapper.toDto(diagnosis);

        // If the entity doesn't have an ID, it will throw BadRequestAlertException
        restDiagnosisMockMvc
            .perform(
                put(ENTITY_API_URL_ID, diagnosisDTO.getId())
                    .contentType(MediaType.APPLICATION_JSON)
                    .content(om.writeValueAsBytes(diagnosisDTO))
            )
            .andExpect(status().isBadRequest());

        // Validate the Diagnosis in the database
        assertSameRepositoryCount(databaseSizeBeforeUpdate);
    }

    @Test
    @Transactional
    void putWithIdMismatchDiagnosis() throws Exception {
        long databaseSizeBeforeUpdate = getRepositoryCount();
        diagnosis.setId(longCount.incrementAndGet());

        // Create the Diagnosis
        DiagnosisDTO diagnosisDTO = diagnosisMapper.toDto(diagnosis);

        // If url ID doesn't match entity ID, it will throw BadRequestAlertException
        restDiagnosisMockMvc
            .perform(
                put(ENTITY_API_URL_ID, longCount.incrementAndGet())
                    .contentType(MediaType.APPLICATION_JSON)
                    .content(om.writeValueAsBytes(diagnosisDTO))
            )
            .andExpect(status().isBadRequest());

        // Validate the Diagnosis in the database
        assertSameRepositoryCount(databaseSizeBeforeUpdate);
    }

    @Test
    @Transactional
    void putWithMissingIdPathParamDiagnosis() throws Exception {
        long databaseSizeBeforeUpdate = getRepositoryCount();
        diagnosis.setId(longCount.incrementAndGet());

        // Create the Diagnosis
        DiagnosisDTO diagnosisDTO = diagnosisMapper.toDto(diagnosis);

        // If url ID doesn't match entity ID, it will throw BadRequestAlertException
        restDiagnosisMockMvc
            .perform(put(ENTITY_API_URL).contentType(MediaType.APPLICATION_JSON).content(om.writeValueAsBytes(diagnosisDTO)))
            .andExpect(status().isMethodNotAllowed());

        // Validate the Diagnosis in the database
        assertSameRepositoryCount(databaseSizeBeforeUpdate);
    }

    @Test
    @Transactional
    void partialUpdateDiagnosisWithPatch() throws Exception {
        // Initialize the database
        insertedDiagnosis = diagnosisRepository.saveAndFlush(diagnosis);

        long databaseSizeBeforeUpdate = getRepositoryCount();

        // Update the diagnosis using partial update
        Diagnosis partialUpdatedDiagnosis = new Diagnosis();
        partialUpdatedDiagnosis.setId(diagnosis.getId());

        partialUpdatedDiagnosis.code(UPDATED_CODE).active(UPDATED_ACTIVE);

        restDiagnosisMockMvc
            .perform(
                patch(ENTITY_API_URL_ID, partialUpdatedDiagnosis.getId())
                    .contentType("application/merge-patch+json")
                    .content(om.writeValueAsBytes(partialUpdatedDiagnosis))
            )
            .andExpect(status().isOk());

        // Validate the Diagnosis in the database

        assertSameRepositoryCount(databaseSizeBeforeUpdate);
        assertDiagnosisUpdatableFieldsEquals(
            createUpdateProxyForBean(partialUpdatedDiagnosis, diagnosis),
            getPersistedDiagnosis(diagnosis)
        );
    }

    @Test
    @Transactional
    void fullUpdateDiagnosisWithPatch() throws Exception {
        // Initialize the database
        insertedDiagnosis = diagnosisRepository.saveAndFlush(diagnosis);

        long databaseSizeBeforeUpdate = getRepositoryCount();

        // Update the diagnosis using partial update
        Diagnosis partialUpdatedDiagnosis = new Diagnosis();
        partialUpdatedDiagnosis.setId(diagnosis.getId());

        partialUpdatedDiagnosis.code(UPDATED_CODE).name(UPDATED_NAME).active(UPDATED_ACTIVE);

        restDiagnosisMockMvc
            .perform(
                patch(ENTITY_API_URL_ID, partialUpdatedDiagnosis.getId())
                    .contentType("application/merge-patch+json")
                    .content(om.writeValueAsBytes(partialUpdatedDiagnosis))
            )
            .andExpect(status().isOk());

        // Validate the Diagnosis in the database

        assertSameRepositoryCount(databaseSizeBeforeUpdate);
        assertDiagnosisUpdatableFieldsEquals(partialUpdatedDiagnosis, getPersistedDiagnosis(partialUpdatedDiagnosis));
    }

    @Test
    @Transactional
    void patchNonExistingDiagnosis() throws Exception {
        long databaseSizeBeforeUpdate = getRepositoryCount();
        diagnosis.setId(longCount.incrementAndGet());

        // Create the Diagnosis
        DiagnosisDTO diagnosisDTO = diagnosisMapper.toDto(diagnosis);

        // If the entity doesn't have an ID, it will throw BadRequestAlertException
        restDiagnosisMockMvc
            .perform(
                patch(ENTITY_API_URL_ID, diagnosisDTO.getId())
                    .contentType("application/merge-patch+json")
                    .content(om.writeValueAsBytes(diagnosisDTO))
            )
            .andExpect(status().isBadRequest());

        // Validate the Diagnosis in the database
        assertSameRepositoryCount(databaseSizeBeforeUpdate);
    }

    @Test
    @Transactional
    void patchWithIdMismatchDiagnosis() throws Exception {
        long databaseSizeBeforeUpdate = getRepositoryCount();
        diagnosis.setId(longCount.incrementAndGet());

        // Create the Diagnosis
        DiagnosisDTO diagnosisDTO = diagnosisMapper.toDto(diagnosis);

        // If url ID doesn't match entity ID, it will throw BadRequestAlertException
        restDiagnosisMockMvc
            .perform(
                patch(ENTITY_API_URL_ID, longCount.incrementAndGet())
                    .contentType("application/merge-patch+json")
                    .content(om.writeValueAsBytes(diagnosisDTO))
            )
            .andExpect(status().isBadRequest());

        // Validate the Diagnosis in the database
        assertSameRepositoryCount(databaseSizeBeforeUpdate);
    }

    @Test
    @Transactional
    void patchWithMissingIdPathParamDiagnosis() throws Exception {
        long databaseSizeBeforeUpdate = getRepositoryCount();
        diagnosis.setId(longCount.incrementAndGet());

        // Create the Diagnosis
        DiagnosisDTO diagnosisDTO = diagnosisMapper.toDto(diagnosis);

        // If url ID doesn't match entity ID, it will throw BadRequestAlertException
        restDiagnosisMockMvc
            .perform(patch(ENTITY_API_URL).contentType("application/merge-patch+json").content(om.writeValueAsBytes(diagnosisDTO)))
            .andExpect(status().isMethodNotAllowed());

        // Validate the Diagnosis in the database
        assertSameRepositoryCount(databaseSizeBeforeUpdate);
    }

    @Test
    @Transactional
    void deleteDiagnosis() throws Exception {
        // Initialize the database
        insertedDiagnosis = diagnosisRepository.saveAndFlush(diagnosis);

        long databaseSizeBeforeDelete = getRepositoryCount();

        // Delete the diagnosis
        restDiagnosisMockMvc
            .perform(delete(ENTITY_API_URL_ID, diagnosis.getId()).accept(MediaType.APPLICATION_JSON))
            .andExpect(status().isNoContent());

        // Validate the database contains one less item
        assertDecrementedRepositoryCount(databaseSizeBeforeDelete);
    }

    protected long getRepositoryCount() {
        return diagnosisRepository.count();
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

    protected Diagnosis getPersistedDiagnosis(Diagnosis diagnosis) {
        return diagnosisRepository.findById(diagnosis.getId()).orElseThrow();
    }

    protected void assertPersistedDiagnosisToMatchAllProperties(Diagnosis expectedDiagnosis) {
        assertDiagnosisAllPropertiesEquals(expectedDiagnosis, getPersistedDiagnosis(expectedDiagnosis));
    }

    protected void assertPersistedDiagnosisToMatchUpdatableProperties(Diagnosis expectedDiagnosis) {
        assertDiagnosisAllUpdatablePropertiesEquals(expectedDiagnosis, getPersistedDiagnosis(expectedDiagnosis));
    }
}
