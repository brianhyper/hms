package com.hyperbrains.hms.web.rest;

import static com.hyperbrains.hms.domain.ResultAsserts.*;
import static com.hyperbrains.hms.web.rest.TestUtil.createUpdateProxyForBean;
import static org.assertj.core.api.Assertions.assertThat;
import static org.hamcrest.Matchers.hasItem;
import static org.mockito.Mockito.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.hyperbrains.hms.IntegrationTest;
import com.hyperbrains.hms.domain.Result;
import com.hyperbrains.hms.domain.User;
import com.hyperbrains.hms.repository.ResultRepository;
import com.hyperbrains.hms.repository.UserRepository;
import com.hyperbrains.hms.service.ResultService;
import com.hyperbrains.hms.service.dto.ResultDTO;
import com.hyperbrains.hms.service.mapper.ResultMapper;
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
 * Integration tests for the {@link ResultResource} REST controller.
 */
@IntegrationTest
@ExtendWith(MockitoExtension.class)
@AutoConfigureMockMvc
// A result written straight to this endpoint bypasses completing the order, charging for it and
// recomputing the visit, so it is closed to the lab and radiology roles. Recording a result is done
// through /api/visit-orders/{id}/result; this test covers the super-admin escape hatch.
@WithMockUser(authorities = AuthoritiesConstants.SUPER_ADMIN)
class ResultResourceIT {

    private static final String DEFAULT_RESULT_VALUE = "AAAAAAAAAA";
    private static final String UPDATED_RESULT_VALUE = "BBBBBBBBBB";

    private static final String DEFAULT_NOTES = "AAAAAAAAAA";
    private static final String UPDATED_NOTES = "BBBBBBBBBB";

    private static final Instant DEFAULT_ENTERED_AT = Instant.ofEpochMilli(0L);
    private static final Instant UPDATED_ENTERED_AT = Instant.ofEpochMilli(1790236635213L);

    private static final String DEFAULT_IMAGE_REFERENCE = "AAAAAAAAAA";
    private static final String UPDATED_IMAGE_REFERENCE = "BBBBBBBBBB";

    private static final String ENTITY_API_URL = "/api/results";
    private static final String ENTITY_API_URL_ID = ENTITY_API_URL + "/{id}";

    private static final Random random = new Random();
    private static final AtomicLong longCount = new AtomicLong(random.nextInt() + 2L * Integer.MAX_VALUE);

    @Autowired
    private ObjectMapper om;

    @Autowired
    private ResultRepository resultRepository;

    @Autowired
    private UserRepository userRepository;

    @Mock
    private ResultRepository resultRepositoryMock;

    @Autowired
    private ResultMapper resultMapper;

    @Mock
    private ResultService resultServiceMock;

    @Autowired
    private EntityManager em;

    @Autowired
    private MockMvc restResultMockMvc;

    private Result result;

    private Result insertedResult;

    /**
     * Create an entity for this test.
     *
     * This is a static method, as tests for other entities might also need it,
     * if they test an entity which requires the current entity.
     */
    public static Result createEntity(EntityManager em) {
        Result result = new Result()
            .resultValue(DEFAULT_RESULT_VALUE)
            .notes(DEFAULT_NOTES)
            .enteredAt(DEFAULT_ENTERED_AT)
            .imageReference(DEFAULT_IMAGE_REFERENCE);
        // Add required entity
        User user = UserResourceIT.createEntity();
        em.persist(user);
        em.flush();
        result.setEnteredBy(user);
        return result;
    }

    /**
     * Create an updated entity for this test.
     *
     * This is a static method, as tests for other entities might also need it,
     * if they test an entity which requires the current entity.
     */
    public static Result createUpdatedEntity(EntityManager em) {
        Result updatedResult = new Result()
            .resultValue(UPDATED_RESULT_VALUE)
            .notes(UPDATED_NOTES)
            .enteredAt(UPDATED_ENTERED_AT)
            .imageReference(UPDATED_IMAGE_REFERENCE);
        // Add required entity
        User user = UserResourceIT.createEntity();
        em.persist(user);
        em.flush();
        updatedResult.setEnteredBy(user);
        return updatedResult;
    }

    @BeforeEach
    void initTest() {
        result = createEntity(em);
    }

    @AfterEach
    void cleanup() {
        if (insertedResult != null) {
            resultRepository.delete(insertedResult);
            insertedResult = null;
        }
    }

