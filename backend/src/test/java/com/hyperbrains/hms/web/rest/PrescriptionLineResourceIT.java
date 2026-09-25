package com.hyperbrains.hms.web.rest;

import static com.hyperbrains.hms.domain.PrescriptionLineAsserts.*;
import static com.hyperbrains.hms.web.rest.TestUtil.createUpdateProxyForBean;
import static org.assertj.core.api.Assertions.assertThat;
import static org.hamcrest.Matchers.hasItem;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.hyperbrains.hms.IntegrationTest;
import com.hyperbrains.hms.domain.Drug;
import com.hyperbrains.hms.domain.Prescription;
import com.hyperbrains.hms.domain.PrescriptionLine;
import com.hyperbrains.hms.repository.PrescriptionLineRepository;
import com.hyperbrains.hms.service.dto.PrescriptionLineDTO;
import com.hyperbrains.hms.service.mapper.PrescriptionLineMapper;
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
 * Integration tests for the {@link PrescriptionLineResource} REST controller.
 */
@IntegrationTest
@AutoConfigureMockMvc
@WithMockUser(authorities = AuthoritiesConstants.SUPER_ADMIN)
class PrescriptionLineResourceIT {

    private static final String DEFAULT_DOSAGE = "AAAAAAAAAA";
    private static final String UPDATED_DOSAGE = "BBBBBBBBBB";

    private static final String DEFAULT_DURATION = "AAAAAAAAAA";
    private static final String UPDATED_DURATION = "BBBBBBBBBB";

    private static final Integer DEFAULT_QUANTITY = 1;
    private static final Integer UPDATED_QUANTITY = 2;

    private static final String ENTITY_API_URL = "/api/prescription-lines";
    private static final String ENTITY_API_URL_ID = ENTITY_API_URL + "/{id}";

    private static final Random random = new Random();
    private static final AtomicLong longCount = new AtomicLong(random.nextInt() + 2L * Integer.MAX_VALUE);

    @Autowired
    private ObjectMapper om;

    @Autowired
    private PrescriptionLineRepository prescriptionLineRepository;

    @Autowired
    private PrescriptionLineMapper prescriptionLineMapper;

    @Autowired
    private EntityManager em;

    @Autowired
    private MockMvc restPrescriptionLineMockMvc;

    private PrescriptionLine prescriptionLine;

    private PrescriptionLine insertedPrescriptionLine;

    /**
     * Create an entity for this test.
     *
     * This is a static method, as tests for other entities might also need it,
     * if they test an entity which requires the current entity.
     */
    public static PrescriptionLine createEntity(EntityManager em) {
        PrescriptionLine prescriptionLine = new PrescriptionLine()
            .dosage(DEFAULT_DOSAGE)
            .duration(DEFAULT_DURATION)
            .quantity(DEFAULT_QUANTITY);
        // Add required entity
        Prescription prescription;
        if (TestUtil.findAll(em, Prescription.class).isEmpty()) {
            prescription = PrescriptionResourceIT.createEntity();
            em.persist(prescription);
            em.flush();
        } else {
            prescription = TestUtil.findAll(em, Prescription.class).get(0);
        }
        prescriptionLine.setPrescription(prescription);
        // Add required entity
        Drug drug;
        if (TestUtil.findAll(em, Drug.class).isEmpty()) {
            drug = DrugResourceIT.createEntity();
            em.persist(drug);
            em.flush();
        } else {
            drug = TestUtil.findAll(em, Drug.class).get(0);
        }
        prescriptionLine.setDrug(drug);
        return prescriptionLine;
    }

    /**
     * Create an updated entity for this test.
     *
     * This is a static method, as tests for other entities might also need it,
     * if they test an entity which requires the current entity.
     */
    public static PrescriptionLine createUpdatedEntity(EntityManager em) {
        PrescriptionLine updatedPrescriptionLine = new PrescriptionLine()
            .dosage(UPDATED_DOSAGE)
            .duration(UPDATED_DURATION)
            .quantity(UPDATED_QUANTITY);
        // Add required entity
        Prescription prescription;
        if (TestUtil.findAll(em, Prescription.class).isEmpty()) {
            prescription = PrescriptionResourceIT.createUpdatedEntity();
            em.persist(prescription);
            em.flush();
        } else {
            prescription = TestUtil.findAll(em, Prescription.class).get(0);
        }
        updatedPrescriptionLine.setPrescription(prescription);
        // Add required entity
        Drug drug;
        if (TestUtil.findAll(em, Drug.class).isEmpty()) {
            drug = DrugResourceIT.createUpdatedEntity();
            em.persist(drug);
            em.flush();
        } else {
            drug = TestUtil.findAll(em, Drug.class).get(0);
        }
        updatedPrescriptionLine.setDrug(drug);
        return updatedPrescriptionLine;
    }

