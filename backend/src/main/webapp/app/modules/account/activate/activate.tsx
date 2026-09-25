import React, { useEffect } from 'react';
import { Alert, Col, Row } from 'react-bootstrap';
import { Link, useSearchParams } from 'react-router';

import { useAppDispatch, useAppSelector } from 'app/config/store';

import { activateAction, reset } from './activate.reducer';

const successAlert = (
  <Alert variant="success">
    <strong>Your user account has been activated.</strong> Please
    <Link to="/login" className="alert-link">
      sign in
    </Link>
    .
  </Alert>
);

const failureAlert = (
  <Alert variant="danger">
    <strong>Your user could not be activated.</strong> Please use the registration form to sign up.
  </Alert>
);

export const ActivatePage = () => {
  const dispatch = useAppDispatch();

  const [searchParams] = useSearchParams();

  useEffect(() => {
    const key = searchParams.get('key');

    dispatch(activateAction(key!));
    return () => {
      dispatch(reset());
    };
  }, []);

  const { activationSuccess, activationFailure } = useAppSelector(state => state.activate);

  return (
    <div>
      <Row className="justify-content-center">
        <Col md="8">
          <h1>Activation</h1>
          {activationSuccess ? successAlert : undefined}
          {activationFailure ? failureAlert : undefined}
        </Col>
      </Row>
    </div>
  );
};

export default ActivatePage;
