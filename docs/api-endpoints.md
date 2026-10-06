# HMS API endpoints

Generated from the running application on 2026-10-06.

292 routes across 65 controllers, all under `/api`.

This is the mapping table, not the permission table. Which role may reach a route is declared in the
`PHASE 1 RBAC TABLE` of `SecurityConfiguration`; a route listed here can still be `denyAll()` or
restricted to a few roles.

## AccountResource

| Method | Path | Handler |
|---|---|---|
| GET | `/api/account` | `getAccount` |
| POST | `/api/account` | `saveAccount` |
| POST | `/api/account/change-password` | `changePassword` |
| POST | `/api/account/reset-password/finish` | `finishPasswordReset` |
| POST | `/api/account/reset-password/init` | `requestPasswordReset` |
| GET | `/api/activate` | `activateAccount` |
| POST | `/api/register` | `registerAccount` |

## AdHocChargeResource

| Method | Path | Handler |
|---|---|---|
| GET | `/api/ad-hoc-charges` | `getAllAdHocCharges` |
| POST | `/api/ad-hoc-charges` | `createAdHocCharge` |
| DELETE | `/api/ad-hoc-charges/{id}` | `deleteAdHocCharge` |
| GET | `/api/ad-hoc-charges/{id}` | `getAdHocCharge` |
| PATCH | `/api/ad-hoc-charges/{id}` | `partialUpdateAdHocCharge` |
| PUT | `/api/ad-hoc-charges/{id}` | `updateAdHocCharge` |

## AdmissionBedResource

| Method | Path | Handler |
|---|---|---|
| PUT | `/api/admissions/{admissionId}/bed` | `assignBed` |

## AdmissionResource

| Method | Path | Handler |
|---|---|---|
| GET | `/api/admissions` | `getAllAdmissions` |
| POST | `/api/admissions` | `createAdmission` |
| DELETE | `/api/admissions/{id}` | `deleteAdmission` |
| GET | `/api/admissions/{id}` | `getAdmission` |
| PATCH | `/api/admissions/{id}` | `partialUpdateAdmission` |
| PUT | `/api/admissions/{id}` | `updateAdmission` |

## AdmissionTransferResource

| Method | Path | Handler |
|---|---|---|
| GET | `/api/admission-transfers` | `getAllAdmissionTransfers` |
| POST | `/api/admission-transfers` | `createAdmissionTransfer` |
| DELETE | `/api/admission-transfers/{id}` | `deleteAdmissionTransfer` |
| GET | `/api/admission-transfers/{id}` | `getAdmissionTransfer` |
| PATCH | `/api/admission-transfers/{id}` | `partialUpdateAdmissionTransfer` |
| PUT | `/api/admission-transfers/{id}` | `updateAdmissionTransfer` |

## AppointmentResource

| Method | Path | Handler |
|---|---|---|
| GET | `/api/appointments` | `getAllAppointments` |
| POST | `/api/appointments` | `createAppointment` |
| DELETE | `/api/appointments/{id}` | `deleteAppointment` |
| GET | `/api/appointments/{id}` | `getAppointment` |
| PATCH | `/api/appointments/{id}` | `partialUpdateAppointment` |
| PUT | `/api/appointments/{id}` | `updateAppointment` |

## AuditLogResource

| Method | Path | Handler |
|---|---|---|
| GET | `/api/audit-logs` | `getAllAuditLogs` |
| GET | `/api/audit-logs/{id}` | `getAuditLog` |

## AuthenticateController

| Method | Path | Handler |
|---|---|---|
| GET | `/api/authenticate` | `isAuthenticated` |
| POST | `/api/authenticate` | `authorize` |

## AuthorityResource

| Method | Path | Handler |
|---|---|---|
| GET | `/api/authorities` | `getAllAuthorities` |
| POST | `/api/authorities` | `createAuthority` |
| DELETE | `/api/authorities/{id}` | `deleteAuthority` |
| GET | `/api/authorities/{id}` | `getAuthority` |

## BedAvailabilityResource

| Method | Path | Handler |
|---|---|---|
| GET | `/api/bed-availability` | `available` |
| GET | `/api/bed-availability/wards` | `wards` |

## BedResource

| Method | Path | Handler |
|---|---|---|
| GET | `/api/beds` | `getAllBeds` |
| POST | `/api/beds` | `createBed` |
| DELETE | `/api/beds/{id}` | `deleteBed` |
| GET | `/api/beds/{id}` | `getBed` |
| PATCH | `/api/beds/{id}` | `partialUpdateBed` |
| PUT | `/api/beds/{id}` | `updateBed` |

