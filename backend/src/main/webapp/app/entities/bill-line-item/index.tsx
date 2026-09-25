import React from 'react';
import { Route } from 'react-router';

import ErrorBoundaryRoutes from 'app/shared/error/error-boundary-routes';

import BillLineItem from './bill-line-item';
import BillLineItemDeleteDialog from './bill-line-item-delete-dialog';
import BillLineItemDetail from './bill-line-item-detail';
import BillLineItemUpdate from './bill-line-item-update';

const BillLineItemRoutes = () => (
  <ErrorBoundaryRoutes>
    <Route index element={<BillLineItem />} />
    <Route path="new" element={<BillLineItemUpdate />} />
    <Route path=":id">
      <Route index element={<BillLineItemDetail />} />
      <Route path="edit" element={<BillLineItemUpdate />} />
      <Route path="delete" element={<BillLineItemDeleteDialog />} />
    </Route>
  </ErrorBoundaryRoutes>
);

export default BillLineItemRoutes;
