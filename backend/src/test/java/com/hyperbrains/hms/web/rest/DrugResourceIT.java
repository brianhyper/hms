package com.hyperbrains.hms.web.rest;

import static com.hyperbrains.hms.domain.DrugAsserts.*;
import static com.hyperbrains.hms.web.rest.TestUtil.createUpdateProxyForBean;
import static com.hyperbrains.hms.web.rest.TestUtil.sameNumber;
import static org.assertj.core.api.Assertions.assertThat;
import static org.hamcrest.Matchers.hasItem;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.hyperbrains.hms.IntegrationTest;
import com.hyperbrains.hms.domain.Drug;
import com.hyperbrains.hms.domain.enumeration.DrugClassification;
import com.hyperbrains.hms.repository.DrugRepository;
import com.hyperbrains.hms.service.dto.DrugDTO;
import com.hyperbrains.hms.service.mapper.DrugMapper;
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
 * Integration tests for the {@link DrugResource} REST controller.
 */
@IntegrationTest
@AutoConfigureMockMvc
@WithMockUser(authorities = AuthoritiesConstants.SUPER_ADMIN)
class DrugResourceIT {

    private static final String DEFAULT_NAME = "AAAAAAAAAA";
    private static final String UPDATED_NAME = "BBBBBBBBBB";

    private static final String DEFAULT_UNIT = "AAAAAAAAAA";
    private static final String UPDATED_UNIT = "BBBBBBBBBB";

    private static final Integer DEFAULT_CURRENT_STOCK = 0;
    private static final Integer UPDATED_CURRENT_STOCK = 1;

    private static final Integer DEFAULT_RESERVED_STOCK = 0;
    private static final Integer UPDATED_RESERVED_STOCK = 1;

    private static final Integer DEFAULT_LOW_STOCK_THRESHOLD = 0;
    private static final Integer UPDATED_LOW_STOCK_THRESHOLD = 1;

    private static final BigDecimal DEFAULT_PRICE = new BigDecimal(0);
    private static final BigDecimal UPDATED_PRICE = new BigDecimal(1);

    private static final DrugClassification DEFAULT_CLASSIFICATION = DrugClassification.OTC;
    private static final DrugClassification UPDATED_CLASSIFICATION = DrugClassification.POM;

    private static final Boolean DEFAULT_ACTIVE = false;
    private static final Boolean UPDATED_ACTIVE = true;

    private static final String ENTITY_API_URL = "/api/drugs";
    private static final String ENTITY_API_URL_ID = ENTITY_API_URL + "/{id}";

    private static final Random random = new Random();
    private static final AtomicLong longCount = new AtomicLong(random.nextInt() + 2L * Integer.MAX_VALUE);

    @Autowired
    private ObjectMapper om;

    @Autowired
    private DrugRepository drugRepository;

    @Autowired
    private DrugMapper drugMapper;

    @Autowired
    private EntityManager em;

    @Autowired
    private MockMvc restDrugMockMvc;

    private Drug drug;

    private Drug insertedDrug;

    /**
     * Create an entity for this test.
     *
     * This is a static method, as tests for other entities might also need it,
     * if they test an entity which requires the current entity.
     */
    public static Drug createEntity() {
        return new Drug()
            .name(DEFAULT_NAME)
            .unit(DEFAULT_UNIT)
            .currentStock(DEFAULT_CURRENT_STOCK)
            .reservedStock(DEFAULT_RESERVED_STOCK)
            .lowStockThreshold(DEFAULT_LOW_STOCK_THRESHOLD)
            .price(DEFAULT_PRICE)
            .classification(DEFAULT_CLASSIFICATION)
            .active(DEFAULT_ACTIVE);
    }

    /**
     * Create an updated entity for this test.
     *
     * This is a static method, as tests for other entities might also need it,
     * if they test an entity which requires the current entity.
     */
    public static Drug createUpdatedEntity() {
        return new Drug()
            .name(UPDATED_NAME)
            .unit(UPDATED_UNIT)
            .currentStock(UPDATED_CURRENT_STOCK)
            .reservedStock(UPDATED_RESERVED_STOCK)
            .lowStockThreshold(UPDATED_LOW_STOCK_THRESHOLD)
            .price(UPDATED_PRICE)
            .classification(UPDATED_CLASSIFICATION)
            .active(UPDATED_ACTIVE);
    }

    @BeforeEach
    void initTest() {
        drug = createEntity();
    }

    @AfterEach
    void cleanup() {
        if (insertedDrug != null) {
            // deleteById re-reads the row first so the optimistic-locking check uses the current
            // version. The cached entity is stale whenever a test modified it (Drug carries @Version).
            drugRepository.deleteById(insertedDrug.getId());
            insertedDrug = null;
        }
    }

