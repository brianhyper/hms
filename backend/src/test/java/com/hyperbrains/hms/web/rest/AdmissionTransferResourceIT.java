package com.hyperbrains.hms.web.rest;

import static com.hyperbrains.hms.domain.AdmissionTransferAsserts.*;
import static com.hyperbrains.hms.web.rest.TestUtil.createUpdateProxyForBean;
import static org.assertj.core.api.Assertions.assertThat;
import static org.hamcrest.Matchers.hasItem;
import static org.mockito.Mockito.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.hyperbrains.hms.IntegrationTest;
import com.hyperbrains.hms.domain.Admission;
import com.hyperbrains.hms.domain.AdmissionTransfer;
import com.hyperbrains.hms.domain.Bed;
import com.hyperbrains.hms.domain.User;
import com.hyperbrains.hms.repository.AdmissionTransferRepository;
import com.hyperbrains.hms.repository.UserRepository;
import com.hyperbrains.hms.service.AdmissionTransferService;
import com.hyperbrains.hms.service.dto.AdmissionTransferDTO;
import com.hyperbrains.hms.service.mapper.AdmissionTransferMapper;
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
 * Integration tests for the {@link AdmissionTransferResource} REST controller.
 */
@IntegrationTest
@ExtendWith(MockitoExtension.class)
@AutoConfigureMockMvc
@WithMockUser
class AdmissionTransferResourceIT {

    private static final Instant DEFAULT_TRANSFERRED_AT = Instant.ofEpochMilli(0L);
    private static final Instant UPDATED_TRANSFERRED_AT = Instant.ofEpochMilli(1790236635213L);

    private static final String DEFAULT_REASON = "AAAAAAAAAA";
    private static final String UPDATED_REASON = "BBBBBBBBBB";

    private static final String ENTITY_API_URL = "/api/admission-transfers";
    private static final String ENTITY_API_URL_ID = ENTITY_API_URL + "/{id}";

    private static final Random random = new Random();
    private static final AtomicLong longCount = new AtomicLong(random.nextInt() + 2L * Integer.MAX_VALUE);

    @Autowired
    private ObjectMapper om;

    @Autowired
    private AdmissionTransferRepository admissionTransferRepository;

    @Autowired
    private UserRepository userRepository;

    @Mock
    private AdmissionTransferRepository admissionTransferRepositoryMock;

    @Autowired
    private AdmissionTransferMapper admissionTransferMapper;

    @Mock
    private AdmissionTransferService admissionTransferServiceMock;

    @Autowired
    private EntityManager em;

    @Autowired
    private MockMvc restAdmissionTransferMockMvc;

    private AdmissionTransfer admissionTransfer;

    private AdmissionTransfer insertedAdmissionTransfer;

    /**
     * Create an entity for this test.
     *
     * This is a static method, as tests for other entities might also need it,
     * if they test an entity which requires the current entity.
     */
    public static AdmissionTransfer createEntity(EntityManager em) {
        AdmissionTransfer admissionTransfer = new AdmissionTransfer().transferredAt(DEFAULT_TRANSFERRED_AT).reason(DEFAULT_REASON);
        // Add required entity
        Admission admission;
        if (TestUtil.findAll(em, Admission.class).isEmpty()) {
            admission = AdmissionResourceIT.createEntity(em);
            em.persist(admission);
            em.flush();
        } else {
            admission = TestUtil.findAll(em, Admission.class).get(0);
        }
        admissionTransfer.setAdmission(admission);
        // Add required entity
        Bed bed;
        if (TestUtil.findAll(em, Bed.class).isEmpty()) {
            bed = BedResourceIT.createEntity(em);
            em.persist(bed);
            em.flush();
        } else {
            bed = TestUtil.findAll(em, Bed.class).get(0);
        }
        admissionTransfer.setToBed(bed);
        // Add required entity
        User user = UserResourceIT.createEntity();
        em.persist(user);
        em.flush();
        admissionTransfer.setTransferredBy(user);
        return admissionTransfer;
    }

