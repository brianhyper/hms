import React, { useEffect, useState } from 'react';
import { Button, Table } from 'react-bootstrap';
import { getSortState } from 'react-jhipster';
import { Link, useLocation, useNavigate } from 'react-router';

import { faSort, faSortDown, faSortUp } from '@fortawesome/free-solid-svg-icons';
import { FontAwesomeIcon } from '@fortawesome/react-fontawesome';

import { useAppDispatch, useAppSelector } from 'app/config/store';
import { overrideSortStateWithQueryParams } from 'app/shared/util/entity-utils';
import { ASC, DESC } from 'app/shared/util/pagination.constants';

import { getEntities } from './dispense-line.reducer';

export const DispenseLine = () => {
  const dispatch = useAppDispatch();

  const pageLocation = useLocation();
  const navigate = useNavigate();

  const [sortState, setSortState] = useState(overrideSortStateWithQueryParams(getSortState(pageLocation, 'id'), pageLocation.search));

  const dispenseLineList = useAppSelector(state => state.dispenseLine.entities);
  const loading = useAppSelector(state => state.dispenseLine.loading);

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
      <h2 id="dispense-line-heading" data-cy="DispenseLineHeading">
        Dispense Lines
        <div className="d-flex justify-content-end">
          <Button className="me-2" variant="info" onClick={handleSyncList} disabled={loading}>
            <FontAwesomeIcon icon="sync" spin={loading} /> Refresh list
          </Button>
          <Link to="/dispense-line/new" className="btn btn-primary jh-create-entity" id="jh-create-entity" data-cy="entityCreateButton">
            <FontAwesomeIcon icon="plus" />
            &nbsp; Create a new Dispense Line
          </Link>
        </div>
      </h2>
      <div className="table-responsive">
        {dispenseLineList?.length > 0 ? (
          <Table responsive>
            <thead>
              <tr>
                <th className="hand" onClick={sort('id')}>
                  ID <FontAwesomeIcon icon={getSortIconByFieldName('id')} />
                </th>
                <th className="hand" onClick={sort('quantity')}>
                  Quantity <FontAwesomeIcon icon={getSortIconByFieldName('quantity')} />
                </th>
                <th className="hand" onClick={sort('substitutionReason')}>
                  Substitution Reason <FontAwesomeIcon icon={getSortIconByFieldName('substitutionReason')} />
                </th>
                <th>
                  Dispense <FontAwesomeIcon icon="sort" />
                </th>
                <th>
                  Prescription Line <FontAwesomeIcon icon="sort" />
                </th>
                <th>
                  Drug <FontAwesomeIcon icon="sort" />
                </th>
                <th />
              </tr>
            </thead>
            <tbody>
              {dispenseLineList.map(dispenseLine => (
                <tr key={`entity-${dispenseLine.id}`} data-cy="entityTable">
                  <td>
                    <Button as={Link as any} to={`/dispense-line/${dispenseLine.id}`} variant="link" size="sm">
                      {dispenseLine.id}
                    </Button>
                  </td>
                  <td>{dispenseLine.quantity}</td>
                  <td>{dispenseLine.substitutionReason}</td>
                  <td>
                    {dispenseLine.dispense ? <Link to={`/dispense/${dispenseLine.dispense.id}`}>{dispenseLine.dispense.id}</Link> : ''}
                  </td>
                  <td>
                    {dispenseLine.prescriptionLine ? (
                      <Link to={`/prescription-line/${dispenseLine.prescriptionLine.id}`}>{dispenseLine.prescriptionLine.id}</Link>
                    ) : (
                      ''
                    )}
                  </td>
                  <td>{dispenseLine.drug ? <Link to={`/drug/${dispenseLine.drug.id}`}>{dispenseLine.drug.id}</Link> : ''}</td>
                  <td className="text-end">
                    <div className="btn-group flex-btn-group-container">
                      <Button
                        as={Link as any}
                        to={`/dispense-line/${dispenseLine.id}`}
                        variant="info"
                        size="sm"
                        data-cy="entityDetailsButton"
                      >
                        <FontAwesomeIcon icon="eye" /> <span className="d-none d-md-inline">View</span>
                      </Button>
                      <Button
                        as={Link as any}
                        to={`/dispense-line/${dispenseLine.id}/edit`}
                        variant="primary"
                        size="sm"
                        data-cy="entityEditButton"
                      >
                        <FontAwesomeIcon icon="pencil-alt" /> <span className="d-none d-md-inline">Edit</span>
                      </Button>
                      <Button
                        onClick={() => (globalThis.location.href = `/dispense-line/${dispenseLine.id}/delete`)}
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
          !loading && <div className="alert alert-warning">No Dispense Lines found</div>
        )}
      </div>
    </div>
  );
};

export default DispenseLine;
