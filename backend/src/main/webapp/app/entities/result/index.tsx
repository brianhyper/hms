import React from 'react';
import { Route } from 'react-router';

import ErrorBoundaryRoutes from 'app/shared/error/error-boundary-routes';

import Result from './result';
import ResultDeleteDialog from './result-delete-dialog';
import ResultDetail from './result-detail';
import ResultUpdate from './result-update';

const ResultRoutes = () => (
  <ErrorBoundaryRoutes>
    <Route index element={<Result />} />
    <Route path="new" element={<ResultUpdate />} />
    <Route path=":id">
      <Route index element={<ResultDetail />} />
      <Route path="edit" element={<ResultUpdate />} />
      <Route path="delete" element={<ResultDeleteDialog />} />
    </Route>
  </ErrorBoundaryRoutes>
);

export default ResultRoutes;
