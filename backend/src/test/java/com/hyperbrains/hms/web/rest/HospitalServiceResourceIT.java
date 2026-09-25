package com.hyperbrains.hms.web.rest;

import static com.hyperbrains.hms.domain.HospitalServiceAsserts.*;
import static com.hyperbrains.hms.web.rest.TestUtil.createUpdateProxyForBean;
import static com.hyperbrains.hms.web.rest.TestUtil.sameNumber;
import static org.assertj.core.api.Assertions.assertThat;
import static org.hamcrest.Matchers.hasItem;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.hyperbrains.hms.IntegrationTest;
import com.hyperbrains.hms.domain.HospitalService;
import com.hyperbrains.hms.repository.HospitalServiceRepository;
import com.hyperbrains.hms.service.dto.HospitalServiceDTO;
import com.hyperbrains.hms.service.mapper.HospitalServiceMapper;
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
 * Integration tests for the {@link HospitalServiceResource} REST controller.
 */
@IntegrationTest
@AutoConfigureMockMvc
@WithMockUser(authorities = AuthoritiesConstants.ADMIN)
class HospitalServiceResourceIT {

    private static final String DEFAULT_NAME = "AAAAAAAAAA";
    private static final String UPDATED_NAME = "BBBBBBBBBB";

    private static final String DEFAULT_SERVICE_TYPE = "AAAAAAAAAA";
    private static final String UPDATED_SERVICE_TYPE = "BBBBBBBBBB";

    private static final BigDecimal DEFAULT_PRICE = new BigDecimal(0);
    private static final BigDecimal UPDATED_PRICE = new BigDecimal(1);

    private static final Boolean DEFAULT_ACTIVE = false;
    private static final Boolean UPDATED_ACTIVE = true;

    private static final String ENTITY_API_URL = "/api/hospital-services";
    private static final String ENTITY_API_URL_ID = ENTITY_API_URL + "/{id}";

    private static final Random random = new Random();
    private static final AtomicLong longCount = new AtomicLong(random.nextInt() + 2L * Integer.MAX_VALUE);

    @Autowired
    private ObjectMapper om;

    @Autowired
    private HospitalServiceRepository hospitalServiceRepository;

    @Autowired
    private HospitalServiceMapper hospitalServiceMapper;

    @Autowired
    private EntityManager em;

    @Autowired
    private MockMvc restHospitalServiceMockMvc;

    private HospitalService hospitalService;

    private HospitalService insertedHospitalService;

    /**
     * Create an entity for this test.
     *
     * This is a static method, as tests for other entities might also need it,
     * if they test an entity which requires the current entity.
     */
    public static HospitalService createEntity() {
        return new HospitalService().name(DEFAULT_NAME).serviceType(DEFAULT_SERVICE_TYPE).price(DEFAULT_PRICE).active(DEFAULT_ACTIVE);
    }

    /**
     * Create an updated entity for this test.
     *
     * This is a static method, as tests for other entities might also need it,
     * if they test an entity which requires the current entity.
     */
    public static HospitalService createUpdatedEntity() {
        return new HospitalService().name(UPDATED_NAME).serviceType(UPDATED_SERVICE_TYPE).price(UPDATED_PRICE).active(UPDATED_ACTIVE);
    }

    @BeforeEach
    void initTest() {
        hospitalService = createEntity();
    }

    @AfterEach
    void cleanup() {
        if (insertedHospitalService != null) {
            hospitalServiceRepository.delete(insertedHospitalService);
            insertedHospitalService = null;
        }
    }

