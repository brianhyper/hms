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

describe('Appointment e2e test', () => {
  const appointmentPageUrl = '/appointment';
  let username: string;
  let password: string;
  const appointmentSample = { scheduledDate: '2026-09-23', scheduledTime: '21:58:00', status: 'SCHEDULED' };

  let appointment;
  let patient;
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
    // create an instance at the required relationship entity:
    cy.authenticatedRequest({
      method: 'POST',
      url: '/api/patients',
      body: {
        hospitalId: 'considering personalise aside',
        fullName: 'sharply',
        dateOfBirth: '2026-09-23',
        estimatedAge: 30,
        sex: 'MALE',
        sexEstimated: false,
        phone: '365.481.7766',
        email: 'Janae96@yahoo.com',
        identityDocumentType: 'NATIONAL_ID',
        identityDocumentNumber: 'angrily',
        occupation: 'ick',
        maritalStatus: 'while',
        nextOfKinName: 'till',
        nextOfKinPhone: 'hence reopen content',
        nextOfKinRelationship: 'PARENT',
        knownAllergies: 'towards psst',
        knownConditions: 'nor refine midst',
        villageEstate: 'far which conservative',
        registrationStatus: 'MERGED',
      },
    }).then(({ body }) => {
      patient = body;
    });
    // create an instance at the required relationship entity:
    cy.authenticatedRequest({
      method: 'POST',
      url: '/api/departments',
      body: { name: 'yippee amidst vivacious', code: 'before absentmindedly', active: false },
    }).then(({ body }) => {
      department = body;
    });
  });

  beforeEach(() => {
    cy.intercept('GET', '/api/appointments+(?*|)').as('entitiesRequest');
    cy.intercept('POST', '/api/appointments').as('postEntityRequest');
    cy.intercept('DELETE', '/api/appointments/*').as('deleteEntityRequest');
  });

  beforeEach(() => {
    // Simulate relationships api for better performance and reproducibility.
    cy.intercept('GET', '/api/visits', {
      statusCode: 200,
      body: [],
    });

    cy.intercept('GET', '/api/patients', {
      statusCode: 200,
      body: [patient],
    });

    cy.intercept('GET', '/api/departments', {
      statusCode: 200,
      body: [department],
    });

    cy.intercept('GET', '/api/users', {
      statusCode: 200,
      body: [],
    });
  });

  afterEach(() => {
    if (appointment) {
      cy.authenticatedRequest({
        method: 'DELETE',
        url: `/api/appointments/${appointment.id}`,
      }).then(() => {
        appointment = undefined;
      });
    }
  });

  afterEach(() => {
    if (patient) {
      cy.authenticatedRequest({
        method: 'DELETE',
        url: `/api/patients/${patient.id}`,
      }).then(() => {
        patient = undefined;
      });
    }
    if (department) {
      cy.authenticatedRequest({
        method: 'DELETE',
        url: `/api/departments/${department.id}`,
      }).then(() => {
        department = undefined;
      });
    }
  });

  it('Appointments menu should load Appointments page', () => {
    cy.visit('/');
    cy.clickOnEntityMenuItem('appointment');
    cy.wait('@entitiesRequest').then(({ response }) => {
      if (response?.body.length === 0) {
        cy.get(entityTableSelector).should('not.exist');
      } else {
        cy.get(entityTableSelector).should('exist');
      }
    });
    cy.getEntityHeading('Appointment').should('exist');
    cy.location('pathname').should('eq', appointmentPageUrl);
  });

  describe('Appointment page', () => {
    describe('create button click', () => {
      beforeEach(() => {
        cy.visit(appointmentPageUrl);
        cy.wait('@entitiesRequest');
      });

      it('should load create Appointment page', () => {
        cy.get(entityCreateButtonSelector).click();
        cy.location('pathname').should('eq', `${appointmentPageUrl}/new`);
        cy.getEntityCreateUpdateHeading('Appointment');
        cy.get(entityCreateSaveButtonSelector).should('exist');
        cy.get(entityCreateCancelButtonSelector).click();
        cy.wait('@entitiesRequest').then(({ response }) => {
          expect(response?.statusCode).to.equal(200);
        });
        cy.location('pathname').should('eq', appointmentPageUrl);
      });
    });

    describe('with existing value', () => {
      beforeEach(() => {
        cy.authenticatedRequest({
          method: 'POST',
          url: '/api/appointments',
          body: {
            ...appointmentSample,
            patient,
            department,
          },
        }).then(({ body }) => {
          appointment = body;

          cy.intercept(
            {
              method: 'GET',
              url: '/api/appointments+(?*|)',
              times: 1,
            },
            {
              statusCode: 200,
              headers: {
                link: '<http://localhost/api/appointments?page=0&size=20>; rel="last",<http://localhost/api/appointments?page=0&size=20>; rel="first"',
              },
              body: [appointment],
            },
          ).as('entitiesRequestInternal');
        });

        cy.visit(appointmentPageUrl);

        cy.wait('@entitiesRequestInternal');
      });

      it('detail button click should load details Appointment page', () => {
        cy.get(entityDetailsButtonSelector).first().click();
        cy.getEntityDetailsHeading('appointment');
        cy.get(entityDetailsBackButtonSelector).click();
        cy.wait('@entitiesRequest').then(({ response }) => {
          expect(response?.statusCode).to.equal(200);
        });
        cy.location('pathname').should('eq', appointmentPageUrl);
      });

      it('edit button click should load edit Appointment page and go back', () => {
        cy.get(entityEditButtonSelector).first().click();
        cy.getEntityCreateUpdateHeading('Appointment');
        cy.get(entityCreateSaveButtonSelector).should('exist');
        cy.get(entityCreateCancelButtonSelector).click();
        cy.wait('@entitiesRequest').then(({ response }) => {
          expect(response?.statusCode).to.equal(200);
        });
        cy.location('pathname').should('eq', appointmentPageUrl);
      });

      it('edit button click should load edit Appointment page and save', () => {
        cy.get(entityEditButtonSelector).first().click();
        cy.getEntityCreateUpdateHeading('Appointment');
        cy.get(entityCreateSaveButtonSelector).click();
        cy.wait('@entitiesRequest').then(({ response }) => {
          expect(response?.statusCode).to.equal(200);
        });
        cy.location('pathname').should('eq', appointmentPageUrl);
      });

      it('last delete button click should delete instance of Appointment', () => {
        cy.intercept('GET', '/api/appointments/*').as('dialogDeleteRequest');
        cy.get(entityDeleteButtonSelector).last().click();
        cy.wait('@dialogDeleteRequest');
        cy.getEntityDeleteDialogHeading('appointment').should('exist');
        cy.get(entityConfirmDeleteButtonSelector).click();
        cy.wait('@deleteEntityRequest').then(({ response }) => {
          expect(response?.statusCode).to.equal(204);
        });
        cy.wait('@entitiesRequest').then(({ response }) => {
          expect(response?.statusCode).to.equal(200);
        });
        cy.location('pathname').should('eq', appointmentPageUrl);

        appointment = undefined;
      });
    });
  });

  describe('new Appointment page', () => {
    beforeEach(() => {
      cy.visit(appointmentPageUrl);
      cy.get(entityCreateButtonSelector).click();
      cy.getEntityCreateUpdateHeading('Appointment');
    });

    it('should create an instance of Appointment', () => {
      cy.get(`[data-cy="scheduledDate"]`).type('2026-09-24');
      cy.get(`[data-cy="scheduledDate"]`).blur();
      cy.get(`[data-cy="scheduledDate"]`).should('have.value', '2026-09-24');

      cy.get(`[data-cy="scheduledTime"]`).type('02:34:00');
      cy.get(`[data-cy="scheduledTime"]`).invoke('val').should('match', new RegExp('02:34:00'));

      cy.get(`[data-cy="reason"]`).type('fragrant without hence');
      cy.get(`[data-cy="reason"]`).should('have.value', 'fragrant without hence');

      cy.get(`[data-cy="status"]`).select('CHECKED_IN');

      cy.get(`[data-cy="patient"]`).select(1);
      cy.get(`[data-cy="department"]`).select(1);

      cy.get(entityCreateSaveButtonSelector).click();

      cy.wait('@postEntityRequest').then(({ response }) => {
        expect(response?.statusCode).to.equal(201);
        appointment = response.body;
      });
      cy.wait('@entitiesRequest').then(({ response }) => {
        expect(response?.statusCode).to.equal(200);
      });
      cy.location('pathname').should('eq', appointmentPageUrl);
    });
  });
});
