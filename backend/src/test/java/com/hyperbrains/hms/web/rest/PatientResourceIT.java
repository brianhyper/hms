package com.hyperbrains.hms.web.rest;

import static com.hyperbrains.hms.domain.PatientAsserts.*;
import static com.hyperbrains.hms.web.rest.TestUtil.createUpdateProxyForBean;
import static org.assertj.core.api.Assertions.assertThat;
import static org.hamcrest.Matchers.hasItem;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.hyperbrains.hms.IntegrationTest;
import com.hyperbrains.hms.domain.Patient;
import com.hyperbrains.hms.domain.enumeration.IdentityDocumentType;
import com.hyperbrains.hms.domain.enumeration.NextOfKinRelationship;
import com.hyperbrains.hms.domain.enumeration.RegistrationStatus;
import com.hyperbrains.hms.domain.enumeration.Sex;
import com.hyperbrains.hms.repository.PatientRepository;
import com.hyperbrains.hms.service.dto.PatientDTO;
import com.hyperbrains.hms.service.mapper.PatientMapper;
import jakarta.persistence.EntityManager;
import java.time.LocalDate;
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
 * Integration tests for the {@link PatientResource} REST controller.
 */
@IntegrationTest
@AutoConfigureMockMvc
// A direct update changes a patient's record with no reason and no history, so it is closed to clinical
// and desk roles. Correcting goes through /api/patient-corrections/{patientId}; this test covers the
// super-admin escape hatch.
@WithMockUser(authorities = AuthoritiesConstants.SUPER_ADMIN)
class PatientResourceIT {

    private static final String DEFAULT_HOSPITAL_ID = "AAAAAAAAAA";
    private static final String UPDATED_HOSPITAL_ID = "BBBBBBBBBB";

    private static final String DEFAULT_FULL_NAME = "AAAAAAAAAA";
    private static final String UPDATED_FULL_NAME = "BBBBBBBBBB";

    private static final LocalDate DEFAULT_DATE_OF_BIRTH = LocalDate.ofEpochDay(0L);
    private static final LocalDate UPDATED_DATE_OF_BIRTH = LocalDate.parse("2026-09-24");

    private static final Integer DEFAULT_ESTIMATED_AGE = 0;
    private static final Integer UPDATED_ESTIMATED_AGE = 1;

    private static final Sex DEFAULT_SEX = Sex.MALE;
    private static final Sex UPDATED_SEX = Sex.FEMALE;

    private static final Boolean DEFAULT_SEX_ESTIMATED = false;
    private static final Boolean UPDATED_SEX_ESTIMATED = true;

    private static final String DEFAULT_PHONE = "AAAAAAAAAA";
    private static final String UPDATED_PHONE = "BBBBBBBBBB";

    private static final String DEFAULT_EMAIL = "AAAAAAAAAA";
    private static final String UPDATED_EMAIL = "BBBBBBBBBB";

    private static final IdentityDocumentType DEFAULT_IDENTITY_DOCUMENT_TYPE = IdentityDocumentType.NATIONAL_ID;
    private static final IdentityDocumentType UPDATED_IDENTITY_DOCUMENT_TYPE = IdentityDocumentType.PASSPORT;

    private static final String DEFAULT_IDENTITY_DOCUMENT_NUMBER = "AAAAAAAAAA";
    private static final String UPDATED_IDENTITY_DOCUMENT_NUMBER = "BBBBBBBBBB";

    private static final String DEFAULT_OCCUPATION = "AAAAAAAAAA";
    private static final String UPDATED_OCCUPATION = "BBBBBBBBBB";

    private static final String DEFAULT_MARITAL_STATUS = "AAAAAAAAAA";
    private static final String UPDATED_MARITAL_STATUS = "BBBBBBBBBB";

    private static final String DEFAULT_NEXT_OF_KIN_NAME = "AAAAAAAAAA";
    private static final String UPDATED_NEXT_OF_KIN_NAME = "BBBBBBBBBB";

