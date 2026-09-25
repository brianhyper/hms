package com.hyperbrains.hms.web.rest;

import static com.hyperbrains.hms.domain.DiagnosticOrderAsserts.*;
import static com.hyperbrains.hms.web.rest.TestUtil.createUpdateProxyForBean;
import static org.assertj.core.api.Assertions.assertThat;
import static org.hamcrest.Matchers.hasItem;
import static org.mockito.Mockito.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.hyperbrains.hms.IntegrationTest;
import com.hyperbrains.hms.domain.DiagnosticOrder;
import com.hyperbrains.hms.domain.User;
import com.hyperbrains.hms.domain.Visit;
import com.hyperbrains.hms.domain.enumeration.OrderStatus;
import com.hyperbrains.hms.domain.enumeration.OrderType;
import com.hyperbrains.hms.repository.DiagnosticOrderRepository;
import com.hyperbrains.hms.repository.UserRepository;
import com.hyperbrains.hms.service.DiagnosticOrderService;
import com.hyperbrains.hms.service.dto.DiagnosticOrderDTO;
import com.hyperbrains.hms.service.mapper.DiagnosticOrderMapper;
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
import com.hyperbrains.hms.security.AuthoritiesConstants;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.transaction.annotation.Transactional;

/**
 * Integration tests for the {@link DiagnosticOrderResource} REST controller.
 */
@IntegrationTest
@ExtendWith(MockitoExtension.class)
@AutoConfigureMockMvc
// The raw entity endpoint is closed to clinical roles: creating an order here would skip pricing
// and the visit-status recomputation. Only the super-admin retains it, so that is what this test
// exercises. The clinical paths are covered by DiagnosticOrderIT.
@WithMockUser(authorities = AuthoritiesConstants.SUPER_ADMIN)
class DiagnosticOrderResourceIT {

    private static final OrderType DEFAULT_TYPE = OrderType.LAB;
    private static final OrderType UPDATED_TYPE = OrderType.RADIOLOGY;

    private static final String DEFAULT_TEST_NAME = "AAAAAAAAAA";
    private static final String UPDATED_TEST_NAME = "BBBBBBBBBB";

    private static final OrderStatus DEFAULT_STATUS = OrderStatus.PENDING;
    private static final OrderStatus UPDATED_STATUS = OrderStatus.IN_PROGRESS;

    private static final String DEFAULT_NOTES = "AAAAAAAAAA";
    private static final String UPDATED_NOTES = "BBBBBBBBBB";

    private static final Instant DEFAULT_ORDERED_AT = Instant.ofEpochMilli(0L);
    private static final Instant UPDATED_ORDERED_AT = Instant.ofEpochMilli(1790236635213L);

    private static final String ENTITY_API_URL = "/api/diagnostic-orders";
    private static final String ENTITY_API_URL_ID = ENTITY_API_URL + "/{id}";

    private static final Random random = new Random();
    private static final AtomicLong longCount = new AtomicLong(random.nextInt() + 2L * Integer.MAX_VALUE);

    @Autowired
    private ObjectMapper om;

    @Autowired
    private DiagnosticOrderRepository diagnosticOrderRepository;

    @Autowired
    private UserRepository userRepository;

    @Mock
    private DiagnosticOrderRepository diagnosticOrderRepositoryMock;

    @Autowired
    private DiagnosticOrderMapper diagnosticOrderMapper;

    @Mock
    private DiagnosticOrderService diagnosticOrderServiceMock;

    @Autowired
    private EntityManager em;

    @Autowired
    private MockMvc restDiagnosticOrderMockMvc;

    private DiagnosticOrder diagnosticOrder;

    private DiagnosticOrder insertedDiagnosticOrder;

    /**
     * Create an entity for this test.
     *
     * This is a static method, as tests for other entities might also need it,
     * if they test an entity which requires the current entity.
     */
    public static DiagnosticOrder createEntity(EntityManager em) {
        DiagnosticOrder diagnosticOrder = new DiagnosticOrder()
            .type(DEFAULT_TYPE)
            .testName(DEFAULT_TEST_NAME)
            .status(DEFAULT_STATUS)
            .notes(DEFAULT_NOTES)
            .orderedAt(DEFAULT_ORDERED_AT);
        // Add required entity
        Visit visit;
        if (TestUtil.findAll(em, Visit.class).isEmpty()) {
            visit = VisitResourceIT.createEntity(em);
            em.persist(visit);
            em.flush();
        } else {
            visit = TestUtil.findAll(em, Visit.class).get(0);
        }
        diagnosticOrder.setVisit(visit);
        // Add required entity
        User user = UserResourceIT.createEntity();
        em.persist(user);
        em.flush();
        diagnosticOrder.setOrderedBy(user);
        return diagnosticOrder;
    }

