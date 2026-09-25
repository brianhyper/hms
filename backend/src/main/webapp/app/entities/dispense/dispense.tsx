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

import { getEntities } from './dispense.reducer';

export const Dispense = () => {
  const dispatch = useAppDispatch();

  const pageLocation = useLocation();
  const navigate = useNavigate();

  const [sortState, setSortState] = useState(overrideSortStateWithQueryParams(getSortState(pageLocation, 'id'), pageLocation.search));

  const dispenseList = useAppSelector(state => state.dispense.entities);
  const loading = useAppSelector(state => state.dispense.loading);

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
      <h2 id="dispense-heading" data-cy="DispenseHeading">
        Dispenses
        <div className="d-flex justify-content-end">
          <Button className="me-2" variant="info" onClick={handleSyncList} disabled={loading}>
            <FontAwesomeIcon icon="sync" spin={loading} /> Refresh list
          </Button>
          <Link to="/dispense/new" className="btn btn-primary jh-create-entity" id="jh-create-entity" data-cy="entityCreateButton">
            <FontAwesomeIcon icon="plus" />
            &nbsp; Create a new Dispense
          </Link>
        </div>
      </h2>
      <div className="table-responsive">
        {dispenseList?.length > 0 ? (
          <Table responsive>
            <thead>
              <tr>
                <th className="hand" onClick={sort('id')}>
                  ID <FontAwesomeIcon icon={getSortIconByFieldName('id')} />
                </th>
                <th className="hand" onClick={sort('dispensedAt')}>
                  Dispensed At <FontAwesomeIcon icon={getSortIconByFieldName('dispensedAt')} />
                </th>
                <th className="hand" onClick={sort('note')}>
                  Note <FontAwesomeIcon icon={getSortIconByFieldName('note')} />
                </th>
                <th>
                  Prescription <FontAwesomeIcon icon="sort" />
                </th>
                <th>
                  Recorded By <FontAwesomeIcon icon="sort" />
                </th>
                <th />
              </tr>
            </thead>
            <tbody>
              {dispenseList.map(dispense => (
                <tr key={`entity-${dispense.id}`} data-cy="entityTable">
                  <td>
                    <Button as={Link as any} to={`/dispense/${dispense.id}`} variant="link" size="sm">
                      {dispense.id}
                    </Button>
                  </td>
                  <td>{dispense.dispensedAt ? <TextFormat type="date" value={dispense.dispensedAt} format={APP_DATE_FORMAT} /> : null}</td>
                  <td>{dispense.note}</td>
                  <td>
                    {dispense.prescription ? <Link to={`/prescription/${dispense.prescription.id}`}>{dispense.prescription.id}</Link> : ''}
                  </td>
                  <td>{dispense.recordedBy ? dispense.recordedBy.login : ''}</td>
                  <td className="text-end">
                    <div className="btn-group flex-btn-group-container">
                      <Button as={Link as any} to={`/dispense/${dispense.id}`} variant="info" size="sm" data-cy="entityDetailsButton">
                        <FontAwesomeIcon icon="eye" /> <span className="d-none d-md-inline">View</span>
                      </Button>
                      <Button as={Link as any} to={`/dispense/${dispense.id}/edit`} variant="primary" size="sm" data-cy="entityEditButton">
                        <FontAwesomeIcon icon="pencil-alt" /> <span className="d-none d-md-inline">Edit</span>
                      </Button>
                      <Button
                        onClick={() => (globalThis.location.href = `/dispense/${dispense.id}/delete`)}
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
          !loading && <div className="alert alert-warning">No Dispenses found</div>
        )}
      </div>
    </div>
  );
};

export default Dispense;