    private static final String DEFAULT_NEXT_OF_KIN_PHONE = "AAAAAAAAAA";
    private static final String UPDATED_NEXT_OF_KIN_PHONE = "BBBBBBBBBB";

    private static final NextOfKinRelationship DEFAULT_NEXT_OF_KIN_RELATIONSHIP = NextOfKinRelationship.PARENT;
    private static final NextOfKinRelationship UPDATED_NEXT_OF_KIN_RELATIONSHIP = NextOfKinRelationship.LEGAL_GUARDIAN;

    private static final String DEFAULT_KNOWN_ALLERGIES = "AAAAAAAAAA";
    private static final String UPDATED_KNOWN_ALLERGIES = "BBBBBBBBBB";

    private static final String DEFAULT_KNOWN_CONDITIONS = "AAAAAAAAAA";
    private static final String UPDATED_KNOWN_CONDITIONS = "BBBBBBBBBB";

    private static final String DEFAULT_VILLAGE_ESTATE = "AAAAAAAAAA";
    private static final String UPDATED_VILLAGE_ESTATE = "BBBBBBBBBB";

    private static final RegistrationStatus DEFAULT_REGISTRATION_STATUS = RegistrationStatus.COMPLETE;
    private static final RegistrationStatus UPDATED_REGISTRATION_STATUS = RegistrationStatus.INCOMPLETE_REGISTRATION;

    private static final String ENTITY_API_URL = "/api/patients";
    private static final String ENTITY_API_URL_ID = ENTITY_API_URL + "/{id}";

    private static final Random random = new Random();
    private static final AtomicLong longCount = new AtomicLong(random.nextInt() + 2L * Integer.MAX_VALUE);

    @Autowired
    private ObjectMapper om;

    @Autowired
    private PatientRepository patientRepository;

    @Autowired
    private PatientMapper patientMapper;

    @Autowired
    private EntityManager em;

    @Autowired
    private MockMvc restPatientMockMvc;

    private Patient patient;

    private Patient insertedPatient;

    /**
     * Create an entity for this test.
     *
     * This is a static method, as tests for other entities might also need it,
     * if they test an entity which requires the current entity.
     */
    public static Patient createEntity() {
        return new Patient()
            .hospitalId(DEFAULT_HOSPITAL_ID)
            .fullName(DEFAULT_FULL_NAME)
            .dateOfBirth(DEFAULT_DATE_OF_BIRTH)
            .estimatedAge(DEFAULT_ESTIMATED_AGE)
            .sex(DEFAULT_SEX)
            .sexEstimated(DEFAULT_SEX_ESTIMATED)
            .phone(DEFAULT_PHONE)
            .email(DEFAULT_EMAIL)
            .identityDocumentType(DEFAULT_IDENTITY_DOCUMENT_TYPE)
            .identityDocumentNumber(DEFAULT_IDENTITY_DOCUMENT_NUMBER)
            .occupation(DEFAULT_OCCUPATION)
            .maritalStatus(DEFAULT_MARITAL_STATUS)
            .nextOfKinName(DEFAULT_NEXT_OF_KIN_NAME)
            .nextOfKinPhone(DEFAULT_NEXT_OF_KIN_PHONE)
            .nextOfKinRelationship(DEFAULT_NEXT_OF_KIN_RELATIONSHIP)
            .knownAllergies(DEFAULT_KNOWN_ALLERGIES)
            .knownConditions(DEFAULT_KNOWN_CONDITIONS)
            .villageEstate(DEFAULT_VILLAGE_ESTATE)
            .registrationStatus(DEFAULT_REGISTRATION_STATUS);
    }

