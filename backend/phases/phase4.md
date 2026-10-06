Staff Records

Already exists from the Admin/Security pass — StaffRecord (name, national ID unique, department FK to Department entity, job title, contact, start date, status ACTIVE/ON_LEAVE/TERMINATED).

Payroll (v1.0 — simple record-keeper)
PayrollEntry: staffRecord (FK), period, grossAmount, deductions (free text — Finance does the tax math outside the system), netAmount, paidBy, paidDate, status PENDING/PAID.
Owned by Finance, not HR — matches "whoever owns the money touches the money."
Single Finance action, no approval chain, audited.
Full statutory calculation (PAYE, NSSF, SHIF, AHL) deferred to v1.1 — flagged for the pitch as "integrate with existing payroll software."
Shift/Rostering
Shift: staffRecord (FK), ward (FK, optional), date, shiftType, startTime, endTime, createdBy.
Flat, one row per date — HR schedules manually, no recurring weekly pattern.
This is what finally makes the inpatient "doctor on duty in current ward" access rule real instead of theoretical.
Leave Tracking

Grounded in Kenya's Employment Act 2007 leave categories:

Type	Entitlement
ANNUAL	21 working days/yr after 12mo service
SICK	full-pay/half-pay split — confirm exact day count against the current Act, it's shifted across amendments
MATERNITY	3 months full pay
PATERNITY	2 weeks full pay
COMPASSIONATE	discretionary, may be unpaid/deducted from annual
UNPAID	catch-all
LeaveRequest: staffRecord (FK), leaveType, startDate, endDate, daysRequested (derived), reason, status PENDING/APPROVED/REJECTED/CANCELLED, requestedAt, approvedBy, approvedAt, decisionNote.
HR approves, single-step, audited.
Balance: fixed annual entitlement per LeaveType (Super Admin configurable) minus sum of APPROVED days this year. No monthly accrual, no 18-month carryforward — deferred to v1.1 if the client needs it.
Performance/Disciplinary
StaffRecordNote: staffRecord (FK), type PERFORMANCE/DISCIPLINARY, date, recordedBy (HR), note, audited.
Plain searchable log, not a structured review system — no ratings, templates, or escalation tiers.

Phase 1 (Outpatient), Phase 2 (Inpatient) and **Phase 3 (Admin/Security) are closed** — every Phase 3 slice S3.0-S3.9 is
delivered. Phase 4 has **P4.0 (`StaffRecord`) delivered**; P4.1-P4.4 are to build.

**What is genuinely left before the spec is complete for handoff**, verified against the code on 2026-10-06, not read
off this document:

| Left | Where it stands | Waits on |
|---|---|---|
| **Discharge and its outcomes** (Phase 2 slice 9) | **Not built.** No discharge route exists anywhere in `web/rest`; no outcome type; `VisitStatusService.onDischarged` is called by a test and by nothing else, and `dischargedAt`/`dischargeNote`/`dischargedByDoctor`/`dischargedByNurse` are columns nothing reaches | Nothing — and it is the second gate, because it is S3.5's missing plug-in point |
| **Stay billing: daily bed-day charges** (Phase 2 slice 6) | **Not built.** `BillLineSourceType` is still `CONSULTATION, LAB, RADIOLOGY, PHARMACY` — no `BED_DAY`, no `DOCTOR_ROUND`, no `ADHOC` — and there is no daily-charge code at all | Real client rates. Build it anyway: the rates are catalogue rows, so the missing rates change data, not code |
| **Doctor's daily round**, and the round's clinical note (Inpatient gap #1) | Not built | Question 3 below, plus rates |
| **`AdHocCharge` has no `Bill`/`Visit` FK** | Still open: the entity carries `admission`, `serviceCatalogue`, `addedBy`, `voidedBy` and nothing else, so a charge Finance records never reaches a bill | Nothing |
| **The payment-plan agreement** (Phase 2 slice 7) | `PaymentPlan` exists as a table; its RBAC row still says writes are Super Admin "until slice 7 builds the agreement itself" | Nothing |
| **The value-guard layer** | **Not "not started" — it is S3.6, which is delivered.** What is left of it is Ruling 4 (create-by-hand) and the reference guards, both folded into Group C3 | Nothing |
| **Phase 4 P4.1-P4.4** | Not started | Questions 4-6 below; P4.1 additionally waits on the roster gate |
---

## Slice plan and prerequisites (engineering, 2026-09-30)

### Correction before anything starts

`StaffRecord` **does not exist**. It was planned as Phase 3 slice S3.8 and never built: there is no entity,
table, repository, service or migration in `backend/src/main/java`. Every entity in this document — `Shift`,
`LeaveRequest`, `StaffRecordNote`, `PayrollEntry` — holds a foreign key to it, so **P4.0 is the prerequisite
for the whole phase**, not a detail inside it. The field list in this document matches what was settled for
S3.8 (full name, national ID unique, department FK to `Department`, job title, contact phone, contact email,
employment start date, status ACTIVE/ON_LEAVE/TERMINATED).

What *does* already exist and this phase builds on: `Department`, the `ROLE_HR` role (S3.0), and Phase 2's
`WardCover` — which is what makes the *"doctor on duty in the current ward"* access rule real, and which a
roster must not be allowed to contradict.

### Slices

| Slice | Delivers | Depends on |
|---|---|---|
| P4.0 | `StaffRecord` (this is Phase 3's S3.8): entity, migration, HR-owned access, optional link to a login-less staff member | — |
| P4.1 | `Shift`: one row per staff member per date, optional ward, shift type, times, `createdBy` | P4.0; the roster-vs-`WardCover` decision is now answered (client ruling 1a) |
| P4.2 | `LeaveRequest` with the entitlement per leave type, HR approving in one step, balance = fixed entitlement − approved days this year | P4.0, and the SICK day counts below |
| P4.3 | `StaffRecordNote`: plain searchable PERFORMANCE/DISCIPLINARY log | P4.0 |
| P4.4 | `PayrollEntry`: period, gross, deductions (free text), net, who paid and when, PENDING/PAID | P4.0, Finance-owned |

**Watch item for P4.4.** Finance owns `PayrollEntry` and will need at least the staff member's name and department to
pay anybody. The read it needs is narrow: either a payroll-shaped read that carries the name and department and
nothing else, or a column-level read of `StaffRecord`. What must not happen is adding FINANCE to the
`/api/staff-records` rows as they stand, because that hands over identity numbers, contact details and employment
history along with the name — the whole record widened to reach one field. Decide the shape when P4.4 is built,
before anyone is tempted to widen the row instead.

**P4.0 is delivered.** `StaffRecord` exists as `staff_record` with `StaffRecordStatus` (ACTIVE / ON_LEAVE /
TERMINATED), an optional and unique link to a `User`, HR-owned access, and no delete route at all — a member of
staff who has left is `TERMINATED`, so the record and the Phase 4 rows that will hang off it survive. A record may
name an account or name nothing, which is what lets a cleaner or a records clerk be on the staff file without ever
signing in. The two things a staff file has to get right are refused with a stated reason rather than left to the
unique indexes: one person is one record (by identity number), and one account belongs to one person. That leaves
P4.1 onward, and each of those still needs the decisions below answered.

Every slice follows the same discipline as Phases 1–3: generated where generation helps, business logic written
by hand, `mvnw verify` before it is committed, and its own RBAC rows — the `/api/**` catch-all is watched by
`CatchAllCoverageIT`, so a new entity without a row now fails a test rather than going unnoticed.

### Decisions this needs

1. **SICK leave day counts** — this document flags them as needing confirmation against the current Act
   (full-pay/half-pay split). Same for the exact ANNUAL accrual date (after 12 months' service: from start
   date or calendar year?).
2. **Pay period shape** — a calendar month, or explicit start/end dates? The document says "period" without
   saying what one is, and it decides whether two entries for one month can coexist.
3. **Shift types** — the values for `shiftType` (DAY/NIGHT/…), and whether a shift must have a ward when the
   staff member has one.
4. **Roster versus `WardCover` — ANSWERED (client ruling 1a, 2026-10-06): one source of truth, the roster.**
   `Shift` owns who is on duty when; "who is covering this ward now" is a query over it, and existing `WardCover`
   rows become shifts, so `WardCover` becomes read-only for one release (`blockers-research.md` §3, option 1). It
   runs as its own gate and gates this phase. What makes it a decision rather than a detail, kept for context: the
   access rule reads `WardCover`, not `Shift`, so this changes an existing security rule.
5. **Payroll arithmetic** — the document says net is recorded, not computed (Finance does the tax maths
   outside). Confirming, because it decides whether the system ever owns PAYE/NSSF/PAYE figures.

### Phase 3 residue — a backlog, not a gate

**Phase 3 is closed: S3.0-S3.9 are all delivered.** What follows is residue, and none of it holds this phase:

- **Ruling 4 — refuse create-by-hand on the nine services.** The nine `POST` routes still create payments, bills,
  executions, dispenses, orders, lines and ward covers. Nothing outside the generated resources calls those `save`
  methods, so refusing them is safe; the cost is the generated create tests.
- **Group C3 — the value-guard layer**, with its one structural test: a reference field on a guarded entity must be
  classified fixed-after-create or free, and the build fails if it is neither.
- **The handoff gaps** in `phase3.md`: the raw `PUT /api/patients/{id}`, `VitalSigns` corrections editing in place,
  the reset key in clear text (and the 500 on a spent link), and the one-day reset link.
- **Group B — `POST /api/admin/users/{login}/initial-password`**, which contradicts the link-only rule.
- **The unscoped record-history route**, which lets every clinical role read a staff record's history.

*A correction worth keeping:* this section used to list **S3.2, S3.3, S3.4, S3.5 and S3.7 as outstanding**, and all five
are delivered (`a8fa154`/`78926c3`/`94d6e9c`, `7d75e39`, `b3c98c6`, `a4c425a`/`5e2b5fa`, `ab0c42f`/`a0e4aea`). Read as
the phase's status, that list made Phase 3 look five slices from done when it was none.

- **Admission/Bed folded into `WorkflowOwnedFields`** — **closed by ruling, 2026-10-06: not to be done.** Attempted on
  2026-09-30 and reverted: comparing references by id threw against Hibernate-backed references and turned every
  guarded PUT/PATCH into a 500, including the edits that must be allowed. Both services compare their own references
  by id already (`Admission.primaryDoctor`, `Bed.ward`), against stored ids read as scalars rather than through a
  proxy, and their guards work; the shared helper would have been a cosmetic consolidation, not a fix. Closed rather
  than left open so that it is not attempted a third time. What is left of the whole question is one structural test
  (Group C3).

### Questions for the client — one page

Each of these is a question, not a task: no code waits on anything else. Send them as they are.

1. **Rates.** Bed-day rate per bed type, the doctor's daily round, and a specialist consultation. Until these exist
   they are catalogue rows (`HospitalService`), so the slice is built with them as data — but nobody can price a stay
   without them.
2. **Discharge outcomes.** Confirm the list to implement (referred out / discharged against advice / absconded /
   deceased) and whether the two sign-offs are doctor **then** nurse, or either order.
3. **The doctor's round.** Is there a clinical note with it, and does that note hang off the round or off the stay?
4. **SICK leave.** The Act's full-pay and half-pay split, and whether the ANNUAL accrual date is the employment start
   date or the calendar year.
5. **Pay period.** A calendar month, or explicit start and end dates? It decides whether two entries can coexist for
   one month.
6. **Payroll arithmetic.** Net is recorded rather than computed (Finance does the tax outside). Confirm, because it
   decides whether the system ever owns PAYE/NSSF figures.

**Answered already, no need to ask again:** the roster as the single source of truth (client ruling 1a), the
break-glass scope, the identity-document age rule, the Employment Act retention decision, and the single-node v1.0
acceptance.