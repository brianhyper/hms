package com.hyperbrains.hms.web.rest;

import static com.hyperbrains.hms.domain.DispenseLineAsserts.*;
import static com.hyperbrains.hms.web.rest.TestUtil.createUpdateProxyForBean;
import static org.assertj.core.api.Assertions.assertThat;
import static org.hamcrest.Matchers.hasItem;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.hyperbrains.hms.IntegrationTest;
import com.hyperbrains.hms.domain.Dispense;
import com.hyperbrains.hms.domain.DispenseLine;
import com.hyperbrains.hms.domain.Drug;
import com.hyperbrains.hms.domain.PrescriptionLine;
import com.hyperbrains.hms.repository.DispenseLineRepository;
import com.hyperbrains.hms.service.dto.DispenseLineDTO;
import com.hyperbrains.hms.service.mapper.DispenseLineMapper;
import jakarta.persistence.EntityManager;
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
 * Integration tests for the {@link DispenseLineResource} REST controller.
 */
@IntegrationTest
@AutoConfigureMockMvc
@WithMockUser(authorities = AuthoritiesConstants.SUPER_ADMIN)
class DispenseLineResourceIT {

    private static final Integer DEFAULT_QUANTITY = 1;
    private static final Integer UPDATED_QUANTITY = 2;

    private static final String DEFAULT_SUBSTITUTION_REASON = "AAAAAAAAAA";
    private static final String UPDATED_SUBSTITUTION_REASON = "BBBBBBBBBB";

    private static final String ENTITY_API_URL = "/api/dispense-lines";
    private static final String ENTITY_API_URL_ID = ENTITY_API_URL + "/{id}";

    private static final Random random = new Random();
    private static final AtomicLong longCount = new AtomicLong(random.nextInt() + 2L * Integer.MAX_VALUE);

    @Autowired
    private ObjectMapper om;

    @Autowired
    private DispenseLineRepository dispenseLineRepository;

    @Autowired
    private DispenseLineMapper dispenseLineMapper;

    @Autowired
    private EntityManager em;

    @Autowired
    private MockMvc restDispenseLineMockMvc;

    private DispenseLine dispenseLine;

    private DispenseLine insertedDispenseLine;

    /**
     * Create an entity for this test.
     *
     * This is a static method, as tests for other entities might also need it,
     * if they test an entity which requires the current entity.
     */
    public static DispenseLine createEntity(EntityManager em) {
        DispenseLine dispenseLine = new DispenseLine().quantity(DEFAULT_QUANTITY).substitutionReason(DEFAULT_SUBSTITUTION_REASON);
        // Add required entity
        Dispense dispense;
        if (TestUtil.findAll(em, Dispense.class).isEmpty()) {
            dispense = DispenseResourceIT.createEntity(em);
            em.persist(dispense);
            em.flush();
        } else {
            dispense = TestUtil.findAll(em, Dispense.class).get(0);
        }
        dispenseLine.setDispense(dispense);
        // Add required entity
        PrescriptionLine prescriptionLine;
        if (TestUtil.findAll(em, PrescriptionLine.class).isEmpty()) {
            prescriptionLine = PrescriptionLineResourceIT.createEntity(em);
            em.persist(prescriptionLine);
            em.flush();
        } else {
            prescriptionLine = TestUtil.findAll(em, PrescriptionLine.class).get(0);
        }
        dispenseLine.setPrescriptionLine(prescriptionLine);
        // Add required entity
        Drug drug;
        if (TestUtil.findAll(em, Drug.class).isEmpty()) {
            drug = DrugResourceIT.createEntity();
            em.persist(drug);
            em.flush();
        } else {
            drug = TestUtil.findAll(em, Drug.class).get(0);
        }
        dispenseLine.setDrug(drug);
        return dispenseLine;
    }

