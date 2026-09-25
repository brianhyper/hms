package com.hyperbrains.hms.web.rest;

import static com.hyperbrains.hms.domain.LabTestAsserts.*;
import static com.hyperbrains.hms.web.rest.TestUtil.createUpdateProxyForBean;
import static com.hyperbrains.hms.web.rest.TestUtil.sameNumber;
import static org.assertj.core.api.Assertions.assertThat;
import static org.hamcrest.Matchers.hasItem;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.hyperbrains.hms.IntegrationTest;
import com.hyperbrains.hms.domain.LabTest;
import com.hyperbrains.hms.repository.LabTestRepository;
import com.hyperbrains.hms.service.dto.LabTestDTO;
import com.hyperbrains.hms.service.mapper.LabTestMapper;
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
 * Integration tests for the {@link LabTestResource} REST controller.
 */
@IntegrationTest
@AutoConfigureMockMvc
@WithMockUser(authorities = AuthoritiesConstants.ADMIN)
class LabTestResourceIT {

    private static final String DEFAULT_NAME = "AAAAAAAAAA";
    private static final String UPDATED_NAME = "BBBBBBBBBB";

    private static final BigDecimal DEFAULT_PRICE = new BigDecimal(0);
    private static final BigDecimal UPDATED_PRICE = new BigDecimal(1);

    private static final String DEFAULT_SPECIMEN_TYPE = "AAAAAAAAAA";
    private static final String UPDATED_SPECIMEN_TYPE = "BBBBBBBBBB";

    private static final Integer DEFAULT_TURNAROUND_TIME_MINUTES = 0;
    private static final Integer UPDATED_TURNAROUND_TIME_MINUTES = 1;

    private static final Boolean DEFAULT_ACTIVE = false;
    private static final Boolean UPDATED_ACTIVE = true;

    private static final String ENTITY_API_URL = "/api/lab-tests";
    private static final String ENTITY_API_URL_ID = ENTITY_API_URL + "/{id}";

    private static final Random random = new Random();
    private static final AtomicLong longCount = new AtomicLong(random.nextInt() + 2L * Integer.MAX_VALUE);

    @Autowired
    private ObjectMapper om;

    @Autowired
    private LabTestRepository labTestRepository;

    @Autowired
    private LabTestMapper labTestMapper;

    @Autowired
    private EntityManager em;

    @Autowired
    private MockMvc restLabTestMockMvc;

    private LabTest labTest;

    private LabTest insertedLabTest;

    /**
     * Create an entity for this test.
     *
     * This is a static method, as tests for other entities might also need it,
     * if they test an entity which requires the current entity.
     */
    public static LabTest createEntity() {
        return new LabTest()
            .name(DEFAULT_NAME)
            .price(DEFAULT_PRICE)
            .specimenType(DEFAULT_SPECIMEN_TYPE)
            .turnaroundTimeMinutes(DEFAULT_TURNAROUND_TIME_MINUTES)
            .active(DEFAULT_ACTIVE);
    }

    /**
     * Create an updated entity for this test.
     *
     * This is a static method, as tests for other entities might also need it,
     * if they test an entity which requires the current entity.
     */
    public static LabTest createUpdatedEntity() {
        return new LabTest()
            .name(UPDATED_NAME)
            .price(UPDATED_PRICE)
            .specimenType(UPDATED_SPECIMEN_TYPE)
            .turnaroundTimeMinutes(UPDATED_TURNAROUND_TIME_MINUTES)
            .active(UPDATED_ACTIVE);
    }

    @BeforeEach
    void initTest() {
        labTest = createEntity();
    }

    @AfterEach
    void cleanup() {
        if (insertedLabTest != null) {
            labTestRepository.delete(insertedLabTest);
            insertedLabTest = null;
        }
    }

    @Test
    @Transactional
    void createLabTest() throws Exception {
        long databaseSizeBeforeCreate = getRepositoryCount();
        // Create the LabTest
        LabTestDTO labTestDTO = labTestMapper.toDto(labTest);
        var returnedLabTestDTO = om.readValue(
            restLabTestMockMvc
                .perform(post(ENTITY_API_URL).contentType(MediaType.APPLICATION_JSON).content(om.writeValueAsBytes(labTestDTO)))
                .andExpect(status().isCreated())
                .andReturn()
                .getResponse()
                .getContentAsString(),
            LabTestDTO.class
        );

        // Validate the LabTest in the database
        assertIncrementedRepositoryCount(databaseSizeBeforeCreate);
        var returnedLabTest = labTestMapper.toEntity(returnedLabTestDTO);
        assertLabTestUpdatableFieldsEquals(returnedLabTest, getPersistedLabTest(returnedLabTest));

        insertedLabTest = returnedLabTest;
    }