    @Test
    @Transactional
    void createHospitalService() throws Exception {
        long databaseSizeBeforeCreate = getRepositoryCount();
        // Create the HospitalService
        HospitalServiceDTO hospitalServiceDTO = hospitalServiceMapper.toDto(hospitalService);
        var returnedHospitalServiceDTO = om.readValue(
            restHospitalServiceMockMvc
                .perform(post(ENTITY_API_URL).contentType(MediaType.APPLICATION_JSON).content(om.writeValueAsBytes(hospitalServiceDTO)))
                .andExpect(status().isCreated())
                .andReturn()
                .getResponse()
                .getContentAsString(),
            HospitalServiceDTO.class
        );

        // Validate the HospitalService in the database
        assertIncrementedRepositoryCount(databaseSizeBeforeCreate);
        var returnedHospitalService = hospitalServiceMapper.toEntity(returnedHospitalServiceDTO);
        assertHospitalServiceUpdatableFieldsEquals(returnedHospitalService, getPersistedHospitalService(returnedHospitalService));

        insertedHospitalService = returnedHospitalService;
    }

    @Test
    @Transactional
    void createHospitalServiceWithExistingId() throws Exception {
        // Create the HospitalService with an existing ID
        hospitalService.setId(1L);
        HospitalServiceDTO hospitalServiceDTO = hospitalServiceMapper.toDto(hospitalService);

        long databaseSizeBeforeCreate = getRepositoryCount();

        // An entity with an existing ID cannot be created, so this API call must fail
        restHospitalServiceMockMvc
            .perform(post(ENTITY_API_URL).contentType(MediaType.APPLICATION_JSON).content(om.writeValueAsBytes(hospitalServiceDTO)))
            .andExpect(status().isBadRequest());

        // Validate the HospitalService in the database
        assertSameRepositoryCount(databaseSizeBeforeCreate);
    }

    @Test
    @Transactional
    void checkNameIsRequired() throws Exception {
        long databaseSizeBeforeTest = getRepositoryCount();
        // set the field null
        hospitalService.setName(null);

        // Create the HospitalService, which fails.
        HospitalServiceDTO hospitalServiceDTO = hospitalServiceMapper.toDto(hospitalService);

        restHospitalServiceMockMvc
            .perform(post(ENTITY_API_URL).contentType(MediaType.APPLICATION_JSON).content(om.writeValueAsBytes(hospitalServiceDTO)))
            .andExpect(status().isBadRequest());

        assertSameRepositoryCount(databaseSizeBeforeTest);
    }

    @Test
    @Transactional
    void checkPriceIsRequired() throws Exception {
        long databaseSizeBeforeTest = getRepositoryCount();
        // set the field null
        hospitalService.setPrice(null);

        // Create the HospitalService, which fails.
        HospitalServiceDTO hospitalServiceDTO = hospitalServiceMapper.toDto(hospitalService);

        restHospitalServiceMockMvc
            .perform(post(ENTITY_API_URL).contentType(MediaType.APPLICATION_JSON).content(om.writeValueAsBytes(hospitalServiceDTO)))
            .andExpect(status().isBadRequest());

        assertSameRepositoryCount(databaseSizeBeforeTest);
    }

    @Test
    @Transactional
    void checkActiveIsRequired() throws Exception {
        long databaseSizeBeforeTest = getRepositoryCount();
        // set the field null
        hospitalService.setActive(null);

        // Create the HospitalService, which fails.
        HospitalServiceDTO hospitalServiceDTO = hospitalServiceMapper.toDto(hospitalService);

        restHospitalServiceMockMvc
            .perform(post(ENTITY_API_URL).contentType(MediaType.APPLICATION_JSON).content(om.writeValueAsBytes(hospitalServiceDTO)))
            .andExpect(status().isBadRequest());

        assertSameRepositoryCount(databaseSizeBeforeTest);
    }

    @Test
    @Transactional
    void getAllHospitalServices() throws Exception {
        // Initialize the database
        insertedHospitalService = hospitalServiceRepository.saveAndFlush(hospitalService);

        // Get all the hospitalServiceList
        restHospitalServiceMockMvc
            .perform(get(ENTITY_API_URL + "?sort=id,desc"))
            .andExpect(status().isOk())
            .andExpect(content().contentType(MediaType.APPLICATION_JSON_VALUE))
            .andExpect(jsonPath("$.[*].id").value(hasItem(hospitalService.getId().intValue())))
            .andExpect(jsonPath("$.[*].name").value(hasItem(DEFAULT_NAME)))
            .andExpect(jsonPath("$.[*].serviceType").value(hasItem(DEFAULT_SERVICE_TYPE)))
            .andExpect(jsonPath("$.[*].price").value(hasItem(sameNumber(DEFAULT_PRICE))))
            .andExpect(jsonPath("$.[*].active").value(hasItem(DEFAULT_ACTIVE)));
    }

