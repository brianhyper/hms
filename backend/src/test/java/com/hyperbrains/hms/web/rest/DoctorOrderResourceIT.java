package com.hyperbrains.hms.web.rest;

import static com.hyperbrains.hms.domain.DoctorOrderAsserts.*;
import static com.hyperbrains.hms.web.rest.TestUtil.createUpdateProxyForBean;
import static org.assertj.core.api.Assertions.assertThat;
import static org.hamcrest.Matchers.hasItem;
import static org.mockito.Mockito.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.hyperbrains.hms.IntegrationTest;
import com.hyperbrains.hms.domain.Admission;
import com.hyperbrains.hms.domain.DoctorOrder;
import com.hyperbrains.hms.domain.User;
import com.hyperbrains.hms.domain.enumeration.DoctorOrderRecurrence;
import com.hyperbrains.hms.domain.enumeration.DoctorOrderStatus;
import com.hyperbrains.hms.domain.enumeration.DoctorOrderType;
import com.hyperbrains.hms.repository.DoctorOrderRepository;
import com.hyperbrains.hms.repository.UserRepository;
import com.hyperbrains.hms.service.DoctorOrderService;
import com.hyperbrains.hms.service.dto.DoctorOrderDTO;
import com.hyperbrains.hms.service.mapper.DoctorOrderMapper;
import jakarta.persistence.EntityManager;
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
 * Integration tests for the {@link DoctorOrderResource} REST controller.
 */
@IntegrationTest
@ExtendWith(MockitoExtension.class)
@AutoConfigureMockMvc
@WithMockUser
class DoctorOrderResourceIT {

    private static final Instant DEFAULT_ORDERED_AT = Instant.ofEpochMilli(0L);
    private static final Instant UPDATED_ORDERED_AT = Instant.ofEpochMilli(1790236635213L);

    private static final DoctorOrderType DEFAULT_TYPE = DoctorOrderType.LAB;
    private static final DoctorOrderType UPDATED_TYPE = DoctorOrderType.RADIOLOGY;

    private static final DoctorOrderRecurrence DEFAULT_RECURRENCE = DoctorOrderRecurrence.ONE_OFF;
    private static final DoctorOrderRecurrence UPDATED_RECURRENCE = DoctorOrderRecurrence.RECURRING;

    private static final String DEFAULT_FREQUENCY = "AAAAAAAAAA";
    private static final String UPDATED_FREQUENCY = "BBBBBBBBBB";

    private static final Instant DEFAULT_END_DATE = Instant.ofEpochMilli(0L);
    private static final Instant UPDATED_END_DATE = Instant.ofEpochMilli(1790236635213L);

    private static final String DEFAULT_DETAILS = "AAAAAAAAAA";
    private static final String UPDATED_DETAILS = "BBBBBBBBBB";

    private static final DoctorOrderStatus DEFAULT_STATUS = DoctorOrderStatus.ACTIVE;
    private static final DoctorOrderStatus UPDATED_STATUS = DoctorOrderStatus.COMPLETED;

    private static final Instant DEFAULT_CANCELLED_AT = Instant.ofEpochMilli(0L);
    private static final Instant UPDATED_CANCELLED_AT = Instant.ofEpochMilli(1790236635213L);

    private static final String DEFAULT_CANCEL_REASON = "AAAAAAAAAA";
    private static final String UPDATED_CANCEL_REASON = "BBBBBBBBBB";

    private static final String ENTITY_API_URL = "/api/doctor-orders";
    private static final String ENTITY_API_URL_ID = ENTITY_API_URL + "/{id}";

    private static final Random random = new Random();
    private static final AtomicLong longCount = new AtomicLong(random.nextInt() + 2L * Integer.MAX_VALUE);

    @Autowired
    private ObjectMapper om;

    @Autowired
    private DoctorOrderRepository doctorOrderRepository;

    @Autowired
    private UserRepository userRepository;

    @Mock
    private DoctorOrderRepository doctorOrderRepositoryMock;

    @Autowired
    private DoctorOrderMapper doctorOrderMapper;

