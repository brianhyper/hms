package com.hyperbrains.hms.web.rest;

import static com.hyperbrains.hms.domain.WardCoverAsserts.*;
import static com.hyperbrains.hms.web.rest.TestUtil.createUpdateProxyForBean;
import static org.assertj.core.api.Assertions.assertThat;
import static org.hamcrest.Matchers.hasItem;
import static org.mockito.Mockito.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.hyperbrains.hms.IntegrationTest;
import com.hyperbrains.hms.domain.User;
import com.hyperbrains.hms.domain.Ward;
import com.hyperbrains.hms.domain.WardCover;
import com.hyperbrains.hms.repository.UserRepository;
import com.hyperbrains.hms.repository.WardCoverRepository;
import com.hyperbrains.hms.service.WardCoverService;
import com.hyperbrains.hms.service.dto.WardCoverDTO;
import com.hyperbrains.hms.service.mapper.WardCoverMapper;
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
 * Integration tests for the {@link WardCoverResource} REST controller.
 */
@IntegrationTest
@ExtendWith(MockitoExtension.class)
@AutoConfigureMockMvc
@WithMockUser
class WardCoverResourceIT {

    private static final Instant DEFAULT_COVERS_FROM = Instant.ofEpochMilli(0L);
    private static final Instant UPDATED_COVERS_FROM = Instant.ofEpochMilli(1790236635213L);

    private static final Instant DEFAULT_COVERS_TO = Instant.ofEpochMilli(0L);
    private static final Instant UPDATED_COVERS_TO = Instant.ofEpochMilli(1790236635213L);

    private static final String DEFAULT_NOTE = "AAAAAAAAAA";
    private static final String UPDATED_NOTE = "BBBBBBBBBB";

    private static final String ENTITY_API_URL = "/api/ward-covers";
    private static final String ENTITY_API_URL_ID = ENTITY_API_URL + "/{id}";

    private static final Random random = new Random();
    private static final AtomicLong longCount = new AtomicLong(random.nextInt() + 2L * Integer.MAX_VALUE);

    @Autowired
    private ObjectMapper om;

    @Autowired
    private WardCoverRepository wardCoverRepository;

    @Autowired
    private UserRepository userRepository;

    @Mock
    private WardCoverRepository wardCoverRepositoryMock;

    @Autowired
    private WardCoverMapper wardCoverMapper;

    @Mock
    private WardCoverService wardCoverServiceMock;

    @Autowired
    private EntityManager em;

    @Autowired
    private MockMvc restWardCoverMockMvc;

    private WardCover wardCover;

    private WardCover insertedWardCover;

    /**
     * Create an entity for this test.
     *
     * This is a static method, as tests for other entities might also need it,
     * if they test an entity which requires the current entity.
     */
    public static WardCover createEntity(EntityManager em) {
        WardCover wardCover = new WardCover().coversFrom(DEFAULT_COVERS_FROM).coversTo(DEFAULT_COVERS_TO).note(DEFAULT_NOTE);
        // Add required entity
        User user = UserResourceIT.createEntity();
        em.persist(user);
        em.flush();
        wardCover.setDoctor(user);
        // Add required entity
        Ward ward;
        if (TestUtil.findAll(em, Ward.class).isEmpty()) {
            ward = WardResourceIT.createEntity(em);
            em.persist(ward);
            em.flush();
        } else {
            ward = TestUtil.findAll(em, Ward.class).get(0);
        }
        wardCover.setWard(ward);
        // Add required entity
        wardCover.setAssignedBy(user);
        return wardCover;
    }

