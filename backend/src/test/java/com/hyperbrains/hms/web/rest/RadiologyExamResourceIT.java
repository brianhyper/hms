package com.hyperbrains.hms.web.rest;

import static com.hyperbrains.hms.domain.RadiologyExamAsserts.*;
import static com.hyperbrains.hms.web.rest.TestUtil.createUpdateProxyForBean;
import static com.hyperbrains.hms.web.rest.TestUtil.sameNumber;
import static org.assertj.core.api.Assertions.assertThat;
import static org.hamcrest.Matchers.hasItem;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.hyperbrains.hms.IntegrationTest;
import com.hyperbrains.hms.domain.RadiologyExam;
import com.hyperbrains.hms.repository.RadiologyExamRepository;
import com.hyperbrains.hms.service.dto.RadiologyExamDTO;
import com.hyperbrains.hms.service.mapper.RadiologyExamMapper;
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
 * Integration tests for the {@link RadiologyExamResource} REST controller.
 */
@IntegrationTest
@AutoConfigureMockMvc
@WithMockUser(authorities = AuthoritiesConstants.ADMIN)
class RadiologyExamResourceIT {

    private static final String DEFAULT_NAME = "AAAAAAAAAA";
    private static final String UPDATED_NAME = "BBBBBBBBBB";

    private static final BigDecimal DEFAULT_PRICE = new BigDecimal(0);
    private static final BigDecimal UPDATED_PRICE = new BigDecimal(1);

    private static final Boolean DEFAULT_ACTIVE = false;
    private static final Boolean UPDATED_ACTIVE = true;

    private static final String ENTITY_API_URL = "/api/radiology-exams";
    private static final String ENTITY_API_URL_ID = ENTITY_API_URL + "/{id}";

    private static final Random random = new Random();
    private static final AtomicLong longCount = new AtomicLong(random.nextInt() + 2L * Integer.MAX_VALUE);

    @Autowired
    private ObjectMapper om;

    @Autowired
    private RadiologyExamRepository radiologyExamRepository;

    @Autowired
    private RadiologyExamMapper radiologyExamMapper;

    @Autowired
    private EntityManager em;

    @Autowired
    private MockMvc restRadiologyExamMockMvc;

    private RadiologyExam radiologyExam;

    private RadiologyExam insertedRadiologyExam;

    /**
     * Create an entity for this test.
     *
     * This is a static method, as tests for other entities might also need it,
     * if they test an entity which requires the current entity.
     */
    public static RadiologyExam createEntity() {
        return new RadiologyExam().name(DEFAULT_NAME).price(DEFAULT_PRICE).active(DEFAULT_ACTIVE);
    }

    /**
     * Create an updated entity for this test.
     *
     * This is a static method, as tests for other entities might also need it,
     * if they test an entity which requires the current entity.
     */
    public static RadiologyExam createUpdatedEntity() {
        return new RadiologyExam().name(UPDATED_NAME).price(UPDATED_PRICE).active(UPDATED_ACTIVE);
    }

    @BeforeEach
    void initTest() {
        radiologyExam = createEntity();
    }

    @AfterEach
    void cleanup() {
        if (insertedRadiologyExam != null) {
            radiologyExamRepository.delete(insertedRadiologyExam);
            insertedRadiologyExam = null;
        }
    }

    @Test
    @Transactional
    void createRadiologyExam() throws Exception {
        long databaseSizeBeforeCreate = getRepositoryCount();
        // Create the RadiologyExam
        RadiologyExamDTO radiologyExamDTO = radiologyExamMapper.toDto(radiologyExam);
        var returnedRadiologyExamDTO = om.readValue(
            restRadiologyExamMockMvc
                .perform(post(ENTITY_API_URL).contentType(MediaType.APPLICATION_JSON).content(om.writeValueAsBytes(radiologyExamDTO)))
                .andExpect(status().isCreated())
                .andReturn()
                .getResponse()
                .getContentAsString(),
            RadiologyExamDTO.class
        );

        // Validate the RadiologyExam in the database
        assertIncrementedRepositoryCount(databaseSizeBeforeCreate);
        var returnedRadiologyExam = radiologyExamMapper.toEntity(returnedRadiologyExamDTO);
        assertRadiologyExamUpdatableFieldsEquals(returnedRadiologyExam, getPersistedRadiologyExam(returnedRadiologyExam));

        insertedRadiologyExam = returnedRadiologyExam;
    }