    /**
     * Create an updated entity for this test.
     *
     * This is a static method, as tests for other entities might also need it,
     * if they test an entity which requires the current entity.
     */
    public static Patient createUpdatedEntity() {
        return new Patient()
            .hospitalId(UPDATED_HOSPITAL_ID)
            .fullName(UPDATED_FULL_NAME)
            .dateOfBirth(UPDATED_DATE_OF_BIRTH)
            .estimatedAge(UPDATED_ESTIMATED_AGE)
            .sex(UPDATED_SEX)
            .sexEstimated(UPDATED_SEX_ESTIMATED)
            .phone(UPDATED_PHONE)
            .email(UPDATED_EMAIL)
            .identityDocumentType(UPDATED_IDENTITY_DOCUMENT_TYPE)
            .identityDocumentNumber(UPDATED_IDENTITY_DOCUMENT_NUMBER)
            .occupation(UPDATED_OCCUPATION)
            .maritalStatus(UPDATED_MARITAL_STATUS)
            .nextOfKinName(UPDATED_NEXT_OF_KIN_NAME)
            .nextOfKinPhone(UPDATED_NEXT_OF_KIN_PHONE)
            .nextOfKinRelationship(UPDATED_NEXT_OF_KIN_RELATIONSHIP)
            .knownAllergies(UPDATED_KNOWN_ALLERGIES)
            .knownConditions(UPDATED_KNOWN_CONDITIONS)
            .villageEstate(UPDATED_VILLAGE_ESTATE)
            .registrationStatus(UPDATED_REGISTRATION_STATUS);
    }

    @BeforeEach
    void initTest() {
        patient = createEntity();
    }

    @AfterEach
    void cleanup() {
        if (insertedPatient != null) {
            patientRepository.delete(insertedPatient);
            insertedPatient = null;
        }
    }

    @Test
    @Transactional
    void createPatient() throws Exception {
        long databaseSizeBeforeCreate = getRepositoryCount();
        // Create the Patient
        PatientDTO patientDTO = patientMapper.toDto(patient);
        var returnedPatientDTO = om.readValue(
            restPatientMockMvc
                .perform(post(ENTITY_API_URL).contentType(MediaType.APPLICATION_JSON).content(om.writeValueAsBytes(patientDTO)))
                .andExpect(status().isCreated())
                .andReturn()
                .getResponse()
                .getContentAsString(),
            PatientDTO.class
        );

        // Validate the Patient in the database
        assertIncrementedRepositoryCount(databaseSizeBeforeCreate);
        var returnedPatient = patientMapper.toEntity(returnedPatientDTO);
        assertPatientUpdatableFieldsEquals(returnedPatient, getPersistedPatient(returnedPatient));

        insertedPatient = returnedPatient;
    }

    @Test
    @Transactional
    void createPatientWithExistingId() throws Exception {
        // Create the Patient with an existing ID
        patient.setId(1L);
        PatientDTO patientDTO = patientMapper.toDto(patient);

        long databaseSizeBeforeCreate = getRepositoryCount();

        // An entity with an existing ID cannot be created, so this API call must fail
        restPatientMockMvc
            .perform(post(ENTITY_API_URL).contentType(MediaType.APPLICATION_JSON).content(om.writeValueAsBytes(patientDTO)))
            .andExpect(status().isBadRequest());

        // Validate the Patient in the database
        assertSameRepositoryCount(databaseSizeBeforeCreate);
    }

    @Test
    @Transactional
    void checkHospitalIdIsRequired() throws Exception {
        long databaseSizeBeforeTest = getRepositoryCount();
        // set the field null
        patient.setHospitalId(null);

        // Create the Patient, which fails.
        PatientDTO patientDTO = patientMapper.toDto(patient);

        restPatientMockMvc
            .perform(post(ENTITY_API_URL).contentType(MediaType.APPLICATION_JSON).content(om.writeValueAsBytes(patientDTO)))
            .andExpect(status().isBadRequest());

        assertSameRepositoryCount(databaseSizeBeforeTest);
    }

    @Test
    @Transactional
    void checkFullNameIsRequired() throws Exception {
        long databaseSizeBeforeTest = getRepositoryCount();
        // set the field null
        patient.setFullName(null);

        // Create the Patient, which fails.
        PatientDTO patientDTO = patientMapper.toDto(patient);

        restPatientMockMvc
            .perform(post(ENTITY_API_URL).contentType(MediaType.APPLICATION_JSON).content(om.writeValueAsBytes(patientDTO)))
            .andExpect(status().isBadRequest());

        assertSameRepositoryCount(databaseSizeBeforeTest);
    }

