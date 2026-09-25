package com.hyperbrains.hms.web.rest;

import static com.hyperbrains.hms.domain.BedTypeAsserts.*;
import static com.hyperbrains.hms.web.rest.TestUtil.createUpdateProxyForBean;
import static com.hyperbrains.hms.web.rest.TestUtil.sameNumber;
import static org.assertj.core.api.Assertions.assertThat;
import static org.hamcrest.Matchers.hasItem;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.hyperbrains.hms.IntegrationTest;
import com.hyperbrains.hms.domain.BedType;
import com.hyperbrains.hms.repository.BedTypeRepository;
import com.hyperbrains.hms.service.dto.BedTypeDTO;
import com.hyperbrains.hms.service.mapper.BedTypeMapper;
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
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.transaction.annotation.Transactional;

/**
 * Integration tests for the {@link BedTypeResource} REST controller.
 */
@IntegrationTest
@AutoConfigureMockMvc
@WithMockUser
class BedTypeResourceIT {

    private static final String DEFAULT_NAME = "AAAAAAAAAA";
    private static final String UPDATED_NAME = "BBBBBBBBBB";

    private static final BigDecimal DEFAULT_DEFAULT_DAILY_RATE = new BigDecimal(0);
    private static final BigDecimal UPDATED_DEFAULT_DAILY_RATE = new BigDecimal(1);

    private static final Boolean DEFAULT_ACTIVE = false;
    private static final Boolean UPDATED_ACTIVE = true;

    private static final String ENTITY_API_URL = "/api/bed-types";
    private static final String ENTITY_API_URL_ID = ENTITY_API_URL + "/{id}";

    private static final Random random = new Random();
    private static final AtomicLong longCount = new AtomicLong(random.nextInt() + 2L * Integer.MAX_VALUE);

    @Autowired
    private ObjectMapper om;

    @Autowired
    private BedTypeRepository bedTypeRepository;

    @Autowired
    private BedTypeMapper bedTypeMapper;

    @Autowired
    private EntityManager em;

    @Autowired
    private MockMvc restBedTypeMockMvc;

    private BedType bedType;

    private BedType insertedBedType;

    /**
     * Create an entity for this test.
     *
     * This is a static method, as tests for other entities might also need it,
     * if they test an entity which requires the current entity.
     */
    public static BedType createEntity() {
        return new BedType().name(DEFAULT_NAME).defaultDailyRate(DEFAULT_DEFAULT_DAILY_RATE).active(DEFAULT_ACTIVE);
    }

    /**
     * Create an updated entity for this test.
     *
     * This is a static method, as tests for other entities might also need it,
     * if they test an entity which requires the current entity.
     */
    public static BedType createUpdatedEntity() {
        return new BedType().name(UPDATED_NAME).defaultDailyRate(UPDATED_DEFAULT_DAILY_RATE).active(UPDATED_ACTIVE);
    }

    @BeforeEach
    void initTest() {
        bedType = createEntity();
    }

    @AfterEach
    void cleanup() {
        if (insertedBedType != null) {
            bedTypeRepository.delete(insertedBedType);
            insertedBedType = null;
        }
    }

    @Test
    @Transactional
    void createBedType() throws Exception {
        long databaseSizeBeforeCreate = getRepositoryCount();
        // Create the BedType
        BedTypeDTO bedTypeDTO = bedTypeMapper.toDto(bedType);
        var returnedBedTypeDTO = om.readValue(
            restBedTypeMockMvc
                .perform(post(ENTITY_API_URL).contentType(MediaType.APPLICATION_JSON).content(om.writeValueAsBytes(bedTypeDTO)))
                .andExpect(status().isCreated())
                .andReturn()
                .getResponse()
                .getContentAsString(),
            BedTypeDTO.class
        );

        // Validate the BedType in the database
        assertIncrementedRepositoryCount(databaseSizeBeforeCreate);
        var returnedBedType = bedTypeMapper.toEntity(returnedBedTypeDTO);
        assertBedTypeUpdatableFieldsEquals(returnedBedType, getPersistedBedType(returnedBedType));

        insertedBedType = returnedBedType;
    }

    @Test
    @Transactional
    void createBedTypeWithExistingId() throws Exception {
        // Create the BedType with an existing ID
        bedType.setId(1L);
        BedTypeDTO bedTypeDTO = bedTypeMapper.toDto(bedType);

        long databaseSizeBeforeCreate = getRepositoryCount();

        // An entity with an existing ID cannot be created, so this API call must fail
        restBedTypeMockMvc
            .perform(post(ENTITY_API_URL).contentType(MediaType.APPLICATION_JSON).content(om.writeValueAsBytes(bedTypeDTO)))
            .andExpect(status().isBadRequest());

        // Validate the BedType in the database
        assertSameRepositoryCount(databaseSizeBeforeCreate);
    }