    @BeforeEach
    void initTest() {
        prescriptionLine = createEntity(em);
    }

    @AfterEach
    void cleanup() {
        if (insertedPrescriptionLine != null) {
            prescriptionLineRepository.delete(insertedPrescriptionLine);
            insertedPrescriptionLine = null;
        }
    }

    @Test
    @Transactional
    void createPrescriptionLine() throws Exception {
        long databaseSizeBeforeCreate = getRepositoryCount();
        // Create the PrescriptionLine
        PrescriptionLineDTO prescriptionLineDTO = prescriptionLineMapper.toDto(prescriptionLine);
        var returnedPrescriptionLineDTO = om.readValue(
            restPrescriptionLineMockMvc
                .perform(post(ENTITY_API_URL).contentType(MediaType.APPLICATION_JSON).content(om.writeValueAsBytes(prescriptionLineDTO)))
                .andExpect(status().isCreated())
                .andReturn()
                .getResponse()
                .getContentAsString(),
            PrescriptionLineDTO.class
        );

        // Validate the PrescriptionLine in the database
        assertIncrementedRepositoryCount(databaseSizeBeforeCreate);
        var returnedPrescriptionLine = prescriptionLineMapper.toEntity(returnedPrescriptionLineDTO);
        assertPrescriptionLineUpdatableFieldsEquals(returnedPrescriptionLine, getPersistedPrescriptionLine(returnedPrescriptionLine));

        insertedPrescriptionLine = returnedPrescriptionLine;
    }

    @Test
    @Transactional
    void createPrescriptionLineWithExistingId() throws Exception {
        // Create the PrescriptionLine with an existing ID
        prescriptionLine.setId(1L);
        PrescriptionLineDTO prescriptionLineDTO = prescriptionLineMapper.toDto(prescriptionLine);

        long databaseSizeBeforeCreate = getRepositoryCount();

        // An entity with an existing ID cannot be created, so this API call must fail
        restPrescriptionLineMockMvc
            .perform(post(ENTITY_API_URL).contentType(MediaType.APPLICATION_JSON).content(om.writeValueAsBytes(prescriptionLineDTO)))
            .andExpect(status().isBadRequest());

        // Validate the PrescriptionLine in the database
        assertSameRepositoryCount(databaseSizeBeforeCreate);
    }

    @Test
    @Transactional
    void checkDosageIsRequired() throws Exception {
        long databaseSizeBeforeTest = getRepositoryCount();
        // set the field null
        prescriptionLine.setDosage(null);

        // Create the PrescriptionLine, which fails.
        PrescriptionLineDTO prescriptionLineDTO = prescriptionLineMapper.toDto(prescriptionLine);

        restPrescriptionLineMockMvc
            .perform(post(ENTITY_API_URL).contentType(MediaType.APPLICATION_JSON).content(om.writeValueAsBytes(prescriptionLineDTO)))
            .andExpect(status().isBadRequest());

        assertSameRepositoryCount(databaseSizeBeforeTest);
    }

    @Test
    @Transactional
    void checkDurationIsRequired() throws Exception {
        long databaseSizeBeforeTest = getRepositoryCount();
        // set the field null
        prescriptionLine.setDuration(null);

        // Create the PrescriptionLine, which fails.
        PrescriptionLineDTO prescriptionLineDTO = prescriptionLineMapper.toDto(prescriptionLine);

        restPrescriptionLineMockMvc
            .perform(post(ENTITY_API_URL).contentType(MediaType.APPLICATION_JSON).content(om.writeValueAsBytes(prescriptionLineDTO)))
            .andExpect(status().isBadRequest());

        assertSameRepositoryCount(databaseSizeBeforeTest);
    }