    @Test
    @Transactional
    void checkSexIsRequired() throws Exception {
        long databaseSizeBeforeTest = getRepositoryCount();
        // set the field null
        patient.setSex(null);

        // Create the Patient, which fails.
        PatientDTO patientDTO = patientMapper.toDto(patient);

        restPatientMockMvc
            .perform(post(ENTITY_API_URL).contentType(MediaType.APPLICATION_JSON).content(om.writeValueAsBytes(patientDTO)))
            .andExpect(status().isBadRequest());

        assertSameRepositoryCount(databaseSizeBeforeTest);
    }

    @Test
    @Transactional
    void checkSexEstimatedIsRequired() throws Exception {
        long databaseSizeBeforeTest = getRepositoryCount();
        // set the field null
        patient.setSexEstimated(null);

        // Create the Patient, which fails.
        PatientDTO patientDTO = patientMapper.toDto(patient);

        restPatientMockMvc
            .perform(post(ENTITY_API_URL).contentType(MediaType.APPLICATION_JSON).content(om.writeValueAsBytes(patientDTO)))
            .andExpect(status().isBadRequest());

        assertSameRepositoryCount(databaseSizeBeforeTest);
    }

    @Test
    @Transactional
    void checkRegistrationStatusIsRequired() throws Exception {
        long databaseSizeBeforeTest = getRepositoryCount();
        // set the field null
        patient.setRegistrationStatus(null);

        // Create the Patient, which fails.
        PatientDTO patientDTO = patientMapper.toDto(patient);

        restPatientMockMvc
            .perform(post(ENTITY_API_URL).contentType(MediaType.APPLICATION_JSON).content(om.writeValueAsBytes(patientDTO)))
            .andExpect(status().isBadRequest());

        assertSameRepositoryCount(databaseSizeBeforeTest);
    }

    @Test
    @Transactional
    void getAllPatients() throws Exception {
        // Initialize the database
        insertedPatient = patientRepository.saveAndFlush(patient);

        // Get all the patientList
        restPatientMockMvc
            .perform(get(ENTITY_API_URL + "?sort=id,desc"))
            .andExpect(status().isOk())
            .andExpect(content().contentType(MediaType.APPLICATION_JSON_VALUE))
            .andExpect(jsonPath("$.[*].id").value(hasItem(patient.getId().intValue())))
            .andExpect(jsonPath("$.[*].hospitalId").value(hasItem(DEFAULT_HOSPITAL_ID)))
            .andExpect(jsonPath("$.[*].fullName").value(hasItem(DEFAULT_FULL_NAME)))
            .andExpect(jsonPath("$.[*].dateOfBirth").value(hasItem(DEFAULT_DATE_OF_BIRTH.toString())))
            .andExpect(jsonPath("$.[*].estimatedAge").value(hasItem(DEFAULT_ESTIMATED_AGE)))
            .andExpect(jsonPath("$.[*].sex").value(hasItem(DEFAULT_SEX.toString())))
            .andExpect(jsonPath("$.[*].sexEstimated").value(hasItem(DEFAULT_SEX_ESTIMATED)))
            .andExpect(jsonPath("$.[*].phone").value(hasItem(DEFAULT_PHONE)))
            .andExpect(jsonPath("$.[*].email").value(hasItem(DEFAULT_EMAIL)))
            .andExpect(jsonPath("$.[*].identityDocumentType").value(hasItem(DEFAULT_IDENTITY_DOCUMENT_TYPE.toString())))
            .andExpect(jsonPath("$.[*].identityDocumentNumber").value(hasItem(DEFAULT_IDENTITY_DOCUMENT_NUMBER)))
            .andExpect(jsonPath("$.[*].occupation").value(hasItem(DEFAULT_OCCUPATION)))
            .andExpect(jsonPath("$.[*].maritalStatus").value(hasItem(DEFAULT_MARITAL_STATUS)))
            .andExpect(jsonPath("$.[*].nextOfKinName").value(hasItem(DEFAULT_NEXT_OF_KIN_NAME)))
            .andExpect(jsonPath("$.[*].nextOfKinPhone").value(hasItem(DEFAULT_NEXT_OF_KIN_PHONE)))
            .andExpect(jsonPath("$.[*].nextOfKinRelationship").value(hasItem(DEFAULT_NEXT_OF_KIN_RELATIONSHIP.toString())))
            .andExpect(jsonPath("$.[*].knownAllergies").value(hasItem(DEFAULT_KNOWN_ALLERGIES)))
            .andExpect(jsonPath("$.[*].knownConditions").value(hasItem(DEFAULT_KNOWN_CONDITIONS)))
            .andExpect(jsonPath("$.[*].villageEstate").value(hasItem(DEFAULT_VILLAGE_ESTATE)))
            .andExpect(jsonPath("$.[*].registrationStatus").value(hasItem(DEFAULT_REGISTRATION_STATUS.toString())));
    }