    /**
     * Create an updated entity for this test.
     *
     * This is a static method, as tests for other entities might also need it,
     * if they test an entity which requires the current entity.
     */
    public static DispenseLine createUpdatedEntity(EntityManager em) {
        DispenseLine updatedDispenseLine = new DispenseLine().quantity(UPDATED_QUANTITY).substitutionReason(UPDATED_SUBSTITUTION_REASON);
        // Add required entity
        Dispense dispense;
        if (TestUtil.findAll(em, Dispense.class).isEmpty()) {
            dispense = DispenseResourceIT.createUpdatedEntity(em);
            em.persist(dispense);
            em.flush();
        } else {
            dispense = TestUtil.findAll(em, Dispense.class).get(0);
        }
        updatedDispenseLine.setDispense(dispense);
        // Add required entity
        PrescriptionLine prescriptionLine;
        if (TestUtil.findAll(em, PrescriptionLine.class).isEmpty()) {
            prescriptionLine = PrescriptionLineResourceIT.createUpdatedEntity(em);
            em.persist(prescriptionLine);
            em.flush();
        } else {
            prescriptionLine = TestUtil.findAll(em, PrescriptionLine.class).get(0);
        }
        updatedDispenseLine.setPrescriptionLine(prescriptionLine);
        // Add required entity
        Drug drug;
        if (TestUtil.findAll(em, Drug.class).isEmpty()) {
            drug = DrugResourceIT.createUpdatedEntity();
            em.persist(drug);
            em.flush();
        } else {
            drug = TestUtil.findAll(em, Drug.class).get(0);
        }
        updatedDispenseLine.setDrug(drug);
        return updatedDispenseLine;
    }

    @BeforeEach
    void initTest() {
        dispenseLine = createEntity(em);
    }

    @AfterEach
    void cleanup() {
        if (insertedDispenseLine != null) {
            dispenseLineRepository.delete(insertedDispenseLine);
            insertedDispenseLine = null;
        }
    }

    @Test
    @Transactional
    void createDispenseLine() throws Exception {
        long databaseSizeBeforeCreate = getRepositoryCount();
        // Create the DispenseLine
        DispenseLineDTO dispenseLineDTO = dispenseLineMapper.toDto(dispenseLine);
        var returnedDispenseLineDTO = om.readValue(
            restDispenseLineMockMvc
                .perform(post(ENTITY_API_URL).contentType(MediaType.APPLICATION_JSON).content(om.writeValueAsBytes(dispenseLineDTO)))
                .andExpect(status().isCreated())
                .andReturn()
                .getResponse()
                .getContentAsString(),
            DispenseLineDTO.class
        );

        // Validate the DispenseLine in the database
        assertIncrementedRepositoryCount(databaseSizeBeforeCreate);
        var returnedDispenseLine = dispenseLineMapper.toEntity(returnedDispenseLineDTO);
        assertDispenseLineUpdatableFieldsEquals(returnedDispenseLine, getPersistedDispenseLine(returnedDispenseLine));

        insertedDispenseLine = returnedDispenseLine;
    }

    @Test
    @Transactional
    void createDispenseLineWithExistingId() throws Exception {
        // Create the DispenseLine with an existing ID
        dispenseLine.setId(1L);
        DispenseLineDTO dispenseLineDTO = dispenseLineMapper.toDto(dispenseLine);

        long databaseSizeBeforeCreate = getRepositoryCount();

        // An entity with an existing ID cannot be created, so this API call must fail
        restDispenseLineMockMvc
            .perform(post(ENTITY_API_URL).contentType(MediaType.APPLICATION_JSON).content(om.writeValueAsBytes(dispenseLineDTO)))
            .andExpect(status().isBadRequest());

        // Validate the DispenseLine in the database
        assertSameRepositoryCount(databaseSizeBeforeCreate);
    }

    @Test
    @Transactional
    void checkQuantityIsRequired() throws Exception {
        long databaseSizeBeforeTest = getRepositoryCount();
        // set the field null
        dispenseLine.setQuantity(null);

        // Create the DispenseLine, which fails.
        DispenseLineDTO dispenseLineDTO = dispenseLineMapper.toDto(dispenseLine);

        restDispenseLineMockMvc
            .perform(post(ENTITY_API_URL).contentType(MediaType.APPLICATION_JSON).content(om.writeValueAsBytes(dispenseLineDTO)))
            .andExpect(status().isBadRequest());

        assertSameRepositoryCount(databaseSizeBeforeTest);
    }

