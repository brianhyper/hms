package com.hyperbrains.hms.web.rest;

import static com.hyperbrains.hms.domain.VitalSignsAsserts.*;
import static com.hyperbrains.hms.web.rest.TestUtil.createUpdateProxyForBean;
import static com.hyperbrains.hms.web.rest.TestUtil.sameNumber;
import static org.assertj.core.api.Assertions.assertThat;
import static org.hamcrest.Matchers.hasItem;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.hyperbrains.hms.IntegrationTest;
import com.hyperbrains.hms.domain.VitalSigns;
import com.hyperbrains.hms.repository.VitalSignsRepository;
import com.hyperbrains.hms.service.dto.VitalSignsDTO;
import com.hyperbrains.hms.service.mapper.VitalSignsMapper;
import jakarta.persistence.EntityManager;
import java.math.BigDecimal;
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
 * Integration tests for the {@link VitalSignsResource} REST controller.
 */
@IntegrationTest
@AutoConfigureMockMvc
@WithMockUser(authorities = AuthoritiesConstants.NURSE)
class VitalSignsResourceIT {

    private static final BigDecimal DEFAULT_TEMPERATURE = new BigDecimal(1);
    private static final BigDecimal UPDATED_TEMPERATURE = new BigDecimal(2);

    private static final Integer DEFAULT_PULSE_RATE = 1;
    private static final Integer UPDATED_PULSE_RATE = 2;

    private static final Integer DEFAULT_SYSTOLIC_BP = 1;
    private static final Integer UPDATED_SYSTOLIC_BP = 2;

    private static final Integer DEFAULT_DIASTOLIC_BP = 1;
    private static final Integer UPDATED_DIASTOLIC_BP = 2;

    private static final Integer DEFAULT_OXYGEN_SATURATION = 1;
    private static final Integer UPDATED_OXYGEN_SATURATION = 2;

    private static final BigDecimal DEFAULT_WEIGHT = new BigDecimal(1);
    private static final BigDecimal UPDATED_WEIGHT = new BigDecimal(2);

    private static final BigDecimal DEFAULT_HEIGHT = new BigDecimal(1);
    private static final BigDecimal UPDATED_HEIGHT = new BigDecimal(2);

    private static final BigDecimal DEFAULT_BMI = new BigDecimal(1);
    private static final BigDecimal UPDATED_BMI = new BigDecimal(2);

    private static final String DEFAULT_NUTRITIONAL_STATUS = "AAAAAAAAAA";
    private static final String UPDATED_NUTRITIONAL_STATUS = "BBBBBBBBBB";

    private static final Boolean DEFAULT_PREGNANCY_SCREENING = false;
    private static final Boolean UPDATED_PREGNANCY_SCREENING = true;

    private static final String DEFAULT_TRIAGE_NOTES = "AAAAAAAAAA";
    private static final String UPDATED_TRIAGE_NOTES = "BBBBBBBBBB";

    private static final String DEFAULT_OTHER_MEASUREMENTS = "AAAAAAAAAA";
    private static final String UPDATED_OTHER_MEASUREMENTS = "BBBBBBBBBB";

    private static final String ENTITY_API_URL = "/api/vital-signs";
    private static final String ENTITY_API_URL_ID = ENTITY_API_URL + "/{id}";

    private static final Random random = new Random();
    private static final AtomicLong longCount = new AtomicLong(random.nextInt() + 2L * Integer.MAX_VALUE);

    @Autowired
    private ObjectMapper om;

    @Autowired
    private VitalSignsRepository vitalSignsRepository;

    @Autowired
    private VitalSignsMapper vitalSignsMapper;

    @Autowired
    private EntityManager em;

    @Autowired
    private MockMvc restVitalSignsMockMvc;

    private VitalSigns vitalSigns;

    private VitalSigns insertedVitalSigns;

