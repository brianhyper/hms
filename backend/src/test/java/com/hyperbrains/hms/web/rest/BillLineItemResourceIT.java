package com.hyperbrains.hms.web.rest;

import static com.hyperbrains.hms.domain.BillLineItemAsserts.*;
import static com.hyperbrains.hms.web.rest.TestUtil.createUpdateProxyForBean;
import static com.hyperbrains.hms.web.rest.TestUtil.sameNumber;
import static org.assertj.core.api.Assertions.assertThat;
import static org.hamcrest.Matchers.hasItem;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.hyperbrains.hms.IntegrationTest;
import com.hyperbrains.hms.domain.Bill;
import com.hyperbrains.hms.domain.BillLineItem;
import com.hyperbrains.hms.domain.enumeration.BillLineSourceType;
import com.hyperbrains.hms.repository.BillLineItemRepository;
import com.hyperbrains.hms.service.dto.BillLineItemDTO;
import com.hyperbrains.hms.service.mapper.BillLineItemMapper;
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
 * Integration tests for the {@link BillLineItemResource} REST controller.
 */
@IntegrationTest
@AutoConfigureMockMvc
@WithMockUser(authorities = AuthoritiesConstants.SUPER_ADMIN)
class BillLineItemResourceIT {

    private static final String DEFAULT_DESCRIPTION = "AAAAAAAAAA";
    private static final String UPDATED_DESCRIPTION = "BBBBBBBBBB";

    private static final BigDecimal DEFAULT_AMOUNT = new BigDecimal(0);
    private static final BigDecimal UPDATED_AMOUNT = new BigDecimal(1);

    private static final BillLineSourceType DEFAULT_SOURCE_TYPE = BillLineSourceType.CONSULTATION;
    private static final BillLineSourceType UPDATED_SOURCE_TYPE = BillLineSourceType.LAB;

    private static final String ENTITY_API_URL = "/api/bill-line-items";
    private static final String ENTITY_API_URL_ID = ENTITY_API_URL + "/{id}";

    private static final Random random = new Random();
    private static final AtomicLong longCount = new AtomicLong(random.nextInt() + 2L * Integer.MAX_VALUE);

    @Autowired
    private ObjectMapper om;

    @Autowired
    private BillLineItemRepository billLineItemRepository;

    @Autowired
    private BillLineItemMapper billLineItemMapper;

    @Autowired
    private EntityManager em;

    @Autowired
    private MockMvc restBillLineItemMockMvc;

    private BillLineItem billLineItem;

    private BillLineItem insertedBillLineItem;

    /**
     * Create an entity for this test.
     *
     * This is a static method, as tests for other entities might also need it,
     * if they test an entity which requires the current entity.
     */
    public static BillLineItem createEntity(EntityManager em) {
        BillLineItem billLineItem = new BillLineItem()
            .description(DEFAULT_DESCRIPTION)
            .amount(DEFAULT_AMOUNT)
            .sourceType(DEFAULT_SOURCE_TYPE);
        // Add required entity
        Bill bill;
        if (TestUtil.findAll(em, Bill.class).isEmpty()) {
            bill = BillResourceIT.createEntity(em);
            em.persist(bill);
            em.flush();
        } else {
            bill = TestUtil.findAll(em, Bill.class).get(0);
        }
        billLineItem.setBill(bill);
        return billLineItem;
    }

    /**
     * Create an updated entity for this test.
     *
     * This is a static method, as tests for other entities might also need it,
     * if they test an entity which requires the current entity.
     */
    public static BillLineItem createUpdatedEntity(EntityManager em) {
        BillLineItem updatedBillLineItem = new BillLineItem()
            .description(UPDATED_DESCRIPTION)
            .amount(UPDATED_AMOUNT)
            .sourceType(UPDATED_SOURCE_TYPE);
        // Add required entity
        Bill bill;
        if (TestUtil.findAll(em, Bill.class).isEmpty()) {
            bill = BillResourceIT.createUpdatedEntity(em);
            em.persist(bill);
            em.flush();
        } else {
            bill = TestUtil.findAll(em, Bill.class).get(0);
        }
        updatedBillLineItem.setBill(bill);
        return updatedBillLineItem;
    }

