package com.hyperbrains.hms.web.rest;

import static com.hyperbrains.hms.domain.WardAsserts.*;
import static com.hyperbrains.hms.web.rest.TestUtil.createUpdateProxyForBean;
import static org.assertj.core.api.Assertions.assertThat;
import static org.hamcrest.Matchers.hasItem;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.hyperbrains.hms.IntegrationTest;
import com.hyperbrains.hms.domain.Department;
import com.hyperbrains.hms.domain.Ward;
import com.hyperbrains.hms.repository.WardRepository;
import com.hyperbrains.hms.service.dto.WardDTO;
import com.hyperbrains.hms.service.mapper.WardMapper;
import jakarta.persistence.EntityManager;
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
 * Integration tests for the {@link WardResource} REST controller.
 */
@IntegrationTest
@AutoConfigureMockMvc
@WithMockUser
class WardResourceIT {

    private static final String DEFAULT_NAME = "AAAAAAAAAA";
    private static final String UPDATED_NAME = "BBBBBBBBBB";

    private static final String DEFAULT_LOCATION = "AAAAAAAAAA";
    private static final String UPDATED_LOCATION = "BBBBBBBBBB";

    private static final Boolean DEFAULT_ACTIVE = false;
    private static final Boolean UPDATED_ACTIVE = true;

    private static final String ENTITY_API_URL = "/api/wards";
    private static final String ENTITY_API_URL_ID = ENTITY_API_URL + "/{id}";

    private static final Random random = new Random();
    private static final AtomicLong longCount = new AtomicLong(random.nextInt() + 2L * Integer.MAX_VALUE);

    @Autowired
    private ObjectMapper om;

    @Autowired
    private WardRepository wardRepository;

    @Autowired
    private WardMapper wardMapper;

    @Autowired
    private EntityManager em;

    @Autowired
    private MockMvc restWardMockMvc;

    private Ward ward;

    private Ward insertedWard;

    /**
     * Create an entity for this test.
     *
     * This is a static method, as tests for other entities might also need it,
     * if they test an entity which requires the current entity.
     */
    public static Ward createEntity(EntityManager em) {
        Ward ward = new Ward().name(DEFAULT_NAME).location(DEFAULT_LOCATION).active(DEFAULT_ACTIVE);
        // Add required entity
        Department department;
        if (TestUtil.findAll(em, Department.class).isEmpty()) {
            department = DepartmentResourceIT.createEntity();
            em.persist(department);
            em.flush();
        } else {
            department = TestUtil.findAll(em, Department.class).get(0);
        }
        ward.setDepartment(department);
        return ward;
    }

    /**
     * Create an updated entity for this test.
     *
     * This is a static method, as tests for other entities might also need it,
     * if they test an entity which requires the current entity.
     */
    public static Ward createUpdatedEntity(EntityManager em) {
        Ward updatedWard = new Ward().name(UPDATED_NAME).location(UPDATED_LOCATION).active(UPDATED_ACTIVE);
        // Add required entity
        Department department;
        if (TestUtil.findAll(em, Department.class).isEmpty()) {
            department = DepartmentResourceIT.createUpdatedEntity();
            em.persist(department);
            em.flush();
        } else {
            department = TestUtil.findAll(em, Department.class).get(0);
        }
        updatedWard.setDepartment(department);
        return updatedWard;
    }

    @BeforeEach
    void initTest() {
        ward = createEntity(em);
    }

    @AfterEach
    void cleanup() {
        if (insertedWard != null) {
            wardRepository.delete(insertedWard);
            insertedWard = null;
        }
    }

    @Test
    @Transactional
    void createWard() throws Exception {
        long databaseSizeBeforeCreate = getRepositoryCount();
        // Create the Ward
        WardDTO wardDTO = wardMapper.toDto(ward);
        var returnedWardDTO = om.readValue(
            restWardMockMvc
                .perform(post(ENTITY_API_URL).contentType(MediaType.APPLICATION_JSON).content(om.writeValueAsBytes(wardDTO)))
                .andExpect(status().isCreated())
                .andReturn()
                .getResponse()
                .getContentAsString(),
            WardDTO.class
        );

        // Validate the Ward in the database
        assertIncrementedRepositoryCount(databaseSizeBeforeCreate);
        var returnedWard = wardMapper.toEntity(returnedWardDTO);
        assertWardUpdatableFieldsEquals(returnedWard, getPersistedWard(returnedWard));

        insertedWard = returnedWard;
    }

