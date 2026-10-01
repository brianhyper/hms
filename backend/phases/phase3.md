### Phase 3 — Admin/Security

The Admin/Security phase defines the hospital's fixed role model, account lifecycle, authentication, auditability, patient-record access tracking, and controlled override mechanisms. The system will use nine fixed roles: **Doctor, Nurse, Administration, Reception, Super Admin, Lab, Pharmacy, Finance, and HR**. Permissions will be hard-coded in the backend through a defined RBAC matrix; there will be no Super Admin-editable permission engine. Authorization must be enforced at the backend service/API layer for every protected operation, regardless of whether the corresponding frontend controls are visible or hidden.

The system will maintain a strict distinction between **Administration** and **Super Admin**. Administration is the hospital's operational administration role and is responsible for functions such as bed and ward assignment, ward transfers, reassignment of a patient's `primaryDoctor`, the billing-gate override where permitted, and operational reports. Administration has view-only access to the audit trail and patient-record access logs. Super Admin is the system/IT administration role and is responsible for user account creation and deactivation, role assignment, and management of system reference data including diagnoses, drug, laboratory, radiology and hospital-service catalogues, and `bedType` tags. Super Admin has full read access to the audit trail and patient-record access logs. Super Admin is the only role permitted to create user accounts, including accounts for other Super Admins.

`User` will remain separate from `StaffRecord`. `User` contains authentication and account information such as login credentials, assigned role, and active/inactive status, while `StaffRecord` contains HR information such as name, department, employment details and rostering data. The two are linked through a foreign key. This allows HR to create a staff record before a system account is provisioned and allows hospital employees such as cleaners or security staff to have an HR record without requiring login access. User accounts will never be deleted; they will be deactivated so historical ownership and audit records remain intact. The system will maintain one non-deletable seed Super Admin account to prevent complete administrative lockout. In addition, the system must prevent deactivation of the last active Super Admin, and a Super Admin must not be able to remove or downgrade their own Super Admin role.

Authentication will require a **forced password change on first login**. Password recovery will use an email-based reset link through the existing email infrastructure, with no secret questions. Password-reset and first-login tokens must expire after a fixed period, be usable only once, and be invalidated after successful password change. The system will enforce a fixed failed-login lockout threshold, with locked accounts requiring manual Super Admin action before access is restored, together with a standard idle-session timeout. Deactivation of a user must also invalidate that user's ability to continue accessing the system through existing authenticated sessions or tokens rather than allowing access to continue until normal token expiry. Password changes, resets, lockouts, account activation/deactivation and other sensitive account operations will be auditable.

The system will maintain a strictly **append-only audit trail** for all writes and sensitive state-changing operations, including create, edit, void, correction, override, discharge, role changes and other defined security-sensitive actions. Audit records will capture the acting user, action type, affected entity and record, timestamp, and the relevant before/after values or mandatory reason where applicable. Audit entries must not be editable or deletable, including by Super Admin. The audit model should record successfully committed sensitive actions rather than treating a failed authorization or validation attempt as a completed business action; security-event logging for failed or suspicious attempts may exist separately where required. Role changes must specifically record the previous and new role. Administration receives view-only access to audit information, while Super Admin receives full read access.

A separate **`PatientAccessLog`** entity will record access to patient clinical records. It will contain the patient, authenticated user, action type such as `VIEW` or `EDIT`, and timestamp. Patient access logging will operate at chart-open level rather than logging every field, tab, export or individual read operation. A successful opening of a patient's record creates the appropriate access entry, while unauthorized, failed or unsuccessful requests do not create misleading patient-access entries. `PatientAccessLog` will not expose normal unrestricted CRUD operations because its records are security history. Super Admin has full read access, Administration has view-only access, and the log is not surfaced to other roles.

The hospital will use a standardized **override/emergency-access mechanism** rather than implementing bespoke override rules independently in each domain. Any authorized override must identify the actor and their role, require a mandatory reason, perform the appropriate backend authorization and validation, and create an audit entry recording the override and its justification. The existing billing-gate override will plug into this mechanism rather than introducing a separate implementation.

