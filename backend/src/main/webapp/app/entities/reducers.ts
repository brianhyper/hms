import appointment from 'app/entities/appointment/appointment.reducer';
import auditLog from 'app/entities/audit-log/audit-log.reducer';
import bill from 'app/entities/bill/bill.reducer';
import billLineItem from 'app/entities/bill-line-item/bill-line-item.reducer';
import consultation from 'app/entities/consultation/consultation.reducer';
import department from 'app/entities/department/department.reducer';
import diagnosis from 'app/entities/diagnosis/diagnosis.reducer';
import diagnosticOrder from 'app/entities/diagnostic-order/diagnostic-order.reducer';
import dispense from 'app/entities/dispense/dispense.reducer';
import dispenseLine from 'app/entities/dispense-line/dispense-line.reducer';
import drug from 'app/entities/drug/drug.reducer';
import hospitalService from 'app/entities/hospital-service/hospital-service.reducer';
import labTest from 'app/entities/lab-test/lab-test.reducer';
import patient from 'app/entities/patient/patient.reducer';
import payment from 'app/entities/payment/payment.reducer';
import prescription from 'app/entities/prescription/prescription.reducer';
import prescriptionLine from 'app/entities/prescription-line/prescription-line.reducer';
import radiologyExam from 'app/entities/radiology-exam/radiology-exam.reducer';
import referral from 'app/entities/referral/referral.reducer';
import result from 'app/entities/result/result.reducer';
import visit from 'app/entities/visit/visit.reducer';
import vitalSigns from 'app/entities/vital-signs/vital-signs.reducer';
/* jhipster-needle-add-reducer-import - JHipster will add reducer here */

const entitiesReducers = {
  patient,
  department,
  appointment,
  visit,
  vitalSigns,
  consultation,
  diagnosis,
  hospitalService,
  labTest,
  radiologyExam,
  diagnosticOrder,
  result,
  referral,
  prescription,
  prescriptionLine,
  drug,
  dispense,
  dispenseLine,
  bill,
  billLineItem,
  payment,
  auditLog,
  // jhipster-needle-add-reducer-combine - JHipster will add reducer here
};

export default entitiesReducers;
