package com.hyperbrains.hms.web.rest;

import static com.hyperbrains.hms.domain.BedAsserts.*;
import static com.hyperbrains.hms.web.rest.TestUtil.createUpdateProxyForBean;
import static com.hyperbrains.hms.web.rest.TestUtil.sameNumber;
import static org.assertj.core.api.Assertions.assertThat;
import static org.hamcrest.Matchers.hasItem;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.hyperbrains.hms.IntegrationTest;
import com.hyperbrains.hms.domain.Bed;
import com.hyperbrains.hms.domain.BedType;
import com.hyperbrains.hms.domain.Ward;
import com.hyperbrains.hms.domain.enumeration.BedStatus;
import com.hyperbrains.hms.repository.BedRepository;
import com.hyperbrains.hms.service.dto.BedDTO;
import com.hyperbrains.hms.service.mapper.BedMapper;
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
 * Integration tests for the {@link BedResource} REST controller.
 */
@IntegrationTest
@AutoConfigureMockMvc
@WithMockUser
class BedResourceIT {

    private static final String DEFAULT_BED_NUMBER = "AAAAAAAAAA";
    private static final String UPDATED_BED_NUMBER = "BBBBBBBBBB";

    private static final BigDecimal DEFAULT_DAILY_RATE_OVERRIDE = new BigDecimal(0);
    private static final BigDecimal UPDATED_DAILY_RATE_OVERRIDE = new BigDecimal(1);

    private static final BedStatus DEFAULT_STATUS = BedStatus.AVAILABLE;
    private static final BedStatus UPDATED_STATUS = BedStatus.OCCUPIED;

    private static final String ENTITY_API_URL = "/api/beds";
    private static final String ENTITY_API_URL_ID = ENTITY_API_URL + "/{id}";

    private static final Random random = new Random();
    private static final AtomicLong longCount = new AtomicLong(random.nextInt() + 2L * Integer.MAX_VALUE);

    @Autowired
    private ObjectMapper om;

    @Autowired
    private BedRepository bedRepository;

    @Autowired
    private BedMapper bedMapper;

    @Autowired
    private EntityManager em;

    @Autowired
    private MockMvc restBedMockMvc;

    private Bed bed;

    private Bed insertedBed;

    /**
     * Create an entity for this test.
     *
     * This is a static method, as tests for other entities might also need it,
     * if they test an entity which requires the current entity.
     */
    public static Bed createEntity(EntityManager em) {
        Bed bed = new Bed().bedNumber(DEFAULT_BED_NUMBER).dailyRateOverride(DEFAULT_DAILY_RATE_OVERRIDE).status(DEFAULT_STATUS);
        // Add required entity
        Ward ward;
        if (TestUtil.findAll(em, Ward.class).isEmpty()) {
            ward = WardResourceIT.createEntity(em);
            em.persist(ward);
            em.flush();
        } else {
            ward = TestUtil.findAll(em, Ward.class).get(0);
        }
        bed.setWard(ward);
        // Add required entity
        BedType bedType;
        if (TestUtil.findAll(em, BedType.class).isEmpty()) {
            bedType = BedTypeResourceIT.createEntity();
            em.persist(bedType);
            em.flush();
        } else {
            bedType = TestUtil.findAll(em, BedType.class).get(0);
        }
        bed.setBedType(bedType);
        return bed;
    }

    /**
     * Create an updated entity for this test.
     *
     * This is a static method, as tests for other entities might also need it,
     * if they test an entity which requires the current entity.
     */
    public static Bed createUpdatedEntity(EntityManager em) {
        Bed updatedBed = new Bed().bedNumber(UPDATED_BED_NUMBER).dailyRateOverride(UPDATED_DAILY_RATE_OVERRIDE).status(UPDATED_STATUS);
        // Add required entity
        Ward ward;
        if (TestUtil.findAll(em, Ward.class).isEmpty()) {
            ward = WardResourceIT.createUpdatedEntity(em);
            em.persist(ward);
            em.flush();
        } else {
            ward = TestUtil.findAll(em, Ward.class).get(0);
        }
        updatedBed.setWard(ward);
        // Add required entity
        BedType bedType;
        if (TestUtil.findAll(em, BedType.class).isEmpty()) {
            bedType = BedTypeResourceIT.createUpdatedEntity();
            em.persist(bedType);
            em.flush();
        } else {
            bedType = TestUtil.findAll(em, BedType.class).get(0);
        }
        updatedBed.setBedType(bedType);
        return updatedBed;
    }

    @BeforeEach
    void initTest() {
        bed = createEntity(em);
    }