    /**
     * Create an entity for this test.
     *
     * This is a static method, as tests for other entities might also need it,
     * if they test an entity which requires the current entity.
     */
    public static VitalSigns createEntity(EntityManager em) {
        VitalSigns vitalSigns = new VitalSigns()
            .temperature(DEFAULT_TEMPERATURE)
            .pulseRate(DEFAULT_PULSE_RATE)
            .systolicBp(DEFAULT_SYSTOLIC_BP)
            .diastolicBp(DEFAULT_DIASTOLIC_BP)
            .oxygenSaturation(DEFAULT_OXYGEN_SATURATION)
            .weight(DEFAULT_WEIGHT)
            .height(DEFAULT_HEIGHT)
            .bmi(DEFAULT_BMI)
            .nutritionalStatus(DEFAULT_NUTRITIONAL_STATUS)
            .pregnancyScreening(DEFAULT_PREGNANCY_SCREENING)
            .triageNotes(DEFAULT_TRIAGE_NOTES)
            .otherMeasurements(DEFAULT_OTHER_MEASUREMENTS);
        return vitalSigns;
    }

    /**
     * Create an updated entity for this test.
     *
     * This is a static method, as tests for other entities might also need it,
     * if they test an entity which requires the current entity.
     */
    public static VitalSigns createUpdatedEntity(EntityManager em) {
        VitalSigns updatedVitalSigns = new VitalSigns()
            .temperature(UPDATED_TEMPERATURE)
            .pulseRate(UPDATED_PULSE_RATE)
            .systolicBp(UPDATED_SYSTOLIC_BP)
            .diastolicBp(UPDATED_DIASTOLIC_BP)
            .oxygenSaturation(UPDATED_OXYGEN_SATURATION)
            .weight(UPDATED_WEIGHT)
            .height(UPDATED_HEIGHT)
            .bmi(UPDATED_BMI)
            .nutritionalStatus(UPDATED_NUTRITIONAL_STATUS)
            .pregnancyScreening(UPDATED_PREGNANCY_SCREENING)
            .triageNotes(UPDATED_TRIAGE_NOTES)
            .otherMeasurements(UPDATED_OTHER_MEASUREMENTS);
        return updatedVitalSigns;
    }

    @BeforeEach
    void initTest() {
        vitalSigns = createEntity(em);
    }

    @AfterEach
    void cleanup() {
        if (insertedVitalSigns != null) {
            vitalSignsRepository.delete(insertedVitalSigns);
            insertedVitalSigns = null;
        }
    }

    @Test
    @Transactional
    void createVitalSigns() throws Exception {
        long databaseSizeBeforeCreate = getRepositoryCount();
        // Create the VitalSigns
        VitalSignsDTO vitalSignsDTO = vitalSignsMapper.toDto(vitalSigns);
        var returnedVitalSignsDTO = om.readValue(
            restVitalSignsMockMvc
                .perform(post(ENTITY_API_URL).contentType(MediaType.APPLICATION_JSON).content(om.writeValueAsBytes(vitalSignsDTO)))
                .andExpect(status().isCreated())
                .andReturn()
                .getResponse()
                .getContentAsString(),
            VitalSignsDTO.class
        );

        // Validate the VitalSigns in the database
        assertIncrementedRepositoryCount(databaseSizeBeforeCreate);
        var returnedVitalSigns = vitalSignsMapper.toEntity(returnedVitalSignsDTO);
        assertVitalSignsUpdatableFieldsEquals(returnedVitalSigns, getPersistedVitalSigns(returnedVitalSigns));

        insertedVitalSigns = returnedVitalSigns;
    }

    @Test
    @Transactional
    void createVitalSignsWithExistingId() throws Exception {
        // Create the VitalSigns with an existing ID
        vitalSigns.setId(1L);
        VitalSignsDTO vitalSignsDTO = vitalSignsMapper.toDto(vitalSigns);

        long databaseSizeBeforeCreate = getRepositoryCount();

        // An entity with an existing ID cannot be created, so this API call must fail
        restVitalSignsMockMvc
            .perform(post(ENTITY_API_URL).contentType(MediaType.APPLICATION_JSON).content(om.writeValueAsBytes(vitalSignsDTO)))
            .andExpect(status().isBadRequest());

        // Validate the VitalSigns in the database
        assertSameRepositoryCount(databaseSizeBeforeCreate);
    }

