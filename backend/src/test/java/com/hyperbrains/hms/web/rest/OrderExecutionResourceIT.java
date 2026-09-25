package com.hyperbrains.hms.web.rest;

import static com.hyperbrains.hms.domain.OrderExecutionAsserts.*;
import static com.hyperbrains.hms.web.rest.TestUtil.createUpdateProxyForBean;
import static org.assertj.core.api.Assertions.assertThat;
import static org.hamcrest.Matchers.hasItem;
import static org.mockito.Mockito.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.hyperbrains.hms.IntegrationTest;
import com.hyperbrains.hms.domain.DoctorOrder;
import com.hyperbrains.hms.domain.OrderExecution;
import com.hyperbrains.hms.domain.User;
import com.hyperbrains.hms.repository.OrderExecutionRepository;
import com.hyperbrains.hms.repository.UserRepository;
import com.hyperbrains.hms.service.OrderExecutionService;
import com.hyperbrains.hms.service.dto.OrderExecutionDTO;
import com.hyperbrains.hms.service.mapper.OrderExecutionMapper;
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
 * Integration tests for the {@link OrderExecutionResource} REST controller.
 */
@IntegrationTest
@ExtendWith(MockitoExtension.class)
@AutoConfigureMockMvc
@WithMockUser
class OrderExecutionResourceIT {

    private static final Instant DEFAULT_EXECUTED_AT = Instant.ofEpochMilli(0L);
    private static final Instant UPDATED_EXECUTED_AT = Instant.ofEpochMilli(1790236635213L);

    private static final String DEFAULT_NOTES = "AAAAAAAAAA";
    private static final String UPDATED_NOTES = "BBBBBBBBBB";

    private static final String ENTITY_API_URL = "/api/order-executions";
    private static final String ENTITY_API_URL_ID = ENTITY_API_URL + "/{id}";

    private static final Random random = new Random();
    private static final AtomicLong longCount = new AtomicLong(random.nextInt() + 2L * Integer.MAX_VALUE);

    @Autowired
    private ObjectMapper om;

    @Autowired
    private OrderExecutionRepository orderExecutionRepository;

    @Autowired
    private UserRepository userRepository;

    @Mock
    private OrderExecutionRepository orderExecutionRepositoryMock;

    @Autowired
    private OrderExecutionMapper orderExecutionMapper;

    @Mock
    private OrderExecutionService orderExecutionServiceMock;

    @Autowired
    private EntityManager em;

    @Autowired
    private MockMvc restOrderExecutionMockMvc;

    private OrderExecution orderExecution;

    private OrderExecution insertedOrderExecution;

    /**
     * Create an entity for this test.
     *
     * This is a static method, as tests for other entities might also need it,
     * if they test an entity which requires the current entity.
     */
    public static OrderExecution createEntity(EntityManager em) {
        OrderExecution orderExecution = new OrderExecution().executedAt(DEFAULT_EXECUTED_AT).notes(DEFAULT_NOTES);
        // Add required entity
        DoctorOrder doctorOrder;
        if (TestUtil.findAll(em, DoctorOrder.class).isEmpty()) {
            doctorOrder = DoctorOrderResourceIT.createEntity(em);
            em.persist(doctorOrder);
            em.flush();
        } else {
            doctorOrder = TestUtil.findAll(em, DoctorOrder.class).get(0);
        }
        orderExecution.setDoctorOrder(doctorOrder);
        // Add required entity
        User user = UserResourceIT.createEntity();
        em.persist(user);
        em.flush();
        orderExecution.setExecutedBy(user);
        return orderExecution;
    }