    @Test
    @Transactional
    void getAllDispenseLines() throws Exception {
        // Initialize the database
        insertedDispenseLine = dispenseLineRepository.saveAndFlush(dispenseLine);

        // Get all the dispenseLineList
        restDispenseLineMockMvc
            .perform(get(ENTITY_API_URL + "?sort=id,desc"))
            .andExpect(status().isOk())
            .andExpect(content().contentType(MediaType.APPLICATION_JSON_VALUE))
            .andExpect(jsonPath("$.[*].id").value(hasItem(dispenseLine.getId().intValue())))
            .andExpect(jsonPath("$.[*].quantity").value(hasItem(DEFAULT_QUANTITY)))
            .andExpect(jsonPath("$.[*].substitutionReason").value(hasItem(DEFAULT_SUBSTITUTION_REASON)));
    }

    @Test
    @Transactional
    void getDispenseLine() throws Exception {
        // Initialize the database
        insertedDispenseLine = dispenseLineRepository.saveAndFlush(dispenseLine);

        // Get the dispenseLine
        restDispenseLineMockMvc
            .perform(get(ENTITY_API_URL_ID, dispenseLine.getId()))
            .andExpect(status().isOk())
            .andExpect(content().contentType(MediaType.APPLICATION_JSON_VALUE))
            .andExpect(jsonPath("$.id").value(dispenseLine.getId().intValue()))
            .andExpect(jsonPath("$.quantity").value(DEFAULT_QUANTITY))
            .andExpect(jsonPath("$.substitutionReason").value(DEFAULT_SUBSTITUTION_REASON));
    }

    @Test
    @Transactional
    void getNonExistingDispenseLine() throws Exception {
        // Get the dispenseLine
        restDispenseLineMockMvc.perform(get(ENTITY_API_URL_ID, Long.MAX_VALUE)).andExpect(status().isNotFound());
    }

    @Test
    @Transactional
    void putExistingDispenseLine() throws Exception {
        // Initialize the database
        insertedDispenseLine = dispenseLineRepository.saveAndFlush(dispenseLine);

        long databaseSizeBeforeUpdate = getRepositoryCount();

        // Update the dispenseLine
        DispenseLine updatedDispenseLine = dispenseLineRepository.findById(dispenseLine.getId()).orElseThrow();
        // Disconnect from session so that the updates on updatedDispenseLine are not directly saved in db
        em.detach(updatedDispenseLine);
        updatedDispenseLine.quantity(UPDATED_QUANTITY).substitutionReason(UPDATED_SUBSTITUTION_REASON);
        DispenseLineDTO dispenseLineDTO = dispenseLineMapper.toDto(updatedDispenseLine);

        restDispenseLineMockMvc
            .perform(
                put(ENTITY_API_URL_ID, dispenseLineDTO.getId())
                    .contentType(MediaType.APPLICATION_JSON)
                    .content(om.writeValueAsBytes(dispenseLineDTO))
            )
            .andExpect(status().isOk());

        // Validate the DispenseLine in the database
        assertSameRepositoryCount(databaseSizeBeforeUpdate);
        assertPersistedDispenseLineToMatchAllProperties(updatedDispenseLine);
    }

    @Test
    @Transactional
    void putNonExistingDispenseLine() throws Exception {
        long databaseSizeBeforeUpdate = getRepositoryCount();
        dispenseLine.setId(longCount.incrementAndGet());

        // Create the DispenseLine
        DispenseLineDTO dispenseLineDTO = dispenseLineMapper.toDto(dispenseLine);

        // If the entity doesn't have an ID, it will throw BadRequestAlertException
        restDispenseLineMockMvc
            .perform(
                put(ENTITY_API_URL_ID, dispenseLineDTO.getId())
                    .contentType(MediaType.APPLICATION_JSON)
                    .content(om.writeValueAsBytes(dispenseLineDTO))
            )
            .andExpect(status().isBadRequest());

        // Validate the DispenseLine in the database
        assertSameRepositoryCount(databaseSizeBeforeUpdate);
    }

