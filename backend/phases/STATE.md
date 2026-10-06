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

**Where the phases stand (2026-10-06).** Phases 1, 2 and 3 are closed — for Phase 3 that means all ten slices S3.0-S3.9
are delivered. Phase 4 has P4.0 (`StaffRecord`) delivered. **Two gates stand in front of Phase 4**: the roster
(`Shift`, item 1) and discharge (item 2). Nothing else waits on a person except the six questions in `phase4.md`
("Questions for the client"), and Phase 3's residue is a backlog rather than an open phase.

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
| — | **Phase 3 closed** — all ten slices S3.0-S3.9 delivered; its residue reclassified as a backlog that holds nothing | `8ac7b9e` |
| P4.1 | roster gate started: `Shift` and `ShiftType`, the `shift` table with its three foreign keys, one person one shift a day | `9cf294a` |
| P4.1 | roster gate step 2: `Shift` CRUD and its access rows — read HR/Admin, every write Super Admin, no delete route, author stamped from the caller | `470a3d6` |
| P4.1 | roster gate step 3: `ShiftDuty` rule, the ward-facing `ShiftViewDTO`, and `GET /api/roster/**` | `24bff91` |

## The roster gate (P4.1) — in progress

`Shift` is the single source of truth for who is on duty (client ruling 1a), and it is the gate for Phase 4. Recorded
step by step so a later session knows where this stopped.

| Step | What | State |
|---|---|---|
| 1 | The model: `Shift` and `ShiftType`, the `shift` table with its three foreign keys, and one person one shift a day | **done** (`9cf294a`) — `ShiftModelIT` proves the day, the person, the ward, the writer, the ward being optional, and the unique rule |
| 2 | CRUD: DTO, mapper, service, resource, and the RBAC rows (`CatchAllCoverageIT` fails the build until the write rows exist) | **done** (`470a3d6`) — the person goes out as an id only, reads stop at HR/Admin, writes are Super Admin, there is no `DELETE`, and the author is stamped from the caller |
| 3 | The roster queries: who is on duty now, and who is covering a ward now (the `inForce` rule moves onto the shift rather than being duplicated) | **done** (`24bff91`) — `ShiftDuty` owns the window, a night shift belongs to the day it started, and the ward-facing view names the person with no field for anything else of theirs |
| 4 | The access rule reads the roster instead of `WardCover` — this is the part that changes a security rule | **coupled to step 5, deliberately.** Switching the rule before the covers are converted points every doctor at an empty table and takes their wards away |
| 5 | Existing `WardCover` rows become shifts, and `WardCover` goes read-only for one release | **this is the blocker, and it needs question 8**: the conversion is not mechanical. `coversTo` is nullable in `WardCover` and every shift has an end; `Shift.staffRecord` is required and reaches a `User` only optionally. An open-ended cover, and a cover held by a doctor with no staff record, have no shift to become — and a dropped cover is dropped access |
| 4 | The access rule reads the roster instead of `WardCover` — this is the part that changes a security rule | not started |
| 5 | Existing `WardCover` rows become shifts, and `WardCover` goes read-only for one release | not started |
| 6 | `WardCover` and `WardCoverage` retired once nothing reads them | not started |

## Next, in order

1. **The roster gate (Ruling 1) — the gate for Phase 4, decided and unbuilt.** `Shift` owns who is on duty when; "who
   is covering this ward now" becomes a query over it; existing `WardCover` rows become shifts and `WardCover` goes
   read-only for one release; the `inForce` rule moves onto the shift. Nothing blocks it: the source-of-truth question
   was answered on 2026-10-06 (option 1, client ruling 1a), and shift-type values can be added later without a
   migration because the type is stored as a string.
2. **Discharge, with outcomes (Phase 2 slice 9) — the second gate.** There is no discharge route in the application
   today: `VisitStatusService.onDischarged` is called by a test and by nothing else, and the columns it writes exist
   with nothing that reaches them. It is also what unblocks S3.5's real plug-in point.
3. **Stay billing (Phase 2 slice 6)** — daily bed-day charges with timezone, idempotency and catch-up, the doctor's
   round, and an `ADHOC` line from `AdHocCharge` so a charge Finance records reaches the bill. Build it with the rates
   as catalogue data: the client's real rates change rows, not code.
4. **Ruling 4** — refuse create-by-hand on the nine services, one at a time, each preceded by confirming a workflow
   create path exists for that role. Update each generated create test.
5. **Group C3 — the value-guard layer**, the status-bearing entities the rest of Ruling 5 named, plus one structural
   test: a reference field on a guarded entity must be classified fixed-after-create or free, and fails the build if it
   is neither. **Ruling 5's service-by-service work is stopped** — the money link is guarded (`588201a`) and
   `Admission`/`Bed` compare by id already. What C3 inherits: `Dispense` (its prescription), `OrderExecution` (its
   order), `BillLineItem` (its bill), `DiagnosticOrder` and `Prescription` (their visit and doctor), `DoctorOrder` (its
   admission).
6. **Ruling 2** — S3.5 two-person mechanism for the billing waiver and new-account approval (Administration only,
   requester and approver different people).
7. **Phase 4 P4.2-P4.4** — `LeaveRequest`, `StaffRecordNote` and `PayrollEntry`, which wait on the client answers in
   `phase4.md`; P4.1 is the roster in item 1.
8. **Handoff gap — `Patient` still has the raw update** its own correction route exists to replace: `PUT
   /api/patients/{id}` can change a name, an allergy or a date of birth with no reason and no record. Super-Admin-only,
   but it is the one path that can still undo the identity rule (see `phase3.md` handoff item 3).
9. **Handoff gap — `VitalSigns` corrections edit in place**: the superseded reading survives only as the audit
   `oldValue`. `InpatientVitals` does it properly, with a new row and a `corrects` reference.
10. **Handoff gap — the reset key is stored in clear text**, and an expired, spent or unknown reset link answers **500**
    instead of 400.
11. **Handoff gap — reset links live for one day**; minutes to hours is right for a token that takes over an account.
12. **Group B — remove or disable `POST /api/admin/users/{login}/initial-password`.** It sets a password on somebody
    else's behalf and contradicts the link-only rule S3.2 settled on: an account is handed a link, and only its owner
    ever chooses its password.
13. **`GET /api/record-history/{entityName}/{entityId}` is unscoped.** One route for every entity, admitted by one row
    (`NURSE`, `DOCTOR`, `ADMIN`, `SUPER_ADMIN`), and `AuditLogServiceImpl.trail` filters by entity name and id only - so
    every clinical role can read a `StaffRecord`'s history and a `User`'s history. Both must be HR and Super Admin only.
    Needs per-entity scoping in the service (an endpoint rule cannot express it: the entity is a path variable).

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