    @Mock
    private DoctorOrderService doctorOrderServiceMock;

    @Autowired
    private EntityManager em;

    @Autowired
    private MockMvc restDoctorOrderMockMvc;

    private DoctorOrder doctorOrder;

    private DoctorOrder insertedDoctorOrder;

    /**
     * Create an entity for this test.
     *
     * This is a static method, as tests for other entities might also need it,
     * if they test an entity which requires the current entity.
     */
    public static DoctorOrder createEntity(EntityManager em) {
        DoctorOrder doctorOrder = new DoctorOrder()
            .orderedAt(DEFAULT_ORDERED_AT)
            .type(DEFAULT_TYPE)
            .recurrence(DEFAULT_RECURRENCE)
            .frequency(DEFAULT_FREQUENCY)
            .endDate(DEFAULT_END_DATE)
            .details(DEFAULT_DETAILS)
            .status(DEFAULT_STATUS)
            .cancelledAt(DEFAULT_CANCELLED_AT)
            .cancelReason(DEFAULT_CANCEL_REASON);
        // Add required entity
        Admission admission;
        if (TestUtil.findAll(em, Admission.class).isEmpty()) {
            admission = AdmissionResourceIT.createEntity(em);
            em.persist(admission);
            em.flush();
        } else {
            admission = TestUtil.findAll(em, Admission.class).get(0);
        }
        doctorOrder.setAdmission(admission);
        // Add required entity
        User user = UserResourceIT.createEntity();
        em.persist(user);
        em.flush();
        doctorOrder.setOrderedBy(user);
        return doctorOrder;
    }

    /**
     * Create an updated entity for this test.
     *
     * This is a static method, as tests for other entities might also need it,
     * if they test an entity which requires the current entity.
     */
    public static DoctorOrder createUpdatedEntity(EntityManager em) {
        DoctorOrder updatedDoctorOrder = new DoctorOrder()
            .orderedAt(UPDATED_ORDERED_AT)
            .type(UPDATED_TYPE)
            .recurrence(UPDATED_RECURRENCE)
            .frequency(UPDATED_FREQUENCY)
            .endDate(UPDATED_END_DATE)
            .details(UPDATED_DETAILS)
            .status(UPDATED_STATUS)
            .cancelledAt(UPDATED_CANCELLED_AT)
            .cancelReason(UPDATED_CANCEL_REASON);
        // Add required entity
        Admission admission;
        if (TestUtil.findAll(em, Admission.class).isEmpty()) {
            admission = AdmissionResourceIT.createUpdatedEntity(em);
            em.persist(admission);
            em.flush();
        } else {
            admission = TestUtil.findAll(em, Admission.class).get(0);
        }
        updatedDoctorOrder.setAdmission(admission);
        // Add required entity
        User user = UserResourceIT.createEntity();
        em.persist(user);
        em.flush();
        updatedDoctorOrder.setOrderedBy(user);
        return updatedDoctorOrder;
    }

    @BeforeEach
    void initTest() {
        doctorOrder = createEntity(em);
    }

    @AfterEach
    void cleanup() {
        if (insertedDoctorOrder != null) {
            doctorOrderRepository.delete(insertedDoctorOrder);
            insertedDoctorOrder = null;
        }
    }

    @Test
    @Transactional
    void createDoctorOrder() throws Exception {
        long databaseSizeBeforeCreate = getRepositoryCount();
        // Create the DoctorOrder
        DoctorOrderDTO doctorOrderDTO = doctorOrderMapper.toDto(doctorOrder);
        var returnedDoctorOrderDTO = om.readValue(
            restDoctorOrderMockMvc
                .perform(post(ENTITY_API_URL).contentType(MediaType.APPLICATION_JSON).content(om.writeValueAsBytes(doctorOrderDTO)))
                .andExpect(status().isCreated())
                .andReturn()
                .getResponse()
                .getContentAsString(),
            DoctorOrderDTO.class
        );

        // Validate the DoctorOrder in the database
        assertIncrementedRepositoryCount(databaseSizeBeforeCreate);
        var returnedDoctorOrder = doctorOrderMapper.toEntity(returnedDoctorOrderDTO);
        assertDoctorOrderUpdatableFieldsEquals(returnedDoctorOrder, getPersistedDoctorOrder(returnedDoctorOrder));

        insertedDoctorOrder = returnedDoctorOrder;
    }

