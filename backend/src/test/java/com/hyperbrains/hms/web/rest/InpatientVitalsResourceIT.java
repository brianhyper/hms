package com.hyperbrains.hms.web.rest;

import static com.hyperbrains.hms.domain.InpatientVitalsAsserts.*;
import static com.hyperbrains.hms.web.rest.TestUtil.createUpdateProxyForBean;
import static com.hyperbrains.hms.web.rest.TestUtil.sameNumber;
import static org.assertj.core.api.Assertions.assertThat;
import static org.hamcrest.Matchers.hasItem;
import static org.mockito.Mockito.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.hyperbrains.hms.IntegrationTest;
import com.hyperbrains.hms.domain.Admission;
import com.hyperbrains.hms.domain.InpatientVitals;
import com.hyperbrains.hms.domain.User;
import com.hyperbrains.hms.repository.InpatientVitalsRepository;
import com.hyperbrains.hms.repository.UserRepository;
import com.hyperbrains.hms.service.InpatientVitalsService;
import com.hyperbrains.hms.service.dto.InpatientVitalsDTO;
import com.hyperbrains.hms.service.mapper.InpatientVitalsMapper;
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
 * Integration tests for the {@link InpatientVitalsResource} REST controller.
 */
@IntegrationTest
@ExtendWith(MockitoExtension.class)
@AutoConfigureMockMvc
@WithMockUser
class InpatientVitalsResourceIT {

    private static final Instant DEFAULT_RECORDED_AT = Instant.ofEpochMilli(0L);
    private static final Instant UPDATED_RECORDED_AT = Instant.ofEpochMilli(1790236635213L);

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

    private static final String DEFAULT_NOTES = "AAAAAAAAAA";
    private static final String UPDATED_NOTES = "BBBBBBBBBB";

    private static final String ENTITY_API_URL = "/api/inpatient-vitals";
    private static final String ENTITY_API_URL_ID = ENTITY_API_URL + "/{id}";

    private static final Random random = new Random();
    private static final AtomicLong longCount = new AtomicLong(random.nextInt() + 2L * Integer.MAX_VALUE);

    @Autowired
    private ObjectMapper om;

    @Autowired
    private InpatientVitalsRepository inpatientVitalsRepository;

    @Autowired
    private UserRepository userRepository;

    @Mock
    private InpatientVitalsRepository inpatientVitalsRepositoryMock;

    @Autowired
    private InpatientVitalsMapper inpatientVitalsMapper;

    @Mock
    private InpatientVitalsService inpatientVitalsServiceMock;

    @Autowired
    private EntityManager em;

    @Autowired
    private MockMvc restInpatientVitalsMockMvc;

    private InpatientVitals inpatientVitals;

    private InpatientVitals insertedInpatientVitals;

    /**
     * Create an entity for this test.
     *
     * This is a static method, as tests for other entities might also need it,
     * if they test an entity which requires the current entity.
     */
    public static InpatientVitals createEntity(EntityManager em) {
        InpatientVitals inpatientVitals = new InpatientVitals()
            .recordedAt(DEFAULT_RECORDED_AT)
            .temperature(DEFAULT_TEMPERATURE)
            .pulseRate(DEFAULT_PULSE_RATE)
            .systolicBp(DEFAULT_SYSTOLIC_BP)
            .diastolicBp(DEFAULT_DIASTOLIC_BP)
            .oxygenSaturation(DEFAULT_OXYGEN_SATURATION)
            .weight(DEFAULT_WEIGHT)
            .height(DEFAULT_HEIGHT)
            .bmi(DEFAULT_BMI)
            .notes(DEFAULT_NOTES);
        // Add required entity
        Admission admission;
        if (TestUtil.findAll(em, Admission.class).isEmpty()) {
            admission = AdmissionResourceIT.createEntity(em);
            em.persist(admission);
            em.flush();
        } else {
            admission = TestUtil.findAll(em, Admission.class).get(0);
        }
        inpatientVitals.setAdmission(admission);
        // Add required entity
        User user = UserResourceIT.createEntity();
        em.persist(user);
        em.flush();
        inpatientVitals.setRecordedBy(user);
        return inpatientVitals;
    }

