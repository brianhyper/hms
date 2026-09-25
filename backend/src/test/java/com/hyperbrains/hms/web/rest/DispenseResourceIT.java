package com.hyperbrains.hms.web.rest;

import static com.hyperbrains.hms.domain.DispenseAsserts.*;
import static com.hyperbrains.hms.web.rest.TestUtil.createUpdateProxyForBean;
import static org.assertj.core.api.Assertions.assertThat;
import static org.hamcrest.Matchers.hasItem;
import static org.mockito.Mockito.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.hyperbrains.hms.IntegrationTest;
import com.hyperbrains.hms.domain.Dispense;
import com.hyperbrains.hms.domain.Prescription;
import com.hyperbrains.hms.domain.User;
import com.hyperbrains.hms.repository.DispenseRepository;
import com.hyperbrains.hms.repository.UserRepository;
import com.hyperbrains.hms.service.DispenseService;
import com.hyperbrains.hms.service.dto.DispenseDTO;
import com.hyperbrains.hms.service.mapper.DispenseMapper;
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
 * Integration tests for the {@link DispenseResource} REST controller.
 */
@IntegrationTest
@ExtendWith(MockitoExtension.class)
@AutoConfigureMockMvc
@WithMockUser(authorities = AuthoritiesConstants.SUPER_ADMIN)
class DispenseResourceIT {

    private static final Instant DEFAULT_DISPENSED_AT = Instant.ofEpochMilli(0L);
    private static final Instant UPDATED_DISPENSED_AT = Instant.ofEpochMilli(1790236635213L);

    private static final String DEFAULT_NOTE = "AAAAAAAAAA";
    private static final String UPDATED_NOTE = "BBBBBBBBBB";

    private static final String ENTITY_API_URL = "/api/dispenses";
    private static final String ENTITY_API_URL_ID = ENTITY_API_URL + "/{id}";

    private static final Random random = new Random();
    private static final AtomicLong longCount = new AtomicLong(random.nextInt() + 2L * Integer.MAX_VALUE);

    @Autowired
    private ObjectMapper om;

    @Autowired
    private DispenseRepository dispenseRepository;

    @Autowired
    private UserRepository userRepository;

    @Mock
    private DispenseRepository dispenseRepositoryMock;

    @Autowired
    private DispenseMapper dispenseMapper;

    @Mock
    private DispenseService dispenseServiceMock;

    @Autowired
    private EntityManager em;

    @Autowired
    private MockMvc restDispenseMockMvc;

    private Dispense dispense;

    private Dispense insertedDispense;

    /**
     * Create an entity for this test.
     *
     * This is a static method, as tests for other entities might also need it,
     * if they test an entity which requires the current entity.
     */
    public static Dispense createEntity(EntityManager em) {
        Dispense dispense = new Dispense().dispensedAt(DEFAULT_DISPENSED_AT).note(DEFAULT_NOTE);
        // Add required entity
        Prescription prescription;
        if (TestUtil.findAll(em, Prescription.class).isEmpty()) {
            prescription = PrescriptionResourceIT.createEntity();
            em.persist(prescription);
            em.flush();
        } else {
            prescription = TestUtil.findAll(em, Prescription.class).get(0);
        }
        dispense.setPrescription(prescription);
        // Add required entity
        User user = UserResourceIT.createEntity();
        em.persist(user);
        em.flush();
        dispense.setRecordedBy(user);
        return dispense;
    }

    /**
     * Create an updated entity for this test.
     *
     * This is a static method, as tests for other entities might also need it,
     * if they test an entity which requires the current entity.
     */
    public static Dispense createUpdatedEntity(EntityManager em) {
        Dispense updatedDispense = new Dispense().dispensedAt(UPDATED_DISPENSED_AT).note(UPDATED_NOTE);
        // Add required entity
        Prescription prescription;
        if (TestUtil.findAll(em, Prescription.class).isEmpty()) {
            prescription = PrescriptionResourceIT.createUpdatedEntity();
            em.persist(prescription);
            em.flush();
        } else {
            prescription = TestUtil.findAll(em, Prescription.class).get(0);
        }
        updatedDispense.setPrescription(prescription);
        // Add required entity
        User user = UserResourceIT.createEntity();
        em.persist(user);
        em.flush();
        updatedDispense.setRecordedBy(user);
        return updatedDispense;
    }

    @BeforeEach
    void initTest() {
        dispense = createEntity(em);
    }

    @AfterEach
    void cleanup() {
        if (insertedDispense != null) {
            dispenseRepository.delete(insertedDispense);
            insertedDispense = null;
        }
    }

