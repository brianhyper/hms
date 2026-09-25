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

describe('Result e2e test', () => {
  const resultPageUrl = '/result';
  let username: string;
  let password: string;
  // const resultSample = {"resultValue":"drowse or","enteredAt":"2026-09-23T10:38:23.742Z"};

  let result;
  // let user;
  // let diagnosticOrder;

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
      url: '/api/users',
      body: {"login":"Everett.White","firstName":"Eddie","lastName":"Braun","email":"Mafalda_Ward@yahoo.com","imageUrl":"upliftingly brush"},
    }).then(({ body }) => {
      user = body;
    });
    // create an instance at the required relationship entity:
    cy.authenticatedRequest({
      method: 'POST',
      url: '/api/diagnostic-orders',
      body: {"type":"LAB","testName":"sophisticated each","status":"IN_PROGRESS","notes":"black-and-white snarling boldly","orderedAt":"2026-09-23T22:25:49.541Z"},
    }).then(({ body }) => {
      diagnosticOrder = body;
    });
  });
   */

  beforeEach(() => {
    cy.intercept('GET', '/api/results+(?*|)').as('entitiesRequest');
    cy.intercept('POST', '/api/results').as('postEntityRequest');
    cy.intercept('DELETE', '/api/results/*').as('deleteEntityRequest');
  });

  /* Disabled due to incompatibility
  beforeEach(() => {
    // Simulate relationships api for better performance and reproducibility.
    cy.intercept('GET', '/api/users', {
      statusCode: 200,
      body: [user],
    });

    cy.intercept('GET', '/api/diagnostic-orders', {
      statusCode: 200,
      body: [diagnosticOrder],
    });

  });
   */

  afterEach(() => {
    if (result) {
      cy.authenticatedRequest({
        method: 'DELETE',
        url: `/api/results/${result.id}`,
      }).then(() => {
        result = undefined;
      });
    }
  });

  /* Disabled due to incompatibility
  afterEach(() => {
    if (user) {
      cy.authenticatedRequest({
        method: 'DELETE',
        url: `/api/users/${user.id}`,
      }).then(() => {
        user = undefined;
      });
    }
    if (diagnosticOrder) {
      cy.authenticatedRequest({
        method: 'DELETE',
        url: `/api/diagnostic-orders/${diagnosticOrder.id}`,
      }).then(() => {
        diagnosticOrder = undefined;
      });
    }
  });
   */

  it('Results menu should load Results page', () => {
    cy.visit('/');
    cy.clickOnEntityMenuItem('result');
    cy.wait('@entitiesRequest').then(({ response }) => {
      if (response?.body.length === 0) {
        cy.get(entityTableSelector).should('not.exist');
      } else {
        cy.get(entityTableSelector).should('exist');
      }
    });
    cy.getEntityHeading('Result').should('exist');
    cy.location('pathname').should('eq', resultPageUrl);
  });

  describe('Result page', () => {
    describe('create button click', () => {
      beforeEach(() => {
        cy.visit(resultPageUrl);
        cy.wait('@entitiesRequest');
      });

      it('should load create Result page', () => {
        cy.get(entityCreateButtonSelector).click();
        cy.location('pathname').should('eq', `${resultPageUrl}/new`);
        cy.getEntityCreateUpdateHeading('Result');
        cy.get(entityCreateSaveButtonSelector).should('exist');
        cy.get(entityCreateCancelButtonSelector).click();
        cy.wait('@entitiesRequest').then(({ response }) => {
          expect(response?.statusCode).to.equal(200);
        });
        cy.location('pathname').should('eq', resultPageUrl);
      });
    });

    describe('with existing value', () => {
      /* Disabled due to incompatibility
      beforeEach(() => {
        cy.authenticatedRequest({
          method: 'POST',
          url: '/api/results',
          body: {
            ...resultSample,
            enteredBy: user,
            order: diagnosticOrder,
          },
        }).then(({ body }) => {
          result = body;

          cy.intercept(
            {
              method: 'GET',
              url: '/api/results+(?*|)',
              times: 1,
            },
            {
              statusCode: 200,
              body: [result],
            }
          ).as('entitiesRequestInternal');
        });

        cy.visit(resultPageUrl);

        cy.wait('@entitiesRequestInternal');
      });
       */

      beforeEach(function () {
        cy.visit(resultPageUrl);

        cy.wait('@entitiesRequest').then(({ response }) => {
          if (response?.body.length === 0) {
            this.skip();
          }
        });
      });

      it('detail button click should load details Result page', () => {
        cy.get(entityDetailsButtonSelector).first().click();
        cy.getEntityDetailsHeading('result');
        cy.get(entityDetailsBackButtonSelector).click();
        cy.wait('@entitiesRequest').then(({ response }) => {
          expect(response?.statusCode).to.equal(200);
        });
        cy.location('pathname').should('eq', resultPageUrl);
      });

      it('edit button click should load edit Result page and go back', () => {
        cy.get(entityEditButtonSelector).first().click();
        cy.getEntityCreateUpdateHeading('Result');
        cy.get(entityCreateSaveButtonSelector).should('exist');
        cy.get(entityCreateCancelButtonSelector).click();
        cy.wait('@entitiesRequest').then(({ response }) => {
          expect(response?.statusCode).to.equal(200);
        });
        cy.location('pathname').should('eq', resultPageUrl);
      });

      it('edit button click should load edit Result page and save', () => {
        cy.get(entityEditButtonSelector).first().click();
        cy.getEntityCreateUpdateHeading('Result');
        cy.get(entityCreateSaveButtonSelector).click();
        cy.wait('@entitiesRequest').then(({ response }) => {
          expect(response?.statusCode).to.equal(200);
        });
        cy.location('pathname').should('eq', resultPageUrl);
      });

      // Reason: cannot create a required entity with relationship with required relationships.
      it.skip('last delete button click should delete instance of Result', () => {
        cy.intercept('GET', '/api/results/*').as('dialogDeleteRequest');
        cy.get(entityDeleteButtonSelector).last().click();
        cy.wait('@dialogDeleteRequest');
        cy.getEntityDeleteDialogHeading('result').should('exist');
        cy.get(entityConfirmDeleteButtonSelector).click();
        cy.wait('@deleteEntityRequest').then(({ response }) => {
          expect(response?.statusCode).to.equal(204);
        });
        cy.wait('@entitiesRequest').then(({ response }) => {
          expect(response?.statusCode).to.equal(200);
        });
        cy.location('pathname').should('eq', resultPageUrl);

        result = undefined;
      });
    });
  });

  describe('new Result page', () => {
    beforeEach(() => {
      cy.visit(resultPageUrl);
      cy.get(entityCreateButtonSelector).click();
      cy.getEntityCreateUpdateHeading('Result');
    });

    // Reason: cannot create a required entity with relationship with required relationships.
    it.skip('should create an instance of Result', () => {
      cy.get(`[data-cy="resultValue"]`).type('even zesty unless');
      cy.get(`[data-cy="resultValue"]`).should('have.value', 'even zesty unless');

      cy.get(`[data-cy="notes"]`).type('boohoo');
      cy.get(`[data-cy="notes"]`).should('have.value', 'boohoo');

      cy.get(`[data-cy="enteredAt"]`).type('2026-09-23T21:36');
      cy.get(`[data-cy="enteredAt"]`).blur();
      cy.get(`[data-cy="enteredAt"]`).should('have.value', '2026-09-23T21:36');

      cy.get(`[data-cy="imageReference"]`).type('knuckle operating until');
      cy.get(`[data-cy="imageReference"]`).should('have.value', 'knuckle operating until');

      cy.get(`[data-cy="enteredBy"]`).select(1);
      cy.get(`[data-cy="order"]`).select(1);

      cy.get(entityCreateSaveButtonSelector).click();

      cy.wait('@postEntityRequest').then(({ response }) => {
        expect(response?.statusCode).to.equal(201);
        result = response.body;
      });
      cy.wait('@entitiesRequest').then(({ response }) => {
        expect(response?.statusCode).to.equal(200);
      });
      cy.location('pathname').should('eq', resultPageUrl);
    });
  });
});
