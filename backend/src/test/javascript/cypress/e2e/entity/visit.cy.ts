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

describe('Visit e2e test', () => {
  const visitPageUrl = '/visit';
  let username: string;
  let password: string;
  const visitSample = {
    type: 'OUTPATIENT',
    priority: 'EMERGENCY',
    reasonForVisit: 'hydrant consequently',
    status: 'IN_VITALS',
    createdAt: '2026-09-24T03:38:42.842Z',
  };

  let visit;
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
    // create an instance at the required relationship entity:
    cy.authenticatedRequest({
      method: 'POST',
      url: '/api/patients',
      body: {
        hospitalId: 'past quietly unless',
        fullName: 'minister source',
        dateOfBirth: '2026-09-24',
        estimatedAge: 89,
        sex: 'FEMALE',
        sexEstimated: true,
        phone: '1-688-423-1502',
        email: 'Joann_Hagenes@gmail.com',
        identityDocumentType: 'BIRTH_CERTIFICATE',
        identityDocumentNumber: 'swelter foolish down',
        occupation: 'apprehensive phooey',
        maritalStatus: 'doting',
        nextOfKinName: 'warmhearted silk',
        nextOfKinPhone: 'concerning phew godfather',
        nextOfKinRelationship: 'SIBLING',
        knownAllergies: 'courteous quixotic',
        knownConditions: 'impact',
        villageEstate: 'bookcase',
        registrationStatus: 'COMPLETE',
      },
    }).then(({ body }) => {
      patient = body;
    });
  });

  beforeEach(() => {
    cy.intercept('GET', '/api/visits+(?*|)').as('entitiesRequest');
    cy.intercept('POST', '/api/visits').as('postEntityRequest');
    cy.intercept('DELETE', '/api/visits/*').as('deleteEntityRequest');
  });

  beforeEach(() => {
    // Simulate relationships api for better performance and reproducibility.
    cy.intercept('GET', '/api/vital-signs', {
      statusCode: 200,
      body: [],
    });

    cy.intercept('GET', '/api/consultations', {
      statusCode: 200,
      body: [],
    });

    cy.intercept('GET', '/api/bills', {
      statusCode: 200,
      body: [],
    });

    cy.intercept('GET', '/api/patients', {
      statusCode: 200,
      body: [patient],
    });

    cy.intercept('GET', '/api/appointments', {
      statusCode: 200,
      body: [],
    });
  });

  afterEach(() => {
    if (visit) {
      cy.authenticatedRequest({
        method: 'DELETE',
        url: `/api/visits/${visit.id}`,
      }).then(() => {
        visit = undefined;
      });
    }
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

  it('Visits menu should load Visits page', () => {
    cy.visit('/');
    cy.clickOnEntityMenuItem('visit');
    cy.wait('@entitiesRequest').then(({ response }) => {
      if (response?.body.length === 0) {
        cy.get(entityTableSelector).should('not.exist');
      } else {
        cy.get(entityTableSelector).should('exist');
      }
    });
    cy.getEntityHeading('Visit').should('exist');
    cy.location('pathname').should('eq', visitPageUrl);
  });

  describe('Visit page', () => {
    describe('create button click', () => {
      beforeEach(() => {
        cy.visit(visitPageUrl);
        cy.wait('@entitiesRequest');
      });

      it('should load create Visit page', () => {
        cy.get(entityCreateButtonSelector).click();
        cy.location('pathname').should('eq', `${visitPageUrl}/new`);
        cy.getEntityCreateUpdateHeading('Visit');
        cy.get(entityCreateSaveButtonSelector).should('exist');
        cy.get(entityCreateCancelButtonSelector).click();
        cy.wait('@entitiesRequest').then(({ response }) => {
          expect(response?.statusCode).to.equal(200);
        });
        cy.location('pathname').should('eq', visitPageUrl);
      });
    });

    describe('with existing value', () => {
      beforeEach(() => {
        cy.authenticatedRequest({
          method: 'POST',
          url: '/api/visits',
          body: {
            ...visitSample,
            patient,
          },
        }).then(({ body }) => {
          visit = body;

          cy.intercept(
            {
              method: 'GET',
              url: '/api/visits+(?*|)',
              times: 1,
            },
            {
              statusCode: 200,
              headers: {
                link: '<http://localhost/api/visits?page=0&size=20>; rel="last",<http://localhost/api/visits?page=0&size=20>; rel="first"',
              },
              body: [visit],
            },
          ).as('entitiesRequestInternal');
        });

        cy.visit(visitPageUrl);

        cy.wait('@entitiesRequestInternal');
      });

      it('detail button click should load details Visit page', () => {
        cy.get(entityDetailsButtonSelector).first().click();
        cy.getEntityDetailsHeading('visit');
        cy.get(entityDetailsBackButtonSelector).click();
        cy.wait('@entitiesRequest').then(({ response }) => {
          expect(response?.statusCode).to.equal(200);
        });
        cy.location('pathname').should('eq', visitPageUrl);
      });

      it('edit button click should load edit Visit page and go back', () => {
        cy.get(entityEditButtonSelector).first().click();
        cy.getEntityCreateUpdateHeading('Visit');
        cy.get(entityCreateSaveButtonSelector).should('exist');
        cy.get(entityCreateCancelButtonSelector).click();
        cy.wait('@entitiesRequest').then(({ response }) => {
          expect(response?.statusCode).to.equal(200);
        });
        cy.location('pathname').should('eq', visitPageUrl);
      });

      it('edit button click should load edit Visit page and save', () => {
        cy.get(entityEditButtonSelector).first().click();
        cy.getEntityCreateUpdateHeading('Visit');
        cy.get(entityCreateSaveButtonSelector).click();
        cy.wait('@entitiesRequest').then(({ response }) => {
          expect(response?.statusCode).to.equal(200);
        });
        cy.location('pathname').should('eq', visitPageUrl);
      });

      it('last delete button click should delete instance of Visit', () => {
        cy.intercept('GET', '/api/visits/*').as('dialogDeleteRequest');
        cy.get(entityDeleteButtonSelector).last().click();
        cy.wait('@dialogDeleteRequest');
        cy.getEntityDeleteDialogHeading('visit').should('exist');
        cy.get(entityConfirmDeleteButtonSelector).click();
        cy.wait('@deleteEntityRequest').then(({ response }) => {
          expect(response?.statusCode).to.equal(204);
        });
        cy.wait('@entitiesRequest').then(({ response }) => {
          expect(response?.statusCode).to.equal(200);
        });
        cy.location('pathname').should('eq', visitPageUrl);

        visit = undefined;
      });
    });
  });

  describe('new Visit page', () => {
    beforeEach(() => {
      cy.visit(visitPageUrl);
      cy.get(entityCreateButtonSelector).click();
      cy.getEntityCreateUpdateHeading('Visit');
    });

    it('should create an instance of Visit', () => {
      cy.get(`[data-cy="type"]`).select('OUTPATIENT');

      cy.get(`[data-cy="priority"]`).select('URGENT');

      cy.get(`[data-cy="reasonForVisit"]`).type('stay');
      cy.get(`[data-cy="reasonForVisit"]`).should('have.value', 'stay');

      cy.get(`[data-cy="status"]`).select('WAITING_DOCTOR');

      cy.get(`[data-cy="queueSkipReason"]`).type('unlike although');
      cy.get(`[data-cy="queueSkipReason"]`).should('have.value', 'unlike although');

      cy.get(`[data-cy="createdAt"]`).type('2026-09-24T01:22');
      cy.get(`[data-cy="createdAt"]`).blur();
      cy.get(`[data-cy="createdAt"]`).should('have.value', '2026-09-24T01:22');

      cy.get(`[data-cy="startedVitalsAt"]`).type('2026-09-23T10:52');
      cy.get(`[data-cy="startedVitalsAt"]`).blur();
      cy.get(`[data-cy="startedVitalsAt"]`).should('have.value', '2026-09-23T10:52');

      cy.get(`[data-cy="startedConsultationAt"]`).type('2026-09-23T19:23');
      cy.get(`[data-cy="startedConsultationAt"]`).blur();
      cy.get(`[data-cy="startedConsultationAt"]`).should('have.value', '2026-09-23T19:23');

      cy.get(`[data-cy="closedAt"]`).type('2026-09-23T11:37');
      cy.get(`[data-cy="closedAt"]`).blur();
      cy.get(`[data-cy="closedAt"]`).should('have.value', '2026-09-23T11:37');

      cy.get(`[data-cy="patient"]`).select(1);

      cy.get(entityCreateSaveButtonSelector).click();

      cy.wait('@postEntityRequest').then(({ response }) => {
        expect(response?.statusCode).to.equal(201);
        visit = response.body;
      });
      cy.wait('@entitiesRequest').then(({ response }) => {
        expect(response?.statusCode).to.equal(200);
      });
      cy.location('pathname').should('eq', visitPageUrl);
    });
  });
});
