package com.hyperbrains.hms.web.rest;

import static com.hyperbrains.hms.domain.PaymentPlanAsserts.*;
import static com.hyperbrains.hms.web.rest.TestUtil.createUpdateProxyForBean;
import static com.hyperbrains.hms.web.rest.TestUtil.sameNumber;
import static org.assertj.core.api.Assertions.assertThat;
import static org.hamcrest.Matchers.hasItem;
import static org.mockito.Mockito.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.hyperbrains.hms.IntegrationTest;
import com.hyperbrains.hms.domain.Bill;
import com.hyperbrains.hms.domain.PaymentPlan;
import com.hyperbrains.hms.domain.User;
import com.hyperbrains.hms.domain.enumeration.PaymentPlanStatus;
import com.hyperbrains.hms.repository.PaymentPlanRepository;
import com.hyperbrains.hms.repository.UserRepository;
import com.hyperbrains.hms.service.PaymentPlanService;
import com.hyperbrains.hms.service.dto.PaymentPlanDTO;
import com.hyperbrains.hms.service.mapper.PaymentPlanMapper;
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
 * Integration tests for the {@link PaymentPlanResource} REST controller.
 */
@IntegrationTest
@ExtendWith(MockitoExtension.class)
@AutoConfigureMockMvc
@WithMockUser
class PaymentPlanResourceIT {

    private static final BigDecimal DEFAULT_TOTAL_OWED = new BigDecimal(0);
    private static final BigDecimal UPDATED_TOTAL_OWED = new BigDecimal(1);

    private static final Instant DEFAULT_AGREED_AT = Instant.ofEpochMilli(0L);
    private static final Instant UPDATED_AGREED_AT = Instant.ofEpochMilli(1790236635213L);

    private static final String DEFAULT_GUARANTOR_NAME = "AAAAAAAAAA";
    private static final String UPDATED_GUARANTOR_NAME = "BBBBBBBBBB";

    private static final String DEFAULT_GUARANTOR_RELATIONSHIP = "AAAAAAAAAA";
    private static final String UPDATED_GUARANTOR_RELATIONSHIP = "BBBBBBBBBB";

    private static final String DEFAULT_GUARANTOR_PHONE = "AAAAAAAAAA";
    private static final String UPDATED_GUARANTOR_PHONE = "BBBBBBBBBB";

    private static final String DEFAULT_NOTES = "AAAAAAAAAA";
    private static final String UPDATED_NOTES = "BBBBBBBBBB";

    private static final PaymentPlanStatus DEFAULT_STATUS = PaymentPlanStatus.ACTIVE;
    private static final PaymentPlanStatus UPDATED_STATUS = PaymentPlanStatus.COMPLETED;

    private static final String ENTITY_API_URL = "/api/payment-plans";
    private static final String ENTITY_API_URL_ID = ENTITY_API_URL + "/{id}";

    private static final Random random = new Random();
    private static final AtomicLong longCount = new AtomicLong(random.nextInt() + 2L * Integer.MAX_VALUE);

    @Autowired
    private ObjectMapper om;

    @Autowired
    private PaymentPlanRepository paymentPlanRepository;

    @Autowired
    private UserRepository userRepository;

    @Mock
    private PaymentPlanRepository paymentPlanRepositoryMock;

    @Autowired
    private PaymentPlanMapper paymentPlanMapper;

    @Mock
    private PaymentPlanService paymentPlanServiceMock;

    @Autowired
    private EntityManager em;

    @Autowired
    private MockMvc restPaymentPlanMockMvc;

    private PaymentPlan paymentPlan;

    private PaymentPlan insertedPaymentPlan;

