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

describe('Payment e2e test', () => {
  const paymentPageUrl = '/payment';
  let username: string;
  let password: string;
  // const paymentSample = {"method":"INSURANCE","confirmationStatus":"REJECTED","receiptNumber":"gah","amount":13669,"recordedAt":"2026-09-23T10:25:53.863Z"};

  let payment;
  // let user;
  // let bill;

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
      body: {"login":"Micheal.Schiller","firstName":"Jaylon","lastName":"Wisozk","email":"Rhonda37@yahoo.com","imageUrl":"always hence bossy"},
    }).then(({ body }) => {
      user = body;
    });
    // create an instance at the required relationship entity:
    cy.authenticatedRequest({
      method: 'POST',
      url: '/api/bills',
      body: {"totalAmount":11131.34,"status":"PAID","paidAt":"2026-09-23T11:32:35.848Z"},
    }).then(({ body }) => {
      bill = body;
    });
  });
   */

  beforeEach(() => {
    cy.intercept('GET', '/api/payments+(?*|)').as('entitiesRequest');
    cy.intercept('POST', '/api/payments').as('postEntityRequest');
    cy.intercept('DELETE', '/api/payments/*').as('deleteEntityRequest');
  });

  /* Disabled due to incompatibility
  beforeEach(() => {
    // Simulate relationships api for better performance and reproducibility.
    cy.intercept('GET', '/api/users', {
      statusCode: 200,
      body: [user],
    });

    cy.intercept('GET', '/api/bills', {
      statusCode: 200,
      body: [bill],
    });

  });
   */

  afterEach(() => {
    if (payment) {
      cy.authenticatedRequest({
        method: 'DELETE',
        url: `/api/payments/${payment.id}`,
      }).then(() => {
        payment = undefined;
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
    if (bill) {
      cy.authenticatedRequest({
        method: 'DELETE',
        url: `/api/bills/${bill.id}`,
      }).then(() => {
        bill = undefined;
      });
    }
  });
   */

  it('Payments menu should load Payments page', () => {
    cy.visit('/');
    cy.clickOnEntityMenuItem('payment');
    cy.wait('@entitiesRequest').then(({ response }) => {
      if (response?.body.length === 0) {
        cy.get(entityTableSelector).should('not.exist');
      } else {
        cy.get(entityTableSelector).should('exist');
      }
    });
    cy.getEntityHeading('Payment').should('exist');
    cy.location('pathname').should('eq', paymentPageUrl);
  });

  describe('Payment page', () => {
    describe('create button click', () => {
      beforeEach(() => {
        cy.visit(paymentPageUrl);
        cy.wait('@entitiesRequest');
      });

      it('should load create Payment page', () => {
        cy.get(entityCreateButtonSelector).click();
        cy.location('pathname').should('eq', `${paymentPageUrl}/new`);
        cy.getEntityCreateUpdateHeading('Payment');
        cy.get(entityCreateSaveButtonSelector).should('exist');
        cy.get(entityCreateCancelButtonSelector).click();
        cy.wait('@entitiesRequest').then(({ response }) => {
          expect(response?.statusCode).to.equal(200);
        });
        cy.location('pathname').should('eq', paymentPageUrl);
      });
    });

    describe('with existing value', () => {
      /* Disabled due to incompatibility
      beforeEach(() => {
        cy.authenticatedRequest({
          method: 'POST',
          url: '/api/payments',
          body: {
            ...paymentSample,
            recordedBy: user,
            bill: bill,
          },
        }).then(({ body }) => {
          payment = body;

          cy.intercept(
            {
              method: 'GET',
              url: '/api/payments+(?*|)',
              times: 1,
            },
            {
              statusCode: 200,
              headers: {
                link: '<http://localhost/api/payments?page=0&size=20>; rel="last",<http://localhost/api/payments?page=0&size=20>; rel="first"',
              },
              body: [payment],
            }
          ).as('entitiesRequestInternal');
        });

        cy.visit(paymentPageUrl);

        cy.wait('@entitiesRequestInternal');
      });
       */

      beforeEach(function () {
        cy.visit(paymentPageUrl);

        cy.wait('@entitiesRequest').then(({ response }) => {
          if (response?.body.length === 0) {
            this.skip();
          }
        });
      });

      it('detail button click should load details Payment page', () => {
        cy.get(entityDetailsButtonSelector).first().click();
        cy.getEntityDetailsHeading('payment');
        cy.get(entityDetailsBackButtonSelector).click();
        cy.wait('@entitiesRequest').then(({ response }) => {
          expect(response?.statusCode).to.equal(200);
        });
        cy.location('pathname').should('eq', paymentPageUrl);
      });

      it('edit button click should load edit Payment page and go back', () => {
        cy.get(entityEditButtonSelector).first().click();
        cy.getEntityCreateUpdateHeading('Payment');
        cy.get(entityCreateSaveButtonSelector).should('exist');
        cy.get(entityCreateCancelButtonSelector).click();
        cy.wait('@entitiesRequest').then(({ response }) => {
          expect(response?.statusCode).to.equal(200);
        });
        cy.location('pathname').should('eq', paymentPageUrl);
      });

      it('edit button click should load edit Payment page and save', () => {
        cy.get(entityEditButtonSelector).first().click();
        cy.getEntityCreateUpdateHeading('Payment');
        cy.get(entityCreateSaveButtonSelector).click();
        cy.wait('@entitiesRequest').then(({ response }) => {
          expect(response?.statusCode).to.equal(200);
        });
        cy.location('pathname').should('eq', paymentPageUrl);
      });

      // Reason: cannot create a required entity with relationship with required relationships.
      it.skip('last delete button click should delete instance of Payment', () => {
        cy.intercept('GET', '/api/payments/*').as('dialogDeleteRequest');
        cy.get(entityDeleteButtonSelector).last().click();
        cy.wait('@dialogDeleteRequest');
        cy.getEntityDeleteDialogHeading('payment').should('exist');
        cy.get(entityConfirmDeleteButtonSelector).click();
        cy.wait('@deleteEntityRequest').then(({ response }) => {
          expect(response?.statusCode).to.equal(204);
        });
        cy.wait('@entitiesRequest').then(({ response }) => {
          expect(response?.statusCode).to.equal(200);
        });
        cy.location('pathname').should('eq', paymentPageUrl);

        payment = undefined;
      });
    });
  });

  describe('new Payment page', () => {
    beforeEach(() => {
      cy.visit(paymentPageUrl);
      cy.get(entityCreateButtonSelector).click();
      cy.getEntityCreateUpdateHeading('Payment');
    });

    // Reason: cannot create a required entity with relationship with required relationships.
    it.skip('should create an instance of Payment', () => {
      cy.get(`[data-cy="method"]`).select('CASH');

      cy.get(`[data-cy="mpesaReference"]`).type('thigh');
      cy.get(`[data-cy="mpesaReference"]`).should('have.value', 'thigh');

      cy.get(`[data-cy="insurerName"]`).type('of');
      cy.get(`[data-cy="insurerName"]`).should('have.value', 'of');

      cy.get(`[data-cy="confirmationStatus"]`).select('PENDING');

      cy.get(`[data-cy="receiptNumber"]`).type('rigid');
      cy.get(`[data-cy="receiptNumber"]`).should('have.value', 'rigid');

      cy.get(`[data-cy="amount"]`).type('30061.53');
      cy.get(`[data-cy="amount"]`).should('have.value', '30061.53');

      cy.get(`[data-cy="recordedAt"]`).type('2026-09-24T00:44');
      cy.get(`[data-cy="recordedAt"]`).blur();
      cy.get(`[data-cy="recordedAt"]`).should('have.value', '2026-09-24T00:44');

      cy.get(`[data-cy="recordedBy"]`).select(1);
      cy.get(`[data-cy="bill"]`).select(1);

      cy.get(entityCreateSaveButtonSelector).click();

      cy.wait('@postEntityRequest').then(({ response }) => {
        expect(response?.statusCode).to.equal(201);
        payment = response.body;
      });
      cy.wait('@entitiesRequest').then(({ response }) => {
        expect(response?.statusCode).to.equal(200);
      });
      cy.location('pathname').should('eq', paymentPageUrl);
    });
  });
});
