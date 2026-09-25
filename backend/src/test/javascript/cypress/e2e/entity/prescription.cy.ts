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

describe('Prescription e2e test', () => {
  const prescriptionPageUrl = '/prescription';
  let username: string;
  let password: string;
  const prescriptionSample = { source: 'EXTERNAL', status: 'READY_FOR_DISPENSE' };

  let prescription;

  before(() => {
    cy.credentials().then(credentials => {
      ({ username, password } = credentials);
    });
  });

  beforeEach(() => {
    cy.login(username, password);
  });

  beforeEach(() => {
    cy.intercept('GET', '/api/prescriptions+(?*|)').as('entitiesRequest');
    cy.intercept('POST', '/api/prescriptions').as('postEntityRequest');
    cy.intercept('DELETE', '/api/prescriptions/*').as('deleteEntityRequest');
  });

  afterEach(() => {
    if (prescription) {
      cy.authenticatedRequest({
        method: 'DELETE',
        url: `/api/prescriptions/${prescription.id}`,
      }).then(() => {
        prescription = undefined;
      });
    }
  });

  it('Prescriptions menu should load Prescriptions page', () => {
    cy.visit('/');
    cy.clickOnEntityMenuItem('prescription');
    cy.wait('@entitiesRequest').then(({ response }) => {
      if (response?.body.length === 0) {
        cy.get(entityTableSelector).should('not.exist');
      } else {
        cy.get(entityTableSelector).should('exist');
      }
    });
    cy.getEntityHeading('Prescription').should('exist');
    cy.location('pathname').should('eq', prescriptionPageUrl);
  });

  describe('Prescription page', () => {
    describe('create button click', () => {
      beforeEach(() => {
        cy.visit(prescriptionPageUrl);
        cy.wait('@entitiesRequest');
      });

      it('should load create Prescription page', () => {
        cy.get(entityCreateButtonSelector).click();
        cy.location('pathname').should('eq', `${prescriptionPageUrl}/new`);
        cy.getEntityCreateUpdateHeading('Prescription');
        cy.get(entityCreateSaveButtonSelector).should('exist');
        cy.get(entityCreateCancelButtonSelector).click();
        cy.wait('@entitiesRequest').then(({ response }) => {
          expect(response?.statusCode).to.equal(200);
        });
        cy.location('pathname').should('eq', prescriptionPageUrl);
      });
    });

    describe('with existing value', () => {
      beforeEach(() => {
        cy.authenticatedRequest({
          method: 'POST',
          url: '/api/prescriptions',
          body: prescriptionSample,
        }).then(({ body }) => {
          prescription = body;

          cy.intercept(
            {
              method: 'GET',
              url: '/api/prescriptions+(?*|)',
              times: 1,
            },
            {
              statusCode: 200,
              headers: {
                link: '<http://localhost/api/prescriptions?page=0&size=20>; rel="last",<http://localhost/api/prescriptions?page=0&size=20>; rel="first"',
              },
              body: [prescription],
            },
          ).as('entitiesRequestInternal');
        });

        cy.visit(prescriptionPageUrl);

        cy.wait('@entitiesRequestInternal');
      });

      it('detail button click should load details Prescription page', () => {
        cy.get(entityDetailsButtonSelector).first().click();
        cy.getEntityDetailsHeading('prescription');
        cy.get(entityDetailsBackButtonSelector).click();
        cy.wait('@entitiesRequest').then(({ response }) => {
          expect(response?.statusCode).to.equal(200);
        });
        cy.location('pathname').should('eq', prescriptionPageUrl);
      });

      it('edit button click should load edit Prescription page and go back', () => {
        cy.get(entityEditButtonSelector).first().click();
        cy.getEntityCreateUpdateHeading('Prescription');
        cy.get(entityCreateSaveButtonSelector).should('exist');
        cy.get(entityCreateCancelButtonSelector).click();
        cy.wait('@entitiesRequest').then(({ response }) => {
          expect(response?.statusCode).to.equal(200);
        });
        cy.location('pathname').should('eq', prescriptionPageUrl);
      });

      it('edit button click should load edit Prescription page and save', () => {
        cy.get(entityEditButtonSelector).first().click();
        cy.getEntityCreateUpdateHeading('Prescription');
        cy.get(entityCreateSaveButtonSelector).click();
        cy.wait('@entitiesRequest').then(({ response }) => {
          expect(response?.statusCode).to.equal(200);
        });
        cy.location('pathname').should('eq', prescriptionPageUrl);
      });

      it('last delete button click should delete instance of Prescription', () => {
        cy.intercept('GET', '/api/prescriptions/*').as('dialogDeleteRequest');
        cy.get(entityDeleteButtonSelector).last().click();
        cy.wait('@dialogDeleteRequest');
        cy.getEntityDeleteDialogHeading('prescription').should('exist');
        cy.get(entityConfirmDeleteButtonSelector).click();
        cy.wait('@deleteEntityRequest').then(({ response }) => {
          expect(response?.statusCode).to.equal(204);
        });
        cy.wait('@entitiesRequest').then(({ response }) => {
          expect(response?.statusCode).to.equal(200);
        });
        cy.location('pathname').should('eq', prescriptionPageUrl);

        prescription = undefined;
      });
    });
  });

  describe('new Prescription page', () => {
    beforeEach(() => {
      cy.visit(prescriptionPageUrl);
      cy.get(entityCreateButtonSelector).click();
      cy.getEntityCreateUpdateHeading('Prescription');
    });

    it('should create an instance of Prescription', () => {
      cy.get(`[data-cy="source"]`).select('INTERNAL');

      cy.get(`[data-cy="prescribingSource"]`).type('hawk ew zowie');
      cy.get(`[data-cy="prescribingSource"]`).should('have.value', 'hawk ew zowie');

      cy.get(`[data-cy="status"]`).select('READY_FOR_DISPENSE');

      cy.get(entityCreateSaveButtonSelector).click();

      cy.wait('@postEntityRequest').then(({ response }) => {
        expect(response?.statusCode).to.equal(201);
        prescription = response.body;
      });
      cy.wait('@entitiesRequest').then(({ response }) => {
        expect(response?.statusCode).to.equal(200);
      });
      cy.location('pathname').should('eq', prescriptionPageUrl);
    });
  });
});
