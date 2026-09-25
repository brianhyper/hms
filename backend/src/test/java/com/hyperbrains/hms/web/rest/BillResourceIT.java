package com.hyperbrains.hms.web.rest;

import static com.hyperbrains.hms.domain.BillAsserts.*;
import static com.hyperbrains.hms.web.rest.TestUtil.createUpdateProxyForBean;
import static com.hyperbrains.hms.web.rest.TestUtil.sameNumber;
import static org.assertj.core.api.Assertions.assertThat;
import static org.hamcrest.Matchers.hasItem;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.hyperbrains.hms.IntegrationTest;
import com.hyperbrains.hms.domain.Bill;
import com.hyperbrains.hms.domain.enumeration.BillStatus;
import com.hyperbrains.hms.repository.BillRepository;
import com.hyperbrains.hms.service.dto.BillDTO;
import com.hyperbrains.hms.service.mapper.BillMapper;
import jakarta.persistence.EntityManager;
import java.math.BigDecimal;
import java.time.Instant;
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
 * Integration tests for the {@link BillResource} REST controller.
 */
@IntegrationTest
@AutoConfigureMockMvc
// A direct write sets the bill's total and status by hand, marking it paid with no money recorded and
// no medicine released. Settlement goes through /api/visit-payments/{visitId}/pay; this test covers the
// super-admin escape hatch.
@WithMockUser(authorities = AuthoritiesConstants.SUPER_ADMIN)
class BillResourceIT {

    private static final BigDecimal DEFAULT_TOTAL_AMOUNT = new BigDecimal(0);
    private static final BigDecimal UPDATED_TOTAL_AMOUNT = new BigDecimal(1);

    private static final BillStatus DEFAULT_STATUS = BillStatus.UNPAID;
    private static final BillStatus UPDATED_STATUS = BillStatus.PAID;

    private static final Instant DEFAULT_PAID_AT = Instant.ofEpochMilli(0L);
    private static final Instant UPDATED_PAID_AT = Instant.ofEpochMilli(1790236635213L);

    private static final String ENTITY_API_URL = "/api/bills";
    private static final String ENTITY_API_URL_ID = ENTITY_API_URL + "/{id}";

    private static final Random random = new Random();
    private static final AtomicLong longCount = new AtomicLong(random.nextInt() + 2L * Integer.MAX_VALUE);

    @Autowired
    private ObjectMapper om;

    @Autowired
    private BillRepository billRepository;

    @Autowired
    private BillMapper billMapper;

    @Autowired
    private EntityManager em;

    @Autowired
    private MockMvc restBillMockMvc;

    private Bill bill;

    private Bill insertedBill;

    /**
     * Create an entity for this test.
     *
     * This is a static method, as tests for other entities might also need it,
     * if they test an entity which requires the current entity.
     */
    public static Bill createEntity(EntityManager em) {
        Bill bill = new Bill().totalAmount(DEFAULT_TOTAL_AMOUNT).status(DEFAULT_STATUS).paidAt(DEFAULT_PAID_AT);
        return bill;
    }

    /**
     * Create an updated entity for this test.
     *
     * This is a static method, as tests for other entities might also need it,
     * if they test an entity which requires the current entity.
     */
    public static Bill createUpdatedEntity(EntityManager em) {
        Bill updatedBill = new Bill().totalAmount(UPDATED_TOTAL_AMOUNT).status(UPDATED_STATUS).paidAt(UPDATED_PAID_AT);
        return updatedBill;
    }

    @BeforeEach
    void initTest() {
        bill = createEntity(em);
    }

    @AfterEach
    void cleanup() {
        if (insertedBill != null) {
            // deleteById re-reads the row first so the optimistic-locking check uses the current
            // version. The cached entity is stale whenever a test modified it (Bill carries @Version).
            billRepository.deleteById(insertedBill.getId());
            insertedBill = null;
        }
    }