    @Test
    @Transactional
    void createResult() throws Exception {
        long databaseSizeBeforeCreate = getRepositoryCount();
        // Create the Result
        ResultDTO resultDTO = resultMapper.toDto(result);
        var returnedResultDTO = om.readValue(
            restResultMockMvc
                .perform(post(ENTITY_API_URL).contentType(MediaType.APPLICATION_JSON).content(om.writeValueAsBytes(resultDTO)))
                .andExpect(status().isCreated())
                .andReturn()
                .getResponse()
                .getContentAsString(),
            ResultDTO.class
        );

        // Validate the Result in the database
        assertIncrementedRepositoryCount(databaseSizeBeforeCreate);
        var returnedResult = resultMapper.toEntity(returnedResultDTO);
        assertResultUpdatableFieldsEquals(returnedResult, getPersistedResult(returnedResult));

        insertedResult = returnedResult;
    }

    @Test
    @Transactional
    void createResultWithExistingId() throws Exception {
        // Create the Result with an existing ID
        result.setId(1L);
        ResultDTO resultDTO = resultMapper.toDto(result);

        long databaseSizeBeforeCreate = getRepositoryCount();

        // An entity with an existing ID cannot be created, so this API call must fail
        restResultMockMvc
            .perform(post(ENTITY_API_URL).contentType(MediaType.APPLICATION_JSON).content(om.writeValueAsBytes(resultDTO)))
            .andExpect(status().isBadRequest());

        // Validate the Result in the database
        assertSameRepositoryCount(databaseSizeBeforeCreate);
    }

    @Test
    @Transactional
    void checkEnteredAtIsRequired() throws Exception {
        long databaseSizeBeforeTest = getRepositoryCount();
        // set the field null
        result.setEnteredAt(null);

        // Create the Result, which fails.
        ResultDTO resultDTO = resultMapper.toDto(result);

        restResultMockMvc
            .perform(post(ENTITY_API_URL).contentType(MediaType.APPLICATION_JSON).content(om.writeValueAsBytes(resultDTO)))
            .andExpect(status().isBadRequest());

        assertSameRepositoryCount(databaseSizeBeforeTest);
    }

    @Test
    @Transactional
    void getAllResults() throws Exception {
        // Initialize the database
        insertedResult = resultRepository.saveAndFlush(result);

        // Get all the resultList
        restResultMockMvc
            .perform(get(ENTITY_API_URL + "?sort=id,desc"))
            .andExpect(status().isOk())
            .andExpect(content().contentType(MediaType.APPLICATION_JSON_VALUE))
            .andExpect(jsonPath("$.[*].id").value(hasItem(result.getId().intValue())))
            .andExpect(jsonPath("$.[*].resultValue").value(hasItem(DEFAULT_RESULT_VALUE)))
            .andExpect(jsonPath("$.[*].notes").value(hasItem(DEFAULT_NOTES)))
            .andExpect(jsonPath("$.[*].enteredAt").value(hasItem(DEFAULT_ENTERED_AT.toString())))
            .andExpect(jsonPath("$.[*].imageReference").value(hasItem(DEFAULT_IMAGE_REFERENCE)));
    }

    @SuppressWarnings({ "unchecked" })
    void getAllResultsWithEagerRelationshipsIsEnabled() throws Exception {
        when(resultServiceMock.findAllWithEagerRelationships(any())).thenReturn(new PageImpl(new ArrayList<>()));

        restResultMockMvc.perform(get(ENTITY_API_URL + "?eagerload=true")).andExpect(status().isOk());

        verify(resultServiceMock, times(1)).findAllWithEagerRelationships(any());
    }

    @SuppressWarnings({ "unchecked" })
    void getAllResultsWithEagerRelationshipsIsNotEnabled() throws Exception {
        when(resultServiceMock.findAllWithEagerRelationships(any())).thenReturn(new PageImpl(new ArrayList<>()));

        restResultMockMvc.perform(get(ENTITY_API_URL + "?eagerload=false")).andExpect(status().isOk());
        verify(resultRepositoryMock, times(1)).findAll(any(Pageable.class));
    }

    @Test
    @Transactional
    void getResult() throws Exception {
        // Initialize the database
        insertedResult = resultRepository.saveAndFlush(result);

        // Get the result
        restResultMockMvc
            .perform(get(ENTITY_API_URL_ID, result.getId()))
            .andExpect(status().isOk())
            .andExpect(content().contentType(MediaType.APPLICATION_JSON_VALUE))
            .andExpect(jsonPath("$.id").value(result.getId().intValue()))
            .andExpect(jsonPath("$.resultValue").value(DEFAULT_RESULT_VALUE))
            .andExpect(jsonPath("$.notes").value(DEFAULT_NOTES))
            .andExpect(jsonPath("$.enteredAt").value(DEFAULT_ENTERED_AT.toString()))
            .andExpect(jsonPath("$.imageReference").value(DEFAULT_IMAGE_REFERENCE));
    }