## BedStatusResource

| Method | Path | Handler |
|---|---|---|
| PUT | `/api/beds/{bedId}/available` | `markAvailable` |
| PUT | `/api/beds/{bedId}/maintenance` | `markMaintenance` |

## BedTypeResource

| Method | Path | Handler |
|---|---|---|
| GET | `/api/bed-types` | `getAllBedTypes` |
| POST | `/api/bed-types` | `createBedType` |
| DELETE | `/api/bed-types/{id}` | `deleteBedType` |
| GET | `/api/bed-types/{id}` | `getBedType` |
| PATCH | `/api/bed-types/{id}` | `partialUpdateBedType` |
| PUT | `/api/bed-types/{id}` | `updateBedType` |

## BillLineItemResource

| Method | Path | Handler |
|---|---|---|
| GET | `/api/bill-line-items` | `getAllBillLineItems` |
| POST | `/api/bill-line-items` | `createBillLineItem` |
| DELETE | `/api/bill-line-items/{id}` | `deleteBillLineItem` |
| GET | `/api/bill-line-items/{id}` | `getBillLineItem` |
| PATCH | `/api/bill-line-items/{id}` | `partialUpdateBillLineItem` |
| PUT | `/api/bill-line-items/{id}` | `updateBillLineItem` |

## BillResource

| Method | Path | Handler |
|---|---|---|
| GET | `/api/bills` | `getAllBills` |
| POST | `/api/bills` | `createBill` |
| DELETE | `/api/bills/{id}` | `deleteBill` |
| GET | `/api/bills/{id}` | `getBill` |
| PATCH | `/api/bills/{id}` | `partialUpdateBill` |
| PUT | `/api/bills/{id}` | `updateBill` |

## ConsultationResource

| Method | Path | Handler |
|---|---|---|
| GET | `/api/consultations` | `getAllConsultations` |
| POST | `/api/consultations` | `createConsultation` |
| DELETE | `/api/consultations/{id}` | `deleteConsultation` |
| GET | `/api/consultations/{id}` | `getConsultation` |
| PATCH | `/api/consultations/{id}` | `partialUpdateConsultation` |
| PUT | `/api/consultations/{id}` | `updateConsultation` |

## DepartmentResource

| Method | Path | Handler |
|---|---|---|
| GET | `/api/departments` | `getAllDepartments` |
| POST | `/api/departments` | `createDepartment` |
| DELETE | `/api/departments/{id}` | `deleteDepartment` |
| GET | `/api/departments/{id}` | `getDepartment` |
| PATCH | `/api/departments/{id}` | `partialUpdateDepartment` |
| PUT | `/api/departments/{id}` | `updateDepartment` |

## DiagnosisResource

| Method | Path | Handler |
|---|---|---|
| GET | `/api/diagnoses` | `getAllDiagnoses` |
| POST | `/api/diagnoses` | `createDiagnosis` |
| DELETE | `/api/diagnoses/{id}` | `deleteDiagnosis` |
| GET | `/api/diagnoses/{id}` | `getDiagnosis` |
| PATCH | `/api/diagnoses/{id}` | `partialUpdateDiagnosis` |
| PUT | `/api/diagnoses/{id}` | `updateDiagnosis` |

## DiagnosticOrderResource

| Method | Path | Handler |
|---|---|---|
| GET | `/api/diagnostic-orders` | `getAllDiagnosticOrders` |
| POST | `/api/diagnostic-orders` | `createDiagnosticOrder` |
| DELETE | `/api/diagnostic-orders/{id}` | `deleteDiagnosticOrder` |
| GET | `/api/diagnostic-orders/{id}` | `getDiagnosticOrder` |
| PATCH | `/api/diagnostic-orders/{id}` | `partialUpdateDiagnosticOrder` |
| PUT | `/api/diagnostic-orders/{id}` | `updateDiagnosticOrder` |

## DispenseLineResource

| Method | Path | Handler |
|---|---|---|
| GET | `/api/dispense-lines` | `getAllDispenseLines` |
| POST | `/api/dispense-lines` | `createDispenseLine` |
| DELETE | `/api/dispense-lines/{id}` | `deleteDispenseLine` |
| GET | `/api/dispense-lines/{id}` | `getDispenseLine` |
| PATCH | `/api/dispense-lines/{id}` | `partialUpdateDispenseLine` |
| PUT | `/api/dispense-lines/{id}` | `updateDispenseLine` |