    @Test
    @Transactional
    void createRadiologyExamWithExistingId() throws Exception {
        // Create the RadiologyExam with an existing ID
        radiologyExam.setId(1L);
        RadiologyExamDTO radiologyExamDTO = radiologyExamMapper.toDto(radiologyExam);

        long databaseSizeBeforeCreate = getRepositoryCount();

        // An entity with an existing ID cannot be created, so this API call must fail
        restRadiologyExamMockMvc
            .perform(post(ENTITY_API_URL).contentType(MediaType.APPLICATION_JSON).content(om.writeValueAsBytes(radiologyExamDTO)))
            .andExpect(status().isBadRequest());

        // Validate the RadiologyExam in the database
        assertSameRepositoryCount(databaseSizeBeforeCreate);
    }

    @Test
    @Transactional
    void checkNameIsRequired() throws Exception {
        long databaseSizeBeforeTest = getRepositoryCount();
        // set the field null
        radiologyExam.setName(null);

        // Create the RadiologyExam, which fails.
        RadiologyExamDTO radiologyExamDTO = radiologyExamMapper.toDto(radiologyExam);

        restRadiologyExamMockMvc
            .perform(post(ENTITY_API_URL).contentType(MediaType.APPLICATION_JSON).content(om.writeValueAsBytes(radiologyExamDTO)))
            .andExpect(status().isBadRequest());

        assertSameRepositoryCount(databaseSizeBeforeTest);
    }

    @Test
    @Transactional
    void checkPriceIsRequired() throws Exception {
        long databaseSizeBeforeTest = getRepositoryCount();
        // set the field null
        radiologyExam.setPrice(null);

        // Create the RadiologyExam, which fails.
        RadiologyExamDTO radiologyExamDTO = radiologyExamMapper.toDto(radiologyExam);

        restRadiologyExamMockMvc
            .perform(post(ENTITY_API_URL).contentType(MediaType.APPLICATION_JSON).content(om.writeValueAsBytes(radiologyExamDTO)))
            .andExpect(status().isBadRequest());

        assertSameRepositoryCount(databaseSizeBeforeTest);
    }

    @Test
    @Transactional
    void checkActiveIsRequired() throws Exception {
        long databaseSizeBeforeTest = getRepositoryCount();
        // set the field null
        radiologyExam.setActive(null);

        // Create the RadiologyExam, which fails.
        RadiologyExamDTO radiologyExamDTO = radiologyExamMapper.toDto(radiologyExam);

        restRadiologyExamMockMvc
            .perform(post(ENTITY_API_URL).contentType(MediaType.APPLICATION_JSON).content(om.writeValueAsBytes(radiologyExamDTO)))
            .andExpect(status().isBadRequest());

        assertSameRepositoryCount(databaseSizeBeforeTest);
    }

    @Test
    @Transactional
    void getAllRadiologyExams() throws Exception {
        // Initialize the database
        insertedRadiologyExam = radiologyExamRepository.saveAndFlush(radiologyExam);

        // Get all the radiologyExamList
        restRadiologyExamMockMvc
            .perform(get(ENTITY_API_URL + "?sort=id,desc"))
            .andExpect(status().isOk())
            .andExpect(content().contentType(MediaType.APPLICATION_JSON_VALUE))
            .andExpect(jsonPath("$.[*].id").value(hasItem(radiologyExam.getId().intValue())))
            .andExpect(jsonPath("$.[*].name").value(hasItem(DEFAULT_NAME)))
            .andExpect(jsonPath("$.[*].price").value(hasItem(sameNumber(DEFAULT_PRICE))))
            .andExpect(jsonPath("$.[*].active").value(hasItem(DEFAULT_ACTIVE)));
    }