    @Test
    @Transactional
    void checkNameIsRequired() throws Exception {
        long databaseSizeBeforeTest = getRepositoryCount();
        // set the field null
        bedType.setName(null);

        // Create the BedType, which fails.
        BedTypeDTO bedTypeDTO = bedTypeMapper.toDto(bedType);

        restBedTypeMockMvc
            .perform(post(ENTITY_API_URL).contentType(MediaType.APPLICATION_JSON).content(om.writeValueAsBytes(bedTypeDTO)))
            .andExpect(status().isBadRequest());

        assertSameRepositoryCount(databaseSizeBeforeTest);
    }

    @Test
    @Transactional
    void checkDefaultDailyRateIsRequired() throws Exception {
        long databaseSizeBeforeTest = getRepositoryCount();
        // set the field null
        bedType.setDefaultDailyRate(null);

        // Create the BedType, which fails.
        BedTypeDTO bedTypeDTO = bedTypeMapper.toDto(bedType);

        restBedTypeMockMvc
            .perform(post(ENTITY_API_URL).contentType(MediaType.APPLICATION_JSON).content(om.writeValueAsBytes(bedTypeDTO)))
            .andExpect(status().isBadRequest());

        assertSameRepositoryCount(databaseSizeBeforeTest);
    }

    @Test
    @Transactional
    void checkActiveIsRequired() throws Exception {
        long databaseSizeBeforeTest = getRepositoryCount();
        // set the field null
        bedType.setActive(null);

        // Create the BedType, which fails.
        BedTypeDTO bedTypeDTO = bedTypeMapper.toDto(bedType);

        restBedTypeMockMvc
            .perform(post(ENTITY_API_URL).contentType(MediaType.APPLICATION_JSON).content(om.writeValueAsBytes(bedTypeDTO)))
            .andExpect(status().isBadRequest());

        assertSameRepositoryCount(databaseSizeBeforeTest);
    }

    @Test
    @Transactional
    void getAllBedTypes() throws Exception {
        // Initialize the database
        insertedBedType = bedTypeRepository.saveAndFlush(bedType);

        // Get all the bedTypeList
        restBedTypeMockMvc
            .perform(get(ENTITY_API_URL + "?sort=id,desc"))
            .andExpect(status().isOk())
            .andExpect(content().contentType(MediaType.APPLICATION_JSON_VALUE))
            .andExpect(jsonPath("$.[*].id").value(hasItem(bedType.getId().intValue())))
            .andExpect(jsonPath("$.[*].name").value(hasItem(DEFAULT_NAME)))
            .andExpect(jsonPath("$.[*].defaultDailyRate").value(hasItem(sameNumber(DEFAULT_DEFAULT_DAILY_RATE))))
            .andExpect(jsonPath("$.[*].active").value(hasItem(DEFAULT_ACTIVE)));
    }

    @Test
    @Transactional
    void getBedType() throws Exception {
        // Initialize the database
        insertedBedType = bedTypeRepository.saveAndFlush(bedType);

        // Get the bedType
        restBedTypeMockMvc
            .perform(get(ENTITY_API_URL_ID, bedType.getId()))
            .andExpect(status().isOk())
            .andExpect(content().contentType(MediaType.APPLICATION_JSON_VALUE))
            .andExpect(jsonPath("$.id").value(bedType.getId().intValue()))
            .andExpect(jsonPath("$.name").value(DEFAULT_NAME))
            .andExpect(jsonPath("$.defaultDailyRate").value(sameNumber(DEFAULT_DEFAULT_DAILY_RATE)))
            .andExpect(jsonPath("$.active").value(DEFAULT_ACTIVE));
    }

    @Test
    @Transactional
    void getNonExistingBedType() throws Exception {
        // Get the bedType
        restBedTypeMockMvc.perform(get(ENTITY_API_URL_ID, Long.MAX_VALUE)).andExpect(status().isNotFound());
    }