    @Test
    @Transactional
    void getHospitalService() throws Exception {
        // Initialize the database
        insertedHospitalService = hospitalServiceRepository.saveAndFlush(hospitalService);

        // Get the hospitalService
        restHospitalServiceMockMvc
            .perform(get(ENTITY_API_URL_ID, hospitalService.getId()))
            .andExpect(status().isOk())
            .andExpect(content().contentType(MediaType.APPLICATION_JSON_VALUE))
            .andExpect(jsonPath("$.id").value(hospitalService.getId().intValue()))
            .andExpect(jsonPath("$.name").value(DEFAULT_NAME))
            .andExpect(jsonPath("$.serviceType").value(DEFAULT_SERVICE_TYPE))
            .andExpect(jsonPath("$.price").value(sameNumber(DEFAULT_PRICE)))
            .andExpect(jsonPath("$.active").value(DEFAULT_ACTIVE));
    }

    @Test
    @Transactional
    void getNonExistingHospitalService() throws Exception {
        // Get the hospitalService
        restHospitalServiceMockMvc.perform(get(ENTITY_API_URL_ID, Long.MAX_VALUE)).andExpect(status().isNotFound());
    }

    @Test
    @Transactional
    void putExistingHospitalService() throws Exception {
        // Initialize the database
        insertedHospitalService = hospitalServiceRepository.saveAndFlush(hospitalService);

        long databaseSizeBeforeUpdate = getRepositoryCount();

        // Update the hospitalService
        HospitalService updatedHospitalService = hospitalServiceRepository.findById(hospitalService.getId()).orElseThrow();
        // Disconnect from session so that the updates on updatedHospitalService are not directly saved in db
        em.detach(updatedHospitalService);
        updatedHospitalService.name(UPDATED_NAME).serviceType(UPDATED_SERVICE_TYPE).price(UPDATED_PRICE).active(UPDATED_ACTIVE);
        HospitalServiceDTO hospitalServiceDTO = hospitalServiceMapper.toDto(updatedHospitalService);

        restHospitalServiceMockMvc
            .perform(
                put(ENTITY_API_URL_ID, hospitalServiceDTO.getId())
                    .contentType(MediaType.APPLICATION_JSON)
                    .content(om.writeValueAsBytes(hospitalServiceDTO))
            )
            .andExpect(status().isOk());

        // Validate the HospitalService in the database
        assertSameRepositoryCount(databaseSizeBeforeUpdate);
        assertPersistedHospitalServiceToMatchAllProperties(updatedHospitalService);
    }

    @Test
    @Transactional
    void putNonExistingHospitalService() throws Exception {
        long databaseSizeBeforeUpdate = getRepositoryCount();
        hospitalService.setId(longCount.incrementAndGet());

        // Create the HospitalService
        HospitalServiceDTO hospitalServiceDTO = hospitalServiceMapper.toDto(hospitalService);

        // If the entity doesn't have an ID, it will throw BadRequestAlertException
        restHospitalServiceMockMvc
            .perform(
                put(ENTITY_API_URL_ID, hospitalServiceDTO.getId())
                    .contentType(MediaType.APPLICATION_JSON)
                    .content(om.writeValueAsBytes(hospitalServiceDTO))
            )
            .andExpect(status().isBadRequest());

        // Validate the HospitalService in the database
        assertSameRepositoryCount(databaseSizeBeforeUpdate);
    }