    @Test
    @Transactional
    void getRadiologyExam() throws Exception {
        // Initialize the database
        insertedRadiologyExam = radiologyExamRepository.saveAndFlush(radiologyExam);

        // Get the radiologyExam
        restRadiologyExamMockMvc
            .perform(get(ENTITY_API_URL_ID, radiologyExam.getId()))
            .andExpect(status().isOk())
            .andExpect(content().contentType(MediaType.APPLICATION_JSON_VALUE))
            .andExpect(jsonPath("$.id").value(radiologyExam.getId().intValue()))
            .andExpect(jsonPath("$.name").value(DEFAULT_NAME))
            .andExpect(jsonPath("$.price").value(sameNumber(DEFAULT_PRICE)))
            .andExpect(jsonPath("$.active").value(DEFAULT_ACTIVE));
    }

    @Test
    @Transactional
    void getNonExistingRadiologyExam() throws Exception {
        // Get the radiologyExam
        restRadiologyExamMockMvc.perform(get(ENTITY_API_URL_ID, Long.MAX_VALUE)).andExpect(status().isNotFound());
    }

    @Test
    @Transactional
    void putExistingRadiologyExam() throws Exception {
        // Initialize the database
        insertedRadiologyExam = radiologyExamRepository.saveAndFlush(radiologyExam);

        long databaseSizeBeforeUpdate = getRepositoryCount();

        // Update the radiologyExam
        RadiologyExam updatedRadiologyExam = radiologyExamRepository.findById(radiologyExam.getId()).orElseThrow();
        // Disconnect from session so that the updates on updatedRadiologyExam are not directly saved in db
        em.detach(updatedRadiologyExam);
        updatedRadiologyExam.name(UPDATED_NAME).price(UPDATED_PRICE).active(UPDATED_ACTIVE);
        RadiologyExamDTO radiologyExamDTO = radiologyExamMapper.toDto(updatedRadiologyExam);

        restRadiologyExamMockMvc
            .perform(
                put(ENTITY_API_URL_ID, radiologyExamDTO.getId())
                    .contentType(MediaType.APPLICATION_JSON)
                    .content(om.writeValueAsBytes(radiologyExamDTO))
            )
            .andExpect(status().isOk());

        // Validate the RadiologyExam in the database
        assertSameRepositoryCount(databaseSizeBeforeUpdate);
        assertPersistedRadiologyExamToMatchAllProperties(updatedRadiologyExam);
    }

    @Test
    @Transactional
    void putNonExistingRadiologyExam() throws Exception {
        long databaseSizeBeforeUpdate = getRepositoryCount();
        radiologyExam.setId(longCount.incrementAndGet());

        // Create the RadiologyExam
        RadiologyExamDTO radiologyExamDTO = radiologyExamMapper.toDto(radiologyExam);

        // If the entity doesn't have an ID, it will throw BadRequestAlertException
        restRadiologyExamMockMvc
            .perform(
                put(ENTITY_API_URL_ID, radiologyExamDTO.getId())
                    .contentType(MediaType.APPLICATION_JSON)
                    .content(om.writeValueAsBytes(radiologyExamDTO))
            )
            .andExpect(status().isBadRequest());

        // Validate the RadiologyExam in the database
        assertSameRepositoryCount(databaseSizeBeforeUpdate);
    }