    @Test
    @Transactional
    void getAllVitalSignses() throws Exception {
        // Initialize the database
        insertedVitalSigns = vitalSignsRepository.saveAndFlush(vitalSigns);

        // Get all the vitalSignsList
        restVitalSignsMockMvc
            .perform(get(ENTITY_API_URL + "?sort=id,desc"))
            .andExpect(status().isOk())
            .andExpect(content().contentType(MediaType.APPLICATION_JSON_VALUE))
            .andExpect(jsonPath("$.[*].id").value(hasItem(vitalSigns.getId().intValue())))
            .andExpect(jsonPath("$.[*].temperature").value(hasItem(sameNumber(DEFAULT_TEMPERATURE))))
            .andExpect(jsonPath("$.[*].pulseRate").value(hasItem(DEFAULT_PULSE_RATE)))
            .andExpect(jsonPath("$.[*].systolicBp").value(hasItem(DEFAULT_SYSTOLIC_BP)))
            .andExpect(jsonPath("$.[*].diastolicBp").value(hasItem(DEFAULT_DIASTOLIC_BP)))
            .andExpect(jsonPath("$.[*].oxygenSaturation").value(hasItem(DEFAULT_OXYGEN_SATURATION)))
            .andExpect(jsonPath("$.[*].weight").value(hasItem(sameNumber(DEFAULT_WEIGHT))))
            .andExpect(jsonPath("$.[*].height").value(hasItem(sameNumber(DEFAULT_HEIGHT))))
            .andExpect(jsonPath("$.[*].bmi").value(hasItem(sameNumber(DEFAULT_BMI))))
            .andExpect(jsonPath("$.[*].nutritionalStatus").value(hasItem(DEFAULT_NUTRITIONAL_STATUS)))
            .andExpect(jsonPath("$.[*].pregnancyScreening").value(hasItem(DEFAULT_PREGNANCY_SCREENING)))
            .andExpect(jsonPath("$.[*].triageNotes").value(hasItem(DEFAULT_TRIAGE_NOTES)))
            .andExpect(jsonPath("$.[*].otherMeasurements").value(hasItem(DEFAULT_OTHER_MEASUREMENTS)));
    }

    @Test
    @Transactional
    void getVitalSigns() throws Exception {
        // Initialize the database
        insertedVitalSigns = vitalSignsRepository.saveAndFlush(vitalSigns);

        // Get the vitalSigns
        restVitalSignsMockMvc
            .perform(get(ENTITY_API_URL_ID, vitalSigns.getId()))
            .andExpect(status().isOk())
            .andExpect(content().contentType(MediaType.APPLICATION_JSON_VALUE))
            .andExpect(jsonPath("$.id").value(vitalSigns.getId().intValue()))
            .andExpect(jsonPath("$.temperature").value(sameNumber(DEFAULT_TEMPERATURE)))
            .andExpect(jsonPath("$.pulseRate").value(DEFAULT_PULSE_RATE))
            .andExpect(jsonPath("$.systolicBp").value(DEFAULT_SYSTOLIC_BP))
            .andExpect(jsonPath("$.diastolicBp").value(DEFAULT_DIASTOLIC_BP))
            .andExpect(jsonPath("$.oxygenSaturation").value(DEFAULT_OXYGEN_SATURATION))
            .andExpect(jsonPath("$.weight").value(sameNumber(DEFAULT_WEIGHT)))
            .andExpect(jsonPath("$.height").value(sameNumber(DEFAULT_HEIGHT)))
            .andExpect(jsonPath("$.bmi").value(sameNumber(DEFAULT_BMI)))
            .andExpect(jsonPath("$.nutritionalStatus").value(DEFAULT_NUTRITIONAL_STATUS))
            .andExpect(jsonPath("$.pregnancyScreening").value(DEFAULT_PREGNANCY_SCREENING))
            .andExpect(jsonPath("$.triageNotes").value(DEFAULT_TRIAGE_NOTES))
            .andExpect(jsonPath("$.otherMeasurements").value(DEFAULT_OTHER_MEASUREMENTS));
    }