    @Test
    @Transactional
    void putWithIdMismatchHospitalService() throws Exception {
        long databaseSizeBeforeUpdate = getRepositoryCount();
        hospitalService.setId(longCount.incrementAndGet());

        // Create the HospitalService
        HospitalServiceDTO hospitalServiceDTO = hospitalServiceMapper.toDto(hospitalService);

        // If url ID doesn't match entity ID, it will throw BadRequestAlertException
        restHospitalServiceMockMvc
            .perform(
                put(ENTITY_API_URL_ID, longCount.incrementAndGet())
                    .contentType(MediaType.APPLICATION_JSON)
                    .content(om.writeValueAsBytes(hospitalServiceDTO))
            )
            .andExpect(status().isBadRequest());

        // Validate the HospitalService in the database
        assertSameRepositoryCount(databaseSizeBeforeUpdate);
    }

    @Test
    @Transactional
    void putWithMissingIdPathParamHospitalService() throws Exception {
        long databaseSizeBeforeUpdate = getRepositoryCount();
        hospitalService.setId(longCount.incrementAndGet());

        // Create the HospitalService
        HospitalServiceDTO hospitalServiceDTO = hospitalServiceMapper.toDto(hospitalService);

        // If url ID doesn't match entity ID, it will throw BadRequestAlertException
        restHospitalServiceMockMvc
            .perform(put(ENTITY_API_URL).contentType(MediaType.APPLICATION_JSON).content(om.writeValueAsBytes(hospitalServiceDTO)))
            .andExpect(status().isMethodNotAllowed());

        // Validate the HospitalService in the database
        assertSameRepositoryCount(databaseSizeBeforeUpdate);
    }

    @Test
    @Transactional
    void partialUpdateHospitalServiceWithPatch() throws Exception {
        // Initialize the database
        insertedHospitalService = hospitalServiceRepository.saveAndFlush(hospitalService);

        long databaseSizeBeforeUpdate = getRepositoryCount();

        // Update the hospitalService using partial update
        HospitalService partialUpdatedHospitalService = new HospitalService();
        partialUpdatedHospitalService.setId(hospitalService.getId());

        partialUpdatedHospitalService.price(UPDATED_PRICE).active(UPDATED_ACTIVE);

        restHospitalServiceMockMvc
            .perform(
                patch(ENTITY_API_URL_ID, partialUpdatedHospitalService.getId())
                    .contentType("application/merge-patch+json")
                    .content(om.writeValueAsBytes(partialUpdatedHospitalService))
            )
            .andExpect(status().isOk());

        // Validate the HospitalService in the database

        assertSameRepositoryCount(databaseSizeBeforeUpdate);
        assertHospitalServiceUpdatableFieldsEquals(
            createUpdateProxyForBean(partialUpdatedHospitalService, hospitalService),
            getPersistedHospitalService(hospitalService)
        );
    }

    @Test
    @Transactional
    void fullUpdateHospitalServiceWithPatch() throws Exception {
        // Initialize the database
        insertedHospitalService = hospitalServiceRepository.saveAndFlush(hospitalService);

        long databaseSizeBeforeUpdate = getRepositoryCount();

        // Update the hospitalService using partial update
        HospitalService partialUpdatedHospitalService = new HospitalService();
        partialUpdatedHospitalService.setId(hospitalService.getId());

        partialUpdatedHospitalService.name(UPDATED_NAME).serviceType(UPDATED_SERVICE_TYPE).price(UPDATED_PRICE).active(UPDATED_ACTIVE);

        restHospitalServiceMockMvc
            .perform(
                patch(ENTITY_API_URL_ID, partialUpdatedHospitalService.getId())
                    .contentType("application/merge-patch+json")
                    .content(om.writeValueAsBytes(partialUpdatedHospitalService))
            )
            .andExpect(status().isOk());

        // Validate the HospitalService in the database

        assertSameRepositoryCount(databaseSizeBeforeUpdate);
        assertHospitalServiceUpdatableFieldsEquals(
            partialUpdatedHospitalService,
            getPersistedHospitalService(partialUpdatedHospitalService)
        );
    }