    /**
     * Create an entity for this test.
     *
     * This is a static method, as tests for other entities might also need it,
     * if they test an entity which requires the current entity.
     */
    public static PaymentPlan createEntity(EntityManager em) {
        PaymentPlan paymentPlan = new PaymentPlan()
            .totalOwed(DEFAULT_TOTAL_OWED)
            .agreedAt(DEFAULT_AGREED_AT)
            .guarantorName(DEFAULT_GUARANTOR_NAME)
            .guarantorRelationship(DEFAULT_GUARANTOR_RELATIONSHIP)
            .guarantorPhone(DEFAULT_GUARANTOR_PHONE)
            .notes(DEFAULT_NOTES)
            .status(DEFAULT_STATUS);
        // Add required entity
        Bill bill;
        if (TestUtil.findAll(em, Bill.class).isEmpty()) {
            bill = BillResourceIT.createEntity(em);
            em.persist(bill);
            em.flush();
        } else {
            bill = TestUtil.findAll(em, Bill.class).get(0);
        }
        paymentPlan.setBill(bill);
        // Add required entity
        User user = UserResourceIT.createEntity();
        em.persist(user);
        em.flush();
        paymentPlan.setAgreedBy(user);
        return paymentPlan;
    }

    /**
     * Create an updated entity for this test.
     *
     * This is a static method, as tests for other entities might also need it,
     * if they test an entity which requires the current entity.
     */
    public static PaymentPlan createUpdatedEntity(EntityManager em) {
        PaymentPlan updatedPaymentPlan = new PaymentPlan()
            .totalOwed(UPDATED_TOTAL_OWED)
            .agreedAt(UPDATED_AGREED_AT)
            .guarantorName(UPDATED_GUARANTOR_NAME)
            .guarantorRelationship(UPDATED_GUARANTOR_RELATIONSHIP)
            .guarantorPhone(UPDATED_GUARANTOR_PHONE)
            .notes(UPDATED_NOTES)
            .status(UPDATED_STATUS);
        // Add required entity
        Bill bill;
        if (TestUtil.findAll(em, Bill.class).isEmpty()) {
            bill = BillResourceIT.createUpdatedEntity(em);
            em.persist(bill);
            em.flush();
        } else {
            bill = TestUtil.findAll(em, Bill.class).get(0);
        }
        updatedPaymentPlan.setBill(bill);
        // Add required entity
        User user = UserResourceIT.createEntity();
        em.persist(user);
        em.flush();
        updatedPaymentPlan.setAgreedBy(user);
        return updatedPaymentPlan;
    }

    @BeforeEach
    void initTest() {
        paymentPlan = createEntity(em);
    }

    @AfterEach
    void cleanup() {
        if (insertedPaymentPlan != null) {
            paymentPlanRepository.delete(insertedPaymentPlan);
            insertedPaymentPlan = null;
        }
    }

    @Test
    @Transactional
    void createPaymentPlan() throws Exception {
        long databaseSizeBeforeCreate = getRepositoryCount();
        // Create the PaymentPlan
        PaymentPlanDTO paymentPlanDTO = paymentPlanMapper.toDto(paymentPlan);
        var returnedPaymentPlanDTO = om.readValue(
            restPaymentPlanMockMvc
                .perform(post(ENTITY_API_URL).contentType(MediaType.APPLICATION_JSON).content(om.writeValueAsBytes(paymentPlanDTO)))
                .andExpect(status().isCreated())
                .andReturn()
                .getResponse()
                .getContentAsString(),
            PaymentPlanDTO.class
        );

        // Validate the PaymentPlan in the database
        assertIncrementedRepositoryCount(databaseSizeBeforeCreate);
        var returnedPaymentPlan = paymentPlanMapper.toEntity(returnedPaymentPlanDTO);
        assertPaymentPlanUpdatableFieldsEquals(returnedPaymentPlan, getPersistedPaymentPlan(returnedPaymentPlan));

        insertedPaymentPlan = returnedPaymentPlan;
    }