    @Test
    @Transactional
    void createDoctorOrderWithExistingId() throws Exception {
        // Create the DoctorOrder with an existing ID
        doctorOrder.setId(1L);
        DoctorOrderDTO doctorOrderDTO = doctorOrderMapper.toDto(doctorOrder);

        long databaseSizeBeforeCreate = getRepositoryCount();

        // An entity with an existing ID cannot be created, so this API call must fail
        restDoctorOrderMockMvc
            .perform(post(ENTITY_API_URL).contentType(MediaType.APPLICATION_JSON).content(om.writeValueAsBytes(doctorOrderDTO)))
            .andExpect(status().isBadRequest());

        // Validate the DoctorOrder in the database
        assertSameRepositoryCount(databaseSizeBeforeCreate);
    }

    @Test
    @Transactional
    void checkOrderedAtIsRequired() throws Exception {
        long databaseSizeBeforeTest = getRepositoryCount();
        // set the field null
        doctorOrder.setOrderedAt(null);

        // Create the DoctorOrder, which fails.
        DoctorOrderDTO doctorOrderDTO = doctorOrderMapper.toDto(doctorOrder);

        restDoctorOrderMockMvc
            .perform(post(ENTITY_API_URL).contentType(MediaType.APPLICATION_JSON).content(om.writeValueAsBytes(doctorOrderDTO)))
            .andExpect(status().isBadRequest());

        assertSameRepositoryCount(databaseSizeBeforeTest);
    }

    @Test
    @Transactional
    void checkTypeIsRequired() throws Exception {
        long databaseSizeBeforeTest = getRepositoryCount();
        // set the field null
        doctorOrder.setType(null);

        // Create the DoctorOrder, which fails.
        DoctorOrderDTO doctorOrderDTO = doctorOrderMapper.toDto(doctorOrder);

        restDoctorOrderMockMvc
            .perform(post(ENTITY_API_URL).contentType(MediaType.APPLICATION_JSON).content(om.writeValueAsBytes(doctorOrderDTO)))
            .andExpect(status().isBadRequest());

        assertSameRepositoryCount(databaseSizeBeforeTest);
    }

    @Test
    @Transactional
    void checkRecurrenceIsRequired() throws Exception {
        long databaseSizeBeforeTest = getRepositoryCount();
        // set the field null
        doctorOrder.setRecurrence(null);

        // Create the DoctorOrder, which fails.
        DoctorOrderDTO doctorOrderDTO = doctorOrderMapper.toDto(doctorOrder);

        restDoctorOrderMockMvc
            .perform(post(ENTITY_API_URL).contentType(MediaType.APPLICATION_JSON).content(om.writeValueAsBytes(doctorOrderDTO)))
            .andExpect(status().isBadRequest());

        assertSameRepositoryCount(databaseSizeBeforeTest);
    }

    @Test
    @Transactional
    void checkDetailsIsRequired() throws Exception {
        long databaseSizeBeforeTest = getRepositoryCount();
        // set the field null
        doctorOrder.setDetails(null);

        // Create the DoctorOrder, which fails.
        DoctorOrderDTO doctorOrderDTO = doctorOrderMapper.toDto(doctorOrder);

        restDoctorOrderMockMvc
            .perform(post(ENTITY_API_URL).contentType(MediaType.APPLICATION_JSON).content(om.writeValueAsBytes(doctorOrderDTO)))
            .andExpect(status().isBadRequest());

        assertSameRepositoryCount(databaseSizeBeforeTest);
    }

