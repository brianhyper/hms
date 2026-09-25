import React, { useEffect, useState } from 'react';
import { Button, Table } from 'react-bootstrap';
import { TextFormat, getSortState } from 'react-jhipster';
import { Link, useLocation, useNavigate } from 'react-router';

import { faSort, faSortDown, faSortUp } from '@fortawesome/free-solid-svg-icons';
import { FontAwesomeIcon } from '@fortawesome/react-fontawesome';

import { APP_DATE_FORMAT } from 'app/config/constants';
import { useAppDispatch, useAppSelector } from 'app/config/store';
import { overrideSortStateWithQueryParams } from 'app/shared/util/entity-utils';
import { ASC, DESC } from 'app/shared/util/pagination.constants';

import { getEntities } from './consultation.reducer';

export const Consultation = () => {
  const dispatch = useAppDispatch();

  const pageLocation = useLocation();
  const navigate = useNavigate();

  const [sortState, setSortState] = useState(overrideSortStateWithQueryParams(getSortState(pageLocation, 'id'), pageLocation.search));

  const consultationList = useAppSelector(state => state.consultation.entities);
  const loading = useAppSelector(state => state.consultation.loading);

  const getAllEntities = () => {
    dispatch(
      getEntities({
        sort: `${sortState.sort},${sortState.order}`,
      }),
    );
  };

  const sortEntities = () => {
    getAllEntities();
    const endURL = `?sort=${sortState.sort},${sortState.order}`;
    if (pageLocation.search !== endURL) {
      navigate(`${pageLocation.pathname}${endURL}`);
    }
  };

  useEffect(() => {
    sortEntities();
  }, [sortState.order, sortState.sort]);

  const sort = p => () => {
    setSortState({
      ...sortState,
      order: sortState.order === ASC ? DESC : ASC,
      sort: p,
    });
  };

  const handleSyncList = () => {
    sortEntities();
  };

  const getSortIconByFieldName = (fieldName: string) => {
    const sortFieldName = sortState.sort;
    const { order } = sortState;
    if (sortFieldName !== fieldName) {
      return faSort;
    }
    return order === ASC ? faSortUp : faSortDown;
  };

  return (
    <div>
      <h2 id="consultation-heading" data-cy="ConsultationHeading">
        Consultations
        <div className="d-flex justify-content-end">
          <Button className="me-2" variant="info" onClick={handleSyncList} disabled={loading}>
            <FontAwesomeIcon icon="sync" spin={loading} /> Refresh list
          </Button>
          <Link to="/consultation/new" className="btn btn-primary jh-create-entity" id="jh-create-entity" data-cy="entityCreateButton">
            <FontAwesomeIcon icon="plus" />
            &nbsp; Create a new Consultation
          </Link>
        </div>
      </h2>
      <div className="table-responsive">
        {consultationList?.length > 0 ? (
          <Table responsive>
            <thead>
              <tr>
                <th className="hand" onClick={sort('id')}>
                  ID <FontAwesomeIcon icon={getSortIconByFieldName('id')} />
                </th>
                <th className="hand" onClick={sort('presentingComplaint')}>
                  Presenting Complaint <FontAwesomeIcon icon={getSortIconByFieldName('presentingComplaint')} />
                </th>
                <th className="hand" onClick={sort('examinationFindings')}>
                  Examination Findings <FontAwesomeIcon icon={getSortIconByFieldName('examinationFindings')} />
                </th>
                <th className="hand" onClick={sort('diagnosisOther')}>
                  Diagnosis Other <FontAwesomeIcon icon={getSortIconByFieldName('diagnosisOther')} />
                </th>
                <th className="hand" onClick={sort('observations')}>
                  Observations <FontAwesomeIcon icon={getSortIconByFieldName('observations')} />
                </th>
                <th className="hand" onClick={sort('followUpInstructions')}>
                  Follow Up Instructions <FontAwesomeIcon icon={getSortIconByFieldName('followUpInstructions')} />
                </th>
                <th className="hand" onClick={sort('status')}>
                  Status <FontAwesomeIcon icon={getSortIconByFieldName('status')} />
                </th>
                <th className="hand" onClick={sort('startedAt')}>
                  Started At <FontAwesomeIcon icon={getSortIconByFieldName('startedAt')} />
                </th>
                <th className="hand" onClick={sort('completedAt')}>
                  Completed At <FontAwesomeIcon icon={getSortIconByFieldName('completedAt')} />
                </th>
                <th>
                  Doctor <FontAwesomeIcon icon="sort" />
                </th>
                <th>
                  Diagnoses <FontAwesomeIcon icon="sort" />
                </th>
                <th />
              </tr>
            </thead>
            <tbody>
              {consultationList.map(consultation => (
                <tr key={`entity-${consultation.id}`} data-cy="entityTable">
                  <td>
                    <Button as={Link as any} to={`/consultation/${consultation.id}`} variant="link" size="sm">
                      {consultation.id}
                    </Button>
                  </td>
                  <td>{consultation.presentingComplaint}</td>
                  <td>{consultation.examinationFindings}</td>
                  <td>{consultation.diagnosisOther}</td>
                  <td>{consultation.observations}</td>
                  <td>{consultation.followUpInstructions}</td>
                  <td>{consultation.status}</td>
                  <td>
                    {consultation.startedAt ? <TextFormat type="date" value={consultation.startedAt} format={APP_DATE_FORMAT} /> : null}
                  </td>
                  <td>
                    {consultation.completedAt ? <TextFormat type="date" value={consultation.completedAt} format={APP_DATE_FORMAT} /> : null}
                  </td>
                  <td>{consultation.doctor ? consultation.doctor.login : ''}</td>
                  <td>
                    {consultation.diagnoseses
                      ? consultation.diagnoseses.map((val, j) => (
                          <span key={j}>
                            <Link to={`/diagnosis/${val.id}`}>{val.id}</Link>
                            {j === consultation.diagnoseses.length - 1 ? '' : ', '}
                          </span>
                        ))
                      : null}
                  </td>
                  <td className="text-end">
                    <div className="btn-group flex-btn-group-container">
                      <Button
                        as={Link as any}
                        to={`/consultation/${consultation.id}`}
                        variant="info"
                        size="sm"
                        data-cy="entityDetailsButton"
                      >
                        <FontAwesomeIcon icon="eye" /> <span className="d-none d-md-inline">View</span>
                      </Button>
                      <Button
                        as={Link as any}
                        to={`/consultation/${consultation.id}/edit`}
                        variant="primary"
                        size="sm"
                        data-cy="entityEditButton"
                      >
                        <FontAwesomeIcon icon="pencil-alt" /> <span className="d-none d-md-inline">Edit</span>
                      </Button>
                      <Button
                        onClick={() => (globalThis.location.href = `/consultation/${consultation.id}/delete`)}
                        variant="danger"
                        size="sm"
                        data-cy="entityDeleteButton"
                      >
                        <FontAwesomeIcon icon="trash" /> <span className="d-none d-md-inline">Delete</span>
                      </Button>
                    </div>
                  </td>
                </tr>
              ))}
            </tbody>
          </Table>
        ) : (
          !loading && <div className="alert alert-warning">No Consultations found</div>
        )}
      </div>
    </div>
  );
};

export default Consultation;
