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

describe('DispenseLine e2e test', () => {
  const dispenseLinePageUrl = '/dispense-line';
  let username: string;
  let password: string;
  // const dispenseLineSample = {"quantity":12613};

  let dispenseLine;
  // let dispense;
  // let prescriptionLine;
  // let drug;

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
      url: '/api/dispenses',
      body: {"dispensedAt":"2026-09-23T12:47:57.569Z","note":"inasmuch astride eek"},
    }).then(({ body }) => {
      dispense = body;
    });
    // create an instance at the required relationship entity:
    cy.authenticatedRequest({
      method: 'POST',
      url: '/api/prescription-lines',
      body: {"dosage":"bungalow yet","duration":"usefully yuck","quantity":2415},
    }).then(({ body }) => {
      prescriptionLine = body;
    });
    // create an instance at the required relationship entity:
    cy.authenticatedRequest({
      method: 'POST',
      url: '/api/drugs',
      body: {"name":"potable","unit":"cantaloupe instantly beautifully","currentStock":10614,"reservedStock":30162,"lowStockThreshold":30264,"price":11342.57,"classification":"POM","active":true},
    }).then(({ body }) => {
      drug = body;
    });
  });
   */

  beforeEach(() => {
    cy.intercept('GET', '/api/dispense-lines+(?*|)').as('entitiesRequest');
    cy.intercept('POST', '/api/dispense-lines').as('postEntityRequest');
    cy.intercept('DELETE', '/api/dispense-lines/*').as('deleteEntityRequest');
  });

  /* Disabled due to incompatibility
  beforeEach(() => {
    // Simulate relationships api for better performance and reproducibility.
    cy.intercept('GET', '/api/dispenses', {
      statusCode: 200,
      body: [dispense],
    });

    cy.intercept('GET', '/api/prescription-lines', {
      statusCode: 200,
      body: [prescriptionLine],
    });

    cy.intercept('GET', '/api/drugs', {
      statusCode: 200,
      body: [drug],
    });

  });
   */

  afterEach(() => {
    if (dispenseLine) {
      cy.authenticatedRequest({
        method: 'DELETE',
        url: `/api/dispense-lines/${dispenseLine.id}`,
      }).then(() => {
        dispenseLine = undefined;
      });
    }
  });

  /* Disabled due to incompatibility
  afterEach(() => {
    if (dispense) {
      cy.authenticatedRequest({
        method: 'DELETE',
        url: `/api/dispenses/${dispense.id}`,
      }).then(() => {
        dispense = undefined;
      });
    }
    if (prescriptionLine) {
      cy.authenticatedRequest({
        method: 'DELETE',
        url: `/api/prescription-lines/${prescriptionLine.id}`,
      }).then(() => {
        prescriptionLine = undefined;
      });
    }
    if (drug) {
      cy.authenticatedRequest({
        method: 'DELETE',
        url: `/api/drugs/${drug.id}`,
      }).then(() => {
        drug = undefined;
      });
    }
  });
   */

  it('DispenseLines menu should load DispenseLines page', () => {
    cy.visit('/');
    cy.clickOnEntityMenuItem('dispense-line');
    cy.wait('@entitiesRequest').then(({ response }) => {
      if (response?.body.length === 0) {
        cy.get(entityTableSelector).should('not.exist');
      } else {
        cy.get(entityTableSelector).should('exist');
      }
    });
    cy.getEntityHeading('DispenseLine').should('exist');
    cy.location('pathname').should('eq', dispenseLinePageUrl);
  });

  describe('DispenseLine page', () => {
    describe('create button click', () => {
      beforeEach(() => {
        cy.visit(dispenseLinePageUrl);
        cy.wait('@entitiesRequest');
      });

      it('should load create DispenseLine page', () => {
        cy.get(entityCreateButtonSelector).click();
        cy.location('pathname').should('eq', `${dispenseLinePageUrl}/new`);
        cy.getEntityCreateUpdateHeading('DispenseLine');
        cy.get(entityCreateSaveButtonSelector).should('exist');
        cy.get(entityCreateCancelButtonSelector).click();
        cy.wait('@entitiesRequest').then(({ response }) => {
          expect(response?.statusCode).to.equal(200);
        });
        cy.location('pathname').should('eq', dispenseLinePageUrl);
      });
    });

    describe('with existing value', () => {
      /* Disabled due to incompatibility
      beforeEach(() => {
        cy.authenticatedRequest({
          method: 'POST',
          url: '/api/dispense-lines',
          body: {
            ...dispenseLineSample,
            dispense: dispense,
            prescriptionLine: prescriptionLine,
            drug: drug,
          },
        }).then(({ body }) => {
          dispenseLine = body;

          cy.intercept(
            {
              method: 'GET',
              url: '/api/dispense-lines+(?*|)',
              times: 1,
            },
            {
              statusCode: 200,
              body: [dispenseLine],
            }
          ).as('entitiesRequestInternal');
        });

        cy.visit(dispenseLinePageUrl);

        cy.wait('@entitiesRequestInternal');
      });
       */

      beforeEach(function () {
        cy.visit(dispenseLinePageUrl);

        cy.wait('@entitiesRequest').then(({ response }) => {
          if (response?.body.length === 0) {
            this.skip();
          }
        });
      });

      it('detail button click should load details DispenseLine page', () => {
        cy.get(entityDetailsButtonSelector).first().click();
        cy.getEntityDetailsHeading('dispenseLine');
        cy.get(entityDetailsBackButtonSelector).click();
        cy.wait('@entitiesRequest').then(({ response }) => {
          expect(response?.statusCode).to.equal(200);
        });
        cy.location('pathname').should('eq', dispenseLinePageUrl);
      });

      it('edit button click should load edit DispenseLine page and go back', () => {
        cy.get(entityEditButtonSelector).first().click();
        cy.getEntityCreateUpdateHeading('DispenseLine');
        cy.get(entityCreateSaveButtonSelector).should('exist');
        cy.get(entityCreateCancelButtonSelector).click();
        cy.wait('@entitiesRequest').then(({ response }) => {
          expect(response?.statusCode).to.equal(200);
        });
        cy.location('pathname').should('eq', dispenseLinePageUrl);
      });

      it('edit button click should load edit DispenseLine page and save', () => {
        cy.get(entityEditButtonSelector).first().click();
        cy.getEntityCreateUpdateHeading('DispenseLine');
        cy.get(entityCreateSaveButtonSelector).click();
        cy.wait('@entitiesRequest').then(({ response }) => {
          expect(response?.statusCode).to.equal(200);
        });
        cy.location('pathname').should('eq', dispenseLinePageUrl);
      });

      // Reason: cannot create a required entity with relationship with required relationships.
      it.skip('last delete button click should delete instance of DispenseLine', () => {
        cy.intercept('GET', '/api/dispense-lines/*').as('dialogDeleteRequest');
        cy.get(entityDeleteButtonSelector).last().click();
        cy.wait('@dialogDeleteRequest');
        cy.getEntityDeleteDialogHeading('dispenseLine').should('exist');
        cy.get(entityConfirmDeleteButtonSelector).click();
        cy.wait('@deleteEntityRequest').then(({ response }) => {
          expect(response?.statusCode).to.equal(204);
        });
        cy.wait('@entitiesRequest').then(({ response }) => {
          expect(response?.statusCode).to.equal(200);
        });
        cy.location('pathname').should('eq', dispenseLinePageUrl);

        dispenseLine = undefined;
      });
    });
  });

  describe('new DispenseLine page', () => {
    beforeEach(() => {
      cy.visit(dispenseLinePageUrl);
      cy.get(entityCreateButtonSelector).click();
      cy.getEntityCreateUpdateHeading('DispenseLine');
    });

    // Reason: cannot create a required entity with relationship with required relationships.
    it.skip('should create an instance of DispenseLine', () => {
      cy.get(`[data-cy="quantity"]`).type('11677');
      cy.get(`[data-cy="quantity"]`).should('have.value', '11677');

      cy.get(`[data-cy="substitutionReason"]`).type('react whispered');
      cy.get(`[data-cy="substitutionReason"]`).should('have.value', 'react whispered');

      cy.get(`[data-cy="dispense"]`).select(1);
      cy.get(`[data-cy="prescriptionLine"]`).select(1);
      cy.get(`[data-cy="drug"]`).select(1);

      cy.get(entityCreateSaveButtonSelector).click();

      cy.wait('@postEntityRequest').then(({ response }) => {
        expect(response?.statusCode).to.equal(201);
        dispenseLine = response.body;
      });
      cy.wait('@entitiesRequest').then(({ response }) => {
        expect(response?.statusCode).to.equal(200);
      });
      cy.location('pathname').should('eq', dispenseLinePageUrl);
    });
  });
});
