import React, { useEffect, useState } from 'react';
import { Button, Table } from 'react-bootstrap';
import { getSortState } from 'react-jhipster';
import { Link, useLocation, useNavigate } from 'react-router';

import { faSort, faSortDown, faSortUp } from '@fortawesome/free-solid-svg-icons';
import { FontAwesomeIcon } from '@fortawesome/react-fontawesome';

import { useAppDispatch, useAppSelector } from 'app/config/store';
import { overrideSortStateWithQueryParams } from 'app/shared/util/entity-utils';
import { ASC, DESC } from 'app/shared/util/pagination.constants';

import { getEntities } from './prescription-line.reducer';

export const PrescriptionLine = () => {
  const dispatch = useAppDispatch();

  const pageLocation = useLocation();
  const navigate = useNavigate();

  const [sortState, setSortState] = useState(overrideSortStateWithQueryParams(getSortState(pageLocation, 'id'), pageLocation.search));

  const prescriptionLineList = useAppSelector(state => state.prescriptionLine.entities);
  const loading = useAppSelector(state => state.prescriptionLine.loading);

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
      <h2 id="prescription-line-heading" data-cy="PrescriptionLineHeading">
        Prescription Lines
        <div className="d-flex justify-content-end">
          <Button className="me-2" variant="info" onClick={handleSyncList} disabled={loading}>
            <FontAwesomeIcon icon="sync" spin={loading} /> Refresh list
          </Button>
          <Link to="/prescription-line/new" className="btn btn-primary jh-create-entity" id="jh-create-entity" data-cy="entityCreateButton">
            <FontAwesomeIcon icon="plus" />
            &nbsp; Create a new Prescription Line
          </Link>
        </div>
      </h2>
      <div className="table-responsive">
        {prescriptionLineList?.length > 0 ? (
          <Table responsive>
            <thead>
              <tr>
                <th className="hand" onClick={sort('id')}>
                  ID <FontAwesomeIcon icon={getSortIconByFieldName('id')} />
                </th>
                <th className="hand" onClick={sort('dosage')}>
                  Dosage <FontAwesomeIcon icon={getSortIconByFieldName('dosage')} />
                </th>
                <th className="hand" onClick={sort('duration')}>
                  Duration <FontAwesomeIcon icon={getSortIconByFieldName('duration')} />
                </th>
                <th className="hand" onClick={sort('quantity')}>
                  Quantity <FontAwesomeIcon icon={getSortIconByFieldName('quantity')} />
                </th>
                <th>
                  Prescription <FontAwesomeIcon icon="sort" />
                </th>
                <th>
                  Drug <FontAwesomeIcon icon="sort" />
                </th>
                <th />
              </tr>
            </thead>
            <tbody>
              {prescriptionLineList.map(prescriptionLine => (
                <tr key={`entity-${prescriptionLine.id}`} data-cy="entityTable">
                  <td>
                    <Button as={Link as any} to={`/prescription-line/${prescriptionLine.id}`} variant="link" size="sm">
                      {prescriptionLine.id}
                    </Button>
                  </td>
                  <td>{prescriptionLine.dosage}</td>
                  <td>{prescriptionLine.duration}</td>
                  <td>{prescriptionLine.quantity}</td>
                  <td>
                    {prescriptionLine.prescription ? (
                      <Link to={`/prescription/${prescriptionLine.prescription.id}`}>{prescriptionLine.prescription.id}</Link>
                    ) : (
                      ''
                    )}
                  </td>
                  <td>{prescriptionLine.drug ? <Link to={`/drug/${prescriptionLine.drug.id}`}>{prescriptionLine.drug.id}</Link> : ''}</td>
                  <td className="text-end">
                    <div className="btn-group flex-btn-group-container">
                      <Button
                        as={Link as any}
                        to={`/prescription-line/${prescriptionLine.id}`}
                        variant="info"
                        size="sm"
                        data-cy="entityDetailsButton"
                      >
                        <FontAwesomeIcon icon="eye" /> <span className="d-none d-md-inline">View</span>
                      </Button>
                      <Button
                        as={Link as any}
                        to={`/prescription-line/${prescriptionLine.id}/edit`}
                        variant="primary"
                        size="sm"
                        data-cy="entityEditButton"
                      >
                        <FontAwesomeIcon icon="pencil-alt" /> <span className="d-none d-md-inline">Edit</span>
                      </Button>
                      <Button
                        onClick={() => (globalThis.location.href = `/prescription-line/${prescriptionLine.id}/delete`)}
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
          !loading && <div className="alert alert-warning">No Prescription Lines found</div>
        )}
      </div>
    </div>
  );
};

export default PrescriptionLine;
