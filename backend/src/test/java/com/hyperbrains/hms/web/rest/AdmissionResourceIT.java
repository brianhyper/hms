package com.hyperbrains.hms.web.rest;

import static com.hyperbrains.hms.domain.AdmissionAsserts.*;
import static com.hyperbrains.hms.web.rest.TestUtil.createUpdateProxyForBean;
import static org.assertj.core.api.Assertions.assertThat;
import static org.hamcrest.Matchers.hasItem;
import static org.mockito.Mockito.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.hyperbrains.hms.IntegrationTest;
import com.hyperbrains.hms.domain.Admission;
import com.hyperbrains.hms.domain.User;
import com.hyperbrains.hms.domain.Visit;
import com.hyperbrains.hms.domain.enumeration.AdmissionStatus;
import com.hyperbrains.hms.repository.AdmissionRepository;
import com.hyperbrains.hms.repository.UserRepository;
import com.hyperbrains.hms.service.AdmissionService;
import com.hyperbrains.hms.service.dto.AdmissionDTO;
import com.hyperbrains.hms.service.mapper.AdmissionMapper;
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
 * Integration tests for the {@link AdmissionResource} REST controller.
 */
@IntegrationTest
@ExtendWith(MockitoExtension.class)
@AutoConfigureMockMvc
@WithMockUser
class AdmissionResourceIT {

    private static final Instant DEFAULT_ADMITTED_AT = Instant.ofEpochMilli(0L);
    private static final Instant UPDATED_ADMITTED_AT = Instant.ofEpochMilli(1790236635213L);

    private static final String DEFAULT_ADMISSION_REASON = "AAAAAAAAAA";
    private static final String UPDATED_ADMISSION_REASON = "BBBBBBBBBB";

    private static final AdmissionStatus DEFAULT_STATUS = AdmissionStatus.PENDING_BED;
    private static final AdmissionStatus UPDATED_STATUS = AdmissionStatus.ADMITTED;

    private static final Instant DEFAULT_DISCHARGED_AT = Instant.ofEpochMilli(0L);
    private static final Instant UPDATED_DISCHARGED_AT = Instant.ofEpochMilli(1790236635213L);

    private static final String DEFAULT_DISCHARGE_NOTE = "AAAAAAAAAA";
    private static final String UPDATED_DISCHARGE_NOTE = "BBBBBBBBBB";

    private static final String ENTITY_API_URL = "/api/admissions";
    private static final String ENTITY_API_URL_ID = ENTITY_API_URL + "/{id}";

    private static final Random random = new Random();
    private static final AtomicLong longCount = new AtomicLong(random.nextInt() + 2L * Integer.MAX_VALUE);

    @Autowired
    private ObjectMapper om;

    @Autowired
    private AdmissionRepository admissionRepository;

    @Autowired
    private UserRepository userRepository;

    @Mock
    private AdmissionRepository admissionRepositoryMock;

    @Autowired
    private AdmissionMapper admissionMapper;

    @Mock
    private AdmissionService admissionServiceMock;

    @Autowired
    private EntityManager em;

    @Autowired
    private MockMvc restAdmissionMockMvc;

    private Admission admission;

    private Admission insertedAdmission;

    /**
     * Create an entity for this test.
     *
     * This is a static method, as tests for other entities might also need it,
     * if they test an entity which requires the current entity.
     */
    public static Admission createEntity(EntityManager em) {
        Admission admission = new Admission()
            .admittedAt(DEFAULT_ADMITTED_AT)
            .admissionReason(DEFAULT_ADMISSION_REASON)
            .status(DEFAULT_STATUS)
            .dischargedAt(DEFAULT_DISCHARGED_AT)
            .dischargeNote(DEFAULT_DISCHARGE_NOTE);
        // Add required entity
        Visit visit;
        if (TestUtil.findAll(em, Visit.class).isEmpty()) {
            visit = VisitResourceIT.createEntity(em);
            em.persist(visit);
            em.flush();
        } else {
            visit = TestUtil.findAll(em, Visit.class).get(0);
        }
        admission.setVisit(visit);
        // Add required entity
        User user = UserResourceIT.createEntity();
        em.persist(user);
        em.flush();
        admission.setAdmittingDoctor(user);
        // Add required entity
        admission.setPrimaryDoctor(user);
        return admission;
    }