    @Test
    @Transactional
    void getPatient() throws Exception {
        // Initialize the database
        insertedPatient = patientRepository.saveAndFlush(patient);

        // Get the patient
        restPatientMockMvc
            .perform(get(ENTITY_API_URL_ID, patient.getId()))
            .andExpect(status().isOk())
            .andExpect(content().contentType(MediaType.APPLICATION_JSON_VALUE))
            .andExpect(jsonPath("$.id").value(patient.getId().intValue()))
            .andExpect(jsonPath("$.hospitalId").value(DEFAULT_HOSPITAL_ID))
            .andExpect(jsonPath("$.fullName").value(DEFAULT_FULL_NAME))
            .andExpect(jsonPath("$.dateOfBirth").value(DEFAULT_DATE_OF_BIRTH.toString()))
            .andExpect(jsonPath("$.estimatedAge").value(DEFAULT_ESTIMATED_AGE))
            .andExpect(jsonPath("$.sex").value(DEFAULT_SEX.toString()))
            .andExpect(jsonPath("$.sexEstimated").value(DEFAULT_SEX_ESTIMATED))
            .andExpect(jsonPath("$.phone").value(DEFAULT_PHONE))
            .andExpect(jsonPath("$.email").value(DEFAULT_EMAIL))
            .andExpect(jsonPath("$.identityDocumentType").value(DEFAULT_IDENTITY_DOCUMENT_TYPE.toString()))
            .andExpect(jsonPath("$.identityDocumentNumber").value(DEFAULT_IDENTITY_DOCUMENT_NUMBER))
            .andExpect(jsonPath("$.occupation").value(DEFAULT_OCCUPATION))
            .andExpect(jsonPath("$.maritalStatus").value(DEFAULT_MARITAL_STATUS))
            .andExpect(jsonPath("$.nextOfKinName").value(DEFAULT_NEXT_OF_KIN_NAME))
            .andExpect(jsonPath("$.nextOfKinPhone").value(DEFAULT_NEXT_OF_KIN_PHONE))
            .andExpect(jsonPath("$.nextOfKinRelationship").value(DEFAULT_NEXT_OF_KIN_RELATIONSHIP.toString()))
            .andExpect(jsonPath("$.knownAllergies").value(DEFAULT_KNOWN_ALLERGIES))
            .andExpect(jsonPath("$.knownConditions").value(DEFAULT_KNOWN_CONDITIONS))
            .andExpect(jsonPath("$.villageEstate").value(DEFAULT_VILLAGE_ESTATE))
            .andExpect(jsonPath("$.registrationStatus").value(DEFAULT_REGISTRATION_STATUS.toString()));
    }