    /**
     * Create an updated entity for this test.
     *
     * This is a static method, as tests for other entities might also need it,
     * if they test an entity which requires the current entity.
     */
    public static InpatientVitals createUpdatedEntity(EntityManager em) {
        InpatientVitals updatedInpatientVitals = new InpatientVitals()
            .recordedAt(UPDATED_RECORDED_AT)
            .temperature(UPDATED_TEMPERATURE)
            .pulseRate(UPDATED_PULSE_RATE)
            .systolicBp(UPDATED_SYSTOLIC_BP)
            .diastolicBp(UPDATED_DIASTOLIC_BP)
            .oxygenSaturation(UPDATED_OXYGEN_SATURATION)
            .weight(UPDATED_WEIGHT)
            .height(UPDATED_HEIGHT)
            .bmi(UPDATED_BMI)
            .notes(UPDATED_NOTES);
        // Add required entity
        Admission admission;
        if (TestUtil.findAll(em, Admission.class).isEmpty()) {
            admission = AdmissionResourceIT.createUpdatedEntity(em);
            em.persist(admission);
            em.flush();
        } else {
            admission = TestUtil.findAll(em, Admission.class).get(0);
        }
        updatedInpatientVitals.setAdmission(admission);
        // Add required entity
        User user = UserResourceIT.createEntity();
        em.persist(user);
        em.flush();
        updatedInpatientVitals.setRecordedBy(user);
        return updatedInpatientVitals;
    }

    @BeforeEach
    void initTest() {
        inpatientVitals = createEntity(em);
    }

    @AfterEach
    void cleanup() {
        if (insertedInpatientVitals != null) {
            inpatientVitalsRepository.delete(insertedInpatientVitals);
            insertedInpatientVitals = null;
        }
    }

    @Test
    @Transactional
    void createInpatientVitals() throws Exception {
        long databaseSizeBeforeCreate = getRepositoryCount();
        // Create the InpatientVitals
        InpatientVitalsDTO inpatientVitalsDTO = inpatientVitalsMapper.toDto(inpatientVitals);
        var returnedInpatientVitalsDTO = om.readValue(
            restInpatientVitalsMockMvc
                .perform(post(ENTITY_API_URL).contentType(MediaType.APPLICATION_JSON).content(om.writeValueAsBytes(inpatientVitalsDTO)))
                .andExpect(status().isCreated())
                .andReturn()
                .getResponse()
                .getContentAsString(),
            InpatientVitalsDTO.class
        );

        // Validate the InpatientVitals in the database
        assertIncrementedRepositoryCount(databaseSizeBeforeCreate);
        var returnedInpatientVitals = inpatientVitalsMapper.toEntity(returnedInpatientVitalsDTO);
        assertInpatientVitalsUpdatableFieldsEquals(returnedInpatientVitals, getPersistedInpatientVitals(returnedInpatientVitals));

        insertedInpatientVitals = returnedInpatientVitals;
    }

    @Test
    @Transactional
    void createInpatientVitalsWithExistingId() throws Exception {
        // Create the InpatientVitals with an existing ID
        inpatientVitals.setId(1L);
        InpatientVitalsDTO inpatientVitalsDTO = inpatientVitalsMapper.toDto(inpatientVitals);

        long databaseSizeBeforeCreate = getRepositoryCount();

        // An entity with an existing ID cannot be created, so this API call must fail
        restInpatientVitalsMockMvc
            .perform(post(ENTITY_API_URL).contentType(MediaType.APPLICATION_JSON).content(om.writeValueAsBytes(inpatientVitalsDTO)))
            .andExpect(status().isBadRequest());

        // Validate the InpatientVitals in the database
        assertSameRepositoryCount(databaseSizeBeforeCreate);
    }

    @Test
    @Transactional
    void checkRecordedAtIsRequired() throws Exception {
        long databaseSizeBeforeTest = getRepositoryCount();
        // set the field null
        inpatientVitals.setRecordedAt(null);

        // Create the InpatientVitals, which fails.
        InpatientVitalsDTO inpatientVitalsDTO = inpatientVitalsMapper.toDto(inpatientVitals);

        restInpatientVitalsMockMvc
            .perform(post(ENTITY_API_URL).contentType(MediaType.APPLICATION_JSON).content(om.writeValueAsBytes(inpatientVitalsDTO)))
            .andExpect(status().isBadRequest());

        assertSameRepositoryCount(databaseSizeBeforeTest);
    }

