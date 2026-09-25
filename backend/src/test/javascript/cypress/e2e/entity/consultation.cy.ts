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

describe('Consultation e2e test', () => {
  const consultationPageUrl = '/consultation';
  let username: string;
  let password: string;
  // const consultationSample = {"status":"COMPLETED","startedAt":"2026-09-24T00:52:49.164Z"};

  let consultation;
  // let user;
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
      url: '/api/users',
      body: {"login":"Mazie.Doyle-Halvorson","firstName":"Imani","lastName":"Bernier-Dicki","email":"Ada.Balistreri-Koss16@yahoo.com","imageUrl":"jubilantly"},
    }).then(({ body }) => {
      user = body;
    });
    // create an instance at the required relationship entity:
    cy.authenticatedRequest({
      method: 'POST',
      url: '/api/visits',
      body: {"type":"OUTPATIENT","priority":"URGENT","reasonForVisit":"suspiciously confide","status":"IN_CONSULTATION","queueSkipReason":"browse vacation ick","createdAt":"2026-09-24T02:51:57.666Z","startedVitalsAt":"2026-09-23T10:20:19.222Z","startedConsultationAt":"2026-09-24T03:36:23.380Z","closedAt":"2026-09-23T17:08:53.886Z"},
    }).then(({ body }) => {
      visit = body;
    });
  });
   */

  beforeEach(() => {
    cy.intercept('GET', '/api/consultations+(?*|)').as('entitiesRequest');
    cy.intercept('POST', '/api/consultations').as('postEntityRequest');
    cy.intercept('DELETE', '/api/consultations/*').as('deleteEntityRequest');
  });

  /* Disabled due to incompatibility
  beforeEach(() => {
    // Simulate relationships api for better performance and reproducibility.
    cy.intercept('GET', '/api/users', {
      statusCode: 200,
      body: [user],
    });

    cy.intercept('GET', '/api/diagnoses', {
      statusCode: 200,
      body: [],
    });

    cy.intercept('GET', '/api/visits', {
      statusCode: 200,
      body: [visit],
    });

  });
   */

  afterEach(() => {
    if (consultation) {
      cy.authenticatedRequest({
        method: 'DELETE',
        url: `/api/consultations/${consultation.id}`,
      }).then(() => {
        consultation = undefined;
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

  it('Consultations menu should load Consultations page', () => {
    cy.visit('/');
    cy.clickOnEntityMenuItem('consultation');
    cy.wait('@entitiesRequest').then(({ response }) => {
      if (response?.body.length === 0) {
        cy.get(entityTableSelector).should('not.exist');
      } else {
        cy.get(entityTableSelector).should('exist');
      }
    });
    cy.getEntityHeading('Consultation').should('exist');
    cy.location('pathname').should('eq', consultationPageUrl);
  });

  describe('Consultation page', () => {
    describe('create button click', () => {
      beforeEach(() => {
        cy.visit(consultationPageUrl);
        cy.wait('@entitiesRequest');
      });

      it('should load create Consultation page', () => {
        cy.get(entityCreateButtonSelector).click();
        cy.location('pathname').should('eq', `${consultationPageUrl}/new`);
        cy.getEntityCreateUpdateHeading('Consultation');
        cy.get(entityCreateSaveButtonSelector).should('exist');
        cy.get(entityCreateCancelButtonSelector).click();
        cy.wait('@entitiesRequest').then(({ response }) => {
          expect(response?.statusCode).to.equal(200);
        });
        cy.location('pathname').should('eq', consultationPageUrl);
      });
    });

    describe('with existing value', () => {
      /* Disabled due to incompatibility
      beforeEach(() => {
        cy.authenticatedRequest({
          method: 'POST',
          url: '/api/consultations',
          body: {
            ...consultationSample,
            doctor: user,
            visit: visit,
          },
        }).then(({ body }) => {
          consultation = body;

          cy.intercept(
            {
              method: 'GET',
              url: '/api/consultations+(?*|)',
              times: 1,
            },
            {
              statusCode: 200,
              body: [consultation],
            }
          ).as('entitiesRequestInternal');
        });

        cy.visit(consultationPageUrl);

        cy.wait('@entitiesRequestInternal');
      });
       */

      beforeEach(function () {
        cy.visit(consultationPageUrl);

        cy.wait('@entitiesRequest').then(({ response }) => {
          if (response?.body.length === 0) {
            this.skip();
          }
        });
      });

      it('detail button click should load details Consultation page', () => {
        cy.get(entityDetailsButtonSelector).first().click();
        cy.getEntityDetailsHeading('consultation');
        cy.get(entityDetailsBackButtonSelector).click();
        cy.wait('@entitiesRequest').then(({ response }) => {
          expect(response?.statusCode).to.equal(200);
        });
        cy.location('pathname').should('eq', consultationPageUrl);
      });

      it('edit button click should load edit Consultation page and go back', () => {
        cy.get(entityEditButtonSelector).first().click();
        cy.getEntityCreateUpdateHeading('Consultation');
        cy.get(entityCreateSaveButtonSelector).should('exist');
        cy.get(entityCreateCancelButtonSelector).click();
        cy.wait('@entitiesRequest').then(({ response }) => {
          expect(response?.statusCode).to.equal(200);
        });
        cy.location('pathname').should('eq', consultationPageUrl);
      });

      it('edit button click should load edit Consultation page and save', () => {
        cy.get(entityEditButtonSelector).first().click();
        cy.getEntityCreateUpdateHeading('Consultation');
        cy.get(entityCreateSaveButtonSelector).click();
        cy.wait('@entitiesRequest').then(({ response }) => {
          expect(response?.statusCode).to.equal(200);
        });
        cy.location('pathname').should('eq', consultationPageUrl);
      });

      // Reason: cannot create a required entity with relationship with required relationships.
      it.skip('last delete button click should delete instance of Consultation', () => {
        cy.intercept('GET', '/api/consultations/*').as('dialogDeleteRequest');
        cy.get(entityDeleteButtonSelector).last().click();
        cy.wait('@dialogDeleteRequest');
        cy.getEntityDeleteDialogHeading('consultation').should('exist');
        cy.get(entityConfirmDeleteButtonSelector).click();
        cy.wait('@deleteEntityRequest').then(({ response }) => {
          expect(response?.statusCode).to.equal(204);
        });
        cy.wait('@entitiesRequest').then(({ response }) => {
          expect(response?.statusCode).to.equal(200);
        });
        cy.location('pathname').should('eq', consultationPageUrl);

        consultation = undefined;
      });
    });
  });

  describe('new Consultation page', () => {
    beforeEach(() => {
      cy.visit(consultationPageUrl);
      cy.get(entityCreateButtonSelector).click();
      cy.getEntityCreateUpdateHeading('Consultation');
    });

    // Reason: cannot create a required entity with relationship with required relationships.
    it.skip('should create an instance of Consultation', () => {
      cy.get(`[data-cy="presentingComplaint"]`).type('another illustrious');
      cy.get(`[data-cy="presentingComplaint"]`).should('have.value', 'another illustrious');

      cy.get(`[data-cy="examinationFindings"]`).type('archaeology unless arraign');
      cy.get(`[data-cy="examinationFindings"]`).should('have.value', 'archaeology unless arraign');

      cy.get(`[data-cy="diagnosisOther"]`).type('encouragement until');
      cy.get(`[data-cy="diagnosisOther"]`).should('have.value', 'encouragement until');

      cy.get(`[data-cy="observations"]`).type('ick');
      cy.get(`[data-cy="observations"]`).should('have.value', 'ick');

      cy.get(`[data-cy="followUpInstructions"]`).type('geez tragic');
      cy.get(`[data-cy="followUpInstructions"]`).should('have.value', 'geez tragic');

      cy.get(`[data-cy="status"]`).select('COMPLETED');

      cy.get(`[data-cy="startedAt"]`).type('2026-09-24T08:03');
      cy.get(`[data-cy="startedAt"]`).blur();
      cy.get(`[data-cy="startedAt"]`).should('have.value', '2026-09-24T08:03');

      cy.get(`[data-cy="completedAt"]`).type('2026-09-23T15:09');
      cy.get(`[data-cy="completedAt"]`).blur();
      cy.get(`[data-cy="completedAt"]`).should('have.value', '2026-09-23T15:09');

      cy.get(`[data-cy="doctor"]`).select(1);
      cy.get(`[data-cy="visit"]`).select(1);

      cy.get(entityCreateSaveButtonSelector).click();

      cy.wait('@postEntityRequest').then(({ response }) => {
        expect(response?.statusCode).to.equal(201);
        consultation = response.body;
      });
      cy.wait('@entitiesRequest').then(({ response }) => {
        expect(response?.statusCode).to.equal(200);
      });
      cy.location('pathname').should('eq', consultationPageUrl);
    });
  });
});