    @Test
    @Transactional
    void createPaymentPlanWithExistingId() throws Exception {
        // Create the PaymentPlan with an existing ID
        paymentPlan.setId(1L);
        PaymentPlanDTO paymentPlanDTO = paymentPlanMapper.toDto(paymentPlan);

        long databaseSizeBeforeCreate = getRepositoryCount();

        // An entity with an existing ID cannot be created, so this API call must fail
        restPaymentPlanMockMvc
            .perform(post(ENTITY_API_URL).contentType(MediaType.APPLICATION_JSON).content(om.writeValueAsBytes(paymentPlanDTO)))
            .andExpect(status().isBadRequest());

        // Validate the PaymentPlan in the database
        assertSameRepositoryCount(databaseSizeBeforeCreate);
    }

    @Test
    @Transactional
    void checkTotalOwedIsRequired() throws Exception {
        long databaseSizeBeforeTest = getRepositoryCount();
        // set the field null
        paymentPlan.setTotalOwed(null);

        // Create the PaymentPlan, which fails.
        PaymentPlanDTO paymentPlanDTO = paymentPlanMapper.toDto(paymentPlan);

        restPaymentPlanMockMvc
            .perform(post(ENTITY_API_URL).contentType(MediaType.APPLICATION_JSON).content(om.writeValueAsBytes(paymentPlanDTO)))
            .andExpect(status().isBadRequest());

        assertSameRepositoryCount(databaseSizeBeforeTest);
    }

    @Test
    @Transactional
    void checkAgreedAtIsRequired() throws Exception {
        long databaseSizeBeforeTest = getRepositoryCount();
        // set the field null
        paymentPlan.setAgreedAt(null);

        // Create the PaymentPlan, which fails.
        PaymentPlanDTO paymentPlanDTO = paymentPlanMapper.toDto(paymentPlan);

        restPaymentPlanMockMvc
            .perform(post(ENTITY_API_URL).contentType(MediaType.APPLICATION_JSON).content(om.writeValueAsBytes(paymentPlanDTO)))
            .andExpect(status().isBadRequest());

        assertSameRepositoryCount(databaseSizeBeforeTest);
    }

    @Test
    @Transactional
    void checkGuarantorNameIsRequired() throws Exception {
        long databaseSizeBeforeTest = getRepositoryCount();
        // set the field null
        paymentPlan.setGuarantorName(null);

        // Create the PaymentPlan, which fails.
        PaymentPlanDTO paymentPlanDTO = paymentPlanMapper.toDto(paymentPlan);

        restPaymentPlanMockMvc
            .perform(post(ENTITY_API_URL).contentType(MediaType.APPLICATION_JSON).content(om.writeValueAsBytes(paymentPlanDTO)))
            .andExpect(status().isBadRequest());

        assertSameRepositoryCount(databaseSizeBeforeTest);
    }

    @Test
    @Transactional
    void checkGuarantorPhoneIsRequired() throws Exception {
        long databaseSizeBeforeTest = getRepositoryCount();
        // set the field null
        paymentPlan.setGuarantorPhone(null);

        // Create the PaymentPlan, which fails.
        PaymentPlanDTO paymentPlanDTO = paymentPlanMapper.toDto(paymentPlan);

        restPaymentPlanMockMvc
            .perform(post(ENTITY_API_URL).contentType(MediaType.APPLICATION_JSON).content(om.writeValueAsBytes(paymentPlanDTO)))
            .andExpect(status().isBadRequest());

        assertSameRepositoryCount(databaseSizeBeforeTest);
    }

    @Test
    @Transactional
    void checkStatusIsRequired() throws Exception {
        long databaseSizeBeforeTest = getRepositoryCount();
        // set the field null
        paymentPlan.setStatus(null);

        // Create the PaymentPlan, which fails.
        PaymentPlanDTO paymentPlanDTO = paymentPlanMapper.toDto(paymentPlan);

        restPaymentPlanMockMvc
            .perform(post(ENTITY_API_URL).contentType(MediaType.APPLICATION_JSON).content(om.writeValueAsBytes(paymentPlanDTO)))
            .andExpect(status().isBadRequest());

        assertSameRepositoryCount(databaseSizeBeforeTest);
    }

