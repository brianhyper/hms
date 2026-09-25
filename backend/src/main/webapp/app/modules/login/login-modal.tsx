import React from 'react';
import { Alert, Button, Col, Form, Modal, ModalBody, ModalFooter, ModalHeader, Row } from 'react-bootstrap';
import { ValidatedField } from 'react-jhipster';
import { Link } from 'react-router';

import { type FieldError, type FieldValues, useForm } from 'react-hook-form';

export interface ILoginModalProps {
  showModal: boolean;
  loginError: boolean;
  handleLogin: (username: string, password: string, rememberMe: boolean) => void;
  handleClose: () => void;
}

const LoginModal = (props: ILoginModalProps) => {
  const login = ({ username, password, rememberMe }: FieldValues) => {
    props.handleLogin(username, password, rememberMe);
  };

  const {
    handleSubmit,
    register,
    formState: { errors, touchedFields },
  } = useForm({ mode: 'onTouched' });

  const { loginError, handleClose } = props;

  const handleLoginSubmit = e => {
    handleSubmit(login)(e);
  };

  return (
    <Modal show={props.showModal} onHide={handleClose} backdrop="static" id="login-page" autoFocus={false}>
      <Form onSubmit={handleLoginSubmit}>
        <ModalHeader id="login-title" data-cy="loginTitle" closeButton>
          Sign in
        </ModalHeader>
        <ModalBody>
          <Row>
            <Col md="12">
              {loginError && (
                <Alert variant="danger" data-cy="loginError">
                  <strong>Failed to sign in!</strong> Please check your credentials and try again.
                </Alert>
              )}
            </Col>
            <Col md="12">
              <ValidatedField
                name="username"
                label="Username"
                placeholder="Your username"
                required
                autoFocus
                data-cy="username"
                validate={{ required: 'Username cannot be empty!' }}
                register={register}
                error={errors.username as FieldError}
                isTouched={touchedFields.username}
              />
              <ValidatedField
                name="password"
                type="password"
                label="Password"
                placeholder="Your password"
                required
                data-cy="password"
                validate={{ required: 'Password cannot be empty!' }}
                register={register}
                error={errors.password as FieldError}
                isTouched={touchedFields.password}
              />
              <ValidatedField name="rememberMe" type="checkbox" check label="Remember me" value={true} register={register} />
            </Col>
          </Row>
          <div className="mt-1">&nbsp;</div>
          <Alert variant="warning">
            <Link to="/account/reset/request" data-cy="forgetYourPasswordSelector">
              Did you forget your password?
            </Link>
          </Alert>
          <Alert variant="warning">
            <span>You don&apos;t have an account yet?</span> <Link to="/account/register">Register a new account</Link>
          </Alert>
        </ModalBody>
        <ModalFooter>
          <Button variant="secondary" onClick={handleClose} tabIndex={1}>
            Cancel
          </Button>{' '}
          <Button variant="primary" type="submit" data-cy="submit">
            Sign in
          </Button>
        </ModalFooter>
      </Form>
    </Modal>
  );
};

export default LoginModal;
