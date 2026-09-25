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

import { getEntities } from './diagnostic-order.reducer';

export const DiagnosticOrder = () => {
  const dispatch = useAppDispatch();

  const pageLocation = useLocation();
  const navigate = useNavigate();

  const [paginationState, setPaginationState] = useState(
    overridePaginationStateWithQueryParams(getPaginationState(pageLocation, ITEMS_PER_PAGE, 'id'), pageLocation.search),
  );

  const diagnosticOrderList = useAppSelector(state => state.diagnosticOrder.entities);
  const loading = useAppSelector(state => state.diagnosticOrder.loading);
  const totalItems = useAppSelector(state => state.diagnosticOrder.totalItems);

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
      <h2 id="diagnostic-order-heading" data-cy="DiagnosticOrderHeading">
        Diagnostic Orders
        <div className="d-flex justify-content-end">
          <Button className="me-2" variant="info" onClick={handleSyncList} disabled={loading}>
            <FontAwesomeIcon icon="sync" spin={loading} /> Refresh list
          </Button>
          <Link to="/diagnostic-order/new" className="btn btn-primary jh-create-entity" id="jh-create-entity" data-cy="entityCreateButton">
            <FontAwesomeIcon icon="plus" />
            &nbsp; Create a new Diagnostic Order
          </Link>
        </div>
      </h2>
      <div className="table-responsive">
        {diagnosticOrderList?.length > 0 ? (
          <Table responsive>
            <thead>
              <tr>
                <th className="hand" onClick={sort('id')}>
                  ID <FontAwesomeIcon icon={getSortIconByFieldName('id')} />
                </th>
                <th className="hand" onClick={sort('type')}>
                  Type <FontAwesomeIcon icon={getSortIconByFieldName('type')} />
                </th>
                <th className="hand" onClick={sort('testName')}>
                  Test Name <FontAwesomeIcon icon={getSortIconByFieldName('testName')} />
                </th>
                <th className="hand" onClick={sort('status')}>
                  Status <FontAwesomeIcon icon={getSortIconByFieldName('status')} />
                </th>
                <th className="hand" onClick={sort('notes')}>
                  Notes <FontAwesomeIcon icon={getSortIconByFieldName('notes')} />
                </th>
                <th className="hand" onClick={sort('orderedAt')}>
                  Ordered At <FontAwesomeIcon icon={getSortIconByFieldName('orderedAt')} />
                </th>
                <th>
                  Result <FontAwesomeIcon icon="sort" />
                </th>
                <th>
                  Visit <FontAwesomeIcon icon="sort" />
                </th>
                <th>
                  Ordered By <FontAwesomeIcon icon="sort" />
                </th>
                <th>
                  Lab Test <FontAwesomeIcon icon="sort" />
                </th>
                <th>
                  Radiology Exam <FontAwesomeIcon icon="sort" />
                </th>
                <th />
              </tr>
            </thead>
            <tbody>
              {diagnosticOrderList.map(diagnosticOrder => (
                <tr key={`entity-${diagnosticOrder.id}`} data-cy="entityTable">
                  <td>
                    <Button as={Link as any} to={`/diagnostic-order/${diagnosticOrder.id}`} variant="link" size="sm">
                      {diagnosticOrder.id}
                    </Button>
                  </td>
                  <td>{diagnosticOrder.type}</td>
                  <td>{diagnosticOrder.testName}</td>
                  <td>{diagnosticOrder.status}</td>
                  <td>{diagnosticOrder.notes}</td>
                  <td>
                    {diagnosticOrder.orderedAt ? (
                      <TextFormat type="date" value={diagnosticOrder.orderedAt} format={APP_DATE_FORMAT} />
                    ) : null}
                  </td>
                  <td>
                    {diagnosticOrder.result ? <Link to={`/result/${diagnosticOrder.result.id}`}>{diagnosticOrder.result.id}</Link> : ''}
                  </td>
                  <td>{diagnosticOrder.visit ? <Link to={`/visit/${diagnosticOrder.visit.id}`}>{diagnosticOrder.visit.id}</Link> : ''}</td>
                  <td>{diagnosticOrder.orderedBy ? diagnosticOrder.orderedBy.login : ''}</td>
                  <td>
                    {diagnosticOrder.labTest ? (
                      <Link to={`/lab-test/${diagnosticOrder.labTest.id}`}>{diagnosticOrder.labTest.id}</Link>
                    ) : (
                      ''
                    )}
                  </td>
                  <td>
                    {diagnosticOrder.radiologyExam ? (
                      <Link to={`/radiology-exam/${diagnosticOrder.radiologyExam.id}`}>{diagnosticOrder.radiologyExam.id}</Link>
                    ) : (
                      ''
                    )}
                  </td>
                  <td className="text-end">
                    <div className="btn-group flex-btn-group-container">
                      <Button
                        as={Link as any}
                        to={`/diagnostic-order/${diagnosticOrder.id}`}
                        variant="info"
                        size="sm"
                        data-cy="entityDetailsButton"
                      >
                        <FontAwesomeIcon icon="eye" /> <span className="d-none d-md-inline">View</span>
                      </Button>
                      <Button
                        as={Link as any}
                        to={`/diagnostic-order/${diagnosticOrder.id}/edit?page=${paginationState.activePage}&sort=${paginationState.sort},${paginationState.order}`}
                        variant="primary"
                        size="sm"
                        data-cy="entityEditButton"
                      >
                        <FontAwesomeIcon icon="pencil-alt" /> <span className="d-none d-md-inline">Edit</span>
                      </Button>
                      <Button
                        onClick={() =>
                          (globalThis.location.href = `/diagnostic-order/${diagnosticOrder.id}/delete?page=${paginationState.activePage}&sort=${paginationState.sort},${paginationState.order}`)
                        }
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
          !loading && <div className="alert alert-warning">No Diagnostic Orders found</div>
        )}
      </div>
      {totalItems ? (
        <div className={diagnosticOrderList && diagnosticOrderList.length > 0 ? '' : 'd-none'}>
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

export default DiagnosticOrder;