    /**
     * Create an updated entity for this test.
     *
     * This is a static method, as tests for other entities might also need it,
     * if they test an entity which requires the current entity.
     */
    public static DiagnosticOrder createUpdatedEntity(EntityManager em) {
        DiagnosticOrder updatedDiagnosticOrder = new DiagnosticOrder()
            .type(UPDATED_TYPE)
            .testName(UPDATED_TEST_NAME)
            .status(UPDATED_STATUS)
            .notes(UPDATED_NOTES)
            .orderedAt(UPDATED_ORDERED_AT);
        // Add required entity
        Visit visit;
        if (TestUtil.findAll(em, Visit.class).isEmpty()) {
            visit = VisitResourceIT.createUpdatedEntity(em);
            em.persist(visit);
            em.flush();
        } else {
            visit = TestUtil.findAll(em, Visit.class).get(0);
        }
        updatedDiagnosticOrder.setVisit(visit);
        // Add required entity
        User user = UserResourceIT.createEntity();
        em.persist(user);
        em.flush();
        updatedDiagnosticOrder.setOrderedBy(user);
        return updatedDiagnosticOrder;
    }

    @BeforeEach
    void initTest() {
        diagnosticOrder = createEntity(em);
    }

    @AfterEach
    void cleanup() {
        if (insertedDiagnosticOrder != null) {
            diagnosticOrderRepository.delete(insertedDiagnosticOrder);
            insertedDiagnosticOrder = null;
        }
    }

    @Test
    @Transactional
    void createDiagnosticOrder() throws Exception {
        long databaseSizeBeforeCreate = getRepositoryCount();
        // Create the DiagnosticOrder
        DiagnosticOrderDTO diagnosticOrderDTO = diagnosticOrderMapper.toDto(diagnosticOrder);
        var returnedDiagnosticOrderDTO = om.readValue(
            restDiagnosticOrderMockMvc
                .perform(post(ENTITY_API_URL).contentType(MediaType.APPLICATION_JSON).content(om.writeValueAsBytes(diagnosticOrderDTO)))
                .andExpect(status().isCreated())
                .andReturn()
                .getResponse()
                .getContentAsString(),
            DiagnosticOrderDTO.class
        );

        // Validate the DiagnosticOrder in the database
        assertIncrementedRepositoryCount(databaseSizeBeforeCreate);
        var returnedDiagnosticOrder = diagnosticOrderMapper.toEntity(returnedDiagnosticOrderDTO);
        assertDiagnosticOrderUpdatableFieldsEquals(returnedDiagnosticOrder, getPersistedDiagnosticOrder(returnedDiagnosticOrder));

        insertedDiagnosticOrder = returnedDiagnosticOrder;
    }

    @Test
    @Transactional
    void createDiagnosticOrderWithExistingId() throws Exception {
        // Create the DiagnosticOrder with an existing ID
        diagnosticOrder.setId(1L);
        DiagnosticOrderDTO diagnosticOrderDTO = diagnosticOrderMapper.toDto(diagnosticOrder);

        long databaseSizeBeforeCreate = getRepositoryCount();

        // An entity with an existing ID cannot be created, so this API call must fail
        restDiagnosticOrderMockMvc
            .perform(post(ENTITY_API_URL).contentType(MediaType.APPLICATION_JSON).content(om.writeValueAsBytes(diagnosticOrderDTO)))
            .andExpect(status().isBadRequest());

        // Validate the DiagnosticOrder in the database
        assertSameRepositoryCount(databaseSizeBeforeCreate);
    }

    @Test
    @Transactional
    void checkTypeIsRequired() throws Exception {
        long databaseSizeBeforeTest = getRepositoryCount();
        // set the field null
        diagnosticOrder.setType(null);

        // Create the DiagnosticOrder, which fails.
        DiagnosticOrderDTO diagnosticOrderDTO = diagnosticOrderMapper.toDto(diagnosticOrder);

        restDiagnosticOrderMockMvc
            .perform(post(ENTITY_API_URL).contentType(MediaType.APPLICATION_JSON).content(om.writeValueAsBytes(diagnosticOrderDTO)))
            .andExpect(status().isBadRequest());

        assertSameRepositoryCount(databaseSizeBeforeTest);
    }