    @Test
    @Transactional
    void createBill() throws Exception {
        long databaseSizeBeforeCreate = getRepositoryCount();
        // Create the Bill
        BillDTO billDTO = billMapper.toDto(bill);
        var returnedBillDTO = om.readValue(
            restBillMockMvc
                .perform(post(ENTITY_API_URL).contentType(MediaType.APPLICATION_JSON).content(om.writeValueAsBytes(billDTO)))
                .andExpect(status().isCreated())
                .andReturn()
                .getResponse()
                .getContentAsString(),
            BillDTO.class
        );

        // Validate the Bill in the database
        assertIncrementedRepositoryCount(databaseSizeBeforeCreate);
        var returnedBill = billMapper.toEntity(returnedBillDTO);
        assertBillUpdatableFieldsEquals(returnedBill, getPersistedBill(returnedBill));

        insertedBill = returnedBill;
    }

    @Test
    @Transactional
    void createBillWithExistingId() throws Exception {
        // Create the Bill with an existing ID
        bill.setId(1L);
        BillDTO billDTO = billMapper.toDto(bill);

        long databaseSizeBeforeCreate = getRepositoryCount();

        // An entity with an existing ID cannot be created, so this API call must fail
        restBillMockMvc
            .perform(post(ENTITY_API_URL).contentType(MediaType.APPLICATION_JSON).content(om.writeValueAsBytes(billDTO)))
            .andExpect(status().isBadRequest());

        // Validate the Bill in the database
        assertSameRepositoryCount(databaseSizeBeforeCreate);
    }

    @Test
    @Transactional
    void checkTotalAmountIsRequired() throws Exception {
        long databaseSizeBeforeTest = getRepositoryCount();
        // set the field null
        bill.setTotalAmount(null);

        // Create the Bill, which fails.
        BillDTO billDTO = billMapper.toDto(bill);

        restBillMockMvc
            .perform(post(ENTITY_API_URL).contentType(MediaType.APPLICATION_JSON).content(om.writeValueAsBytes(billDTO)))
            .andExpect(status().isBadRequest());

        assertSameRepositoryCount(databaseSizeBeforeTest);
    }

    @Test
    @Transactional
    void checkStatusIsRequired() throws Exception {
        long databaseSizeBeforeTest = getRepositoryCount();
        // set the field null
        bill.setStatus(null);

        // Create the Bill, which fails.
        BillDTO billDTO = billMapper.toDto(bill);

        restBillMockMvc
            .perform(post(ENTITY_API_URL).contentType(MediaType.APPLICATION_JSON).content(om.writeValueAsBytes(billDTO)))
            .andExpect(status().isBadRequest());

        assertSameRepositoryCount(databaseSizeBeforeTest);
    }

    @Test
    @Transactional
    void getAllBills() throws Exception {
        // Initialize the database
        insertedBill = billRepository.saveAndFlush(bill);

        // Get all the billList
        restBillMockMvc
            .perform(get(ENTITY_API_URL + "?sort=id,desc"))
            .andExpect(status().isOk())
            .andExpect(content().contentType(MediaType.APPLICATION_JSON_VALUE))
            .andExpect(jsonPath("$.[*].id").value(hasItem(bill.getId().intValue())))
            .andExpect(jsonPath("$.[*].totalAmount").value(hasItem(sameNumber(DEFAULT_TOTAL_AMOUNT))))
            .andExpect(jsonPath("$.[*].status").value(hasItem(DEFAULT_STATUS.toString())))
            .andExpect(jsonPath("$.[*].paidAt").value(hasItem(DEFAULT_PAID_AT.toString())));
    }

    @Test
    @Transactional
    void getBill() throws Exception {
        // Initialize the database
        insertedBill = billRepository.saveAndFlush(bill);

        // Get the bill
        restBillMockMvc
            .perform(get(ENTITY_API_URL_ID, bill.getId()))
            .andExpect(status().isOk())
            .andExpect(content().contentType(MediaType.APPLICATION_JSON_VALUE))
            .andExpect(jsonPath("$.id").value(bill.getId().intValue()))
            .andExpect(jsonPath("$.totalAmount").value(sameNumber(DEFAULT_TOTAL_AMOUNT)))
            .andExpect(jsonPath("$.status").value(DEFAULT_STATUS.toString()))
            .andExpect(jsonPath("$.paidAt").value(DEFAULT_PAID_AT.toString()));
    }

    @Test
    @Transactional
    void getNonExistingBill() throws Exception {
        // Get the bill
        restBillMockMvc.perform(get(ENTITY_API_URL_ID, Long.MAX_VALUE)).andExpect(status().isNotFound());
    }

