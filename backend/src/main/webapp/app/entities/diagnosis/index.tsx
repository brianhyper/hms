import React from 'react';
import { Route } from 'react-router';

import ErrorBoundaryRoutes from 'app/shared/error/error-boundary-routes';

import Diagnosis from './diagnosis';
import DiagnosisDeleteDialog from './diagnosis-delete-dialog';
import DiagnosisDetail from './diagnosis-detail';
import DiagnosisUpdate from './diagnosis-update';

const DiagnosisRoutes = () => (
  <ErrorBoundaryRoutes>
    <Route index element={<Diagnosis />} />
    <Route path="new" element={<DiagnosisUpdate />} />
    <Route path=":id">
      <Route index element={<DiagnosisDetail />} />
      <Route path="edit" element={<DiagnosisUpdate />} />
      <Route path="delete" element={<DiagnosisDeleteDialog />} />
    </Route>
  </ErrorBoundaryRoutes>
);

export default DiagnosisRoutes;