    @Test
    @Transactional
    void checkStatusIsRequired() throws Exception {
        long databaseSizeBeforeTest = getRepositoryCount();
        // set the field null
        doctorOrder.setStatus(null);

        // Create the DoctorOrder, which fails.
        DoctorOrderDTO doctorOrderDTO = doctorOrderMapper.toDto(doctorOrder);

        restDoctorOrderMockMvc
            .perform(post(ENTITY_API_URL).contentType(MediaType.APPLICATION_JSON).content(om.writeValueAsBytes(doctorOrderDTO)))
            .andExpect(status().isBadRequest());

        assertSameRepositoryCount(databaseSizeBeforeTest);
    }

    @Test
    @Transactional
    void getAllDoctorOrders() throws Exception {
        // Initialize the database
        insertedDoctorOrder = doctorOrderRepository.saveAndFlush(doctorOrder);

        // Get all the doctorOrderList
        restDoctorOrderMockMvc
            .perform(get(ENTITY_API_URL + "?sort=id,desc"))
            .andExpect(status().isOk())
            .andExpect(content().contentType(MediaType.APPLICATION_JSON_VALUE))
            .andExpect(jsonPath("$.[*].id").value(hasItem(doctorOrder.getId().intValue())))
            .andExpect(jsonPath("$.[*].orderedAt").value(hasItem(DEFAULT_ORDERED_AT.toString())))
            .andExpect(jsonPath("$.[*].type").value(hasItem(DEFAULT_TYPE.toString())))
            .andExpect(jsonPath("$.[*].recurrence").value(hasItem(DEFAULT_RECURRENCE.toString())))
            .andExpect(jsonPath("$.[*].frequency").value(hasItem(DEFAULT_FREQUENCY)))
            .andExpect(jsonPath("$.[*].endDate").value(hasItem(DEFAULT_END_DATE.toString())))
            .andExpect(jsonPath("$.[*].details").value(hasItem(DEFAULT_DETAILS)))
            .andExpect(jsonPath("$.[*].status").value(hasItem(DEFAULT_STATUS.toString())))
            .andExpect(jsonPath("$.[*].cancelledAt").value(hasItem(DEFAULT_CANCELLED_AT.toString())))
            .andExpect(jsonPath("$.[*].cancelReason").value(hasItem(DEFAULT_CANCEL_REASON)));
    }

    @SuppressWarnings({ "unchecked" })
    void getAllDoctorOrdersWithEagerRelationshipsIsEnabled() throws Exception {
        when(doctorOrderServiceMock.findAllWithEagerRelationships(any())).thenReturn(new PageImpl(new ArrayList<>()));

        restDoctorOrderMockMvc.perform(get(ENTITY_API_URL + "?eagerload=true")).andExpect(status().isOk());

        verify(doctorOrderServiceMock, times(1)).findAllWithEagerRelationships(any());
    }

    @SuppressWarnings({ "unchecked" })
    void getAllDoctorOrdersWithEagerRelationshipsIsNotEnabled() throws Exception {
        when(doctorOrderServiceMock.findAllWithEagerRelationships(any())).thenReturn(new PageImpl(new ArrayList<>()));

        restDoctorOrderMockMvc.perform(get(ENTITY_API_URL + "?eagerload=false")).andExpect(status().isOk());
        verify(doctorOrderRepositoryMock, times(1)).findAll(any(Pageable.class));
    }

    @Test
    @Transactional
    void getDoctorOrder() throws Exception {
        // Initialize the database
        insertedDoctorOrder = doctorOrderRepository.saveAndFlush(doctorOrder);

        // Get the doctorOrder
        restDoctorOrderMockMvc
            .perform(get(ENTITY_API_URL_ID, doctorOrder.getId()))
            .andExpect(status().isOk())
            .andExpect(content().contentType(MediaType.APPLICATION_JSON_VALUE))
            .andExpect(jsonPath("$.id").value(doctorOrder.getId().intValue()))
            .andExpect(jsonPath("$.orderedAt").value(DEFAULT_ORDERED_AT.toString()))
            .andExpect(jsonPath("$.type").value(DEFAULT_TYPE.toString()))
            .andExpect(jsonPath("$.recurrence").value(DEFAULT_RECURRENCE.toString()))
            .andExpect(jsonPath("$.frequency").value(DEFAULT_FREQUENCY))
            .andExpect(jsonPath("$.endDate").value(DEFAULT_END_DATE.toString()))
            .andExpect(jsonPath("$.details").value(DEFAULT_DETAILS))
            .andExpect(jsonPath("$.status").value(DEFAULT_STATUS.toString()))
            .andExpect(jsonPath("$.cancelledAt").value(DEFAULT_CANCELLED_AT.toString()))
            .andExpect(jsonPath("$.cancelReason").value(DEFAULT_CANCEL_REASON));
    }