The Phase 3 security model also establishes a general **domain-operation security rule** for the entire HMS. Any entity whose state transition has clinical, financial, inventory, admission, bed-management or other downstream business consequences must not expose unrestricted generated CRUD updates that can bypass business rules. Such transitions must use dedicated backend domain operations that enforce authorization, validation, required side effects and audit requirements. Generic generated CRUD endpoints must therefore be restricted or disabled wherever they could bypass those domain operations. This specifically covers the currently identified admission vulnerability where generated `PUT /api/admissions/{id}` can directly change an admission to `DISCHARGED`; until the proper discharge operation exists, this raw write is restricted to Super Admin only, and once the real discharge endpoint is implemented, the generic state-changing write will be disabled for all users. The real discharge operation must enforce sign-off, the billing gate, bed release and visit closure as one controlled workflow.

The previously identified pharmacy security correction remains part of this phase: `/api/pharmacy-dispense/*/dispense` is restricted to **PHARMACY-only** access, while execution of recurring drug orders by authorized clinical users such as nurses performs the required stock deduction internally through the server-side order-execution workflow rather than exposing or calling the pharmacy dispense endpoint directly. The `/api/payment-plans` endpoint must also be explicitly assigned an RBAC rule because it currently falls through the open `/api/**` catch-all; its protection is required regardless of when the related billing-gate functionality is completed.

Super Admin-managed reference data must also preserve historical integrity. Changes to reference data such as drugs, diagnoses, laboratory tests, radiology exams, services or bed-type metadata must not rewrite the historical meaning of existing clinical or financial transactions. Transactional records must retain the relevant historical value through the appropriate snapshot or historical-data mechanism rather than relying on a mutable current reference value.

With these controls, Phase 3 establishes the complete administrative and security boundary for the HMS: fixed backend-enforced roles, separated operational and system administration, controlled user-account lifecycle, secure authentication and recovery, append-only auditability, patient-record access logging, standardized overrides, protection against generated-CRUD bypasses, and secure handling of reference data and sensitive endpoints. **Phase 3 — Admin/Security is therefore considered closed and locked**, subject only to implementation details that do not alter the agreed security model.

---

## Slice plan and status (engineering, 2026-09-30)

Phase 3 is delivered in slices, each implemented, verified with `mvnw verify`, committed and pushed on its own — the same discipline as Phases 1 and 2. Numbering starts at S3.0 because it is the groundwork the rest is checked against.

### Already satisfied when the phase started

| Requirement | Where |
|---|---|
| `/api/pharmacy-dispense/*/dispense` is PHARMACY-only | RBAC row, held by `DoctorOrderIT.theWardCannotReachThePharmacyCounterItself` |
| A recurring drug order's execution deducts stock server-side, without that endpoint | `DoctorOrderWorkflowServiceImpl.takeOffTheShelf` → `DispenseWorkflowService.dispense`, in the executing transaction (`ee1cd13`) |
| `/api/payment-plans` has an explicit RBAC rule | reads FINANCE/ADMIN/SUPER_ADMIN, writes SUPER_ADMIN (`4a6b409`) |
| The admission raw write cannot change state | `AdmissionServiceImpl` refuses the create and the lifecycle columns on PUT/PATCH (`4a6b409`) — stronger than "restricted to Super Admin", so the discharge operation can land without revisiting it |
| The domain-operation rule, applied where it was identified | `AdmissionServiceImpl`, `BedServiceImpl` (`27bd4cf`) |
| Audit entries are not editable or deletable over HTTP | `AuditLogResource` exposes GET only |
| Money-bearing transactions copy their reference values | `BillLineItem.description`/`amount` and `DiagnosticOrder.testName` are copies |

### Slices