    @BeforeEach
    void initTest() {
        billLineItem = createEntity(em);
    }

    @AfterEach
    void cleanup() {
        if (insertedBillLineItem != null) {
            billLineItemRepository.delete(insertedBillLineItem);
            insertedBillLineItem = null;
        }
    }

    @Test
    @Transactional
    void createBillLineItem() throws Exception {
        long databaseSizeBeforeCreate = getRepositoryCount();
        // Create the BillLineItem
        BillLineItemDTO billLineItemDTO = billLineItemMapper.toDto(billLineItem);
        var returnedBillLineItemDTO = om.readValue(
            restBillLineItemMockMvc
                .perform(post(ENTITY_API_URL).contentType(MediaType.APPLICATION_JSON).content(om.writeValueAsBytes(billLineItemDTO)))
                .andExpect(status().isCreated())
                .andReturn()
                .getResponse()
                .getContentAsString(),
            BillLineItemDTO.class
        );

        // Validate the BillLineItem in the database
        assertIncrementedRepositoryCount(databaseSizeBeforeCreate);
        var returnedBillLineItem = billLineItemMapper.toEntity(returnedBillLineItemDTO);
        assertBillLineItemUpdatableFieldsEquals(returnedBillLineItem, getPersistedBillLineItem(returnedBillLineItem));

        insertedBillLineItem = returnedBillLineItem;
    }

    @Test
    @Transactional
    void createBillLineItemWithExistingId() throws Exception {
        // Create the BillLineItem with an existing ID
        billLineItem.setId(1L);
        BillLineItemDTO billLineItemDTO = billLineItemMapper.toDto(billLineItem);

        long databaseSizeBeforeCreate = getRepositoryCount();

        // An entity with an existing ID cannot be created, so this API call must fail
        restBillLineItemMockMvc
            .perform(post(ENTITY_API_URL).contentType(MediaType.APPLICATION_JSON).content(om.writeValueAsBytes(billLineItemDTO)))
            .andExpect(status().isBadRequest());

        // Validate the BillLineItem in the database
        assertSameRepositoryCount(databaseSizeBeforeCreate);
    }

    @Test
    @Transactional
    void checkDescriptionIsRequired() throws Exception {
        long databaseSizeBeforeTest = getRepositoryCount();
        // set the field null
        billLineItem.setDescription(null);

        // Create the BillLineItem, which fails.
        BillLineItemDTO billLineItemDTO = billLineItemMapper.toDto(billLineItem);

        restBillLineItemMockMvc
            .perform(post(ENTITY_API_URL).contentType(MediaType.APPLICATION_JSON).content(om.writeValueAsBytes(billLineItemDTO)))
            .andExpect(status().isBadRequest());

        assertSameRepositoryCount(databaseSizeBeforeTest);
    }

    @Test
    @Transactional
    void checkAmountIsRequired() throws Exception {
        long databaseSizeBeforeTest = getRepositoryCount();
        // set the field null
        billLineItem.setAmount(null);

        // Create the BillLineItem, which fails.
        BillLineItemDTO billLineItemDTO = billLineItemMapper.toDto(billLineItem);

        restBillLineItemMockMvc
            .perform(post(ENTITY_API_URL).contentType(MediaType.APPLICATION_JSON).content(om.writeValueAsBytes(billLineItemDTO)))
            .andExpect(status().isBadRequest());

        assertSameRepositoryCount(databaseSizeBeforeTest);
    }

    @Test
    @Transactional
    void checkSourceTypeIsRequired() throws Exception {
        long databaseSizeBeforeTest = getRepositoryCount();
        // set the field null
        billLineItem.setSourceType(null);

        // Create the BillLineItem, which fails.
        BillLineItemDTO billLineItemDTO = billLineItemMapper.toDto(billLineItem);

        restBillLineItemMockMvc
            .perform(post(ENTITY_API_URL).contentType(MediaType.APPLICATION_JSON).content(om.writeValueAsBytes(billLineItemDTO)))
            .andExpect(status().isBadRequest());

        assertSameRepositoryCount(databaseSizeBeforeTest);
    }