    /**
     * Create an updated entity for this test.
     *
     * This is a static method, as tests for other entities might also need it,
     * if they test an entity which requires the current entity.
     */
    public static AdmissionTransfer createUpdatedEntity(EntityManager em) {
        AdmissionTransfer updatedAdmissionTransfer = new AdmissionTransfer().transferredAt(UPDATED_TRANSFERRED_AT).reason(UPDATED_REASON);
        // Add required entity
        Admission admission;
        if (TestUtil.findAll(em, Admission.class).isEmpty()) {
            admission = AdmissionResourceIT.createUpdatedEntity(em);
            em.persist(admission);
            em.flush();
        } else {
            admission = TestUtil.findAll(em, Admission.class).get(0);
        }
        updatedAdmissionTransfer.setAdmission(admission);
        // Add required entity
        Bed bed;
        if (TestUtil.findAll(em, Bed.class).isEmpty()) {
            bed = BedResourceIT.createUpdatedEntity(em);
            em.persist(bed);
            em.flush();
        } else {
            bed = TestUtil.findAll(em, Bed.class).get(0);
        }
        updatedAdmissionTransfer.setToBed(bed);
        // Add required entity
        User user = UserResourceIT.createEntity();
        em.persist(user);
        em.flush();
        updatedAdmissionTransfer.setTransferredBy(user);
        return updatedAdmissionTransfer;
    }

    @BeforeEach
    void initTest() {
        admissionTransfer = createEntity(em);
    }

    @AfterEach
    void cleanup() {
        if (insertedAdmissionTransfer != null) {
            admissionTransferRepository.delete(insertedAdmissionTransfer);
            insertedAdmissionTransfer = null;
        }
    }

    @Test
    @Transactional
    void createAdmissionTransfer() throws Exception {
        long databaseSizeBeforeCreate = getRepositoryCount();
        // Create the AdmissionTransfer
        AdmissionTransferDTO admissionTransferDTO = admissionTransferMapper.toDto(admissionTransfer);
        var returnedAdmissionTransferDTO = om.readValue(
            restAdmissionTransferMockMvc
                .perform(post(ENTITY_API_URL).contentType(MediaType.APPLICATION_JSON).content(om.writeValueAsBytes(admissionTransferDTO)))
                .andExpect(status().isCreated())
                .andReturn()
                .getResponse()
                .getContentAsString(),
            AdmissionTransferDTO.class
        );

        // Validate the AdmissionTransfer in the database
        assertIncrementedRepositoryCount(databaseSizeBeforeCreate);
        var returnedAdmissionTransfer = admissionTransferMapper.toEntity(returnedAdmissionTransferDTO);
        assertAdmissionTransferUpdatableFieldsEquals(returnedAdmissionTransfer, getPersistedAdmissionTransfer(returnedAdmissionTransfer));

        insertedAdmissionTransfer = returnedAdmissionTransfer;
    }

    @Test
    @Transactional
    void createAdmissionTransferWithExistingId() throws Exception {
        // Create the AdmissionTransfer with an existing ID
        admissionTransfer.setId(1L);
        AdmissionTransferDTO admissionTransferDTO = admissionTransferMapper.toDto(admissionTransfer);

        long databaseSizeBeforeCreate = getRepositoryCount();

        // An entity with an existing ID cannot be created, so this API call must fail
        restAdmissionTransferMockMvc
            .perform(post(ENTITY_API_URL).contentType(MediaType.APPLICATION_JSON).content(om.writeValueAsBytes(admissionTransferDTO)))
            .andExpect(status().isBadRequest());

        // Validate the AdmissionTransfer in the database
        assertSameRepositoryCount(databaseSizeBeforeCreate);
    }

    @Test
    @Transactional
    void checkTransferredAtIsRequired() throws Exception {
        long databaseSizeBeforeTest = getRepositoryCount();
        // set the field null
        admissionTransfer.setTransferredAt(null);

        // Create the AdmissionTransfer, which fails.
        AdmissionTransferDTO admissionTransferDTO = admissionTransferMapper.toDto(admissionTransfer);

        restAdmissionTransferMockMvc
            .perform(post(ENTITY_API_URL).contentType(MediaType.APPLICATION_JSON).content(om.writeValueAsBytes(admissionTransferDTO)))
            .andExpect(status().isBadRequest());

        assertSameRepositoryCount(databaseSizeBeforeTest);
    }

    @Test
    @Transactional
    void checkReasonIsRequired() throws Exception {
        long databaseSizeBeforeTest = getRepositoryCount();
        // set the field null
        admissionTransfer.setReason(null);

        // Create the AdmissionTransfer, which fails.
        AdmissionTransferDTO admissionTransferDTO = admissionTransferMapper.toDto(admissionTransfer);

        restAdmissionTransferMockMvc
            .perform(post(ENTITY_API_URL).contentType(MediaType.APPLICATION_JSON).content(om.writeValueAsBytes(admissionTransferDTO)))
            .andExpect(status().isBadRequest());

        assertSameRepositoryCount(databaseSizeBeforeTest);
    }

