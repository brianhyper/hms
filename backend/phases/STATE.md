# State — read this first, and only this, when starting work

One screen. What is done, what is next, what is waiting on a person. The phase documents are the spec and the rulings;
read them when you need a specific requirement, not to find out where things are.

## How work is done here

```
mvnw test -Dtest=ClassName          # targeted, while building
mvnw clean verify                   # the real gate: spotless, modernizer, checkstyle, all tests
```
`test` does not run modernizer or checkstyle. The gate is green at `dc55537` (0 failures).

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

## Next, in order

1. **Break-glass wiring — release medicine before the bill is settled.** The mechanism is built and committed
   (`a4c425a`, S3.5). What remains is the caller: the dispensing gate (`DispenseWorkflowServiceImpl.dispense`),
   invoked by the **pharmacist or doctor at the point of care** for an existing doctor's prescription, leaving the bill
   OUTSTANDING and reviewed afterwards by Administration. **Blocked on** the confirmed scope (emergency-triaged visits
   and admitted patients) and the Article 43(2) check. Note: inpatients already bypass the gate
   (`initialStatusForInpatient()`), so the genuinely blocked path is the **emergency-triaged outpatient**.
2. **Ruling 5 (continued)** — apply the id-based reference guard to the other services that own a reference, one
   at a time: `WardCover` (ward, doctor), then the admission/bed guards. `changed` is untouched; the new
   `WorkflowOwnedFields.referenceChanged` compares plain ids, never a proxy. Re-pointing refused, same id accepted.
3. **Ruling 4** — refuse create-by-hand on the nine services, one at a time, each preceded by confirming a workflow
   create path exists for that role. Update each generated create test.
4. **Ruling 1** — the roster gate on its own: `Shift` owns who is on duty, `WardCover` read-only for one release.
   Now answered (client ruling 1a): the roster is the single source of truth.
5. **Ruling 2** — S3.5 two-person mechanism for the billing waiver and new-account approval (Administration only,
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
