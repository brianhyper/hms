# State — read this first, and only this, when starting work

One screen. What is done, what is next, what is waiting on a person. The phase documents are the spec and the rulings;
read them when you need a specific requirement, not to find out where things are.

## How work is done here

```
mvnw test -Dtest=ClassName          # targeted, while building
mvnw clean verify                   # the real gate: spotless, modernizer, checkstyle, all tests
```
`test` does not run modernizer or checkstyle. The gate is green at `a4bdb5f` (1038 tests, 0 failures).

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

## Next, in order

1. **Patient identity rule** — `identityDocumentNumber` required above 19, with an "ID PK" marker so emergency intake
   stays possible. Field confirmed for patients only (see the client rulings in `phase3.md`). Wire it in the
   **registration operation**, never in `PatientRegistrationServiceImpl.normalizedDocument` (the duplicate check calls
   that too, and requiring a document there refuses a duplicate *check*).
2. **Break-glass — emergency medicine release before the bill is settled** (named 2026-10-06). Pharmacist or doctor at
   the point of care, an existing doctor's prescription, a mandatory reason, its own audit event, the bill stays
   OUTSTANDING, reviewed afterwards by Administration; scope is emergency-triaged visits and admitted patients
   (confirm with the client). **Checked first:** the outpatient dispense gate exists
   (`DispenseWorkflowServiceImpl.dispense` refuses `prescriptionNotReadyForDispense` until `READY_FOR_DISPENSE`,
   reached only after payment), but **inpatients already bypass it** via `initialStatusForInpatient()` — so the missing
   piece is the audited override, not the release. This is S3.5's first **existing** plug-in point.
3. **Ruling 5** — compare references by id in `WorkflowOwnedFields` (plain values or a projection, never an
   uninitialised proxy). One service at a time: re-pointing refused, same id accepted.
4. **Ruling 4** — refuse create-by-hand on the nine services, one at a time, each preceded by confirming a workflow
   create path exists for that role. Update each generated create test.
5. **Ruling 1** — the roster gate on its own: `Shift` owns who is on duty, `WardCover` read-only for one release.
   Now answered (client ruling 1a): the roster is the single source of truth.
6. **Ruling 2** — S3.5 two-person mechanism for the billing waiver and new-account approval (Administration only,
   requester and approver different people).

## Waiting on a person

- **Verify Article 43(2) of the Constitution** protects emergency treatment, with the client or a lawyer, before the
  break-glass design leans on it.
- **Confirm the break-glass scope** with the client: emergency-triaged visits and admitted patients only.
- **Employment Act retention period** — read section 10(6) and (7). Five years is believed, not confirmed, and the
  citation is a party's submission, not a ruling. Until then do not build staff erasure: a terminated record stays,
  HR and Super Admin only (handoff gap).

## Rules that cost time to learn

1. Read the file before editing it. Never write an edit anchor from memory of a similar file.
2. Never filter compiler output narrowly — keep the `symbol:` line.
3. One entity, one service, one test per slice. No sweeps in one pass.
4. Write the failing test first; it shows how many layers must change.
5. `clean verify` per slice, not at the end.
6. Check a precondition exists before starting (entity, plug-in point, field name).
7. Commit only when its own tests are green; revert rather than commit red.
8. `git add` explicit paths — `endpointAPI.md` is the user's and must not be committed.