    /**
     * Create an updated entity for this test.
     *
     * This is a static method, as tests for other entities might also need it,
     * if they test an entity which requires the current entity.
     */
    public static WardCover createUpdatedEntity(EntityManager em) {
        WardCover updatedWardCover = new WardCover().coversFrom(UPDATED_COVERS_FROM).coversTo(UPDATED_COVERS_TO).note(UPDATED_NOTE);
        // Add required entity
        User user = UserResourceIT.createEntity();
        em.persist(user);
        em.flush();
        updatedWardCover.setDoctor(user);
        // Add required entity
        Ward ward;
        if (TestUtil.findAll(em, Ward.class).isEmpty()) {
            ward = WardResourceIT.createUpdatedEntity(em);
            em.persist(ward);
            em.flush();
        } else {
            ward = TestUtil.findAll(em, Ward.class).get(0);
        }
        updatedWardCover.setWard(ward);
        // Add required entity
        updatedWardCover.setAssignedBy(user);
        return updatedWardCover;
    }

    @BeforeEach
    void initTest() {
        wardCover = createEntity(em);
    }

    @AfterEach
    void cleanup() {
        if (insertedWardCover != null) {
            wardCoverRepository.delete(insertedWardCover);
            insertedWardCover = null;
        }
    }

    @Test
    @Transactional
    void createWardCover() throws Exception {
        long databaseSizeBeforeCreate = getRepositoryCount();
        // Create the WardCover
        WardCoverDTO wardCoverDTO = wardCoverMapper.toDto(wardCover);
        var returnedWardCoverDTO = om.readValue(
            restWardCoverMockMvc
                .perform(post(ENTITY_API_URL).contentType(MediaType.APPLICATION_JSON).content(om.writeValueAsBytes(wardCoverDTO)))
                .andExpect(status().isCreated())
                .andReturn()
                .getResponse()
                .getContentAsString(),
            WardCoverDTO.class
        );

        // Validate the WardCover in the database
        assertIncrementedRepositoryCount(databaseSizeBeforeCreate);
        var returnedWardCover = wardCoverMapper.toEntity(returnedWardCoverDTO);
        assertWardCoverUpdatableFieldsEquals(returnedWardCover, getPersistedWardCover(returnedWardCover));

        insertedWardCover = returnedWardCover;
    }

    @Test
    @Transactional
    void createWardCoverWithExistingId() throws Exception {
        // Create the WardCover with an existing ID
        wardCover.setId(1L);
        WardCoverDTO wardCoverDTO = wardCoverMapper.toDto(wardCover);

        long databaseSizeBeforeCreate = getRepositoryCount();

        // An entity with an existing ID cannot be created, so this API call must fail
        restWardCoverMockMvc
            .perform(post(ENTITY_API_URL).contentType(MediaType.APPLICATION_JSON).content(om.writeValueAsBytes(wardCoverDTO)))
            .andExpect(status().isBadRequest());

        // Validate the WardCover in the database
        assertSameRepositoryCount(databaseSizeBeforeCreate);
    }

    @Test
    @Transactional
    void checkCoversFromIsRequired() throws Exception {
        long databaseSizeBeforeTest = getRepositoryCount();
        // set the field null
        wardCover.setCoversFrom(null);

        // Create the WardCover, which fails.
        WardCoverDTO wardCoverDTO = wardCoverMapper.toDto(wardCover);

        restWardCoverMockMvc
            .perform(post(ENTITY_API_URL).contentType(MediaType.APPLICATION_JSON).content(om.writeValueAsBytes(wardCoverDTO)))
            .andExpect(status().isBadRequest());

        assertSameRepositoryCount(databaseSizeBeforeTest);
    }

    @Test
    @Transactional
    void getAllWardCovers() throws Exception {
        // Initialize the database
        insertedWardCover = wardCoverRepository.saveAndFlush(wardCover);

        // Get all the wardCoverList
        restWardCoverMockMvc
            .perform(get(ENTITY_API_URL + "?sort=id,desc"))
            .andExpect(status().isOk())
            .andExpect(content().contentType(MediaType.APPLICATION_JSON_VALUE))
            .andExpect(jsonPath("$.[*].id").value(hasItem(wardCover.getId().intValue())))
            .andExpect(jsonPath("$.[*].coversFrom").value(hasItem(DEFAULT_COVERS_FROM.toString())))
            .andExpect(jsonPath("$.[*].coversTo").value(hasItem(DEFAULT_COVERS_TO.toString())))
            .andExpect(jsonPath("$.[*].note").value(hasItem(DEFAULT_NOTE)));
    }

