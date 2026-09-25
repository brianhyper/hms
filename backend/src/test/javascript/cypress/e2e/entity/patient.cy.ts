import {
  entityConfirmDeleteButtonSelector,
  entityCreateButtonSelector,
  entityCreateCancelButtonSelector,
  entityCreateSaveButtonSelector,
  entityDeleteButtonSelector,
  entityDetailsBackButtonSelector,
  entityDetailsButtonSelector,
  entityEditButtonSelector,
  entityTableSelector,
} from '../../support/entity';

describe('Patient e2e test', () => {
  const patientPageUrl = '/patient';
  let username: string;
  let password: string;
  const patientSample = {
    hospitalId: 'pace hornet how',
    fullName: 'searchingly safely',
    sex: 'OTHER',
    sexEstimated: true,
    registrationStatus: 'COMPLETE',
  };

  let patient;

  before(() => {
    cy.credentials().then(credentials => {
      ({ username, password } = credentials);
    });
  });

  beforeEach(() => {
    cy.login(username, password);
  });

  beforeEach(() => {
    cy.intercept('GET', '/api/patients+(?*|)').as('entitiesRequest');
    cy.intercept('POST', '/api/patients').as('postEntityRequest');
    cy.intercept('DELETE', '/api/patients/*').as('deleteEntityRequest');
  });

  afterEach(() => {
    if (patient) {
      cy.authenticatedRequest({
        method: 'DELETE',
        url: `/api/patients/${patient.id}`,
      }).then(() => {
        patient = undefined;
      });
    }
  });

  it('Patients menu should load Patients page', () => {
    cy.visit('/');
    cy.clickOnEntityMenuItem('patient');
    cy.wait('@entitiesRequest').then(({ response }) => {
      if (response?.body.length === 0) {
        cy.get(entityTableSelector).should('not.exist');
      } else {
        cy.get(entityTableSelector).should('exist');
      }
    });
    cy.getEntityHeading('Patient').should('exist');
    cy.location('pathname').should('eq', patientPageUrl);
  });

  describe('Patient page', () => {
    describe('create button click', () => {
      beforeEach(() => {
        cy.visit(patientPageUrl);
        cy.wait('@entitiesRequest');
      });

      it('should load create Patient page', () => {
        cy.get(entityCreateButtonSelector).click();
        cy.location('pathname').should('eq', `${patientPageUrl}/new`);
        cy.getEntityCreateUpdateHeading('Patient');
        cy.get(entityCreateSaveButtonSelector).should('exist');
        cy.get(entityCreateCancelButtonSelector).click();
        cy.wait('@entitiesRequest').then(({ response }) => {
          expect(response?.statusCode).to.equal(200);
        });
        cy.location('pathname').should('eq', patientPageUrl);
      });
    });

    describe('with existing value', () => {
      beforeEach(() => {
        cy.authenticatedRequest({
          method: 'POST',
          url: '/api/patients',
          body: patientSample,
        }).then(({ body }) => {
          patient = body;

          cy.intercept(
            {
              method: 'GET',
              url: '/api/patients+(?*|)',
              times: 1,
            },
            {
              statusCode: 200,
              headers: {
                link: '<http://localhost/api/patients?page=0&size=20>; rel="last",<http://localhost/api/patients?page=0&size=20>; rel="first"',
              },
              body: [patient],
            },
          ).as('entitiesRequestInternal');
        });

        cy.visit(patientPageUrl);

        cy.wait('@entitiesRequestInternal');
      });

      it('detail button click should load details Patient page', () => {
        cy.get(entityDetailsButtonSelector).first().click();
        cy.getEntityDetailsHeading('patient');
        cy.get(entityDetailsBackButtonSelector).click();
        cy.wait('@entitiesRequest').then(({ response }) => {
          expect(response?.statusCode).to.equal(200);
        });
        cy.location('pathname').should('eq', patientPageUrl);
      });

      it('edit button click should load edit Patient page and go back', () => {
        cy.get(entityEditButtonSelector).first().click();
        cy.getEntityCreateUpdateHeading('Patient');
        cy.get(entityCreateSaveButtonSelector).should('exist');
        cy.get(entityCreateCancelButtonSelector).click();
        cy.wait('@entitiesRequest').then(({ response }) => {
          expect(response?.statusCode).to.equal(200);
        });
        cy.location('pathname').should('eq', patientPageUrl);
      });

      it('edit button click should load edit Patient page and save', () => {
        cy.get(entityEditButtonSelector).first().click();
        cy.getEntityCreateUpdateHeading('Patient');
        cy.get(entityCreateSaveButtonSelector).click();
        cy.wait('@entitiesRequest').then(({ response }) => {
          expect(response?.statusCode).to.equal(200);
        });
        cy.location('pathname').should('eq', patientPageUrl);
      });

      it('last delete button click should delete instance of Patient', () => {
        cy.intercept('GET', '/api/patients/*').as('dialogDeleteRequest');
        cy.get(entityDeleteButtonSelector).last().click();
        cy.wait('@dialogDeleteRequest');
        cy.getEntityDeleteDialogHeading('patient').should('exist');
        cy.get(entityConfirmDeleteButtonSelector).click();
        cy.wait('@deleteEntityRequest').then(({ response }) => {
          expect(response?.statusCode).to.equal(204);
        });
        cy.wait('@entitiesRequest').then(({ response }) => {
          expect(response?.statusCode).to.equal(200);
        });
        cy.location('pathname').should('eq', patientPageUrl);

        patient = undefined;
      });
    });
  });

  describe('new Patient page', () => {
    beforeEach(() => {
      cy.visit(patientPageUrl);
      cy.get(entityCreateButtonSelector).click();
      cy.getEntityCreateUpdateHeading('Patient');
    });

    it('should create an instance of Patient', () => {
      cy.get(`[data-cy="hospitalId"]`).type('per times');
      cy.get(`[data-cy="hospitalId"]`).should('have.value', 'per times');

      cy.get(`[data-cy="fullName"]`).type('geez redress');
      cy.get(`[data-cy="fullName"]`).should('have.value', 'geez redress');

      cy.get(`[data-cy="dateOfBirth"]`).type('2026-09-23');
      cy.get(`[data-cy="dateOfBirth"]`).blur();
      cy.get(`[data-cy="dateOfBirth"]`).should('have.value', '2026-09-23');

      cy.get(`[data-cy="estimatedAge"]`).type('29');
      cy.get(`[data-cy="estimatedAge"]`).should('have.value', '29');

      cy.get(`[data-cy="sex"]`).select('FEMALE');

      cy.get(`[data-cy="sexEstimated"]`).should('not.be.checked');
      cy.get(`[data-cy="sexEstimated"]`).click();
      cy.get(`[data-cy="sexEstimated"]`).should('be.checked');

      cy.get(`[data-cy="phone"]`).type('1-790-337-1685 x508');
      cy.get(`[data-cy="phone"]`).should('have.value', '1-790-337-1685 x508');

      cy.get(`[data-cy="email"]`).type('Prince.Goyette@gmail.com');
      cy.get(`[data-cy="email"]`).should('have.value', 'Prince.Goyette@gmail.com');

      cy.get(`[data-cy="identityDocumentType"]`).select('PASSPORT');

      cy.get(`[data-cy="identityDocumentNumber"]`).type('till through tighten');
      cy.get(`[data-cy="identityDocumentNumber"]`).should('have.value', 'till through tighten');

      cy.get(`[data-cy="occupation"]`).type('perfectly communicate');
      cy.get(`[data-cy="occupation"]`).should('have.value', 'perfectly communicate');

      cy.get(`[data-cy="maritalStatus"]`).type('pension');
      cy.get(`[data-cy="maritalStatus"]`).should('have.value', 'pension');

      cy.get(`[data-cy="nextOfKinName"]`).type('ectoderm');
      cy.get(`[data-cy="nextOfKinName"]`).should('have.value', 'ectoderm');

      cy.get(`[data-cy="nextOfKinPhone"]`).type('supposing handover notwithstandi');
      cy.get(`[data-cy="nextOfKinPhone"]`).should('have.value', 'supposing handover notwithstandi');

      cy.get(`[data-cy="nextOfKinRelationship"]`).select('OTHER_RELATIVE');

      cy.get(`[data-cy="knownAllergies"]`).type('honestly furthermore');
      cy.get(`[data-cy="knownAllergies"]`).should('have.value', 'honestly furthermore');

      cy.get(`[data-cy="knownConditions"]`).type('handsome what sheepishly');
      cy.get(`[data-cy="knownConditions"]`).should('have.value', 'handsome what sheepishly');

      cy.get(`[data-cy="villageEstate"]`).type('but westernise who');
      cy.get(`[data-cy="villageEstate"]`).should('have.value', 'but westernise who');

      cy.get(`[data-cy="registrationStatus"]`).select('MERGED');

      cy.get(entityCreateSaveButtonSelector).click();

      cy.wait('@postEntityRequest').then(({ response }) => {
        expect(response?.statusCode).to.equal(201);
        patient = response.body;
      });
      cy.wait('@entitiesRequest').then(({ response }) => {
        expect(response?.statusCode).to.equal(200);
      });
      cy.location('pathname').should('eq', patientPageUrl);
    });
  });
});
