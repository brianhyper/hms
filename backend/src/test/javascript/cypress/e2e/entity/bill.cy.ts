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

describe('Bill e2e test', () => {
  const billPageUrl = '/bill';
  let username: string;
  let password: string;
  // const billSample = {"totalAmount":11479.9,"status":"UNPAID"};

  let bill;
  // let visit;

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
      body: {"type":"ADMISSION","priority":"EMERGENCY","reasonForVisit":"for","status":"WAITING_PAYMENT","queueSkipReason":"clearly","createdAt":"2026-09-24T09:12:51.506Z","startedVitalsAt":"2026-09-23T13:41:01.902Z","startedConsultationAt":"2026-09-24T08:43:00.599Z","closedAt":"2026-09-23T22:17:30.018Z"},
    }).then(({ body }) => {
      visit = body;
    });
  });
   */

  beforeEach(() => {
    cy.intercept('GET', '/api/bills+(?*|)').as('entitiesRequest');
    cy.intercept('POST', '/api/bills').as('postEntityRequest');
    cy.intercept('DELETE', '/api/bills/*').as('deleteEntityRequest');
  });

  /* Disabled due to incompatibility
  beforeEach(() => {
    // Simulate relationships api for better performance and reproducibility.
    cy.intercept('GET', '/api/payments', {
      statusCode: 200,
      body: [],
    });

    cy.intercept('GET', '/api/visits', {
      statusCode: 200,
      body: [visit],
    });

  });
   */

  afterEach(() => {
    if (bill) {
      cy.authenticatedRequest({
        method: 'DELETE',
        url: `/api/bills/${bill.id}`,
      }).then(() => {
        bill = undefined;
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
  });
   */

  it('Bills menu should load Bills page', () => {
    cy.visit('/');
    cy.clickOnEntityMenuItem('bill');
    cy.wait('@entitiesRequest').then(({ response }) => {
      if (response?.body.length === 0) {
        cy.get(entityTableSelector).should('not.exist');
      } else {
        cy.get(entityTableSelector).should('exist');
      }
    });
    cy.getEntityHeading('Bill').should('exist');
    cy.location('pathname').should('eq', billPageUrl);
  });

  describe('Bill page', () => {
    describe('create button click', () => {
      beforeEach(() => {
        cy.visit(billPageUrl);
        cy.wait('@entitiesRequest');
      });

      it('should load create Bill page', () => {
        cy.get(entityCreateButtonSelector).click();
        cy.location('pathname').should('eq', `${billPageUrl}/new`);
        cy.getEntityCreateUpdateHeading('Bill');
        cy.get(entityCreateSaveButtonSelector).should('exist');
        cy.get(entityCreateCancelButtonSelector).click();
        cy.wait('@entitiesRequest').then(({ response }) => {
          expect(response?.statusCode).to.equal(200);
        });
        cy.location('pathname').should('eq', billPageUrl);
      });
    });

    describe('with existing value', () => {
      /* Disabled due to incompatibility
      beforeEach(() => {
        cy.authenticatedRequest({
          method: 'POST',
          url: '/api/bills',
          body: {
            ...billSample,
            visit: visit,
          },
        }).then(({ body }) => {
          bill = body;

          cy.intercept(
            {
              method: 'GET',
              url: '/api/bills+(?*|)',
              times: 1,
            },
            {
              statusCode: 200,
              headers: {
                link: '<http://localhost/api/bills?page=0&size=20>; rel="last",<http://localhost/api/bills?page=0&size=20>; rel="first"',
              },
              body: [bill],
            }
          ).as('entitiesRequestInternal');
        });

        cy.visit(billPageUrl);

        cy.wait('@entitiesRequestInternal');
      });
       */

      beforeEach(function () {
        cy.visit(billPageUrl);

        cy.wait('@entitiesRequest').then(({ response }) => {
          if (response?.body.length === 0) {
            this.skip();
          }
        });
      });

      it('detail button click should load details Bill page', () => {
        cy.get(entityDetailsButtonSelector).first().click();
        cy.getEntityDetailsHeading('bill');
        cy.get(entityDetailsBackButtonSelector).click();
        cy.wait('@entitiesRequest').then(({ response }) => {
          expect(response?.statusCode).to.equal(200);
        });
        cy.location('pathname').should('eq', billPageUrl);
      });

      it('edit button click should load edit Bill page and go back', () => {
        cy.get(entityEditButtonSelector).first().click();
        cy.getEntityCreateUpdateHeading('Bill');
        cy.get(entityCreateSaveButtonSelector).should('exist');
        cy.get(entityCreateCancelButtonSelector).click();
        cy.wait('@entitiesRequest').then(({ response }) => {
          expect(response?.statusCode).to.equal(200);
        });
        cy.location('pathname').should('eq', billPageUrl);
      });

      it('edit button click should load edit Bill page and save', () => {
        cy.get(entityEditButtonSelector).first().click();
        cy.getEntityCreateUpdateHeading('Bill');
        cy.get(entityCreateSaveButtonSelector).click();
        cy.wait('@entitiesRequest').then(({ response }) => {
          expect(response?.statusCode).to.equal(200);
        });
        cy.location('pathname').should('eq', billPageUrl);
      });

      // Reason: cannot create a required entity with relationship with required relationships.
      it.skip('last delete button click should delete instance of Bill', () => {
        cy.intercept('GET', '/api/bills/*').as('dialogDeleteRequest');
        cy.get(entityDeleteButtonSelector).last().click();
        cy.wait('@dialogDeleteRequest');
        cy.getEntityDeleteDialogHeading('bill').should('exist');
        cy.get(entityConfirmDeleteButtonSelector).click();
        cy.wait('@deleteEntityRequest').then(({ response }) => {
          expect(response?.statusCode).to.equal(204);
        });
        cy.wait('@entitiesRequest').then(({ response }) => {
          expect(response?.statusCode).to.equal(200);
        });
        cy.location('pathname').should('eq', billPageUrl);

        bill = undefined;
      });
    });
  });

  describe('new Bill page', () => {
    beforeEach(() => {
      cy.visit(billPageUrl);
      cy.get(entityCreateButtonSelector).click();
      cy.getEntityCreateUpdateHeading('Bill');
    });

    // Reason: cannot create a required entity with relationship with required relationships.
    it.skip('should create an instance of Bill', () => {
      cy.get(`[data-cy="totalAmount"]`).type('4626.06');
      cy.get(`[data-cy="totalAmount"]`).should('have.value', '4626.06');

      cy.get(`[data-cy="status"]`).select('UNPAID');

      cy.get(`[data-cy="paidAt"]`).type('2026-09-23T11:50');
      cy.get(`[data-cy="paidAt"]`).blur();
      cy.get(`[data-cy="paidAt"]`).should('have.value', '2026-09-23T11:50');

      cy.get(`[data-cy="visit"]`).select(1);

      cy.get(entityCreateSaveButtonSelector).click();

      cy.wait('@postEntityRequest').then(({ response }) => {
        expect(response?.statusCode).to.equal(201);
        bill = response.body;
      });
      cy.wait('@entitiesRequest').then(({ response }) => {
        expect(response?.statusCode).to.equal(200);
      });
      cy.location('pathname').should('eq', billPageUrl);
    });
  });
});