    @Test
    @Transactional
    void putExistingBedType() throws Exception {
        // Initialize the database
        insertedBedType = bedTypeRepository.saveAndFlush(bedType);

        long databaseSizeBeforeUpdate = getRepositoryCount();

        // Update the bedType
        BedType updatedBedType = bedTypeRepository.findById(bedType.getId()).orElseThrow();
        // Disconnect from session so that the updates on updatedBedType are not directly saved in db
        em.detach(updatedBedType);
        updatedBedType.name(UPDATED_NAME).defaultDailyRate(UPDATED_DEFAULT_DAILY_RATE).active(UPDATED_ACTIVE);
        BedTypeDTO bedTypeDTO = bedTypeMapper.toDto(updatedBedType);

        restBedTypeMockMvc
            .perform(
                put(ENTITY_API_URL_ID, bedTypeDTO.getId()).contentType(MediaType.APPLICATION_JSON).content(om.writeValueAsBytes(bedTypeDTO))
            )
            .andExpect(status().isOk());

        // Validate the BedType in the database
        assertSameRepositoryCount(databaseSizeBeforeUpdate);
        assertPersistedBedTypeToMatchAllProperties(updatedBedType);
    }

    @Test
    @Transactional
    void putNonExistingBedType() throws Exception {
        long databaseSizeBeforeUpdate = getRepositoryCount();
        bedType.setId(longCount.incrementAndGet());

        // Create the BedType
        BedTypeDTO bedTypeDTO = bedTypeMapper.toDto(bedType);

        // If the entity doesn't have an ID, it will throw BadRequestAlertException
        restBedTypeMockMvc
            .perform(
                put(ENTITY_API_URL_ID, bedTypeDTO.getId()).contentType(MediaType.APPLICATION_JSON).content(om.writeValueAsBytes(bedTypeDTO))
            )
            .andExpect(status().isBadRequest());

        // Validate the BedType in the database
        assertSameRepositoryCount(databaseSizeBeforeUpdate);
    }

    @Test
    @Transactional
    void putWithIdMismatchBedType() throws Exception {
        long databaseSizeBeforeUpdate = getRepositoryCount();
        bedType.setId(longCount.incrementAndGet());

        // Create the BedType
        BedTypeDTO bedTypeDTO = bedTypeMapper.toDto(bedType);

        // If url ID doesn't match entity ID, it will throw BadRequestAlertException
        restBedTypeMockMvc
            .perform(
                put(ENTITY_API_URL_ID, longCount.incrementAndGet())
                    .contentType(MediaType.APPLICATION_JSON)
                    .content(om.writeValueAsBytes(bedTypeDTO))
            )
            .andExpect(status().isBadRequest());

        // Validate the BedType in the database
        assertSameRepositoryCount(databaseSizeBeforeUpdate);
    }

    @Test
    @Transactional
    void putWithMissingIdPathParamBedType() throws Exception {
        long databaseSizeBeforeUpdate = getRepositoryCount();
        bedType.setId(longCount.incrementAndGet());

        // Create the BedType
        BedTypeDTO bedTypeDTO = bedTypeMapper.toDto(bedType);

        // If url ID doesn't match entity ID, it will throw BadRequestAlertException
        restBedTypeMockMvc
            .perform(put(ENTITY_API_URL).contentType(MediaType.APPLICATION_JSON).content(om.writeValueAsBytes(bedTypeDTO)))
            .andExpect(status().isMethodNotAllowed());

        // Validate the BedType in the database
        assertSameRepositoryCount(databaseSizeBeforeUpdate);
    }

    @Test
    @Transactional
    void partialUpdateBedTypeWithPatch() throws Exception {
        // Initialize the database
        insertedBedType = bedTypeRepository.saveAndFlush(bedType);

        long databaseSizeBeforeUpdate = getRepositoryCount();

        // Update the bedType using partial update
        BedType partialUpdatedBedType = new BedType();
        partialUpdatedBedType.setId(bedType.getId());

        partialUpdatedBedType.name(UPDATED_NAME);

        restBedTypeMockMvc
            .perform(
                patch(ENTITY_API_URL_ID, partialUpdatedBedType.getId())
                    .contentType("application/merge-patch+json")
                    .content(om.writeValueAsBytes(partialUpdatedBedType))
            )
            .andExpect(status().isOk());

        // Validate the BedType in the database

        assertSameRepositoryCount(databaseSizeBeforeUpdate);
        assertBedTypeUpdatableFieldsEquals(createUpdateProxyForBean(partialUpdatedBedType, bedType), getPersistedBedType(bedType));
    }

