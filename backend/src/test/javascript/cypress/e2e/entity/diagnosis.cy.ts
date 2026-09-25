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

describe('Diagnosis e2e test', () => {
  const diagnosisPageUrl = '/diagnosis';
  let username: string;
  let password: string;
  const diagnosisSample = { code: 'gladly as', name: 'joint', active: true };

  let diagnosis;

  before(() => {
    cy.credentials().then(credentials => {
      ({ username, password } = credentials);
    });
  });

  beforeEach(() => {
    cy.login(username, password);
  });

  beforeEach(() => {
    cy.intercept('GET', '/api/diagnoses+(?*|)').as('entitiesRequest');
    cy.intercept('POST', '/api/diagnoses').as('postEntityRequest');
    cy.intercept('DELETE', '/api/diagnoses/*').as('deleteEntityRequest');
  });

  afterEach(() => {
    if (diagnosis) {
      cy.authenticatedRequest({
        method: 'DELETE',
        url: `/api/diagnoses/${diagnosis.id}`,
      }).then(() => {
        diagnosis = undefined;
      });
    }
  });

  it('Diagnoses menu should load Diagnoses page', () => {
    cy.visit('/');
    cy.clickOnEntityMenuItem('diagnosis');
    cy.wait('@entitiesRequest').then(({ response }) => {
      if (response?.body.length === 0) {
        cy.get(entityTableSelector).should('not.exist');
      } else {
        cy.get(entityTableSelector).should('exist');
      }
    });
    cy.getEntityHeading('Diagnosis').should('exist');
    cy.location('pathname').should('eq', diagnosisPageUrl);
  });

  describe('Diagnosis page', () => {
    describe('create button click', () => {
      beforeEach(() => {
        cy.visit(diagnosisPageUrl);
        cy.wait('@entitiesRequest');
      });

      it('should load create Diagnosis page', () => {
        cy.get(entityCreateButtonSelector).click();
        cy.location('pathname').should('eq', `${diagnosisPageUrl}/new`);
        cy.getEntityCreateUpdateHeading('Diagnosis');
        cy.get(entityCreateSaveButtonSelector).should('exist');
        cy.get(entityCreateCancelButtonSelector).click();
        cy.wait('@entitiesRequest').then(({ response }) => {
          expect(response?.statusCode).to.equal(200);
        });
        cy.location('pathname').should('eq', diagnosisPageUrl);
      });
    });

    describe('with existing value', () => {
      beforeEach(() => {
        cy.authenticatedRequest({
          method: 'POST',
          url: '/api/diagnoses',
          body: diagnosisSample,
        }).then(({ body }) => {
          diagnosis = body;

          cy.intercept(
            {
              method: 'GET',
              url: '/api/diagnoses+(?*|)',
              times: 1,
            },
            {
              statusCode: 200,
              body: [diagnosis],
            },
          ).as('entitiesRequestInternal');
        });

        cy.visit(diagnosisPageUrl);

        cy.wait('@entitiesRequestInternal');
      });

      it('detail button click should load details Diagnosis page', () => {
        cy.get(entityDetailsButtonSelector).first().click();
        cy.getEntityDetailsHeading('diagnosis');
        cy.get(entityDetailsBackButtonSelector).click();
        cy.wait('@entitiesRequest').then(({ response }) => {
          expect(response?.statusCode).to.equal(200);
        });
        cy.location('pathname').should('eq', diagnosisPageUrl);
      });

      it('edit button click should load edit Diagnosis page and go back', () => {
        cy.get(entityEditButtonSelector).first().click();
        cy.getEntityCreateUpdateHeading('Diagnosis');
        cy.get(entityCreateSaveButtonSelector).should('exist');
        cy.get(entityCreateCancelButtonSelector).click();
        cy.wait('@entitiesRequest').then(({ response }) => {
          expect(response?.statusCode).to.equal(200);
        });
        cy.location('pathname').should('eq', diagnosisPageUrl);
      });

      it('edit button click should load edit Diagnosis page and save', () => {
        cy.get(entityEditButtonSelector).first().click();
        cy.getEntityCreateUpdateHeading('Diagnosis');
        cy.get(entityCreateSaveButtonSelector).click();
        cy.wait('@entitiesRequest').then(({ response }) => {
          expect(response?.statusCode).to.equal(200);
        });
        cy.location('pathname').should('eq', diagnosisPageUrl);
      });

      it('last delete button click should delete instance of Diagnosis', () => {
        cy.intercept('GET', '/api/diagnoses/*').as('dialogDeleteRequest');
        cy.get(entityDeleteButtonSelector).last().click();
        cy.wait('@dialogDeleteRequest');
        cy.getEntityDeleteDialogHeading('diagnosis').should('exist');
        cy.get(entityConfirmDeleteButtonSelector).click();
        cy.wait('@deleteEntityRequest').then(({ response }) => {
          expect(response?.statusCode).to.equal(204);
        });
        cy.wait('@entitiesRequest').then(({ response }) => {
          expect(response?.statusCode).to.equal(200);
        });
        cy.location('pathname').should('eq', diagnosisPageUrl);

        diagnosis = undefined;
      });
    });
  });

  describe('new Diagnosis page', () => {
    beforeEach(() => {
      cy.visit(diagnosisPageUrl);
      cy.get(entityCreateButtonSelector).click();
      cy.getEntityCreateUpdateHeading('Diagnosis');
    });

    it('should create an instance of Diagnosis', () => {
      cy.get(`[data-cy="code"]`).type('lest');
      cy.get(`[data-cy="code"]`).should('have.value', 'lest');

      cy.get(`[data-cy="name"]`).type('competent weary');
      cy.get(`[data-cy="name"]`).should('have.value', 'competent weary');

      cy.get(`[data-cy="active"]`).should('not.be.checked');
      cy.get(`[data-cy="active"]`).click();
      cy.get(`[data-cy="active"]`).should('be.checked');

      cy.get(entityCreateSaveButtonSelector).click();

      cy.wait('@postEntityRequest').then(({ response }) => {
        expect(response?.statusCode).to.equal(201);
        diagnosis = response.body;
      });
      cy.wait('@entitiesRequest').then(({ response }) => {
        expect(response?.statusCode).to.equal(200);
      });
      cy.location('pathname').should('eq', diagnosisPageUrl);
    });
  });
});
