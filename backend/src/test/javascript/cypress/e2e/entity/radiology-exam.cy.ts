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

describe('RadiologyExam e2e test', () => {
  const radiologyExamPageUrl = '/radiology-exam';
  let username: string;
  let password: string;
  const radiologyExamSample = { name: 'oof meanwhile', price: 20111.22, active: false };

  let radiologyExam;

  before(() => {
    cy.credentials().then(credentials => {
      ({ username, password } = credentials);
    });
  });

  beforeEach(() => {
    cy.login(username, password);
  });

  beforeEach(() => {
    cy.intercept('GET', '/api/radiology-exams+(?*|)').as('entitiesRequest');
    cy.intercept('POST', '/api/radiology-exams').as('postEntityRequest');
    cy.intercept('DELETE', '/api/radiology-exams/*').as('deleteEntityRequest');
  });

  afterEach(() => {
    if (radiologyExam) {
      cy.authenticatedRequest({
        method: 'DELETE',
        url: `/api/radiology-exams/${radiologyExam.id}`,
      }).then(() => {
        radiologyExam = undefined;
      });
    }
  });

  it('RadiologyExams menu should load RadiologyExams page', () => {
    cy.visit('/');
    cy.clickOnEntityMenuItem('radiology-exam');
    cy.wait('@entitiesRequest').then(({ response }) => {
      if (response?.body.length === 0) {
        cy.get(entityTableSelector).should('not.exist');
      } else {
        cy.get(entityTableSelector).should('exist');
      }
    });
    cy.getEntityHeading('RadiologyExam').should('exist');
    cy.location('pathname').should('eq', radiologyExamPageUrl);
  });

  describe('RadiologyExam page', () => {
    describe('create button click', () => {
      beforeEach(() => {
        cy.visit(radiologyExamPageUrl);
        cy.wait('@entitiesRequest');
      });

      it('should load create RadiologyExam page', () => {
        cy.get(entityCreateButtonSelector).click();
        cy.location('pathname').should('eq', `${radiologyExamPageUrl}/new`);
        cy.getEntityCreateUpdateHeading('RadiologyExam');
        cy.get(entityCreateSaveButtonSelector).should('exist');
        cy.get(entityCreateCancelButtonSelector).click();
        cy.wait('@entitiesRequest').then(({ response }) => {
          expect(response?.statusCode).to.equal(200);
        });
        cy.location('pathname').should('eq', radiologyExamPageUrl);
      });
    });

    describe('with existing value', () => {
      beforeEach(() => {
        cy.authenticatedRequest({
          method: 'POST',
          url: '/api/radiology-exams',
          body: radiologyExamSample,
        }).then(({ body }) => {
          radiologyExam = body;

          cy.intercept(
            {
              method: 'GET',
              url: '/api/radiology-exams+(?*|)',
              times: 1,
            },
            {
              statusCode: 200,
              body: [radiologyExam],
            },
          ).as('entitiesRequestInternal');
        });

        cy.visit(radiologyExamPageUrl);

        cy.wait('@entitiesRequestInternal');
      });

      it('detail button click should load details RadiologyExam page', () => {
        cy.get(entityDetailsButtonSelector).first().click();
        cy.getEntityDetailsHeading('radiologyExam');
        cy.get(entityDetailsBackButtonSelector).click();
        cy.wait('@entitiesRequest').then(({ response }) => {
          expect(response?.statusCode).to.equal(200);
        });
        cy.location('pathname').should('eq', radiologyExamPageUrl);
      });

      it('edit button click should load edit RadiologyExam page and go back', () => {
        cy.get(entityEditButtonSelector).first().click();
        cy.getEntityCreateUpdateHeading('RadiologyExam');
        cy.get(entityCreateSaveButtonSelector).should('exist');
        cy.get(entityCreateCancelButtonSelector).click();
        cy.wait('@entitiesRequest').then(({ response }) => {
          expect(response?.statusCode).to.equal(200);
        });
        cy.location('pathname').should('eq', radiologyExamPageUrl);
      });

      it('edit button click should load edit RadiologyExam page and save', () => {
        cy.get(entityEditButtonSelector).first().click();
        cy.getEntityCreateUpdateHeading('RadiologyExam');
        cy.get(entityCreateSaveButtonSelector).click();
        cy.wait('@entitiesRequest').then(({ response }) => {
          expect(response?.statusCode).to.equal(200);
        });
        cy.location('pathname').should('eq', radiologyExamPageUrl);
      });

      it('last delete button click should delete instance of RadiologyExam', () => {
        cy.intercept('GET', '/api/radiology-exams/*').as('dialogDeleteRequest');
        cy.get(entityDeleteButtonSelector).last().click();
        cy.wait('@dialogDeleteRequest');
        cy.getEntityDeleteDialogHeading('radiologyExam').should('exist');
        cy.get(entityConfirmDeleteButtonSelector).click();
        cy.wait('@deleteEntityRequest').then(({ response }) => {
          expect(response?.statusCode).to.equal(204);
        });
        cy.wait('@entitiesRequest').then(({ response }) => {
          expect(response?.statusCode).to.equal(200);
        });
        cy.location('pathname').should('eq', radiologyExamPageUrl);

        radiologyExam = undefined;
      });
    });
  });

  describe('new RadiologyExam page', () => {
    beforeEach(() => {
      cy.visit(radiologyExamPageUrl);
      cy.get(entityCreateButtonSelector).click();
      cy.getEntityCreateUpdateHeading('RadiologyExam');
    });

    it('should create an instance of RadiologyExam', () => {
      cy.get(`[data-cy="name"]`).type('mortally');
      cy.get(`[data-cy="name"]`).should('have.value', 'mortally');

      cy.get(`[data-cy="price"]`).type('7337.1');
      cy.get(`[data-cy="price"]`).should('have.value', '7337.1');

      cy.get(`[data-cy="active"]`).should('not.be.checked');
      cy.get(`[data-cy="active"]`).click();
      cy.get(`[data-cy="active"]`).should('be.checked');

      cy.get(entityCreateSaveButtonSelector).click();

      cy.wait('@postEntityRequest').then(({ response }) => {
        expect(response?.statusCode).to.equal(201);
        radiologyExam = response.body;
      });
      cy.wait('@entitiesRequest').then(({ response }) => {
        expect(response?.statusCode).to.equal(200);
      });
      cy.location('pathname').should('eq', radiologyExamPageUrl);
    });
  });
});