    @Test
    @Transactional
    void patchNonExistingHospitalService() throws Exception {
        long databaseSizeBeforeUpdate = getRepositoryCount();
        hospitalService.setId(longCount.incrementAndGet());

        // Create the HospitalService
        HospitalServiceDTO hospitalServiceDTO = hospitalServiceMapper.toDto(hospitalService);

        // If the entity doesn't have an ID, it will throw BadRequestAlertException
        restHospitalServiceMockMvc
            .perform(
                patch(ENTITY_API_URL_ID, hospitalServiceDTO.getId())
                    .contentType("application/merge-patch+json")
                    .content(om.writeValueAsBytes(hospitalServiceDTO))
            )
            .andExpect(status().isBadRequest());

        // Validate the HospitalService in the database
        assertSameRepositoryCount(databaseSizeBeforeUpdate);
    }

    @Test
    @Transactional
    void patchWithIdMismatchHospitalService() throws Exception {
        long databaseSizeBeforeUpdate = getRepositoryCount();
        hospitalService.setId(longCount.incrementAndGet());

        // Create the HospitalService
        HospitalServiceDTO hospitalServiceDTO = hospitalServiceMapper.toDto(hospitalService);

        // If url ID doesn't match entity ID, it will throw BadRequestAlertException
        restHospitalServiceMockMvc
            .perform(
                patch(ENTITY_API_URL_ID, longCount.incrementAndGet())
                    .contentType("application/merge-patch+json")
                    .content(om.writeValueAsBytes(hospitalServiceDTO))
            )
            .andExpect(status().isBadRequest());

        // Validate the HospitalService in the database
        assertSameRepositoryCount(databaseSizeBeforeUpdate);
    }

    @Test
    @Transactional
    void patchWithMissingIdPathParamHospitalService() throws Exception {
        long databaseSizeBeforeUpdate = getRepositoryCount();
        hospitalService.setId(longCount.incrementAndGet());

        // Create the HospitalService
        HospitalServiceDTO hospitalServiceDTO = hospitalServiceMapper.toDto(hospitalService);

        // If url ID doesn't match entity ID, it will throw BadRequestAlertException
        restHospitalServiceMockMvc
            .perform(patch(ENTITY_API_URL).contentType("application/merge-patch+json").content(om.writeValueAsBytes(hospitalServiceDTO)))
            .andExpect(status().isMethodNotAllowed());

        // Validate the HospitalService in the database
        assertSameRepositoryCount(databaseSizeBeforeUpdate);
    }

    @Test
    @Transactional
    void deleteHospitalService() throws Exception {
        // Initialize the database
        insertedHospitalService = hospitalServiceRepository.saveAndFlush(hospitalService);

        long databaseSizeBeforeDelete = getRepositoryCount();

        // Delete the hospitalService
        restHospitalServiceMockMvc
            .perform(delete(ENTITY_API_URL_ID, hospitalService.getId()).accept(MediaType.APPLICATION_JSON))
            .andExpect(status().isNoContent());

        // Validate the database contains one less item
        assertDecrementedRepositoryCount(databaseSizeBeforeDelete);
    }

    protected long getRepositoryCount() {
        return hospitalServiceRepository.count();
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

    protected HospitalService getPersistedHospitalService(HospitalService hospitalService) {
        return hospitalServiceRepository.findById(hospitalService.getId()).orElseThrow();
    }

    protected void assertPersistedHospitalServiceToMatchAllProperties(HospitalService expectedHospitalService) {
        assertHospitalServiceAllPropertiesEquals(expectedHospitalService, getPersistedHospitalService(expectedHospitalService));
    }

    protected void assertPersistedHospitalServiceToMatchUpdatableProperties(HospitalService expectedHospitalService) {
        assertHospitalServiceAllUpdatablePropertiesEquals(expectedHospitalService, getPersistedHospitalService(expectedHospitalService));
    }
}