    @AfterEach
    void cleanup() {
        if (insertedBed != null) {
            // deleteById re-reads the row first so the optimistic-locking check uses the current
            // version. The cached entity is stale whenever a test modified it (Bed carries @Version).
            bedRepository.deleteById(insertedBed.getId());
            insertedBed = null;
        }
    }

    @Test
    @Transactional
    void createBed() throws Exception {
        long databaseSizeBeforeCreate = getRepositoryCount();
        // Create the Bed
        BedDTO bedDTO = bedMapper.toDto(bed);
        var returnedBedDTO = om.readValue(
            restBedMockMvc
                .perform(post(ENTITY_API_URL).contentType(MediaType.APPLICATION_JSON).content(om.writeValueAsBytes(bedDTO)))
                .andExpect(status().isCreated())
                .andReturn()
                .getResponse()
                .getContentAsString(),
            BedDTO.class
        );

        // Validate the Bed in the database
        assertIncrementedRepositoryCount(databaseSizeBeforeCreate);
        var returnedBed = bedMapper.toEntity(returnedBedDTO);
        assertBedUpdatableFieldsEquals(returnedBed, getPersistedBed(returnedBed));

        insertedBed = returnedBed;
    }

    @Test
    @Transactional
    void createBedWithExistingId() throws Exception {
        // Create the Bed with an existing ID
        bed.setId(1L);
        BedDTO bedDTO = bedMapper.toDto(bed);

        long databaseSizeBeforeCreate = getRepositoryCount();

        // An entity with an existing ID cannot be created, so this API call must fail
        restBedMockMvc
            .perform(post(ENTITY_API_URL).contentType(MediaType.APPLICATION_JSON).content(om.writeValueAsBytes(bedDTO)))
            .andExpect(status().isBadRequest());

        // Validate the Bed in the database
        assertSameRepositoryCount(databaseSizeBeforeCreate);
    }

    @Test
    @Transactional
    void checkBedNumberIsRequired() throws Exception {
        long databaseSizeBeforeTest = getRepositoryCount();
        // set the field null
        bed.setBedNumber(null);

        // Create the Bed, which fails.
        BedDTO bedDTO = bedMapper.toDto(bed);

        restBedMockMvc
            .perform(post(ENTITY_API_URL).contentType(MediaType.APPLICATION_JSON).content(om.writeValueAsBytes(bedDTO)))
            .andExpect(status().isBadRequest());

        assertSameRepositoryCount(databaseSizeBeforeTest);
    }

    @Test
    @Transactional
    void checkStatusIsRequired() throws Exception {
        long databaseSizeBeforeTest = getRepositoryCount();
        // set the field null
        bed.setStatus(null);

        // Create the Bed, which fails.
        BedDTO bedDTO = bedMapper.toDto(bed);

        restBedMockMvc
            .perform(post(ENTITY_API_URL).contentType(MediaType.APPLICATION_JSON).content(om.writeValueAsBytes(bedDTO)))
            .andExpect(status().isBadRequest());

        assertSameRepositoryCount(databaseSizeBeforeTest);
    }

    @Test
    @Transactional
    void getAllBeds() throws Exception {
        // Initialize the database
        insertedBed = bedRepository.saveAndFlush(bed);

        // Get all the bedList
        restBedMockMvc
            .perform(get(ENTITY_API_URL + "?sort=id,desc"))
            .andExpect(status().isOk())
            .andExpect(content().contentType(MediaType.APPLICATION_JSON_VALUE))
            .andExpect(jsonPath("$.[*].id").value(hasItem(bed.getId().intValue())))
            .andExpect(jsonPath("$.[*].bedNumber").value(hasItem(DEFAULT_BED_NUMBER)))
            .andExpect(jsonPath("$.[*].dailyRateOverride").value(hasItem(sameNumber(DEFAULT_DAILY_RATE_OVERRIDE))))
            .andExpect(jsonPath("$.[*].status").value(hasItem(DEFAULT_STATUS.toString())));
    }

    @Test
    @Transactional
    void getBed() throws Exception {
        // Initialize the database
        insertedBed = bedRepository.saveAndFlush(bed);

        // Get the bed
        restBedMockMvc
            .perform(get(ENTITY_API_URL_ID, bed.getId()))
            .andExpect(status().isOk())
            .andExpect(content().contentType(MediaType.APPLICATION_JSON_VALUE))
            .andExpect(jsonPath("$.id").value(bed.getId().intValue()))
            .andExpect(jsonPath("$.bedNumber").value(DEFAULT_BED_NUMBER))
            .andExpect(jsonPath("$.dailyRateOverride").value(sameNumber(DEFAULT_DAILY_RATE_OVERRIDE)))
            .andExpect(jsonPath("$.status").value(DEFAULT_STATUS.toString()));
    }