    @Test
    @Transactional
    void getAllAdmissionTransfers() throws Exception {
        // Initialize the database
        insertedAdmissionTransfer = admissionTransferRepository.saveAndFlush(admissionTransfer);

        // Get all the admissionTransferList
        restAdmissionTransferMockMvc
            .perform(get(ENTITY_API_URL + "?sort=id,desc"))
            .andExpect(status().isOk())
            .andExpect(content().contentType(MediaType.APPLICATION_JSON_VALUE))
            .andExpect(jsonPath("$.[*].id").value(hasItem(admissionTransfer.getId().intValue())))
            .andExpect(jsonPath("$.[*].transferredAt").value(hasItem(DEFAULT_TRANSFERRED_AT.toString())))
            .andExpect(jsonPath("$.[*].reason").value(hasItem(DEFAULT_REASON)));
    }

    @SuppressWarnings({ "unchecked" })
    void getAllAdmissionTransfersWithEagerRelationshipsIsEnabled() throws Exception {
        when(admissionTransferServiceMock.findAllWithEagerRelationships(any())).thenReturn(new PageImpl(new ArrayList<>()));

        restAdmissionTransferMockMvc.perform(get(ENTITY_API_URL + "?eagerload=true")).andExpect(status().isOk());

        verify(admissionTransferServiceMock, times(1)).findAllWithEagerRelationships(any());
    }

    @SuppressWarnings({ "unchecked" })
    void getAllAdmissionTransfersWithEagerRelationshipsIsNotEnabled() throws Exception {
        when(admissionTransferServiceMock.findAllWithEagerRelationships(any())).thenReturn(new PageImpl(new ArrayList<>()));

        restAdmissionTransferMockMvc.perform(get(ENTITY_API_URL + "?eagerload=false")).andExpect(status().isOk());
        verify(admissionTransferRepositoryMock, times(1)).findAll(any(Pageable.class));
    }

    @Test
    @Transactional
    void getAdmissionTransfer() throws Exception {
        // Initialize the database
        insertedAdmissionTransfer = admissionTransferRepository.saveAndFlush(admissionTransfer);

        // Get the admissionTransfer
        restAdmissionTransferMockMvc
            .perform(get(ENTITY_API_URL_ID, admissionTransfer.getId()))
            .andExpect(status().isOk())
            .andExpect(content().contentType(MediaType.APPLICATION_JSON_VALUE))
            .andExpect(jsonPath("$.id").value(admissionTransfer.getId().intValue()))
            .andExpect(jsonPath("$.transferredAt").value(DEFAULT_TRANSFERRED_AT.toString()))
            .andExpect(jsonPath("$.reason").value(DEFAULT_REASON));
    }

    @Test
    @Transactional
    void getNonExistingAdmissionTransfer() throws Exception {
        // Get the admissionTransfer
        restAdmissionTransferMockMvc.perform(get(ENTITY_API_URL_ID, Long.MAX_VALUE)).andExpect(status().isNotFound());
    }

    @Test
    @Transactional
    void putExistingAdmissionTransfer() throws Exception {
        // Initialize the database
        insertedAdmissionTransfer = admissionTransferRepository.saveAndFlush(admissionTransfer);

        long databaseSizeBeforeUpdate = getRepositoryCount();

        // Update the admissionTransfer
        AdmissionTransfer updatedAdmissionTransfer = admissionTransferRepository.findById(admissionTransfer.getId()).orElseThrow();
        // Disconnect from session so that the updates on updatedAdmissionTransfer are not directly saved in db
        em.detach(updatedAdmissionTransfer);
        updatedAdmissionTransfer.transferredAt(UPDATED_TRANSFERRED_AT).reason(UPDATED_REASON);
        AdmissionTransferDTO admissionTransferDTO = admissionTransferMapper.toDto(updatedAdmissionTransfer);

        restAdmissionTransferMockMvc
            .perform(
                put(ENTITY_API_URL_ID, admissionTransferDTO.getId())
                    .contentType(MediaType.APPLICATION_JSON)
                    .content(om.writeValueAsBytes(admissionTransferDTO))
            )
            .andExpect(status().isOk());

        // Validate the AdmissionTransfer in the database
        assertSameRepositoryCount(databaseSizeBeforeUpdate);
        assertPersistedAdmissionTransferToMatchAllProperties(updatedAdmissionTransfer);
    }

