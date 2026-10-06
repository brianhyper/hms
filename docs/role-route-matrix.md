# Role-by-route matrix

Who may reach which route, read off the RBAC table in
`backend/src/main/java/com/hyperbrains/hms/config/SecurityConfiguration.java` on 2026-10-06.

**The table in `SecurityConfiguration` is the single source. This page is a copy of it, not a second one.** If the two
disagree, the table is right and this page is stale — regenerate it the same way the endpoint catalogue is regenerated,
after each group.

## How to read it

- **First match wins.** Spring Security applies the *first* rule in the table that matches the request, and every
  entity rule sits above the `/api/**` catch-all. The routes below show the rule that actually decides them.
- Role names here are the `AuthoritiesConstants` values without the `ROLE_` prefix, so `SUPER_ADMIN` in this page is
  `ROLE_SUPER_ADMIN` in the token and in `GET /api/account`.
- **`USER` is the stock account role**, admitted by the catch-all and by nothing else. That is what keeps the login and
  account endpoints working for an account with no hospital role yet. It is not a hospital role: do not offer it in a
  role picker.
- A route with **no row of its own** falls to the catch-all and is therefore reachable by *any* signed-in hospital role.
  `CatchAllCoverageIT` fails the build when a state-changing route is in that position. Reads are allowed to be there
  deliberately.
- This answers who may **reach** a route, not what is **inside** the response. A Lab user only ever gets `OrderType.LAB`
  rows (filtered repository query), Finance only gets priced non-clinical views (redacted DTOs), a doctor only sees their
  own queue (filtered by `Consultation.doctor`), and `GET /api/patients/{id}` records a `PatientAccessLog` entry.
- **The generated CRUD writes on a status-bearing entity are Super Admin's**, everywhere, on purpose: they are the
  escape hatch that lets Super Admin fix a row the workflow cannot express. They are not a normal screen's write path,
  and the service behind each one also refuses workflow-owned fields with a 409. Do not surface them in the UI.
- There is no `PUT`/`PATCH` on a "status-bearing entity" that any operational role can reach. Everything the application
  does is done through the action routes listed as `POST .../do-something`.

## Everything outside the Phase 1 table

| Path | Methods | Who may reach it |
| --- | --- | --- |
| `/index.html`, `/*.js`, `/*.txt`, `/*.json`, `/*.map`, `/*.css` | all | public |
| `/*.ico`, `/*.png`, `/*.svg`, `/*.webapp`, `/app/**`, `/i18n/**`, `/content/**`, `/swagger-ui/**` | all | public |
| `/api/authenticate` | `POST`, `GET` | public |
| `/api/register` | all | **nobody** — denied outright; accounts are created by Super Admin |
| `/api/activate` | all | public |
| `/api/account/reset-password/init`, `/api/account/reset-password/finish` | all | public |
| `/api/admin/**` | all | `SUPER_ADMIN` |
| `/api/authorities`, `/api/authorities/**` | `GET` | `SUPER_ADMIN` |
| `/api/authorities`, `/api/authorities/**` | everything else | **nobody** — roles are seeded with the application, never written over HTTP |
| `/v3/api-docs/**` | all | `ADMIN` |
| `/management/health`, `/management/health/**`, `/management/info`, `/management/prometheus` | all | public |
| `/management/**` | all | `ADMIN` |
| `/api/**` | all | the catch-all: `ADMIN`, `SUPER_ADMIN`, `RECEPTION`, `NURSE`, `DOCTOR`, `LAB`, `RADIOLOGY`, `PHARMACY`, `FINANCE`, `USER` |

`/api/account` and `/api/account/change-password` have no row of their own and are decided by the catch-all: every
signed-in account may read and change *itself*. That is the intent, not an oversight.

`/api/admin/**` is Super Admin's alone, including account creation and role assignment. Administration does not reach
it. Among those routes is `POST /api/admin/users/{login}/initial-password`, which sets a password on somebody else's
behalf and contradicts the link-only reset rule; **it is queued to be removed or disabled (Group B)** and no screen
should call it.

## Reference data — Super Admin alone (S3.9)

Every one of these reads as wide as it did before and writes as narrowly as possible: prices, codes and clinical
catalogues decide what the hospital charges and what it can do.