    @Test
    @Transactional
    void createWardWithExistingId() throws Exception {
        // Create the Ward with an existing ID
        ward.setId(1L);
        WardDTO wardDTO = wardMapper.toDto(ward);

        long databaseSizeBeforeCreate = getRepositoryCount();

        // An entity with an existing ID cannot be created, so this API call must fail
        restWardMockMvc
            .perform(post(ENTITY_API_URL).contentType(MediaType.APPLICATION_JSON).content(om.writeValueAsBytes(wardDTO)))
            .andExpect(status().isBadRequest());

        // Validate the Ward in the database
        assertSameRepositoryCount(databaseSizeBeforeCreate);
    }

    @Test
    @Transactional
    void checkNameIsRequired() throws Exception {
        long databaseSizeBeforeTest = getRepositoryCount();
        // set the field null
        ward.setName(null);

        // Create the Ward, which fails.
        WardDTO wardDTO = wardMapper.toDto(ward);

        restWardMockMvc
            .perform(post(ENTITY_API_URL).contentType(MediaType.APPLICATION_JSON).content(om.writeValueAsBytes(wardDTO)))
            .andExpect(status().isBadRequest());

        assertSameRepositoryCount(databaseSizeBeforeTest);
    }

    @Test
    @Transactional
    void checkActiveIsRequired() throws Exception {
        long databaseSizeBeforeTest = getRepositoryCount();
        // set the field null
        ward.setActive(null);

        // Create the Ward, which fails.
        WardDTO wardDTO = wardMapper.toDto(ward);

        restWardMockMvc
            .perform(post(ENTITY_API_URL).contentType(MediaType.APPLICATION_JSON).content(om.writeValueAsBytes(wardDTO)))
            .andExpect(status().isBadRequest());

        assertSameRepositoryCount(databaseSizeBeforeTest);
    }

    @Test
    @Transactional
    void getAllWards() throws Exception {
        // Initialize the database
        insertedWard = wardRepository.saveAndFlush(ward);

        // Get all the wardList
        restWardMockMvc
            .perform(get(ENTITY_API_URL + "?sort=id,desc"))
            .andExpect(status().isOk())
            .andExpect(content().contentType(MediaType.APPLICATION_JSON_VALUE))
            .andExpect(jsonPath("$.[*].id").value(hasItem(ward.getId().intValue())))
            .andExpect(jsonPath("$.[*].name").value(hasItem(DEFAULT_NAME)))
            .andExpect(jsonPath("$.[*].location").value(hasItem(DEFAULT_LOCATION)))
            .andExpect(jsonPath("$.[*].active").value(hasItem(DEFAULT_ACTIVE)));
    }

    @Test
    @Transactional
    void getWard() throws Exception {
        // Initialize the database
        insertedWard = wardRepository.saveAndFlush(ward);

        // Get the ward
        restWardMockMvc
            .perform(get(ENTITY_API_URL_ID, ward.getId()))
            .andExpect(status().isOk())
            .andExpect(content().contentType(MediaType.APPLICATION_JSON_VALUE))
            .andExpect(jsonPath("$.id").value(ward.getId().intValue()))
            .andExpect(jsonPath("$.name").value(DEFAULT_NAME))
            .andExpect(jsonPath("$.location").value(DEFAULT_LOCATION))
            .andExpect(jsonPath("$.active").value(DEFAULT_ACTIVE));
    }

    @Test
    @Transactional
    void getNonExistingWard() throws Exception {
        // Get the ward
        restWardMockMvc.perform(get(ENTITY_API_URL_ID, Long.MAX_VALUE)).andExpect(status().isNotFound());
    }