    @Test
    @Transactional
    void getNonExistingResult() throws Exception {
        // Get the result
        restResultMockMvc.perform(get(ENTITY_API_URL_ID, Long.MAX_VALUE)).andExpect(status().isNotFound());
    }

    @Test
    @Transactional
    void putExistingResult() throws Exception {
        // Initialize the database
        insertedResult = resultRepository.saveAndFlush(result);

        long databaseSizeBeforeUpdate = getRepositoryCount();

        // Update the result
        Result updatedResult = resultRepository.findById(result.getId()).orElseThrow();
        // Disconnect from session so that the updates on updatedResult are not directly saved in db
        em.detach(updatedResult);
        updatedResult
            .resultValue(UPDATED_RESULT_VALUE)
            .notes(UPDATED_NOTES)
            .enteredAt(UPDATED_ENTERED_AT)
            .imageReference(UPDATED_IMAGE_REFERENCE);
        ResultDTO resultDTO = resultMapper.toDto(updatedResult);

        restResultMockMvc
            .perform(
                put(ENTITY_API_URL_ID, resultDTO.getId()).contentType(MediaType.APPLICATION_JSON).content(om.writeValueAsBytes(resultDTO))
            )
            .andExpect(status().isOk());

        // Validate the Result in the database
        assertSameRepositoryCount(databaseSizeBeforeUpdate);
        assertPersistedResultToMatchAllProperties(updatedResult);
    }

    @Test
    @Transactional
    void putNonExistingResult() throws Exception {
        long databaseSizeBeforeUpdate = getRepositoryCount();
        result.setId(longCount.incrementAndGet());

        // Create the Result
        ResultDTO resultDTO = resultMapper.toDto(result);

        // If the entity doesn't have an ID, it will throw BadRequestAlertException
        restResultMockMvc
            .perform(
                put(ENTITY_API_URL_ID, resultDTO.getId()).contentType(MediaType.APPLICATION_JSON).content(om.writeValueAsBytes(resultDTO))
            )
            .andExpect(status().isBadRequest());

        // Validate the Result in the database
        assertSameRepositoryCount(databaseSizeBeforeUpdate);
    }

    @Test
    @Transactional
    void putWithIdMismatchResult() throws Exception {
        long databaseSizeBeforeUpdate = getRepositoryCount();
        result.setId(longCount.incrementAndGet());

        // Create the Result
        ResultDTO resultDTO = resultMapper.toDto(result);

        // If url ID doesn't match entity ID, it will throw BadRequestAlertException
        restResultMockMvc
            .perform(
                put(ENTITY_API_URL_ID, longCount.incrementAndGet())
                    .contentType(MediaType.APPLICATION_JSON)
                    .content(om.writeValueAsBytes(resultDTO))
            )
            .andExpect(status().isBadRequest());

        // Validate the Result in the database
        assertSameRepositoryCount(databaseSizeBeforeUpdate);
    }

    @Test
    @Transactional
    void putWithMissingIdPathParamResult() throws Exception {
        long databaseSizeBeforeUpdate = getRepositoryCount();
        result.setId(longCount.incrementAndGet());

        // Create the Result
        ResultDTO resultDTO = resultMapper.toDto(result);

        // If url ID doesn't match entity ID, it will throw BadRequestAlertException
        restResultMockMvc
            .perform(put(ENTITY_API_URL).contentType(MediaType.APPLICATION_JSON).content(om.writeValueAsBytes(resultDTO)))
            .andExpect(status().isMethodNotAllowed());

        // Validate the Result in the database
        assertSameRepositoryCount(databaseSizeBeforeUpdate);
    }

    @Test
    @Transactional
    void partialUpdateResultWithPatch() throws Exception {
        // Initialize the database
        insertedResult = resultRepository.saveAndFlush(result);

        long databaseSizeBeforeUpdate = getRepositoryCount();

        // Update the result using partial update
        Result partialUpdatedResult = new Result();
        partialUpdatedResult.setId(result.getId());

        partialUpdatedResult.resultValue(UPDATED_RESULT_VALUE).enteredAt(UPDATED_ENTERED_AT).imageReference(UPDATED_IMAGE_REFERENCE);

        restResultMockMvc
            .perform(
                patch(ENTITY_API_URL_ID, partialUpdatedResult.getId())
                    .contentType("application/merge-patch+json")
                    .content(om.writeValueAsBytes(partialUpdatedResult))
            )
            .andExpect(status().isOk());

        // Validate the Result in the database

        assertSameRepositoryCount(databaseSizeBeforeUpdate);
        assertResultUpdatableFieldsEquals(createUpdateProxyForBean(partialUpdatedResult, result), getPersistedResult(result));
    }