    @Test
    @Transactional
    void putWithIdMismatchRadiologyExam() throws Exception {
        long databaseSizeBeforeUpdate = getRepositoryCount();
        radiologyExam.setId(longCount.incrementAndGet());

        // Create the RadiologyExam
        RadiologyExamDTO radiologyExamDTO = radiologyExamMapper.toDto(radiologyExam);

        // If url ID doesn't match entity ID, it will throw BadRequestAlertException
        restRadiologyExamMockMvc
            .perform(
                put(ENTITY_API_URL_ID, longCount.incrementAndGet())
                    .contentType(MediaType.APPLICATION_JSON)
                    .content(om.writeValueAsBytes(radiologyExamDTO))
            )
            .andExpect(status().isBadRequest());

        // Validate the RadiologyExam in the database
        assertSameRepositoryCount(databaseSizeBeforeUpdate);
    }

    @Test
    @Transactional
    void putWithMissingIdPathParamRadiologyExam() throws Exception {
        long databaseSizeBeforeUpdate = getRepositoryCount();
        radiologyExam.setId(longCount.incrementAndGet());

        // Create the RadiologyExam
        RadiologyExamDTO radiologyExamDTO = radiologyExamMapper.toDto(radiologyExam);

        // If url ID doesn't match entity ID, it will throw BadRequestAlertException
        restRadiologyExamMockMvc
            .perform(put(ENTITY_API_URL).contentType(MediaType.APPLICATION_JSON).content(om.writeValueAsBytes(radiologyExamDTO)))
            .andExpect(status().isMethodNotAllowed());

        // Validate the RadiologyExam in the database
        assertSameRepositoryCount(databaseSizeBeforeUpdate);
    }

    @Test
    @Transactional
    void partialUpdateRadiologyExamWithPatch() throws Exception {
        // Initialize the database
        insertedRadiologyExam = radiologyExamRepository.saveAndFlush(radiologyExam);

        long databaseSizeBeforeUpdate = getRepositoryCount();

        // Update the radiologyExam using partial update
        RadiologyExam partialUpdatedRadiologyExam = new RadiologyExam();
        partialUpdatedRadiologyExam.setId(radiologyExam.getId());

        partialUpdatedRadiologyExam.active(UPDATED_ACTIVE);

        restRadiologyExamMockMvc
            .perform(
                patch(ENTITY_API_URL_ID, partialUpdatedRadiologyExam.getId())
                    .contentType("application/merge-patch+json")
                    .content(om.writeValueAsBytes(partialUpdatedRadiologyExam))
            )
            .andExpect(status().isOk());

        // Validate the RadiologyExam in the database

        assertSameRepositoryCount(databaseSizeBeforeUpdate);
        assertRadiologyExamUpdatableFieldsEquals(
            createUpdateProxyForBean(partialUpdatedRadiologyExam, radiologyExam),
            getPersistedRadiologyExam(radiologyExam)
        );
    }

    @Test
    @Transactional
    void fullUpdateRadiologyExamWithPatch() throws Exception {
        // Initialize the database
        insertedRadiologyExam = radiologyExamRepository.saveAndFlush(radiologyExam);

        long databaseSizeBeforeUpdate = getRepositoryCount();

        // Update the radiologyExam using partial update
        RadiologyExam partialUpdatedRadiologyExam = new RadiologyExam();
        partialUpdatedRadiologyExam.setId(radiologyExam.getId());

        partialUpdatedRadiologyExam.name(UPDATED_NAME).price(UPDATED_PRICE).active(UPDATED_ACTIVE);

        restRadiologyExamMockMvc
            .perform(
                patch(ENTITY_API_URL_ID, partialUpdatedRadiologyExam.getId())
                    .contentType("application/merge-patch+json")
                    .content(om.writeValueAsBytes(partialUpdatedRadiologyExam))
            )
            .andExpect(status().isOk());

        // Validate the RadiologyExam in the database

        assertSameRepositoryCount(databaseSizeBeforeUpdate);
        assertRadiologyExamUpdatableFieldsEquals(partialUpdatedRadiologyExam, getPersistedRadiologyExam(partialUpdatedRadiologyExam));
    }