    @Test
    @Transactional
    void putWithIdMismatchDispenseLine() throws Exception {
        long databaseSizeBeforeUpdate = getRepositoryCount();
        dispenseLine.setId(longCount.incrementAndGet());

        // Create the DispenseLine
        DispenseLineDTO dispenseLineDTO = dispenseLineMapper.toDto(dispenseLine);

        // If url ID doesn't match entity ID, it will throw BadRequestAlertException
        restDispenseLineMockMvc
            .perform(
                put(ENTITY_API_URL_ID, longCount.incrementAndGet())
                    .contentType(MediaType.APPLICATION_JSON)
                    .content(om.writeValueAsBytes(dispenseLineDTO))
            )
            .andExpect(status().isBadRequest());

        // Validate the DispenseLine in the database
        assertSameRepositoryCount(databaseSizeBeforeUpdate);
    }

    @Test
    @Transactional
    void putWithMissingIdPathParamDispenseLine() throws Exception {
        long databaseSizeBeforeUpdate = getRepositoryCount();
        dispenseLine.setId(longCount.incrementAndGet());

        // Create the DispenseLine
        DispenseLineDTO dispenseLineDTO = dispenseLineMapper.toDto(dispenseLine);

        // If url ID doesn't match entity ID, it will throw BadRequestAlertException
        restDispenseLineMockMvc
            .perform(put(ENTITY_API_URL).contentType(MediaType.APPLICATION_JSON).content(om.writeValueAsBytes(dispenseLineDTO)))
            .andExpect(status().isMethodNotAllowed());

        // Validate the DispenseLine in the database
        assertSameRepositoryCount(databaseSizeBeforeUpdate);
    }

    @Test
    @Transactional
    void partialUpdateDispenseLineWithPatch() throws Exception {
        // Initialize the database
        insertedDispenseLine = dispenseLineRepository.saveAndFlush(dispenseLine);

        long databaseSizeBeforeUpdate = getRepositoryCount();

        // Update the dispenseLine using partial update
        DispenseLine partialUpdatedDispenseLine = new DispenseLine();
        partialUpdatedDispenseLine.setId(dispenseLine.getId());

        partialUpdatedDispenseLine.quantity(UPDATED_QUANTITY).substitutionReason(UPDATED_SUBSTITUTION_REASON);

        restDispenseLineMockMvc
            .perform(
                patch(ENTITY_API_URL_ID, partialUpdatedDispenseLine.getId())
                    .contentType("application/merge-patch+json")
                    .content(om.writeValueAsBytes(partialUpdatedDispenseLine))
            )
            .andExpect(status().isOk());

        // Validate the DispenseLine in the database

        assertSameRepositoryCount(databaseSizeBeforeUpdate);
        assertDispenseLineUpdatableFieldsEquals(
            createUpdateProxyForBean(partialUpdatedDispenseLine, dispenseLine),
            getPersistedDispenseLine(dispenseLine)
        );
    }

    @Test
    @Transactional
    void fullUpdateDispenseLineWithPatch() throws Exception {
        // Initialize the database
        insertedDispenseLine = dispenseLineRepository.saveAndFlush(dispenseLine);

        long databaseSizeBeforeUpdate = getRepositoryCount();

        // Update the dispenseLine using partial update
        DispenseLine partialUpdatedDispenseLine = new DispenseLine();
        partialUpdatedDispenseLine.setId(dispenseLine.getId());

        partialUpdatedDispenseLine.quantity(UPDATED_QUANTITY).substitutionReason(UPDATED_SUBSTITUTION_REASON);

        restDispenseLineMockMvc
            .perform(
                patch(ENTITY_API_URL_ID, partialUpdatedDispenseLine.getId())
                    .contentType("application/merge-patch+json")
                    .content(om.writeValueAsBytes(partialUpdatedDispenseLine))
            )
            .andExpect(status().isOk());

        // Validate the DispenseLine in the database

        assertSameRepositoryCount(databaseSizeBeforeUpdate);
        assertDispenseLineUpdatableFieldsEquals(partialUpdatedDispenseLine, getPersistedDispenseLine(partialUpdatedDispenseLine));
    }