    @SuppressWarnings({ "unchecked" })
    void getAllWardCoversWithEagerRelationshipsIsEnabled() throws Exception {
        when(wardCoverServiceMock.findAllWithEagerRelationships(any())).thenReturn(new PageImpl(new ArrayList<>()));

        restWardCoverMockMvc.perform(get(ENTITY_API_URL + "?eagerload=true")).andExpect(status().isOk());

        verify(wardCoverServiceMock, times(1)).findAllWithEagerRelationships(any());
    }

    @SuppressWarnings({ "unchecked" })
    void getAllWardCoversWithEagerRelationshipsIsNotEnabled() throws Exception {
        when(wardCoverServiceMock.findAllWithEagerRelationships(any())).thenReturn(new PageImpl(new ArrayList<>()));

        restWardCoverMockMvc.perform(get(ENTITY_API_URL + "?eagerload=false")).andExpect(status().isOk());
        verify(wardCoverRepositoryMock, times(1)).findAll(any(Pageable.class));
    }

    @Test
    @Transactional
    void getWardCover() throws Exception {
        // Initialize the database
        insertedWardCover = wardCoverRepository.saveAndFlush(wardCover);

        // Get the wardCover
        restWardCoverMockMvc
            .perform(get(ENTITY_API_URL_ID, wardCover.getId()))
            .andExpect(status().isOk())
            .andExpect(content().contentType(MediaType.APPLICATION_JSON_VALUE))
            .andExpect(jsonPath("$.id").value(wardCover.getId().intValue()))
            .andExpect(jsonPath("$.coversFrom").value(DEFAULT_COVERS_FROM.toString()))
            .andExpect(jsonPath("$.coversTo").value(DEFAULT_COVERS_TO.toString()))
            .andExpect(jsonPath("$.note").value(DEFAULT_NOTE));
    }

    @Test
    @Transactional
    void getNonExistingWardCover() throws Exception {
        // Get the wardCover
        restWardCoverMockMvc.perform(get(ENTITY_API_URL_ID, Long.MAX_VALUE)).andExpect(status().isNotFound());
    }

    @Test
    @Transactional
    void putExistingWardCover() throws Exception {
        // Initialize the database
        insertedWardCover = wardCoverRepository.saveAndFlush(wardCover);

        long databaseSizeBeforeUpdate = getRepositoryCount();

        // Update the wardCover
        WardCover updatedWardCover = wardCoverRepository.findById(wardCover.getId()).orElseThrow();
        // Disconnect from session so that the updates on updatedWardCover are not directly saved in db
        em.detach(updatedWardCover);
        updatedWardCover.coversFrom(UPDATED_COVERS_FROM).coversTo(UPDATED_COVERS_TO).note(UPDATED_NOTE);
        WardCoverDTO wardCoverDTO = wardCoverMapper.toDto(updatedWardCover);

        restWardCoverMockMvc
            .perform(
                put(ENTITY_API_URL_ID, wardCoverDTO.getId())
                    .contentType(MediaType.APPLICATION_JSON)
                    .content(om.writeValueAsBytes(wardCoverDTO))
            )
            .andExpect(status().isOk());

        // Validate the WardCover in the database
        assertSameRepositoryCount(databaseSizeBeforeUpdate);
        assertPersistedWardCoverToMatchAllProperties(updatedWardCover);
    }

    @Test
    @Transactional
    void putNonExistingWardCover() throws Exception {
        long databaseSizeBeforeUpdate = getRepositoryCount();
        wardCover.setId(longCount.incrementAndGet());

        // Create the WardCover
        WardCoverDTO wardCoverDTO = wardCoverMapper.toDto(wardCover);

        // If the entity doesn't have an ID, it will throw BadRequestAlertException
        restWardCoverMockMvc
            .perform(
                put(ENTITY_API_URL_ID, wardCoverDTO.getId())
                    .contentType(MediaType.APPLICATION_JSON)
                    .content(om.writeValueAsBytes(wardCoverDTO))
            )
            .andExpect(status().isBadRequest());

        // Validate the WardCover in the database
        assertSameRepositoryCount(databaseSizeBeforeUpdate);
    }