    @Test
    @Transactional
    void getNonExistingPatient() throws Exception {
        // Get the patient
        restPatientMockMvc.perform(get(ENTITY_API_URL_ID, Long.MAX_VALUE)).andExpect(status().isNotFound());
    }

    @Test
    @Transactional
    void putExistingPatient() throws Exception {
        // Initialize the database
        insertedPatient = patientRepository.saveAndFlush(patient);

        long databaseSizeBeforeUpdate = getRepositoryCount();

        // Update the patient
        Patient updatedPatient = patientRepository.findById(patient.getId()).orElseThrow();
        // Disconnect from session so that the updates on updatedPatient are not directly saved in db
        em.detach(updatedPatient);
        updatedPatient
            .hospitalId(UPDATED_HOSPITAL_ID)
            .fullName(UPDATED_FULL_NAME)
            .dateOfBirth(UPDATED_DATE_OF_BIRTH)
            .estimatedAge(UPDATED_ESTIMATED_AGE)
            .sex(UPDATED_SEX)
            .sexEstimated(UPDATED_SEX_ESTIMATED)
            .phone(UPDATED_PHONE)
            .email(UPDATED_EMAIL)
            .identityDocumentType(UPDATED_IDENTITY_DOCUMENT_TYPE)
            .identityDocumentNumber(UPDATED_IDENTITY_DOCUMENT_NUMBER)
            .occupation(UPDATED_OCCUPATION)
            .maritalStatus(UPDATED_MARITAL_STATUS)
            .nextOfKinName(UPDATED_NEXT_OF_KIN_NAME)
            .nextOfKinPhone(UPDATED_NEXT_OF_KIN_PHONE)
            .nextOfKinRelationship(UPDATED_NEXT_OF_KIN_RELATIONSHIP)
            .knownAllergies(UPDATED_KNOWN_ALLERGIES)
            .knownConditions(UPDATED_KNOWN_CONDITIONS)
            .villageEstate(UPDATED_VILLAGE_ESTATE)
            .registrationStatus(UPDATED_REGISTRATION_STATUS);
        PatientDTO patientDTO = patientMapper.toDto(updatedPatient);

        restPatientMockMvc
            .perform(
                put(ENTITY_API_URL_ID, patientDTO.getId()).contentType(MediaType.APPLICATION_JSON).content(om.writeValueAsBytes(patientDTO))
            )
            .andExpect(status().isOk());

        // Validate the Patient in the database
        assertSameRepositoryCount(databaseSizeBeforeUpdate);
        assertPersistedPatientToMatchAllProperties(updatedPatient);
    }

    @Test
    @Transactional
    void putNonExistingPatient() throws Exception {
        long databaseSizeBeforeUpdate = getRepositoryCount();
        patient.setId(longCount.incrementAndGet());

        // Create the Patient
        PatientDTO patientDTO = patientMapper.toDto(patient);

        // If the entity doesn't have an ID, it will throw BadRequestAlertException
        restPatientMockMvc
            .perform(
                put(ENTITY_API_URL_ID, patientDTO.getId()).contentType(MediaType.APPLICATION_JSON).content(om.writeValueAsBytes(patientDTO))
            )
            .andExpect(status().isBadRequest());

        // Validate the Patient in the database
        assertSameRepositoryCount(databaseSizeBeforeUpdate);
    }

    @Test
    @Transactional
    void putWithIdMismatchPatient() throws Exception {
        long databaseSizeBeforeUpdate = getRepositoryCount();
        patient.setId(longCount.incrementAndGet());

        // Create the Patient
        PatientDTO patientDTO = patientMapper.toDto(patient);

        // If url ID doesn't match entity ID, it will throw BadRequestAlertException
        restPatientMockMvc
            .perform(
                put(ENTITY_API_URL_ID, longCount.incrementAndGet())
                    .contentType(MediaType.APPLICATION_JSON)
                    .content(om.writeValueAsBytes(patientDTO))
            )
            .andExpect(status().isBadRequest());

        // Validate the Patient in the database
        assertSameRepositoryCount(databaseSizeBeforeUpdate);
    }

