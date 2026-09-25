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

describe('DiagnosticOrder e2e test', () => {
  const diagnosticOrderPageUrl = '/diagnostic-order';
  let username: string;
  let password: string;
  // const diagnosticOrderSample = {"type":"LAB","testName":"between writ overproduce","status":"COMPLETED","orderedAt":"2026-09-23T19:37:39.583Z"};

  let diagnosticOrder;
  // let visit;
  // let user;

  before(() => {
    cy.credentials().then(credentials => {
      ({ username, password } = credentials);
    });
  });

  beforeEach(() => {
    cy.login(username, password);
  });

  /* Disabled due to incompatibility
  beforeEach(() => {
    // create an instance at the required relationship entity:
    cy.authenticatedRequest({
      method: 'POST',
      url: '/api/visits',
      body: {"type":"OUTPATIENT","priority":"NORMAL","reasonForVisit":"under gleefully aw","status":"WAITING_DOCTOR","queueSkipReason":"scotch before","createdAt":"2026-09-24T04:57:44.383Z","startedVitalsAt":"2026-09-23T20:03:53.041Z","startedConsultationAt":"2026-09-23T19:34:05.896Z","closedAt":"2026-09-23T09:42:01.397Z"},
    }).then(({ body }) => {
      visit = body;
    });
    // create an instance at the required relationship entity:
    cy.authenticatedRequest({
      method: 'POST',
      url: '/api/users',
      body: {"login":"Cameron.Adams","firstName":"Millie","lastName":"Rutherford","email":"Maye.Ryan7@yahoo.com","imageUrl":"aside now happily"},
    }).then(({ body }) => {
      user = body;
    });
  });
   */

  beforeEach(() => {
    cy.intercept('GET', '/api/diagnostic-orders+(?*|)').as('entitiesRequest');
    cy.intercept('POST', '/api/diagnostic-orders').as('postEntityRequest');
    cy.intercept('DELETE', '/api/diagnostic-orders/*').as('deleteEntityRequest');
  });

  /* Disabled due to incompatibility
  beforeEach(() => {
    // Simulate relationships api for better performance and reproducibility.
    cy.intercept('GET', '/api/results', {
      statusCode: 200,
      body: [],
    });

    cy.intercept('GET', '/api/visits', {
      statusCode: 200,
      body: [visit],
    });

    cy.intercept('GET', '/api/users', {
      statusCode: 200,
      body: [user],
    });

    cy.intercept('GET', '/api/lab-tests', {
      statusCode: 200,
      body: [],
    });

    cy.intercept('GET', '/api/radiology-exams', {
      statusCode: 200,
      body: [],
    });

  });
   */

  afterEach(() => {
    if (diagnosticOrder) {
      cy.authenticatedRequest({
        method: 'DELETE',
        url: `/api/diagnostic-orders/${diagnosticOrder.id}`,
      }).then(() => {
        diagnosticOrder = undefined;
      });
    }
  });

  /* Disabled due to incompatibility
  afterEach(() => {
    if (visit) {
      cy.authenticatedRequest({
        method: 'DELETE',
        url: `/api/visits/${visit.id}`,
      }).then(() => {
        visit = undefined;
      });
    }
    if (user) {
      cy.authenticatedRequest({
        method: 'DELETE',
        url: `/api/users/${user.id}`,
      }).then(() => {
        user = undefined;
      });
    }
  });
   */

  it('DiagnosticOrders menu should load DiagnosticOrders page', () => {
    cy.visit('/');
    cy.clickOnEntityMenuItem('diagnostic-order');
    cy.wait('@entitiesRequest').then(({ response }) => {
      if (response?.body.length === 0) {
        cy.get(entityTableSelector).should('not.exist');
      } else {
        cy.get(entityTableSelector).should('exist');
      }
    });
    cy.getEntityHeading('DiagnosticOrder').should('exist');
    cy.location('pathname').should('eq', diagnosticOrderPageUrl);
  });

  describe('DiagnosticOrder page', () => {
    describe('create button click', () => {
      beforeEach(() => {
        cy.visit(diagnosticOrderPageUrl);
        cy.wait('@entitiesRequest');
      });

      it('should load create DiagnosticOrder page', () => {
        cy.get(entityCreateButtonSelector).click();
        cy.location('pathname').should('eq', `${diagnosticOrderPageUrl}/new`);
        cy.getEntityCreateUpdateHeading('DiagnosticOrder');
        cy.get(entityCreateSaveButtonSelector).should('exist');
        cy.get(entityCreateCancelButtonSelector).click();
        cy.wait('@entitiesRequest').then(({ response }) => {
          expect(response?.statusCode).to.equal(200);
        });
        cy.location('pathname').should('eq', diagnosticOrderPageUrl);
      });
    });

    describe('with existing value', () => {
      /* Disabled due to incompatibility
      beforeEach(() => {
        cy.authenticatedRequest({
          method: 'POST',
          url: '/api/diagnostic-orders',
          body: {
            ...diagnosticOrderSample,
            visit: visit,
            orderedBy: user,
          },
        }).then(({ body }) => {
          diagnosticOrder = body;

          cy.intercept(
            {
              method: 'GET',
              url: '/api/diagnostic-orders+(?*|)',
              times: 1,
            },
            {
              statusCode: 200,
              headers: {
                link: '<http://localhost/api/diagnostic-orders?page=0&size=20>; rel="last",<http://localhost/api/diagnostic-orders?page=0&size=20>; rel="first"',
              },
              body: [diagnosticOrder],
            }
          ).as('entitiesRequestInternal');
        });

        cy.visit(diagnosticOrderPageUrl);

        cy.wait('@entitiesRequestInternal');
      });
       */

      beforeEach(function () {
        cy.visit(diagnosticOrderPageUrl);

        cy.wait('@entitiesRequest').then(({ response }) => {
          if (response?.body.length === 0) {
            this.skip();
          }
        });
      });

      it('detail button click should load details DiagnosticOrder page', () => {
        cy.get(entityDetailsButtonSelector).first().click();
        cy.getEntityDetailsHeading('diagnosticOrder');
        cy.get(entityDetailsBackButtonSelector).click();
        cy.wait('@entitiesRequest').then(({ response }) => {
          expect(response?.statusCode).to.equal(200);
        });
        cy.location('pathname').should('eq', diagnosticOrderPageUrl);
      });

      it('edit button click should load edit DiagnosticOrder page and go back', () => {
        cy.get(entityEditButtonSelector).first().click();
        cy.getEntityCreateUpdateHeading('DiagnosticOrder');
        cy.get(entityCreateSaveButtonSelector).should('exist');
        cy.get(entityCreateCancelButtonSelector).click();
        cy.wait('@entitiesRequest').then(({ response }) => {
          expect(response?.statusCode).to.equal(200);
        });
        cy.location('pathname').should('eq', diagnosticOrderPageUrl);
      });

      it('edit button click should load edit DiagnosticOrder page and save', () => {
        cy.get(entityEditButtonSelector).first().click();
        cy.getEntityCreateUpdateHeading('DiagnosticOrder');
        cy.get(entityCreateSaveButtonSelector).click();
        cy.wait('@entitiesRequest').then(({ response }) => {
          expect(response?.statusCode).to.equal(200);
        });
        cy.location('pathname').should('eq', diagnosticOrderPageUrl);
      });

      // Reason: cannot create a required entity with relationship with required relationships.
      it.skip('last delete button click should delete instance of DiagnosticOrder', () => {
        cy.intercept('GET', '/api/diagnostic-orders/*').as('dialogDeleteRequest');
        cy.get(entityDeleteButtonSelector).last().click();
        cy.wait('@dialogDeleteRequest');
        cy.getEntityDeleteDialogHeading('diagnosticOrder').should('exist');
        cy.get(entityConfirmDeleteButtonSelector).click();
        cy.wait('@deleteEntityRequest').then(({ response }) => {
          expect(response?.statusCode).to.equal(204);
        });
        cy.wait('@entitiesRequest').then(({ response }) => {
          expect(response?.statusCode).to.equal(200);
        });
        cy.location('pathname').should('eq', diagnosticOrderPageUrl);

        diagnosticOrder = undefined;
      });
    });
  });

  describe('new DiagnosticOrder page', () => {
    beforeEach(() => {
      cy.visit(diagnosticOrderPageUrl);
      cy.get(entityCreateButtonSelector).click();
      cy.getEntityCreateUpdateHeading('DiagnosticOrder');
    });

    // Reason: cannot create a required entity with relationship with required relationships.
    it.skip('should create an instance of DiagnosticOrder', () => {
      cy.get(`[data-cy="type"]`).select('RADIOLOGY');

      cy.get(`[data-cy="testName"]`).type('apropos anenst pish');
      cy.get(`[data-cy="testName"]`).should('have.value', 'apropos anenst pish');

      cy.get(`[data-cy="status"]`).select('CANCELLED');

      cy.get(`[data-cy="notes"]`).type('like');
      cy.get(`[data-cy="notes"]`).should('have.value', 'like');

      cy.get(`[data-cy="orderedAt"]`).type('2026-09-23T15:39');
      cy.get(`[data-cy="orderedAt"]`).blur();
      cy.get(`[data-cy="orderedAt"]`).should('have.value', '2026-09-23T15:39');

      cy.get(`[data-cy="visit"]`).select(1);
      cy.get(`[data-cy="orderedBy"]`).select(1);

      cy.get(entityCreateSaveButtonSelector).click();

      cy.wait('@postEntityRequest').then(({ response }) => {
        expect(response?.statusCode).to.equal(201);
        diagnosticOrder = response.body;
      });
      cy.wait('@entitiesRequest').then(({ response }) => {
        expect(response?.statusCode).to.equal(200);
      });
      cy.location('pathname').should('eq', diagnosticOrderPageUrl);
    });
  });
});