    @Test
    @Transactional
    void getAllInpatientVitalses() throws Exception {
        // Initialize the database
        insertedInpatientVitals = inpatientVitalsRepository.saveAndFlush(inpatientVitals);

        // Get all the inpatientVitalsList
        restInpatientVitalsMockMvc
            .perform(get(ENTITY_API_URL + "?sort=id,desc"))
            .andExpect(status().isOk())
            .andExpect(content().contentType(MediaType.APPLICATION_JSON_VALUE))
            .andExpect(jsonPath("$.[*].id").value(hasItem(inpatientVitals.getId().intValue())))
            .andExpect(jsonPath("$.[*].recordedAt").value(hasItem(DEFAULT_RECORDED_AT.toString())))
            .andExpect(jsonPath("$.[*].temperature").value(hasItem(sameNumber(DEFAULT_TEMPERATURE))))
            .andExpect(jsonPath("$.[*].pulseRate").value(hasItem(DEFAULT_PULSE_RATE)))
            .andExpect(jsonPath("$.[*].systolicBp").value(hasItem(DEFAULT_SYSTOLIC_BP)))
            .andExpect(jsonPath("$.[*].diastolicBp").value(hasItem(DEFAULT_DIASTOLIC_BP)))
            .andExpect(jsonPath("$.[*].oxygenSaturation").value(hasItem(DEFAULT_OXYGEN_SATURATION)))
            .andExpect(jsonPath("$.[*].weight").value(hasItem(sameNumber(DEFAULT_WEIGHT))))
            .andExpect(jsonPath("$.[*].height").value(hasItem(sameNumber(DEFAULT_HEIGHT))))
            .andExpect(jsonPath("$.[*].bmi").value(hasItem(sameNumber(DEFAULT_BMI))))
            .andExpect(jsonPath("$.[*].notes").value(hasItem(DEFAULT_NOTES)));
    }

    @SuppressWarnings({ "unchecked" })
    void getAllInpatientVitalsesWithEagerRelationshipsIsEnabled() throws Exception {
        when(inpatientVitalsServiceMock.findAllWithEagerRelationships(any())).thenReturn(new PageImpl(new ArrayList<>()));

        restInpatientVitalsMockMvc.perform(get(ENTITY_API_URL + "?eagerload=true")).andExpect(status().isOk());

        verify(inpatientVitalsServiceMock, times(1)).findAllWithEagerRelationships(any());
    }

    @SuppressWarnings({ "unchecked" })
    void getAllInpatientVitalsesWithEagerRelationshipsIsNotEnabled() throws Exception {
        when(inpatientVitalsServiceMock.findAllWithEagerRelationships(any())).thenReturn(new PageImpl(new ArrayList<>()));

        restInpatientVitalsMockMvc.perform(get(ENTITY_API_URL + "?eagerload=false")).andExpect(status().isOk());
        verify(inpatientVitalsRepositoryMock, times(1)).findAll(any(Pageable.class));
    }

    @Test
    @Transactional
    void getInpatientVitals() throws Exception {
        // Initialize the database
        insertedInpatientVitals = inpatientVitalsRepository.saveAndFlush(inpatientVitals);

        // Get the inpatientVitals
        restInpatientVitalsMockMvc
            .perform(get(ENTITY_API_URL_ID, inpatientVitals.getId()))
            .andExpect(status().isOk())
            .andExpect(content().contentType(MediaType.APPLICATION_JSON_VALUE))
            .andExpect(jsonPath("$.id").value(inpatientVitals.getId().intValue()))
            .andExpect(jsonPath("$.recordedAt").value(DEFAULT_RECORDED_AT.toString()))
            .andExpect(jsonPath("$.temperature").value(sameNumber(DEFAULT_TEMPERATURE)))
            .andExpect(jsonPath("$.pulseRate").value(DEFAULT_PULSE_RATE))
            .andExpect(jsonPath("$.systolicBp").value(DEFAULT_SYSTOLIC_BP))
            .andExpect(jsonPath("$.diastolicBp").value(DEFAULT_DIASTOLIC_BP))
            .andExpect(jsonPath("$.oxygenSaturation").value(DEFAULT_OXYGEN_SATURATION))
            .andExpect(jsonPath("$.weight").value(sameNumber(DEFAULT_WEIGHT)))
            .andExpect(jsonPath("$.height").value(sameNumber(DEFAULT_HEIGHT)))
            .andExpect(jsonPath("$.bmi").value(sameNumber(DEFAULT_BMI)))
            .andExpect(jsonPath("$.notes").value(DEFAULT_NOTES));
    }