    /**
     * Create an updated entity for this test.
     *
     * This is a static method, as tests for other entities might also need it,
     * if they test an entity which requires the current entity.
     */
    public static OrderExecution createUpdatedEntity(EntityManager em) {
        OrderExecution updatedOrderExecution = new OrderExecution().executedAt(UPDATED_EXECUTED_AT).notes(UPDATED_NOTES);
        // Add required entity
        DoctorOrder doctorOrder;
        if (TestUtil.findAll(em, DoctorOrder.class).isEmpty()) {
            doctorOrder = DoctorOrderResourceIT.createUpdatedEntity(em);
            em.persist(doctorOrder);
            em.flush();
        } else {
            doctorOrder = TestUtil.findAll(em, DoctorOrder.class).get(0);
        }
        updatedOrderExecution.setDoctorOrder(doctorOrder);
        // Add required entity
        User user = UserResourceIT.createEntity();
        em.persist(user);
        em.flush();
        updatedOrderExecution.setExecutedBy(user);
        return updatedOrderExecution;
    }

    @BeforeEach
    void initTest() {
        orderExecution = createEntity(em);
    }

    @AfterEach
    void cleanup() {
        if (insertedOrderExecution != null) {
            orderExecutionRepository.delete(insertedOrderExecution);
            insertedOrderExecution = null;
        }
    }

    @Test
    @Transactional
    void createOrderExecution() throws Exception {
        long databaseSizeBeforeCreate = getRepositoryCount();
        // Create the OrderExecution
        OrderExecutionDTO orderExecutionDTO = orderExecutionMapper.toDto(orderExecution);
        var returnedOrderExecutionDTO = om.readValue(
            restOrderExecutionMockMvc
                .perform(post(ENTITY_API_URL).contentType(MediaType.APPLICATION_JSON).content(om.writeValueAsBytes(orderExecutionDTO)))
                .andExpect(status().isCreated())
                .andReturn()
                .getResponse()
                .getContentAsString(),
            OrderExecutionDTO.class
        );

        // Validate the OrderExecution in the database
        assertIncrementedRepositoryCount(databaseSizeBeforeCreate);
        var returnedOrderExecution = orderExecutionMapper.toEntity(returnedOrderExecutionDTO);
        assertOrderExecutionUpdatableFieldsEquals(returnedOrderExecution, getPersistedOrderExecution(returnedOrderExecution));

        insertedOrderExecution = returnedOrderExecution;
    }

    @Test
    @Transactional
    void createOrderExecutionWithExistingId() throws Exception {
        // Create the OrderExecution with an existing ID
        orderExecution.setId(1L);
        OrderExecutionDTO orderExecutionDTO = orderExecutionMapper.toDto(orderExecution);

        long databaseSizeBeforeCreate = getRepositoryCount();

        // An entity with an existing ID cannot be created, so this API call must fail
        restOrderExecutionMockMvc
            .perform(post(ENTITY_API_URL).contentType(MediaType.APPLICATION_JSON).content(om.writeValueAsBytes(orderExecutionDTO)))
            .andExpect(status().isBadRequest());

        // Validate the OrderExecution in the database
        assertSameRepositoryCount(databaseSizeBeforeCreate);
    }

    @Test
    @Transactional
    void checkExecutedAtIsRequired() throws Exception {
        long databaseSizeBeforeTest = getRepositoryCount();
        // set the field null
        orderExecution.setExecutedAt(null);

        // Create the OrderExecution, which fails.
        OrderExecutionDTO orderExecutionDTO = orderExecutionMapper.toDto(orderExecution);

        restOrderExecutionMockMvc
            .perform(post(ENTITY_API_URL).contentType(MediaType.APPLICATION_JSON).content(om.writeValueAsBytes(orderExecutionDTO)))
            .andExpect(status().isBadRequest());

        assertSameRepositoryCount(databaseSizeBeforeTest);
    }

    @Test
    @Transactional
    void getAllOrderExecutions() throws Exception {
        // Initialize the database
        insertedOrderExecution = orderExecutionRepository.saveAndFlush(orderExecution);

        // Get all the orderExecutionList
        restOrderExecutionMockMvc
            .perform(get(ENTITY_API_URL + "?sort=id,desc"))
            .andExpect(status().isOk())
            .andExpect(content().contentType(MediaType.APPLICATION_JSON_VALUE))
            .andExpect(jsonPath("$.[*].id").value(hasItem(orderExecution.getId().intValue())))
            .andExpect(jsonPath("$.[*].executedAt").value(hasItem(DEFAULT_EXECUTED_AT.toString())))
            .andExpect(jsonPath("$.[*].notes").value(hasItem(DEFAULT_NOTES)));
    }