| Slice | Delivers | Depends on | Status (2026-09-30) |
|---|---|---|---|
| S3.0 | The nine-role model as constants (+ `ROLE_HR`), user and role management moved to Super Admin only, this plan | — | **delivered** (`16ebdcc`) |
| S3.1 | Account lifecycle: users are never deleted, the last active Super Admin cannot be deactivated, a Super Admin cannot downgrade themselves, and account/role changes are audited with the previous and new role | S3.0 | **delivered** (`55495bf`) |
| S3.2 | Authentication: forced password change on first login, one-time expiring reset tokens, failed-login lockout needing manual release, idle session timeout, and deactivation that actually ends existing tokens | S3.1 | **part: revocation, lockout and idle timeout delivered**; forced first-login change and one-time reset tokens **open** |
| S3.3 | Audit hardening: audit read rows GET-only, action constants for the account and security events | S3.1 | **delivered** — the read is named as a read and writes are refused outright (`7d75e39`) |
| S3.4 | `PatientAccessLog`: chart-open access logging, no CRUD, Administration view-only and Super Admin full read | S3.0 | not started |
| S3.5 | The standard override/emergency-access mechanism (actor, role, mandatory reason, audit entry) that the billing gate plugs into | S3.3 | not started |
| S3.6 | The domain-operation guard applied to every remaining generated CRUD that can still overwrite a status or an amount by hand — including `BillLineItem.amount`, which is editable by FINANCE today | S3.0 | **part: bill lines delivered** (`691e1fa`); nine services open |
| S3.7 | Historical integrity where it is still missing: drug name, unit, price **and classification** at the time, on prescription and dispense lines (the money side is already snapshotted by `BillLineItem`) | S3.6 | not started |
| S3.8 | `StaffRecord` (HR data, optional link to a `User`, no login required) and the HR role's own access | S3.1 | **delivered** — it is Phase 4's P4.0, built when Phase 4 started |
| S3.9 | Reference-data management closed to Super Admin, and a structural test that no state-changing route falls through to the `/api/**` catch-all | S3.6 | **delivered** — the catch-all test (`483f037`), its two holes closed, and the five reference-data write rows that still admitted ADMIN closed to Super Admin (`7d75e39`) |

### Rulings (client and engineering, 2026-10-01)

These change the plan or reverse a decision already taken, so they are recorded here rather than left in a
conversation. Each one names what it overrides.

1. **The lockout gets an auto-release, after 15 minutes** — overriding the original "no timer" rule. The client set
   that rule without the consequence on the table: five wrong passwords lock any named account, only a Super Admin
   could release it, and nothing expired, so a script could lock every known login and shut the hospital out. The
   audit row stays, the manual release stays, and HR is to be told in one sentence before this ships. A per-IP cap
   is not the answer: the whole hospital sits behind one address.
2. **Revocation freshness: investigated, and the cache suspicion refuted** (`7d75e39`). `UserService` evicts
   `usersByLogin` on update, so the control reads a fresh account by design. `SessionRevocationCacheIT` now locks
   that in.
3. **Reactivation is fixed, and the `@Disabled` test is gone** (`7d75e39`). The cause was a token's `iat` being
   whole seconds against a microsecond stamp, so a sign-in in the same second as the revocation compared as older
   than it. Nothing in the suite is disabled any more (`Skipped: 0`).
4. **The idle timeout is per account, not per session, and that is accepted for v1.0.** It bounds an unattended
   workstation, not a stolen token; the bound on a stolen token is the token's lifetime.
5. **Two 401 shapes are to be handed to the frontend developer now**, so one handler covers both.
6. **Staff records cannot be deleted, and erasure is deferred.** The likely shape is a Super Admin anonymise action
   that blanks the identity fields and keeps the row, because a deleted record breaks audit and payroll references.
   Whether the Employment Act requires keeping employment records after termination needs confirming before
   anything is promised to anyone. Recorded as a handoff gap.
7. **`/api/register` is to be disabled** once nothing legitimate calls it, and the not-activated answer made the
   same generic refusal as everything else — it currently confirms to an anonymous caller that the account exists.