    @Test
    @Transactional
    void getNonExistingBed() throws Exception {
        // Get the bed
        restBedMockMvc.perform(get(ENTITY_API_URL_ID, Long.MAX_VALUE)).andExpect(status().isNotFound());
    }

    @Test
    @Transactional
    void putExistingBed() throws Exception {
        // Initialize the database
        insertedBed = bedRepository.saveAndFlush(bed);

        long databaseSizeBeforeUpdate = getRepositoryCount();

        // Update the bed
        Bed updatedBed = bedRepository.findById(bed.getId()).orElseThrow();
        // Disconnect from session so that the updates on updatedBed are not directly saved in db
        em.detach(updatedBed);
        updatedBed.bedNumber(UPDATED_BED_NUMBER).dailyRateOverride(UPDATED_DAILY_RATE_OVERRIDE).status(UPDATED_STATUS);
        BedDTO bedDTO = bedMapper.toDto(updatedBed);

        restBedMockMvc
            .perform(put(ENTITY_API_URL_ID, bedDTO.getId()).contentType(MediaType.APPLICATION_JSON).content(om.writeValueAsBytes(bedDTO)))
            .andExpect(status().isOk());

        // Validate the Bed in the database
        assertSameRepositoryCount(databaseSizeBeforeUpdate);
        assertPersistedBedToMatchAllProperties(updatedBed);
    }

    @Test
    @Transactional
    void putNonExistingBed() throws Exception {
        long databaseSizeBeforeUpdate = getRepositoryCount();
        bed.setId(longCount.incrementAndGet());

        // Create the Bed
        BedDTO bedDTO = bedMapper.toDto(bed);

        // If the entity doesn't have an ID, it will throw BadRequestAlertException
        restBedMockMvc
            .perform(put(ENTITY_API_URL_ID, bedDTO.getId()).contentType(MediaType.APPLICATION_JSON).content(om.writeValueAsBytes(bedDTO)))
            .andExpect(status().isBadRequest());

        // Validate the Bed in the database
        assertSameRepositoryCount(databaseSizeBeforeUpdate);
    }

    @Test
    @Transactional
    void putWithIdMismatchBed() throws Exception {
        long databaseSizeBeforeUpdate = getRepositoryCount();
        bed.setId(longCount.incrementAndGet());

        // Create the Bed
        BedDTO bedDTO = bedMapper.toDto(bed);

        // If url ID doesn't match entity ID, it will throw BadRequestAlertException
        restBedMockMvc
            .perform(
                put(ENTITY_API_URL_ID, longCount.incrementAndGet())
                    .contentType(MediaType.APPLICATION_JSON)
                    .content(om.writeValueAsBytes(bedDTO))
            )
            .andExpect(status().isBadRequest());

        // Validate the Bed in the database
        assertSameRepositoryCount(databaseSizeBeforeUpdate);
    }

    @Test
    @Transactional
    void putWithMissingIdPathParamBed() throws Exception {
        long databaseSizeBeforeUpdate = getRepositoryCount();
        bed.setId(longCount.incrementAndGet());

        // Create the Bed
        BedDTO bedDTO = bedMapper.toDto(bed);

        // If url ID doesn't match entity ID, it will throw BadRequestAlertException
        restBedMockMvc
            .perform(put(ENTITY_API_URL).contentType(MediaType.APPLICATION_JSON).content(om.writeValueAsBytes(bedDTO)))
            .andExpect(status().isMethodNotAllowed());

        // Validate the Bed in the database
        assertSameRepositoryCount(databaseSizeBeforeUpdate);
    }

    @Test
    @Transactional
    void partialUpdateBedWithPatch() throws Exception {
        // Initialize the database
        insertedBed = bedRepository.saveAndFlush(bed);

        long databaseSizeBeforeUpdate = getRepositoryCount();

        // Update the bed using partial update
        Bed partialUpdatedBed = new Bed();
        partialUpdatedBed.setId(bed.getId());

        partialUpdatedBed.dailyRateOverride(UPDATED_DAILY_RATE_OVERRIDE).status(UPDATED_STATUS);

        restBedMockMvc
            .perform(
                patch(ENTITY_API_URL_ID, partialUpdatedBed.getId())
                    .contentType("application/merge-patch+json")
                    .content(om.writeValueAsBytes(partialUpdatedBed))
            )
            .andExpect(status().isOk());

        // Validate the Bed in the database

        assertSameRepositoryCount(databaseSizeBeforeUpdate);
        assertBedUpdatableFieldsEquals(createUpdateProxyForBean(partialUpdatedBed, bed), getPersistedBed(bed));
    }

