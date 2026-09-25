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

describe('Referral e2e test', () => {
  const referralPageUrl = '/referral';
  let username: string;
  let password: string;
  // const referralSample = {"type":"EXTERNAL","destination":"excluding below","reason":"case however resolve","status":"PENDING","createdAt":"2026-09-24T00:40:49.216Z"};

  let referral;
  // let visit;
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
      url: '/api/visits',
      body: {"type":"PHARMACY_ONLY","priority":"NORMAL","reasonForVisit":"content across and","status":"CLOSED","queueSkipReason":"roadway rebuff","createdAt":"2026-09-23T18:43:58.611Z","startedVitalsAt":"2026-09-23T14:00:17.505Z","startedConsultationAt":"2026-09-23T18:03:21.609Z","closedAt":"2026-09-23T19:03:28.305Z"},
    }).then(({ body }) => {
      visit = body;
    });
    // create an instance at the required relationship entity:
    cy.authenticatedRequest({
      method: 'POST',
      url: '/api/users',
      body: {"login":"Shad72","firstName":"Oran","lastName":"Rolfson","email":"Joshua40@gmail.com","imageUrl":"unlike selfishly never"},
    }).then(({ body }) => {
      user = body;
    });
  });
   */

  beforeEach(() => {
    cy.intercept('GET', '/api/referrals+(?*|)').as('entitiesRequest');
    cy.intercept('POST', '/api/referrals').as('postEntityRequest');
    cy.intercept('DELETE', '/api/referrals/*').as('deleteEntityRequest');
  });

  /* Disabled due to incompatibility
  beforeEach(() => {
    // Simulate relationships api for better performance and reproducibility.
    cy.intercept('GET', '/api/visits', {
      statusCode: 200,
      body: [visit],
    });

    cy.intercept('GET', '/api/users', {
      statusCode: 200,
      body: [user],
    });

    cy.intercept('GET', '/api/departments', {
      statusCode: 200,
      body: [],
    });

  });
   */

  afterEach(() => {
    if (referral) {
      cy.authenticatedRequest({
        method: 'DELETE',
        url: `/api/referrals/${referral.id}`,
      }).then(() => {
        referral = undefined;
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

  it('Referrals menu should load Referrals page', () => {
    cy.visit('/');
    cy.clickOnEntityMenuItem('referral');
    cy.wait('@entitiesRequest').then(({ response }) => {
      if (response?.body.length === 0) {
        cy.get(entityTableSelector).should('not.exist');
      } else {
        cy.get(entityTableSelector).should('exist');
      }
    });
    cy.getEntityHeading('Referral').should('exist');
    cy.location('pathname').should('eq', referralPageUrl);
  });

  describe('Referral page', () => {
    describe('create button click', () => {
      beforeEach(() => {
        cy.visit(referralPageUrl);
        cy.wait('@entitiesRequest');
      });

      it('should load create Referral page', () => {
        cy.get(entityCreateButtonSelector).click();
        cy.location('pathname').should('eq', `${referralPageUrl}/new`);
        cy.getEntityCreateUpdateHeading('Referral');
        cy.get(entityCreateSaveButtonSelector).should('exist');
        cy.get(entityCreateCancelButtonSelector).click();
        cy.wait('@entitiesRequest').then(({ response }) => {
          expect(response?.statusCode).to.equal(200);
        });
        cy.location('pathname').should('eq', referralPageUrl);
      });
    });

    describe('with existing value', () => {
      /* Disabled due to incompatibility
      beforeEach(() => {
        cy.authenticatedRequest({
          method: 'POST',
          url: '/api/referrals',
          body: {
            ...referralSample,
            visit: visit,
            referredBy: user,
          },
        }).then(({ body }) => {
          referral = body;

          cy.intercept(
            {
              method: 'GET',
              url: '/api/referrals+(?*|)',
              times: 1,
            },
            {
              statusCode: 200,
              body: [referral],
            }
          ).as('entitiesRequestInternal');
        });

        cy.visit(referralPageUrl);

        cy.wait('@entitiesRequestInternal');
      });
       */

      beforeEach(function () {
        cy.visit(referralPageUrl);

        cy.wait('@entitiesRequest').then(({ response }) => {
          if (response?.body.length === 0) {
            this.skip();
          }
        });
      });

      it('detail button click should load details Referral page', () => {
        cy.get(entityDetailsButtonSelector).first().click();
        cy.getEntityDetailsHeading('referral');
        cy.get(entityDetailsBackButtonSelector).click();
        cy.wait('@entitiesRequest').then(({ response }) => {
          expect(response?.statusCode).to.equal(200);
        });
        cy.location('pathname').should('eq', referralPageUrl);
      });

      it('edit button click should load edit Referral page and go back', () => {
        cy.get(entityEditButtonSelector).first().click();
        cy.getEntityCreateUpdateHeading('Referral');
        cy.get(entityCreateSaveButtonSelector).should('exist');
        cy.get(entityCreateCancelButtonSelector).click();
        cy.wait('@entitiesRequest').then(({ response }) => {
          expect(response?.statusCode).to.equal(200);
        });
        cy.location('pathname').should('eq', referralPageUrl);
      });

      it('edit button click should load edit Referral page and save', () => {
        cy.get(entityEditButtonSelector).first().click();
        cy.getEntityCreateUpdateHeading('Referral');
        cy.get(entityCreateSaveButtonSelector).click();
        cy.wait('@entitiesRequest').then(({ response }) => {
          expect(response?.statusCode).to.equal(200);
        });
        cy.location('pathname').should('eq', referralPageUrl);
      });

      // Reason: cannot create a required entity with relationship with required relationships.
      it.skip('last delete button click should delete instance of Referral', () => {
        cy.intercept('GET', '/api/referrals/*').as('dialogDeleteRequest');
        cy.get(entityDeleteButtonSelector).last().click();
        cy.wait('@dialogDeleteRequest');
        cy.getEntityDeleteDialogHeading('referral').should('exist');
        cy.get(entityConfirmDeleteButtonSelector).click();
        cy.wait('@deleteEntityRequest').then(({ response }) => {
          expect(response?.statusCode).to.equal(204);
        });
        cy.wait('@entitiesRequest').then(({ response }) => {
          expect(response?.statusCode).to.equal(200);
        });
        cy.location('pathname').should('eq', referralPageUrl);

        referral = undefined;
      });
    });
  });

  describe('new Referral page', () => {
    beforeEach(() => {
      cy.visit(referralPageUrl);
      cy.get(entityCreateButtonSelector).click();
      cy.getEntityCreateUpdateHeading('Referral');
    });

    // Reason: cannot create a required entity with relationship with required relationships.
    it.skip('should create an instance of Referral', () => {
      cy.get(`[data-cy="type"]`).select('INTERNAL');

      cy.get(`[data-cy="destination"]`).type('why treble unimpressively');
      cy.get(`[data-cy="destination"]`).should('have.value', 'why treble unimpressively');

      cy.get(`[data-cy="destinationEmail"]`).type('because');
      cy.get(`[data-cy="destinationEmail"]`).should('have.value', 'because');

      cy.get(`[data-cy="reason"]`).type('muted');
      cy.get(`[data-cy="reason"]`).should('have.value', 'muted');

      cy.get(`[data-cy="notes"]`).type('treble');
      cy.get(`[data-cy="notes"]`).should('have.value', 'treble');

      cy.get(`[data-cy="status"]`).select('COMPLETED');

      cy.get(`[data-cy="createdAt"]`).type('2026-09-23T09:32');
      cy.get(`[data-cy="createdAt"]`).blur();
      cy.get(`[data-cy="createdAt"]`).should('have.value', '2026-09-23T09:32');

      cy.get(`[data-cy="visit"]`).select(1);
      cy.get(`[data-cy="referredBy"]`).select(1);

      cy.get(entityCreateSaveButtonSelector).click();

      cy.wait('@postEntityRequest').then(({ response }) => {
        expect(response?.statusCode).to.equal(201);
        referral = response.body;
      });
      cy.wait('@entitiesRequest').then(({ response }) => {
        expect(response?.statusCode).to.equal(200);
      });
      cy.location('pathname').should('eq', referralPageUrl);
    });
  });
});
