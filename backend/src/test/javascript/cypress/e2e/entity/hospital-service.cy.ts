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

describe('HospitalService e2e test', () => {
  const hospitalServicePageUrl = '/hospital-service';
  let username: string;
  let password: string;
  const hospitalServiceSample = { name: 'tank', price: 437.13, active: false };

  let hospitalService;

  before(() => {
    cy.credentials().then(credentials => {
      ({ username, password } = credentials);
    });
  });

  beforeEach(() => {
    cy.login(username, password);
  });

  beforeEach(() => {
    cy.intercept('GET', '/api/hospital-services+(?*|)').as('entitiesRequest');
    cy.intercept('POST', '/api/hospital-services').as('postEntityRequest');
    cy.intercept('DELETE', '/api/hospital-services/*').as('deleteEntityRequest');
  });

  afterEach(() => {
    if (hospitalService) {
      cy.authenticatedRequest({
        method: 'DELETE',
        url: `/api/hospital-services/${hospitalService.id}`,
      }).then(() => {
        hospitalService = undefined;
      });
    }
  });

  it('HospitalServices menu should load HospitalServices page', () => {
    cy.visit('/');
    cy.clickOnEntityMenuItem('hospital-service');
    cy.wait('@entitiesRequest').then(({ response }) => {
      if (response?.body.length === 0) {
        cy.get(entityTableSelector).should('not.exist');
      } else {
        cy.get(entityTableSelector).should('exist');
      }
    });
    cy.getEntityHeading('HospitalService').should('exist');
    cy.location('pathname').should('eq', hospitalServicePageUrl);
  });

  describe('HospitalService page', () => {
    describe('create button click', () => {
      beforeEach(() => {
        cy.visit(hospitalServicePageUrl);
        cy.wait('@entitiesRequest');
      });

      it('should load create HospitalService page', () => {
        cy.get(entityCreateButtonSelector).click();
        cy.location('pathname').should('eq', `${hospitalServicePageUrl}/new`);
        cy.getEntityCreateUpdateHeading('HospitalService');
        cy.get(entityCreateSaveButtonSelector).should('exist');
        cy.get(entityCreateCancelButtonSelector).click();
        cy.wait('@entitiesRequest').then(({ response }) => {
          expect(response?.statusCode).to.equal(200);
        });
        cy.location('pathname').should('eq', hospitalServicePageUrl);
      });
    });

    describe('with existing value', () => {
      beforeEach(() => {
        cy.authenticatedRequest({
          method: 'POST',
          url: '/api/hospital-services',
          body: hospitalServiceSample,
        }).then(({ body }) => {
          hospitalService = body;

          cy.intercept(
            {
              method: 'GET',
              url: '/api/hospital-services+(?*|)',
              times: 1,
            },
            {
              statusCode: 200,
              body: [hospitalService],
            },
          ).as('entitiesRequestInternal');
        });

        cy.visit(hospitalServicePageUrl);

        cy.wait('@entitiesRequestInternal');
      });

      it('detail button click should load details HospitalService page', () => {
        cy.get(entityDetailsButtonSelector).first().click();
        cy.getEntityDetailsHeading('hospitalService');
        cy.get(entityDetailsBackButtonSelector).click();
        cy.wait('@entitiesRequest').then(({ response }) => {
          expect(response?.statusCode).to.equal(200);
        });
        cy.location('pathname').should('eq', hospitalServicePageUrl);
      });

      it('edit button click should load edit HospitalService page and go back', () => {
        cy.get(entityEditButtonSelector).first().click();
        cy.getEntityCreateUpdateHeading('HospitalService');
        cy.get(entityCreateSaveButtonSelector).should('exist');
        cy.get(entityCreateCancelButtonSelector).click();
        cy.wait('@entitiesRequest').then(({ response }) => {
          expect(response?.statusCode).to.equal(200);
        });
        cy.location('pathname').should('eq', hospitalServicePageUrl);
      });

      it('edit button click should load edit HospitalService page and save', () => {
        cy.get(entityEditButtonSelector).first().click();
        cy.getEntityCreateUpdateHeading('HospitalService');
        cy.get(entityCreateSaveButtonSelector).click();
        cy.wait('@entitiesRequest').then(({ response }) => {
          expect(response?.statusCode).to.equal(200);
        });
        cy.location('pathname').should('eq', hospitalServicePageUrl);
      });

      it('last delete button click should delete instance of HospitalService', () => {
        cy.intercept('GET', '/api/hospital-services/*').as('dialogDeleteRequest');
        cy.get(entityDeleteButtonSelector).last().click();
        cy.wait('@dialogDeleteRequest');
        cy.getEntityDeleteDialogHeading('hospitalService').should('exist');
        cy.get(entityConfirmDeleteButtonSelector).click();
        cy.wait('@deleteEntityRequest').then(({ response }) => {
          expect(response?.statusCode).to.equal(204);
        });
        cy.wait('@entitiesRequest').then(({ response }) => {
          expect(response?.statusCode).to.equal(200);
        });
        cy.location('pathname').should('eq', hospitalServicePageUrl);

        hospitalService = undefined;
      });
    });
  });

  describe('new HospitalService page', () => {
    beforeEach(() => {
      cy.visit(hospitalServicePageUrl);
      cy.get(entityCreateButtonSelector).click();
      cy.getEntityCreateUpdateHeading('HospitalService');
    });

    it('should create an instance of HospitalService', () => {
      cy.get(`[data-cy="name"]`).type('wavy');
      cy.get(`[data-cy="name"]`).should('have.value', 'wavy');

      cy.get(`[data-cy="serviceType"]`).type('outrun');
      cy.get(`[data-cy="serviceType"]`).should('have.value', 'outrun');

      cy.get(`[data-cy="price"]`).type('11938.57');
      cy.get(`[data-cy="price"]`).should('have.value', '11938.57');

      cy.get(`[data-cy="active"]`).should('not.be.checked');
      cy.get(`[data-cy="active"]`).click();
      cy.get(`[data-cy="active"]`).should('be.checked');

      cy.get(entityCreateSaveButtonSelector).click();

      cy.wait('@postEntityRequest').then(({ response }) => {
        expect(response?.statusCode).to.equal(201);
        hospitalService = response.body;
      });
      cy.wait('@entitiesRequest').then(({ response }) => {
        expect(response?.statusCode).to.equal(200);
      });
      cy.location('pathname').should('eq', hospitalServicePageUrl);
    });
  });
});