    @SuppressWarnings({ "unchecked" })
    void getAllOrderExecutionsWithEagerRelationshipsIsEnabled() throws Exception {
        when(orderExecutionServiceMock.findAllWithEagerRelationships(any())).thenReturn(new PageImpl(new ArrayList<>()));

        restOrderExecutionMockMvc.perform(get(ENTITY_API_URL + "?eagerload=true")).andExpect(status().isOk());

        verify(orderExecutionServiceMock, times(1)).findAllWithEagerRelationships(any());
    }

    @SuppressWarnings({ "unchecked" })
    void getAllOrderExecutionsWithEagerRelationshipsIsNotEnabled() throws Exception {
        when(orderExecutionServiceMock.findAllWithEagerRelationships(any())).thenReturn(new PageImpl(new ArrayList<>()));

        restOrderExecutionMockMvc.perform(get(ENTITY_API_URL + "?eagerload=false")).andExpect(status().isOk());
        verify(orderExecutionRepositoryMock, times(1)).findAll(any(Pageable.class));
    }

    @Test
    @Transactional
    void getOrderExecution() throws Exception {
        // Initialize the database
        insertedOrderExecution = orderExecutionRepository.saveAndFlush(orderExecution);

        // Get the orderExecution
        restOrderExecutionMockMvc
            .perform(get(ENTITY_API_URL_ID, orderExecution.getId()))
            .andExpect(status().isOk())
            .andExpect(content().contentType(MediaType.APPLICATION_JSON_VALUE))
            .andExpect(jsonPath("$.id").value(orderExecution.getId().intValue()))
            .andExpect(jsonPath("$.executedAt").value(DEFAULT_EXECUTED_AT.toString()))
            .andExpect(jsonPath("$.notes").value(DEFAULT_NOTES));
    }

    @Test
    @Transactional
    void getNonExistingOrderExecution() throws Exception {
        // Get the orderExecution
        restOrderExecutionMockMvc.perform(get(ENTITY_API_URL_ID, Long.MAX_VALUE)).andExpect(status().isNotFound());
    }

    @Test
    @Transactional
    void putExistingOrderExecution() throws Exception {
        // Initialize the database
        insertedOrderExecution = orderExecutionRepository.saveAndFlush(orderExecution);

        long databaseSizeBeforeUpdate = getRepositoryCount();

        // Update the orderExecution
        OrderExecution updatedOrderExecution = orderExecutionRepository.findById(orderExecution.getId()).orElseThrow();
        // Disconnect from session so that the updates on updatedOrderExecution are not directly saved in db
        em.detach(updatedOrderExecution);
        updatedOrderExecution.executedAt(UPDATED_EXECUTED_AT).notes(UPDATED_NOTES);
        OrderExecutionDTO orderExecutionDTO = orderExecutionMapper.toDto(updatedOrderExecution);

        restOrderExecutionMockMvc
            .perform(
                put(ENTITY_API_URL_ID, orderExecutionDTO.getId())
                    .contentType(MediaType.APPLICATION_JSON)
                    .content(om.writeValueAsBytes(orderExecutionDTO))
            )
            .andExpect(status().isOk());

        // Validate the OrderExecution in the database
        assertSameRepositoryCount(databaseSizeBeforeUpdate);
        assertPersistedOrderExecutionToMatchAllProperties(updatedOrderExecution);
    }

    @Test
    @Transactional
    void putNonExistingOrderExecution() throws Exception {
        long databaseSizeBeforeUpdate = getRepositoryCount();
        orderExecution.setId(longCount.incrementAndGet());

        // Create the OrderExecution
        OrderExecutionDTO orderExecutionDTO = orderExecutionMapper.toDto(orderExecution);

        // If the entity doesn't have an ID, it will throw BadRequestAlertException
        restOrderExecutionMockMvc
            .perform(
                put(ENTITY_API_URL_ID, orderExecutionDTO.getId())
                    .contentType(MediaType.APPLICATION_JSON)
                    .content(om.writeValueAsBytes(orderExecutionDTO))
            )
            .andExpect(status().isBadRequest());

        // Validate the OrderExecution in the database
        assertSameRepositoryCount(databaseSizeBeforeUpdate);
    }

