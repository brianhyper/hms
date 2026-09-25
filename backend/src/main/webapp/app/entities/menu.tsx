import React from 'react';
// eslint-disable-line

import MenuItem from 'app/shared/layout/menus/menu-item'; // eslint-disable-line

const EntitiesMenu = () => {
  return (
    <>
      {/* prettier-ignore */}
      <MenuItem icon="asterisk" to="/patient">
        Patient
      </MenuItem>
      <MenuItem icon="asterisk" to="/department">
        Department
      </MenuItem>
      <MenuItem icon="asterisk" to="/appointment">
        Appointment
      </MenuItem>
      <MenuItem icon="asterisk" to="/visit">
        Visit
      </MenuItem>
      <MenuItem icon="asterisk" to="/vital-signs">
        Vital Signs
      </MenuItem>
      <MenuItem icon="asterisk" to="/consultation">
        Consultation
      </MenuItem>
      <MenuItem icon="asterisk" to="/diagnosis">
        Diagnosis
      </MenuItem>
      <MenuItem icon="asterisk" to="/hospital-service">
        Hospital Service
      </MenuItem>
      <MenuItem icon="asterisk" to="/lab-test">
        Lab Test
      </MenuItem>
      <MenuItem icon="asterisk" to="/radiology-exam">
        Radiology Exam
      </MenuItem>
      <MenuItem icon="asterisk" to="/diagnostic-order">
        Diagnostic Order
      </MenuItem>
      <MenuItem icon="asterisk" to="/result">
        Result
      </MenuItem>
      <MenuItem icon="asterisk" to="/referral">
        Referral
      </MenuItem>
      <MenuItem icon="asterisk" to="/prescription">
        Prescription
      </MenuItem>
      <MenuItem icon="asterisk" to="/prescription-line">
        Prescription Line
      </MenuItem>
      <MenuItem icon="asterisk" to="/drug">
        Drug
      </MenuItem>
      <MenuItem icon="asterisk" to="/dispense">
        Dispense
      </MenuItem>
      <MenuItem icon="asterisk" to="/dispense-line">
        Dispense Line
      </MenuItem>
      <MenuItem icon="asterisk" to="/bill">
        Bill
      </MenuItem>
      <MenuItem icon="asterisk" to="/bill-line-item">
        Bill Line Item
      </MenuItem>
      <MenuItem icon="asterisk" to="/payment">
        Payment
      </MenuItem>
      <MenuItem icon="asterisk" to="/audit-log">
        Audit Log
      </MenuItem>
      {/* jhipster-needle-add-entity-to-menu - JHipster will add entities to the menu here */}
    </>
  );
};

export default EntitiesMenu;
