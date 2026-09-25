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

describe('Department e2e test', () => {
  const departmentPageUrl = '/department';
  let username: string;
  let password: string;
  const departmentSample = { name: 'duh pfft gosh', code: 'that', active: true };

  let department;

  before(() => {
    cy.credentials().then(credentials => {
      ({ username, password } = credentials);
    });
  });

  beforeEach(() => {
    cy.login(username, password);
  });

  beforeEach(() => {
    cy.intercept('GET', '/api/departments+(?*|)').as('entitiesRequest');
    cy.intercept('POST', '/api/departments').as('postEntityRequest');
    cy.intercept('DELETE', '/api/departments/*').as('deleteEntityRequest');
  });

  afterEach(() => {
    if (department) {
      cy.authenticatedRequest({
        method: 'DELETE',
        url: `/api/departments/${department.id}`,
      }).then(() => {
        department = undefined;
      });
    }
  });

  it('Departments menu should load Departments page', () => {
    cy.visit('/');
    cy.clickOnEntityMenuItem('department');
    cy.wait('@entitiesRequest').then(({ response }) => {
      if (response?.body.length === 0) {
        cy.get(entityTableSelector).should('not.exist');
      } else {
        cy.get(entityTableSelector).should('exist');
      }
    });
    cy.getEntityHeading('Department').should('exist');
    cy.location('pathname').should('eq', departmentPageUrl);
  });

  describe('Department page', () => {
    describe('create button click', () => {
      beforeEach(() => {
        cy.visit(departmentPageUrl);
        cy.wait('@entitiesRequest');
      });

      it('should load create Department page', () => {
        cy.get(entityCreateButtonSelector).click();
        cy.location('pathname').should('eq', `${departmentPageUrl}/new`);
        cy.getEntityCreateUpdateHeading('Department');
        cy.get(entityCreateSaveButtonSelector).should('exist');
        cy.get(entityCreateCancelButtonSelector).click();
        cy.wait('@entitiesRequest').then(({ response }) => {
          expect(response?.statusCode).to.equal(200);
        });
        cy.location('pathname').should('eq', departmentPageUrl);
      });
    });

    describe('with existing value', () => {
      beforeEach(() => {
        cy.authenticatedRequest({
          method: 'POST',
          url: '/api/departments',
          body: departmentSample,
        }).then(({ body }) => {
          department = body;

          cy.intercept(
            {
              method: 'GET',
              url: '/api/departments+(?*|)',
              times: 1,
            },
            {
              statusCode: 200,
              body: [department],
            },
          ).as('entitiesRequestInternal');
        });

        cy.visit(departmentPageUrl);

        cy.wait('@entitiesRequestInternal');
      });

      it('detail button click should load details Department page', () => {
        cy.get(entityDetailsButtonSelector).first().click();
        cy.getEntityDetailsHeading('department');
        cy.get(entityDetailsBackButtonSelector).click();
        cy.wait('@entitiesRequest').then(({ response }) => {
          expect(response?.statusCode).to.equal(200);
        });
        cy.location('pathname').should('eq', departmentPageUrl);
      });

      it('edit button click should load edit Department page and go back', () => {
        cy.get(entityEditButtonSelector).first().click();
        cy.getEntityCreateUpdateHeading('Department');
        cy.get(entityCreateSaveButtonSelector).should('exist');
        cy.get(entityCreateCancelButtonSelector).click();
        cy.wait('@entitiesRequest').then(({ response }) => {
          expect(response?.statusCode).to.equal(200);
        });
        cy.location('pathname').should('eq', departmentPageUrl);
      });

      it('edit button click should load edit Department page and save', () => {
        cy.get(entityEditButtonSelector).first().click();
        cy.getEntityCreateUpdateHeading('Department');
        cy.get(entityCreateSaveButtonSelector).click();
        cy.wait('@entitiesRequest').then(({ response }) => {
          expect(response?.statusCode).to.equal(200);
        });
        cy.location('pathname').should('eq', departmentPageUrl);
      });

      it('last delete button click should delete instance of Department', () => {
        cy.intercept('GET', '/api/departments/*').as('dialogDeleteRequest');
        cy.get(entityDeleteButtonSelector).last().click();
        cy.wait('@dialogDeleteRequest');
        cy.getEntityDeleteDialogHeading('department').should('exist');
        cy.get(entityConfirmDeleteButtonSelector).click();
        cy.wait('@deleteEntityRequest').then(({ response }) => {
          expect(response?.statusCode).to.equal(204);
        });
        cy.wait('@entitiesRequest').then(({ response }) => {
          expect(response?.statusCode).to.equal(200);
        });
        cy.location('pathname').should('eq', departmentPageUrl);

        department = undefined;
      });
    });
  });

  describe('new Department page', () => {
    beforeEach(() => {
      cy.visit(departmentPageUrl);
      cy.get(entityCreateButtonSelector).click();
      cy.getEntityCreateUpdateHeading('Department');
    });

    it('should create an instance of Department', () => {
      cy.get(`[data-cy="name"]`).type('absentmindedly potable');
      cy.get(`[data-cy="name"]`).should('have.value', 'absentmindedly potable');

      cy.get(`[data-cy="code"]`).type('ghost since carefree');
      cy.get(`[data-cy="code"]`).should('have.value', 'ghost since carefree');

      cy.get(`[data-cy="active"]`).should('not.be.checked');
      cy.get(`[data-cy="active"]`).click();
      cy.get(`[data-cy="active"]`).should('be.checked');

      cy.get(entityCreateSaveButtonSelector).click();

      cy.wait('@postEntityRequest').then(({ response }) => {
        expect(response?.statusCode).to.equal(201);
        department = response.body;
      });
      cy.wait('@entitiesRequest').then(({ response }) => {
        expect(response?.statusCode).to.equal(200);
      });
      cy.location('pathname').should('eq', departmentPageUrl);
    });
  });
});