    @Test
    @Transactional
    void putWithMissingIdPathParamPatient() throws Exception {
        long databaseSizeBeforeUpdate = getRepositoryCount();
        patient.setId(longCount.incrementAndGet());

        // Create the Patient
        PatientDTO patientDTO = patientMapper.toDto(patient);

        // If url ID doesn't match entity ID, it will throw BadRequestAlertException
        restPatientMockMvc
            .perform(put(ENTITY_API_URL).contentType(MediaType.APPLICATION_JSON).content(om.writeValueAsBytes(patientDTO)))
            .andExpect(status().isMethodNotAllowed());

        // Validate the Patient in the database
        assertSameRepositoryCount(databaseSizeBeforeUpdate);
    }

    @Test
    @Transactional
    void partialUpdatePatientWithPatch() throws Exception {
        // Initialize the database
        insertedPatient = patientRepository.saveAndFlush(patient);

        long databaseSizeBeforeUpdate = getRepositoryCount();

        // Update the patient using partial update
        Patient partialUpdatedPatient = new Patient();
        partialUpdatedPatient.setId(patient.getId());

        partialUpdatedPatient
            .fullName(UPDATED_FULL_NAME)
            .dateOfBirth(UPDATED_DATE_OF_BIRTH)
            .estimatedAge(UPDATED_ESTIMATED_AGE)
            .sexEstimated(UPDATED_SEX_ESTIMATED)
            .occupation(UPDATED_OCCUPATION)
            .maritalStatus(UPDATED_MARITAL_STATUS)
            .nextOfKinName(UPDATED_NEXT_OF_KIN_NAME)
            .villageEstate(UPDATED_VILLAGE_ESTATE);

        restPatientMockMvc
            .perform(
                patch(ENTITY_API_URL_ID, partialUpdatedPatient.getId())
                    .contentType("application/merge-patch+json")
                    .content(om.writeValueAsBytes(partialUpdatedPatient))
            )
            .andExpect(status().isOk());

        // Validate the Patient in the database

        assertSameRepositoryCount(databaseSizeBeforeUpdate);
        assertPatientUpdatableFieldsEquals(createUpdateProxyForBean(partialUpdatedPatient, patient), getPersistedPatient(patient));
    }

    @Test
    @Transactional
    void fullUpdatePatientWithPatch() throws Exception {
        // Initialize the database
        insertedPatient = patientRepository.saveAndFlush(patient);

        long databaseSizeBeforeUpdate = getRepositoryCount();

        // Update the patient using partial update
        Patient partialUpdatedPatient = new Patient();
        partialUpdatedPatient.setId(patient.getId());

        partialUpdatedPatient
            .hospitalId(UPDATED_HOSPITAL_ID)
            .fullName(UPDATED_FULL_NAME)
            .dateOfBirth(UPDATED_DATE_OF_BIRTH)
            .estimatedAge(UPDATED_ESTIMATED_AGE)
            .sex(UPDATED_SEX)
            .sexEstimated(UPDATED_SEX_ESTIMATED)
            .phone(UPDATED_PHONE)
            .email(UPDATED_EMAIL)
            .identityDocumentType(UPDATED_IDENTITY_DOCUMENT_TYPE)
            .identityDocumentNumber(UPDATED_IDENTITY_DOCUMENT_NUMBER)
            .occupation(UPDATED_OCCUPATION)
            .maritalStatus(UPDATED_MARITAL_STATUS)
            .nextOfKinName(UPDATED_NEXT_OF_KIN_NAME)
            .nextOfKinPhone(UPDATED_NEXT_OF_KIN_PHONE)
            .nextOfKinRelationship(UPDATED_NEXT_OF_KIN_RELATIONSHIP)
            .knownAllergies(UPDATED_KNOWN_ALLERGIES)
            .knownConditions(UPDATED_KNOWN_CONDITIONS)
            .villageEstate(UPDATED_VILLAGE_ESTATE)
            .registrationStatus(UPDATED_REGISTRATION_STATUS);

        restPatientMockMvc
            .perform(
                patch(ENTITY_API_URL_ID, partialUpdatedPatient.getId())
                    .contentType("application/merge-patch+json")
                    .content(om.writeValueAsBytes(partialUpdatedPatient))
            )
            .andExpect(status().isOk());

        // Validate the Patient in the database

        assertSameRepositoryCount(databaseSizeBeforeUpdate);
        assertPatientUpdatableFieldsEquals(partialUpdatedPatient, getPersistedPatient(partialUpdatedPatient));
    }