    @Test
    @Transactional
    void getNonExistingInpatientVitals() throws Exception {
        // Get the inpatientVitals
        restInpatientVitalsMockMvc.perform(get(ENTITY_API_URL_ID, Long.MAX_VALUE)).andExpect(status().isNotFound());
    }

    @Test
    @Transactional
    void putExistingInpatientVitals() throws Exception {
        // Initialize the database
        insertedInpatientVitals = inpatientVitalsRepository.saveAndFlush(inpatientVitals);

        long databaseSizeBeforeUpdate = getRepositoryCount();

        // Update the inpatientVitals
        InpatientVitals updatedInpatientVitals = inpatientVitalsRepository.findById(inpatientVitals.getId()).orElseThrow();
        // Disconnect from session so that the updates on updatedInpatientVitals are not directly saved in db
        em.detach(updatedInpatientVitals);
        updatedInpatientVitals
            .recordedAt(UPDATED_RECORDED_AT)
            .temperature(UPDATED_TEMPERATURE)
            .pulseRate(UPDATED_PULSE_RATE)
            .systolicBp(UPDATED_SYSTOLIC_BP)
            .diastolicBp(UPDATED_DIASTOLIC_BP)
            .oxygenSaturation(UPDATED_OXYGEN_SATURATION)
            .weight(UPDATED_WEIGHT)
            .height(UPDATED_HEIGHT)
            .bmi(UPDATED_BMI)
            .notes(UPDATED_NOTES);
        InpatientVitalsDTO inpatientVitalsDTO = inpatientVitalsMapper.toDto(updatedInpatientVitals);

        restInpatientVitalsMockMvc
            .perform(
                put(ENTITY_API_URL_ID, inpatientVitalsDTO.getId())
                    .contentType(MediaType.APPLICATION_JSON)
                    .content(om.writeValueAsBytes(inpatientVitalsDTO))
            )
            .andExpect(status().isOk());

        // Validate the InpatientVitals in the database
        assertSameRepositoryCount(databaseSizeBeforeUpdate);
        assertPersistedInpatientVitalsToMatchAllProperties(updatedInpatientVitals);
    }

    @Test
    @Transactional
    void putNonExistingInpatientVitals() throws Exception {
        long databaseSizeBeforeUpdate = getRepositoryCount();
        inpatientVitals.setId(longCount.incrementAndGet());

        // Create the InpatientVitals
        InpatientVitalsDTO inpatientVitalsDTO = inpatientVitalsMapper.toDto(inpatientVitals);

        // If the entity doesn't have an ID, it will throw BadRequestAlertException
        restInpatientVitalsMockMvc
            .perform(
                put(ENTITY_API_URL_ID, inpatientVitalsDTO.getId())
                    .contentType(MediaType.APPLICATION_JSON)
                    .content(om.writeValueAsBytes(inpatientVitalsDTO))
            )
            .andExpect(status().isBadRequest());

        // Validate the InpatientVitals in the database
        assertSameRepositoryCount(databaseSizeBeforeUpdate);
    }

    @Test
    @Transactional
    void putWithIdMismatchInpatientVitals() throws Exception {
        long databaseSizeBeforeUpdate = getRepositoryCount();
        inpatientVitals.setId(longCount.incrementAndGet());

        // Create the InpatientVitals
        InpatientVitalsDTO inpatientVitalsDTO = inpatientVitalsMapper.toDto(inpatientVitals);

        // If url ID doesn't match entity ID, it will throw BadRequestAlertException
        restInpatientVitalsMockMvc
            .perform(
                put(ENTITY_API_URL_ID, longCount.incrementAndGet())
                    .contentType(MediaType.APPLICATION_JSON)
                    .content(om.writeValueAsBytes(inpatientVitalsDTO))
            )
            .andExpect(status().isBadRequest());

        // Validate the InpatientVitals in the database
        assertSameRepositoryCount(databaseSizeBeforeUpdate);
    }