    @Test
    @Transactional
    void createDrug() throws Exception {
        long databaseSizeBeforeCreate = getRepositoryCount();
        // Create the Drug
        DrugDTO drugDTO = drugMapper.toDto(drug);
        var returnedDrugDTO = om.readValue(
            restDrugMockMvc
                .perform(post(ENTITY_API_URL).contentType(MediaType.APPLICATION_JSON).content(om.writeValueAsBytes(drugDTO)))
                .andExpect(status().isCreated())
                .andReturn()
                .getResponse()
                .getContentAsString(),
            DrugDTO.class
        );

        // Validate the Drug in the database
        assertIncrementedRepositoryCount(databaseSizeBeforeCreate);
        var returnedDrug = drugMapper.toEntity(returnedDrugDTO);
        assertDrugUpdatableFieldsEquals(returnedDrug, getPersistedDrug(returnedDrug));

        insertedDrug = returnedDrug;
    }

    @Test
    @Transactional
    void createDrugWithExistingId() throws Exception {
        // Create the Drug with an existing ID
        drug.setId(1L);
        DrugDTO drugDTO = drugMapper.toDto(drug);

        long databaseSizeBeforeCreate = getRepositoryCount();

        // An entity with an existing ID cannot be created, so this API call must fail
        restDrugMockMvc
            .perform(post(ENTITY_API_URL).contentType(MediaType.APPLICATION_JSON).content(om.writeValueAsBytes(drugDTO)))
            .andExpect(status().isBadRequest());

        // Validate the Drug in the database
        assertSameRepositoryCount(databaseSizeBeforeCreate);
    }

    @Test
    @Transactional
    void checkNameIsRequired() throws Exception {
        long databaseSizeBeforeTest = getRepositoryCount();
        // set the field null
        drug.setName(null);

        // Create the Drug, which fails.
        DrugDTO drugDTO = drugMapper.toDto(drug);

        restDrugMockMvc
            .perform(post(ENTITY_API_URL).contentType(MediaType.APPLICATION_JSON).content(om.writeValueAsBytes(drugDTO)))
            .andExpect(status().isBadRequest());

        assertSameRepositoryCount(databaseSizeBeforeTest);
    }

    @Test
    @Transactional
    void checkUnitIsRequired() throws Exception {
        long databaseSizeBeforeTest = getRepositoryCount();
        // set the field null
        drug.setUnit(null);

        // Create the Drug, which fails.
        DrugDTO drugDTO = drugMapper.toDto(drug);

        restDrugMockMvc
            .perform(post(ENTITY_API_URL).contentType(MediaType.APPLICATION_JSON).content(om.writeValueAsBytes(drugDTO)))
            .andExpect(status().isBadRequest());

        assertSameRepositoryCount(databaseSizeBeforeTest);
    }

    @Test
    @Transactional
    void checkCurrentStockIsRequired() throws Exception {
        long databaseSizeBeforeTest = getRepositoryCount();
        // set the field null
        drug.setCurrentStock(null);

        // Create the Drug, which fails.
        DrugDTO drugDTO = drugMapper.toDto(drug);

        restDrugMockMvc
            .perform(post(ENTITY_API_URL).contentType(MediaType.APPLICATION_JSON).content(om.writeValueAsBytes(drugDTO)))
            .andExpect(status().isBadRequest());

        assertSameRepositoryCount(databaseSizeBeforeTest);
    }

    @Test
    @Transactional
    void checkReservedStockIsRequired() throws Exception {
        long databaseSizeBeforeTest = getRepositoryCount();
        // set the field null
        drug.setReservedStock(null);

        // Create the Drug, which fails.
        DrugDTO drugDTO = drugMapper.toDto(drug);

        restDrugMockMvc
            .perform(post(ENTITY_API_URL).contentType(MediaType.APPLICATION_JSON).content(om.writeValueAsBytes(drugDTO)))
            .andExpect(status().isBadRequest());

        assertSameRepositoryCount(databaseSizeBeforeTest);
    }

    @Test
    @Transactional
    void checkLowStockThresholdIsRequired() throws Exception {
        long databaseSizeBeforeTest = getRepositoryCount();
        // set the field null
        drug.setLowStockThreshold(null);

        // Create the Drug, which fails.
        DrugDTO drugDTO = drugMapper.toDto(drug);

        restDrugMockMvc
            .perform(post(ENTITY_API_URL).contentType(MediaType.APPLICATION_JSON).content(om.writeValueAsBytes(drugDTO)))
            .andExpect(status().isBadRequest());

        assertSameRepositoryCount(databaseSizeBeforeTest);
    }