    @Test
    @Transactional
    void getNonExistingDoctorOrder() throws Exception {
        // Get the doctorOrder
        restDoctorOrderMockMvc.perform(get(ENTITY_API_URL_ID, Long.MAX_VALUE)).andExpect(status().isNotFound());
    }

    @Test
    @Transactional
    void putExistingDoctorOrder() throws Exception {
        // Initialize the database
        insertedDoctorOrder = doctorOrderRepository.saveAndFlush(doctorOrder);

        long databaseSizeBeforeUpdate = getRepositoryCount();

        // Update the doctorOrder
        DoctorOrder updatedDoctorOrder = doctorOrderRepository.findById(doctorOrder.getId()).orElseThrow();
        // Disconnect from session so that the updates on updatedDoctorOrder are not directly saved in db
        em.detach(updatedDoctorOrder);
        updatedDoctorOrder
            .orderedAt(UPDATED_ORDERED_AT)
            .type(UPDATED_TYPE)
            .recurrence(UPDATED_RECURRENCE)
            .frequency(UPDATED_FREQUENCY)
            .endDate(UPDATED_END_DATE)
            .details(UPDATED_DETAILS)
            .status(UPDATED_STATUS)
            .cancelledAt(UPDATED_CANCELLED_AT)
            .cancelReason(UPDATED_CANCEL_REASON);
        DoctorOrderDTO doctorOrderDTO = doctorOrderMapper.toDto(updatedDoctorOrder);

        restDoctorOrderMockMvc
            .perform(
                put(ENTITY_API_URL_ID, doctorOrderDTO.getId())
                    .contentType(MediaType.APPLICATION_JSON)
                    .content(om.writeValueAsBytes(doctorOrderDTO))
            )
            .andExpect(status().isOk());

        // Validate the DoctorOrder in the database
        assertSameRepositoryCount(databaseSizeBeforeUpdate);
        assertPersistedDoctorOrderToMatchAllProperties(updatedDoctorOrder);
    }

    @Test
    @Transactional
    void putNonExistingDoctorOrder() throws Exception {
        long databaseSizeBeforeUpdate = getRepositoryCount();
        doctorOrder.setId(longCount.incrementAndGet());

        // Create the DoctorOrder
        DoctorOrderDTO doctorOrderDTO = doctorOrderMapper.toDto(doctorOrder);

        // If the entity doesn't have an ID, it will throw BadRequestAlertException
        restDoctorOrderMockMvc
            .perform(
                put(ENTITY_API_URL_ID, doctorOrderDTO.getId())
                    .contentType(MediaType.APPLICATION_JSON)
                    .content(om.writeValueAsBytes(doctorOrderDTO))
            )
            .andExpect(status().isBadRequest());

        // Validate the DoctorOrder in the database
        assertSameRepositoryCount(databaseSizeBeforeUpdate);
    }

    @Test
    @Transactional
    void putWithIdMismatchDoctorOrder() throws Exception {
        long databaseSizeBeforeUpdate = getRepositoryCount();
        doctorOrder.setId(longCount.incrementAndGet());

        // Create the DoctorOrder
        DoctorOrderDTO doctorOrderDTO = doctorOrderMapper.toDto(doctorOrder);

        // If url ID doesn't match entity ID, it will throw BadRequestAlertException
        restDoctorOrderMockMvc
            .perform(
                put(ENTITY_API_URL_ID, longCount.incrementAndGet())
                    .contentType(MediaType.APPLICATION_JSON)
                    .content(om.writeValueAsBytes(doctorOrderDTO))
            )
            .andExpect(status().isBadRequest());

        // Validate the DoctorOrder in the database
        assertSameRepositoryCount(databaseSizeBeforeUpdate);
    }