    @Test
    @Transactional
    void checkTestNameIsRequired() throws Exception {
        long databaseSizeBeforeTest = getRepositoryCount();
        // set the field null
        diagnosticOrder.setTestName(null);

        // Create the DiagnosticOrder, which fails.
        DiagnosticOrderDTO diagnosticOrderDTO = diagnosticOrderMapper.toDto(diagnosticOrder);

        restDiagnosticOrderMockMvc
            .perform(post(ENTITY_API_URL).contentType(MediaType.APPLICATION_JSON).content(om.writeValueAsBytes(diagnosticOrderDTO)))
            .andExpect(status().isBadRequest());

        assertSameRepositoryCount(databaseSizeBeforeTest);
    }

    @Test
    @Transactional
    void checkStatusIsRequired() throws Exception {
        long databaseSizeBeforeTest = getRepositoryCount();
        // set the field null
        diagnosticOrder.setStatus(null);

        // Create the DiagnosticOrder, which fails.
        DiagnosticOrderDTO diagnosticOrderDTO = diagnosticOrderMapper.toDto(diagnosticOrder);

        restDiagnosticOrderMockMvc
            .perform(post(ENTITY_API_URL).contentType(MediaType.APPLICATION_JSON).content(om.writeValueAsBytes(diagnosticOrderDTO)))
            .andExpect(status().isBadRequest());

        assertSameRepositoryCount(databaseSizeBeforeTest);
    }

    @Test
    @Transactional
    void checkOrderedAtIsRequired() throws Exception {
        long databaseSizeBeforeTest = getRepositoryCount();
        // set the field null
        diagnosticOrder.setOrderedAt(null);

        // Create the DiagnosticOrder, which fails.
        DiagnosticOrderDTO diagnosticOrderDTO = diagnosticOrderMapper.toDto(diagnosticOrder);

        restDiagnosticOrderMockMvc
            .perform(post(ENTITY_API_URL).contentType(MediaType.APPLICATION_JSON).content(om.writeValueAsBytes(diagnosticOrderDTO)))
            .andExpect(status().isBadRequest());

        assertSameRepositoryCount(databaseSizeBeforeTest);
    }

    @Test
    @Transactional
    void getAllDiagnosticOrders() throws Exception {
        // Initialize the database
        insertedDiagnosticOrder = diagnosticOrderRepository.saveAndFlush(diagnosticOrder);

        // Get all the diagnosticOrderList
        restDiagnosticOrderMockMvc
            .perform(get(ENTITY_API_URL + "?sort=id,desc"))
            .andExpect(status().isOk())
            .andExpect(content().contentType(MediaType.APPLICATION_JSON_VALUE))
            .andExpect(jsonPath("$.[*].id").value(hasItem(diagnosticOrder.getId().intValue())))
            .andExpect(jsonPath("$.[*].type").value(hasItem(DEFAULT_TYPE.toString())))
            .andExpect(jsonPath("$.[*].testName").value(hasItem(DEFAULT_TEST_NAME)))
            .andExpect(jsonPath("$.[*].status").value(hasItem(DEFAULT_STATUS.toString())))
            .andExpect(jsonPath("$.[*].notes").value(hasItem(DEFAULT_NOTES)))
            .andExpect(jsonPath("$.[*].orderedAt").value(hasItem(DEFAULT_ORDERED_AT.toString())));
    }

    @SuppressWarnings({ "unchecked" })
    void getAllDiagnosticOrdersWithEagerRelationshipsIsEnabled() throws Exception {
        when(diagnosticOrderServiceMock.findAllWithEagerRelationships(any())).thenReturn(new PageImpl(new ArrayList<>()));

        restDiagnosticOrderMockMvc.perform(get(ENTITY_API_URL + "?eagerload=true")).andExpect(status().isOk());

        verify(diagnosticOrderServiceMock, times(1)).findAllWithEagerRelationships(any());
    }