    @Test
    @Transactional
    void createLabTestWithExistingId() throws Exception {
        // Create the LabTest with an existing ID
        labTest.setId(1L);
        LabTestDTO labTestDTO = labTestMapper.toDto(labTest);

        long databaseSizeBeforeCreate = getRepositoryCount();

        // An entity with an existing ID cannot be created, so this API call must fail
        restLabTestMockMvc
            .perform(post(ENTITY_API_URL).contentType(MediaType.APPLICATION_JSON).content(om.writeValueAsBytes(labTestDTO)))
            .andExpect(status().isBadRequest());

        // Validate the LabTest in the database
        assertSameRepositoryCount(databaseSizeBeforeCreate);
    }

    @Test
    @Transactional
    void checkNameIsRequired() throws Exception {
        long databaseSizeBeforeTest = getRepositoryCount();
        // set the field null
        labTest.setName(null);

        // Create the LabTest, which fails.
        LabTestDTO labTestDTO = labTestMapper.toDto(labTest);

        restLabTestMockMvc
            .perform(post(ENTITY_API_URL).contentType(MediaType.APPLICATION_JSON).content(om.writeValueAsBytes(labTestDTO)))
            .andExpect(status().isBadRequest());

        assertSameRepositoryCount(databaseSizeBeforeTest);
    }

    @Test
    @Transactional
    void checkPriceIsRequired() throws Exception {
        long databaseSizeBeforeTest = getRepositoryCount();
        // set the field null
        labTest.setPrice(null);

        // Create the LabTest, which fails.
        LabTestDTO labTestDTO = labTestMapper.toDto(labTest);

        restLabTestMockMvc
            .perform(post(ENTITY_API_URL).contentType(MediaType.APPLICATION_JSON).content(om.writeValueAsBytes(labTestDTO)))
            .andExpect(status().isBadRequest());

        assertSameRepositoryCount(databaseSizeBeforeTest);
    }

    @Test
    @Transactional
    void checkActiveIsRequired() throws Exception {
        long databaseSizeBeforeTest = getRepositoryCount();
        // set the field null
        labTest.setActive(null);

        // Create the LabTest, which fails.
        LabTestDTO labTestDTO = labTestMapper.toDto(labTest);

        restLabTestMockMvc
            .perform(post(ENTITY_API_URL).contentType(MediaType.APPLICATION_JSON).content(om.writeValueAsBytes(labTestDTO)))
            .andExpect(status().isBadRequest());

        assertSameRepositoryCount(databaseSizeBeforeTest);
    }

    @Test
    @Transactional
    void getAllLabTests() throws Exception {
        // Initialize the database
        insertedLabTest = labTestRepository.saveAndFlush(labTest);

        // Get all the labTestList
        restLabTestMockMvc
            .perform(get(ENTITY_API_URL + "?sort=id,desc"))
            .andExpect(status().isOk())
            .andExpect(content().contentType(MediaType.APPLICATION_JSON_VALUE))
            .andExpect(jsonPath("$.[*].id").value(hasItem(labTest.getId().intValue())))
            .andExpect(jsonPath("$.[*].name").value(hasItem(DEFAULT_NAME)))
            .andExpect(jsonPath("$.[*].price").value(hasItem(sameNumber(DEFAULT_PRICE))))
            .andExpect(jsonPath("$.[*].specimenType").value(hasItem(DEFAULT_SPECIMEN_TYPE)))
            .andExpect(jsonPath("$.[*].turnaroundTimeMinutes").value(hasItem(DEFAULT_TURNAROUND_TIME_MINUTES)))
            .andExpect(jsonPath("$.[*].active").value(hasItem(DEFAULT_ACTIVE)));
    }

    @Test
    @Transactional
    void getLabTest() throws Exception {
        // Initialize the database
        insertedLabTest = labTestRepository.saveAndFlush(labTest);

        // Get the labTest
        restLabTestMockMvc
            .perform(get(ENTITY_API_URL_ID, labTest.getId()))
            .andExpect(status().isOk())
            .andExpect(content().contentType(MediaType.APPLICATION_JSON_VALUE))
            .andExpect(jsonPath("$.id").value(labTest.getId().intValue()))
            .andExpect(jsonPath("$.name").value(DEFAULT_NAME))
            .andExpect(jsonPath("$.price").value(sameNumber(DEFAULT_PRICE)))
            .andExpect(jsonPath("$.specimenType").value(DEFAULT_SPECIMEN_TYPE))
            .andExpect(jsonPath("$.turnaroundTimeMinutes").value(DEFAULT_TURNAROUND_TIME_MINUTES))
            .andExpect(jsonPath("$.active").value(DEFAULT_ACTIVE));
    }

    @Test
    @Transactional
    void getNonExistingLabTest() throws Exception {
        // Get the labTest
        restLabTestMockMvc.perform(get(ENTITY_API_URL_ID, Long.MAX_VALUE)).andExpect(status().isNotFound());
    }