    @Test
    @Transactional
    void patchNonExistingDispenseLine() throws Exception {
        long databaseSizeBeforeUpdate = getRepositoryCount();
        dispenseLine.setId(longCount.incrementAndGet());

        // Create the DispenseLine
        DispenseLineDTO dispenseLineDTO = dispenseLineMapper.toDto(dispenseLine);

        // If the entity doesn't have an ID, it will throw BadRequestAlertException
        restDispenseLineMockMvc
            .perform(
                patch(ENTITY_API_URL_ID, dispenseLineDTO.getId())
                    .contentType("application/merge-patch+json")
                    .content(om.writeValueAsBytes(dispenseLineDTO))
            )
            .andExpect(status().isBadRequest());

        // Validate the DispenseLine in the database
        assertSameRepositoryCount(databaseSizeBeforeUpdate);
    }

    @Test
    @Transactional
    void patchWithIdMismatchDispenseLine() throws Exception {
        long databaseSizeBeforeUpdate = getRepositoryCount();
        dispenseLine.setId(longCount.incrementAndGet());

        // Create the DispenseLine
        DispenseLineDTO dispenseLineDTO = dispenseLineMapper.toDto(dispenseLine);

        // If url ID doesn't match entity ID, it will throw BadRequestAlertException
        restDispenseLineMockMvc
            .perform(
                patch(ENTITY_API_URL_ID, longCount.incrementAndGet())
                    .contentType("application/merge-patch+json")
                    .content(om.writeValueAsBytes(dispenseLineDTO))
            )
            .andExpect(status().isBadRequest());

        // Validate the DispenseLine in the database
        assertSameRepositoryCount(databaseSizeBeforeUpdate);
    }

    @Test
    @Transactional
    void patchWithMissingIdPathParamDispenseLine() throws Exception {
        long databaseSizeBeforeUpdate = getRepositoryCount();
        dispenseLine.setId(longCount.incrementAndGet());

        // Create the DispenseLine
        DispenseLineDTO dispenseLineDTO = dispenseLineMapper.toDto(dispenseLine);

        // If url ID doesn't match entity ID, it will throw BadRequestAlertException
        restDispenseLineMockMvc
            .perform(patch(ENTITY_API_URL).contentType("application/merge-patch+json").content(om.writeValueAsBytes(dispenseLineDTO)))
            .andExpect(status().isMethodNotAllowed());

        // Validate the DispenseLine in the database
        assertSameRepositoryCount(databaseSizeBeforeUpdate);
    }

    @Test
    @Transactional
    void deleteDispenseLine() throws Exception {
        // Initialize the database
        insertedDispenseLine = dispenseLineRepository.saveAndFlush(dispenseLine);

        long databaseSizeBeforeDelete = getRepositoryCount();

        // Delete the dispenseLine
        restDispenseLineMockMvc
            .perform(delete(ENTITY_API_URL_ID, dispenseLine.getId()).accept(MediaType.APPLICATION_JSON))
            .andExpect(status().isNoContent());

        // Validate the database contains one less item
        assertDecrementedRepositoryCount(databaseSizeBeforeDelete);
    }

    protected long getRepositoryCount() {
        return dispenseLineRepository.count();
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

    protected DispenseLine getPersistedDispenseLine(DispenseLine dispenseLine) {
        return dispenseLineRepository.findById(dispenseLine.getId()).orElseThrow();
    }

    protected void assertPersistedDispenseLineToMatchAllProperties(DispenseLine expectedDispenseLine) {
        assertDispenseLineAllPropertiesEquals(expectedDispenseLine, getPersistedDispenseLine(expectedDispenseLine));
    }

    protected void assertPersistedDispenseLineToMatchUpdatableProperties(DispenseLine expectedDispenseLine) {
        assertDispenseLineAllUpdatablePropertiesEquals(expectedDispenseLine, getPersistedDispenseLine(expectedDispenseLine));
    }
}