    @Test
    @Transactional
    void getNonExistingVitalSigns() throws Exception {
        // Get the vitalSigns
        restVitalSignsMockMvc.perform(get(ENTITY_API_URL_ID, Long.MAX_VALUE)).andExpect(status().isNotFound());
    }

    @Test
    @Transactional
    void putExistingVitalSigns() throws Exception {
        // Initialize the database
        insertedVitalSigns = vitalSignsRepository.saveAndFlush(vitalSigns);

        long databaseSizeBeforeUpdate = getRepositoryCount();

        // Update the vitalSigns
        VitalSigns updatedVitalSigns = vitalSignsRepository.findById(vitalSigns.getId()).orElseThrow();
        // Disconnect from session so that the updates on updatedVitalSigns are not directly saved in db
        em.detach(updatedVitalSigns);
        updatedVitalSigns
            .temperature(UPDATED_TEMPERATURE)
            .pulseRate(UPDATED_PULSE_RATE)
            .systolicBp(UPDATED_SYSTOLIC_BP)
            .diastolicBp(UPDATED_DIASTOLIC_BP)
            .oxygenSaturation(UPDATED_OXYGEN_SATURATION)
            .weight(UPDATED_WEIGHT)
            .height(UPDATED_HEIGHT)
            .bmi(UPDATED_BMI)
            .nutritionalStatus(UPDATED_NUTRITIONAL_STATUS)
            .pregnancyScreening(UPDATED_PREGNANCY_SCREENING)
            .triageNotes(UPDATED_TRIAGE_NOTES)
            .otherMeasurements(UPDATED_OTHER_MEASUREMENTS);
        VitalSignsDTO vitalSignsDTO = vitalSignsMapper.toDto(updatedVitalSigns);

        restVitalSignsMockMvc
            .perform(
                put(ENTITY_API_URL_ID, vitalSignsDTO.getId())
                    .contentType(MediaType.APPLICATION_JSON)
                    .content(om.writeValueAsBytes(vitalSignsDTO))
            )
            .andExpect(status().isOk());

        // Validate the VitalSigns in the database
        assertSameRepositoryCount(databaseSizeBeforeUpdate);
        assertPersistedVitalSignsToMatchAllProperties(updatedVitalSigns);
    }

    @Test
    @Transactional
    void putNonExistingVitalSigns() throws Exception {
        long databaseSizeBeforeUpdate = getRepositoryCount();
        vitalSigns.setId(longCount.incrementAndGet());

        // Create the VitalSigns
        VitalSignsDTO vitalSignsDTO = vitalSignsMapper.toDto(vitalSigns);

        // If the entity doesn't have an ID, it will throw BadRequestAlertException
        restVitalSignsMockMvc
            .perform(
                put(ENTITY_API_URL_ID, vitalSignsDTO.getId())
                    .contentType(MediaType.APPLICATION_JSON)
                    .content(om.writeValueAsBytes(vitalSignsDTO))
            )
            .andExpect(status().isBadRequest());

        // Validate the VitalSigns in the database
        assertSameRepositoryCount(databaseSizeBeforeUpdate);
    }

    @Test
    @Transactional
    void putWithIdMismatchVitalSigns() throws Exception {
        long databaseSizeBeforeUpdate = getRepositoryCount();
        vitalSigns.setId(longCount.incrementAndGet());

        // Create the VitalSigns
        VitalSignsDTO vitalSignsDTO = vitalSignsMapper.toDto(vitalSigns);

        // If url ID doesn't match entity ID, it will throw BadRequestAlertException
        restVitalSignsMockMvc
            .perform(
                put(ENTITY_API_URL_ID, longCount.incrementAndGet())
                    .contentType(MediaType.APPLICATION_JSON)
                    .content(om.writeValueAsBytes(vitalSignsDTO))
            )
            .andExpect(status().isBadRequest());

        // Validate the VitalSigns in the database
        assertSameRepositoryCount(databaseSizeBeforeUpdate);
    }