    @Test
    @Transactional
    void checkQuantityIsRequired() throws Exception {
        long databaseSizeBeforeTest = getRepositoryCount();
        // set the field null
        prescriptionLine.setQuantity(null);

        // Create the PrescriptionLine, which fails.
        PrescriptionLineDTO prescriptionLineDTO = prescriptionLineMapper.toDto(prescriptionLine);

        restPrescriptionLineMockMvc
            .perform(post(ENTITY_API_URL).contentType(MediaType.APPLICATION_JSON).content(om.writeValueAsBytes(prescriptionLineDTO)))
            .andExpect(status().isBadRequest());

        assertSameRepositoryCount(databaseSizeBeforeTest);
    }

    @Test
    @Transactional
    void getAllPrescriptionLines() throws Exception {
        // Initialize the database
        insertedPrescriptionLine = prescriptionLineRepository.saveAndFlush(prescriptionLine);

        // Get all the prescriptionLineList
        restPrescriptionLineMockMvc
            .perform(get(ENTITY_API_URL + "?sort=id,desc"))
            .andExpect(status().isOk())
            .andExpect(content().contentType(MediaType.APPLICATION_JSON_VALUE))
            .andExpect(jsonPath("$.[*].id").value(hasItem(prescriptionLine.getId().intValue())))
            .andExpect(jsonPath("$.[*].dosage").value(hasItem(DEFAULT_DOSAGE)))
            .andExpect(jsonPath("$.[*].duration").value(hasItem(DEFAULT_DURATION)))
            .andExpect(jsonPath("$.[*].quantity").value(hasItem(DEFAULT_QUANTITY)));
    }

    @Test
    @Transactional
    void getPrescriptionLine() throws Exception {
        // Initialize the database
        insertedPrescriptionLine = prescriptionLineRepository.saveAndFlush(prescriptionLine);

        // Get the prescriptionLine
        restPrescriptionLineMockMvc
            .perform(get(ENTITY_API_URL_ID, prescriptionLine.getId()))
            .andExpect(status().isOk())
            .andExpect(content().contentType(MediaType.APPLICATION_JSON_VALUE))
            .andExpect(jsonPath("$.id").value(prescriptionLine.getId().intValue()))
            .andExpect(jsonPath("$.dosage").value(DEFAULT_DOSAGE))
            .andExpect(jsonPath("$.duration").value(DEFAULT_DURATION))
            .andExpect(jsonPath("$.quantity").value(DEFAULT_QUANTITY));
    }

    @Test
    @Transactional
    void getNonExistingPrescriptionLine() throws Exception {
        // Get the prescriptionLine
        restPrescriptionLineMockMvc.perform(get(ENTITY_API_URL_ID, Long.MAX_VALUE)).andExpect(status().isNotFound());
    }

    @Test
    @Transactional
    void putExistingPrescriptionLine() throws Exception {
        // Initialize the database
        insertedPrescriptionLine = prescriptionLineRepository.saveAndFlush(prescriptionLine);

        long databaseSizeBeforeUpdate = getRepositoryCount();

        // Update the prescriptionLine
        PrescriptionLine updatedPrescriptionLine = prescriptionLineRepository.findById(prescriptionLine.getId()).orElseThrow();
        // Disconnect from session so that the updates on updatedPrescriptionLine are not directly saved in db
        em.detach(updatedPrescriptionLine);
        updatedPrescriptionLine.dosage(UPDATED_DOSAGE).duration(UPDATED_DURATION).quantity(UPDATED_QUANTITY);
        PrescriptionLineDTO prescriptionLineDTO = prescriptionLineMapper.toDto(updatedPrescriptionLine);

        restPrescriptionLineMockMvc
            .perform(
                put(ENTITY_API_URL_ID, prescriptionLineDTO.getId())
                    .contentType(MediaType.APPLICATION_JSON)
                    .content(om.writeValueAsBytes(prescriptionLineDTO))
            )
            .andExpect(status().isOk());

        // Validate the PrescriptionLine in the database
        assertSameRepositoryCount(databaseSizeBeforeUpdate);
        assertPersistedPrescriptionLineToMatchAllProperties(updatedPrescriptionLine);
    }

    @Test
    @Transactional
    void putNonExistingPrescriptionLine() throws Exception {
        long databaseSizeBeforeUpdate = getRepositoryCount();
        prescriptionLine.setId(longCount.incrementAndGet());

        // Create the PrescriptionLine
        PrescriptionLineDTO prescriptionLineDTO = prescriptionLineMapper.toDto(prescriptionLine);

        // If the entity doesn't have an ID, it will throw BadRequestAlertException
        restPrescriptionLineMockMvc
            .perform(
                put(ENTITY_API_URL_ID, prescriptionLineDTO.getId())
                    .contentType(MediaType.APPLICATION_JSON)
                    .content(om.writeValueAsBytes(prescriptionLineDTO))
            )
            .andExpect(status().isBadRequest());

        // Validate the PrescriptionLine in the database
        assertSameRepositoryCount(databaseSizeBeforeUpdate);
    }