    @Test
    @Transactional
    void putWithIdMismatchOrderExecution() throws Exception {
        long databaseSizeBeforeUpdate = getRepositoryCount();
        orderExecution.setId(longCount.incrementAndGet());

        // Create the OrderExecution
        OrderExecutionDTO orderExecutionDTO = orderExecutionMapper.toDto(orderExecution);

        // If url ID doesn't match entity ID, it will throw BadRequestAlertException
        restOrderExecutionMockMvc
            .perform(
                put(ENTITY_API_URL_ID, longCount.incrementAndGet())
                    .contentType(MediaType.APPLICATION_JSON)
                    .content(om.writeValueAsBytes(orderExecutionDTO))
            )
            .andExpect(status().isBadRequest());

        // Validate the OrderExecution in the database
        assertSameRepositoryCount(databaseSizeBeforeUpdate);
    }

    @Test
    @Transactional
    void putWithMissingIdPathParamOrderExecution() throws Exception {
        long databaseSizeBeforeUpdate = getRepositoryCount();
        orderExecution.setId(longCount.incrementAndGet());

        // Create the OrderExecution
        OrderExecutionDTO orderExecutionDTO = orderExecutionMapper.toDto(orderExecution);

        // If url ID doesn't match entity ID, it will throw BadRequestAlertException
        restOrderExecutionMockMvc
            .perform(put(ENTITY_API_URL).contentType(MediaType.APPLICATION_JSON).content(om.writeValueAsBytes(orderExecutionDTO)))
            .andExpect(status().isMethodNotAllowed());

        // Validate the OrderExecution in the database
        assertSameRepositoryCount(databaseSizeBeforeUpdate);
    }

    @Test
    @Transactional
    void partialUpdateOrderExecutionWithPatch() throws Exception {
        // Initialize the database
        insertedOrderExecution = orderExecutionRepository.saveAndFlush(orderExecution);

        long databaseSizeBeforeUpdate = getRepositoryCount();

        // Update the orderExecution using partial update
        OrderExecution partialUpdatedOrderExecution = new OrderExecution();
        partialUpdatedOrderExecution.setId(orderExecution.getId());

        partialUpdatedOrderExecution.executedAt(UPDATED_EXECUTED_AT);

        restOrderExecutionMockMvc
            .perform(
                patch(ENTITY_API_URL_ID, partialUpdatedOrderExecution.getId())
                    .contentType("application/merge-patch+json")
                    .content(om.writeValueAsBytes(partialUpdatedOrderExecution))
            )
            .andExpect(status().isOk());

        // Validate the OrderExecution in the database

        assertSameRepositoryCount(databaseSizeBeforeUpdate);
        assertOrderExecutionUpdatableFieldsEquals(
            createUpdateProxyForBean(partialUpdatedOrderExecution, orderExecution),
            getPersistedOrderExecution(orderExecution)
        );
    }

    @Test
    @Transactional
    void fullUpdateOrderExecutionWithPatch() throws Exception {
        // Initialize the database
        insertedOrderExecution = orderExecutionRepository.saveAndFlush(orderExecution);

        long databaseSizeBeforeUpdate = getRepositoryCount();

        // Update the orderExecution using partial update
        OrderExecution partialUpdatedOrderExecution = new OrderExecution();
        partialUpdatedOrderExecution.setId(orderExecution.getId());

        partialUpdatedOrderExecution.executedAt(UPDATED_EXECUTED_AT).notes(UPDATED_NOTES);

        restOrderExecutionMockMvc
            .perform(
                patch(ENTITY_API_URL_ID, partialUpdatedOrderExecution.getId())
                    .contentType("application/merge-patch+json")
                    .content(om.writeValueAsBytes(partialUpdatedOrderExecution))
            )
            .andExpect(status().isOk());

        // Validate the OrderExecution in the database

        assertSameRepositoryCount(databaseSizeBeforeUpdate);
        assertOrderExecutionUpdatableFieldsEquals(partialUpdatedOrderExecution, getPersistedOrderExecution(partialUpdatedOrderExecution));
    }