    @Test
    @Transactional
    void putWithMissingIdPathParamDoctorOrder() throws Exception {
        long databaseSizeBeforeUpdate = getRepositoryCount();
        doctorOrder.setId(longCount.incrementAndGet());

        // Create the DoctorOrder
        DoctorOrderDTO doctorOrderDTO = doctorOrderMapper.toDto(doctorOrder);

        // If url ID doesn't match entity ID, it will throw BadRequestAlertException
        restDoctorOrderMockMvc
            .perform(put(ENTITY_API_URL).contentType(MediaType.APPLICATION_JSON).content(om.writeValueAsBytes(doctorOrderDTO)))
            .andExpect(status().isMethodNotAllowed());

        // Validate the DoctorOrder in the database
        assertSameRepositoryCount(databaseSizeBeforeUpdate);
    }

    @Test
    @Transactional
    void partialUpdateDoctorOrderWithPatch() throws Exception {
        // Initialize the database
        insertedDoctorOrder = doctorOrderRepository.saveAndFlush(doctorOrder);

        long databaseSizeBeforeUpdate = getRepositoryCount();

        // Update the doctorOrder using partial update
        DoctorOrder partialUpdatedDoctorOrder = new DoctorOrder();
        partialUpdatedDoctorOrder.setId(doctorOrder.getId());

        partialUpdatedDoctorOrder.orderedAt(UPDATED_ORDERED_AT).type(UPDATED_TYPE).status(UPDATED_STATUS).cancelledAt(UPDATED_CANCELLED_AT);

        restDoctorOrderMockMvc
            .perform(
                patch(ENTITY_API_URL_ID, partialUpdatedDoctorOrder.getId())
                    .contentType("application/merge-patch+json")
                    .content(om.writeValueAsBytes(partialUpdatedDoctorOrder))
            )
            .andExpect(status().isOk());

        // Validate the DoctorOrder in the database

        assertSameRepositoryCount(databaseSizeBeforeUpdate);
        assertDoctorOrderUpdatableFieldsEquals(
            createUpdateProxyForBean(partialUpdatedDoctorOrder, doctorOrder),
            getPersistedDoctorOrder(doctorOrder)
        );
    }

    @Test
    @Transactional
    void fullUpdateDoctorOrderWithPatch() throws Exception {
        // Initialize the database
        insertedDoctorOrder = doctorOrderRepository.saveAndFlush(doctorOrder);

        long databaseSizeBeforeUpdate = getRepositoryCount();

        // Update the doctorOrder using partial update
        DoctorOrder partialUpdatedDoctorOrder = new DoctorOrder();
        partialUpdatedDoctorOrder.setId(doctorOrder.getId());

        partialUpdatedDoctorOrder
            .orderedAt(UPDATED_ORDERED_AT)
            .type(UPDATED_TYPE)
            .recurrence(UPDATED_RECURRENCE)
            .frequency(UPDATED_FREQUENCY)
            .endDate(UPDATED_END_DATE)
            .details(UPDATED_DETAILS)
            .status(UPDATED_STATUS)
            .cancelledAt(UPDATED_CANCELLED_AT)
            .cancelReason(UPDATED_CANCEL_REASON);

        restDoctorOrderMockMvc
            .perform(
                patch(ENTITY_API_URL_ID, partialUpdatedDoctorOrder.getId())
                    .contentType("application/merge-patch+json")
                    .content(om.writeValueAsBytes(partialUpdatedDoctorOrder))
            )
            .andExpect(status().isOk());

        // Validate the DoctorOrder in the database

        assertSameRepositoryCount(databaseSizeBeforeUpdate);
        assertDoctorOrderUpdatableFieldsEquals(partialUpdatedDoctorOrder, getPersistedDoctorOrder(partialUpdatedDoctorOrder));
    }