## DispenseResource

| Method | Path | Handler |
|---|---|---|
| GET | `/api/dispenses` | `getAllDispenses` |
| POST | `/api/dispenses` | `createDispense` |
| DELETE | `/api/dispenses/{id}` | `deleteDispense` |
| GET | `/api/dispenses/{id}` | `getDispense` |
| PATCH | `/api/dispenses/{id}` | `partialUpdateDispense` |
| PUT | `/api/dispenses/{id}` | `updateDispense` |

## DoctorOrderResource

| Method | Path | Handler |
|---|---|---|
| GET | `/api/doctor-orders` | `getAllDoctorOrders` |
| POST | `/api/doctor-orders` | `createDoctorOrder` |
| DELETE | `/api/doctor-orders/{id}` | `deleteDoctorOrder` |
| GET | `/api/doctor-orders/{id}` | `getDoctorOrder` |
| PATCH | `/api/doctor-orders/{id}` | `partialUpdateDoctorOrder` |
| PUT | `/api/doctor-orders/{id}` | `updateDoctorOrder` |

## DrugResource

| Method | Path | Handler |
|---|---|---|
| GET | `/api/drugs` | `getAllDrugs` |
| POST | `/api/drugs` | `createDrug` |
| DELETE | `/api/drugs/{id}` | `deleteDrug` |
| GET | `/api/drugs/{id}` | `getDrug` |
| PATCH | `/api/drugs/{id}` | `partialUpdateDrug` |
| PUT | `/api/drugs/{id}` | `updateDrug` |

## ExceptionTranslatorTestController

| Method | Path | Handler |
|---|---|---|
| GET | `/api/exception-translator-test/access-denied` | `accessdenied` |
| GET | `/api/exception-translator-test/concurrency-failure` | `concurrencyFailure` |
| GET | `/api/exception-translator-test/internal-server-error` | `internalServerError` |
| POST | `/api/exception-translator-test/method-argument` | `methodArgument` |
| GET | `/api/exception-translator-test/missing-servlet-request-parameter` | `missingServletRequestParameterException` |
| GET | `/api/exception-translator-test/missing-servlet-request-part` | `missingServletRequestPartException` |
| GET | `/api/exception-translator-test/response-status` | `exceptionWithResponseStatus` |
| GET | `/api/exception-translator-test/unauthorized` | `unauthorized` |

## HospitalServiceResource

| Method | Path | Handler |
|---|---|---|
| GET | `/api/hospital-services` | `getAllHospitalServices` |
| POST | `/api/hospital-services` | `createHospitalService` |
| DELETE | `/api/hospital-services/{id}` | `deleteHospitalService` |
| GET | `/api/hospital-services/{id}` | `getHospitalService` |
| PATCH | `/api/hospital-services/{id}` | `partialUpdateHospitalService` |
| PUT | `/api/hospital-services/{id}` | `updateHospitalService` |

## InpatientChartingResource

| Method | Path | Handler |
|---|---|---|
| GET | `/api/inpatient-charting/{admissionId}/vitals` | `chart` |
| POST | `/api/inpatient-charting/{admissionId}/vitals` | `chart` |
| POST | `/api/inpatient-charting/{admissionId}/vitals/{vitalsId}/corrections` | `correct` |

## InpatientOrderResource

| Method | Path | Handler |
|---|---|---|
| GET | `/api/inpatient-orders/admission/{admissionId}` | `orderSheet` |
| POST | `/api/inpatient-orders/{admissionId}` | `place` |
| PUT | `/api/inpatient-orders/{orderId}/cancel` | `cancel` |
| PUT | `/api/inpatient-orders/{orderId}/complete` | `complete` |
| POST | `/api/inpatient-orders/{orderId}/executions` | `execute` |

## InpatientVitalsResource

| Method | Path | Handler |
|---|---|---|
| GET | `/api/inpatient-vitals` | `getAllInpatientVitalses` |
| POST | `/api/inpatient-vitals` | `createInpatientVitals` |
| DELETE | `/api/inpatient-vitals/{id}` | `deleteInpatientVitals` |
| GET | `/api/inpatient-vitals/{id}` | `getInpatientVitals` |
| PATCH | `/api/inpatient-vitals/{id}` | `partialUpdateInpatientVitals` |
| PUT | `/api/inpatient-vitals/{id}` | `updateInpatientVitals` |

## InpatientWorklistResource

| Method | Path | Handler |
|---|---|---|
| GET | `/api/inpatient-worklist/awaiting-bed` | `awaitingBed` |
| GET | `/api/inpatient-worklist/my-patients` | `myPatients` |