    /**
     * Create an updated entity for this test.
     *
     * This is a static method, as tests for other entities might also need it,
     * if they test an entity which requires the current entity.
     */
    public static Admission createUpdatedEntity(EntityManager em) {
        Admission updatedAdmission = new Admission()
            .admittedAt(UPDATED_ADMITTED_AT)
            .admissionReason(UPDATED_ADMISSION_REASON)
            .status(UPDATED_STATUS)
            .dischargedAt(UPDATED_DISCHARGED_AT)
            .dischargeNote(UPDATED_DISCHARGE_NOTE);
        // Add required entity
        Visit visit;
        if (TestUtil.findAll(em, Visit.class).isEmpty()) {
            visit = VisitResourceIT.createUpdatedEntity(em);
            em.persist(visit);
            em.flush();
        } else {
            visit = TestUtil.findAll(em, Visit.class).get(0);
        }
        updatedAdmission.setVisit(visit);
        // Add required entity
        User user = UserResourceIT.createEntity();
        em.persist(user);
        em.flush();
        updatedAdmission.setAdmittingDoctor(user);
        // Add required entity
        updatedAdmission.setPrimaryDoctor(user);
        return updatedAdmission;
    }

    @BeforeEach
    void initTest() {
        admission = createEntity(em);
    }

    @AfterEach
    void cleanup() {
        if (insertedAdmission != null) {
            admissionRepository.delete(insertedAdmission);
            insertedAdmission = null;
        }
    }

    @Test
    @Transactional
    void createAdmission() throws Exception {
        long databaseSizeBeforeCreate = getRepositoryCount();
        // Create the Admission
        AdmissionDTO admissionDTO = admissionMapper.toDto(admission);
        var returnedAdmissionDTO = om.readValue(
            restAdmissionMockMvc
                .perform(post(ENTITY_API_URL).contentType(MediaType.APPLICATION_JSON).content(om.writeValueAsBytes(admissionDTO)))
                .andExpect(status().isCreated())
                .andReturn()
                .getResponse()
                .getContentAsString(),
            AdmissionDTO.class
        );

        // Validate the Admission in the database
        assertIncrementedRepositoryCount(databaseSizeBeforeCreate);
        var returnedAdmission = admissionMapper.toEntity(returnedAdmissionDTO);
        assertAdmissionUpdatableFieldsEquals(returnedAdmission, getPersistedAdmission(returnedAdmission));

        insertedAdmission = returnedAdmission;
    }

    @Test
    @Transactional
    void createAdmissionWithExistingId() throws Exception {
        // Create the Admission with an existing ID
        admission.setId(1L);
        AdmissionDTO admissionDTO = admissionMapper.toDto(admission);

        long databaseSizeBeforeCreate = getRepositoryCount();

        // An entity with an existing ID cannot be created, so this API call must fail
        restAdmissionMockMvc
            .perform(post(ENTITY_API_URL).contentType(MediaType.APPLICATION_JSON).content(om.writeValueAsBytes(admissionDTO)))
            .andExpect(status().isBadRequest());

        // Validate the Admission in the database
        assertSameRepositoryCount(databaseSizeBeforeCreate);
    }

    @Test
    @Transactional
    void checkAdmittedAtIsRequired() throws Exception {
        long databaseSizeBeforeTest = getRepositoryCount();
        // set the field null
        admission.setAdmittedAt(null);

        // Create the Admission, which fails.
        AdmissionDTO admissionDTO = admissionMapper.toDto(admission);

        restAdmissionMockMvc
            .perform(post(ENTITY_API_URL).contentType(MediaType.APPLICATION_JSON).content(om.writeValueAsBytes(admissionDTO)))
            .andExpect(status().isBadRequest());

        assertSameRepositoryCount(databaseSizeBeforeTest);
    }

    @Test
    @Transactional
    void checkAdmissionReasonIsRequired() throws Exception {
        long databaseSizeBeforeTest = getRepositoryCount();
        // set the field null
        admission.setAdmissionReason(null);

        // Create the Admission, which fails.
        AdmissionDTO admissionDTO = admissionMapper.toDto(admission);

        restAdmissionMockMvc
            .perform(post(ENTITY_API_URL).contentType(MediaType.APPLICATION_JSON).content(om.writeValueAsBytes(admissionDTO)))
            .andExpect(status().isBadRequest());

        assertSameRepositoryCount(databaseSizeBeforeTest);
    }