    @Test
    @Transactional
    void putWithIdMismatchPrescriptionLine() throws Exception {
        long databaseSizeBeforeUpdate = getRepositoryCount();
        prescriptionLine.setId(longCount.incrementAndGet());

        // Create the PrescriptionLine
        PrescriptionLineDTO prescriptionLineDTO = prescriptionLineMapper.toDto(prescriptionLine);

        // If url ID doesn't match entity ID, it will throw BadRequestAlertException
        restPrescriptionLineMockMvc
            .perform(
                put(ENTITY_API_URL_ID, longCount.incrementAndGet())
                    .contentType(MediaType.APPLICATION_JSON)
                    .content(om.writeValueAsBytes(prescriptionLineDTO))
            )
            .andExpect(status().isBadRequest());

        // Validate the PrescriptionLine in the database
        assertSameRepositoryCount(databaseSizeBeforeUpdate);
    }

    @Test
    @Transactional
    void putWithMissingIdPathParamPrescriptionLine() throws Exception {
        long databaseSizeBeforeUpdate = getRepositoryCount();
        prescriptionLine.setId(longCount.incrementAndGet());

        // Create the PrescriptionLine
        PrescriptionLineDTO prescriptionLineDTO = prescriptionLineMapper.toDto(prescriptionLine);

        // If url ID doesn't match entity ID, it will throw BadRequestAlertException
        restPrescriptionLineMockMvc
            .perform(put(ENTITY_API_URL).contentType(MediaType.APPLICATION_JSON).content(om.writeValueAsBytes(prescriptionLineDTO)))
            .andExpect(status().isMethodNotAllowed());

        // Validate the PrescriptionLine in the database
        assertSameRepositoryCount(databaseSizeBeforeUpdate);
    }

    @Test
    @Transactional
    void partialUpdatePrescriptionLineWithPatch() throws Exception {
        // Initialize the database
        insertedPrescriptionLine = prescriptionLineRepository.saveAndFlush(prescriptionLine);

        long databaseSizeBeforeUpdate = getRepositoryCount();

        // Update the prescriptionLine using partial update
        PrescriptionLine partialUpdatedPrescriptionLine = new PrescriptionLine();
        partialUpdatedPrescriptionLine.setId(prescriptionLine.getId());

        partialUpdatedPrescriptionLine.dosage(UPDATED_DOSAGE);

        restPrescriptionLineMockMvc
            .perform(
                patch(ENTITY_API_URL_ID, partialUpdatedPrescriptionLine.getId())
                    .contentType("application/merge-patch+json")
                    .content(om.writeValueAsBytes(partialUpdatedPrescriptionLine))
            )
            .andExpect(status().isOk());

        // Validate the PrescriptionLine in the database

        assertSameRepositoryCount(databaseSizeBeforeUpdate);
        assertPrescriptionLineUpdatableFieldsEquals(
            createUpdateProxyForBean(partialUpdatedPrescriptionLine, prescriptionLine),
            getPersistedPrescriptionLine(prescriptionLine)
        );
    }

    @Test
    @Transactional
    void fullUpdatePrescriptionLineWithPatch() throws Exception {
        // Initialize the database
        insertedPrescriptionLine = prescriptionLineRepository.saveAndFlush(prescriptionLine);

        long databaseSizeBeforeUpdate = getRepositoryCount();

        // Update the prescriptionLine using partial update
        PrescriptionLine partialUpdatedPrescriptionLine = new PrescriptionLine();
        partialUpdatedPrescriptionLine.setId(prescriptionLine.getId());

        partialUpdatedPrescriptionLine.dosage(UPDATED_DOSAGE).duration(UPDATED_DURATION).quantity(UPDATED_QUANTITY);

        restPrescriptionLineMockMvc
            .perform(
                patch(ENTITY_API_URL_ID, partialUpdatedPrescriptionLine.getId())
                    .contentType("application/merge-patch+json")
                    .content(om.writeValueAsBytes(partialUpdatedPrescriptionLine))
            )
            .andExpect(status().isOk());

        // Validate the PrescriptionLine in the database

        assertSameRepositoryCount(databaseSizeBeforeUpdate);
        assertPrescriptionLineUpdatableFieldsEquals(
            partialUpdatedPrescriptionLine,
            getPersistedPrescriptionLine(partialUpdatedPrescriptionLine)
        );
    }