    @Test
    @Transactional
    void putNonExistingAdmissionTransfer() throws Exception {
        long databaseSizeBeforeUpdate = getRepositoryCount();
        admissionTransfer.setId(longCount.incrementAndGet());

        // Create the AdmissionTransfer
        AdmissionTransferDTO admissionTransferDTO = admissionTransferMapper.toDto(admissionTransfer);

        // If the entity doesn't have an ID, it will throw BadRequestAlertException
        restAdmissionTransferMockMvc
            .perform(
                put(ENTITY_API_URL_ID, admissionTransferDTO.getId())
                    .contentType(MediaType.APPLICATION_JSON)
                    .content(om.writeValueAsBytes(admissionTransferDTO))
            )
            .andExpect(status().isBadRequest());

        // Validate the AdmissionTransfer in the database
        assertSameRepositoryCount(databaseSizeBeforeUpdate);
    }

    @Test
    @Transactional
    void putWithIdMismatchAdmissionTransfer() throws Exception {
        long databaseSizeBeforeUpdate = getRepositoryCount();
        admissionTransfer.setId(longCount.incrementAndGet());

        // Create the AdmissionTransfer
        AdmissionTransferDTO admissionTransferDTO = admissionTransferMapper.toDto(admissionTransfer);

        // If url ID doesn't match entity ID, it will throw BadRequestAlertException
        restAdmissionTransferMockMvc
            .perform(
                put(ENTITY_API_URL_ID, longCount.incrementAndGet())
                    .contentType(MediaType.APPLICATION_JSON)
                    .content(om.writeValueAsBytes(admissionTransferDTO))
            )
            .andExpect(status().isBadRequest());

        // Validate the AdmissionTransfer in the database
        assertSameRepositoryCount(databaseSizeBeforeUpdate);
    }

    @Test
    @Transactional
    void putWithMissingIdPathParamAdmissionTransfer() throws Exception {
        long databaseSizeBeforeUpdate = getRepositoryCount();
        admissionTransfer.setId(longCount.incrementAndGet());

        // Create the AdmissionTransfer
        AdmissionTransferDTO admissionTransferDTO = admissionTransferMapper.toDto(admissionTransfer);

        // If url ID doesn't match entity ID, it will throw BadRequestAlertException
        restAdmissionTransferMockMvc
            .perform(put(ENTITY_API_URL).contentType(MediaType.APPLICATION_JSON).content(om.writeValueAsBytes(admissionTransferDTO)))
            .andExpect(status().isMethodNotAllowed());

        // Validate the AdmissionTransfer in the database
        assertSameRepositoryCount(databaseSizeBeforeUpdate);
    }

    @Test
    @Transactional
    void partialUpdateAdmissionTransferWithPatch() throws Exception {
        // Initialize the database
        insertedAdmissionTransfer = admissionTransferRepository.saveAndFlush(admissionTransfer);

        long databaseSizeBeforeUpdate = getRepositoryCount();

        // Update the admissionTransfer using partial update
        AdmissionTransfer partialUpdatedAdmissionTransfer = new AdmissionTransfer();
        partialUpdatedAdmissionTransfer.setId(admissionTransfer.getId());

        partialUpdatedAdmissionTransfer.reason(UPDATED_REASON);

        restAdmissionTransferMockMvc
            .perform(
                patch(ENTITY_API_URL_ID, partialUpdatedAdmissionTransfer.getId())
                    .contentType("application/merge-patch+json")
                    .content(om.writeValueAsBytes(partialUpdatedAdmissionTransfer))
            )
            .andExpect(status().isOk());

        // Validate the AdmissionTransfer in the database

        assertSameRepositoryCount(databaseSizeBeforeUpdate);
        assertAdmissionTransferUpdatableFieldsEquals(
            createUpdateProxyForBean(partialUpdatedAdmissionTransfer, admissionTransfer),
            getPersistedAdmissionTransfer(admissionTransfer)
        );
    }

    @Test
    @Transactional
    void fullUpdateAdmissionTransferWithPatch() throws Exception {
        // Initialize the database
        insertedAdmissionTransfer = admissionTransferRepository.saveAndFlush(admissionTransfer);

        long databaseSizeBeforeUpdate = getRepositoryCount();

        // Update the admissionTransfer using partial update
        AdmissionTransfer partialUpdatedAdmissionTransfer = new AdmissionTransfer();
        partialUpdatedAdmissionTransfer.setId(admissionTransfer.getId());

        partialUpdatedAdmissionTransfer.transferredAt(UPDATED_TRANSFERRED_AT).reason(UPDATED_REASON);

        restAdmissionTransferMockMvc
            .perform(
                patch(ENTITY_API_URL_ID, partialUpdatedAdmissionTransfer.getId())
                    .contentType("application/merge-patch+json")
                    .content(om.writeValueAsBytes(partialUpdatedAdmissionTransfer))
            )
            .andExpect(status().isOk());

        // Validate the AdmissionTransfer in the database

        assertSameRepositoryCount(databaseSizeBeforeUpdate);
        assertAdmissionTransferUpdatableFieldsEquals(
            partialUpdatedAdmissionTransfer,
            getPersistedAdmissionTransfer(partialUpdatedAdmissionTransfer)
        );
    }

