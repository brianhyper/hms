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

describe('Drug e2e test', () => {
  const drugPageUrl = '/drug';
  let username: string;
  let password: string;
  const drugSample = {
    name: 'oh',
    unit: 'grouchy square red',
    currentStock: 28610,
    reservedStock: 17316,
    lowStockThreshold: 26713,
    price: 30396.17,
    active: false,
  };

  let drug;

  before(() => {
    cy.credentials().then(credentials => {
      ({ username, password } = credentials);
    });
  });

  beforeEach(() => {
    cy.login(username, password);
  });

  beforeEach(() => {
    cy.intercept('GET', '/api/drugs+(?*|)').as('entitiesRequest');
    cy.intercept('POST', '/api/drugs').as('postEntityRequest');
    cy.intercept('DELETE', '/api/drugs/*').as('deleteEntityRequest');
  });

  afterEach(() => {
    if (drug) {
      cy.authenticatedRequest({
        method: 'DELETE',
        url: `/api/drugs/${drug.id}`,
      }).then(() => {
        drug = undefined;
      });
    }
  });

  it('Drugs menu should load Drugs page', () => {
    cy.visit('/');
    cy.clickOnEntityMenuItem('drug');
    cy.wait('@entitiesRequest').then(({ response }) => {
      if (response?.body.length === 0) {
        cy.get(entityTableSelector).should('not.exist');
      } else {
        cy.get(entityTableSelector).should('exist');
      }
    });
    cy.getEntityHeading('Drug').should('exist');
    cy.location('pathname').should('eq', drugPageUrl);
  });

  describe('Drug page', () => {
    describe('create button click', () => {
      beforeEach(() => {
        cy.visit(drugPageUrl);
        cy.wait('@entitiesRequest');
      });

      it('should load create Drug page', () => {
        cy.get(entityCreateButtonSelector).click();
        cy.location('pathname').should('eq', `${drugPageUrl}/new`);
        cy.getEntityCreateUpdateHeading('Drug');
        cy.get(entityCreateSaveButtonSelector).should('exist');
        cy.get(entityCreateCancelButtonSelector).click();
        cy.wait('@entitiesRequest').then(({ response }) => {
          expect(response?.statusCode).to.equal(200);
        });
        cy.location('pathname').should('eq', drugPageUrl);
      });
    });

    describe('with existing value', () => {
      beforeEach(() => {
        cy.authenticatedRequest({
          method: 'POST',
          url: '/api/drugs',
          body: drugSample,
        }).then(({ body }) => {
          drug = body;

          cy.intercept(
            {
              method: 'GET',
              url: '/api/drugs+(?*|)',
              times: 1,
            },
            {
              statusCode: 200,
              headers: {
                link: '<http://localhost/api/drugs?page=0&size=20>; rel="last",<http://localhost/api/drugs?page=0&size=20>; rel="first"',
              },
              body: [drug],
            },
          ).as('entitiesRequestInternal');
        });

        cy.visit(drugPageUrl);

        cy.wait('@entitiesRequestInternal');
      });

      it('detail button click should load details Drug page', () => {
        cy.get(entityDetailsButtonSelector).first().click();
        cy.getEntityDetailsHeading('drug');
        cy.get(entityDetailsBackButtonSelector).click();
        cy.wait('@entitiesRequest').then(({ response }) => {
          expect(response?.statusCode).to.equal(200);
        });
        cy.location('pathname').should('eq', drugPageUrl);
      });

      it('edit button click should load edit Drug page and go back', () => {
        cy.get(entityEditButtonSelector).first().click();
        cy.getEntityCreateUpdateHeading('Drug');
        cy.get(entityCreateSaveButtonSelector).should('exist');
        cy.get(entityCreateCancelButtonSelector).click();
        cy.wait('@entitiesRequest').then(({ response }) => {
          expect(response?.statusCode).to.equal(200);
        });
        cy.location('pathname').should('eq', drugPageUrl);
      });

      it('edit button click should load edit Drug page and save', () => {
        cy.get(entityEditButtonSelector).first().click();
        cy.getEntityCreateUpdateHeading('Drug');
        cy.get(entityCreateSaveButtonSelector).click();
        cy.wait('@entitiesRequest').then(({ response }) => {
          expect(response?.statusCode).to.equal(200);
        });
        cy.location('pathname').should('eq', drugPageUrl);
      });

      it('last delete button click should delete instance of Drug', () => {
        cy.intercept('GET', '/api/drugs/*').as('dialogDeleteRequest');
        cy.get(entityDeleteButtonSelector).last().click();
        cy.wait('@dialogDeleteRequest');
        cy.getEntityDeleteDialogHeading('drug').should('exist');
        cy.get(entityConfirmDeleteButtonSelector).click();
        cy.wait('@deleteEntityRequest').then(({ response }) => {
          expect(response?.statusCode).to.equal(204);
        });
        cy.wait('@entitiesRequest').then(({ response }) => {
          expect(response?.statusCode).to.equal(200);
        });
        cy.location('pathname').should('eq', drugPageUrl);

        drug = undefined;
      });
    });
  });

  describe('new Drug page', () => {
    beforeEach(() => {
      cy.visit(drugPageUrl);
      cy.get(entityCreateButtonSelector).click();
      cy.getEntityCreateUpdateHeading('Drug');
    });

    it('should create an instance of Drug', () => {
      cy.get(`[data-cy="name"]`).type('ill-fated');
      cy.get(`[data-cy="name"]`).should('have.value', 'ill-fated');

      cy.get(`[data-cy="unit"]`).type('drat carelessly');
      cy.get(`[data-cy="unit"]`).should('have.value', 'drat carelessly');

      cy.get(`[data-cy="currentStock"]`).type('1156');
      cy.get(`[data-cy="currentStock"]`).should('have.value', '1156');

      cy.get(`[data-cy="reservedStock"]`).type('25419');
      cy.get(`[data-cy="reservedStock"]`).should('have.value', '25419');

      cy.get(`[data-cy="lowStockThreshold"]`).type('24946');
      cy.get(`[data-cy="lowStockThreshold"]`).should('have.value', '24946');

      cy.get(`[data-cy="price"]`).type('13273.2');
      cy.get(`[data-cy="price"]`).should('have.value', '13273.2');

      cy.get(`[data-cy="classification"]`).select('POM');

      cy.get(`[data-cy="active"]`).should('not.be.checked');
      cy.get(`[data-cy="active"]`).click();
      cy.get(`[data-cy="active"]`).should('be.checked');

      cy.get(entityCreateSaveButtonSelector).click();

      cy.wait('@postEntityRequest').then(({ response }) => {
        expect(response?.statusCode).to.equal(201);
        drug = response.body;
      });
      cy.wait('@entitiesRequest').then(({ response }) => {
        expect(response?.statusCode).to.equal(200);
      });
      cy.location('pathname').should('eq', drugPageUrl);
    });
  });
});