    @Test
    @Transactional
    void createDispense() throws Exception {
        long databaseSizeBeforeCreate = getRepositoryCount();
        // Create the Dispense
        DispenseDTO dispenseDTO = dispenseMapper.toDto(dispense);
        var returnedDispenseDTO = om.readValue(
            restDispenseMockMvc
                .perform(post(ENTITY_API_URL).contentType(MediaType.APPLICATION_JSON).content(om.writeValueAsBytes(dispenseDTO)))
                .andExpect(status().isCreated())
                .andReturn()
                .getResponse()
                .getContentAsString(),
            DispenseDTO.class
        );

        // Validate the Dispense in the database
        assertIncrementedRepositoryCount(databaseSizeBeforeCreate);
        var returnedDispense = dispenseMapper.toEntity(returnedDispenseDTO);
        assertDispenseUpdatableFieldsEquals(returnedDispense, getPersistedDispense(returnedDispense));

        insertedDispense = returnedDispense;
    }

    @Test
    @Transactional
    void createDispenseWithExistingId() throws Exception {
        // Create the Dispense with an existing ID
        dispense.setId(1L);
        DispenseDTO dispenseDTO = dispenseMapper.toDto(dispense);

        long databaseSizeBeforeCreate = getRepositoryCount();

        // An entity with an existing ID cannot be created, so this API call must fail
        restDispenseMockMvc
            .perform(post(ENTITY_API_URL).contentType(MediaType.APPLICATION_JSON).content(om.writeValueAsBytes(dispenseDTO)))
            .andExpect(status().isBadRequest());

        // Validate the Dispense in the database
        assertSameRepositoryCount(databaseSizeBeforeCreate);
    }

    @Test
    @Transactional
    void checkDispensedAtIsRequired() throws Exception {
        long databaseSizeBeforeTest = getRepositoryCount();
        // set the field null
        dispense.setDispensedAt(null);

        // Create the Dispense, which fails.
        DispenseDTO dispenseDTO = dispenseMapper.toDto(dispense);

        restDispenseMockMvc
            .perform(post(ENTITY_API_URL).contentType(MediaType.APPLICATION_JSON).content(om.writeValueAsBytes(dispenseDTO)))
            .andExpect(status().isBadRequest());

        assertSameRepositoryCount(databaseSizeBeforeTest);
    }

    @Test
    @Transactional
    void getAllDispenses() throws Exception {
        // Initialize the database
        insertedDispense = dispenseRepository.saveAndFlush(dispense);

        // Get all the dispenseList
        restDispenseMockMvc
            .perform(get(ENTITY_API_URL + "?sort=id,desc"))
            .andExpect(status().isOk())
            .andExpect(content().contentType(MediaType.APPLICATION_JSON_VALUE))
            .andExpect(jsonPath("$.[*].id").value(hasItem(dispense.getId().intValue())))
            .andExpect(jsonPath("$.[*].dispensedAt").value(hasItem(DEFAULT_DISPENSED_AT.toString())))
            .andExpect(jsonPath("$.[*].note").value(hasItem(DEFAULT_NOTE)));
    }

    @SuppressWarnings({ "unchecked" })
    void getAllDispensesWithEagerRelationshipsIsEnabled() throws Exception {
        when(dispenseServiceMock.findAllWithEagerRelationships(any())).thenReturn(new PageImpl(new ArrayList<>()));

        restDispenseMockMvc.perform(get(ENTITY_API_URL + "?eagerload=true")).andExpect(status().isOk());

        verify(dispenseServiceMock, times(1)).findAllWithEagerRelationships(any());
    }

    @SuppressWarnings({ "unchecked" })
    void getAllDispensesWithEagerRelationshipsIsNotEnabled() throws Exception {
        when(dispenseServiceMock.findAllWithEagerRelationships(any())).thenReturn(new PageImpl(new ArrayList<>()));

        restDispenseMockMvc.perform(get(ENTITY_API_URL + "?eagerload=false")).andExpect(status().isOk());
        verify(dispenseRepositoryMock, times(1)).findAll(any(Pageable.class));
    }

    @Test
    @Transactional
    void getDispense() throws Exception {
        // Initialize the database
        insertedDispense = dispenseRepository.saveAndFlush(dispense);

        // Get the dispense
        restDispenseMockMvc
            .perform(get(ENTITY_API_URL_ID, dispense.getId()))
            .andExpect(status().isOk())
            .andExpect(content().contentType(MediaType.APPLICATION_JSON_VALUE))
            .andExpect(jsonPath("$.id").value(dispense.getId().intValue()))
            .andExpect(jsonPath("$.dispensedAt").value(DEFAULT_DISPENSED_AT.toString()))
            .andExpect(jsonPath("$.note").value(DEFAULT_NOTE));
    }