| Path | Methods | Who may reach it |
| --- | --- | --- |
| `/api/departments`, `/api/departments/**` | `GET` | `RECEPTION`, `NURSE`, `DOCTOR`, `LAB`, `RADIOLOGY`, `PHARMACY`, `FINANCE`, `ADMIN`, `SUPER_ADMIN` |
| `/api/departments`, `/api/departments/**` | everything else | `SUPER_ADMIN` |
| `/api/diagnoses`, `/api/diagnoses/**` | `GET` | same nine as above |
| `/api/diagnoses`, `/api/diagnoses/**` | everything else | `SUPER_ADMIN` |
| `/api/hospital-services`, `/api/hospital-services/**` | `GET` | same nine as above |
| `/api/hospital-services`, `/api/hospital-services/**` | everything else | `SUPER_ADMIN` |
| `/api/lab-tests`, `/api/lab-tests/**` | `GET` | same nine as above |
| `/api/lab-tests`, `/api/lab-tests/**` | everything else | `SUPER_ADMIN` |
| `/api/radiology-exams`, `/api/radiology-exams/**` | `GET` | same nine as above |
| `/api/radiology-exams`, `/api/radiology-exams/**` | everything else | `SUPER_ADMIN` |

"Same nine as above" is `RECEPTION`, `NURSE`, `DOCTOR`, `LAB`, `RADIOLOGY`, `PHARMACY`, `FINANCE`, `ADMIN`,
`SUPER_ADMIN` — that is, every hospital role except `USER`.

## Patients, encounters and the clinical records

| Path | Methods | Who may reach it |
| --- | --- | --- |
| `/api/patients`, `/api/patients/**` | `GET` | every hospital role (the nine) |
| `/api/patients`, `/api/patients/**` | everything else | `SUPER_ADMIN` — correcting a patient goes through `/api/patient-corrections/*` instead |
| `/api/appointments`, `/api/appointments/**` | `GET` | `RECEPTION`, `NURSE`, `DOCTOR`, `ADMIN`, `SUPER_ADMIN` |
| `/api/appointments`, `/api/appointments/**` | everything else | `RECEPTION`, `ADMIN`, `SUPER_ADMIN` |
| `/api/visits`, `/api/visits/**` | `GET` | every hospital role (the nine) |
| `/api/visits`, `/api/visits/**` | everything else | `SUPER_ADMIN` — admitting goes through `/api/visit-admissions/*/admit` |
| `POST /api/visit-admissions/*/admit` | `POST` | `DOCTOR`, `ADMIN`, `SUPER_ADMIN` |
| `/api/vital-signs`, `/api/vital-signs/**` | all | `NURSE`, `DOCTOR`, `ADMIN`, `SUPER_ADMIN` |
| `/api/consultations`, `/api/consultations/**` | `GET` | `NURSE`, `DOCTOR`, `ADMIN`, `SUPER_ADMIN` |
| `/api/consultations`, `/api/consultations/**` | everything else | `DOCTOR`, `ADMIN`, `SUPER_ADMIN` |
| `/api/diagnostic-orders`, `/api/diagnostic-orders/**` | `GET` | `NURSE`, `DOCTOR`, `ADMIN`, `SUPER_ADMIN` |
| `/api/diagnostic-orders`, `/api/diagnostic-orders/**` | everything else | `SUPER_ADMIN` |
| `/api/results`, `/api/results/**` | `GET` | `DOCTOR`, `ADMIN`, `SUPER_ADMIN` |
| `/api/results`, `/api/results/**` | everything else | `SUPER_ADMIN` |
| `/api/referrals`, `/api/referrals/**` | `GET` | `RECEPTION`, `NURSE`, `DOCTOR`, `ADMIN`, `SUPER_ADMIN` |
| `/api/referrals`, `/api/referrals/**` | everything else | `SUPER_ADMIN` |
| `/api/prescriptions`, `/api/prescriptions/**` | `GET` | `NURSE`, `DOCTOR`, `PHARMACY`, `ADMIN`, `SUPER_ADMIN` |
| `/api/prescriptions`, `/api/prescriptions/**` | everything else | `SUPER_ADMIN` |
| `/api/prescription-lines`, `/api/prescription-lines/**` | `GET` | `NURSE`, `DOCTOR`, `PHARMACY`, `ADMIN`, `SUPER_ADMIN` |
| `/api/prescription-lines`, `/api/prescription-lines/**` | everything else | `SUPER_ADMIN` |
| `/api/drugs`, `/api/drugs/**` | `GET` | `NURSE`, `DOCTOR`, `PHARMACY`, `ADMIN`, `SUPER_ADMIN` |
| `/api/drugs`, `/api/drugs/**` | everything else | `SUPER_ADMIN` — stock moves through `/api/pharmacy-stock/**` |
| `/api/dispenses`, `/api/dispenses/**` | `GET` | `DOCTOR`, `PHARMACY`, `ADMIN`, `SUPER_ADMIN` |
| `/api/dispenses`, `/api/dispenses/**` | everything else | `SUPER_ADMIN` |
| `/api/dispense-lines`, `/api/dispense-lines/**` | `GET` | `DOCTOR`, `PHARMACY`, `ADMIN`, `SUPER_ADMIN` |
| `/api/dispense-lines`, `/api/dispense-lines/**` | everything else | `SUPER_ADMIN` |
| `/api/bills`, `/api/bills/**` | `GET` | `RECEPTION`, `FINANCE`, `ADMIN`, `SUPER_ADMIN` |
| `/api/bills`, `/api/bills/**` | everything else | `SUPER_ADMIN` |
| `/api/bill-line-items`, `/api/bill-line-items/**` | `GET` | `FINANCE`, `ADMIN`, `SUPER_ADMIN` |
| `/api/bill-line-items`, `/api/bill-line-items/**` | everything else | `SUPER_ADMIN` |
| `/api/ad-hoc-charges`, `/api/ad-hoc-charges/**` | all | `FINANCE`, `ADMIN`, `SUPER_ADMIN` — **temporary**: slice 6 builds the reasoned action and closes the raw write |
| `/api/payments`, `/api/payments/**` | `GET` | `RECEPTION`, `FINANCE`, `ADMIN`, `SUPER_ADMIN` |
| `/api/payments`, `/api/payments/**` | everything else | `SUPER_ADMIN` — taking money goes through `POST /api/visit-payments/*/pay` |
| `/api/audit-logs`, `/api/audit-logs/**` | `GET` | `ADMIN`, `SUPER_ADMIN` |
| `/api/audit-logs`, `/api/audit-logs/**` | everything else | **nobody** — the trail is written by the application |
| `/api/patient-access-logs`, `/api/patient-access-logs/**` | `GET` | `ADMIN`, `SUPER_ADMIN` |
| `/api/patient-access-logs`, `/api/patient-access-logs/**` | everything else | **nobody** |