    @Test
    @Transactional
    void putWithMissingIdPathParamInpatientVitals() throws Exception {
        long databaseSizeBeforeUpdate = getRepositoryCount();
        inpatientVitals.setId(longCount.incrementAndGet());

        // Create the InpatientVitals
        InpatientVitalsDTO inpatientVitalsDTO = inpatientVitalsMapper.toDto(inpatientVitals);

        // If url ID doesn't match entity ID, it will throw BadRequestAlertException
        restInpatientVitalsMockMvc
            .perform(put(ENTITY_API_URL).contentType(MediaType.APPLICATION_JSON).content(om.writeValueAsBytes(inpatientVitalsDTO)))
            .andExpect(status().isMethodNotAllowed());

        // Validate the InpatientVitals in the database
        assertSameRepositoryCount(databaseSizeBeforeUpdate);
    }

    @Test
    @Transactional
    void partialUpdateInpatientVitalsWithPatch() throws Exception {
        // Initialize the database
        insertedInpatientVitals = inpatientVitalsRepository.saveAndFlush(inpatientVitals);

        long databaseSizeBeforeUpdate = getRepositoryCount();

        // Update the inpatientVitals using partial update
        InpatientVitals partialUpdatedInpatientVitals = new InpatientVitals();
        partialUpdatedInpatientVitals.setId(inpatientVitals.getId());

        partialUpdatedInpatientVitals
            .temperature(UPDATED_TEMPERATURE)
            .pulseRate(UPDATED_PULSE_RATE)
            .systolicBp(UPDATED_SYSTOLIC_BP)
            .diastolicBp(UPDATED_DIASTOLIC_BP)
            .oxygenSaturation(UPDATED_OXYGEN_SATURATION)
            .bmi(UPDATED_BMI)
            .notes(UPDATED_NOTES);

        restInpatientVitalsMockMvc
            .perform(
                patch(ENTITY_API_URL_ID, partialUpdatedInpatientVitals.getId())
                    .contentType("application/merge-patch+json")
                    .content(om.writeValueAsBytes(partialUpdatedInpatientVitals))
            )
            .andExpect(status().isOk());

        // Validate the InpatientVitals in the database

        assertSameRepositoryCount(databaseSizeBeforeUpdate);
        assertInpatientVitalsUpdatableFieldsEquals(
            createUpdateProxyForBean(partialUpdatedInpatientVitals, inpatientVitals),
            getPersistedInpatientVitals(inpatientVitals)
        );
    }

    @Test
    @Transactional
    void fullUpdateInpatientVitalsWithPatch() throws Exception {
        // Initialize the database
        insertedInpatientVitals = inpatientVitalsRepository.saveAndFlush(inpatientVitals);

        long databaseSizeBeforeUpdate = getRepositoryCount();

        // Update the inpatientVitals using partial update
        InpatientVitals partialUpdatedInpatientVitals = new InpatientVitals();
        partialUpdatedInpatientVitals.setId(inpatientVitals.getId());

        partialUpdatedInpatientVitals
            .recordedAt(UPDATED_RECORDED_AT)
            .temperature(UPDATED_TEMPERATURE)
            .pulseRate(UPDATED_PULSE_RATE)
            .systolicBp(UPDATED_SYSTOLIC_BP)
            .diastolicBp(UPDATED_DIASTOLIC_BP)
            .oxygenSaturation(UPDATED_OXYGEN_SATURATION)
            .weight(UPDATED_WEIGHT)
            .height(UPDATED_HEIGHT)
            .bmi(UPDATED_BMI)
            .notes(UPDATED_NOTES);

        restInpatientVitalsMockMvc
            .perform(
                patch(ENTITY_API_URL_ID, partialUpdatedInpatientVitals.getId())
                    .contentType("application/merge-patch+json")
                    .content(om.writeValueAsBytes(partialUpdatedInpatientVitals))
            )
            .andExpect(status().isOk());

        // Validate the InpatientVitals in the database

        assertSameRepositoryCount(databaseSizeBeforeUpdate);
        assertInpatientVitalsUpdatableFieldsEquals(
            partialUpdatedInpatientVitals,
            getPersistedInpatientVitals(partialUpdatedInpatientVitals)
        );
    }