    @Test
    @Transactional
    void getNonExistingDispense() throws Exception {
        // Get the dispense
        restDispenseMockMvc.perform(get(ENTITY_API_URL_ID, Long.MAX_VALUE)).andExpect(status().isNotFound());
    }

    @Test
    @Transactional
    void putExistingDispense() throws Exception {
        // Initialize the database
        insertedDispense = dispenseRepository.saveAndFlush(dispense);

        long databaseSizeBeforeUpdate = getRepositoryCount();

        // Update the dispense
        Dispense updatedDispense = dispenseRepository.findById(dispense.getId()).orElseThrow();
        // Disconnect from session so that the updates on updatedDispense are not directly saved in db
        em.detach(updatedDispense);
        updatedDispense.dispensedAt(UPDATED_DISPENSED_AT).note(UPDATED_NOTE);
        DispenseDTO dispenseDTO = dispenseMapper.toDto(updatedDispense);

        restDispenseMockMvc
            .perform(
                put(ENTITY_API_URL_ID, dispenseDTO.getId())
                    .contentType(MediaType.APPLICATION_JSON)
                    .content(om.writeValueAsBytes(dispenseDTO))
            )
            .andExpect(status().isOk());

        // Validate the Dispense in the database
        assertSameRepositoryCount(databaseSizeBeforeUpdate);
        assertPersistedDispenseToMatchAllProperties(updatedDispense);
    }

    @Test
    @Transactional
    void putNonExistingDispense() throws Exception {
        long databaseSizeBeforeUpdate = getRepositoryCount();
        dispense.setId(longCount.incrementAndGet());

        // Create the Dispense
        DispenseDTO dispenseDTO = dispenseMapper.toDto(dispense);

        // If the entity doesn't have an ID, it will throw BadRequestAlertException
        restDispenseMockMvc
            .perform(
                put(ENTITY_API_URL_ID, dispenseDTO.getId())
                    .contentType(MediaType.APPLICATION_JSON)
                    .content(om.writeValueAsBytes(dispenseDTO))
            )
            .andExpect(status().isBadRequest());

        // Validate the Dispense in the database
        assertSameRepositoryCount(databaseSizeBeforeUpdate);
    }

    @Test
    @Transactional
    void putWithIdMismatchDispense() throws Exception {
        long databaseSizeBeforeUpdate = getRepositoryCount();
        dispense.setId(longCount.incrementAndGet());

        // Create the Dispense
        DispenseDTO dispenseDTO = dispenseMapper.toDto(dispense);

        // If url ID doesn't match entity ID, it will throw BadRequestAlertException
        restDispenseMockMvc
            .perform(
                put(ENTITY_API_URL_ID, longCount.incrementAndGet())
                    .contentType(MediaType.APPLICATION_JSON)
                    .content(om.writeValueAsBytes(dispenseDTO))
            )
            .andExpect(status().isBadRequest());

        // Validate the Dispense in the database
        assertSameRepositoryCount(databaseSizeBeforeUpdate);
    }

    @Test
    @Transactional
    void putWithMissingIdPathParamDispense() throws Exception {
        long databaseSizeBeforeUpdate = getRepositoryCount();
        dispense.setId(longCount.incrementAndGet());

        // Create the Dispense
        DispenseDTO dispenseDTO = dispenseMapper.toDto(dispense);

        // If url ID doesn't match entity ID, it will throw BadRequestAlertException
        restDispenseMockMvc
            .perform(put(ENTITY_API_URL).contentType(MediaType.APPLICATION_JSON).content(om.writeValueAsBytes(dispenseDTO)))
            .andExpect(status().isMethodNotAllowed());

        // Validate the Dispense in the database
        assertSameRepositoryCount(databaseSizeBeforeUpdate);
    }

    @Test
    @Transactional
    void partialUpdateDispenseWithPatch() throws Exception {
        // Initialize the database
        insertedDispense = dispenseRepository.saveAndFlush(dispense);

        long databaseSizeBeforeUpdate = getRepositoryCount();

        // Update the dispense using partial update
        Dispense partialUpdatedDispense = new Dispense();
        partialUpdatedDispense.setId(dispense.getId());

        restDispenseMockMvc
            .perform(
                patch(ENTITY_API_URL_ID, partialUpdatedDispense.getId())
                    .contentType("application/merge-patch+json")
                    .content(om.writeValueAsBytes(partialUpdatedDispense))
            )
            .andExpect(status().isOk());

        // Validate the Dispense in the database

        assertSameRepositoryCount(databaseSizeBeforeUpdate);
        assertDispenseUpdatableFieldsEquals(createUpdateProxyForBean(partialUpdatedDispense, dispense), getPersistedDispense(dispense));
    }

