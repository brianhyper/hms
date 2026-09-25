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

describe('LabTest e2e test', () => {
  const labTestPageUrl = '/lab-test';
  let username: string;
  let password: string;
  const labTestSample = { name: 'same abaft yet', price: 29321.62, active: false };

  let labTest;

  before(() => {
    cy.credentials().then(credentials => {
      ({ username, password } = credentials);
    });
  });

  beforeEach(() => {
    cy.login(username, password);
  });

  beforeEach(() => {
    cy.intercept('GET', '/api/lab-tests+(?*|)').as('entitiesRequest');
    cy.intercept('POST', '/api/lab-tests').as('postEntityRequest');
    cy.intercept('DELETE', '/api/lab-tests/*').as('deleteEntityRequest');
  });

  afterEach(() => {
    if (labTest) {
      cy.authenticatedRequest({
        method: 'DELETE',
        url: `/api/lab-tests/${labTest.id}`,
      }).then(() => {
        labTest = undefined;
      });
    }
  });

  it('LabTests menu should load LabTests page', () => {
    cy.visit('/');
    cy.clickOnEntityMenuItem('lab-test');
    cy.wait('@entitiesRequest').then(({ response }) => {
      if (response?.body.length === 0) {
        cy.get(entityTableSelector).should('not.exist');
      } else {
        cy.get(entityTableSelector).should('exist');
      }
    });
    cy.getEntityHeading('LabTest').should('exist');
    cy.location('pathname').should('eq', labTestPageUrl);
  });

  describe('LabTest page', () => {
    describe('create button click', () => {
      beforeEach(() => {
        cy.visit(labTestPageUrl);
        cy.wait('@entitiesRequest');
      });

      it('should load create LabTest page', () => {
        cy.get(entityCreateButtonSelector).click();
        cy.location('pathname').should('eq', `${labTestPageUrl}/new`);
        cy.getEntityCreateUpdateHeading('LabTest');
        cy.get(entityCreateSaveButtonSelector).should('exist');
        cy.get(entityCreateCancelButtonSelector).click();
        cy.wait('@entitiesRequest').then(({ response }) => {
          expect(response?.statusCode).to.equal(200);
        });
        cy.location('pathname').should('eq', labTestPageUrl);
      });
    });

    describe('with existing value', () => {
      beforeEach(() => {
        cy.authenticatedRequest({
          method: 'POST',
          url: '/api/lab-tests',
          body: labTestSample,
        }).then(({ body }) => {
          labTest = body;

          cy.intercept(
            {
              method: 'GET',
              url: '/api/lab-tests+(?*|)',
              times: 1,
            },
            {
              statusCode: 200,
              body: [labTest],
            },
          ).as('entitiesRequestInternal');
        });

        cy.visit(labTestPageUrl);

        cy.wait('@entitiesRequestInternal');
      });

      it('detail button click should load details LabTest page', () => {
        cy.get(entityDetailsButtonSelector).first().click();
        cy.getEntityDetailsHeading('labTest');
        cy.get(entityDetailsBackButtonSelector).click();
        cy.wait('@entitiesRequest').then(({ response }) => {
          expect(response?.statusCode).to.equal(200);
        });
        cy.location('pathname').should('eq', labTestPageUrl);
      });

      it('edit button click should load edit LabTest page and go back', () => {
        cy.get(entityEditButtonSelector).first().click();
        cy.getEntityCreateUpdateHeading('LabTest');
        cy.get(entityCreateSaveButtonSelector).should('exist');
        cy.get(entityCreateCancelButtonSelector).click();
        cy.wait('@entitiesRequest').then(({ response }) => {
          expect(response?.statusCode).to.equal(200);
        });
        cy.location('pathname').should('eq', labTestPageUrl);
      });

      it('edit button click should load edit LabTest page and save', () => {
        cy.get(entityEditButtonSelector).first().click();
        cy.getEntityCreateUpdateHeading('LabTest');
        cy.get(entityCreateSaveButtonSelector).click();
        cy.wait('@entitiesRequest').then(({ response }) => {
          expect(response?.statusCode).to.equal(200);
        });
        cy.location('pathname').should('eq', labTestPageUrl);
      });

      it('last delete button click should delete instance of LabTest', () => {
        cy.intercept('GET', '/api/lab-tests/*').as('dialogDeleteRequest');
        cy.get(entityDeleteButtonSelector).last().click();
        cy.wait('@dialogDeleteRequest');
        cy.getEntityDeleteDialogHeading('labTest').should('exist');
        cy.get(entityConfirmDeleteButtonSelector).click();
        cy.wait('@deleteEntityRequest').then(({ response }) => {
          expect(response?.statusCode).to.equal(204);
        });
        cy.wait('@entitiesRequest').then(({ response }) => {
          expect(response?.statusCode).to.equal(200);
        });
        cy.location('pathname').should('eq', labTestPageUrl);

        labTest = undefined;
      });
    });
  });

  describe('new LabTest page', () => {
    beforeEach(() => {
      cy.visit(labTestPageUrl);
      cy.get(entityCreateButtonSelector).click();
      cy.getEntityCreateUpdateHeading('LabTest');
    });

    it('should create an instance of LabTest', () => {
      cy.get(`[data-cy="name"]`).type('mathematics enlightened amongst');
      cy.get(`[data-cy="name"]`).should('have.value', 'mathematics enlightened amongst');

      cy.get(`[data-cy="price"]`).type('28070.59');
      cy.get(`[data-cy="price"]`).should('have.value', '28070.59');

      cy.get(`[data-cy="specimenType"]`).type('amidst');
      cy.get(`[data-cy="specimenType"]`).should('have.value', 'amidst');

      cy.get(`[data-cy="turnaroundTimeMinutes"]`).type('5809');
      cy.get(`[data-cy="turnaroundTimeMinutes"]`).should('have.value', '5809');

      cy.get(`[data-cy="active"]`).should('not.be.checked');
      cy.get(`[data-cy="active"]`).click();
      cy.get(`[data-cy="active"]`).should('be.checked');

      cy.get(entityCreateSaveButtonSelector).click();

      cy.wait('@postEntityRequest').then(({ response }) => {
        expect(response?.statusCode).to.equal(201);
        labTest = response.body;
      });
      cy.wait('@entitiesRequest').then(({ response }) => {
        expect(response?.statusCode).to.equal(200);
      });
      cy.location('pathname').should('eq', labTestPageUrl);
    });
  });
});