    @Test
    @Transactional
    void fullUpdateBedWithPatch() throws Exception {
        // Initialize the database
        insertedBed = bedRepository.saveAndFlush(bed);

        long databaseSizeBeforeUpdate = getRepositoryCount();

        // Update the bed using partial update
        Bed partialUpdatedBed = new Bed();
        partialUpdatedBed.setId(bed.getId());

        partialUpdatedBed.bedNumber(UPDATED_BED_NUMBER).dailyRateOverride(UPDATED_DAILY_RATE_OVERRIDE).status(UPDATED_STATUS);

        restBedMockMvc
            .perform(
                patch(ENTITY_API_URL_ID, partialUpdatedBed.getId())
                    .contentType("application/merge-patch+json")
                    .content(om.writeValueAsBytes(partialUpdatedBed))
            )
            .andExpect(status().isOk());

        // Validate the Bed in the database

        assertSameRepositoryCount(databaseSizeBeforeUpdate);
        assertBedUpdatableFieldsEquals(partialUpdatedBed, getPersistedBed(partialUpdatedBed));
    }

    @Test
    @Transactional
    void patchNonExistingBed() throws Exception {
        long databaseSizeBeforeUpdate = getRepositoryCount();
        bed.setId(longCount.incrementAndGet());

        // Create the Bed
        BedDTO bedDTO = bedMapper.toDto(bed);

        // If the entity doesn't have an ID, it will throw BadRequestAlertException
        restBedMockMvc
            .perform(
                patch(ENTITY_API_URL_ID, bedDTO.getId()).contentType("application/merge-patch+json").content(om.writeValueAsBytes(bedDTO))
            )
            .andExpect(status().isBadRequest());

        // Validate the Bed in the database
        assertSameRepositoryCount(databaseSizeBeforeUpdate);
    }

    @Test
    @Transactional
    void patchWithIdMismatchBed() throws Exception {
        long databaseSizeBeforeUpdate = getRepositoryCount();
        bed.setId(longCount.incrementAndGet());

        // Create the Bed
        BedDTO bedDTO = bedMapper.toDto(bed);

        // If url ID doesn't match entity ID, it will throw BadRequestAlertException
        restBedMockMvc
            .perform(
                patch(ENTITY_API_URL_ID, longCount.incrementAndGet())
                    .contentType("application/merge-patch+json")
                    .content(om.writeValueAsBytes(bedDTO))
            )
            .andExpect(status().isBadRequest());

        // Validate the Bed in the database
        assertSameRepositoryCount(databaseSizeBeforeUpdate);
    }

    @Test
    @Transactional
    void patchWithMissingIdPathParamBed() throws Exception {
        long databaseSizeBeforeUpdate = getRepositoryCount();
        bed.setId(longCount.incrementAndGet());

        // Create the Bed
        BedDTO bedDTO = bedMapper.toDto(bed);

        // If url ID doesn't match entity ID, it will throw BadRequestAlertException
        restBedMockMvc
            .perform(patch(ENTITY_API_URL).contentType("application/merge-patch+json").content(om.writeValueAsBytes(bedDTO)))
            .andExpect(status().isMethodNotAllowed());

        // Validate the Bed in the database
        assertSameRepositoryCount(databaseSizeBeforeUpdate);
    }

    @Test
    @Transactional
    void deleteBed() throws Exception {
        // Initialize the database
        insertedBed = bedRepository.saveAndFlush(bed);

        long databaseSizeBeforeDelete = getRepositoryCount();

        // Delete the bed
        restBedMockMvc.perform(delete(ENTITY_API_URL_ID, bed.getId()).accept(MediaType.APPLICATION_JSON)).andExpect(status().isNoContent());

        // Validate the database contains one less item
        assertDecrementedRepositoryCount(databaseSizeBeforeDelete);
    }

    protected long getRepositoryCount() {
        return bedRepository.count();
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

    protected Bed getPersistedBed(Bed bed) {
        return bedRepository.findById(bed.getId()).orElseThrow();
    }

    protected void assertPersistedBedToMatchAllProperties(Bed expectedBed) {
        assertBedAllPropertiesEquals(expectedBed, getPersistedBed(expectedBed));
    }

    protected void assertPersistedBedToMatchUpdatableProperties(Bed expectedBed) {
        assertBedAllUpdatablePropertiesEquals(expectedBed, getPersistedBed(expectedBed));
    }
}