## Action routes

| Path | Methods | Who may reach it |
| --- | --- | --- |
| `POST /api/patient-registration/duplicate-check` | `POST` | `RECEPTION`, `NURSE`, `DOCTOR`, `ADMIN`, `SUPER_ADMIN` |
| `POST /api/patient-registration/register` | `POST` | `RECEPTION`, `ADMIN`, `SUPER_ADMIN` |
| `POST /api/patient-registration/emergency-intake` | `POST` | `NURSE`, `DOCTOR`, `ADMIN`, `SUPER_ADMIN` |
| `GET /api/patient-registration/identity-pending` | `GET` | `RECEPTION`, `ADMIN`, `SUPER_ADMIN` |
| `POST /api/visit-intake/check-in/**` | `POST` | `RECEPTION`, `ADMIN`, `SUPER_ADMIN` |
| `POST /api/visit-intake/open` | `POST` | `RECEPTION`, `NURSE`, `DOCTOR`, `ADMIN`, `SUPER_ADMIN` |
| `POST /api/visit-intake/pharmacy-only` | `POST` | `RECEPTION`, `PHARMACY`, `ADMIN`, `SUPER_ADMIN` |
| `GET /api/queues/vitals` | `GET` | `NURSE`, `DOCTOR`, `ADMIN`, `SUPER_ADMIN` |
| `GET /api/queues/consultation` | `GET` | `DOCTOR`, `NURSE`, `ADMIN`, `SUPER_ADMIN` |
| `GET /api/queues/active` | `GET` | every hospital role (the nine) |
| `/api/visit-triage/**` | all | `NURSE`, `DOCTOR`, `ADMIN`, `SUPER_ADMIN` |
| `/api/visit-consultation/**` | `GET` | `NURSE`, `DOCTOR`, `ADMIN`, `SUPER_ADMIN` |
| `/api/visit-consultation/**` | everything else | `DOCTOR`, `ADMIN`, `SUPER_ADMIN` — clinical writing is the doctor's |
| `GET /api/visit-orders/worklist` | `GET` | `LAB`, `RADIOLOGY`, `DOCTOR`, `ADMIN`, `SUPER_ADMIN` |
| `GET /api/visit-orders/billable/**` | `GET` | `FINANCE`, `ADMIN`, `SUPER_ADMIN` |
| `GET /api/visit-orders/visit/**` | `GET` | `NURSE`, `DOCTOR`, `LAB`, `RADIOLOGY`, `ADMIN`, `SUPER_ADMIN` |
| `POST /api/visit-orders/*/result` | `POST` | `LAB`, `RADIOLOGY`, `ADMIN`, `SUPER_ADMIN` |
| `POST /api/visit-orders/*/place`, `POST /api/visit-orders/*/cancel` | `POST` | `DOCTOR`, `ADMIN`, `SUPER_ADMIN` |
| `PUT /api/patient-corrections/*` | `PUT` | `RECEPTION`, `NURSE`, `DOCTOR`, `ADMIN`, `SUPER_ADMIN` — **which fields** a caller may correct is enforced in the service with a 403, because no endpoint rule can say "this role, but not those fields" |
| `GET /api/record-history/**` | `GET` | `NURSE`, `DOCTOR`, `ADMIN`, `SUPER_ADMIN` — **the same list for every entity**, which is queued to be narrowed: `StaffRecord` and `User` history must be HR and Super Admin only |
| `POST /api/patient-merges` | `POST` | `RECEPTION`, `SUPER_ADMIN` |
| `GET /api/visit-prescriptions/pharmacy-queue` | `GET` | `PHARMACY`, `ADMIN`, `SUPER_ADMIN` |
| `GET /api/visit-prescriptions/billable/**` | `GET` | `FINANCE`, `ADMIN`, `SUPER_ADMIN` |
| `GET /api/visit-prescriptions/visit/**` | `GET` | `NURSE`, `DOCTOR`, `PHARMACY`, `ADMIN`, `SUPER_ADMIN` |
| `POST /api/visit-prescriptions/*/place`, `POST /api/visit-prescriptions/*/cancel` | `POST` | `DOCTOR`, `PHARMACY`, `ADMIN`, `SUPER_ADMIN` |
| `/api/pharmacy-stock/**` | all | `PHARMACY`, `ADMIN`, `SUPER_ADMIN` |
| `GET /api/visit-payments/bill/**` | `GET` | `RECEPTION`, `FINANCE`, `ADMIN`, `SUPER_ADMIN` |
| `POST /api/visit-payments/*/pay` | `POST` | `FINANCE`, `ADMIN`, `SUPER_ADMIN` — the only route that settles a bill, closes the visit and releases medicine |
| `/api/payment-plans`, `/api/payment-plans/**` | `GET` | `FINANCE`, `ADMIN`, `SUPER_ADMIN` |
| `/api/payment-plans`, `/api/payment-plans/**` | everything else | `SUPER_ADMIN` until slice 7 builds the agreement itself |
| `POST /api/pharmacy-dispense/*/dispense` | `POST` | `PHARMACY`, `DOCTOR` — the doctor is on this route only for break-glass, which is scoped to an emergency priority/type and records an override |
| `GET /api/pharmacy-dispense/*/history` | `GET` | `DOCTOR`, `PHARMACY`, `ADMIN`, `SUPER_ADMIN` |
| `GET /api/overrides` | `GET` | `ADMIN`, `SUPER_ADMIN` — the after-the-fact break-glass review |
| `GET /api/visit-referrals/*/letter` | `GET` | `NURSE`, `DOCTOR`, `ADMIN`, `SUPER_ADMIN` |
| `GET /api/visit-referrals/visit/**` | `GET` | `RECEPTION`, `NURSE`, `DOCTOR`, `ADMIN`, `SUPER_ADMIN` |
| `POST /api/visit-referrals/*/create`, `POST /api/visit-referrals/*/email` | `POST` | `DOCTOR`, `ADMIN`, `SUPER_ADMIN` |