    @Test
    @Transactional
    void checkStatusIsRequired() throws Exception {
        long databaseSizeBeforeTest = getRepositoryCount();
        // set the field null
        admission.setStatus(null);

        // Create the Admission, which fails.
        AdmissionDTO admissionDTO = admissionMapper.toDto(admission);

        restAdmissionMockMvc
            .perform(post(ENTITY_API_URL).contentType(MediaType.APPLICATION_JSON).content(om.writeValueAsBytes(admissionDTO)))
            .andExpect(status().isBadRequest());

        assertSameRepositoryCount(databaseSizeBeforeTest);
    }

    @Test
    @Transactional
    void getAllAdmissions() throws Exception {
        // Initialize the database
        insertedAdmission = admissionRepository.saveAndFlush(admission);

        // Get all the admissionList
        restAdmissionMockMvc
            .perform(get(ENTITY_API_URL + "?sort=id,desc"))
            .andExpect(status().isOk())
            .andExpect(content().contentType(MediaType.APPLICATION_JSON_VALUE))
            .andExpect(jsonPath("$.[*].id").value(hasItem(admission.getId().intValue())))
            .andExpect(jsonPath("$.[*].admittedAt").value(hasItem(DEFAULT_ADMITTED_AT.toString())))
            .andExpect(jsonPath("$.[*].admissionReason").value(hasItem(DEFAULT_ADMISSION_REASON)))
            .andExpect(jsonPath("$.[*].status").value(hasItem(DEFAULT_STATUS.toString())))
            .andExpect(jsonPath("$.[*].dischargedAt").value(hasItem(DEFAULT_DISCHARGED_AT.toString())))
            .andExpect(jsonPath("$.[*].dischargeNote").value(hasItem(DEFAULT_DISCHARGE_NOTE)));
    }

    @SuppressWarnings({ "unchecked" })
    void getAllAdmissionsWithEagerRelationshipsIsEnabled() throws Exception {
        when(admissionServiceMock.findAllWithEagerRelationships(any())).thenReturn(new PageImpl(new ArrayList<>()));

        restAdmissionMockMvc.perform(get(ENTITY_API_URL + "?eagerload=true")).andExpect(status().isOk());

        verify(admissionServiceMock, times(1)).findAllWithEagerRelationships(any());
    }

    @SuppressWarnings({ "unchecked" })
    void getAllAdmissionsWithEagerRelationshipsIsNotEnabled() throws Exception {
        when(admissionServiceMock.findAllWithEagerRelationships(any())).thenReturn(new PageImpl(new ArrayList<>()));

        restAdmissionMockMvc.perform(get(ENTITY_API_URL + "?eagerload=false")).andExpect(status().isOk());
        verify(admissionRepositoryMock, times(1)).findAll(any(Pageable.class));
    }

    @Test
    @Transactional
    void getAdmission() throws Exception {
        // Initialize the database
        insertedAdmission = admissionRepository.saveAndFlush(admission);

        // Get the admission
        restAdmissionMockMvc
            .perform(get(ENTITY_API_URL_ID, admission.getId()))
            .andExpect(status().isOk())
            .andExpect(content().contentType(MediaType.APPLICATION_JSON_VALUE))
            .andExpect(jsonPath("$.id").value(admission.getId().intValue()))
            .andExpect(jsonPath("$.admittedAt").value(DEFAULT_ADMITTED_AT.toString()))
            .andExpect(jsonPath("$.admissionReason").value(DEFAULT_ADMISSION_REASON))
            .andExpect(jsonPath("$.status").value(DEFAULT_STATUS.toString()))
            .andExpect(jsonPath("$.dischargedAt").value(DEFAULT_DISCHARGED_AT.toString()))
            .andExpect(jsonPath("$.dischargeNote").value(DEFAULT_DISCHARGE_NOTE));
    }

    @Test
    @Transactional
    void getNonExistingAdmission() throws Exception {
        // Get the admission
        restAdmissionMockMvc.perform(get(ENTITY_API_URL_ID, Long.MAX_VALUE)).andExpect(status().isNotFound());
    }