## LabTestResource

| Method | Path | Handler |
|---|---|---|
| GET | `/api/lab-tests` | `getAllLabTests` |
| POST | `/api/lab-tests` | `createLabTest` |
| DELETE | `/api/lab-tests/{id}` | `deleteLabTest` |
| GET | `/api/lab-tests/{id}` | `getLabTest` |
| PATCH | `/api/lab-tests/{id}` | `partialUpdateLabTest` |
| PUT | `/api/lab-tests/{id}` | `updateLabTest` |

## OrderExecutionResource

| Method | Path | Handler |
|---|---|---|
| GET | `/api/order-executions` | `getAllOrderExecutions` |
| POST | `/api/order-executions` | `createOrderExecution` |
| DELETE | `/api/order-executions/{id}` | `deleteOrderExecution` |
| GET | `/api/order-executions/{id}` | `getOrderExecution` |
| PATCH | `/api/order-executions/{id}` | `partialUpdateOrderExecution` |
| PUT | `/api/order-executions/{id}` | `updateOrderExecution` |

## PatientAccessLogResource

| Method | Path | Handler |
|---|---|---|
| GET | `/api/patient-access-logs` | `getAccessLog` |

## PatientCorrectionResource

| Method | Path | Handler |
|---|---|---|
| PUT | `/api/patient-corrections/{patientId}` | `correct` |

## PatientMergeResource

| Method | Path | Handler |
|---|---|---|
| POST | `/api/patient-merges` | `merge` |

## PatientRegistrationResource

| Method | Path | Handler |
|---|---|---|
| POST | `/api/patient-registration/duplicate-check` | `checkForDuplicates` |
| POST | `/api/patient-registration/emergency-intake` | `emergencyIntake` |
| GET | `/api/patient-registration/identity-pending` | `identityPendingWorklist` |
| POST | `/api/patient-registration/register` | `register` |

## PatientResource

| Method | Path | Handler |
|---|---|---|
| GET | `/api/patients` | `getAllPatients` |
| POST | `/api/patients` | `createPatient` |
| DELETE | `/api/patients/{id}` | `deletePatient` |
| GET | `/api/patients/{id}` | `getPatient` |
| PATCH | `/api/patients/{id}` | `partialUpdatePatient` |
| PUT | `/api/patients/{id}` | `updatePatient` |

## PaymentPlanResource

| Method | Path | Handler |
|---|---|---|
| GET | `/api/payment-plans` | `getAllPaymentPlans` |
| POST | `/api/payment-plans` | `createPaymentPlan` |
| DELETE | `/api/payment-plans/{id}` | `deletePaymentPlan` |
| GET | `/api/payment-plans/{id}` | `getPaymentPlan` |
| PATCH | `/api/payment-plans/{id}` | `partialUpdatePaymentPlan` |
| PUT | `/api/payment-plans/{id}` | `updatePaymentPlan` |

## PaymentResource

| Method | Path | Handler |
|---|---|---|
| GET | `/api/payments` | `getAllPayments` |
| POST | `/api/payments` | `createPayment` |
| DELETE | `/api/payments/{id}` | `deletePayment` |
| GET | `/api/payments/{id}` | `getPayment` |
| PATCH | `/api/payments/{id}` | `partialUpdatePayment` |
| PUT | `/api/payments/{id}` | `updatePayment` |

## PharmacyDispenseResource

| Method | Path | Handler |
|---|---|---|
| POST | `/api/pharmacy-dispense/{prescriptionId}/dispense` | `dispense` |
| GET | `/api/pharmacy-dispense/{prescriptionId}/history` | `history` |

## PharmacyStockResource

| Method | Path | Handler |
|---|---|---|
| POST | `/api/pharmacy-stock/{drugId}/receive` | `receive` |
| POST | `/api/pharmacy-stock/{drugId}/write-off` | `writeOff` |

## PrescriptionLineResource

| Method | Path | Handler |
|---|---|---|
| GET | `/api/prescription-lines` | `getAllPrescriptionLines` |
| POST | `/api/prescription-lines` | `createPrescriptionLine` |
| DELETE | `/api/prescription-lines/{id}` | `deletePrescriptionLine` |
| GET | `/api/prescription-lines/{id}` | `getPrescriptionLine` |
| PATCH | `/api/prescription-lines/{id}` | `partialUpdatePrescriptionLine` |
| PUT | `/api/prescription-lines/{id}` | `updatePrescriptionLine` |

