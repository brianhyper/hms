# State — read this first, and only this, when starting work

One screen. What is done, what is next, what is waiting on a person. The phase documents are the spec and the rulings;
read them when you need a specific requirement, not to find out where things are.

## How work is done here

```
mvnw test -Dtest=ClassName          # targeted, while building
mvnw clean verify                   # the real gate: spotless, modernizer, checkstyle, all tests
```
`test` does not run modernizer or checkstyle. The gate is green at `bcdc4d4`.

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
| — | `StaffRecord.nationalId` optional, unique when present; DTO no longer requires it | `2747bff`, `bcdc4d4` |

## Next, in order

1. **`StaffRecordServiceImpl` still requires `nationalId`.** The owed test asserts 201 and gets 400 even with the DTO
   fixed, so one layer deeper refuses it. Fix that, then re-add the test (it is written and was reverted rather than
   committed red — the body must be built by hand, because `aStaffRecord(...)` concatenates JSON and turns a null
   argument into the string `"null"`).
2. **Patient identity rule** — `identityDocumentNumber` required above 19, with an "ID PK" marker so emergency intake
   stays possible. Wire it in the **registration operation**, never in `PatientRegistrationServiceImpl.normalizedDocument`
   (the duplicate check calls that too, and requiring a document there refuses a duplicate *check*).
3. **Ruling 5** — compare references by id in `WorkflowOwnedFields` (plain values or a projection, never an
   uninitialised proxy). One service at a time: re-pointing refused, same id accepted.
4. **Ruling 4** — refuse create-by-hand on the nine services, one at a time, each preceded by confirming a workflow
   create path exists for that role. Update each generated create test.
5. **Ruling 1** — the roster gate on its own: `Shift` owns who is on duty, `WardCover` read-only for one release.
6. **Ruling 2** — S3.5 two-person mechanism for the billing waiver and new-account approval (Administration only,
   requester and approver different people).

## Waiting on a person

- **Break-glass case must be named** before it is built. Proposed: emergency medicine release before the bill is settled.
- **Roster source of truth** — gates `inForce` and Phase 4.
- Confirm `identityDocumentNumber` is the patient field for the age rule.
- Employment Act: are staff employment records retained after termination? (erasure)
- Will this ever run on more than one node? (shared cache versus a stamp read that bypasses it)

## Rules that cost time to learn

1. Read the file before editing it. Never write an edit anchor from memory of a similar file.
2. Never filter compiler output narrowly — keep the `symbol:` line.
3. One entity, one service, one test per slice. No sweeps in one pass.
4. Write the failing test first; it shows how many layers must change.
5. `clean verify` per slice, not at the end.
6. Check a precondition exists before starting (entity, plug-in point, field name).
7. Commit only when its own tests are green; revert rather than commit red.
8. `git add` explicit paths — `endpointAPI.md` is the user's and must not be committed.