    @Test
    @Transactional
    void patchNonExistingInpatientVitals() throws Exception {
        long databaseSizeBeforeUpdate = getRepositoryCount();
        inpatientVitals.setId(longCount.incrementAndGet());

        // Create the InpatientVitals
        InpatientVitalsDTO inpatientVitalsDTO = inpatientVitalsMapper.toDto(inpatientVitals);

        // If the entity doesn't have an ID, it will throw BadRequestAlertException
        restInpatientVitalsMockMvc
            .perform(
                patch(ENTITY_API_URL_ID, inpatientVitalsDTO.getId())
                    .contentType("application/merge-patch+json")
                    .content(om.writeValueAsBytes(inpatientVitalsDTO))
            )
            .andExpect(status().isBadRequest());

        // Validate the InpatientVitals in the database
        assertSameRepositoryCount(databaseSizeBeforeUpdate);
    }

    @Test
    @Transactional
    void patchWithIdMismatchInpatientVitals() throws Exception {
        long databaseSizeBeforeUpdate = getRepositoryCount();
        inpatientVitals.setId(longCount.incrementAndGet());

        // Create the InpatientVitals
        InpatientVitalsDTO inpatientVitalsDTO = inpatientVitalsMapper.toDto(inpatientVitals);

        // If url ID doesn't match entity ID, it will throw BadRequestAlertException
        restInpatientVitalsMockMvc
            .perform(
                patch(ENTITY_API_URL_ID, longCount.incrementAndGet())
                    .contentType("application/merge-patch+json")
                    .content(om.writeValueAsBytes(inpatientVitalsDTO))
            )
            .andExpect(status().isBadRequest());

        // Validate the InpatientVitals in the database
        assertSameRepositoryCount(databaseSizeBeforeUpdate);
    }

    @Test
    @Transactional
    void patchWithMissingIdPathParamInpatientVitals() throws Exception {
        long databaseSizeBeforeUpdate = getRepositoryCount();
        inpatientVitals.setId(longCount.incrementAndGet());

        // Create the InpatientVitals
        InpatientVitalsDTO inpatientVitalsDTO = inpatientVitalsMapper.toDto(inpatientVitals);

        // If url ID doesn't match entity ID, it will throw BadRequestAlertException
        restInpatientVitalsMockMvc
            .perform(patch(ENTITY_API_URL).contentType("application/merge-patch+json").content(om.writeValueAsBytes(inpatientVitalsDTO)))
            .andExpect(status().isMethodNotAllowed());

        // Validate the InpatientVitals in the database
        assertSameRepositoryCount(databaseSizeBeforeUpdate);
    }

    @Test
    @Transactional
    void deleteInpatientVitals() throws Exception {
        // Initialize the database
        insertedInpatientVitals = inpatientVitalsRepository.saveAndFlush(inpatientVitals);

        long databaseSizeBeforeDelete = getRepositoryCount();

        // Delete the inpatientVitals
        restInpatientVitalsMockMvc
            .perform(delete(ENTITY_API_URL_ID, inpatientVitals.getId()).accept(MediaType.APPLICATION_JSON))
            .andExpect(status().isNoContent());

        // Validate the database contains one less item
        assertDecrementedRepositoryCount(databaseSizeBeforeDelete);
    }

    protected long getRepositoryCount() {
        return inpatientVitalsRepository.count();
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

    protected InpatientVitals getPersistedInpatientVitals(InpatientVitals inpatientVitals) {
        return inpatientVitalsRepository.findById(inpatientVitals.getId()).orElseThrow();
    }

    protected void assertPersistedInpatientVitalsToMatchAllProperties(InpatientVitals expectedInpatientVitals) {
        assertInpatientVitalsAllPropertiesEquals(expectedInpatientVitals, getPersistedInpatientVitals(expectedInpatientVitals));
    }

    protected void assertPersistedInpatientVitalsToMatchUpdatableProperties(InpatientVitals expectedInpatientVitals) {
        assertInpatientVitalsAllUpdatablePropertiesEquals(expectedInpatientVitals, getPersistedInpatientVitals(expectedInpatientVitals));
    }
}