    @Test
    @Transactional
    void putWithMissingIdPathParamVitalSigns() throws Exception {
        long databaseSizeBeforeUpdate = getRepositoryCount();
        vitalSigns.setId(longCount.incrementAndGet());

        // Create the VitalSigns
        VitalSignsDTO vitalSignsDTO = vitalSignsMapper.toDto(vitalSigns);

        // If url ID doesn't match entity ID, it will throw BadRequestAlertException
        restVitalSignsMockMvc
            .perform(put(ENTITY_API_URL).contentType(MediaType.APPLICATION_JSON).content(om.writeValueAsBytes(vitalSignsDTO)))
            .andExpect(status().isMethodNotAllowed());

        // Validate the VitalSigns in the database
        assertSameRepositoryCount(databaseSizeBeforeUpdate);
    }

    @Test
    @Transactional
    void partialUpdateVitalSignsWithPatch() throws Exception {
        // Initialize the database
        insertedVitalSigns = vitalSignsRepository.saveAndFlush(vitalSigns);

        long databaseSizeBeforeUpdate = getRepositoryCount();

        // Update the vitalSigns using partial update
        VitalSigns partialUpdatedVitalSigns = new VitalSigns();
        partialUpdatedVitalSigns.setId(vitalSigns.getId());

        partialUpdatedVitalSigns
            .temperature(UPDATED_TEMPERATURE)
            .diastolicBp(UPDATED_DIASTOLIC_BP)
            .weight(UPDATED_WEIGHT)
            .nutritionalStatus(UPDATED_NUTRITIONAL_STATUS)
            .triageNotes(UPDATED_TRIAGE_NOTES)
            .otherMeasurements(UPDATED_OTHER_MEASUREMENTS);

        restVitalSignsMockMvc
            .perform(
                patch(ENTITY_API_URL_ID, partialUpdatedVitalSigns.getId())
                    .contentType("application/merge-patch+json")
                    .content(om.writeValueAsBytes(partialUpdatedVitalSigns))
            )
            .andExpect(status().isOk());

        // Validate the VitalSigns in the database

        assertSameRepositoryCount(databaseSizeBeforeUpdate);
        assertVitalSignsUpdatableFieldsEquals(
            createUpdateProxyForBean(partialUpdatedVitalSigns, vitalSigns),
            getPersistedVitalSigns(vitalSigns)
        );
    }

    @Test
    @Transactional
    void fullUpdateVitalSignsWithPatch() throws Exception {
        // Initialize the database
        insertedVitalSigns = vitalSignsRepository.saveAndFlush(vitalSigns);

        long databaseSizeBeforeUpdate = getRepositoryCount();

        // Update the vitalSigns using partial update
        VitalSigns partialUpdatedVitalSigns = new VitalSigns();
        partialUpdatedVitalSigns.setId(vitalSigns.getId());

        partialUpdatedVitalSigns
            .temperature(UPDATED_TEMPERATURE)
            .pulseRate(UPDATED_PULSE_RATE)
            .systolicBp(UPDATED_SYSTOLIC_BP)
            .diastolicBp(UPDATED_DIASTOLIC_BP)
            .oxygenSaturation(UPDATED_OXYGEN_SATURATION)
            .weight(UPDATED_WEIGHT)
            .height(UPDATED_HEIGHT)
            .bmi(UPDATED_BMI)
            .nutritionalStatus(UPDATED_NUTRITIONAL_STATUS)
            .pregnancyScreening(UPDATED_PREGNANCY_SCREENING)
            .triageNotes(UPDATED_TRIAGE_NOTES)
            .otherMeasurements(UPDATED_OTHER_MEASUREMENTS);

        restVitalSignsMockMvc
            .perform(
                patch(ENTITY_API_URL_ID, partialUpdatedVitalSigns.getId())
                    .contentType("application/merge-patch+json")
                    .content(om.writeValueAsBytes(partialUpdatedVitalSigns))
            )
            .andExpect(status().isOk());

        // Validate the VitalSigns in the database

        assertSameRepositoryCount(databaseSizeBeforeUpdate);
        assertVitalSignsUpdatableFieldsEquals(partialUpdatedVitalSigns, getPersistedVitalSigns(partialUpdatedVitalSigns));
    }

