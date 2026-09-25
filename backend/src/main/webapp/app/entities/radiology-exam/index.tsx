import React from 'react';
import { Route } from 'react-router';

import ErrorBoundaryRoutes from 'app/shared/error/error-boundary-routes';

import RadiologyExam from './radiology-exam';
import RadiologyExamDeleteDialog from './radiology-exam-delete-dialog';
import RadiologyExamDetail from './radiology-exam-detail';
import RadiologyExamUpdate from './radiology-exam-update';

const RadiologyExamRoutes = () => (
  <ErrorBoundaryRoutes>
    <Route index element={<RadiologyExam />} />
    <Route path="new" element={<RadiologyExamUpdate />} />
    <Route path=":id">
      <Route index element={<RadiologyExamDetail />} />
      <Route path="edit" element={<RadiologyExamUpdate />} />
      <Route path="delete" element={<RadiologyExamDeleteDialog />} />
    </Route>
  </ErrorBoundaryRoutes>
);

export default RadiologyExamRoutes;