8. **Real sign-in tests are a standing requirement, not a one-off.** The suite authenticates with
   `@WithMockUser` almost everywhere, which is what let a broken account lookup ship unnoticed.
9. **The lock that outlived its test transaction stays unexplained**, and is recorded as such so nobody invents a
   mechanism for it.

**A slice counts as delivered only when it is verified with `mvnw verify` and committed.** Anything else is
planned work, however finished it reads. This column exists because the table was read as a delivery list once
already, and S3.2 was recorded as implemented when only its revocation half was.

**A defect the lockout work exposed, fixed with it.** `DaoAuthenticationProvider` wraps anything that
`loadUserByUsername` throws into `InternalAuthenticationServiceException`, and the single place this app decides
401 is `ExceptionTranslator.getMappedStatus`, which recognised `BadCredentialsException` alone. So **a locked
account and an account that was never activated both answered 500 rather than 401** — the inactive-account one
in shipped code, unnoticed because the whole suite signs in with `@WithMockUser` and never
reaches the real login path. `getMappedStatus` now looks through that wrap for those two refusals only, so a
genuine failure to reach the database is still reported as the server error it is. `AccountLockoutIT` is the
only test that signs in for real, and it asserts that the answer for a locked account is byte-identical to the
answer for a wrong password — the non-disclosure decision in `DomainUserDetailsService`, made testable.

**The idle timeout sits on the same filter, and had one trap in it worth recording.** The time of the last request
is remembered on the account rather than in memory, because a memory copy answers for one node and is lost on
deploy. But the account the revocation check loads is served from the `usersByLogin` cache, and a cached copy of a
value written on requests would be a value that never moved — an idle timeout that never fires, and an active
session refused once its token was old enough. So that one value is read and written straight against the row,
at most once per thirty seconds rather than once per request. The token's own issue time is a floor under the
recorded activity, so signing in again after an absence starts fresh instead of being refused on its first use by
the previous session's age. Activity is per account rather than per token, so two concurrent sessions for one
account share it — using one keeps the other alive; per-session tracking would need the token registry this
design avoids.

**Left as a decision, not taken silently:** the answer for a never-activated account still says so, which
confirms to an anonymous caller that the account exists. That is JHipster's original wording and it tells a
genuine activation-pending user why they cannot sign in, but it is the same class of disclosure that was
rejected for the lock.

### Open questions

Each blocks the slice it is listed against, and no earlier one.

1. **`ROLE_RADIOLOGY` is not in the Phase 3 role list.** The list names nine roles and Radiology is not one of them, yet the code has `ROLE_RADIOLOGY` with its own queues and RBAC rows, and `ROLE_LAB` is separate. Does Lab cover radiology (nine roles, Radiology's rows folded into Lab), or is the list short a tenth role? S3.0 keeps both, so nothing breaks either way.
2. **Is `ROLE_ADMIN` the "Administration" role of this model?** Read as yes: the operational role, distinct from Super Admin. S3.0 assumes yes, and moves account and role management to Super Admin only as the phase requires.
3. **`StaffRecord` fields.** "Name, department, employment details and rostering data" needs to be concrete: which are required, is department a `Department` record or free text, and what does a rostering entry hold? S3.8 cannot start without it.
4. **Lockout and session numbers** — failed attempts before lockout, how long an account stays locked, and the idle-session timeout. S3.2 cannot start without them.
5. **How deactivation ends an existing token.** With stateless JWTs and no revocation list, "deactivation ends existing access" needs a mechanism. The cheapest that fits the current design is a `credentialsChangedAt` stamp carried as a claim and checked against a cached lookup; a token registry is the heavier alternative. This is a design decision, not a detail.
6. **Who may use the override mechanism, and does it need a second approver?** The phase requires an override to identify its actor and role and carry a mandatory reason, but not who is authorized to make one. S3.5 cannot start without it.