    @Test
    @Transactional
    void putExistingBill() throws Exception {
        // Initialize the database
        insertedBill = billRepository.saveAndFlush(bill);

        long databaseSizeBeforeUpdate = getRepositoryCount();

        // Update the bill
        Bill updatedBill = billRepository.findById(bill.getId()).orElseThrow();
        // Disconnect from session so that the updates on updatedBill are not directly saved in db
        em.detach(updatedBill);
        updatedBill.totalAmount(UPDATED_TOTAL_AMOUNT).status(UPDATED_STATUS).paidAt(UPDATED_PAID_AT);
        BillDTO billDTO = billMapper.toDto(updatedBill);

        restBillMockMvc
            .perform(put(ENTITY_API_URL_ID, billDTO.getId()).contentType(MediaType.APPLICATION_JSON).content(om.writeValueAsBytes(billDTO)))
            .andExpect(status().isOk());

        // Validate the Bill in the database
        assertSameRepositoryCount(databaseSizeBeforeUpdate);
        assertPersistedBillToMatchAllProperties(updatedBill);
    }

    @Test
    @Transactional
    void putNonExistingBill() throws Exception {
        long databaseSizeBeforeUpdate = getRepositoryCount();
        bill.setId(longCount.incrementAndGet());

        // Create the Bill
        BillDTO billDTO = billMapper.toDto(bill);

        // If the entity doesn't have an ID, it will throw BadRequestAlertException
        restBillMockMvc
            .perform(put(ENTITY_API_URL_ID, billDTO.getId()).contentType(MediaType.APPLICATION_JSON).content(om.writeValueAsBytes(billDTO)))
            .andExpect(status().isBadRequest());

        // Validate the Bill in the database
        assertSameRepositoryCount(databaseSizeBeforeUpdate);
    }

    @Test
    @Transactional
    void putWithIdMismatchBill() throws Exception {
        long databaseSizeBeforeUpdate = getRepositoryCount();
        bill.setId(longCount.incrementAndGet());

        // Create the Bill
        BillDTO billDTO = billMapper.toDto(bill);

        // If url ID doesn't match entity ID, it will throw BadRequestAlertException
        restBillMockMvc
            .perform(
                put(ENTITY_API_URL_ID, longCount.incrementAndGet())
                    .contentType(MediaType.APPLICATION_JSON)
                    .content(om.writeValueAsBytes(billDTO))
            )
            .andExpect(status().isBadRequest());

        // Validate the Bill in the database
        assertSameRepositoryCount(databaseSizeBeforeUpdate);
    }

    @Test
    @Transactional
    void putWithMissingIdPathParamBill() throws Exception {
        long databaseSizeBeforeUpdate = getRepositoryCount();
        bill.setId(longCount.incrementAndGet());

        // Create the Bill
        BillDTO billDTO = billMapper.toDto(bill);

        // If url ID doesn't match entity ID, it will throw BadRequestAlertException
        restBillMockMvc
            .perform(put(ENTITY_API_URL).contentType(MediaType.APPLICATION_JSON).content(om.writeValueAsBytes(billDTO)))
            .andExpect(status().isMethodNotAllowed());

        // Validate the Bill in the database
        assertSameRepositoryCount(databaseSizeBeforeUpdate);
    }

    @Test
    @Transactional
    void partialUpdateBillWithPatch() throws Exception {
        // Initialize the database
        insertedBill = billRepository.saveAndFlush(bill);

        long databaseSizeBeforeUpdate = getRepositoryCount();

        // Update the bill using partial update
        Bill partialUpdatedBill = new Bill();
        partialUpdatedBill.setId(bill.getId());

        restBillMockMvc
            .perform(
                patch(ENTITY_API_URL_ID, partialUpdatedBill.getId())
                    .contentType("application/merge-patch+json")
                    .content(om.writeValueAsBytes(partialUpdatedBill))
            )
            .andExpect(status().isOk());

        // Validate the Bill in the database

        assertSameRepositoryCount(databaseSizeBeforeUpdate);
        assertBillUpdatableFieldsEquals(createUpdateProxyForBean(partialUpdatedBill, bill), getPersistedBill(bill));
    }