    @Test
    @Transactional
    void patchNonExistingPrescriptionLine() throws Exception {
        long databaseSizeBeforeUpdate = getRepositoryCount();
        prescriptionLine.setId(longCount.incrementAndGet());

        // Create the PrescriptionLine
        PrescriptionLineDTO prescriptionLineDTO = prescriptionLineMapper.toDto(prescriptionLine);

        // If the entity doesn't have an ID, it will throw BadRequestAlertException
        restPrescriptionLineMockMvc
            .perform(
                patch(ENTITY_API_URL_ID, prescriptionLineDTO.getId())
                    .contentType("application/merge-patch+json")
                    .content(om.writeValueAsBytes(prescriptionLineDTO))
            )
            .andExpect(status().isBadRequest());

        // Validate the PrescriptionLine in the database
        assertSameRepositoryCount(databaseSizeBeforeUpdate);
    }

    @Test
    @Transactional
    void patchWithIdMismatchPrescriptionLine() throws Exception {
        long databaseSizeBeforeUpdate = getRepositoryCount();
        prescriptionLine.setId(longCount.incrementAndGet());

        // Create the PrescriptionLine
        PrescriptionLineDTO prescriptionLineDTO = prescriptionLineMapper.toDto(prescriptionLine);

        // If url ID doesn't match entity ID, it will throw BadRequestAlertException
        restPrescriptionLineMockMvc
            .perform(
                patch(ENTITY_API_URL_ID, longCount.incrementAndGet())
                    .contentType("application/merge-patch+json")
                    .content(om.writeValueAsBytes(prescriptionLineDTO))
            )
            .andExpect(status().isBadRequest());

        // Validate the PrescriptionLine in the database
        assertSameRepositoryCount(databaseSizeBeforeUpdate);
    }

    @Test
    @Transactional
    void patchWithMissingIdPathParamPrescriptionLine() throws Exception {
        long databaseSizeBeforeUpdate = getRepositoryCount();
        prescriptionLine.setId(longCount.incrementAndGet());

        // Create the PrescriptionLine
        PrescriptionLineDTO prescriptionLineDTO = prescriptionLineMapper.toDto(prescriptionLine);

        // If url ID doesn't match entity ID, it will throw BadRequestAlertException
        restPrescriptionLineMockMvc
            .perform(patch(ENTITY_API_URL).contentType("application/merge-patch+json").content(om.writeValueAsBytes(prescriptionLineDTO)))
            .andExpect(status().isMethodNotAllowed());

        // Validate the PrescriptionLine in the database
        assertSameRepositoryCount(databaseSizeBeforeUpdate);
    }

    @Test
    @Transactional
    void deletePrescriptionLine() throws Exception {
        // Initialize the database
        insertedPrescriptionLine = prescriptionLineRepository.saveAndFlush(prescriptionLine);

        long databaseSizeBeforeDelete = getRepositoryCount();

        // Delete the prescriptionLine
        restPrescriptionLineMockMvc
            .perform(delete(ENTITY_API_URL_ID, prescriptionLine.getId()).accept(MediaType.APPLICATION_JSON))
            .andExpect(status().isNoContent());

        // Validate the database contains one less item
        assertDecrementedRepositoryCount(databaseSizeBeforeDelete);
    }

    protected long getRepositoryCount() {
        return prescriptionLineRepository.count();
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

    protected PrescriptionLine getPersistedPrescriptionLine(PrescriptionLine prescriptionLine) {
        return prescriptionLineRepository.findById(prescriptionLine.getId()).orElseThrow();
    }

    protected void assertPersistedPrescriptionLineToMatchAllProperties(PrescriptionLine expectedPrescriptionLine) {
        assertPrescriptionLineAllPropertiesEquals(expectedPrescriptionLine, getPersistedPrescriptionLine(expectedPrescriptionLine));
    }

    protected void assertPersistedPrescriptionLineToMatchUpdatableProperties(PrescriptionLine expectedPrescriptionLine) {
        assertPrescriptionLineAllUpdatablePropertiesEquals(
            expectedPrescriptionLine,
            getPersistedPrescriptionLine(expectedPrescriptionLine)
        );
    }
}