    @Test
    @Transactional
    void putExistingLabTest() throws Exception {
        // Initialize the database
        insertedLabTest = labTestRepository.saveAndFlush(labTest);

        long databaseSizeBeforeUpdate = getRepositoryCount();

        // Update the labTest
        LabTest updatedLabTest = labTestRepository.findById(labTest.getId()).orElseThrow();
        // Disconnect from session so that the updates on updatedLabTest are not directly saved in db
        em.detach(updatedLabTest);
        updatedLabTest
            .name(UPDATED_NAME)
            .price(UPDATED_PRICE)
            .specimenType(UPDATED_SPECIMEN_TYPE)
            .turnaroundTimeMinutes(UPDATED_TURNAROUND_TIME_MINUTES)
            .active(UPDATED_ACTIVE);
        LabTestDTO labTestDTO = labTestMapper.toDto(updatedLabTest);

        restLabTestMockMvc
            .perform(
                put(ENTITY_API_URL_ID, labTestDTO.getId()).contentType(MediaType.APPLICATION_JSON).content(om.writeValueAsBytes(labTestDTO))
            )
            .andExpect(status().isOk());

        // Validate the LabTest in the database
        assertSameRepositoryCount(databaseSizeBeforeUpdate);
        assertPersistedLabTestToMatchAllProperties(updatedLabTest);
    }

    @Test
    @Transactional
    void putNonExistingLabTest() throws Exception {
        long databaseSizeBeforeUpdate = getRepositoryCount();
        labTest.setId(longCount.incrementAndGet());

        // Create the LabTest
        LabTestDTO labTestDTO = labTestMapper.toDto(labTest);

        // If the entity doesn't have an ID, it will throw BadRequestAlertException
        restLabTestMockMvc
            .perform(
                put(ENTITY_API_URL_ID, labTestDTO.getId()).contentType(MediaType.APPLICATION_JSON).content(om.writeValueAsBytes(labTestDTO))
            )
            .andExpect(status().isBadRequest());

        // Validate the LabTest in the database
        assertSameRepositoryCount(databaseSizeBeforeUpdate);
    }

    @Test
    @Transactional
    void putWithIdMismatchLabTest() throws Exception {
        long databaseSizeBeforeUpdate = getRepositoryCount();
        labTest.setId(longCount.incrementAndGet());

        // Create the LabTest
        LabTestDTO labTestDTO = labTestMapper.toDto(labTest);

        // If url ID doesn't match entity ID, it will throw BadRequestAlertException
        restLabTestMockMvc
            .perform(
                put(ENTITY_API_URL_ID, longCount.incrementAndGet())
                    .contentType(MediaType.APPLICATION_JSON)
                    .content(om.writeValueAsBytes(labTestDTO))
            )
            .andExpect(status().isBadRequest());

        // Validate the LabTest in the database
        assertSameRepositoryCount(databaseSizeBeforeUpdate);
    }

    @Test
    @Transactional
    void putWithMissingIdPathParamLabTest() throws Exception {
        long databaseSizeBeforeUpdate = getRepositoryCount();
        labTest.setId(longCount.incrementAndGet());

        // Create the LabTest
        LabTestDTO labTestDTO = labTestMapper.toDto(labTest);

        // If url ID doesn't match entity ID, it will throw BadRequestAlertException
        restLabTestMockMvc
            .perform(put(ENTITY_API_URL).contentType(MediaType.APPLICATION_JSON).content(om.writeValueAsBytes(labTestDTO)))
            .andExpect(status().isMethodNotAllowed());

        // Validate the LabTest in the database
        assertSameRepositoryCount(databaseSizeBeforeUpdate);
    }

    @Test
    @Transactional
    void partialUpdateLabTestWithPatch() throws Exception {
        // Initialize the database
        insertedLabTest = labTestRepository.saveAndFlush(labTest);

        long databaseSizeBeforeUpdate = getRepositoryCount();

        // Update the labTest using partial update
        LabTest partialUpdatedLabTest = new LabTest();
        partialUpdatedLabTest.setId(labTest.getId());

        restLabTestMockMvc
            .perform(
                patch(ENTITY_API_URL_ID, partialUpdatedLabTest.getId())
                    .contentType("application/merge-patch+json")
                    .content(om.writeValueAsBytes(partialUpdatedLabTest))
            )
            .andExpect(status().isOk());

        // Validate the LabTest in the database

        assertSameRepositoryCount(databaseSizeBeforeUpdate);
        assertLabTestUpdatableFieldsEquals(createUpdateProxyForBean(partialUpdatedLabTest, labTest), getPersistedLabTest(labTest));
    }