## PrescriptionResource

| Method | Path | Handler |
|---|---|---|
| GET | `/api/prescriptions` | `getAllPrescriptions` |
| POST | `/api/prescriptions` | `createPrescription` |
| DELETE | `/api/prescriptions/{id}` | `deletePrescription` |
| GET | `/api/prescriptions/{id}` | `getPrescription` |
| PATCH | `/api/prescriptions/{id}` | `partialUpdatePrescription` |
| PUT | `/api/prescriptions/{id}` | `updatePrescription` |

## PublicUserResource

| Method | Path | Handler |
|---|---|---|
| GET | `/api/users` | `getAllPublicUsers` |

## QueueResource

| Method | Path | Handler |
|---|---|---|
| GET | `/api/queues/active` | `activeVisits` |
| GET | `/api/queues/consultation` | `consultationQueue` |
| GET | `/api/queues/vitals` | `vitalsQueue` |

## RadiologyExamResource

| Method | Path | Handler |
|---|---|---|
| GET | `/api/radiology-exams` | `getAllRadiologyExams` |
| POST | `/api/radiology-exams` | `createRadiologyExam` |
| DELETE | `/api/radiology-exams/{id}` | `deleteRadiologyExam` |
| GET | `/api/radiology-exams/{id}` | `getRadiologyExam` |
| PATCH | `/api/radiology-exams/{id}` | `partialUpdateRadiologyExam` |
| PUT | `/api/radiology-exams/{id}` | `updateRadiologyExam` |

## RecordHistoryResource

| Method | Path | Handler |
|---|---|---|
| GET | `/api/record-history/{entityName}/{entityId}` | `trail` |

## ReferralResource

| Method | Path | Handler |
|---|---|---|
| GET | `/api/referrals` | `getAllReferrals` |
| POST | `/api/referrals` | `createReferral` |
| DELETE | `/api/referrals/{id}` | `deleteReferral` |
| GET | `/api/referrals/{id}` | `getReferral` |
| PATCH | `/api/referrals/{id}` | `partialUpdateReferral` |
| PUT | `/api/referrals/{id}` | `updateReferral` |

## ResultResource

| Method | Path | Handler |
|---|---|---|
| GET | `/api/results` | `getAllResults` |
| POST | `/api/results` | `createResult` |
| DELETE | `/api/results/{id}` | `deleteResult` |
| GET | `/api/results/{id}` | `getResult` |
| PATCH | `/api/results/{id}` | `partialUpdateResult` |
| PUT | `/api/results/{id}` | `updateResult` |

## StaffRecordResource

| Method | Path | Handler |
|---|---|---|
| GET | `/api/staff-records` | `getAllStaffRecords` |
| POST | `/api/staff-records` | `createStaffRecord` |
| GET | `/api/staff-records/{id}` | `getStaffRecord` |
| PATCH | `/api/staff-records/{id}` | `partialUpdateStaffRecord` |
| PUT | `/api/staff-records/{id}` | `updateStaffRecord` |

## TriageResource

| Method | Path | Handler |
|---|---|---|
| POST | `/api/visit-triage/{visitId}/start` | `startVitals` |
| POST | `/api/visit-triage/{visitId}/vitals` | `submitVitals` |

## UserResource

| Method | Path | Handler |
|---|---|---|
| GET | `/api/admin/users` | `getAllUsers` |
| POST | `/api/admin/users` | `createUser` |
| PUT | `/api/admin/users` | `updateUser` |
| DELETE | `/api/admin/users/{login}` | `deleteUser` |
| GET | `/api/admin/users/{login}` | `getUser` |
| PUT | `/api/admin/users/{login}` | `updateUser` |
| POST | `/api/admin/users/{login}/initial-password` | `setInitialPassword` |
| POST | `/api/admin/users/{login}/unlock` | `unlockUser` |

## VisitAdmissionResource

| Method | Path | Handler |
|---|---|---|
| POST | `/api/visit-admissions/{visitId}/admit` | `admit` |

## VisitConsultationResource

| Method | Path | Handler |
|---|---|---|
| GET | `/api/visit-consultation/{consultationId}/addenda` | `addenda` |
| POST | `/api/visit-consultation/{consultationId}/addenda` | `addAddendum` |
| POST | `/api/visit-consultation/{consultationId}/complete` | `complete` |
| PUT | `/api/visit-consultation/{consultationId}/notes` | `updateNotes` |
| POST | `/api/visit-consultation/{visitId}/start` | `start` |

