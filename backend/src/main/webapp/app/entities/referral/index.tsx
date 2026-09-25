import React from 'react';
import { Route } from 'react-router';

import ErrorBoundaryRoutes from 'app/shared/error/error-boundary-routes';

import Referral from './referral';
import ReferralDeleteDialog from './referral-delete-dialog';
import ReferralDetail from './referral-detail';
import ReferralUpdate from './referral-update';

const ReferralRoutes = () => (
  <ErrorBoundaryRoutes>
    <Route index element={<Referral />} />
    <Route path="new" element={<ReferralUpdate />} />
    <Route path=":id">
      <Route index element={<ReferralDetail />} />
      <Route path="edit" element={<ReferralUpdate />} />
      <Route path="delete" element={<ReferralDeleteDialog />} />
    </Route>
  </ErrorBoundaryRoutes>
);

export default ReferralRoutes;