    @Test
    @Transactional
    void fullUpdateDispenseWithPatch() throws Exception {
        // Initialize the database
        insertedDispense = dispenseRepository.saveAndFlush(dispense);

        long databaseSizeBeforeUpdate = getRepositoryCount();

        // Update the dispense using partial update
        Dispense partialUpdatedDispense = new Dispense();
        partialUpdatedDispense.setId(dispense.getId());

        partialUpdatedDispense.dispensedAt(UPDATED_DISPENSED_AT).note(UPDATED_NOTE);

        restDispenseMockMvc
            .perform(
                patch(ENTITY_API_URL_ID, partialUpdatedDispense.getId())
                    .contentType("application/merge-patch+json")
                    .content(om.writeValueAsBytes(partialUpdatedDispense))
            )
            .andExpect(status().isOk());

        // Validate the Dispense in the database

        assertSameRepositoryCount(databaseSizeBeforeUpdate);
        assertDispenseUpdatableFieldsEquals(partialUpdatedDispense, getPersistedDispense(partialUpdatedDispense));
    }

    @Test
    @Transactional
    void patchNonExistingDispense() throws Exception {
        long databaseSizeBeforeUpdate = getRepositoryCount();
        dispense.setId(longCount.incrementAndGet());

        // Create the Dispense
        DispenseDTO dispenseDTO = dispenseMapper.toDto(dispense);

        // If the entity doesn't have an ID, it will throw BadRequestAlertException
        restDispenseMockMvc
            .perform(
                patch(ENTITY_API_URL_ID, dispenseDTO.getId())
                    .contentType("application/merge-patch+json")
                    .content(om.writeValueAsBytes(dispenseDTO))
            )
            .andExpect(status().isBadRequest());

        // Validate the Dispense in the database
        assertSameRepositoryCount(databaseSizeBeforeUpdate);
    }

    @Test
    @Transactional
    void patchWithIdMismatchDispense() throws Exception {
        long databaseSizeBeforeUpdate = getRepositoryCount();
        dispense.setId(longCount.incrementAndGet());

        // Create the Dispense
        DispenseDTO dispenseDTO = dispenseMapper.toDto(dispense);

        // If url ID doesn't match entity ID, it will throw BadRequestAlertException
        restDispenseMockMvc
            .perform(
                patch(ENTITY_API_URL_ID, longCount.incrementAndGet())
                    .contentType("application/merge-patch+json")
                    .content(om.writeValueAsBytes(dispenseDTO))
            )
            .andExpect(status().isBadRequest());

        // Validate the Dispense in the database
        assertSameRepositoryCount(databaseSizeBeforeUpdate);
    }

    @Test
    @Transactional
    void patchWithMissingIdPathParamDispense() throws Exception {
        long databaseSizeBeforeUpdate = getRepositoryCount();
        dispense.setId(longCount.incrementAndGet());

        // Create the Dispense
        DispenseDTO dispenseDTO = dispenseMapper.toDto(dispense);

        // If url ID doesn't match entity ID, it will throw BadRequestAlertException
        restDispenseMockMvc
            .perform(patch(ENTITY_API_URL).contentType("application/merge-patch+json").content(om.writeValueAsBytes(dispenseDTO)))
            .andExpect(status().isMethodNotAllowed());

        // Validate the Dispense in the database
        assertSameRepositoryCount(databaseSizeBeforeUpdate);
    }

    @Test
    @Transactional
    void deleteDispense() throws Exception {
        // Initialize the database
        insertedDispense = dispenseRepository.saveAndFlush(dispense);

        long databaseSizeBeforeDelete = getRepositoryCount();

        // Delete the dispense
        restDispenseMockMvc
            .perform(delete(ENTITY_API_URL_ID, dispense.getId()).accept(MediaType.APPLICATION_JSON))
            .andExpect(status().isNoContent());

        // Validate the database contains one less item
        assertDecrementedRepositoryCount(databaseSizeBeforeDelete);
    }

    protected long getRepositoryCount() {
        return dispenseRepository.count();
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

    protected Dispense getPersistedDispense(Dispense dispense) {
        return dispenseRepository.findById(dispense.getId()).orElseThrow();
    }

    protected void assertPersistedDispenseToMatchAllProperties(Dispense expectedDispense) {
        assertDispenseAllPropertiesEquals(expectedDispense, getPersistedDispense(expectedDispense));
    }

    protected void assertPersistedDispenseToMatchUpdatableProperties(Dispense expectedDispense) {
        assertDispenseAllUpdatablePropertiesEquals(expectedDispense, getPersistedDispense(expectedDispense));
    }
}