    @Test
    @Transactional
    void patchNonExistingVitalSigns() throws Exception {
        long databaseSizeBeforeUpdate = getRepositoryCount();
        vitalSigns.setId(longCount.incrementAndGet());

        // Create the VitalSigns
        VitalSignsDTO vitalSignsDTO = vitalSignsMapper.toDto(vitalSigns);

        // If the entity doesn't have an ID, it will throw BadRequestAlertException
        restVitalSignsMockMvc
            .perform(
                patch(ENTITY_API_URL_ID, vitalSignsDTO.getId())
                    .contentType("application/merge-patch+json")
                    .content(om.writeValueAsBytes(vitalSignsDTO))
            )
            .andExpect(status().isBadRequest());

        // Validate the VitalSigns in the database
        assertSameRepositoryCount(databaseSizeBeforeUpdate);
    }

    @Test
    @Transactional
    void patchWithIdMismatchVitalSigns() throws Exception {
        long databaseSizeBeforeUpdate = getRepositoryCount();
        vitalSigns.setId(longCount.incrementAndGet());

        // Create the VitalSigns
        VitalSignsDTO vitalSignsDTO = vitalSignsMapper.toDto(vitalSigns);

        // If url ID doesn't match entity ID, it will throw BadRequestAlertException
        restVitalSignsMockMvc
            .perform(
                patch(ENTITY_API_URL_ID, longCount.incrementAndGet())
                    .contentType("application/merge-patch+json")
                    .content(om.writeValueAsBytes(vitalSignsDTO))
            )
            .andExpect(status().isBadRequest());

        // Validate the VitalSigns in the database
        assertSameRepositoryCount(databaseSizeBeforeUpdate);
    }

    @Test
    @Transactional
    void patchWithMissingIdPathParamVitalSigns() throws Exception {
        long databaseSizeBeforeUpdate = getRepositoryCount();
        vitalSigns.setId(longCount.incrementAndGet());

        // Create the VitalSigns
        VitalSignsDTO vitalSignsDTO = vitalSignsMapper.toDto(vitalSigns);

        // If url ID doesn't match entity ID, it will throw BadRequestAlertException
        restVitalSignsMockMvc
            .perform(patch(ENTITY_API_URL).contentType("application/merge-patch+json").content(om.writeValueAsBytes(vitalSignsDTO)))
            .andExpect(status().isMethodNotAllowed());

        // Validate the VitalSigns in the database
        assertSameRepositoryCount(databaseSizeBeforeUpdate);
    }

    @Test
    @Transactional
    void deleteVitalSigns() throws Exception {
        // Initialize the database
        insertedVitalSigns = vitalSignsRepository.saveAndFlush(vitalSigns);

        long databaseSizeBeforeDelete = getRepositoryCount();

        // Delete the vitalSigns
        restVitalSignsMockMvc
            .perform(delete(ENTITY_API_URL_ID, vitalSigns.getId()).accept(MediaType.APPLICATION_JSON))
            .andExpect(status().isNoContent());

        // Validate the database contains one less item
        assertDecrementedRepositoryCount(databaseSizeBeforeDelete);
    }

    protected long getRepositoryCount() {
        return vitalSignsRepository.count();
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

    protected VitalSigns getPersistedVitalSigns(VitalSigns vitalSigns) {
        return vitalSignsRepository.findById(vitalSigns.getId()).orElseThrow();
    }

    protected void assertPersistedVitalSignsToMatchAllProperties(VitalSigns expectedVitalSigns) {
        assertVitalSignsAllPropertiesEquals(expectedVitalSigns, getPersistedVitalSigns(expectedVitalSigns));
    }

    protected void assertPersistedVitalSignsToMatchUpdatableProperties(VitalSigns expectedVitalSigns) {
        assertVitalSignsAllUpdatablePropertiesEquals(expectedVitalSigns, getPersistedVitalSigns(expectedVitalSigns));
    }
}