## VisitIntakeResource

| Method | Path | Handler |
|---|---|---|
| POST | `/api/visit-intake/check-in/{appointmentId}` | `checkIn` |
| POST | `/api/visit-intake/open` | `open` |
| POST | `/api/visit-intake/pharmacy-only` | `openPharmacyOnly` |

## VisitOrderResource

| Method | Path | Handler |
|---|---|---|
| GET | `/api/visit-orders/billable/{visitId}` | `billable` |
| GET | `/api/visit-orders/visit/{visitId}` | `forVisit` |
| GET | `/api/visit-orders/worklist` | `worklist` |
| POST | `/api/visit-orders/{orderId}/cancel` | `cancel` |
| POST | `/api/visit-orders/{orderId}/result` | `enterResult` |
| POST | `/api/visit-orders/{visitId}/place` | `place` |

## VisitPaymentResource

| Method | Path | Handler |
|---|---|---|
| GET | `/api/visit-payments/bill/{visitId}` | `bill` |
| POST | `/api/visit-payments/{visitId}/pay` | `pay` |

## VisitPrescriptionResource

| Method | Path | Handler |
|---|---|---|
| GET | `/api/visit-prescriptions/billable/{visitId}` | `billable` |
| GET | `/api/visit-prescriptions/pharmacy-queue` | `pharmacyQueue` |
| GET | `/api/visit-prescriptions/visit/{visitId}` | `forVisit` |
| POST | `/api/visit-prescriptions/{prescriptionId}/cancel` | `cancel` |
| POST | `/api/visit-prescriptions/{visitId}/place` | `place` |

## VisitReferralResource

| Method | Path | Handler |
|---|---|---|
| GET | `/api/visit-referrals/visit/{visitId}` | `forVisit` |
| POST | `/api/visit-referrals/{referralId}/email` | `email` |
| GET | `/api/visit-referrals/{referralId}/letter` | `letter` |
| POST | `/api/visit-referrals/{visitId}/create` | `create` |

## VisitResource

| Method | Path | Handler |
|---|---|---|
| GET | `/api/visits` | `getAllVisits` |
| POST | `/api/visits` | `createVisit` |
| DELETE | `/api/visits/{id}` | `deleteVisit` |
| GET | `/api/visits/{id}` | `getVisit` |
| PATCH | `/api/visits/{id}` | `partialUpdateVisit` |
| PUT | `/api/visits/{id}` | `updateVisit` |

## VitalSignsResource

| Method | Path | Handler |
|---|---|---|
| GET | `/api/vital-signs` | `getAllVitalSignses` |
| POST | `/api/vital-signs` | `createVitalSigns` |
| DELETE | `/api/vital-signs/{id}` | `deleteVitalSigns` |
| GET | `/api/vital-signs/{id}` | `getVitalSigns` |
| PATCH | `/api/vital-signs/{id}` | `partialUpdateVitalSigns` |
| PUT | `/api/vital-signs/{id}` | `updateVitalSigns` |

## WardCoverManagementResource

| Method | Path | Handler |
|---|---|---|
| POST | `/api/ward-cover-roster` | `assign` |
| GET | `/api/ward-cover-roster/current` | `current` |
| PUT | `/api/ward-cover-roster/{coverId}/end` | `end` |

## WardCoverResource

| Method | Path | Handler |
|---|---|---|
| GET | `/api/ward-covers` | `getAllWardCovers` |
| POST | `/api/ward-covers` | `createWardCover` |
| DELETE | `/api/ward-covers/{id}` | `deleteWardCover` |
| GET | `/api/ward-covers/{id}` | `getWardCover` |
| PATCH | `/api/ward-covers/{id}` | `partialUpdateWardCover` |
| PUT | `/api/ward-covers/{id}` | `updateWardCover` |

## WardResource

| Method | Path | Handler |
|---|---|---|
| GET | `/api/wards` | `getAllWards` |
| POST | `/api/wards` | `createWard` |
| DELETE | `/api/wards/{id}` | `deleteWard` |
| GET | `/api/wards/{id}` | `getWard` |
| PATCH | `/api/wards/{id}` | `partialUpdateWard` |
| PUT | `/api/wards/{id}` | `updateWard` |

## WardTransferResource

| Method | Path | Handler |
|---|---|---|
| POST | `/api/admissions/{admissionId}/transfers` | `transfer` |

## WebConfigurerTestController

| Method | Path | Handler |
|---|---|---|
| GET | `/api/test-cors` | `testCorsOnApiPath` |

