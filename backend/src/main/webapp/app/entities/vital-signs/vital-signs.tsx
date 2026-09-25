import React, { useEffect, useState } from 'react';
import { Button, Table } from 'react-bootstrap';
import { getSortState } from 'react-jhipster';
import { Link, useLocation, useNavigate } from 'react-router';

import { faSort, faSortDown, faSortUp } from '@fortawesome/free-solid-svg-icons';
import { FontAwesomeIcon } from '@fortawesome/react-fontawesome';

import { useAppDispatch, useAppSelector } from 'app/config/store';
import { overrideSortStateWithQueryParams } from 'app/shared/util/entity-utils';
import { ASC, DESC } from 'app/shared/util/pagination.constants';

import { getEntities } from './vital-signs.reducer';

export const VitalSigns = () => {
  const dispatch = useAppDispatch();

  const pageLocation = useLocation();
  const navigate = useNavigate();

  const [sortState, setSortState] = useState(overrideSortStateWithQueryParams(getSortState(pageLocation, 'id'), pageLocation.search));

  const vitalSignsList = useAppSelector(state => state.vitalSigns.entities);
  const loading = useAppSelector(state => state.vitalSigns.loading);

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
      <h2 id="vital-signs-heading" data-cy="VitalSignsHeading">
        Vital Signs
        <div className="d-flex justify-content-end">
          <Button className="me-2" variant="info" onClick={handleSyncList} disabled={loading}>
            <FontAwesomeIcon icon="sync" spin={loading} /> Refresh list
          </Button>
          <Link to="/vital-signs/new" className="btn btn-primary jh-create-entity" id="jh-create-entity" data-cy="entityCreateButton">
            <FontAwesomeIcon icon="plus" />
            &nbsp; Create a new Vital Signs
          </Link>
        </div>
      </h2>
      <div className="table-responsive">
        {vitalSignsList?.length > 0 ? (
          <Table responsive>
            <thead>
              <tr>
                <th className="hand" onClick={sort('id')}>
                  ID <FontAwesomeIcon icon={getSortIconByFieldName('id')} />
                </th>
                <th className="hand" onClick={sort('temperature')}>
                  Temperature <FontAwesomeIcon icon={getSortIconByFieldName('temperature')} />
                </th>
                <th className="hand" onClick={sort('pulseRate')}>
                  Pulse Rate <FontAwesomeIcon icon={getSortIconByFieldName('pulseRate')} />
                </th>
                <th className="hand" onClick={sort('systolicBp')}>
                  Systolic Bp <FontAwesomeIcon icon={getSortIconByFieldName('systolicBp')} />
                </th>
                <th className="hand" onClick={sort('diastolicBp')}>
                  Diastolic Bp <FontAwesomeIcon icon={getSortIconByFieldName('diastolicBp')} />
                </th>
                <th className="hand" onClick={sort('oxygenSaturation')}>
                  Oxygen Saturation <FontAwesomeIcon icon={getSortIconByFieldName('oxygenSaturation')} />
                </th>
                <th className="hand" onClick={sort('weight')}>
                  Weight <FontAwesomeIcon icon={getSortIconByFieldName('weight')} />
                </th>
                <th className="hand" onClick={sort('height')}>
                  Height <FontAwesomeIcon icon={getSortIconByFieldName('height')} />
                </th>
                <th className="hand" onClick={sort('bmi')}>
                  Bmi <FontAwesomeIcon icon={getSortIconByFieldName('bmi')} />
                </th>
                <th className="hand" onClick={sort('nutritionalStatus')}>
                  Nutritional Status <FontAwesomeIcon icon={getSortIconByFieldName('nutritionalStatus')} />
                </th>
                <th className="hand" onClick={sort('pregnancyScreening')}>
                  Pregnancy Screening <FontAwesomeIcon icon={getSortIconByFieldName('pregnancyScreening')} />
                </th>
                <th className="hand" onClick={sort('triageNotes')}>
                  Triage Notes <FontAwesomeIcon icon={getSortIconByFieldName('triageNotes')} />
                </th>
                <th className="hand" onClick={sort('otherMeasurements')}>
                  Other Measurements <FontAwesomeIcon icon={getSortIconByFieldName('otherMeasurements')} />
                </th>
                <th />
              </tr>
            </thead>
            <tbody>
              {vitalSignsList.map(vitalSigns => (
                <tr key={`entity-${vitalSigns.id}`} data-cy="entityTable">
                  <td>
                    <Button as={Link as any} to={`/vital-signs/${vitalSigns.id}`} variant="link" size="sm">
                      {vitalSigns.id}
                    </Button>
                  </td>
                  <td>{vitalSigns.temperature}</td>
                  <td>{vitalSigns.pulseRate}</td>
                  <td>{vitalSigns.systolicBp}</td>
                  <td>{vitalSigns.diastolicBp}</td>
                  <td>{vitalSigns.oxygenSaturation}</td>
                  <td>{vitalSigns.weight}</td>
                  <td>{vitalSigns.height}</td>
                  <td>{vitalSigns.bmi}</td>
                  <td>{vitalSigns.nutritionalStatus}</td>
                  <td>{vitalSigns.pregnancyScreening ? 'true' : 'false'}</td>
                  <td>{vitalSigns.triageNotes}</td>
                  <td>{vitalSigns.otherMeasurements}</td>
                  <td className="text-end">
                    <div className="btn-group flex-btn-group-container">
                      <Button as={Link as any} to={`/vital-signs/${vitalSigns.id}`} variant="info" size="sm" data-cy="entityDetailsButton">
                        <FontAwesomeIcon icon="eye" /> <span className="d-none d-md-inline">View</span>
                      </Button>
                      <Button
                        as={Link as any}
                        to={`/vital-signs/${vitalSigns.id}/edit`}
                        variant="primary"
                        size="sm"
                        data-cy="entityEditButton"
                      >
                        <FontAwesomeIcon icon="pencil-alt" /> <span className="d-none d-md-inline">Edit</span>
                      </Button>
                      <Button
                        onClick={() => (globalThis.location.href = `/vital-signs/${vitalSigns.id}/delete`)}
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
          !loading && <div className="alert alert-warning">No Vital Signs found</div>
        )}
      </div>
    </div>
  );
};

export default VitalSigns;