    @Test
    @Transactional
    void checkPriceIsRequired() throws Exception {
        long databaseSizeBeforeTest = getRepositoryCount();
        // set the field null
        drug.setPrice(null);

        // Create the Drug, which fails.
        DrugDTO drugDTO = drugMapper.toDto(drug);

        restDrugMockMvc
            .perform(post(ENTITY_API_URL).contentType(MediaType.APPLICATION_JSON).content(om.writeValueAsBytes(drugDTO)))
            .andExpect(status().isBadRequest());

        assertSameRepositoryCount(databaseSizeBeforeTest);
    }

    @Test
    @Transactional
    void checkActiveIsRequired() throws Exception {
        long databaseSizeBeforeTest = getRepositoryCount();
        // set the field null
        drug.setActive(null);

        // Create the Drug, which fails.
        DrugDTO drugDTO = drugMapper.toDto(drug);

        restDrugMockMvc
            .perform(post(ENTITY_API_URL).contentType(MediaType.APPLICATION_JSON).content(om.writeValueAsBytes(drugDTO)))
            .andExpect(status().isBadRequest());

        assertSameRepositoryCount(databaseSizeBeforeTest);
    }

    @Test
    @Transactional
    void getAllDrugs() throws Exception {
        // Initialize the database
        insertedDrug = drugRepository.saveAndFlush(drug);

        // Get all the drugList
        restDrugMockMvc
            .perform(get(ENTITY_API_URL + "?sort=id,desc"))
            .andExpect(status().isOk())
            .andExpect(content().contentType(MediaType.APPLICATION_JSON_VALUE))
            .andExpect(jsonPath("$.[*].id").value(hasItem(drug.getId().intValue())))
            .andExpect(jsonPath("$.[*].name").value(hasItem(DEFAULT_NAME)))
            .andExpect(jsonPath("$.[*].unit").value(hasItem(DEFAULT_UNIT)))
            .andExpect(jsonPath("$.[*].currentStock").value(hasItem(DEFAULT_CURRENT_STOCK)))
            .andExpect(jsonPath("$.[*].reservedStock").value(hasItem(DEFAULT_RESERVED_STOCK)))
            .andExpect(jsonPath("$.[*].lowStockThreshold").value(hasItem(DEFAULT_LOW_STOCK_THRESHOLD)))
            .andExpect(jsonPath("$.[*].price").value(hasItem(sameNumber(DEFAULT_PRICE))))
            .andExpect(jsonPath("$.[*].classification").value(hasItem(DEFAULT_CLASSIFICATION.toString())))
            .andExpect(jsonPath("$.[*].active").value(hasItem(DEFAULT_ACTIVE)));
    }

    @Test
    @Transactional
    void getDrug() throws Exception {
        // Initialize the database
        insertedDrug = drugRepository.saveAndFlush(drug);

        // Get the drug
        restDrugMockMvc
            .perform(get(ENTITY_API_URL_ID, drug.getId()))
            .andExpect(status().isOk())
            .andExpect(content().contentType(MediaType.APPLICATION_JSON_VALUE))
            .andExpect(jsonPath("$.id").value(drug.getId().intValue()))
            .andExpect(jsonPath("$.name").value(DEFAULT_NAME))
            .andExpect(jsonPath("$.unit").value(DEFAULT_UNIT))
            .andExpect(jsonPath("$.currentStock").value(DEFAULT_CURRENT_STOCK))
            .andExpect(jsonPath("$.reservedStock").value(DEFAULT_RESERVED_STOCK))
            .andExpect(jsonPath("$.lowStockThreshold").value(DEFAULT_LOW_STOCK_THRESHOLD))
            .andExpect(jsonPath("$.price").value(sameNumber(DEFAULT_PRICE)))
            .andExpect(jsonPath("$.classification").value(DEFAULT_CLASSIFICATION.toString()))
            .andExpect(jsonPath("$.active").value(DEFAULT_ACTIVE));
    }

    @Test
    @Transactional
    void getNonExistingDrug() throws Exception {
        // Get the drug
        restDrugMockMvc.perform(get(ENTITY_API_URL_ID, Long.MAX_VALUE)).andExpect(status().isNotFound());
    }

