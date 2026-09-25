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

describe('PrescriptionLine e2e test', () => {
  const prescriptionLinePageUrl = '/prescription-line';
  let username: string;
  let password: string;
  const prescriptionLineSample = { dosage: 'towards', duration: 'enraged worth', quantity: 18424 };

  let prescriptionLine;
  let prescription;
  let drug;

  before(() => {
    cy.credentials().then(credentials => {
      ({ username, password } = credentials);
    });
  });

  beforeEach(() => {
    cy.login(username, password);
  });

  beforeEach(() => {
    // create an instance at the required relationship entity:
    cy.authenticatedRequest({
      method: 'POST',
      url: '/api/prescriptions',
      body: { source: 'INTERNAL', prescribingSource: 'instead incidentally', status: 'PARTIALLY_DISPENSED' },
    }).then(({ body }) => {
      prescription = body;
    });
    // create an instance at the required relationship entity:
    cy.authenticatedRequest({
      method: 'POST',
      url: '/api/drugs',
      body: {
        name: 'yahoo questionably ah',
        unit: 'porter deceivingly',
        currentStock: 21294,
        reservedStock: 2090,
        lowStockThreshold: 7102,
        price: 16221.49,
        classification: 'POM',
        active: false,
      },
    }).then(({ body }) => {
      drug = body;
    });
  });

  beforeEach(() => {
    cy.intercept('GET', '/api/prescription-lines+(?*|)').as('entitiesRequest');
    cy.intercept('POST', '/api/prescription-lines').as('postEntityRequest');
    cy.intercept('DELETE', '/api/prescription-lines/*').as('deleteEntityRequest');
  });

  beforeEach(() => {
    // Simulate relationships api for better performance and reproducibility.
    cy.intercept('GET', '/api/prescriptions', {
      statusCode: 200,
      body: [prescription],
    });

    cy.intercept('GET', '/api/drugs', {
      statusCode: 200,
      body: [drug],
    });
  });

  afterEach(() => {
    if (prescriptionLine) {
      cy.authenticatedRequest({
        method: 'DELETE',
        url: `/api/prescription-lines/${prescriptionLine.id}`,
      }).then(() => {
        prescriptionLine = undefined;
      });
    }
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
    if (drug) {
      cy.authenticatedRequest({
        method: 'DELETE',
        url: `/api/drugs/${drug.id}`,
      }).then(() => {
        drug = undefined;
      });
    }
  });

  it('PrescriptionLines menu should load PrescriptionLines page', () => {
    cy.visit('/');
    cy.clickOnEntityMenuItem('prescription-line');
    cy.wait('@entitiesRequest').then(({ response }) => {
      if (response?.body.length === 0) {
        cy.get(entityTableSelector).should('not.exist');
      } else {
        cy.get(entityTableSelector).should('exist');
      }
    });
    cy.getEntityHeading('PrescriptionLine').should('exist');
    cy.location('pathname').should('eq', prescriptionLinePageUrl);
  });

  describe('PrescriptionLine page', () => {
    describe('create button click', () => {
      beforeEach(() => {
        cy.visit(prescriptionLinePageUrl);
        cy.wait('@entitiesRequest');
      });

      it('should load create PrescriptionLine page', () => {
        cy.get(entityCreateButtonSelector).click();
        cy.location('pathname').should('eq', `${prescriptionLinePageUrl}/new`);
        cy.getEntityCreateUpdateHeading('PrescriptionLine');
        cy.get(entityCreateSaveButtonSelector).should('exist');
        cy.get(entityCreateCancelButtonSelector).click();
        cy.wait('@entitiesRequest').then(({ response }) => {
          expect(response?.statusCode).to.equal(200);
        });
        cy.location('pathname').should('eq', prescriptionLinePageUrl);
      });
    });

    describe('with existing value', () => {
      beforeEach(() => {
        cy.authenticatedRequest({
          method: 'POST',
          url: '/api/prescription-lines',
          body: {
            ...prescriptionLineSample,
            prescription,
            drug,
          },
        }).then(({ body }) => {
          prescriptionLine = body;

          cy.intercept(
            {
              method: 'GET',
              url: '/api/prescription-lines+(?*|)',
              times: 1,
            },
            {
              statusCode: 200,
              body: [prescriptionLine],
            },
          ).as('entitiesRequestInternal');
        });

        cy.visit(prescriptionLinePageUrl);

        cy.wait('@entitiesRequestInternal');
      });

      it('detail button click should load details PrescriptionLine page', () => {
        cy.get(entityDetailsButtonSelector).first().click();
        cy.getEntityDetailsHeading('prescriptionLine');
        cy.get(entityDetailsBackButtonSelector).click();
        cy.wait('@entitiesRequest').then(({ response }) => {
          expect(response?.statusCode).to.equal(200);
        });
        cy.location('pathname').should('eq', prescriptionLinePageUrl);
      });

      it('edit button click should load edit PrescriptionLine page and go back', () => {
        cy.get(entityEditButtonSelector).first().click();
        cy.getEntityCreateUpdateHeading('PrescriptionLine');
        cy.get(entityCreateSaveButtonSelector).should('exist');
        cy.get(entityCreateCancelButtonSelector).click();
        cy.wait('@entitiesRequest').then(({ response }) => {
          expect(response?.statusCode).to.equal(200);
        });
        cy.location('pathname').should('eq', prescriptionLinePageUrl);
      });

      it('edit button click should load edit PrescriptionLine page and save', () => {
        cy.get(entityEditButtonSelector).first().click();
        cy.getEntityCreateUpdateHeading('PrescriptionLine');
        cy.get(entityCreateSaveButtonSelector).click();
        cy.wait('@entitiesRequest').then(({ response }) => {
          expect(response?.statusCode).to.equal(200);
        });
        cy.location('pathname').should('eq', prescriptionLinePageUrl);
      });

      it('last delete button click should delete instance of PrescriptionLine', () => {
        cy.intercept('GET', '/api/prescription-lines/*').as('dialogDeleteRequest');
        cy.get(entityDeleteButtonSelector).last().click();
        cy.wait('@dialogDeleteRequest');
        cy.getEntityDeleteDialogHeading('prescriptionLine').should('exist');
        cy.get(entityConfirmDeleteButtonSelector).click();
        cy.wait('@deleteEntityRequest').then(({ response }) => {
          expect(response?.statusCode).to.equal(204);
        });
        cy.wait('@entitiesRequest').then(({ response }) => {
          expect(response?.statusCode).to.equal(200);
        });
        cy.location('pathname').should('eq', prescriptionLinePageUrl);

        prescriptionLine = undefined;
      });
    });
  });

  describe('new PrescriptionLine page', () => {
    beforeEach(() => {
      cy.visit(prescriptionLinePageUrl);
      cy.get(entityCreateButtonSelector).click();
      cy.getEntityCreateUpdateHeading('PrescriptionLine');
    });

    it('should create an instance of PrescriptionLine', () => {
      cy.get(`[data-cy="dosage"]`).type('freely sans');
      cy.get(`[data-cy="dosage"]`).should('have.value', 'freely sans');

      cy.get(`[data-cy="duration"]`).type('amid hence');
      cy.get(`[data-cy="duration"]`).should('have.value', 'amid hence');

      cy.get(`[data-cy="quantity"]`).type('21688');
      cy.get(`[data-cy="quantity"]`).should('have.value', '21688');

      cy.get(`[data-cy="prescription"]`).select(1);
      cy.get(`[data-cy="drug"]`).select(1);

      cy.get(entityCreateSaveButtonSelector).click();

      cy.wait('@postEntityRequest').then(({ response }) => {
        expect(response?.statusCode).to.equal(201);
        prescriptionLine = response.body;
      });
      cy.wait('@entitiesRequest').then(({ response }) => {
        expect(response?.statusCode).to.equal(200);
      });
      cy.location('pathname').should('eq', prescriptionLinePageUrl);
    });
  });
});