    @Test
    @Transactional
    void patchNonExistingPatient() throws Exception {
        long databaseSizeBeforeUpdate = getRepositoryCount();
        patient.setId(longCount.incrementAndGet());

        // Create the Patient
        PatientDTO patientDTO = patientMapper.toDto(patient);

        // If the entity doesn't have an ID, it will throw BadRequestAlertException
        restPatientMockMvc
            .perform(
                patch(ENTITY_API_URL_ID, patientDTO.getId())
                    .contentType("application/merge-patch+json")
                    .content(om.writeValueAsBytes(patientDTO))
            )
            .andExpect(status().isBadRequest());

        // Validate the Patient in the database
        assertSameRepositoryCount(databaseSizeBeforeUpdate);
    }

    @Test
    @Transactional
    void patchWithIdMismatchPatient() throws Exception {
        long databaseSizeBeforeUpdate = getRepositoryCount();
        patient.setId(longCount.incrementAndGet());

        // Create the Patient
        PatientDTO patientDTO = patientMapper.toDto(patient);

        // If url ID doesn't match entity ID, it will throw BadRequestAlertException
        restPatientMockMvc
            .perform(
                patch(ENTITY_API_URL_ID, longCount.incrementAndGet())
                    .contentType("application/merge-patch+json")
                    .content(om.writeValueAsBytes(patientDTO))
            )
            .andExpect(status().isBadRequest());

        // Validate the Patient in the database
        assertSameRepositoryCount(databaseSizeBeforeUpdate);
    }

    @Test
    @Transactional
    void patchWithMissingIdPathParamPatient() throws Exception {
        long databaseSizeBeforeUpdate = getRepositoryCount();
        patient.setId(longCount.incrementAndGet());

        // Create the Patient
        PatientDTO patientDTO = patientMapper.toDto(patient);

        // If url ID doesn't match entity ID, it will throw BadRequestAlertException
        restPatientMockMvc
            .perform(patch(ENTITY_API_URL).contentType("application/merge-patch+json").content(om.writeValueAsBytes(patientDTO)))
            .andExpect(status().isMethodNotAllowed());

        // Validate the Patient in the database
        assertSameRepositoryCount(databaseSizeBeforeUpdate);
    }

    @Test
    @Transactional
    void deletePatient() throws Exception {
        // Initialize the database
        insertedPatient = patientRepository.saveAndFlush(patient);

        long databaseSizeBeforeDelete = getRepositoryCount();

        // Delete the patient
        restPatientMockMvc
            .perform(delete(ENTITY_API_URL_ID, patient.getId()).accept(MediaType.APPLICATION_JSON))
            .andExpect(status().isNoContent());

        // Validate the database contains one less item
        assertDecrementedRepositoryCount(databaseSizeBeforeDelete);
    }

    protected long getRepositoryCount() {
        return patientRepository.count();
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

    protected Patient getPersistedPatient(Patient patient) {
        return patientRepository.findById(patient.getId()).orElseThrow();
    }

    protected void assertPersistedPatientToMatchAllProperties(Patient expectedPatient) {
        assertPatientAllPropertiesEquals(expectedPatient, getPersistedPatient(expectedPatient));
    }

    protected void assertPersistedPatientToMatchUpdatableProperties(Patient expectedPatient) {
        assertPatientAllUpdatablePropertiesEquals(expectedPatient, getPersistedPatient(expectedPatient));
    }
}