    @Test
    @Transactional
    void patchNonExistingDoctorOrder() throws Exception {
        long databaseSizeBeforeUpdate = getRepositoryCount();
        doctorOrder.setId(longCount.incrementAndGet());

        // Create the DoctorOrder
        DoctorOrderDTO doctorOrderDTO = doctorOrderMapper.toDto(doctorOrder);

        // If the entity doesn't have an ID, it will throw BadRequestAlertException
        restDoctorOrderMockMvc
            .perform(
                patch(ENTITY_API_URL_ID, doctorOrderDTO.getId())
                    .contentType("application/merge-patch+json")
                    .content(om.writeValueAsBytes(doctorOrderDTO))
            )
            .andExpect(status().isBadRequest());

        // Validate the DoctorOrder in the database
        assertSameRepositoryCount(databaseSizeBeforeUpdate);
    }

    @Test
    @Transactional
    void patchWithIdMismatchDoctorOrder() throws Exception {
        long databaseSizeBeforeUpdate = getRepositoryCount();
        doctorOrder.setId(longCount.incrementAndGet());

        // Create the DoctorOrder
        DoctorOrderDTO doctorOrderDTO = doctorOrderMapper.toDto(doctorOrder);

        // If url ID doesn't match entity ID, it will throw BadRequestAlertException
        restDoctorOrderMockMvc
            .perform(
                patch(ENTITY_API_URL_ID, longCount.incrementAndGet())
                    .contentType("application/merge-patch+json")
                    .content(om.writeValueAsBytes(doctorOrderDTO))
            )
            .andExpect(status().isBadRequest());

        // Validate the DoctorOrder in the database
        assertSameRepositoryCount(databaseSizeBeforeUpdate);
    }

    @Test
    @Transactional
    void patchWithMissingIdPathParamDoctorOrder() throws Exception {
        long databaseSizeBeforeUpdate = getRepositoryCount();
        doctorOrder.setId(longCount.incrementAndGet());

        // Create the DoctorOrder
        DoctorOrderDTO doctorOrderDTO = doctorOrderMapper.toDto(doctorOrder);

        // If url ID doesn't match entity ID, it will throw BadRequestAlertException
        restDoctorOrderMockMvc
            .perform(patch(ENTITY_API_URL).contentType("application/merge-patch+json").content(om.writeValueAsBytes(doctorOrderDTO)))
            .andExpect(status().isMethodNotAllowed());

        // Validate the DoctorOrder in the database
        assertSameRepositoryCount(databaseSizeBeforeUpdate);
    }

    @Test
    @Transactional
    void deleteDoctorOrder() throws Exception {
        // Initialize the database
        insertedDoctorOrder = doctorOrderRepository.saveAndFlush(doctorOrder);

        long databaseSizeBeforeDelete = getRepositoryCount();

        // Delete the doctorOrder
        restDoctorOrderMockMvc
            .perform(delete(ENTITY_API_URL_ID, doctorOrder.getId()).accept(MediaType.APPLICATION_JSON))
            .andExpect(status().isNoContent());

        // Validate the database contains one less item
        assertDecrementedRepositoryCount(databaseSizeBeforeDelete);
    }

    protected long getRepositoryCount() {
        return doctorOrderRepository.count();
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

    protected DoctorOrder getPersistedDoctorOrder(DoctorOrder doctorOrder) {
        return doctorOrderRepository.findById(doctorOrder.getId()).orElseThrow();
    }

    protected void assertPersistedDoctorOrderToMatchAllProperties(DoctorOrder expectedDoctorOrder) {
        assertDoctorOrderAllPropertiesEquals(expectedDoctorOrder, getPersistedDoctorOrder(expectedDoctorOrder));
    }

    protected void assertPersistedDoctorOrderToMatchUpdatableProperties(DoctorOrder expectedDoctorOrder) {
        assertDoctorOrderAllUpdatablePropertiesEquals(expectedDoctorOrder, getPersistedDoctorOrder(expectedDoctorOrder));
    }
}