    @Test
    @Transactional
    void putExistingDrug() throws Exception {
        // Initialize the database
        insertedDrug = drugRepository.saveAndFlush(drug);

        long databaseSizeBeforeUpdate = getRepositoryCount();

        // Update the drug
        Drug updatedDrug = drugRepository.findById(drug.getId()).orElseThrow();
        // Disconnect from session so that the updates on updatedDrug are not directly saved in db
        em.detach(updatedDrug);
        updatedDrug
            .name(UPDATED_NAME)
            .unit(UPDATED_UNIT)
            .currentStock(UPDATED_CURRENT_STOCK)
            .reservedStock(UPDATED_RESERVED_STOCK)
            .lowStockThreshold(UPDATED_LOW_STOCK_THRESHOLD)
            .price(UPDATED_PRICE)
            .classification(UPDATED_CLASSIFICATION)
            .active(UPDATED_ACTIVE);
        DrugDTO drugDTO = drugMapper.toDto(updatedDrug);

        restDrugMockMvc
            .perform(put(ENTITY_API_URL_ID, drugDTO.getId()).contentType(MediaType.APPLICATION_JSON).content(om.writeValueAsBytes(drugDTO)))
            .andExpect(status().isOk());

        // Validate the Drug in the database
        assertSameRepositoryCount(databaseSizeBeforeUpdate);
        assertPersistedDrugToMatchAllProperties(updatedDrug);
    }

    @Test
    @Transactional
    void putNonExistingDrug() throws Exception {
        long databaseSizeBeforeUpdate = getRepositoryCount();
        drug.setId(longCount.incrementAndGet());

        // Create the Drug
        DrugDTO drugDTO = drugMapper.toDto(drug);

        // If the entity doesn't have an ID, it will throw BadRequestAlertException
        restDrugMockMvc
            .perform(put(ENTITY_API_URL_ID, drugDTO.getId()).contentType(MediaType.APPLICATION_JSON).content(om.writeValueAsBytes(drugDTO)))
            .andExpect(status().isBadRequest());

        // Validate the Drug in the database
        assertSameRepositoryCount(databaseSizeBeforeUpdate);
    }

    @Test
    @Transactional
    void putWithIdMismatchDrug() throws Exception {
        long databaseSizeBeforeUpdate = getRepositoryCount();
        drug.setId(longCount.incrementAndGet());

        // Create the Drug
        DrugDTO drugDTO = drugMapper.toDto(drug);

        // If url ID doesn't match entity ID, it will throw BadRequestAlertException
        restDrugMockMvc
            .perform(
                put(ENTITY_API_URL_ID, longCount.incrementAndGet())
                    .contentType(MediaType.APPLICATION_JSON)
                    .content(om.writeValueAsBytes(drugDTO))
            )
            .andExpect(status().isBadRequest());

        // Validate the Drug in the database
        assertSameRepositoryCount(databaseSizeBeforeUpdate);
    }

    @Test
    @Transactional
    void putWithMissingIdPathParamDrug() throws Exception {
        long databaseSizeBeforeUpdate = getRepositoryCount();
        drug.setId(longCount.incrementAndGet());

        // Create the Drug
        DrugDTO drugDTO = drugMapper.toDto(drug);

        // If url ID doesn't match entity ID, it will throw BadRequestAlertException
        restDrugMockMvc
            .perform(put(ENTITY_API_URL).contentType(MediaType.APPLICATION_JSON).content(om.writeValueAsBytes(drugDTO)))
            .andExpect(status().isMethodNotAllowed());

        // Validate the Drug in the database
        assertSameRepositoryCount(databaseSizeBeforeUpdate);
    }

    @Test
    @Transactional
    void partialUpdateDrugWithPatch() throws Exception {
        // Initialize the database
        insertedDrug = drugRepository.saveAndFlush(drug);

        long databaseSizeBeforeUpdate = getRepositoryCount();

        // Update the drug using partial update
        Drug partialUpdatedDrug = new Drug();
        partialUpdatedDrug.setId(drug.getId());

        partialUpdatedDrug.unit(UPDATED_UNIT).reservedStock(UPDATED_RESERVED_STOCK).price(UPDATED_PRICE).active(UPDATED_ACTIVE);

        restDrugMockMvc
            .perform(
                patch(ENTITY_API_URL_ID, partialUpdatedDrug.getId())
                    .contentType("application/merge-patch+json")
                    .content(om.writeValueAsBytes(partialUpdatedDrug))
            )
            .andExpect(status().isOk());

        // Validate the Drug in the database

        assertSameRepositoryCount(databaseSizeBeforeUpdate);
        assertDrugUpdatableFieldsEquals(createUpdateProxyForBean(partialUpdatedDrug, drug), getPersistedDrug(drug));
    }