    @Test
    @Transactional
    void getAllPaymentPlans() throws Exception {
        // Initialize the database
        insertedPaymentPlan = paymentPlanRepository.saveAndFlush(paymentPlan);

        // Get all the paymentPlanList
        restPaymentPlanMockMvc
            .perform(get(ENTITY_API_URL + "?sort=id,desc"))
            .andExpect(status().isOk())
            .andExpect(content().contentType(MediaType.APPLICATION_JSON_VALUE))
            .andExpect(jsonPath("$.[*].id").value(hasItem(paymentPlan.getId().intValue())))
            .andExpect(jsonPath("$.[*].totalOwed").value(hasItem(sameNumber(DEFAULT_TOTAL_OWED))))
            .andExpect(jsonPath("$.[*].agreedAt").value(hasItem(DEFAULT_AGREED_AT.toString())))
            .andExpect(jsonPath("$.[*].guarantorName").value(hasItem(DEFAULT_GUARANTOR_NAME)))
            .andExpect(jsonPath("$.[*].guarantorRelationship").value(hasItem(DEFAULT_GUARANTOR_RELATIONSHIP)))
            .andExpect(jsonPath("$.[*].guarantorPhone").value(hasItem(DEFAULT_GUARANTOR_PHONE)))
            .andExpect(jsonPath("$.[*].notes").value(hasItem(DEFAULT_NOTES)))
            .andExpect(jsonPath("$.[*].status").value(hasItem(DEFAULT_STATUS.toString())));
    }

    @SuppressWarnings({ "unchecked" })
    void getAllPaymentPlansWithEagerRelationshipsIsEnabled() throws Exception {
        when(paymentPlanServiceMock.findAllWithEagerRelationships(any())).thenReturn(new PageImpl(new ArrayList<>()));

        restPaymentPlanMockMvc.perform(get(ENTITY_API_URL + "?eagerload=true")).andExpect(status().isOk());

        verify(paymentPlanServiceMock, times(1)).findAllWithEagerRelationships(any());
    }

    @SuppressWarnings({ "unchecked" })
    void getAllPaymentPlansWithEagerRelationshipsIsNotEnabled() throws Exception {
        when(paymentPlanServiceMock.findAllWithEagerRelationships(any())).thenReturn(new PageImpl(new ArrayList<>()));

        restPaymentPlanMockMvc.perform(get(ENTITY_API_URL + "?eagerload=false")).andExpect(status().isOk());
        verify(paymentPlanRepositoryMock, times(1)).findAll(any(Pageable.class));
    }

    @Test
    @Transactional
    void getPaymentPlan() throws Exception {
        // Initialize the database
        insertedPaymentPlan = paymentPlanRepository.saveAndFlush(paymentPlan);

        // Get the paymentPlan
        restPaymentPlanMockMvc
            .perform(get(ENTITY_API_URL_ID, paymentPlan.getId()))
            .andExpect(status().isOk())
            .andExpect(content().contentType(MediaType.APPLICATION_JSON_VALUE))
            .andExpect(jsonPath("$.id").value(paymentPlan.getId().intValue()))
            .andExpect(jsonPath("$.totalOwed").value(sameNumber(DEFAULT_TOTAL_OWED)))
            .andExpect(jsonPath("$.agreedAt").value(DEFAULT_AGREED_AT.toString()))
            .andExpect(jsonPath("$.guarantorName").value(DEFAULT_GUARANTOR_NAME))
            .andExpect(jsonPath("$.guarantorRelationship").value(DEFAULT_GUARANTOR_RELATIONSHIP))
            .andExpect(jsonPath("$.guarantorPhone").value(DEFAULT_GUARANTOR_PHONE))
            .andExpect(jsonPath("$.notes").value(DEFAULT_NOTES))
            .andExpect(jsonPath("$.status").value(DEFAULT_STATUS.toString()));
    }

