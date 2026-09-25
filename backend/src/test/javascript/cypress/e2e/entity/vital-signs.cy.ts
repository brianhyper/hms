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

describe('VitalSigns e2e test', () => {
  const vitalSignsPageUrl = '/vital-signs';
  let username: string;
  let password: string;
  // const vitalSignsSample = {};

  let vitalSigns;
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
      body: {"type":"PHARMACY_ONLY","priority":"URGENT","reasonForVisit":"brown unbearably","status":"WAITING_VITALS","queueSkipReason":"apud","createdAt":"2026-09-24T08:06:47.941Z","startedVitalsAt":"2026-09-24T01:53:16.157Z","startedConsultationAt":"2026-09-23T12:28:18.708Z","closedAt":"2026-09-23T14:34:45.053Z"},
    }).then(({ body }) => {
      visit = body;
    });
  });
   */

  beforeEach(() => {
    cy.intercept('GET', '/api/vital-signs+(?*|)').as('entitiesRequest');
    cy.intercept('POST', '/api/vital-signs').as('postEntityRequest');
    cy.intercept('DELETE', '/api/vital-signs/*').as('deleteEntityRequest');
  });

  /* Disabled due to incompatibility
  beforeEach(() => {
    // Simulate relationships api for better performance and reproducibility.
    cy.intercept('GET', '/api/visits', {
      statusCode: 200,
      body: [visit],
    });

  });
   */

  afterEach(() => {
    if (vitalSigns) {
      cy.authenticatedRequest({
        method: 'DELETE',
        url: `/api/vital-signs/${vitalSigns.id}`,
      }).then(() => {
        vitalSigns = undefined;
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

  it('VitalSignses menu should load VitalSignses page', () => {
    cy.visit('/');
    cy.clickOnEntityMenuItem('vital-signs');
    cy.wait('@entitiesRequest').then(({ response }) => {
      if (response?.body.length === 0) {
        cy.get(entityTableSelector).should('not.exist');
      } else {
        cy.get(entityTableSelector).should('exist');
      }
    });
    cy.getEntityHeading('VitalSigns').should('exist');
    cy.location('pathname').should('eq', vitalSignsPageUrl);
  });

  describe('VitalSigns page', () => {
    describe('create button click', () => {
      beforeEach(() => {
        cy.visit(vitalSignsPageUrl);
        cy.wait('@entitiesRequest');
      });

      it('should load create VitalSigns page', () => {
        cy.get(entityCreateButtonSelector).click();
        cy.location('pathname').should('eq', `${vitalSignsPageUrl}/new`);
        cy.getEntityCreateUpdateHeading('VitalSigns');
        cy.get(entityCreateSaveButtonSelector).should('exist');
        cy.get(entityCreateCancelButtonSelector).click();
        cy.wait('@entitiesRequest').then(({ response }) => {
          expect(response?.statusCode).to.equal(200);
        });
        cy.location('pathname').should('eq', vitalSignsPageUrl);
      });
    });

    describe('with existing value', () => {
      /* Disabled due to incompatibility
      beforeEach(() => {
        cy.authenticatedRequest({
          method: 'POST',
          url: '/api/vital-signs',
          body: {
            ...vitalSignsSample,
            visit: visit,
          },
        }).then(({ body }) => {
          vitalSigns = body;

          cy.intercept(
            {
              method: 'GET',
              url: '/api/vital-signs+(?*|)',
              times: 1,
            },
            {
              statusCode: 200,
              body: [vitalSigns],
            }
          ).as('entitiesRequestInternal');
        });

        cy.visit(vitalSignsPageUrl);

        cy.wait('@entitiesRequestInternal');
      });
       */

      beforeEach(function () {
        cy.visit(vitalSignsPageUrl);

        cy.wait('@entitiesRequest').then(({ response }) => {
          if (response?.body.length === 0) {
            this.skip();
          }
        });
      });

      it('detail button click should load details VitalSigns page', () => {
        cy.get(entityDetailsButtonSelector).first().click();
        cy.getEntityDetailsHeading('vitalSigns');
        cy.get(entityDetailsBackButtonSelector).click();
        cy.wait('@entitiesRequest').then(({ response }) => {
          expect(response?.statusCode).to.equal(200);
        });
        cy.location('pathname').should('eq', vitalSignsPageUrl);
      });

      it('edit button click should load edit VitalSigns page and go back', () => {
        cy.get(entityEditButtonSelector).first().click();
        cy.getEntityCreateUpdateHeading('VitalSigns');
        cy.get(entityCreateSaveButtonSelector).should('exist');
        cy.get(entityCreateCancelButtonSelector).click();
        cy.wait('@entitiesRequest').then(({ response }) => {
          expect(response?.statusCode).to.equal(200);
        });
        cy.location('pathname').should('eq', vitalSignsPageUrl);
      });

      it('edit button click should load edit VitalSigns page and save', () => {
        cy.get(entityEditButtonSelector).first().click();
        cy.getEntityCreateUpdateHeading('VitalSigns');
        cy.get(entityCreateSaveButtonSelector).click();
        cy.wait('@entitiesRequest').then(({ response }) => {
          expect(response?.statusCode).to.equal(200);
        });
        cy.location('pathname').should('eq', vitalSignsPageUrl);
      });

      // Reason: cannot create a required entity with relationship with required relationships.
      it.skip('last delete button click should delete instance of VitalSigns', () => {
        cy.intercept('GET', '/api/vital-signs/*').as('dialogDeleteRequest');
        cy.get(entityDeleteButtonSelector).last().click();
        cy.wait('@dialogDeleteRequest');
        cy.getEntityDeleteDialogHeading('vitalSigns').should('exist');
        cy.get(entityConfirmDeleteButtonSelector).click();
        cy.wait('@deleteEntityRequest').then(({ response }) => {
          expect(response?.statusCode).to.equal(204);
        });
        cy.wait('@entitiesRequest').then(({ response }) => {
          expect(response?.statusCode).to.equal(200);
        });
        cy.location('pathname').should('eq', vitalSignsPageUrl);

        vitalSigns = undefined;
      });
    });
  });

  describe('new VitalSigns page', () => {
    beforeEach(() => {
      cy.visit(vitalSignsPageUrl);
      cy.get(entityCreateButtonSelector).click();
      cy.getEntityCreateUpdateHeading('VitalSigns');
    });

    // Reason: cannot create a required entity with relationship with required relationships.
    it.skip('should create an instance of VitalSigns', () => {
      cy.get(`[data-cy="temperature"]`).type('8040.24');
      cy.get(`[data-cy="temperature"]`).should('have.value', '8040.24');

      cy.get(`[data-cy="pulseRate"]`).type('6829');
      cy.get(`[data-cy="pulseRate"]`).should('have.value', '6829');

      cy.get(`[data-cy="systolicBp"]`).type('14755');
      cy.get(`[data-cy="systolicBp"]`).should('have.value', '14755');

      cy.get(`[data-cy="diastolicBp"]`).type('32043');
      cy.get(`[data-cy="diastolicBp"]`).should('have.value', '32043');

      cy.get(`[data-cy="oxygenSaturation"]`).type('15814');
      cy.get(`[data-cy="oxygenSaturation"]`).should('have.value', '15814');

      cy.get(`[data-cy="weight"]`).type('31125.41');
      cy.get(`[data-cy="weight"]`).should('have.value', '31125.41');

      cy.get(`[data-cy="height"]`).type('24241.19');
      cy.get(`[data-cy="height"]`).should('have.value', '24241.19');

      cy.get(`[data-cy="bmi"]`).type('2173.22');
      cy.get(`[data-cy="bmi"]`).should('have.value', '2173.22');

      cy.get(`[data-cy="nutritionalStatus"]`).type('under consistency whenever');
      cy.get(`[data-cy="nutritionalStatus"]`).should('have.value', 'under consistency whenever');

      cy.get(`[data-cy="pregnancyScreening"]`).should('not.be.checked');
      cy.get(`[data-cy="pregnancyScreening"]`).click();
      cy.get(`[data-cy="pregnancyScreening"]`).should('be.checked');

      cy.get(`[data-cy="triageNotes"]`).type('apud');
      cy.get(`[data-cy="triageNotes"]`).should('have.value', 'apud');

      cy.get(`[data-cy="otherMeasurements"]`).type('gosh aw bonfire');
      cy.get(`[data-cy="otherMeasurements"]`).should('have.value', 'gosh aw bonfire');

      cy.get(`[data-cy="visit"]`).select(1);

      cy.get(entityCreateSaveButtonSelector).click();

      cy.wait('@postEntityRequest').then(({ response }) => {
        expect(response?.statusCode).to.equal(201);
        vitalSigns = response.body;
      });
      cy.wait('@entitiesRequest').then(({ response }) => {
        expect(response?.statusCode).to.equal(200);
      });
      cy.location('pathname').should('eq', vitalSignsPageUrl);
    });
  });
});