    @Test
    @Transactional
    void putWithIdMismatchWardCover() throws Exception {
        long databaseSizeBeforeUpdate = getRepositoryCount();
        wardCover.setId(longCount.incrementAndGet());

        // Create the WardCover
        WardCoverDTO wardCoverDTO = wardCoverMapper.toDto(wardCover);

        // If url ID doesn't match entity ID, it will throw BadRequestAlertException
        restWardCoverMockMvc
            .perform(
                put(ENTITY_API_URL_ID, longCount.incrementAndGet())
                    .contentType(MediaType.APPLICATION_JSON)
                    .content(om.writeValueAsBytes(wardCoverDTO))
            )
            .andExpect(status().isBadRequest());

        // Validate the WardCover in the database
        assertSameRepositoryCount(databaseSizeBeforeUpdate);
    }

    @Test
    @Transactional
    void putWithMissingIdPathParamWardCover() throws Exception {
        long databaseSizeBeforeUpdate = getRepositoryCount();
        wardCover.setId(longCount.incrementAndGet());

        // Create the WardCover
        WardCoverDTO wardCoverDTO = wardCoverMapper.toDto(wardCover);

        // If url ID doesn't match entity ID, it will throw BadRequestAlertException
        restWardCoverMockMvc
            .perform(put(ENTITY_API_URL).contentType(MediaType.APPLICATION_JSON).content(om.writeValueAsBytes(wardCoverDTO)))
            .andExpect(status().isMethodNotAllowed());

        // Validate the WardCover in the database
        assertSameRepositoryCount(databaseSizeBeforeUpdate);
    }

    @Test
    @Transactional
    void partialUpdateWardCoverWithPatch() throws Exception {
        // Initialize the database
        insertedWardCover = wardCoverRepository.saveAndFlush(wardCover);

        long databaseSizeBeforeUpdate = getRepositoryCount();

        // Update the wardCover using partial update
        WardCover partialUpdatedWardCover = new WardCover();
        partialUpdatedWardCover.setId(wardCover.getId());

        partialUpdatedWardCover.coversFrom(UPDATED_COVERS_FROM).note(UPDATED_NOTE);

        restWardCoverMockMvc
            .perform(
                patch(ENTITY_API_URL_ID, partialUpdatedWardCover.getId())
                    .contentType("application/merge-patch+json")
                    .content(om.writeValueAsBytes(partialUpdatedWardCover))
            )
            .andExpect(status().isOk());

        // Validate the WardCover in the database

        assertSameRepositoryCount(databaseSizeBeforeUpdate);
        assertWardCoverUpdatableFieldsEquals(
            createUpdateProxyForBean(partialUpdatedWardCover, wardCover),
            getPersistedWardCover(wardCover)
        );
    }

    @Test
    @Transactional
    void fullUpdateWardCoverWithPatch() throws Exception {
        // Initialize the database
        insertedWardCover = wardCoverRepository.saveAndFlush(wardCover);

        long databaseSizeBeforeUpdate = getRepositoryCount();

        // Update the wardCover using partial update
        WardCover partialUpdatedWardCover = new WardCover();
        partialUpdatedWardCover.setId(wardCover.getId());

        partialUpdatedWardCover.coversFrom(UPDATED_COVERS_FROM).coversTo(UPDATED_COVERS_TO).note(UPDATED_NOTE);

        restWardCoverMockMvc
            .perform(
                patch(ENTITY_API_URL_ID, partialUpdatedWardCover.getId())
                    .contentType("application/merge-patch+json")
                    .content(om.writeValueAsBytes(partialUpdatedWardCover))
            )
            .andExpect(status().isOk());

        // Validate the WardCover in the database

        assertSameRepositoryCount(databaseSizeBeforeUpdate);
        assertWardCoverUpdatableFieldsEquals(partialUpdatedWardCover, getPersistedWardCover(partialUpdatedWardCover));
    }