    @SuppressWarnings({ "unchecked" })
    void getAllDiagnosticOrdersWithEagerRelationshipsIsNotEnabled() throws Exception {
        when(diagnosticOrderServiceMock.findAllWithEagerRelationships(any())).thenReturn(new PageImpl(new ArrayList<>()));

        restDiagnosticOrderMockMvc.perform(get(ENTITY_API_URL + "?eagerload=false")).andExpect(status().isOk());
        verify(diagnosticOrderRepositoryMock, times(1)).findAll(any(Pageable.class));
    }

    @Test
    @Transactional
    void getDiagnosticOrder() throws Exception {
        // Initialize the database
        insertedDiagnosticOrder = diagnosticOrderRepository.saveAndFlush(diagnosticOrder);

        // Get the diagnosticOrder
        restDiagnosticOrderMockMvc
            .perform(get(ENTITY_API_URL_ID, diagnosticOrder.getId()))
            .andExpect(status().isOk())
            .andExpect(content().contentType(MediaType.APPLICATION_JSON_VALUE))
            .andExpect(jsonPath("$.id").value(diagnosticOrder.getId().intValue()))
            .andExpect(jsonPath("$.type").value(DEFAULT_TYPE.toString()))
            .andExpect(jsonPath("$.testName").value(DEFAULT_TEST_NAME))
            .andExpect(jsonPath("$.status").value(DEFAULT_STATUS.toString()))
            .andExpect(jsonPath("$.notes").value(DEFAULT_NOTES))
            .andExpect(jsonPath("$.orderedAt").value(DEFAULT_ORDERED_AT.toString()));
    }

    @Test
    @Transactional
    void getNonExistingDiagnosticOrder() throws Exception {
        // Get the diagnosticOrder
        restDiagnosticOrderMockMvc.perform(get(ENTITY_API_URL_ID, Long.MAX_VALUE)).andExpect(status().isNotFound());
    }

    @Test
    @Transactional
    void putExistingDiagnosticOrder() throws Exception {
        // Initialize the database
        insertedDiagnosticOrder = diagnosticOrderRepository.saveAndFlush(diagnosticOrder);

        long databaseSizeBeforeUpdate = getRepositoryCount();

        // Update the diagnosticOrder
        DiagnosticOrder updatedDiagnosticOrder = diagnosticOrderRepository.findById(diagnosticOrder.getId()).orElseThrow();
        // Disconnect from session so that the updates on updatedDiagnosticOrder are not directly saved in db
        em.detach(updatedDiagnosticOrder);
        updatedDiagnosticOrder
            .type(UPDATED_TYPE)
            .testName(UPDATED_TEST_NAME)
            .status(UPDATED_STATUS)
            .notes(UPDATED_NOTES)
            .orderedAt(UPDATED_ORDERED_AT);
        DiagnosticOrderDTO diagnosticOrderDTO = diagnosticOrderMapper.toDto(updatedDiagnosticOrder);

        restDiagnosticOrderMockMvc
            .perform(
                put(ENTITY_API_URL_ID, diagnosticOrderDTO.getId())
                    .contentType(MediaType.APPLICATION_JSON)
                    .content(om.writeValueAsBytes(diagnosticOrderDTO))
            )
            .andExpect(status().isOk());

        // Validate the DiagnosticOrder in the database
        assertSameRepositoryCount(databaseSizeBeforeUpdate);
        assertPersistedDiagnosticOrderToMatchAllProperties(updatedDiagnosticOrder);
    }

    @Test
    @Transactional
    void putNonExistingDiagnosticOrder() throws Exception {
        long databaseSizeBeforeUpdate = getRepositoryCount();
        diagnosticOrder.setId(longCount.incrementAndGet());

        // Create the DiagnosticOrder
        DiagnosticOrderDTO diagnosticOrderDTO = diagnosticOrderMapper.toDto(diagnosticOrder);

        // If the entity doesn't have an ID, it will throw BadRequestAlertException
        restDiagnosticOrderMockMvc
            .perform(
                put(ENTITY_API_URL_ID, diagnosticOrderDTO.getId())
                    .contentType(MediaType.APPLICATION_JSON)
                    .content(om.writeValueAsBytes(diagnosticOrderDTO))
            )
            .andExpect(status().isBadRequest());

        // Validate the DiagnosticOrder in the database
        assertSameRepositoryCount(databaseSizeBeforeUpdate);
    }