There is deliberately **no** route for marking a prescription paid: releasing medicine before the money arrives would
defeat the guarantee the payment gate exists to hold.

## Inpatient (Phase 2)

| Path | Methods | Who may reach it |
| --- | --- | --- |
| `/api/wards`, `/api/wards/**` | `GET` | `RECEPTION`, `NURSE`, `DOCTOR`, `ADMIN`, `SUPER_ADMIN` |
| `/api/wards`, `/api/wards/**` | everything else | `SUPER_ADMIN` |
| `/api/bed-types`, `/api/bed-types/**` | `GET` | `RECEPTION`, `NURSE`, `DOCTOR`, `ADMIN`, `SUPER_ADMIN` |
| `/api/bed-types`, `/api/bed-types/**` | everything else | `SUPER_ADMIN` |
| `/api/ward-covers`, `/api/ward-covers/**` | `GET` | `NURSE`, `DOCTOR`, `ADMIN`, `SUPER_ADMIN` |
| `/api/ward-covers`, `/api/ward-covers/**` | everything else | `SUPER_ADMIN` |
| `/api/staff-records`, `/api/staff-records/**` | all | `HR`, `SUPER_ADMIN` — the staff file is employment data, not clinical data, and the ward and the desk do not read it |
| `/api/beds`, `/api/beds/**` | `GET` | `RECEPTION`, `NURSE`, `DOCTOR`, `ADMIN`, `SUPER_ADMIN` |
| `PUT /api/beds/*/available`, `PUT /api/beds/*/maintenance` | `PUT` | `NURSE`, `ADMIN`, `SUPER_ADMIN` |
| `/api/beds`, `/api/beds/**` | everything else | `SUPER_ADMIN` |
| `/api/bed-availability`, `/api/bed-availability/**` | `GET` | `RECEPTION`, `NURSE`, `DOCTOR`, `ADMIN`, `SUPER_ADMIN` |
| `/api/admissions`, `/api/admissions/**` | `GET` | `NURSE`, `ADMIN`, `SUPER_ADMIN` |
| `PUT /api/admissions/*/bed` | `PUT` | `NURSE`, `ADMIN`, `SUPER_ADMIN` |
| `POST /api/admissions/*/transfers` | `POST` | `NURSE`, `ADMIN`, `SUPER_ADMIN` |
| `/api/admission-transfers`, `/api/admission-transfers/**` | `GET` | `NURSE`, `DOCTOR`, `ADMIN`, `SUPER_ADMIN` |
| `/api/admission-transfers`, `/api/admission-transfers/**` | everything else | `SUPER_ADMIN` |
| `/api/admissions`, `/api/admissions/**` | everything else | `SUPER_ADMIN` |
| `/api/inpatient-worklist`, `/api/inpatient-worklist/**` | `GET` | `NURSE`, `DOCTOR`, `ADMIN`, `SUPER_ADMIN` |
| `POST /api/inpatient-charting/*/vitals`, `POST /api/inpatient-charting/*/vitals/*/corrections` | `POST` | `NURSE`, `ADMIN`, `SUPER_ADMIN` |
| `GET /api/inpatient-charting/*/vitals` | `GET` | `NURSE`, `DOCTOR`, `ADMIN`, `SUPER_ADMIN` |
| `/api/inpatient-vitals`, `/api/inpatient-vitals/**` | `GET` | `NURSE`, `DOCTOR`, `ADMIN`, `SUPER_ADMIN` |
| `/api/inpatient-vitals`, `/api/inpatient-vitals/**` | everything else | `SUPER_ADMIN` |
| `POST /api/inpatient-orders/*/executions` | `POST` | `NURSE`, `ADMIN`, `SUPER_ADMIN` — the ward records what was given |
| `POST /api/inpatient-orders/*` | `POST` | `DOCTOR`, `ADMIN`, `SUPER_ADMIN` — the doctor writes the order |
| `PUT /api/inpatient-orders/*/complete`, `PUT /api/inpatient-orders/*/cancel` | `PUT` | `DOCTOR`, `ADMIN`, `SUPER_ADMIN` |
| `GET /api/inpatient-orders/admission/*` | `GET` | `NURSE`, `DOCTOR`, `ADMIN`, `SUPER_ADMIN` |
| `/api/doctor-orders`, `/api/doctor-orders/**` | `GET` | `NURSE`, `DOCTOR`, `ADMIN`, `SUPER_ADMIN` |
| `/api/doctor-orders`, `/api/doctor-orders/**` | everything else | `SUPER_ADMIN` |
| `/api/order-executions`, `/api/order-executions/**` | `GET` | `NURSE`, `DOCTOR`, `ADMIN`, `SUPER_ADMIN` |
| `/api/order-executions`, `/api/order-executions/**` | everything else | `SUPER_ADMIN` |
| `GET /api/ward-cover-roster/current` | `GET` | `NURSE`, `DOCTOR`, `ADMIN`, `SUPER_ADMIN` |
| `/api/ward-cover-roster`, `/api/ward-cover-roster/**` | all | `SUPER_ADMIN` |

## Notes for the frontend

1. **Hide, do not disable.** A route a role cannot reach answers 403, and a screen that shows a control the role cannot
   use is a screen that teaches staff to expect a failure.
2. `GET /api/account` returns the signed-in account and its `authorities`. That list is the only thing the UI needs to
   decide what to show; do not hard-code a role list in the frontend.
3. `POST /api/authenticate` needs the token from the response body; there is no session, every call carries
   `Authorization: Bearer <id_token>`.
4. The catalogue of every route, generated from the running application, is in `docs/api-endpoints.md`.