    @Test
    @Transactional
    void patchNonExistingWardCover() throws Exception {
        long databaseSizeBeforeUpdate = getRepositoryCount();
        wardCover.setId(longCount.incrementAndGet());

        // Create the WardCover
        WardCoverDTO wardCoverDTO = wardCoverMapper.toDto(wardCover);

        // If the entity doesn't have an ID, it will throw BadRequestAlertException
        restWardCoverMockMvc
            .perform(
                patch(ENTITY_API_URL_ID, wardCoverDTO.getId())
                    .contentType("application/merge-patch+json")
                    .content(om.writeValueAsBytes(wardCoverDTO))
            )
            .andExpect(status().isBadRequest());

        // Validate the WardCover in the database
        assertSameRepositoryCount(databaseSizeBeforeUpdate);
    }

    @Test
    @Transactional
    void patchWithIdMismatchWardCover() throws Exception {
        long databaseSizeBeforeUpdate = getRepositoryCount();
        wardCover.setId(longCount.incrementAndGet());

        // Create the WardCover
        WardCoverDTO wardCoverDTO = wardCoverMapper.toDto(wardCover);

        // If url ID doesn't match entity ID, it will throw BadRequestAlertException
        restWardCoverMockMvc
            .perform(
                patch(ENTITY_API_URL_ID, longCount.incrementAndGet())
                    .contentType("application/merge-patch+json")
                    .content(om.writeValueAsBytes(wardCoverDTO))
            )
            .andExpect(status().isBadRequest());

        // Validate the WardCover in the database
        assertSameRepositoryCount(databaseSizeBeforeUpdate);
    }

    @Test
    @Transactional
    void patchWithMissingIdPathParamWardCover() throws Exception {
        long databaseSizeBeforeUpdate = getRepositoryCount();
        wardCover.setId(longCount.incrementAndGet());

        // Create the WardCover
        WardCoverDTO wardCoverDTO = wardCoverMapper.toDto(wardCover);

        // If url ID doesn't match entity ID, it will throw BadRequestAlertException
        restWardCoverMockMvc
            .perform(patch(ENTITY_API_URL).contentType("application/merge-patch+json").content(om.writeValueAsBytes(wardCoverDTO)))
            .andExpect(status().isMethodNotAllowed());

        // Validate the WardCover in the database
        assertSameRepositoryCount(databaseSizeBeforeUpdate);
    }

    @Test
    @Transactional
    void deleteWardCover() throws Exception {
        // Initialize the database
        insertedWardCover = wardCoverRepository.saveAndFlush(wardCover);

        long databaseSizeBeforeDelete = getRepositoryCount();

        // Delete the wardCover
        restWardCoverMockMvc
            .perform(delete(ENTITY_API_URL_ID, wardCover.getId()).accept(MediaType.APPLICATION_JSON))
            .andExpect(status().isNoContent());

        // Validate the database contains one less item
        assertDecrementedRepositoryCount(databaseSizeBeforeDelete);
    }

    protected long getRepositoryCount() {
        return wardCoverRepository.count();
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

    protected WardCover getPersistedWardCover(WardCover wardCover) {
        return wardCoverRepository.findById(wardCover.getId()).orElseThrow();
    }

    protected void assertPersistedWardCoverToMatchAllProperties(WardCover expectedWardCover) {
        assertWardCoverAllPropertiesEquals(expectedWardCover, getPersistedWardCover(expectedWardCover));
    }

    protected void assertPersistedWardCoverToMatchUpdatableProperties(WardCover expectedWardCover) {
        assertWardCoverAllUpdatablePropertiesEquals(expectedWardCover, getPersistedWardCover(expectedWardCover));
    }
}