    @Test
    @Transactional
    void putWithIdMismatchDiagnosticOrder() throws Exception {
        long databaseSizeBeforeUpdate = getRepositoryCount();
        diagnosticOrder.setId(longCount.incrementAndGet());

        // Create the DiagnosticOrder
        DiagnosticOrderDTO diagnosticOrderDTO = diagnosticOrderMapper.toDto(diagnosticOrder);

        // If url ID doesn't match entity ID, it will throw BadRequestAlertException
        restDiagnosticOrderMockMvc
            .perform(
                put(ENTITY_API_URL_ID, longCount.incrementAndGet())
                    .contentType(MediaType.APPLICATION_JSON)
                    .content(om.writeValueAsBytes(diagnosticOrderDTO))
            )
            .andExpect(status().isBadRequest());

        // Validate the DiagnosticOrder in the database
        assertSameRepositoryCount(databaseSizeBeforeUpdate);
    }

    @Test
    @Transactional
    void putWithMissingIdPathParamDiagnosticOrder() throws Exception {
        long databaseSizeBeforeUpdate = getRepositoryCount();
        diagnosticOrder.setId(longCount.incrementAndGet());

        // Create the DiagnosticOrder
        DiagnosticOrderDTO diagnosticOrderDTO = diagnosticOrderMapper.toDto(diagnosticOrder);

        // If url ID doesn't match entity ID, it will throw BadRequestAlertException
        restDiagnosticOrderMockMvc
            .perform(put(ENTITY_API_URL).contentType(MediaType.APPLICATION_JSON).content(om.writeValueAsBytes(diagnosticOrderDTO)))
            .andExpect(status().isMethodNotAllowed());

        // Validate the DiagnosticOrder in the database
        assertSameRepositoryCount(databaseSizeBeforeUpdate);
    }

    @Test
    @Transactional
    void partialUpdateDiagnosticOrderWithPatch() throws Exception {
        // Initialize the database
        insertedDiagnosticOrder = diagnosticOrderRepository.saveAndFlush(diagnosticOrder);

        long databaseSizeBeforeUpdate = getRepositoryCount();

        // Update the diagnosticOrder using partial update
        DiagnosticOrder partialUpdatedDiagnosticOrder = new DiagnosticOrder();
        partialUpdatedDiagnosticOrder.setId(diagnosticOrder.getId());

        partialUpdatedDiagnosticOrder.type(UPDATED_TYPE).status(UPDATED_STATUS);

        restDiagnosticOrderMockMvc
            .perform(
                patch(ENTITY_API_URL_ID, partialUpdatedDiagnosticOrder.getId())
                    .contentType("application/merge-patch+json")
                    .content(om.writeValueAsBytes(partialUpdatedDiagnosticOrder))
            )
            .andExpect(status().isOk());

        // Validate the DiagnosticOrder in the database

        assertSameRepositoryCount(databaseSizeBeforeUpdate);
        assertDiagnosticOrderUpdatableFieldsEquals(
            createUpdateProxyForBean(partialUpdatedDiagnosticOrder, diagnosticOrder),
            getPersistedDiagnosticOrder(diagnosticOrder)
        );
    }

    @Test
    @Transactional
    void fullUpdateDiagnosticOrderWithPatch() throws Exception {
        // Initialize the database
        insertedDiagnosticOrder = diagnosticOrderRepository.saveAndFlush(diagnosticOrder);

        long databaseSizeBeforeUpdate = getRepositoryCount();

        // Update the diagnosticOrder using partial update
        DiagnosticOrder partialUpdatedDiagnosticOrder = new DiagnosticOrder();
        partialUpdatedDiagnosticOrder.setId(diagnosticOrder.getId());

        partialUpdatedDiagnosticOrder
            .type(UPDATED_TYPE)
            .testName(UPDATED_TEST_NAME)
            .status(UPDATED_STATUS)
            .notes(UPDATED_NOTES)
            .orderedAt(UPDATED_ORDERED_AT);

        restDiagnosticOrderMockMvc
            .perform(
                patch(ENTITY_API_URL_ID, partialUpdatedDiagnosticOrder.getId())
                    .contentType("application/merge-patch+json")
                    .content(om.writeValueAsBytes(partialUpdatedDiagnosticOrder))
            )
            .andExpect(status().isOk());

        // Validate the DiagnosticOrder in the database

        assertSameRepositoryCount(databaseSizeBeforeUpdate);
        assertDiagnosticOrderUpdatableFieldsEquals(
            partialUpdatedDiagnosticOrder,
            getPersistedDiagnosticOrder(partialUpdatedDiagnosticOrder)
        );
    }