    @Test
    @Transactional
    void patchNonExistingRadiologyExam() throws Exception {
        long databaseSizeBeforeUpdate = getRepositoryCount();
        radiologyExam.setId(longCount.incrementAndGet());

        // Create the RadiologyExam
        RadiologyExamDTO radiologyExamDTO = radiologyExamMapper.toDto(radiologyExam);

        // If the entity doesn't have an ID, it will throw BadRequestAlertException
        restRadiologyExamMockMvc
            .perform(
                patch(ENTITY_API_URL_ID, radiologyExamDTO.getId())
                    .contentType("application/merge-patch+json")
                    .content(om.writeValueAsBytes(radiologyExamDTO))
            )
            .andExpect(status().isBadRequest());

        // Validate the RadiologyExam in the database
        assertSameRepositoryCount(databaseSizeBeforeUpdate);
    }

    @Test
    @Transactional
    void patchWithIdMismatchRadiologyExam() throws Exception {
        long databaseSizeBeforeUpdate = getRepositoryCount();
        radiologyExam.setId(longCount.incrementAndGet());

        // Create the RadiologyExam
        RadiologyExamDTO radiologyExamDTO = radiologyExamMapper.toDto(radiologyExam);

        // If url ID doesn't match entity ID, it will throw BadRequestAlertException
        restRadiologyExamMockMvc
            .perform(
                patch(ENTITY_API_URL_ID, longCount.incrementAndGet())
                    .contentType("application/merge-patch+json")
                    .content(om.writeValueAsBytes(radiologyExamDTO))
            )
            .andExpect(status().isBadRequest());

        // Validate the RadiologyExam in the database
        assertSameRepositoryCount(databaseSizeBeforeUpdate);
    }

    @Test
    @Transactional
    void patchWithMissingIdPathParamRadiologyExam() throws Exception {
        long databaseSizeBeforeUpdate = getRepositoryCount();
        radiologyExam.setId(longCount.incrementAndGet());

        // Create the RadiologyExam
        RadiologyExamDTO radiologyExamDTO = radiologyExamMapper.toDto(radiologyExam);

        // If url ID doesn't match entity ID, it will throw BadRequestAlertException
        restRadiologyExamMockMvc
            .perform(patch(ENTITY_API_URL).contentType("application/merge-patch+json").content(om.writeValueAsBytes(radiologyExamDTO)))
            .andExpect(status().isMethodNotAllowed());

        // Validate the RadiologyExam in the database
        assertSameRepositoryCount(databaseSizeBeforeUpdate);
    }

    @Test
    @Transactional
    void deleteRadiologyExam() throws Exception {
        // Initialize the database
        insertedRadiologyExam = radiologyExamRepository.saveAndFlush(radiologyExam);

        long databaseSizeBeforeDelete = getRepositoryCount();

        // Delete the radiologyExam
        restRadiologyExamMockMvc
            .perform(delete(ENTITY_API_URL_ID, radiologyExam.getId()).accept(MediaType.APPLICATION_JSON))
            .andExpect(status().isNoContent());

        // Validate the database contains one less item
        assertDecrementedRepositoryCount(databaseSizeBeforeDelete);
    }

    protected long getRepositoryCount() {
        return radiologyExamRepository.count();
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

    protected RadiologyExam getPersistedRadiologyExam(RadiologyExam radiologyExam) {
        return radiologyExamRepository.findById(radiologyExam.getId()).orElseThrow();
    }

    protected void assertPersistedRadiologyExamToMatchAllProperties(RadiologyExam expectedRadiologyExam) {
        assertRadiologyExamAllPropertiesEquals(expectedRadiologyExam, getPersistedRadiologyExam(expectedRadiologyExam));
    }

    protected void assertPersistedRadiologyExamToMatchUpdatableProperties(RadiologyExam expectedRadiologyExam) {
        assertRadiologyExamAllUpdatablePropertiesEquals(expectedRadiologyExam, getPersistedRadiologyExam(expectedRadiologyExam));
    }
}