    @Test
    @Transactional
    void getAllBillLineItems() throws Exception {
        // Initialize the database
        insertedBillLineItem = billLineItemRepository.saveAndFlush(billLineItem);

        // Get all the billLineItemList
        restBillLineItemMockMvc
            .perform(get(ENTITY_API_URL + "?sort=id,desc"))
            .andExpect(status().isOk())
            .andExpect(content().contentType(MediaType.APPLICATION_JSON_VALUE))
            .andExpect(jsonPath("$.[*].id").value(hasItem(billLineItem.getId().intValue())))
            .andExpect(jsonPath("$.[*].description").value(hasItem(DEFAULT_DESCRIPTION)))
            .andExpect(jsonPath("$.[*].amount").value(hasItem(sameNumber(DEFAULT_AMOUNT))))
            .andExpect(jsonPath("$.[*].sourceType").value(hasItem(DEFAULT_SOURCE_TYPE.toString())));
    }

    @Test
    @Transactional
    void getBillLineItem() throws Exception {
        // Initialize the database
        insertedBillLineItem = billLineItemRepository.saveAndFlush(billLineItem);

        // Get the billLineItem
        restBillLineItemMockMvc
            .perform(get(ENTITY_API_URL_ID, billLineItem.getId()))
            .andExpect(status().isOk())
            .andExpect(content().contentType(MediaType.APPLICATION_JSON_VALUE))
            .andExpect(jsonPath("$.id").value(billLineItem.getId().intValue()))
            .andExpect(jsonPath("$.description").value(DEFAULT_DESCRIPTION))
            .andExpect(jsonPath("$.amount").value(sameNumber(DEFAULT_AMOUNT)))
            .andExpect(jsonPath("$.sourceType").value(DEFAULT_SOURCE_TYPE.toString()));
    }

    @Test
    @Transactional
    void getNonExistingBillLineItem() throws Exception {
        // Get the billLineItem
        restBillLineItemMockMvc.perform(get(ENTITY_API_URL_ID, Long.MAX_VALUE)).andExpect(status().isNotFound());
    }

    @Test
    @Transactional
    void putExistingBillLineItem() throws Exception {
        // Initialize the database
        insertedBillLineItem = billLineItemRepository.saveAndFlush(billLineItem);

        long databaseSizeBeforeUpdate = getRepositoryCount();

        // Update the billLineItem
        BillLineItem updatedBillLineItem = billLineItemRepository.findById(billLineItem.getId()).orElseThrow();
        // Disconnect from session so that the updates on updatedBillLineItem are not directly saved in db
        em.detach(updatedBillLineItem);
        updatedBillLineItem.description(UPDATED_DESCRIPTION).amount(UPDATED_AMOUNT).sourceType(UPDATED_SOURCE_TYPE);
        BillLineItemDTO billLineItemDTO = billLineItemMapper.toDto(updatedBillLineItem);

        restBillLineItemMockMvc
            .perform(
                put(ENTITY_API_URL_ID, billLineItemDTO.getId())
                    .contentType(MediaType.APPLICATION_JSON)
                    .content(om.writeValueAsBytes(billLineItemDTO))
            )
            .andExpect(status().isOk());

        // Validate the BillLineItem in the database
        assertSameRepositoryCount(databaseSizeBeforeUpdate);
        assertPersistedBillLineItemToMatchAllProperties(updatedBillLineItem);
    }

    @Test
    @Transactional
    void putNonExistingBillLineItem() throws Exception {
        long databaseSizeBeforeUpdate = getRepositoryCount();
        billLineItem.setId(longCount.incrementAndGet());

        // Create the BillLineItem
        BillLineItemDTO billLineItemDTO = billLineItemMapper.toDto(billLineItem);

        // If the entity doesn't have an ID, it will throw BadRequestAlertException
        restBillLineItemMockMvc
            .perform(
                put(ENTITY_API_URL_ID, billLineItemDTO.getId())
                    .contentType(MediaType.APPLICATION_JSON)
                    .content(om.writeValueAsBytes(billLineItemDTO))
            )
            .andExpect(status().isBadRequest());

        // Validate the BillLineItem in the database
        assertSameRepositoryCount(databaseSizeBeforeUpdate);
    }