    @Test
    @Transactional
    void putExistingWard() throws Exception {
        // Initialize the database
        insertedWard = wardRepository.saveAndFlush(ward);

        long databaseSizeBeforeUpdate = getRepositoryCount();

        // Update the ward
        Ward updatedWard = wardRepository.findById(ward.getId()).orElseThrow();
        // Disconnect from session so that the updates on updatedWard are not directly saved in db
        em.detach(updatedWard);
        updatedWard.name(UPDATED_NAME).location(UPDATED_LOCATION).active(UPDATED_ACTIVE);
        WardDTO wardDTO = wardMapper.toDto(updatedWard);

        restWardMockMvc
            .perform(put(ENTITY_API_URL_ID, wardDTO.getId()).contentType(MediaType.APPLICATION_JSON).content(om.writeValueAsBytes(wardDTO)))
            .andExpect(status().isOk());

        // Validate the Ward in the database
        assertSameRepositoryCount(databaseSizeBeforeUpdate);
        assertPersistedWardToMatchAllProperties(updatedWard);
    }

    @Test
    @Transactional
    void putNonExistingWard() throws Exception {
        long databaseSizeBeforeUpdate = getRepositoryCount();
        ward.setId(longCount.incrementAndGet());

        // Create the Ward
        WardDTO wardDTO = wardMapper.toDto(ward);

        // If the entity doesn't have an ID, it will throw BadRequestAlertException
        restWardMockMvc
            .perform(put(ENTITY_API_URL_ID, wardDTO.getId()).contentType(MediaType.APPLICATION_JSON).content(om.writeValueAsBytes(wardDTO)))
            .andExpect(status().isBadRequest());

        // Validate the Ward in the database
        assertSameRepositoryCount(databaseSizeBeforeUpdate);
    }

    @Test
    @Transactional
    void putWithIdMismatchWard() throws Exception {
        long databaseSizeBeforeUpdate = getRepositoryCount();
        ward.setId(longCount.incrementAndGet());

        // Create the Ward
        WardDTO wardDTO = wardMapper.toDto(ward);

        // If url ID doesn't match entity ID, it will throw BadRequestAlertException
        restWardMockMvc
            .perform(
                put(ENTITY_API_URL_ID, longCount.incrementAndGet())
                    .contentType(MediaType.APPLICATION_JSON)
                    .content(om.writeValueAsBytes(wardDTO))
            )
            .andExpect(status().isBadRequest());

        // Validate the Ward in the database
        assertSameRepositoryCount(databaseSizeBeforeUpdate);
    }

    @Test
    @Transactional
    void putWithMissingIdPathParamWard() throws Exception {
        long databaseSizeBeforeUpdate = getRepositoryCount();
        ward.setId(longCount.incrementAndGet());

        // Create the Ward
        WardDTO wardDTO = wardMapper.toDto(ward);

        // If url ID doesn't match entity ID, it will throw BadRequestAlertException
        restWardMockMvc
            .perform(put(ENTITY_API_URL).contentType(MediaType.APPLICATION_JSON).content(om.writeValueAsBytes(wardDTO)))
            .andExpect(status().isMethodNotAllowed());

        // Validate the Ward in the database
        assertSameRepositoryCount(databaseSizeBeforeUpdate);
    }

    @Test
    @Transactional
    void partialUpdateWardWithPatch() throws Exception {
        // Initialize the database
        insertedWard = wardRepository.saveAndFlush(ward);

        long databaseSizeBeforeUpdate = getRepositoryCount();

        // Update the ward using partial update
        Ward partialUpdatedWard = new Ward();
        partialUpdatedWard.setId(ward.getId());

        partialUpdatedWard.name(UPDATED_NAME).active(UPDATED_ACTIVE);

        restWardMockMvc
            .perform(
                patch(ENTITY_API_URL_ID, partialUpdatedWard.getId())
                    .contentType("application/merge-patch+json")
                    .content(om.writeValueAsBytes(partialUpdatedWard))
            )
            .andExpect(status().isOk());

        // Validate the Ward in the database

        assertSameRepositoryCount(databaseSizeBeforeUpdate);
        assertWardUpdatableFieldsEquals(createUpdateProxyForBean(partialUpdatedWard, ward), getPersistedWard(ward));
    }