    @Test
    @Transactional
    void getNonExistingPaymentPlan() throws Exception {
        // Get the paymentPlan
        restPaymentPlanMockMvc.perform(get(ENTITY_API_URL_ID, Long.MAX_VALUE)).andExpect(status().isNotFound());
    }

    @Test
    @Transactional
    void putExistingPaymentPlan() throws Exception {
        // Initialize the database
        insertedPaymentPlan = paymentPlanRepository.saveAndFlush(paymentPlan);

        long databaseSizeBeforeUpdate = getRepositoryCount();

        // Update the paymentPlan
        PaymentPlan updatedPaymentPlan = paymentPlanRepository.findById(paymentPlan.getId()).orElseThrow();
        // Disconnect from session so that the updates on updatedPaymentPlan are not directly saved in db
        em.detach(updatedPaymentPlan);
        updatedPaymentPlan
            .totalOwed(UPDATED_TOTAL_OWED)
            .agreedAt(UPDATED_AGREED_AT)
            .guarantorName(UPDATED_GUARANTOR_NAME)
            .guarantorRelationship(UPDATED_GUARANTOR_RELATIONSHIP)
            .guarantorPhone(UPDATED_GUARANTOR_PHONE)
            .notes(UPDATED_NOTES)
            .status(UPDATED_STATUS);
        PaymentPlanDTO paymentPlanDTO = paymentPlanMapper.toDto(updatedPaymentPlan);

        restPaymentPlanMockMvc
            .perform(
                put(ENTITY_API_URL_ID, paymentPlanDTO.getId())
                    .contentType(MediaType.APPLICATION_JSON)
                    .content(om.writeValueAsBytes(paymentPlanDTO))
            )
            .andExpect(status().isOk());

        // Validate the PaymentPlan in the database
        assertSameRepositoryCount(databaseSizeBeforeUpdate);
        assertPersistedPaymentPlanToMatchAllProperties(updatedPaymentPlan);
    }

    @Test
    @Transactional
    void putNonExistingPaymentPlan() throws Exception {
        long databaseSizeBeforeUpdate = getRepositoryCount();
        paymentPlan.setId(longCount.incrementAndGet());

        // Create the PaymentPlan
        PaymentPlanDTO paymentPlanDTO = paymentPlanMapper.toDto(paymentPlan);

        // If the entity doesn't have an ID, it will throw BadRequestAlertException
        restPaymentPlanMockMvc
            .perform(
                put(ENTITY_API_URL_ID, paymentPlanDTO.getId())
                    .contentType(MediaType.APPLICATION_JSON)
                    .content(om.writeValueAsBytes(paymentPlanDTO))
            )
            .andExpect(status().isBadRequest());

        // Validate the PaymentPlan in the database
        assertSameRepositoryCount(databaseSizeBeforeUpdate);
    }

    @Test
    @Transactional
    void putWithIdMismatchPaymentPlan() throws Exception {
        long databaseSizeBeforeUpdate = getRepositoryCount();
        paymentPlan.setId(longCount.incrementAndGet());

        // Create the PaymentPlan
        PaymentPlanDTO paymentPlanDTO = paymentPlanMapper.toDto(paymentPlan);

        // If url ID doesn't match entity ID, it will throw BadRequestAlertException
        restPaymentPlanMockMvc
            .perform(
                put(ENTITY_API_URL_ID, longCount.incrementAndGet())
                    .contentType(MediaType.APPLICATION_JSON)
                    .content(om.writeValueAsBytes(paymentPlanDTO))
            )
            .andExpect(status().isBadRequest());

        // Validate the PaymentPlan in the database
        assertSameRepositoryCount(databaseSizeBeforeUpdate);
    }