    @Test
    @Transactional
    void fullUpdateLabTestWithPatch() throws Exception {
        // Initialize the database
        insertedLabTest = labTestRepository.saveAndFlush(labTest);

        long databaseSizeBeforeUpdate = getRepositoryCount();

        // Update the labTest using partial update
        LabTest partialUpdatedLabTest = new LabTest();
        partialUpdatedLabTest.setId(labTest.getId());

        partialUpdatedLabTest
            .name(UPDATED_NAME)
            .price(UPDATED_PRICE)
            .specimenType(UPDATED_SPECIMEN_TYPE)
            .turnaroundTimeMinutes(UPDATED_TURNAROUND_TIME_MINUTES)
            .active(UPDATED_ACTIVE);

        restLabTestMockMvc
            .perform(
                patch(ENTITY_API_URL_ID, partialUpdatedLabTest.getId())
                    .contentType("application/merge-patch+json")
                    .content(om.writeValueAsBytes(partialUpdatedLabTest))
            )
            .andExpect(status().isOk());

        // Validate the LabTest in the database

        assertSameRepositoryCount(databaseSizeBeforeUpdate);
        assertLabTestUpdatableFieldsEquals(partialUpdatedLabTest, getPersistedLabTest(partialUpdatedLabTest));
    }

    @Test
    @Transactional
    void patchNonExistingLabTest() throws Exception {
        long databaseSizeBeforeUpdate = getRepositoryCount();
        labTest.setId(longCount.incrementAndGet());

        // Create the LabTest
        LabTestDTO labTestDTO = labTestMapper.toDto(labTest);

        // If the entity doesn't have an ID, it will throw BadRequestAlertException
        restLabTestMockMvc
            .perform(
                patch(ENTITY_API_URL_ID, labTestDTO.getId())
                    .contentType("application/merge-patch+json")
                    .content(om.writeValueAsBytes(labTestDTO))
            )
            .andExpect(status().isBadRequest());

        // Validate the LabTest in the database
        assertSameRepositoryCount(databaseSizeBeforeUpdate);
    }

    @Test
    @Transactional
    void patchWithIdMismatchLabTest() throws Exception {
        long databaseSizeBeforeUpdate = getRepositoryCount();
        labTest.setId(longCount.incrementAndGet());

        // Create the LabTest
        LabTestDTO labTestDTO = labTestMapper.toDto(labTest);

        // If url ID doesn't match entity ID, it will throw BadRequestAlertException
        restLabTestMockMvc
            .perform(
                patch(ENTITY_API_URL_ID, longCount.incrementAndGet())
                    .contentType("application/merge-patch+json")
                    .content(om.writeValueAsBytes(labTestDTO))
            )
            .andExpect(status().isBadRequest());

        // Validate the LabTest in the database
        assertSameRepositoryCount(databaseSizeBeforeUpdate);
    }

    @Test
    @Transactional
    void patchWithMissingIdPathParamLabTest() throws Exception {
        long databaseSizeBeforeUpdate = getRepositoryCount();
        labTest.setId(longCount.incrementAndGet());

        // Create the LabTest
        LabTestDTO labTestDTO = labTestMapper.toDto(labTest);

        // If url ID doesn't match entity ID, it will throw BadRequestAlertException
        restLabTestMockMvc
            .perform(patch(ENTITY_API_URL).contentType("application/merge-patch+json").content(om.writeValueAsBytes(labTestDTO)))
            .andExpect(status().isMethodNotAllowed());

        // Validate the LabTest in the database
        assertSameRepositoryCount(databaseSizeBeforeUpdate);
    }

    @Test
    @Transactional
    void deleteLabTest() throws Exception {
        // Initialize the database
        insertedLabTest = labTestRepository.saveAndFlush(labTest);

        long databaseSizeBeforeDelete = getRepositoryCount();

        // Delete the labTest
        restLabTestMockMvc
            .perform(delete(ENTITY_API_URL_ID, labTest.getId()).accept(MediaType.APPLICATION_JSON))
            .andExpect(status().isNoContent());

        // Validate the database contains one less item
        assertDecrementedRepositoryCount(databaseSizeBeforeDelete);
    }

    protected long getRepositoryCount() {
        return labTestRepository.count();
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

    protected LabTest getPersistedLabTest(LabTest labTest) {
        return labTestRepository.findById(labTest.getId()).orElseThrow();
    }

    protected void assertPersistedLabTestToMatchAllProperties(LabTest expectedLabTest) {
        assertLabTestAllPropertiesEquals(expectedLabTest, getPersistedLabTest(expectedLabTest));
    }

    protected void assertPersistedLabTestToMatchUpdatableProperties(LabTest expectedLabTest) {
        assertLabTestAllUpdatablePropertiesEquals(expectedLabTest, getPersistedLabTest(expectedLabTest));
    }
}