    @Test
    @Transactional
    void patchNonExistingAdmissionTransfer() throws Exception {
        long databaseSizeBeforeUpdate = getRepositoryCount();
        admissionTransfer.setId(longCount.incrementAndGet());

        // Create the AdmissionTransfer
        AdmissionTransferDTO admissionTransferDTO = admissionTransferMapper.toDto(admissionTransfer);

        // If the entity doesn't have an ID, it will throw BadRequestAlertException
        restAdmissionTransferMockMvc
            .perform(
                patch(ENTITY_API_URL_ID, admissionTransferDTO.getId())
                    .contentType("application/merge-patch+json")
                    .content(om.writeValueAsBytes(admissionTransferDTO))
            )
            .andExpect(status().isBadRequest());

        // Validate the AdmissionTransfer in the database
        assertSameRepositoryCount(databaseSizeBeforeUpdate);
    }

    @Test
    @Transactional
    void patchWithIdMismatchAdmissionTransfer() throws Exception {
        long databaseSizeBeforeUpdate = getRepositoryCount();
        admissionTransfer.setId(longCount.incrementAndGet());

        // Create the AdmissionTransfer
        AdmissionTransferDTO admissionTransferDTO = admissionTransferMapper.toDto(admissionTransfer);

        // If url ID doesn't match entity ID, it will throw BadRequestAlertException
        restAdmissionTransferMockMvc
            .perform(
                patch(ENTITY_API_URL_ID, longCount.incrementAndGet())
                    .contentType("application/merge-patch+json")
                    .content(om.writeValueAsBytes(admissionTransferDTO))
            )
            .andExpect(status().isBadRequest());

        // Validate the AdmissionTransfer in the database
        assertSameRepositoryCount(databaseSizeBeforeUpdate);
    }

    @Test
    @Transactional
    void patchWithMissingIdPathParamAdmissionTransfer() throws Exception {
        long databaseSizeBeforeUpdate = getRepositoryCount();
        admissionTransfer.setId(longCount.incrementAndGet());

        // Create the AdmissionTransfer
        AdmissionTransferDTO admissionTransferDTO = admissionTransferMapper.toDto(admissionTransfer);

        // If url ID doesn't match entity ID, it will throw BadRequestAlertException
        restAdmissionTransferMockMvc
            .perform(patch(ENTITY_API_URL).contentType("application/merge-patch+json").content(om.writeValueAsBytes(admissionTransferDTO)))
            .andExpect(status().isMethodNotAllowed());

        // Validate the AdmissionTransfer in the database
        assertSameRepositoryCount(databaseSizeBeforeUpdate);
    }

    @Test
    @Transactional
    void deleteAdmissionTransfer() throws Exception {
        // Initialize the database
        insertedAdmissionTransfer = admissionTransferRepository.saveAndFlush(admissionTransfer);

        long databaseSizeBeforeDelete = getRepositoryCount();

        // Delete the admissionTransfer
        restAdmissionTransferMockMvc
            .perform(delete(ENTITY_API_URL_ID, admissionTransfer.getId()).accept(MediaType.APPLICATION_JSON))
            .andExpect(status().isNoContent());

        // Validate the database contains one less item
        assertDecrementedRepositoryCount(databaseSizeBeforeDelete);
    }

    protected long getRepositoryCount() {
        return admissionTransferRepository.count();
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

    protected AdmissionTransfer getPersistedAdmissionTransfer(AdmissionTransfer admissionTransfer) {
        return admissionTransferRepository.findById(admissionTransfer.getId()).orElseThrow();
    }

    protected void assertPersistedAdmissionTransferToMatchAllProperties(AdmissionTransfer expectedAdmissionTransfer) {
        assertAdmissionTransferAllPropertiesEquals(expectedAdmissionTransfer, getPersistedAdmissionTransfer(expectedAdmissionTransfer));
    }

    protected void assertPersistedAdmissionTransferToMatchUpdatableProperties(AdmissionTransfer expectedAdmissionTransfer) {
        assertAdmissionTransferAllUpdatablePropertiesEquals(
            expectedAdmissionTransfer,
            getPersistedAdmissionTransfer(expectedAdmissionTransfer)
        );
    }
}