    @Test
    @Transactional
    void putWithMissingIdPathParamPaymentPlan() throws Exception {
        long databaseSizeBeforeUpdate = getRepositoryCount();
        paymentPlan.setId(longCount.incrementAndGet());

        // Create the PaymentPlan
        PaymentPlanDTO paymentPlanDTO = paymentPlanMapper.toDto(paymentPlan);

        // If url ID doesn't match entity ID, it will throw BadRequestAlertException
        restPaymentPlanMockMvc
            .perform(put(ENTITY_API_URL).contentType(MediaType.APPLICATION_JSON).content(om.writeValueAsBytes(paymentPlanDTO)))
            .andExpect(status().isMethodNotAllowed());

        // Validate the PaymentPlan in the database
        assertSameRepositoryCount(databaseSizeBeforeUpdate);
    }

    @Test
    @Transactional
    void partialUpdatePaymentPlanWithPatch() throws Exception {
        // Initialize the database
        insertedPaymentPlan = paymentPlanRepository.saveAndFlush(paymentPlan);

        long databaseSizeBeforeUpdate = getRepositoryCount();

        // Update the paymentPlan using partial update
        PaymentPlan partialUpdatedPaymentPlan = new PaymentPlan();
        partialUpdatedPaymentPlan.setId(paymentPlan.getId());

        partialUpdatedPaymentPlan
            .totalOwed(UPDATED_TOTAL_OWED)
            .agreedAt(UPDATED_AGREED_AT)
            .guarantorName(UPDATED_GUARANTOR_NAME)
            .guarantorRelationship(UPDATED_GUARANTOR_RELATIONSHIP)
            .guarantorPhone(UPDATED_GUARANTOR_PHONE);

        restPaymentPlanMockMvc
            .perform(
                patch(ENTITY_API_URL_ID, partialUpdatedPaymentPlan.getId())
                    .contentType("application/merge-patch+json")
                    .content(om.writeValueAsBytes(partialUpdatedPaymentPlan))
            )
            .andExpect(status().isOk());

        // Validate the PaymentPlan in the database

        assertSameRepositoryCount(databaseSizeBeforeUpdate);
        assertPaymentPlanUpdatableFieldsEquals(
            createUpdateProxyForBean(partialUpdatedPaymentPlan, paymentPlan),
            getPersistedPaymentPlan(paymentPlan)
        );
    }

    @Test
    @Transactional
    void fullUpdatePaymentPlanWithPatch() throws Exception {
        // Initialize the database
        insertedPaymentPlan = paymentPlanRepository.saveAndFlush(paymentPlan);

        long databaseSizeBeforeUpdate = getRepositoryCount();

        // Update the paymentPlan using partial update
        PaymentPlan partialUpdatedPaymentPlan = new PaymentPlan();
        partialUpdatedPaymentPlan.setId(paymentPlan.getId());

        partialUpdatedPaymentPlan
            .totalOwed(UPDATED_TOTAL_OWED)
            .agreedAt(UPDATED_AGREED_AT)
            .guarantorName(UPDATED_GUARANTOR_NAME)
            .guarantorRelationship(UPDATED_GUARANTOR_RELATIONSHIP)
            .guarantorPhone(UPDATED_GUARANTOR_PHONE)
            .notes(UPDATED_NOTES)
            .status(UPDATED_STATUS);

        restPaymentPlanMockMvc
            .perform(
                patch(ENTITY_API_URL_ID, partialUpdatedPaymentPlan.getId())
                    .contentType("application/merge-patch+json")
                    .content(om.writeValueAsBytes(partialUpdatedPaymentPlan))
            )
            .andExpect(status().isOk());

        // Validate the PaymentPlan in the database

        assertSameRepositoryCount(databaseSizeBeforeUpdate);
        assertPaymentPlanUpdatableFieldsEquals(partialUpdatedPaymentPlan, getPersistedPaymentPlan(partialUpdatedPaymentPlan));
    }