    @Test
    @Transactional
    void patchNonExistingDiagnosticOrder() throws Exception {
        long databaseSizeBeforeUpdate = getRepositoryCount();
        diagnosticOrder.setId(longCount.incrementAndGet());

        // Create the DiagnosticOrder
        DiagnosticOrderDTO diagnosticOrderDTO = diagnosticOrderMapper.toDto(diagnosticOrder);

        // If the entity doesn't have an ID, it will throw BadRequestAlertException
        restDiagnosticOrderMockMvc
            .perform(
                patch(ENTITY_API_URL_ID, diagnosticOrderDTO.getId())
                    .contentType("application/merge-patch+json")
                    .content(om.writeValueAsBytes(diagnosticOrderDTO))
            )
            .andExpect(status().isBadRequest());

        // Validate the DiagnosticOrder in the database
        assertSameRepositoryCount(databaseSizeBeforeUpdate);
    }

    @Test
    @Transactional
    void patchWithIdMismatchDiagnosticOrder() throws Exception {
        long databaseSizeBeforeUpdate = getRepositoryCount();
        diagnosticOrder.setId(longCount.incrementAndGet());

        // Create the DiagnosticOrder
        DiagnosticOrderDTO diagnosticOrderDTO = diagnosticOrderMapper.toDto(diagnosticOrder);

        // If url ID doesn't match entity ID, it will throw BadRequestAlertException
        restDiagnosticOrderMockMvc
            .perform(
                patch(ENTITY_API_URL_ID, longCount.incrementAndGet())
                    .contentType("application/merge-patch+json")
                    .content(om.writeValueAsBytes(diagnosticOrderDTO))
            )
            .andExpect(status().isBadRequest());

        // Validate the DiagnosticOrder in the database
        assertSameRepositoryCount(databaseSizeBeforeUpdate);
    }

    @Test
    @Transactional
    void patchWithMissingIdPathParamDiagnosticOrder() throws Exception {
        long databaseSizeBeforeUpdate = getRepositoryCount();
        diagnosticOrder.setId(longCount.incrementAndGet());

        // Create the DiagnosticOrder
        DiagnosticOrderDTO diagnosticOrderDTO = diagnosticOrderMapper.toDto(diagnosticOrder);

        // If url ID doesn't match entity ID, it will throw BadRequestAlertException
        restDiagnosticOrderMockMvc
            .perform(patch(ENTITY_API_URL).contentType("application/merge-patch+json").content(om.writeValueAsBytes(diagnosticOrderDTO)))
            .andExpect(status().isMethodNotAllowed());

        // Validate the DiagnosticOrder in the database
        assertSameRepositoryCount(databaseSizeBeforeUpdate);
    }

    @Test
    @Transactional
    void deleteDiagnosticOrder() throws Exception {
        // Initialize the database
        insertedDiagnosticOrder = diagnosticOrderRepository.saveAndFlush(diagnosticOrder);

        long databaseSizeBeforeDelete = getRepositoryCount();

        // Delete the diagnosticOrder
        restDiagnosticOrderMockMvc
            .perform(delete(ENTITY_API_URL_ID, diagnosticOrder.getId()).accept(MediaType.APPLICATION_JSON))
            .andExpect(status().isNoContent());

        // Validate the database contains one less item
        assertDecrementedRepositoryCount(databaseSizeBeforeDelete);
    }

    protected long getRepositoryCount() {
        return diagnosticOrderRepository.count();
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

    protected DiagnosticOrder getPersistedDiagnosticOrder(DiagnosticOrder diagnosticOrder) {
        return diagnosticOrderRepository.findById(diagnosticOrder.getId()).orElseThrow();
    }

    protected void assertPersistedDiagnosticOrderToMatchAllProperties(DiagnosticOrder expectedDiagnosticOrder) {
        assertDiagnosticOrderAllPropertiesEquals(expectedDiagnosticOrder, getPersistedDiagnosticOrder(expectedDiagnosticOrder));
    }

    protected void assertPersistedDiagnosticOrderToMatchUpdatableProperties(DiagnosticOrder expectedDiagnosticOrder) {
        assertDiagnosticOrderAllUpdatablePropertiesEquals(expectedDiagnosticOrder, getPersistedDiagnosticOrder(expectedDiagnosticOrder));
    }
}
