import React, { useEffect, useState } from 'react';
import { Button, Table } from 'react-bootstrap';
import { JhiItemCount, JhiPagination, TextFormat, getPaginationState } from 'react-jhipster';
import { Link, useLocation, useNavigate } from 'react-router';

import { faSort, faSortDown, faSortUp } from '@fortawesome/free-solid-svg-icons';
import { FontAwesomeIcon } from '@fortawesome/react-fontawesome';

import { APP_DATE_FORMAT } from 'app/config/constants';
import { useAppDispatch, useAppSelector } from 'app/config/store';
import { overridePaginationStateWithQueryParams } from 'app/shared/util/entity-utils';
import { ASC, DESC, ITEMS_PER_PAGE, SORT } from 'app/shared/util/pagination.constants';

import { getEntities } from './audit-log.reducer';

export const AuditLog = () => {
  const dispatch = useAppDispatch();

  const pageLocation = useLocation();
  const navigate = useNavigate();

  const [paginationState, setPaginationState] = useState(
    overridePaginationStateWithQueryParams(getPaginationState(pageLocation, ITEMS_PER_PAGE, 'id'), pageLocation.search),
  );

  const auditLogList = useAppSelector(state => state.auditLog.entities);
  const loading = useAppSelector(state => state.auditLog.loading);
  const totalItems = useAppSelector(state => state.auditLog.totalItems);

  const getAllEntities = () => {
    dispatch(
      getEntities({
        page: paginationState.activePage - 1,
        size: paginationState.itemsPerPage,
        sort: `${paginationState.sort},${paginationState.order}`,
      }),
    );
  };

  const sortEntities = () => {
    getAllEntities();
    const endURL = `?page=${paginationState.activePage}&sort=${paginationState.sort},${paginationState.order}`;
    if (pageLocation.search !== endURL) {
      navigate(`${pageLocation.pathname}${endURL}`);
    }
  };

  useEffect(() => {
    sortEntities();
  }, [paginationState.activePage, paginationState.order, paginationState.sort]);

  useEffect(() => {
    const params = new URLSearchParams(pageLocation.search);
    const page = params.get('page');
    const sort = params.get(SORT);
    if (page && sort) {
      const sortSplit = sort.split(',');
      setPaginationState({
        ...paginationState,
        activePage: +page,
        sort: sortSplit[0],
        order: sortSplit[1],
      });
    }
  }, [pageLocation.search]);

  const sort = p => () => {
    setPaginationState({
      ...paginationState,
      order: paginationState.order === ASC ? DESC : ASC,
      sort: p,
    });
  };

  const handlePagination = currentPage =>
    setPaginationState({
      ...paginationState,
      activePage: currentPage,
    });

  const handleSyncList = () => {
    sortEntities();
  };

  const getSortIconByFieldName = (fieldName: string) => {
    const sortFieldName = paginationState.sort;
    const { order } = paginationState;
    if (sortFieldName !== fieldName) {
      return faSort;
    }
    return order === ASC ? faSortUp : faSortDown;
  };

  return (
    <div>
      <h2 id="audit-log-heading" data-cy="AuditLogHeading">
        Audit Logs
        <div className="d-flex justify-content-end">
          <Button className="me-2" variant="info" onClick={handleSyncList} disabled={loading}>
            <FontAwesomeIcon icon="sync" spin={loading} /> Refresh list
          </Button>
        </div>
      </h2>
      <div className="table-responsive">
        {auditLogList?.length > 0 ? (
          <Table responsive>
            <thead>
              <tr>
                <th className="hand" onClick={sort('id')}>
                  ID <FontAwesomeIcon icon={getSortIconByFieldName('id')} />
                </th>
                <th className="hand" onClick={sort('action')}>
                  Action <FontAwesomeIcon icon={getSortIconByFieldName('action')} />
                </th>
                <th className="hand" onClick={sort('entityName')}>
                  Entity Name <FontAwesomeIcon icon={getSortIconByFieldName('entityName')} />
                </th>
                <th className="hand" onClick={sort('entityId')}>
                  Entity Id <FontAwesomeIcon icon={getSortIconByFieldName('entityId')} />
                </th>
                <th className="hand" onClick={sort('reason')}>
                  Reason <FontAwesomeIcon icon={getSortIconByFieldName('reason')} />
                </th>
                <th className="hand" onClick={sort('oldValue')}>
                  Old Value <FontAwesomeIcon icon={getSortIconByFieldName('oldValue')} />
                </th>
                <th className="hand" onClick={sort('newValue')}>
                  New Value <FontAwesomeIcon icon={getSortIconByFieldName('newValue')} />
                </th>
                <th className="hand" onClick={sort('details')}>
                  Details <FontAwesomeIcon icon={getSortIconByFieldName('details')} />
                </th>
                <th className="hand" onClick={sort('performedAt')}>
                  Performed At <FontAwesomeIcon icon={getSortIconByFieldName('performedAt')} />
                </th>
                <th>
                  Actor <FontAwesomeIcon icon="sort" />
                </th>
                <th />
              </tr>
            </thead>
            <tbody>
              {auditLogList.map(auditLog => (
                <tr key={`entity-${auditLog.id}`} data-cy="entityTable">
                  <td>
                    <Button as={Link as any} to={`/audit-log/${auditLog.id}`} variant="link" size="sm">
                      {auditLog.id}
                    </Button>
                  </td>
                  <td>{auditLog.action}</td>
                  <td>{auditLog.entityName}</td>
                  <td>{auditLog.entityId}</td>
                  <td>{auditLog.reason}</td>
                  <td>{auditLog.oldValue}</td>
                  <td>{auditLog.newValue}</td>
                  <td>{auditLog.details}</td>
                  <td>{auditLog.performedAt ? <TextFormat type="date" value={auditLog.performedAt} format={APP_DATE_FORMAT} /> : null}</td>
                  <td>{auditLog.actor ? auditLog.actor.login : ''}</td>
                  <td className="text-end">
                    <div className="btn-group flex-btn-group-container">
                      <Button as={Link as any} to={`/audit-log/${auditLog.id}`} variant="info" size="sm" data-cy="entityDetailsButton">
                        <FontAwesomeIcon icon="eye" /> <span className="d-none d-md-inline">View</span>
                      </Button>
                    </div>
                  </td>
                </tr>
              ))}
            </tbody>
          </Table>
        ) : (
          !loading && <div className="alert alert-warning">No Audit Logs found</div>
        )}
      </div>
      {totalItems ? (
        <div className={auditLogList && auditLogList.length > 0 ? '' : 'd-none'}>
          <div className="justify-content-center d-flex">
            <JhiItemCount page={paginationState.activePage} total={totalItems} itemsPerPage={paginationState.itemsPerPage} />
          </div>
          <div className="justify-content-center d-flex">
            <JhiPagination
              activePage={paginationState.activePage}
              onSelect={handlePagination}
              maxButtons={5}
              itemsPerPage={paginationState.itemsPerPage}
              totalItems={totalItems}
            />
          </div>
        </div>
      ) : (
        ''
      )}
    </div>
  );
};

export default AuditLog;