    @Test
    @Transactional
    void putWithIdMismatchBillLineItem() throws Exception {
        long databaseSizeBeforeUpdate = getRepositoryCount();
        billLineItem.setId(longCount.incrementAndGet());

        // Create the BillLineItem
        BillLineItemDTO billLineItemDTO = billLineItemMapper.toDto(billLineItem);

        // If url ID doesn't match entity ID, it will throw BadRequestAlertException
        restBillLineItemMockMvc
            .perform(
                put(ENTITY_API_URL_ID, longCount.incrementAndGet())
                    .contentType(MediaType.APPLICATION_JSON)
                    .content(om.writeValueAsBytes(billLineItemDTO))
            )
            .andExpect(status().isBadRequest());

        // Validate the BillLineItem in the database
        assertSameRepositoryCount(databaseSizeBeforeUpdate);
    }

    @Test
    @Transactional
    void putWithMissingIdPathParamBillLineItem() throws Exception {
        long databaseSizeBeforeUpdate = getRepositoryCount();
        billLineItem.setId(longCount.incrementAndGet());

        // Create the BillLineItem
        BillLineItemDTO billLineItemDTO = billLineItemMapper.toDto(billLineItem);

        // If url ID doesn't match entity ID, it will throw BadRequestAlertException
        restBillLineItemMockMvc
            .perform(put(ENTITY_API_URL).contentType(MediaType.APPLICATION_JSON).content(om.writeValueAsBytes(billLineItemDTO)))
            .andExpect(status().isMethodNotAllowed());

        // Validate the BillLineItem in the database
        assertSameRepositoryCount(databaseSizeBeforeUpdate);
    }

    @Test
    @Transactional
    void partialUpdateBillLineItemWithPatch() throws Exception {
        // Initialize the database
        insertedBillLineItem = billLineItemRepository.saveAndFlush(billLineItem);

        long databaseSizeBeforeUpdate = getRepositoryCount();

        // Update the billLineItem using partial update
        BillLineItem partialUpdatedBillLineItem = new BillLineItem();
        partialUpdatedBillLineItem.setId(billLineItem.getId());

        partialUpdatedBillLineItem.description(UPDATED_DESCRIPTION).sourceType(UPDATED_SOURCE_TYPE);

        restBillLineItemMockMvc
            .perform(
                patch(ENTITY_API_URL_ID, partialUpdatedBillLineItem.getId())
                    .contentType("application/merge-patch+json")
                    .content(om.writeValueAsBytes(partialUpdatedBillLineItem))
            )
            .andExpect(status().isOk());

        // Validate the BillLineItem in the database

        assertSameRepositoryCount(databaseSizeBeforeUpdate);
        assertBillLineItemUpdatableFieldsEquals(
            createUpdateProxyForBean(partialUpdatedBillLineItem, billLineItem),
            getPersistedBillLineItem(billLineItem)
        );
    }

    @Test
    @Transactional
    void fullUpdateBillLineItemWithPatch() throws Exception {
        // Initialize the database
        insertedBillLineItem = billLineItemRepository.saveAndFlush(billLineItem);

        long databaseSizeBeforeUpdate = getRepositoryCount();

        // Update the billLineItem using partial update
        BillLineItem partialUpdatedBillLineItem = new BillLineItem();
        partialUpdatedBillLineItem.setId(billLineItem.getId());

        partialUpdatedBillLineItem.description(UPDATED_DESCRIPTION).amount(UPDATED_AMOUNT).sourceType(UPDATED_SOURCE_TYPE);

        restBillLineItemMockMvc
            .perform(
                patch(ENTITY_API_URL_ID, partialUpdatedBillLineItem.getId())
                    .contentType("application/merge-patch+json")
                    .content(om.writeValueAsBytes(partialUpdatedBillLineItem))
            )
            .andExpect(status().isOk());

        // Validate the BillLineItem in the database

        assertSameRepositoryCount(databaseSizeBeforeUpdate);
        assertBillLineItemUpdatableFieldsEquals(partialUpdatedBillLineItem, getPersistedBillLineItem(partialUpdatedBillLineItem));
    }