    @Test
    @Transactional
    void fullUpdateWardWithPatch() throws Exception {
        // Initialize the database
        insertedWard = wardRepository.saveAndFlush(ward);

        long databaseSizeBeforeUpdate = getRepositoryCount();

        // Update the ward using partial update
        Ward partialUpdatedWard = new Ward();
        partialUpdatedWard.setId(ward.getId());

        partialUpdatedWard.name(UPDATED_NAME).location(UPDATED_LOCATION).active(UPDATED_ACTIVE);

        restWardMockMvc
            .perform(
                patch(ENTITY_API_URL_ID, partialUpdatedWard.getId())
                    .contentType("application/merge-patch+json")
                    .content(om.writeValueAsBytes(partialUpdatedWard))
            )
            .andExpect(status().isOk());

        // Validate the Ward in the database

        assertSameRepositoryCount(databaseSizeBeforeUpdate);
        assertWardUpdatableFieldsEquals(partialUpdatedWard, getPersistedWard(partialUpdatedWard));
    }

    @Test
    @Transactional
    void patchNonExistingWard() throws Exception {
        long databaseSizeBeforeUpdate = getRepositoryCount();
        ward.setId(longCount.incrementAndGet());

        // Create the Ward
        WardDTO wardDTO = wardMapper.toDto(ward);

        // If the entity doesn't have an ID, it will throw BadRequestAlertException
        restWardMockMvc
            .perform(
                patch(ENTITY_API_URL_ID, wardDTO.getId()).contentType("application/merge-patch+json").content(om.writeValueAsBytes(wardDTO))
            )
            .andExpect(status().isBadRequest());

        // Validate the Ward in the database
        assertSameRepositoryCount(databaseSizeBeforeUpdate);
    }

    @Test
    @Transactional
    void patchWithIdMismatchWard() throws Exception {
        long databaseSizeBeforeUpdate = getRepositoryCount();
        ward.setId(longCount.incrementAndGet());

        // Create the Ward
        WardDTO wardDTO = wardMapper.toDto(ward);

        // If url ID doesn't match entity ID, it will throw BadRequestAlertException
        restWardMockMvc
            .perform(
                patch(ENTITY_API_URL_ID, longCount.incrementAndGet())
                    .contentType("application/merge-patch+json")
                    .content(om.writeValueAsBytes(wardDTO))
            )
            .andExpect(status().isBadRequest());

        // Validate the Ward in the database
        assertSameRepositoryCount(databaseSizeBeforeUpdate);
    }

    @Test
    @Transactional
    void patchWithMissingIdPathParamWard() throws Exception {
        long databaseSizeBeforeUpdate = getRepositoryCount();
        ward.setId(longCount.incrementAndGet());

        // Create the Ward
        WardDTO wardDTO = wardMapper.toDto(ward);

        // If url ID doesn't match entity ID, it will throw BadRequestAlertException
        restWardMockMvc
            .perform(patch(ENTITY_API_URL).contentType("application/merge-patch+json").content(om.writeValueAsBytes(wardDTO)))
            .andExpect(status().isMethodNotAllowed());

        // Validate the Ward in the database
        assertSameRepositoryCount(databaseSizeBeforeUpdate);
    }

    @Test
    @Transactional
    void deleteWard() throws Exception {
        // Initialize the database
        insertedWard = wardRepository.saveAndFlush(ward);

        long databaseSizeBeforeDelete = getRepositoryCount();

        // Delete the ward
        restWardMockMvc
            .perform(delete(ENTITY_API_URL_ID, ward.getId()).accept(MediaType.APPLICATION_JSON))
            .andExpect(status().isNoContent());

        // Validate the database contains one less item
        assertDecrementedRepositoryCount(databaseSizeBeforeDelete);
    }

    protected long getRepositoryCount() {
        return wardRepository.count();
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

    protected Ward getPersistedWard(Ward ward) {
        return wardRepository.findById(ward.getId()).orElseThrow();
    }

    protected void assertPersistedWardToMatchAllProperties(Ward expectedWard) {
        assertWardAllPropertiesEquals(expectedWard, getPersistedWard(expectedWard));
    }

    protected void assertPersistedWardToMatchUpdatableProperties(Ward expectedWard) {
        assertWardAllUpdatablePropertiesEquals(expectedWard, getPersistedWard(expectedWard));
    }
}
