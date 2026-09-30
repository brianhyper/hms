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

All four domains are now locked: Phase 1 (Outpatient), Phase 2 (Inpatient), Phase 3 (Admin/Security), Phase 4 (HR). Before calling the full spec complete for handoff, still open:

Doctor's-round clinical-note decision (Inpatient gap #1)
Bed-day/doctor's-round billing — blocked on real client rates
Discharge outcome type (referred-out/AMA/absconded/deceased)
AdHocCharge's missing Bill/Visit FK link
Admission/Bed guards not yet folded into the shared WorkflowOwnedFields helper
Value-guard layer (stopping Super Admin from hand-editing status/amount fields) — not started
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
| P4.1 | `Shift`: one row per staff member per date, optional ward, shift type, times, `createdBy` | P4.0, and the roster-vs-`WardCover` decision below |
| P4.2 | `LeaveRequest` with the entitlement per leave type, HR approving in one step, balance = fixed entitlement − approved days this year | P4.0, and the SICK day counts below |
| P4.3 | `StaffRecordNote`: plain searchable PERFORMANCE/DISCIPLINARY log | P4.0 |
| P4.4 | `PayrollEntry`: period, gross, deductions (free text), net, who paid and when, PENDING/PAID | P4.0, Finance-owned |

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
4. **Roster versus `WardCover`** — the access rule reads `WardCover`, not `Shift`. Do shifts become the single
   source of truth for who is on duty (with `WardCover` derived or retired), or do they stay separate and the
   two are allowed to disagree? This is the one that changes an existing security rule, so it is a decision
   rather than a detail.
5. **Payroll arithmetic** — the document says net is recorded, not computed (Finance does the tax maths
   outside). Confirming, because it decides whether the system ever owns PAYE/NSSF/PAYE figures.

### Phase 3 items that are still open

Phase 3 is not closed. Outstanding, in the order I would take them:

- **S3.2 authentication hardening** — 5 failed attempts, 30-minute idle timeout, forced password change on
  first login, and **revoking live tokens when an account is deactivated**, which is the case this was raised
  for: a deactivated account keeps working until its token expires. Independent of this phase.
- **The value-guard layer** (listed above) — the other half of the domain-operation rule.
- **Admission/Bed folded into `WorkflowOwnedFields`** (listed above). Attempted on 2026-09-30 and reverted: 
  comparing references by id threw against Hibernate-backed references and turned every guarded PUT/PATCH into
  a 500, including the edits that must be allowed. It needs doing again against real entities in an integration
  test — the unit test passed while the integration tests failed, which is the lesson.
- **S3.4** `PatientAccessLog`, **S3.5** the standard override mechanism (Administration, mandatory reason),
  **S3.7** the drug name/price snapshot on prescription and dispense lines, **S3.3** audit-action constants
  for the account events.