    @Test
    @Transactional
    void patchNonExistingBillLineItem() throws Exception {
        long databaseSizeBeforeUpdate = getRepositoryCount();
        billLineItem.setId(longCount.incrementAndGet());

        // Create the BillLineItem
        BillLineItemDTO billLineItemDTO = billLineItemMapper.toDto(billLineItem);

        // If the entity doesn't have an ID, it will throw BadRequestAlertException
        restBillLineItemMockMvc
            .perform(
                patch(ENTITY_API_URL_ID, billLineItemDTO.getId())
                    .contentType("application/merge-patch+json")
                    .content(om.writeValueAsBytes(billLineItemDTO))
            )
            .andExpect(status().isBadRequest());

        // Validate the BillLineItem in the database
        assertSameRepositoryCount(databaseSizeBeforeUpdate);
    }

    @Test
    @Transactional
    void patchWithIdMismatchBillLineItem() throws Exception {
        long databaseSizeBeforeUpdate = getRepositoryCount();
        billLineItem.setId(longCount.incrementAndGet());

        // Create the BillLineItem
        BillLineItemDTO billLineItemDTO = billLineItemMapper.toDto(billLineItem);

        // If url ID doesn't match entity ID, it will throw BadRequestAlertException
        restBillLineItemMockMvc
            .perform(
                patch(ENTITY_API_URL_ID, longCount.incrementAndGet())
                    .contentType("application/merge-patch+json")
                    .content(om.writeValueAsBytes(billLineItemDTO))
            )
            .andExpect(status().isBadRequest());

        // Validate the BillLineItem in the database
        assertSameRepositoryCount(databaseSizeBeforeUpdate);
    }

    @Test
    @Transactional
    void patchWithMissingIdPathParamBillLineItem() throws Exception {
        long databaseSizeBeforeUpdate = getRepositoryCount();
        billLineItem.setId(longCount.incrementAndGet());

        // Create the BillLineItem
        BillLineItemDTO billLineItemDTO = billLineItemMapper.toDto(billLineItem);

        // If url ID doesn't match entity ID, it will throw BadRequestAlertException
        restBillLineItemMockMvc
            .perform(patch(ENTITY_API_URL).contentType("application/merge-patch+json").content(om.writeValueAsBytes(billLineItemDTO)))
            .andExpect(status().isMethodNotAllowed());

        // Validate the BillLineItem in the database
        assertSameRepositoryCount(databaseSizeBeforeUpdate);
    }

    @Test
    @Transactional
    void deleteBillLineItem() throws Exception {
        // Initialize the database
        insertedBillLineItem = billLineItemRepository.saveAndFlush(billLineItem);

        long databaseSizeBeforeDelete = getRepositoryCount();

        // Delete the billLineItem
        restBillLineItemMockMvc
            .perform(delete(ENTITY_API_URL_ID, billLineItem.getId()).accept(MediaType.APPLICATION_JSON))
            .andExpect(status().isNoContent());

        // Validate the database contains one less item
        assertDecrementedRepositoryCount(databaseSizeBeforeDelete);
    }

    protected long getRepositoryCount() {
        return billLineItemRepository.count();
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

    protected BillLineItem getPersistedBillLineItem(BillLineItem billLineItem) {
        return billLineItemRepository.findById(billLineItem.getId()).orElseThrow();
    }

    protected void assertPersistedBillLineItemToMatchAllProperties(BillLineItem expectedBillLineItem) {
        assertBillLineItemAllPropertiesEquals(expectedBillLineItem, getPersistedBillLineItem(expectedBillLineItem));
    }

    protected void assertPersistedBillLineItemToMatchUpdatableProperties(BillLineItem expectedBillLineItem) {
        assertBillLineItemAllUpdatablePropertiesEquals(expectedBillLineItem, getPersistedBillLineItem(expectedBillLineItem));
    }
}
