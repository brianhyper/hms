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

describe('BillLineItem e2e test', () => {
  const billLineItemPageUrl = '/bill-line-item';
  let username: string;
  let password: string;
  // const billLineItemSample = {"description":"psst","amount":11843.95,"sourceType":"CONSULTATION"};

  let billLineItem;
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
      url: '/api/bills',
      body: {"totalAmount":203.68,"status":"PAID","paidAt":"2026-09-23T16:53:28.916Z"},
    }).then(({ body }) => {
      bill = body;
    });
  });
   */

  beforeEach(() => {
    cy.intercept('GET', '/api/bill-line-items+(?*|)').as('entitiesRequest');
    cy.intercept('POST', '/api/bill-line-items').as('postEntityRequest');
    cy.intercept('DELETE', '/api/bill-line-items/*').as('deleteEntityRequest');
  });

  /* Disabled due to incompatibility
  beforeEach(() => {
    // Simulate relationships api for better performance and reproducibility.
    cy.intercept('GET', '/api/bills', {
      statusCode: 200,
      body: [bill],
    });

  });
   */

  afterEach(() => {
    if (billLineItem) {
      cy.authenticatedRequest({
        method: 'DELETE',
        url: `/api/bill-line-items/${billLineItem.id}`,
      }).then(() => {
        billLineItem = undefined;
      });
    }
  });

  /* Disabled due to incompatibility
  afterEach(() => {
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

  it('BillLineItems menu should load BillLineItems page', () => {
    cy.visit('/');
    cy.clickOnEntityMenuItem('bill-line-item');
    cy.wait('@entitiesRequest').then(({ response }) => {
      if (response?.body.length === 0) {
        cy.get(entityTableSelector).should('not.exist');
      } else {
        cy.get(entityTableSelector).should('exist');
      }
    });
    cy.getEntityHeading('BillLineItem').should('exist');
    cy.location('pathname').should('eq', billLineItemPageUrl);
  });

  describe('BillLineItem page', () => {
    describe('create button click', () => {
      beforeEach(() => {
        cy.visit(billLineItemPageUrl);
        cy.wait('@entitiesRequest');
      });

      it('should load create BillLineItem page', () => {
        cy.get(entityCreateButtonSelector).click();
        cy.location('pathname').should('eq', `${billLineItemPageUrl}/new`);
        cy.getEntityCreateUpdateHeading('BillLineItem');
        cy.get(entityCreateSaveButtonSelector).should('exist');
        cy.get(entityCreateCancelButtonSelector).click();
        cy.wait('@entitiesRequest').then(({ response }) => {
          expect(response?.statusCode).to.equal(200);
        });
        cy.location('pathname').should('eq', billLineItemPageUrl);
      });
    });

    describe('with existing value', () => {
      /* Disabled due to incompatibility
      beforeEach(() => {
        cy.authenticatedRequest({
          method: 'POST',
          url: '/api/bill-line-items',
          body: {
            ...billLineItemSample,
            bill: bill,
          },
        }).then(({ body }) => {
          billLineItem = body;

          cy.intercept(
            {
              method: 'GET',
              url: '/api/bill-line-items+(?*|)',
              times: 1,
            },
            {
              statusCode: 200,
              body: [billLineItem],
            }
          ).as('entitiesRequestInternal');
        });

        cy.visit(billLineItemPageUrl);

        cy.wait('@entitiesRequestInternal');
      });
       */

      beforeEach(function () {
        cy.visit(billLineItemPageUrl);

        cy.wait('@entitiesRequest').then(({ response }) => {
          if (response?.body.length === 0) {
            this.skip();
          }
        });
      });

      it('detail button click should load details BillLineItem page', () => {
        cy.get(entityDetailsButtonSelector).first().click();
        cy.getEntityDetailsHeading('billLineItem');
        cy.get(entityDetailsBackButtonSelector).click();
        cy.wait('@entitiesRequest').then(({ response }) => {
          expect(response?.statusCode).to.equal(200);
        });
        cy.location('pathname').should('eq', billLineItemPageUrl);
      });

      it('edit button click should load edit BillLineItem page and go back', () => {
        cy.get(entityEditButtonSelector).first().click();
        cy.getEntityCreateUpdateHeading('BillLineItem');
        cy.get(entityCreateSaveButtonSelector).should('exist');
        cy.get(entityCreateCancelButtonSelector).click();
        cy.wait('@entitiesRequest').then(({ response }) => {
          expect(response?.statusCode).to.equal(200);
        });
        cy.location('pathname').should('eq', billLineItemPageUrl);
      });

      it('edit button click should load edit BillLineItem page and save', () => {
        cy.get(entityEditButtonSelector).first().click();
        cy.getEntityCreateUpdateHeading('BillLineItem');
        cy.get(entityCreateSaveButtonSelector).click();
        cy.wait('@entitiesRequest').then(({ response }) => {
          expect(response?.statusCode).to.equal(200);
        });
        cy.location('pathname').should('eq', billLineItemPageUrl);
      });

      // Reason: cannot create a required entity with relationship with required relationships.
      it.skip('last delete button click should delete instance of BillLineItem', () => {
        cy.intercept('GET', '/api/bill-line-items/*').as('dialogDeleteRequest');
        cy.get(entityDeleteButtonSelector).last().click();
        cy.wait('@dialogDeleteRequest');
        cy.getEntityDeleteDialogHeading('billLineItem').should('exist');
        cy.get(entityConfirmDeleteButtonSelector).click();
        cy.wait('@deleteEntityRequest').then(({ response }) => {
          expect(response?.statusCode).to.equal(204);
        });
        cy.wait('@entitiesRequest').then(({ response }) => {
          expect(response?.statusCode).to.equal(200);
        });
        cy.location('pathname').should('eq', billLineItemPageUrl);

        billLineItem = undefined;
      });
    });
  });

  describe('new BillLineItem page', () => {
    beforeEach(() => {
      cy.visit(billLineItemPageUrl);
      cy.get(entityCreateButtonSelector).click();
      cy.getEntityCreateUpdateHeading('BillLineItem');
    });

    // Reason: cannot create a required entity with relationship with required relationships.
    it.skip('should create an instance of BillLineItem', () => {
      cy.get(`[data-cy="description"]`).type('but');
      cy.get(`[data-cy="description"]`).should('have.value', 'but');

      cy.get(`[data-cy="amount"]`).type('12985.72');
      cy.get(`[data-cy="amount"]`).should('have.value', '12985.72');

      cy.get(`[data-cy="sourceType"]`).select('RADIOLOGY');

      cy.get(`[data-cy="bill"]`).select(1);

      cy.get(entityCreateSaveButtonSelector).click();

      cy.wait('@postEntityRequest').then(({ response }) => {
        expect(response?.statusCode).to.equal(201);
        billLineItem = response.body;
      });
      cy.wait('@entitiesRequest').then(({ response }) => {
        expect(response?.statusCode).to.equal(200);
      });
      cy.location('pathname').should('eq', billLineItemPageUrl);
    });
  });
});
