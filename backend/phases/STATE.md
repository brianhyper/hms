# State — read this first, and only this, when starting work

One screen. What is done, what is next, what is waiting on a person. The phase documents are the spec and the rulings;
read them when you need a specific requirement, not to find out where things are.

## How work is done here

```
mvnw test -Dtest=ClassName          # targeted, while building
mvnw clean verify                   # the real gate: spotless, modernizer, checkstyle, all tests
```
`test` does not run modernizer or checkstyle. The gate is green at `63e6372` (0 failures).

**One slice per session.** A fresh conversation is cheap; a long one is not. Keep the state here, not in the thread.

## Done

| Slice | What | Commit |
|---|---|---|
| S3.3 | audit read rows GET-only | `7d75e39` |
| S3.4 | `PatientAccessLog` — chart-open logging, read-only route, Admin + Super Admin | `b3c98c6` |
| S3.6 | the nine generated CRUDs refuse hand-written state and money edits | `6416e58`, `e5b6992`, `a9c80bd`, `f340471` |
| S3.7 | drug name/unit/price/classification recorded on prescription and dispense lines, and read back | `ab0c42f`, `28a03cc`, `a9bf047`, `a0e4aea` |
| S3.2 | forced first-login change, one-time reset tokens under a row lock, admin initial-password | `a8fa154`, `78926c3`, `94d6e9c` |
| S3.4+ | patient delete refused for every role; access log records `EDIT` | `df59020`, `639198c` |
| — | `StaffRecord.nationalId` optional, unique when present; entity, DTO and a file with no number all verified | `2747bff`, `bcdc4d4`, `a4bdb5f` |
| — | Patient identity: required above 19 at registration, explicit `PENDING` marker, pending worklist, correction completes it | `2763195` |
| S3.5 | override/emergency-access mechanism: own audit event, mandatory reason, acting role, one-query review; no caller yet | `a4c425a` |
| Ruling 5 | id-based reference guard in `WorkflowOwnedFields`; applied to `PrescriptionLine` (drug, prescription) | `dc55537` |
| — | Break-glass release wired to the dispensing gate: scope, invoker, mandatory reason, own audit event, Administration review route | `5e2b5fa` |
| Ruling 5 | id-based reference guard applied to `WardCover` (ward, doctor, assignedBy) | `b347ff1` |
| Ruling 5 | admission/bed reference refusals covered by tests; their guards were already id-based | `da80137` |
| Ruling 5 | id-based reference guard on the money link: `Bill.payment` cannot be re-pointed by hand (a bill's `payment` is the side that owns the foreign key) | `588201a` |
| — | `/api/authorities` reads as Super Admin; POST and DELETE denied for every role, Super Admin included | `efe30c0` |
| — | route catalogue and role-by-route matrix committed under `docs/` | `63e6372` |

## Next, in order

1. **Ruling 5 (continued) — stopped by ruling, 2026-10-06.** The money case is done and it is the one that mattered:
   the link lives on `Bill.payment`, which owns the foreign key, and it is guarded by id (`588201a`).
   `Payment.bill` is the inverse side (`mappedBy`) and has nothing to re-point, so `Payment`'s service needed no
   reference guard of its own. The remaining services still exclude their references from the field-name guard, so a
   hand-written update can re-point them: `Dispense` (its prescription), `OrderExecution` (its order), `BillLineItem`
   (its bill), `DiagnosticOrder` and `Prescription` (their visit and doctor), and `DoctorOrder` (its admission).
   **Do not work through these one at a time** — they fold into Group C3, item 9. `Admission`/`Bed` compare by id
   already and are closed as well (`phase4.md`).
2. **Ruling 4** — refuse create-by-hand on the nine services, one at a time, each preceded by confirming a workflow
   create path exists for that role. Update each generated create test.
3. **Ruling 1** — the roster gate on its own: `Shift` owns who is on duty, `WardCover` read-only for one release.
   Now answered (client ruling 1a): the roster is the single source of truth.
4. **Ruling 2** — S3.5 two-person mechanism for the billing waiver and new-account approval (Administration only,
   requester and approver different people).
5. **Handoff gap — `Patient` still has the raw update** its own correction route exists to replace: `PUT /api/patients/{id}`
   can change a name, an allergy or a date of birth with no reason and no record. Super-Admin-only, but it is the one
   path that can still undo the identity rule (see `phase3.md` handoff item 3).
6. **Handoff gap — `VitalSigns` corrections edit in place**: the superseded reading survives only as the audit
   `oldValue`. `InpatientVitals` does it properly, with a new row and a `corrects` reference.
7. **Handoff gap — the reset key is stored in clear text**, and an expired, spent or unknown reset link answers **500**
   instead of 400.
8. **Handoff gap — reset links live for one day**; minutes to hours is right for a token that takes over an account.
9. **Group C3 — the value-guard layer**, which already covers the status-bearing entities the rest of Ruling 5 named.
   It gets **one structural test**, not a second mechanism: a reference field on a guarded entity must be classified
   either fixed-after-create or free, and the build fails if it is neither. That is the whole of the reference-guard
   question; `WorkflowOwnedFields.referenceChanged` plus a scalar projection is the mechanism it tests.
10. **Group B — remove or disable `POST /api/admin/users/{login}/initial-password`.** It sets a password on somebody
    else's behalf and contradicts the link-only rule S3.2 settled on: an account is handed a link, and only the person
    who owns it ever chooses its password. Found by reading the route catalogue.
11. **`GET /api/record-history/{entityName}/{entityId}` is unscoped.** It is one route for every entity, admitted by
    one row (`NURSE`, `DOCTOR`, `ADMIN`, `SUPER_ADMIN`), and `AuditLogServiceImpl.trail` filters by entity name and id
    only - so every clinical role can read a `StaffRecord`'s history and a `User`'s history. Staff-record history is
    employment data and account history is security data, and both must be HR and Super Admin only. Needs per-entity
    scoping in the service (an endpoint rule cannot express it: the entity is a path variable).

## Reports asked for, and their answers

- **Exposure of the eight guarded services' PUT/PATCH** (Ruling 5): all eight are **Super Admin only**. Every
  status-bearing entity has a method-agnostic `hasAnyAuthority(SUPER_ADMIN)` row over its raw CRUD, so the only caller
  who could re-point a reference by hand was Super Admin, reaching for the documented escape hatch. That is why the
  reference guard is still worth having - it is what makes the escape hatch safe - but the exposure was never a
  routine path.
- **Do the two test controllers ship?** No. `ExceptionTranslatorTestController` and `WebConfigurerTestController` are
  both already under `src/test/java` (`web/rest/errors/` and `config/`), so they are not in the packaged jar. They
  appear in the route catalogue because it was dumped from the running application during tests. No change needed.
- **Who may read each entity's history?** The same four roles for every entity - `NURSE`, `DOCTOR`, `ADMIN`,
  `SUPER_ADMIN` - which is the item 11 gap.

## Waiting on a person

- Nothing blocks the queue. **Article 43(2)** was accepted (proceed), the **break-glass scope** was confirmed, and the
  **Employment Act retention** was decided **not to be implemented** — a terminated record stays, HR and Super Admin
  only, recorded as a handoff gap. The last two are written into `phase3.md`'s client rulings.

## Rules that cost time to learn

1. Read the file before editing it. Never write an edit anchor from memory of a similar file.
2. Never filter compiler output narrowly — keep the `symbol:` line.
3. One entity, one service, one test per slice. No sweeps in one pass.
4. Write the failing test first; it shows how many layers must change.
5. `clean verify` per slice, not at the end.
6. Check a precondition exists before starting (entity, plug-in point, field name).
7. Commit only when its own tests are green; revert rather than commit red.
8. `git add` explicit paths, always — never `git add -A`. `docs/api-endpoints.md` is the one generated file that is
   committed; the catalogue is regenerated after each group.