    @Test
    @Transactional
    void patchNonExistingPaymentPlan() throws Exception {
        long databaseSizeBeforeUpdate = getRepositoryCount();
        paymentPlan.setId(longCount.incrementAndGet());

        // Create the PaymentPlan
        PaymentPlanDTO paymentPlanDTO = paymentPlanMapper.toDto(paymentPlan);

        // If the entity doesn't have an ID, it will throw BadRequestAlertException
        restPaymentPlanMockMvc
            .perform(
                patch(ENTITY_API_URL_ID, paymentPlanDTO.getId())
                    .contentType("application/merge-patch+json")
                    .content(om.writeValueAsBytes(paymentPlanDTO))
            )
            .andExpect(status().isBadRequest());

        // Validate the PaymentPlan in the database
        assertSameRepositoryCount(databaseSizeBeforeUpdate);
    }

    @Test
    @Transactional
    void patchWithIdMismatchPaymentPlan() throws Exception {
        long databaseSizeBeforeUpdate = getRepositoryCount();
        paymentPlan.setId(longCount.incrementAndGet());

        // Create the PaymentPlan
        PaymentPlanDTO paymentPlanDTO = paymentPlanMapper.toDto(paymentPlan);

        // If url ID doesn't match entity ID, it will throw BadRequestAlertException
        restPaymentPlanMockMvc
            .perform(
                patch(ENTITY_API_URL_ID, longCount.incrementAndGet())
                    .contentType("application/merge-patch+json")
                    .content(om.writeValueAsBytes(paymentPlanDTO))
            )
            .andExpect(status().isBadRequest());

        // Validate the PaymentPlan in the database
        assertSameRepositoryCount(databaseSizeBeforeUpdate);
    }

    @Test
    @Transactional
    void patchWithMissingIdPathParamPaymentPlan() throws Exception {
        long databaseSizeBeforeUpdate = getRepositoryCount();
        paymentPlan.setId(longCount.incrementAndGet());

        // Create the PaymentPlan
        PaymentPlanDTO paymentPlanDTO = paymentPlanMapper.toDto(paymentPlan);

        // If url ID doesn't match entity ID, it will throw BadRequestAlertException
        restPaymentPlanMockMvc
            .perform(patch(ENTITY_API_URL).contentType("application/merge-patch+json").content(om.writeValueAsBytes(paymentPlanDTO)))
            .andExpect(status().isMethodNotAllowed());

        // Validate the PaymentPlan in the database
        assertSameRepositoryCount(databaseSizeBeforeUpdate);
    }

    @Test
    @Transactional
    void deletePaymentPlan() throws Exception {
        // Initialize the database
        insertedPaymentPlan = paymentPlanRepository.saveAndFlush(paymentPlan);

        long databaseSizeBeforeDelete = getRepositoryCount();

        // Delete the paymentPlan
        restPaymentPlanMockMvc
            .perform(delete(ENTITY_API_URL_ID, paymentPlan.getId()).accept(MediaType.APPLICATION_JSON))
            .andExpect(status().isNoContent());

        // Validate the database contains one less item
        assertDecrementedRepositoryCount(databaseSizeBeforeDelete);
    }

    protected long getRepositoryCount() {
        return paymentPlanRepository.count();
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

    protected PaymentPlan getPersistedPaymentPlan(PaymentPlan paymentPlan) {
        return paymentPlanRepository.findById(paymentPlan.getId()).orElseThrow();
    }

    protected void assertPersistedPaymentPlanToMatchAllProperties(PaymentPlan expectedPaymentPlan) {
        assertPaymentPlanAllPropertiesEquals(expectedPaymentPlan, getPersistedPaymentPlan(expectedPaymentPlan));
    }

    protected void assertPersistedPaymentPlanToMatchUpdatableProperties(PaymentPlan expectedPaymentPlan) {
        assertPaymentPlanAllUpdatablePropertiesEquals(expectedPaymentPlan, getPersistedPaymentPlan(expectedPaymentPlan));
    }
}