    @Test
    @Transactional
    void fullUpdateResultWithPatch() throws Exception {
        // Initialize the database
        insertedResult = resultRepository.saveAndFlush(result);

        long databaseSizeBeforeUpdate = getRepositoryCount();

        // Update the result using partial update
        Result partialUpdatedResult = new Result();
        partialUpdatedResult.setId(result.getId());

        partialUpdatedResult
            .resultValue(UPDATED_RESULT_VALUE)
            .notes(UPDATED_NOTES)
            .enteredAt(UPDATED_ENTERED_AT)
            .imageReference(UPDATED_IMAGE_REFERENCE);

        restResultMockMvc
            .perform(
                patch(ENTITY_API_URL_ID, partialUpdatedResult.getId())
                    .contentType("application/merge-patch+json")
                    .content(om.writeValueAsBytes(partialUpdatedResult))
            )
            .andExpect(status().isOk());

        // Validate the Result in the database

        assertSameRepositoryCount(databaseSizeBeforeUpdate);
        assertResultUpdatableFieldsEquals(partialUpdatedResult, getPersistedResult(partialUpdatedResult));
    }

    @Test
    @Transactional
    void patchNonExistingResult() throws Exception {
        long databaseSizeBeforeUpdate = getRepositoryCount();
        result.setId(longCount.incrementAndGet());

        // Create the Result
        ResultDTO resultDTO = resultMapper.toDto(result);

        // If the entity doesn't have an ID, it will throw BadRequestAlertException
        restResultMockMvc
            .perform(
                patch(ENTITY_API_URL_ID, resultDTO.getId())
                    .contentType("application/merge-patch+json")
                    .content(om.writeValueAsBytes(resultDTO))
            )
            .andExpect(status().isBadRequest());

        // Validate the Result in the database
        assertSameRepositoryCount(databaseSizeBeforeUpdate);
    }

    @Test
    @Transactional
    void patchWithIdMismatchResult() throws Exception {
        long databaseSizeBeforeUpdate = getRepositoryCount();
        result.setId(longCount.incrementAndGet());

        // Create the Result
        ResultDTO resultDTO = resultMapper.toDto(result);

        // If url ID doesn't match entity ID, it will throw BadRequestAlertException
        restResultMockMvc
            .perform(
                patch(ENTITY_API_URL_ID, longCount.incrementAndGet())
                    .contentType("application/merge-patch+json")
                    .content(om.writeValueAsBytes(resultDTO))
            )
            .andExpect(status().isBadRequest());

        // Validate the Result in the database
        assertSameRepositoryCount(databaseSizeBeforeUpdate);
    }

    @Test
    @Transactional
    void patchWithMissingIdPathParamResult() throws Exception {
        long databaseSizeBeforeUpdate = getRepositoryCount();
        result.setId(longCount.incrementAndGet());

        // Create the Result
        ResultDTO resultDTO = resultMapper.toDto(result);

        // If url ID doesn't match entity ID, it will throw BadRequestAlertException
        restResultMockMvc
            .perform(patch(ENTITY_API_URL).contentType("application/merge-patch+json").content(om.writeValueAsBytes(resultDTO)))
            .andExpect(status().isMethodNotAllowed());

        // Validate the Result in the database
        assertSameRepositoryCount(databaseSizeBeforeUpdate);
    }

    @Test
    @Transactional
    void deleteResult() throws Exception {
        // Initialize the database
        insertedResult = resultRepository.saveAndFlush(result);

        long databaseSizeBeforeDelete = getRepositoryCount();

        // Delete the result
        restResultMockMvc
            .perform(delete(ENTITY_API_URL_ID, result.getId()).accept(MediaType.APPLICATION_JSON))
            .andExpect(status().isNoContent());

        // Validate the database contains one less item
        assertDecrementedRepositoryCount(databaseSizeBeforeDelete);
    }

    protected long getRepositoryCount() {
        return resultRepository.count();
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

    protected Result getPersistedResult(Result result) {
        return resultRepository.findById(result.getId()).orElseThrow();
    }

    protected void assertPersistedResultToMatchAllProperties(Result expectedResult) {
        assertResultAllPropertiesEquals(expectedResult, getPersistedResult(expectedResult));
    }

    protected void assertPersistedResultToMatchUpdatableProperties(Result expectedResult) {
        assertResultAllUpdatablePropertiesEquals(expectedResult, getPersistedResult(expectedResult));
    }
}