    @Test
    @Transactional
    void putExistingAdmission() throws Exception {
        // Initialize the database
        insertedAdmission = admissionRepository.saveAndFlush(admission);

        long databaseSizeBeforeUpdate = getRepositoryCount();

        // Update the admission
        Admission updatedAdmission = admissionRepository.findById(admission.getId()).orElseThrow();
        // Disconnect from session so that the updates on updatedAdmission are not directly saved in db
        em.detach(updatedAdmission);
        updatedAdmission
            .admittedAt(UPDATED_ADMITTED_AT)
            .admissionReason(UPDATED_ADMISSION_REASON)
            .status(UPDATED_STATUS)
            .dischargedAt(UPDATED_DISCHARGED_AT)
            .dischargeNote(UPDATED_DISCHARGE_NOTE);
        AdmissionDTO admissionDTO = admissionMapper.toDto(updatedAdmission);

        restAdmissionMockMvc
            .perform(
                put(ENTITY_API_URL_ID, admissionDTO.getId())
                    .contentType(MediaType.APPLICATION_JSON)
                    .content(om.writeValueAsBytes(admissionDTO))
            )
            .andExpect(status().isOk());

        // Validate the Admission in the database
        assertSameRepositoryCount(databaseSizeBeforeUpdate);
        assertPersistedAdmissionToMatchAllProperties(updatedAdmission);
    }

    @Test
    @Transactional
    void putNonExistingAdmission() throws Exception {
        long databaseSizeBeforeUpdate = getRepositoryCount();
        admission.setId(longCount.incrementAndGet());

        // Create the Admission
        AdmissionDTO admissionDTO = admissionMapper.toDto(admission);

        // If the entity doesn't have an ID, it will throw BadRequestAlertException
        restAdmissionMockMvc
            .perform(
                put(ENTITY_API_URL_ID, admissionDTO.getId())
                    .contentType(MediaType.APPLICATION_JSON)
                    .content(om.writeValueAsBytes(admissionDTO))
            )
            .andExpect(status().isBadRequest());

        // Validate the Admission in the database
        assertSameRepositoryCount(databaseSizeBeforeUpdate);
    }

    @Test
    @Transactional
    void putWithIdMismatchAdmission() throws Exception {
        long databaseSizeBeforeUpdate = getRepositoryCount();
        admission.setId(longCount.incrementAndGet());

        // Create the Admission
        AdmissionDTO admissionDTO = admissionMapper.toDto(admission);

        // If url ID doesn't match entity ID, it will throw BadRequestAlertException
        restAdmissionMockMvc
            .perform(
                put(ENTITY_API_URL_ID, longCount.incrementAndGet())
                    .contentType(MediaType.APPLICATION_JSON)
                    .content(om.writeValueAsBytes(admissionDTO))
            )
            .andExpect(status().isBadRequest());

        // Validate the Admission in the database
        assertSameRepositoryCount(databaseSizeBeforeUpdate);
    }

    @Test
    @Transactional
    void putWithMissingIdPathParamAdmission() throws Exception {
        long databaseSizeBeforeUpdate = getRepositoryCount();
        admission.setId(longCount.incrementAndGet());

        // Create the Admission
        AdmissionDTO admissionDTO = admissionMapper.toDto(admission);

        // If url ID doesn't match entity ID, it will throw BadRequestAlertException
        restAdmissionMockMvc
            .perform(put(ENTITY_API_URL).contentType(MediaType.APPLICATION_JSON).content(om.writeValueAsBytes(admissionDTO)))
            .andExpect(status().isMethodNotAllowed());

        // Validate the Admission in the database
        assertSameRepositoryCount(databaseSizeBeforeUpdate);
    }

    @Test
    @Transactional
    void partialUpdateAdmissionWithPatch() throws Exception {
        // Initialize the database
        insertedAdmission = admissionRepository.saveAndFlush(admission);

        long databaseSizeBeforeUpdate = getRepositoryCount();

        // Update the admission using partial update
        Admission partialUpdatedAdmission = new Admission();
        partialUpdatedAdmission.setId(admission.getId());

        partialUpdatedAdmission.status(UPDATED_STATUS).dischargedAt(UPDATED_DISCHARGED_AT);

        restAdmissionMockMvc
            .perform(
                patch(ENTITY_API_URL_ID, partialUpdatedAdmission.getId())
                    .contentType("application/merge-patch+json")
                    .content(om.writeValueAsBytes(partialUpdatedAdmission))
            )
            .andExpect(status().isOk());

        // Validate the Admission in the database

        assertSameRepositoryCount(databaseSizeBeforeUpdate);
        assertAdmissionUpdatableFieldsEquals(
            createUpdateProxyForBean(partialUpdatedAdmission, admission),
            getPersistedAdmission(admission)
        );
    }

