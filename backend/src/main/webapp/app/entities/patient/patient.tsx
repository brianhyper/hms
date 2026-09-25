import React, { useEffect, useState } from 'react';
import { Button, Table } from 'react-bootstrap';
import { JhiItemCount, JhiPagination, TextFormat, getPaginationState } from 'react-jhipster';
import { Link, useLocation, useNavigate } from 'react-router';

import { faSort, faSortDown, faSortUp } from '@fortawesome/free-solid-svg-icons';
import { FontAwesomeIcon } from '@fortawesome/react-fontawesome';

import { APP_LOCAL_DATE_FORMAT } from 'app/config/constants';
import { useAppDispatch, useAppSelector } from 'app/config/store';
import { overridePaginationStateWithQueryParams } from 'app/shared/util/entity-utils';
import { ASC, DESC, ITEMS_PER_PAGE, SORT } from 'app/shared/util/pagination.constants';

import { getEntities } from './patient.reducer';

export const Patient = () => {
  const dispatch = useAppDispatch();

  const pageLocation = useLocation();
  const navigate = useNavigate();

  const [paginationState, setPaginationState] = useState(
    overridePaginationStateWithQueryParams(getPaginationState(pageLocation, ITEMS_PER_PAGE, 'id'), pageLocation.search),
  );

  const patientList = useAppSelector(state => state.patient.entities);
  const loading = useAppSelector(state => state.patient.loading);
  const totalItems = useAppSelector(state => state.patient.totalItems);

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
      <h2 id="patient-heading" data-cy="PatientHeading">
        Patients
        <div className="d-flex justify-content-end">
          <Button className="me-2" variant="info" onClick={handleSyncList} disabled={loading}>
            <FontAwesomeIcon icon="sync" spin={loading} /> Refresh list
          </Button>
          <Link to="/patient/new" className="btn btn-primary jh-create-entity" id="jh-create-entity" data-cy="entityCreateButton">
            <FontAwesomeIcon icon="plus" />
            &nbsp; Create a new Patient
          </Link>
        </div>
      </h2>
      <div className="table-responsive">
        {patientList?.length > 0 ? (
          <Table responsive>
            <thead>
              <tr>
                <th className="hand" onClick={sort('id')}>
                  ID <FontAwesomeIcon icon={getSortIconByFieldName('id')} />
                </th>
                <th className="hand" onClick={sort('hospitalId')}>
                  Hospital Id <FontAwesomeIcon icon={getSortIconByFieldName('hospitalId')} />
                </th>
                <th className="hand" onClick={sort('fullName')}>
                  Full Name <FontAwesomeIcon icon={getSortIconByFieldName('fullName')} />
                </th>
                <th className="hand" onClick={sort('dateOfBirth')}>
                  Date Of Birth <FontAwesomeIcon icon={getSortIconByFieldName('dateOfBirth')} />
                </th>
                <th className="hand" onClick={sort('estimatedAge')}>
                  Estimated Age <FontAwesomeIcon icon={getSortIconByFieldName('estimatedAge')} />
                </th>
                <th className="hand" onClick={sort('sex')}>
                  Sex <FontAwesomeIcon icon={getSortIconByFieldName('sex')} />
                </th>
                <th className="hand" onClick={sort('sexEstimated')}>
                  Sex Estimated <FontAwesomeIcon icon={getSortIconByFieldName('sexEstimated')} />
                </th>
                <th className="hand" onClick={sort('phone')}>
                  Phone <FontAwesomeIcon icon={getSortIconByFieldName('phone')} />
                </th>
                <th className="hand" onClick={sort('email')}>
                  Email <FontAwesomeIcon icon={getSortIconByFieldName('email')} />
                </th>
                <th className="hand" onClick={sort('identityDocumentType')}>
                  Identity Document Type <FontAwesomeIcon icon={getSortIconByFieldName('identityDocumentType')} />
                </th>
                <th className="hand" onClick={sort('identityDocumentNumber')}>
                  Identity Document Number <FontAwesomeIcon icon={getSortIconByFieldName('identityDocumentNumber')} />
                </th>
                <th className="hand" onClick={sort('occupation')}>
                  Occupation <FontAwesomeIcon icon={getSortIconByFieldName('occupation')} />
                </th>
                <th className="hand" onClick={sort('maritalStatus')}>
                  Marital Status <FontAwesomeIcon icon={getSortIconByFieldName('maritalStatus')} />
                </th>
                <th className="hand" onClick={sort('nextOfKinName')}>
                  Next Of Kin Name <FontAwesomeIcon icon={getSortIconByFieldName('nextOfKinName')} />
                </th>
                <th className="hand" onClick={sort('nextOfKinPhone')}>
                  Next Of Kin Phone <FontAwesomeIcon icon={getSortIconByFieldName('nextOfKinPhone')} />
                </th>
                <th className="hand" onClick={sort('nextOfKinRelationship')}>
                  Next Of Kin Relationship <FontAwesomeIcon icon={getSortIconByFieldName('nextOfKinRelationship')} />
                </th>
                <th className="hand" onClick={sort('knownAllergies')}>
                  Known Allergies <FontAwesomeIcon icon={getSortIconByFieldName('knownAllergies')} />
                </th>
                <th className="hand" onClick={sort('knownConditions')}>
                  Known Conditions <FontAwesomeIcon icon={getSortIconByFieldName('knownConditions')} />
                </th>
                <th className="hand" onClick={sort('villageEstate')}>
                  Village Estate <FontAwesomeIcon icon={getSortIconByFieldName('villageEstate')} />
                </th>
                <th className="hand" onClick={sort('registrationStatus')}>
                  Registration Status <FontAwesomeIcon icon={getSortIconByFieldName('registrationStatus')} />
                </th>
                <th>
                  Merged Into Patient <FontAwesomeIcon icon="sort" />
                </th>
                <th />
              </tr>
            </thead>
            <tbody>
              {patientList.map(patient => (
                <tr key={`entity-${patient.id}`} data-cy="entityTable">
                  <td>
                    <Button as={Link as any} to={`/patient/${patient.id}`} variant="link" size="sm">
                      {patient.id}
                    </Button>
                  </td>
                  <td>{patient.hospitalId}</td>
                  <td>{patient.fullName}</td>
                  <td>
                    {patient.dateOfBirth ? <TextFormat type="date" value={patient.dateOfBirth} format={APP_LOCAL_DATE_FORMAT} /> : null}
                  </td>
                  <td>{patient.estimatedAge}</td>
                  <td>{patient.sex}</td>
                  <td>{patient.sexEstimated ? 'true' : 'false'}</td>
                  <td>{patient.phone}</td>
                  <td>{patient.email}</td>
                  <td>{patient.identityDocumentType}</td>
                  <td>{patient.identityDocumentNumber}</td>
                  <td>{patient.occupation}</td>
                  <td>{patient.maritalStatus}</td>
                  <td>{patient.nextOfKinName}</td>
                  <td>{patient.nextOfKinPhone}</td>
                  <td>{patient.nextOfKinRelationship}</td>
                  <td>{patient.knownAllergies}</td>
                  <td>{patient.knownConditions}</td>
                  <td>{patient.villageEstate}</td>
                  <td>{patient.registrationStatus}</td>
                  <td>
                    {patient.mergedIntoPatient ? (
                      <Link to={`/patient/${patient.mergedIntoPatient.id}`}>{patient.mergedIntoPatient.id}</Link>
                    ) : (
                      ''
                    )}
                  </td>
                  <td className="text-end">
                    <div className="btn-group flex-btn-group-container">
                      <Button as={Link as any} to={`/patient/${patient.id}`} variant="info" size="sm" data-cy="entityDetailsButton">
                        <FontAwesomeIcon icon="eye" /> <span className="d-none d-md-inline">View</span>
                      </Button>
                      <Button
                        as={Link as any}
                        to={`/patient/${patient.id}/edit?page=${paginationState.activePage}&sort=${paginationState.sort},${paginationState.order}`}
                        variant="primary"
                        size="sm"
                        data-cy="entityEditButton"
                      >
                        <FontAwesomeIcon icon="pencil-alt" /> <span className="d-none d-md-inline">Edit</span>
                      </Button>
                      <Button
                        onClick={() =>
                          (globalThis.location.href = `/patient/${patient.id}/delete?page=${paginationState.activePage}&sort=${paginationState.sort},${paginationState.order}`)
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
          !loading && <div className="alert alert-warning">No Patients found</div>
        )}
      </div>
      {totalItems ? (
        <div className={patientList && patientList.length > 0 ? '' : 'd-none'}>
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

export default Patient;