    @Test
    @Transactional
    void fullUpdateDrugWithPatch() throws Exception {
        // Initialize the database
        insertedDrug = drugRepository.saveAndFlush(drug);

        long databaseSizeBeforeUpdate = getRepositoryCount();

        // Update the drug using partial update
        Drug partialUpdatedDrug = new Drug();
        partialUpdatedDrug.setId(drug.getId());

        partialUpdatedDrug
            .name(UPDATED_NAME)
            .unit(UPDATED_UNIT)
            .currentStock(UPDATED_CURRENT_STOCK)
            .reservedStock(UPDATED_RESERVED_STOCK)
            .lowStockThreshold(UPDATED_LOW_STOCK_THRESHOLD)
            .price(UPDATED_PRICE)
            .classification(UPDATED_CLASSIFICATION)
            .active(UPDATED_ACTIVE);

        restDrugMockMvc
            .perform(
                patch(ENTITY_API_URL_ID, partialUpdatedDrug.getId())
                    .contentType("application/merge-patch+json")
                    .content(om.writeValueAsBytes(partialUpdatedDrug))
            )
            .andExpect(status().isOk());

        // Validate the Drug in the database

        assertSameRepositoryCount(databaseSizeBeforeUpdate);
        assertDrugUpdatableFieldsEquals(partialUpdatedDrug, getPersistedDrug(partialUpdatedDrug));
    }

    @Test
    @Transactional
    void patchNonExistingDrug() throws Exception {
        long databaseSizeBeforeUpdate = getRepositoryCount();
        drug.setId(longCount.incrementAndGet());

        // Create the Drug
        DrugDTO drugDTO = drugMapper.toDto(drug);

        // If the entity doesn't have an ID, it will throw BadRequestAlertException
        restDrugMockMvc
            .perform(
                patch(ENTITY_API_URL_ID, drugDTO.getId()).contentType("application/merge-patch+json").content(om.writeValueAsBytes(drugDTO))
            )
            .andExpect(status().isBadRequest());

        // Validate the Drug in the database
        assertSameRepositoryCount(databaseSizeBeforeUpdate);
    }

    @Test
    @Transactional
    void patchWithIdMismatchDrug() throws Exception {
        long databaseSizeBeforeUpdate = getRepositoryCount();
        drug.setId(longCount.incrementAndGet());

        // Create the Drug
        DrugDTO drugDTO = drugMapper.toDto(drug);

        // If url ID doesn't match entity ID, it will throw BadRequestAlertException
        restDrugMockMvc
            .perform(
                patch(ENTITY_API_URL_ID, longCount.incrementAndGet())
                    .contentType("application/merge-patch+json")
                    .content(om.writeValueAsBytes(drugDTO))
            )
            .andExpect(status().isBadRequest());

        // Validate the Drug in the database
        assertSameRepositoryCount(databaseSizeBeforeUpdate);
    }

    @Test
    @Transactional
    void patchWithMissingIdPathParamDrug() throws Exception {
        long databaseSizeBeforeUpdate = getRepositoryCount();
        drug.setId(longCount.incrementAndGet());

        // Create the Drug
        DrugDTO drugDTO = drugMapper.toDto(drug);

        // If url ID doesn't match entity ID, it will throw BadRequestAlertException
        restDrugMockMvc
            .perform(patch(ENTITY_API_URL).contentType("application/merge-patch+json").content(om.writeValueAsBytes(drugDTO)))
            .andExpect(status().isMethodNotAllowed());

        // Validate the Drug in the database
        assertSameRepositoryCount(databaseSizeBeforeUpdate);
    }

    @Test
    @Transactional
    void deleteDrug() throws Exception {
        // Initialize the database
        insertedDrug = drugRepository.saveAndFlush(drug);

        long databaseSizeBeforeDelete = getRepositoryCount();

        // Delete the drug
        restDrugMockMvc
            .perform(delete(ENTITY_API_URL_ID, drug.getId()).accept(MediaType.APPLICATION_JSON))
            .andExpect(status().isNoContent());

        // Validate the database contains one less item
        assertDecrementedRepositoryCount(databaseSizeBeforeDelete);
    }

    protected long getRepositoryCount() {
        return drugRepository.count();
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

    protected Drug getPersistedDrug(Drug drug) {
        return drugRepository.findById(drug.getId()).orElseThrow();
    }

    protected void assertPersistedDrugToMatchAllProperties(Drug expectedDrug) {
        assertDrugAllPropertiesEquals(expectedDrug, getPersistedDrug(expectedDrug));
    }

    protected void assertPersistedDrugToMatchUpdatableProperties(Drug expectedDrug) {
        assertDrugAllUpdatablePropertiesEquals(expectedDrug, getPersistedDrug(expectedDrug));
    }
}