    @Test
    @Transactional
    void fullUpdateAdmissionWithPatch() throws Exception {
        // Initialize the database
        insertedAdmission = admissionRepository.saveAndFlush(admission);

        long databaseSizeBeforeUpdate = getRepositoryCount();

        // Update the admission using partial update
        Admission partialUpdatedAdmission = new Admission();
        partialUpdatedAdmission.setId(admission.getId());

        partialUpdatedAdmission
            .admittedAt(UPDATED_ADMITTED_AT)
            .admissionReason(UPDATED_ADMISSION_REASON)
            .status(UPDATED_STATUS)
            .dischargedAt(UPDATED_DISCHARGED_AT)
            .dischargeNote(UPDATED_DISCHARGE_NOTE);

        restAdmissionMockMvc
            .perform(
                patch(ENTITY_API_URL_ID, partialUpdatedAdmission.getId())
                    .contentType("application/merge-patch+json")
                    .content(om.writeValueAsBytes(partialUpdatedAdmission))
            )
            .andExpect(status().isOk());

        // Validate the Admission in the database

        assertSameRepositoryCount(databaseSizeBeforeUpdate);
        assertAdmissionUpdatableFieldsEquals(partialUpdatedAdmission, getPersistedAdmission(partialUpdatedAdmission));
    }

    @Test
    @Transactional
    void patchNonExistingAdmission() throws Exception {
        long databaseSizeBeforeUpdate = getRepositoryCount();
        admission.setId(longCount.incrementAndGet());

        // Create the Admission
        AdmissionDTO admissionDTO = admissionMapper.toDto(admission);

        // If the entity doesn't have an ID, it will throw BadRequestAlertException
        restAdmissionMockMvc
            .perform(
                patch(ENTITY_API_URL_ID, admissionDTO.getId())
                    .contentType("application/merge-patch+json")
                    .content(om.writeValueAsBytes(admissionDTO))
            )
            .andExpect(status().isBadRequest());

        // Validate the Admission in the database
        assertSameRepositoryCount(databaseSizeBeforeUpdate);
    }

    @Test
    @Transactional
    void patchWithIdMismatchAdmission() throws Exception {
        long databaseSizeBeforeUpdate = getRepositoryCount();
        admission.setId(longCount.incrementAndGet());

        // Create the Admission
        AdmissionDTO admissionDTO = admissionMapper.toDto(admission);

        // If url ID doesn't match entity ID, it will throw BadRequestAlertException
        restAdmissionMockMvc
            .perform(
                patch(ENTITY_API_URL_ID, longCount.incrementAndGet())
                    .contentType("application/merge-patch+json")
                    .content(om.writeValueAsBytes(admissionDTO))
            )
            .andExpect(status().isBadRequest());

        // Validate the Admission in the database
        assertSameRepositoryCount(databaseSizeBeforeUpdate);
    }

    @Test
    @Transactional
    void patchWithMissingIdPathParamAdmission() throws Exception {
        long databaseSizeBeforeUpdate = getRepositoryCount();
        admission.setId(longCount.incrementAndGet());

        // Create the Admission
        AdmissionDTO admissionDTO = admissionMapper.toDto(admission);

        // If url ID doesn't match entity ID, it will throw BadRequestAlertException
        restAdmissionMockMvc
            .perform(patch(ENTITY_API_URL).contentType("application/merge-patch+json").content(om.writeValueAsBytes(admissionDTO)))
            .andExpect(status().isMethodNotAllowed());

        // Validate the Admission in the database
        assertSameRepositoryCount(databaseSizeBeforeUpdate);
    }

    @Test
    @Transactional
    void deleteAdmission() throws Exception {
        // Initialize the database
        insertedAdmission = admissionRepository.saveAndFlush(admission);

        long databaseSizeBeforeDelete = getRepositoryCount();

        // Delete the admission
        restAdmissionMockMvc
            .perform(delete(ENTITY_API_URL_ID, admission.getId()).accept(MediaType.APPLICATION_JSON))
            .andExpect(status().isNoContent());

        // Validate the database contains one less item
        assertDecrementedRepositoryCount(databaseSizeBeforeDelete);
    }

    protected long getRepositoryCount() {
        return admissionRepository.count();
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

    protected Admission getPersistedAdmission(Admission admission) {
        return admissionRepository.findById(admission.getId()).orElseThrow();
    }

    protected void assertPersistedAdmissionToMatchAllProperties(Admission expectedAdmission) {
        assertAdmissionAllPropertiesEquals(expectedAdmission, getPersistedAdmission(expectedAdmission));
    }

    protected void assertPersistedAdmissionToMatchUpdatableProperties(Admission expectedAdmission) {
        assertAdmissionAllUpdatablePropertiesEquals(expectedAdmission, getPersistedAdmission(expectedAdmission));
    }
}