    @Test
    @Transactional
    void patchNonExistingOrderExecution() throws Exception {
        long databaseSizeBeforeUpdate = getRepositoryCount();
        orderExecution.setId(longCount.incrementAndGet());

        // Create the OrderExecution
        OrderExecutionDTO orderExecutionDTO = orderExecutionMapper.toDto(orderExecution);

        // If the entity doesn't have an ID, it will throw BadRequestAlertException
        restOrderExecutionMockMvc
            .perform(
                patch(ENTITY_API_URL_ID, orderExecutionDTO.getId())
                    .contentType("application/merge-patch+json")
                    .content(om.writeValueAsBytes(orderExecutionDTO))
            )
            .andExpect(status().isBadRequest());

        // Validate the OrderExecution in the database
        assertSameRepositoryCount(databaseSizeBeforeUpdate);
    }

    @Test
    @Transactional
    void patchWithIdMismatchOrderExecution() throws Exception {
        long databaseSizeBeforeUpdate = getRepositoryCount();
        orderExecution.setId(longCount.incrementAndGet());

        // Create the OrderExecution
        OrderExecutionDTO orderExecutionDTO = orderExecutionMapper.toDto(orderExecution);

        // If url ID doesn't match entity ID, it will throw BadRequestAlertException
        restOrderExecutionMockMvc
            .perform(
                patch(ENTITY_API_URL_ID, longCount.incrementAndGet())
                    .contentType("application/merge-patch+json")
                    .content(om.writeValueAsBytes(orderExecutionDTO))
            )
            .andExpect(status().isBadRequest());

        // Validate the OrderExecution in the database
        assertSameRepositoryCount(databaseSizeBeforeUpdate);
    }

    @Test
    @Transactional
    void patchWithMissingIdPathParamOrderExecution() throws Exception {
        long databaseSizeBeforeUpdate = getRepositoryCount();
        orderExecution.setId(longCount.incrementAndGet());

        // Create the OrderExecution
        OrderExecutionDTO orderExecutionDTO = orderExecutionMapper.toDto(orderExecution);

        // If url ID doesn't match entity ID, it will throw BadRequestAlertException
        restOrderExecutionMockMvc
            .perform(patch(ENTITY_API_URL).contentType("application/merge-patch+json").content(om.writeValueAsBytes(orderExecutionDTO)))
            .andExpect(status().isMethodNotAllowed());

        // Validate the OrderExecution in the database
        assertSameRepositoryCount(databaseSizeBeforeUpdate);
    }

    @Test
    @Transactional
    void deleteOrderExecution() throws Exception {
        // Initialize the database
        insertedOrderExecution = orderExecutionRepository.saveAndFlush(orderExecution);

        long databaseSizeBeforeDelete = getRepositoryCount();

        // Delete the orderExecution
        restOrderExecutionMockMvc
            .perform(delete(ENTITY_API_URL_ID, orderExecution.getId()).accept(MediaType.APPLICATION_JSON))
            .andExpect(status().isNoContent());

        // Validate the database contains one less item
        assertDecrementedRepositoryCount(databaseSizeBeforeDelete);
    }

    protected long getRepositoryCount() {
        return orderExecutionRepository.count();
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

    protected OrderExecution getPersistedOrderExecution(OrderExecution orderExecution) {
        return orderExecutionRepository.findById(orderExecution.getId()).orElseThrow();
    }

    protected void assertPersistedOrderExecutionToMatchAllProperties(OrderExecution expectedOrderExecution) {
        assertOrderExecutionAllPropertiesEquals(expectedOrderExecution, getPersistedOrderExecution(expectedOrderExecution));
    }

    protected void assertPersistedOrderExecutionToMatchUpdatableProperties(OrderExecution expectedOrderExecution) {
        assertOrderExecutionAllUpdatablePropertiesEquals(expectedOrderExecution, getPersistedOrderExecution(expectedOrderExecution));
    }
}