    @Test
    @Transactional
    void fullUpdateBillWithPatch() throws Exception {
        // Initialize the database
        insertedBill = billRepository.saveAndFlush(bill);

        long databaseSizeBeforeUpdate = getRepositoryCount();

        // Update the bill using partial update
        Bill partialUpdatedBill = new Bill();
        partialUpdatedBill.setId(bill.getId());

        partialUpdatedBill.totalAmount(UPDATED_TOTAL_AMOUNT).status(UPDATED_STATUS).paidAt(UPDATED_PAID_AT);

        restBillMockMvc
            .perform(
                patch(ENTITY_API_URL_ID, partialUpdatedBill.getId())
                    .contentType("application/merge-patch+json")
                    .content(om.writeValueAsBytes(partialUpdatedBill))
            )
            .andExpect(status().isOk());

        // Validate the Bill in the database

        assertSameRepositoryCount(databaseSizeBeforeUpdate);
        assertBillUpdatableFieldsEquals(partialUpdatedBill, getPersistedBill(partialUpdatedBill));
    }

    @Test
    @Transactional
    void patchNonExistingBill() throws Exception {
        long databaseSizeBeforeUpdate = getRepositoryCount();
        bill.setId(longCount.incrementAndGet());

        // Create the Bill
        BillDTO billDTO = billMapper.toDto(bill);

        // If the entity doesn't have an ID, it will throw BadRequestAlertException
        restBillMockMvc
            .perform(
                patch(ENTITY_API_URL_ID, billDTO.getId()).contentType("application/merge-patch+json").content(om.writeValueAsBytes(billDTO))
            )
            .andExpect(status().isBadRequest());

        // Validate the Bill in the database
        assertSameRepositoryCount(databaseSizeBeforeUpdate);
    }

    @Test
    @Transactional
    void patchWithIdMismatchBill() throws Exception {
        long databaseSizeBeforeUpdate = getRepositoryCount();
        bill.setId(longCount.incrementAndGet());

        // Create the Bill
        BillDTO billDTO = billMapper.toDto(bill);

        // If url ID doesn't match entity ID, it will throw BadRequestAlertException
        restBillMockMvc
            .perform(
                patch(ENTITY_API_URL_ID, longCount.incrementAndGet())
                    .contentType("application/merge-patch+json")
                    .content(om.writeValueAsBytes(billDTO))
            )
            .andExpect(status().isBadRequest());

        // Validate the Bill in the database
        assertSameRepositoryCount(databaseSizeBeforeUpdate);
    }

    @Test
    @Transactional
    void patchWithMissingIdPathParamBill() throws Exception {
        long databaseSizeBeforeUpdate = getRepositoryCount();
        bill.setId(longCount.incrementAndGet());

        // Create the Bill
        BillDTO billDTO = billMapper.toDto(bill);

        // If url ID doesn't match entity ID, it will throw BadRequestAlertException
        restBillMockMvc
            .perform(patch(ENTITY_API_URL).contentType("application/merge-patch+json").content(om.writeValueAsBytes(billDTO)))
            .andExpect(status().isMethodNotAllowed());

        // Validate the Bill in the database
        assertSameRepositoryCount(databaseSizeBeforeUpdate);
    }

    @Test
    @Transactional
    void deleteBill() throws Exception {
        // Initialize the database
        insertedBill = billRepository.saveAndFlush(bill);

        long databaseSizeBeforeDelete = getRepositoryCount();

        // Delete the bill
        restBillMockMvc
            .perform(delete(ENTITY_API_URL_ID, bill.getId()).accept(MediaType.APPLICATION_JSON))
            .andExpect(status().isNoContent());

        // Validate the database contains one less item
        assertDecrementedRepositoryCount(databaseSizeBeforeDelete);
    }

    protected long getRepositoryCount() {
        return billRepository.count();
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

    protected Bill getPersistedBill(Bill bill) {
        return billRepository.findById(bill.getId()).orElseThrow();
    }

    protected void assertPersistedBillToMatchAllProperties(Bill expectedBill) {
        assertBillAllPropertiesEquals(expectedBill, getPersistedBill(expectedBill));
    }

    protected void assertPersistedBillToMatchUpdatableProperties(Bill expectedBill) {
        assertBillAllUpdatablePropertiesEquals(expectedBill, getPersistedBill(expectedBill));
    }
}