    @Test
    @Transactional
    void fullUpdateBedTypeWithPatch() throws Exception {
        // Initialize the database
        insertedBedType = bedTypeRepository.saveAndFlush(bedType);

        long databaseSizeBeforeUpdate = getRepositoryCount();

        // Update the bedType using partial update
        BedType partialUpdatedBedType = new BedType();
        partialUpdatedBedType.setId(bedType.getId());

        partialUpdatedBedType.name(UPDATED_NAME).defaultDailyRate(UPDATED_DEFAULT_DAILY_RATE).active(UPDATED_ACTIVE);

        restBedTypeMockMvc
            .perform(
                patch(ENTITY_API_URL_ID, partialUpdatedBedType.getId())
                    .contentType("application/merge-patch+json")
                    .content(om.writeValueAsBytes(partialUpdatedBedType))
            )
            .andExpect(status().isOk());

        // Validate the BedType in the database

        assertSameRepositoryCount(databaseSizeBeforeUpdate);
        assertBedTypeUpdatableFieldsEquals(partialUpdatedBedType, getPersistedBedType(partialUpdatedBedType));
    }

    @Test
    @Transactional
    void patchNonExistingBedType() throws Exception {
        long databaseSizeBeforeUpdate = getRepositoryCount();
        bedType.setId(longCount.incrementAndGet());

        // Create the BedType
        BedTypeDTO bedTypeDTO = bedTypeMapper.toDto(bedType);

        // If the entity doesn't have an ID, it will throw BadRequestAlertException
        restBedTypeMockMvc
            .perform(
                patch(ENTITY_API_URL_ID, bedTypeDTO.getId())
                    .contentType("application/merge-patch+json")
                    .content(om.writeValueAsBytes(bedTypeDTO))
            )
            .andExpect(status().isBadRequest());

        // Validate the BedType in the database
        assertSameRepositoryCount(databaseSizeBeforeUpdate);
    }

    @Test
    @Transactional
    void patchWithIdMismatchBedType() throws Exception {
        long databaseSizeBeforeUpdate = getRepositoryCount();
        bedType.setId(longCount.incrementAndGet());

        // Create the BedType
        BedTypeDTO bedTypeDTO = bedTypeMapper.toDto(bedType);

        // If url ID doesn't match entity ID, it will throw BadRequestAlertException
        restBedTypeMockMvc
            .perform(
                patch(ENTITY_API_URL_ID, longCount.incrementAndGet())
                    .contentType("application/merge-patch+json")
                    .content(om.writeValueAsBytes(bedTypeDTO))
            )
            .andExpect(status().isBadRequest());

        // Validate the BedType in the database
        assertSameRepositoryCount(databaseSizeBeforeUpdate);
    }

    @Test
    @Transactional
    void patchWithMissingIdPathParamBedType() throws Exception {
        long databaseSizeBeforeUpdate = getRepositoryCount();
        bedType.setId(longCount.incrementAndGet());

        // Create the BedType
        BedTypeDTO bedTypeDTO = bedTypeMapper.toDto(bedType);

        // If url ID doesn't match entity ID, it will throw BadRequestAlertException
        restBedTypeMockMvc
            .perform(patch(ENTITY_API_URL).contentType("application/merge-patch+json").content(om.writeValueAsBytes(bedTypeDTO)))
            .andExpect(status().isMethodNotAllowed());

        // Validate the BedType in the database
        assertSameRepositoryCount(databaseSizeBeforeUpdate);
    }

    @Test
    @Transactional
    void deleteBedType() throws Exception {
        // Initialize the database
        insertedBedType = bedTypeRepository.saveAndFlush(bedType);

        long databaseSizeBeforeDelete = getRepositoryCount();

        // Delete the bedType
        restBedTypeMockMvc
            .perform(delete(ENTITY_API_URL_ID, bedType.getId()).accept(MediaType.APPLICATION_JSON))
            .andExpect(status().isNoContent());

        // Validate the database contains one less item
        assertDecrementedRepositoryCount(databaseSizeBeforeDelete);
    }

    protected long getRepositoryCount() {
        return bedTypeRepository.count();
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

    protected BedType getPersistedBedType(BedType bedType) {
        return bedTypeRepository.findById(bedType.getId()).orElseThrow();
    }

    protected void assertPersistedBedTypeToMatchAllProperties(BedType expectedBedType) {
        assertBedTypeAllPropertiesEquals(expectedBedType, getPersistedBedType(expectedBedType));
    }

    protected void assertPersistedBedTypeToMatchUpdatableProperties(BedType expectedBedType) {
        assertBedTypeAllUpdatablePropertiesEquals(expectedBedType, getPersistedBedType(expectedBedType));
    }
}
