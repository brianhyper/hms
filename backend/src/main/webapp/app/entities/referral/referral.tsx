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

import { getEntities } from './referral.reducer';

export const Referral = () => {
  const dispatch = useAppDispatch();

  const pageLocation = useLocation();
  const navigate = useNavigate();

  const [sortState, setSortState] = useState(overrideSortStateWithQueryParams(getSortState(pageLocation, 'id'), pageLocation.search));

  const referralList = useAppSelector(state => state.referral.entities);
  const loading = useAppSelector(state => state.referral.loading);

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
      <h2 id="referral-heading" data-cy="ReferralHeading">
        Referrals
        <div className="d-flex justify-content-end">
          <Button className="me-2" variant="info" onClick={handleSyncList} disabled={loading}>
            <FontAwesomeIcon icon="sync" spin={loading} /> Refresh list
          </Button>
          <Link to="/referral/new" className="btn btn-primary jh-create-entity" id="jh-create-entity" data-cy="entityCreateButton">
            <FontAwesomeIcon icon="plus" />
            &nbsp; Create a new Referral
          </Link>
        </div>
      </h2>
      <div className="table-responsive">
        {referralList?.length > 0 ? (
          <Table responsive>
            <thead>
              <tr>
                <th className="hand" onClick={sort('id')}>
                  ID <FontAwesomeIcon icon={getSortIconByFieldName('id')} />
                </th>
                <th className="hand" onClick={sort('type')}>
                  Type <FontAwesomeIcon icon={getSortIconByFieldName('type')} />
                </th>
                <th className="hand" onClick={sort('destination')}>
                  Destination <FontAwesomeIcon icon={getSortIconByFieldName('destination')} />
                </th>
                <th className="hand" onClick={sort('destinationEmail')}>
                  Destination Email <FontAwesomeIcon icon={getSortIconByFieldName('destinationEmail')} />
                </th>
                <th className="hand" onClick={sort('reason')}>
                  Reason <FontAwesomeIcon icon={getSortIconByFieldName('reason')} />
                </th>
                <th className="hand" onClick={sort('notes')}>
                  Notes <FontAwesomeIcon icon={getSortIconByFieldName('notes')} />
                </th>
                <th className="hand" onClick={sort('status')}>
                  Status <FontAwesomeIcon icon={getSortIconByFieldName('status')} />
                </th>
                <th className="hand" onClick={sort('createdAt')}>
                  Created At <FontAwesomeIcon icon={getSortIconByFieldName('createdAt')} />
                </th>
                <th>
                  Visit <FontAwesomeIcon icon="sort" />
                </th>
                <th>
                  Referred By <FontAwesomeIcon icon="sort" />
                </th>
                <th>
                  Department <FontAwesomeIcon icon="sort" />
                </th>
                <th />
              </tr>
            </thead>
            <tbody>
              {referralList.map(referral => (
                <tr key={`entity-${referral.id}`} data-cy="entityTable">
                  <td>
                    <Button as={Link as any} to={`/referral/${referral.id}`} variant="link" size="sm">
                      {referral.id}
                    </Button>
                  </td>
                  <td>{referral.type}</td>
                  <td>{referral.destination}</td>
                  <td>{referral.destinationEmail}</td>
                  <td>{referral.reason}</td>
                  <td>{referral.notes}</td>
                  <td>{referral.status}</td>
                  <td>{referral.createdAt ? <TextFormat type="date" value={referral.createdAt} format={APP_DATE_FORMAT} /> : null}</td>
                  <td>{referral.visit ? <Link to={`/visit/${referral.visit.id}`}>{referral.visit.id}</Link> : ''}</td>
                  <td>{referral.referredBy ? referral.referredBy.login : ''}</td>
                  <td>{referral.department ? <Link to={`/department/${referral.department.id}`}>{referral.department.id}</Link> : ''}</td>
                  <td className="text-end">
                    <div className="btn-group flex-btn-group-container">
                      <Button as={Link as any} to={`/referral/${referral.id}`} variant="info" size="sm" data-cy="entityDetailsButton">
                        <FontAwesomeIcon icon="eye" /> <span className="d-none d-md-inline">View</span>
                      </Button>
                      <Button as={Link as any} to={`/referral/${referral.id}/edit`} variant="primary" size="sm" data-cy="entityEditButton">
                        <FontAwesomeIcon icon="pencil-alt" /> <span className="d-none d-md-inline">Edit</span>
                      </Button>
                      <Button
                        onClick={() => (globalThis.location.href = `/referral/${referral.id}/delete`)}
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
          !loading && <div className="alert alert-warning">No Referrals found</div>
        )}
      </div>
    </div>
  );
};

export default Referral;
