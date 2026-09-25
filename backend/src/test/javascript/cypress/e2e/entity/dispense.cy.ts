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

describe('Dispense e2e test', () => {
  const dispensePageUrl = '/dispense';
  let username: string;
  let password: string;
  // const dispenseSample = {"dispensedAt":"2026-09-23T12:44:25.510Z"};

  let dispense;
  // let prescription;
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
      url: '/api/prescriptions',
      body: {"source":"INTERNAL","prescribingSource":"gadzooks heavily","status":"PENDING"},
    }).then(({ body }) => {
      prescription = body;
    });
    // create an instance at the required relationship entity:
    cy.authenticatedRequest({
      method: 'POST',
      url: '/api/users',
      body: {"login":"Bobbie1","firstName":"Jeannette","lastName":"Ankunding","email":"Lillian.Wuckert63@yahoo.com","imageUrl":"than whoa"},
    }).then(({ body }) => {
      user = body;
    });
  });
   */

  beforeEach(() => {
    cy.intercept('GET', '/api/dispenses+(?*|)').as('entitiesRequest');
    cy.intercept('POST', '/api/dispenses').as('postEntityRequest');
    cy.intercept('DELETE', '/api/dispenses/*').as('deleteEntityRequest');
  });

  /* Disabled due to incompatibility
  beforeEach(() => {
    // Simulate relationships api for better performance and reproducibility.
    cy.intercept('GET', '/api/prescriptions', {
      statusCode: 200,
      body: [prescription],
    });

    cy.intercept('GET', '/api/users', {
      statusCode: 200,
      body: [user],
    });

  });
   */

  afterEach(() => {
    if (dispense) {
      cy.authenticatedRequest({
        method: 'DELETE',
        url: `/api/dispenses/${dispense.id}`,
      }).then(() => {
        dispense = undefined;
      });
    }
  });

  /* Disabled due to incompatibility
  afterEach(() => {
    if (prescription) {
      cy.authenticatedRequest({
        method: 'DELETE',
        url: `/api/prescriptions/${prescription.id}`,
      }).then(() => {
        prescription = undefined;
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

  it('Dispenses menu should load Dispenses page', () => {
    cy.visit('/');
    cy.clickOnEntityMenuItem('dispense');
    cy.wait('@entitiesRequest').then(({ response }) => {
      if (response?.body.length === 0) {
        cy.get(entityTableSelector).should('not.exist');
      } else {
        cy.get(entityTableSelector).should('exist');
      }
    });
    cy.getEntityHeading('Dispense').should('exist');
    cy.location('pathname').should('eq', dispensePageUrl);
  });

  describe('Dispense page', () => {
    describe('create button click', () => {
      beforeEach(() => {
        cy.visit(dispensePageUrl);
        cy.wait('@entitiesRequest');
      });

      it('should load create Dispense page', () => {
        cy.get(entityCreateButtonSelector).click();
        cy.location('pathname').should('eq', `${dispensePageUrl}/new`);
        cy.getEntityCreateUpdateHeading('Dispense');
        cy.get(entityCreateSaveButtonSelector).should('exist');
        cy.get(entityCreateCancelButtonSelector).click();
        cy.wait('@entitiesRequest').then(({ response }) => {
          expect(response?.statusCode).to.equal(200);
        });
        cy.location('pathname').should('eq', dispensePageUrl);
      });
    });

    describe('with existing value', () => {
      /* Disabled due to incompatibility
      beforeEach(() => {
        cy.authenticatedRequest({
          method: 'POST',
          url: '/api/dispenses',
          body: {
            ...dispenseSample,
            prescription: prescription,
            recordedBy: user,
          },
        }).then(({ body }) => {
          dispense = body;

          cy.intercept(
            {
              method: 'GET',
              url: '/api/dispenses+(?*|)',
              times: 1,
            },
            {
              statusCode: 200,
              body: [dispense],
            }
          ).as('entitiesRequestInternal');
        });

        cy.visit(dispensePageUrl);

        cy.wait('@entitiesRequestInternal');
      });
       */

      beforeEach(function () {
        cy.visit(dispensePageUrl);

        cy.wait('@entitiesRequest').then(({ response }) => {
          if (response?.body.length === 0) {
            this.skip();
          }
        });
      });

      it('detail button click should load details Dispense page', () => {
        cy.get(entityDetailsButtonSelector).first().click();
        cy.getEntityDetailsHeading('dispense');
        cy.get(entityDetailsBackButtonSelector).click();
        cy.wait('@entitiesRequest').then(({ response }) => {
          expect(response?.statusCode).to.equal(200);
        });
        cy.location('pathname').should('eq', dispensePageUrl);
      });

      it('edit button click should load edit Dispense page and go back', () => {
        cy.get(entityEditButtonSelector).first().click();
        cy.getEntityCreateUpdateHeading('Dispense');
        cy.get(entityCreateSaveButtonSelector).should('exist');
        cy.get(entityCreateCancelButtonSelector).click();
        cy.wait('@entitiesRequest').then(({ response }) => {
          expect(response?.statusCode).to.equal(200);
        });
        cy.location('pathname').should('eq', dispensePageUrl);
      });

      it('edit button click should load edit Dispense page and save', () => {
        cy.get(entityEditButtonSelector).first().click();
        cy.getEntityCreateUpdateHeading('Dispense');
        cy.get(entityCreateSaveButtonSelector).click();
        cy.wait('@entitiesRequest').then(({ response }) => {
          expect(response?.statusCode).to.equal(200);
        });
        cy.location('pathname').should('eq', dispensePageUrl);
      });

      // Reason: cannot create a required entity with relationship with required relationships.
      it.skip('last delete button click should delete instance of Dispense', () => {
        cy.intercept('GET', '/api/dispenses/*').as('dialogDeleteRequest');
        cy.get(entityDeleteButtonSelector).last().click();
        cy.wait('@dialogDeleteRequest');
        cy.getEntityDeleteDialogHeading('dispense').should('exist');
        cy.get(entityConfirmDeleteButtonSelector).click();
        cy.wait('@deleteEntityRequest').then(({ response }) => {
          expect(response?.statusCode).to.equal(204);
        });
        cy.wait('@entitiesRequest').then(({ response }) => {
          expect(response?.statusCode).to.equal(200);
        });
        cy.location('pathname').should('eq', dispensePageUrl);

        dispense = undefined;
      });
    });
  });

  describe('new Dispense page', () => {
    beforeEach(() => {
      cy.visit(dispensePageUrl);
      cy.get(entityCreateButtonSelector).click();
      cy.getEntityCreateUpdateHeading('Dispense');
    });

    // Reason: cannot create a required entity with relationship with required relationships.
    it.skip('should create an instance of Dispense', () => {
      cy.get(`[data-cy="dispensedAt"]`).type('2026-09-24T04:55');
      cy.get(`[data-cy="dispensedAt"]`).blur();
      cy.get(`[data-cy="dispensedAt"]`).should('have.value', '2026-09-24T04:55');

      cy.get(`[data-cy="note"]`).type('slipper');
      cy.get(`[data-cy="note"]`).should('have.value', 'slipper');

      cy.get(`[data-cy="prescription"]`).select(1);
      cy.get(`[data-cy="recordedBy"]`).select(1);

      cy.get(entityCreateSaveButtonSelector).click();

      cy.wait('@postEntityRequest').then(({ response }) => {
        expect(response?.statusCode).to.equal(201);
        dispense = response.body;
      });
      cy.wait('@entitiesRequest').then(({ response }) => {
        expect(response?.statusCode).to.equal(200);
      });
      cy.location('pathname').should('eq', dispensePageUrl);
    });
  });
});
