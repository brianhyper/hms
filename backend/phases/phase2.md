# Hospital Management System — Phase 2 (Inpatient) Backend Domain Spec

Companion to the Phase 1 (Outpatient) domain spec. Assumes all Phase 1 entities
(Patient, Visit, Consultation, Vitals, Order, Prescription, Drug, Service,
Bill, BillLineItem, Payment, Referral) already exist and are implemented.

---

## 1. Admission Trigger

Single mechanism for entering inpatient care — no elective/pre-booked
admission, no transfer-in from another facility in v1.0.

- Doctor action **"Admit Patient"**, available on a Consultation that is in
  progress **or already completed**. "After the results came back" is a normal
  way to reach the decision, so requiring the consultation to still be open
  would refuse the commonest case. This also matches the Phase 1 wording
  ("during or after a consultation") and is what Phase 1 already ships.
- On trigger:
  - `Visit.type` transitions `OUTPATIENT`/`EMERGENCY` → `ADMISSION`. All prior
    Visit history (vitals, consultation notes, orders) stays attached — this
    is an in-place transition, not a new Visit.
  - `Visit.status` → `ADMITTED`. Phase 1 already does both of these; Phase 2
    adds the `Admission` record on top rather than replacing the action.
  - A new `Admission` record is created, status `PENDING_BED`.
- `Admission.admissionReason` is the free-text reason the admitting doctor
  already supplies, and stays mandatory: the clinical grounds for keeping
  someone in are the doctor's own words. The consultation diagnosis — a *set*,
  `rel_consultation__diagnoses` — is recorded as a catalogue reference when one
  exists, but never replaces the free text and is never the only source, since
  a consultation may legitimately have no diagnosis attached.
- Identical path for the emergency/unidentified-patient case (Phase 1):
  works the same against an `INCOMPLETE_REGISTRATION` Patient.
- **Patient identity merge must carry the admission with it.** Phase 1's merge
  moves `Visit` and `Appointment`, and its implementation records that those two
  are the only Patient references. `Admission.patient` would become a third.
  Preferred: derive the admission's patient through its Visit and do not store a
  second reference at all. If the reference is kept for query convenience, the
  merge must move it too and gain a test. See §9.

## 2. Ward / Bed

### Ward
| Field | Notes |
|---|---|
| name | |
| department | |
| floor/location | optional |

Capacity is derived (count of Beds), never stored.

### Bed
| Field | Notes |
|---|---|
| bedNumber | unique within the ward |
| ward | FK |
| bedType | FK to the `BedType` catalogue below — not a free-text tag |
| dailyRateOverride | optional; see pricing below |
| status | `AVAILABLE` / `OCCUPIED` / `CLEANING` / `MAINTENANCE` |

Status cycle: `OCCUPIED → CLEANING → AVAILABLE`. Matches HL7 v2 Bed Status
(housekeeping) — not an invented state. A bed in `CLEANING` or `MAINTENANCE`
is not assignable; the admission that vacated it has already lost the link, so
nothing depends on it being reassignable immediately.

**Who closes the loop back to `AVAILABLE`:** there is no housekeeping role in
Phase 1's role set (Reception, Nurse, Doctor, Lab, Radiology, Pharmacy,
Finance, Admin, Super Admin), so v1.0 gives the action to **Nurse** and Admin
(`PUT /api/beds/{id}/available`, audited). A bed left in `CLEANING` is
otherwise invisible work: somebody has to be able to say it is ready.

### BedType (new catalogue)

`BedType` is an entity, not a tag typed onto a bed: hand-typed categories
become GENERAL / general / Gen within a week and the occupancy and revenue
reports stop adding up. Same shape as the existing catalogue entities.

| Field | Notes |
|---|---|
| name | e.g. GENERAL, ICU, MATERNITY, HDU |
| defaultDailyRate | the price of a night in this type of bed |
| active | Super Admin managed, same as `Department` / `HospitalService` |

**Pricing.** The bed-day charge reads `Bed.dailyRateOverride` when set, and
`BedType.defaultDailyRate` otherwise. Putting the rate only on the bed means a
price change touches every bed in the hospital; putting it only on the type
makes a premium room impossible. The override gets both, at the cost of one
nullable column. A rate change is not retroactive — see §8.

## 3. Admission

| Field | Notes |
|---|---|
| patient | nullable — prefer deriving it from `visit`; see §1 and §9 |
| visit | FK — the Visit converted via Admit Patient, **one Admission per Visit** |
| admittedAt | when the Admission was created. Immutable. **Required** — bed-days, length of stay and the daily charge (§8) are all computed from it, and `dischargeDate` alone cannot answer any of them |
| admittingDoctor | **immutable**, historical — who made the admit decision |
| primaryDoctor | currently-responsible doctor; defaults to admittingDoctor at creation; **reassignable by Admin at any time** during the stay |
| bed | FK, nullable while `PENDING_BED` |
| admissionReason | free-text, mandatory (§1); optional diagnosis reference alongside it |
| status | `PENDING_BED` / `ADMITTED` / `DISCHARGED` (+ outcome, §11) |
| dischargeDate | |
| dischargeNote | mandatory at discharge |
| dischargedByDoctor | doctor sign-off |
| dischargedByNurse | nurse sign-off |

**Two different `ADMITTED`s, deliberately.** `VisitStatus.ADMITTED` means "this
encounter is now an inpatient one" and is set the moment the doctor converts
it — while the patient may still be waiting for a bed. `AdmissionStatus.ADMITTED`
means "this patient has a bed". They are not the same moment and nothing should
treat them as interchangeable.

**Concurrency, stated as an invariant rather than left to luck.** Two nurses
assigning the same bed at the same moment is a real race, and a bed holding two
patients is not a recoverable state. Therefore:

- `@Version` on `Bed` (primitive `int`, matching the Phase 1 convention), so a
  lost update fails loudly rather than silently;
- a partial unique index on `admission(bed_id) WHERE status = 'ADMITTED'` — one
  patient per bed at a time, while still allowing the same bed to be reused by
  the next admission (history stays intact);
- a partial unique index on `admission(patient_id) WHERE status <> 'DISCHARGED'`,
  so one patient cannot be admitted twice concurrently.

Both indexes are constraints the database enforces; the service checks are for
the error message, not for correctness.

**Doctor-access rule (inpatient):** a doctor sees admissions where they are
`primaryDoctor`, **or** any admission whose current ward matches a ward
they're on duty/covering. Current ward must be derived live as
`Admission.currentBed.ward` — never cached — so the rule self-corrects
automatically on ward transfer.

The covering half of that rule needs data that does not exist yet: nothing in
Phase 1 records which doctor is on duty for which ward. Hence:

### WardCover (new — the roster the access rule reads)

| Field | Notes |
|---|---|
| doctor | FK |
| ward | FK |
| from / to | the period covered; `to` nullable for open-ended |
| assignedBy | Super Admin / Admin |

Without this the "ward they're covering" clause cannot be implemented at all,
and a doctor rotating through a ward would see nothing.

Flow: Admit Patient → `PENDING_BED` (no bed yet) → Nurse/Admin assigns a
bed → `ADMITTED` (Bed flips `AVAILABLE → OCCUPIED`).

`PENDING_BED` needs somewhere to be visible, or patients waiting for a bed are
lost between the doctor's decision and the bed being found: the inpatient
worklist (§12, slice 3) lists `PENDING_BED` admissions and the beds available
to put them in.

## 4. Ward Transfer

Modeled as a sub-event on the existing Admission — never a new Admission.

### AdmissionTransfer
| Field | Notes |
|---|---|
| admission | FK |
| fromBed | FK, nullable — null when the patient was still `PENDING_BED` |
| toBed | FK — reject if not `AVAILABLE` |
| transferredBy | |
| reason | mandatory — moving a patient changes who is responsible for them, and it is the kind of decision that gets questioned later |
| timestamp | |

On transfer: `fromBed → CLEANING`, `toBed → OCCUPIED`. Bed's rate is
re-evaluated on the next bed-day charge automatically, since the charge
always reads the *current* bed. Which means a same-day transfer charges the
receiving bed's rate for that day — that is the intended reading, and it is
stated here because the alternative (splitting a day between two rates) is a
rule nobody has asked for yet.

**Who may transfer:** the RBAC summary gives this to Admin, while §3 gives bed
assignment to Nurse/Admin. Those should be the same role set — a nurse who can
put a patient into a bed can move them to another one — unless the client says
otherwise. To be confirmed, see §11.

## 5. Nurse Charting (Inpatient Vitals)

**Separate entity from outpatient Vitals** — deliberately not reused,
since usage shape differs (one-shot per visit vs. many rows over days).

### InpatientVitals
| Field | Notes |
|---|---|
| admission | FK |
| recordedBy | nurse |
| timestamp | |
| temperature, pulse, systolicBP, diastolicBP, SpO2, weight, height | same field set/validation as outpatient Vitals |
| bmi | derived |
| notes | |

**Pure nurse routine** — no doctor-order linkage, no due/overdue
computation. Nurse logs on their own schedule. Surfaces on the doctor
dashboard as a chronological log: My Patients → Patient → Vitals.

**Validation is reused, not re-implemented.** Phase 1 already has the two-tier
rule in `VitalsValidator` (a physiologically impossible value is refused
outright; a merely alarming one is admitted and flagged). Inpatient charting
calls the same class with the same field set, so a pulse of 400 is refused at
the ward exactly as it is in triage, and there is one place to change the
bounds. The fields match because `VitalSigns` already carries temperature,
pulse, BP, SpO2, weight, height, BMI and nutritional status — what differs is
the *cardinality*, which is why this is a separate entity at all: outpatient
`VitalSigns` is unique per visit, and a patient stays in for days.

**Correcting a charted observation.** A wrong entry is corrected by recording a
corrected row that supersedes it, with the original kept and the change
audited — the same rule and the same machinery Phase 1 uses for vitals
corrections. Charting is a clinical record; it is not a spreadsheet cell. Who
may correct whose charting is part of §10's audit surface.

## 6. Daily Doctor Orders

### DoctorOrder
| Field | Notes |
|---|---|
| admission | FK |
| orderedBy | doctor |
| type | `LAB` / `RADIOLOGY` / `DRUG` / `INSTRUCTION` |
| recurrence | `ONE_OFF` / `RECURRING` |
| frequency | free text (e.g. "q4h") — not a structured scheduling DSL |
| duration / endDate | optional, open-ended if blank |
| status | `ACTIVE` / `COMPLETED` / `CANCELLED` |
| cancelledBy, cancelReason | |

- `ONE_OFF` orders: mark `COMPLETED` directly, no child records.
- `RECURRING` orders: each actual dose/action tracked separately via:

### OrderExecution
| Field | Notes |
|---|---|
| order | FK |
| executedBy | nurse |
| executedAt | |
| notes | e.g. "patient refused", "delayed 30 min" |

**Explicitly manual** — no due/overdue computation, no scheduling engine.
Execution history surfaces to `primaryDoctor` via Patient → Orders.

**`INSTRUCTION` is non-billable** and never produces a charge, whatever else
it says ("continue IV fluids" is a ward instruction, not a line on a bill).

**`DRUG` must not become a second prescribing path.** Phase 1 already has
`Prescription` + `PrescriptionLine` with stock reservation; adding a `DRUG`
doctor-order that *also* carries drugs gives two ways to order the same thing,
which is how a patient gets charged twice or supplied twice. The rule is:

- the **order** is the clinical instruction to give a drug, and it is what a
  nurse executes;
- the **supply** is a `Prescription`, as in Phase 1, because that is where
  reservation, the pharmacy queue and the charge already live;
- a `DRUG` order therefore either *is* backed by a `Prescription` (created or
  linked when the order is placed) or does not exist at all in v1.0. Deciding
  between those two is a slice-5 decision, but it is not left open here.

**Administration and dispensing are different events and the words must not be
swapped.** `OrderExecution` records a nurse *giving* a dose from ward stock;
dispensing is pharmacy *handing stock over* (§8). Mixing them is how a drug gets
counted out of the store twice.

**Who completes a `RECURRING` order:** the prescriber, through an explicit stop
(or it is resolved at discharge, §7). A nurse does not decide that a course of
antibiotics is finished.

### What "reuse Phase 1 entities" can and cannot mean

Phase 1 deliberately refuses every clinical write against an admitted visit:
`VisitLifecycle.isOpen()` returns false for `ADMITTED`, and that guard sits in
triage, consultation, order placement and prescription placement alike. A
patient who is in the building for a week is exactly the patient whose
observations, orders and drugs still need recording, so this is the seam Phase 2
has to open — deliberately, per workflow, rather than by relaxing the guard
globally. `InpatientVitals` and `DoctorOrder` sidestep it by being new entities,
which is the cleaner half of the fix; anything that genuinely reuses a Phase 1
entity (drug supply, diagnostics) needs its own decision about whether an
admitted visit is a legal target. See §9.

## 7. Discharge

- Requires **both** doctor and nurse sign-off (`dischargedByDoctor`,
  `dischargedByNurse` on Admission). Mechanically these are **two separate
  actions**, each recording its own actor and its own audit entry; the admission
  becomes `DISCHARGED` only when both are present. One endpoint taking two names
  would let a single caller claim both sign-offs, which defeats the reason for
  asking for two. If the client wants the same person to be able to do both, that
  is a decision to record, not an accident to allow.
- On discharge attempt: system surfaces all `ACTIVE` `RECURRING` orders.
  Doctor/nurse either resolve/cancel them there, or explicitly
  acknowledge-and-proceed — **logged, not a hard block.**
- **Billing gate:** the discharge action is unavailable until the running bill is
  `PAID` **or** covered by an `ACTIVE` payment plan (§8). This gate is the reason
  §8's payment changes are not optional — see §9.
- On completion:
  - `Admission.status → DISCHARGED`
  - `Bed.status → CLEANING`
  - `dischargeDate`, `dischargeNote` captured
  - `Visit.status → CLOSED`, through a dedicated
    `VisitStatusService.onDischarged(visitId)` — **not** through the payment
    path, which closes an outpatient encounter when the bill is settled (§9)

**Not every stay ends in a discharge.** A patient may die, or leave against
medical advice. Neither is a discharge, neither should require two signatures,
and both release the bed and end the encounter. Phase 1's status set has no
place to put them, so this is an explicit decision rather than an oversight —
see §11.

## 8. Stay-Based Billing

Unlike outpatient (bundled at visit-close), the inpatient Bill stays
**open/running for the entire Admission** — because PaymentPlan (below)
allows paying mid-stay, so the running total must always be accurate.

### Automatic daily charges (unconditional, once per calendar day of the stay)

No clinical confirmation gate and no "skipped" toggle in v1.0 — a deliberate
policy decision, see §11:
1. **Bed-day charge** — rate read from the current bed (bed override else
   bed-type default) **at the moment the day is charged, and stored on the line
   item**. A later rate change must not rewrite a night that has already been
   billed.
2. **Doctor's-daily-round charge** — priced via the Service catalogue
   (new entries needed: "Doctor's daily round", "Specialist
   consultation", etc. — Super Admin managed, same as existing
   catalogue).

**"Once per calendar day" needs four things defined before it can be coded:**

- **Which day.** The hospital's local day, from one configured timezone — never
  the server's. Get this wrong and the charge lands on the wrong date for
  everyone, or twice for a night shift.
- **Idempotency.** Each charge is written through
  `BillingService.addOrUpdateLine` with `sourceRef = BED_DAY:<admissionId>:<yyyy-MM-dd>`
  (and `DOCTOR_ROUND:...`), which is already the uniqueness key Phase 1's
  incremental billing depends on. Re-running the job must be a no-op, not a
  second night.
- **Catch-up.** If the job did not run — deploy, outage, a restart at 00:05 —
  it must charge **every missing day from `admittedAt` to yesterday**, not just
  today. A billing job that silently loses the nights it missed is worse than no
  job at all, because nobody goes looking.
- **Trigger.** A scheduled task following the existing
  `AppointmentNoShowScheduler` + service pattern, plus an idempotent
  on-demand "post charges up to today" callable from the admission screen so a
  missed run self-heals without waiting for midnight.

**Day-count policy is a business rule and needs sign-off (§11):** whether the
admission day and the discharge day are each charged. The recommendation is to
charge **nights** — the admission day and each following day up to but not
including the discharge day — because that is how hospitals bill and it makes a
same-day discharge free rather than a one-night charge.

### Event-triggered charges (reuse Phase 1 entities where the visit state allows it — see §9)

- Lab/Radiology order completion (same catalogue pricing) — works, because
  Phase 1's order flow still charges an admitted visit even though it refuses to
  *place* a new order against one.
- Drug supply (Prescription/Drug, same reservation pattern) — **only after the
  §9 decoupling**, because Phase 1 releases medicine to the pharmacy only when
  the bill is settled, and an inpatient bill is not settled until discharge.

### AdHocCharge (new — for anything not covered above)
| Field | Notes |
|---|---|
| admission | FK |
| serviceCatalogue | FK, optional |
| description | free-text fallback if no catalogue match |
| amount | |
| addedBy | Finance |
| reason | mandatory |
| timestamp | |
| voided / voidReason / voidedBy | optional; see below |

Added by **Finance**. It does not post itself to the bill: it goes through
`BillingService.addOrUpdateLine` with `sourceRef = ADHOC:<id>` and a new
`BillLineSourceType.ADHOC`, so voids are `removeLine` + an audited
void-with-reason rather than a delete — the same guarantee Phase 1 gives for a
withdrawn prescription, and for the same reason: money that appears and
disappears without a trace cannot be reconciled.

### PaymentPlan (new — installment/partial settlement)
| Field | Notes |
|---|---|
| bill | FK |
| totalOwed | |
| agreedBy | Finance staff |
| guarantorName, guarantorRelationship, guarantorPhone | |
| notes | terms as agreed — free text, no structured repayment schedule |
| status | `ACTIVE` / `COMPLETED` / `DEFAULTED` |

Installments are ordinary `Payment` records against the Bill — running
balance is always `totalOwed − sum(payments)`, no separate ledger.
No automated reminders or default-tracking — Finance marks `DEFAULTED`
manually if a plan falls through.

**One fact, one place.** `BillStatus` gains `PAYMENT_PLAN_AGREED`, but the plan's
existence must not be tracked in two records that can drift apart. The bill's
status is what the discharge gate reads; `PaymentPlan.status` is the plan's own
lifecycle. A plan is `ACTIVE` exactly while it is what the bill is waiting on,
and creating or defaulting a plan updates the bill's status in the same
transaction. Nothing derives the bill's status from a query at read time, so the
gate cannot be answered two different ways by two callers.

**Installments must be payable during the stay**, which is the whole point of
the plan — and Phase 1 currently refuses to record a payment against anything
that is not `WAITING_PAYMENT` (§9).

### Surgery (v1.0 scope note)
Only the billing side exists — Finance adds a charge (catalogue or
AdHocCharge) for a procedure. **No clinical record** (surgeon, procedure
name, complications) in v1.0, since Theatre/surgery scheduling is
deferred to v1.1. Whether v1.0 needs even a bare clinical record is
**open — to be confirmed with the client at the pitch.**

---

## Entity Summary (new in Phase 2)

Reference data: `Ward`, `Bed`, `BedType` (catalogue), `WardCover` (duty roster)

Stay records: `Admission`, `AdmissionTransfer`, `InpatientVitals`,
`DoctorOrder`, `OrderExecution`

Money: `AdHocCharge`, `PaymentPlan`

## RBAC Summary (new/changed)

| Role | Inpatient access |
|---|---|
| Doctor | Admit Patient action; sees admissions where `primaryDoctor` or on-duty ward matches; places DoctorOrders; discharge sign-off |
| Nurse | Assigns bed (PENDING_BED → ADMITTED); ward transfer; logs InpatientVitals; logs OrderExecution; marks a bed available again; discharge sign-off |
| Admin | Reassigns `primaryDoctor`; ward transfer; ward/bed reference data |
| Finance | AdHocCharge (and its void); PaymentPlan; the discharge billing gate |
| Super Admin | Ward/Bed/BedType reference data, `WardCover` roster, Service catalogue entries (doctor's round, specialist consultation) |

This table is the *specification* of who may do what. The implementation of it
remains Phase 1's single `PHASE 1 RBAC TABLE` block in
`config/SecurityConfiguration.java` plus, for row-level rules (a doctor seeing
only their own patients), a service-layer filter — the same split Phase 1 uses,
and for the same reason: a role check cannot express "this row, but not that
one".

---

## 9. Interaction with the shipped Phase 1

Everything below is verified against the code Phase 1 shipped, not inferred.
Three of these are blockers: Phase 2 cannot work without them.

**The single root cause:** Phase 1 ties three different things to one event —
*the outpatient bill has been settled*. Money in, medicine released, encounter
closed. For an outpatient those genuinely are the same moment. For a stay they
are three different moments, and Phase 2 has to pull them apart.

| # | Shipped behaviour | Where | Effect on Phase 2 |
|---|---|---|---|
| 1 | Payment is refused unless `visit.status == WAITING_PAYMENT` ("its bill is not finalised until the visit is waiting to be paid") | `PaymentWorkflowServiceImpl` | An admitted visit is `ADMITTED`, never `WAITING_PAYMENT`, so **no inpatient payment can be recorded at all** — §7's discharge gate and §8's installments are both unreachable |
| 2 | Settling closes the encounter | `settleEncounter()` → `VisitStatusService.onBillPaid` → `moveTo(CLOSED, "bill settled in full")` | Even once #1 is fixed, paying mid-stay would **close the visit before discharge** |
| 3 | Medicine is released only by settlement | `settleEncounter()` → `PrescriptionService.markPaid`; `PrescriptionLifecycle.afterPayment` is the only route to `READY_FOR_DISPENSE` | **Nothing can be dispensed during a stay** while the bill is still running |
| 4 | Every clinical write refuses an admitted visit | `VisitLifecycle.isOpen()` is false for `ADMITTED`; guarded in `TriageServiceImpl`, `ConsultationWorkflowServiceImpl`, `DiagnosticOrderWorkflowServiceImpl`, `PrescriptionWorkflowServiceImpl` | New entities (`InpatientVitals`, `DoctorOrder`) sidestep it; anything *reusing* a Phase 1 entity must decide explicitly whether an admitted visit is a legal target |
| 5 | Nothing can move an admitted visit | `DERIVED_STATUSES = {WAITING_RESULTS, WAITING_PAYMENT}`; `participatesInOutpatientPath(ADMISSION)` is false | Discharge needs its own transition — `VisitStatusService.onDischarged(visitId)` — and must not go via the payment path |

### The fix, stated as one principle

> **Settling money and ending an encounter are different events.** Payment
> records money and releases what the money was holding. Closure is a clinical
> decision that the encounter is over — for an outpatient that is the payment
> desk; for an inpatient it is discharge.

Concretely, in the slice-0 change to Phase 1:

- `PaymentWorkflowServiceImpl` accepts a bill on an **open encounter** —
  `WAITING_PAYMENT` *or* an `ADMITTED` visit — because an inpatient bill is
  collected against while the patient is still in the building.
- `settleEncounter` keeps doing what it does for money (`markPaid`, release to
  pharmacy, audit) but **only closes the visit when the visit is an outpatient
  one**. Closure moves behind `closesOnSettlement(visit)`, a one-line rule.
- `VisitStatusService.onDischarged(visitId)` moves `ADMITTED → CLOSED` and is
  called by the discharge action (§7) — never by payment.
- `VisitLifecycle.isOpen()` stays as it is. Its meaning ("this visit is still on
  the outpatient path") is correct; it is the *callers* that need to ask a
  different question for inpatient work.

A worked example of the result: a patient is admitted on day 1; the
consultation fee, the lab work and the first prescription are already on the
bill, which simply keeps running. Finance takes a deposit on day 2 (allowed:
open encounter). The ward's drugs are released on the order that consumed them,
not on the deposit. On day 6 both sign-offs are in, a plan covers the balance,
and discharge closes the visit and sends the bed to `CLEANING`.

None of this is hypothetical tidying: #1 and #3 are the reason "reuse Phase 1
entities as-is" cannot be taken literally in §8.

## 10. Migration, seed and audit surface

What a Phase 2 slice has to touch besides its own entities.

**New enum values and enumerations**

| Enum | Change |
|---|---|
| `BillStatus` | add `PAYMENT_PLAN_AGREED` (currently `UNPAID`/`PAID`) |
| `BillLineSourceType` | add `BED_DAY`, `DOCTOR_ROUND`, `ADHOC` (currently `CONSULTATION`/`LAB`/`RADIOLOGY`/`PHARMACY`) |
| new | `AdmissionStatus`, `BedStatus`, `DoctorOrderType`, `DoctorOrderRecurrence`, `DoctorOrderStatus`, `PaymentPlanStatus`, `AdmissionOutcome` (§11) |
| `AuditActions` | add `PATIENT_DISCHARGED`, `BED_ASSIGNED`, `BED_STATUS_CHANGED`, `WARD_TRANSFERRED`, `PRIMARY_DOCTOR_CHANGED`, `ORDER_PLACED`/`ORDER_EXECUTED`, `CHARGE_ADDED`/`CHARGE_VOIDED`, `PAYMENT_PLAN_AGREED`/`PAYMENT_PLAN_DEFAULTED` (`PATIENT_ADMITTED` already exists) |

**Seed data**: Service catalogue entries for "Doctor's daily round" and
"Specialist consultation" — with real prices, not zeros, since a zero daily
charge silently makes the whole stay-billing path a no-op and every test that
reads the catalogue will still pass. One `BedType` per type and one `Ward` are
worth seeding in dev so the ward screens are not empty on first run.

**RBAC**: new rows in the single `PHASE 1 RBAC TABLE` block, first-match-wins,
above the `/api/**` catch-all; each generated CRUD write closed to `SUPER_ADMIN`
as its workflow endpoint lands, exactly as Phase 1 did, with the matching
`*ResourceIT` updated to match.

**Migrations**: one changelog per slice, included in `master.xml` **before** the
needle, and — because a changeset that is context-filtered never gets validated
— verified by running it against a **virgin database** once, not only against the
development one where it may be skipped.

**Phase 1 machinery to reuse rather than reinvent**: `BillingService.addOrUpdateLine`
+ `sourceRef` (idempotency), `BillingService.removeLine` (voids),
`VitalsValidator` (bounds), `AuditLogService.Entry` (audit),
`BusinessRuleViolationException` (409 + error key),
`HospitalIdService` (nothing new needed), the `@Version`-on-a-primitive-`int`
optimistic-locking convention, and the `XWorkflowService` naming rule that avoids
a clash with a generated `XService` bean.

## 11. Open questions for the client

Each of these is a business or clinical decision, so the spec records the
question rather than inventing an answer.

1. **Charging the daily round with no clinical gate.** The spec says a round is
   billed every day, unconditionally. Billing for a service that was not
   delivered is a compliance question in most jurisdictions, not a design one.
   Minimum mitigation if it stays: record the doctor who did the round.
2. **Bed-day counting.** Nights only (recommended), or admission day and
   discharge day both charged? A same-day admission-and-discharge is the case
   that exposes the difference.
3. **No due/overdue scheduling.** Deliberate in v1.0, and it means nothing in
   the system tells a nurse what is due. Is the client content to run inpatient
   medication from paper while the system records what happened?
4. **Death in hospital and discharge against medical advice.** Neither is a
   discharge. Do they get their own outcome/status (recommended), and who signs
   them off?
5. **Ward transfers.** Can a nurse move a patient, or is it Admin only?
6. **Same-day transfers between differently priced beds** charge the receiving
   bed's rate for that day (§4). Confirm that is the intended reading.
7. **Duplicate-dose protection.** With no scheduling engine, two nurses can each
   chart the same 14:00 dose. Is `OrderExecution` allowed to be an unstructured
   log, or should the system at least warn on a duplicate within a window?

## 12. Implementation plan

The Phase 1 method carries over unchanged: **JHipster generates the entities,
then the business logic is written on top.** Nothing here is hand-written schema.

| # | Slice | Contents |
|---|---|---|
| 0 | Settle money without ending the encounter | The §9 decoupling in Phase 1, with `VisitStatusService.onDischarged`. Touches shipped code, so it lands first, alone, with the full existing suite green |
| 1 | Phase 2 model | JDL for the new entities → generate → migration → RBAC rows → catalogue seeds. No workflow logic yet |
| 2 | Ward, bed and availability | `Ward`/`Bed`/`BedType` reference data, bed status lifecycle including `CLEANING → AVAILABLE`, availability query |
| 3 | Admission record and bed assignment | The shipped admit action additionally creates the `Admission`; assign/reassign a bed; `PENDING_BED` worklist |
| 4 | Inpatient charting and the doctor's access rule | `InpatientVitals` reusing `VitalsValidator`; `WardCover` and the row-level "my patients" filter |
| 5 | Doctor orders and nurse execution | `DoctorOrder` + `OrderExecution`, including the `DRUG` ↔ `Prescription` decision (no drug order is built until that is settled) |
| 6 | Stay billing | Daily charges with timezone, idempotency and catch-up; `AdHocCharge` and its void; the running bill |
| 7 | Payment plan and inpatient payment | `PaymentPlan`, and paying against a running bill |
| 8 | Ward transfer | `AdmissionTransfer`, bed status moves, live ward derivation |
| 9 | Discharge and outcomes | Two sign-offs, the billing gate, `onDischarged`, bed to `CLEANING`, death/AMA if confirmed |
| 10 | Corrections, audit and merge | Which roles may correct which inpatient fields; the §10 audit actions; `Admission` handled by patient merge |
| 11 | Inpatient worklists and role seeding | My patients / awaiting bed queues, and a dev login for any role added |

**Ordering rationale.** Slices 0–1 are prerequisites for everything else; 2–3
make a patient exist in a bed; 4–5 make the stay clinically real; 6–7 make it
payable; 8–9 make it movable and finishable; 10–11 are the cross-cutting work
that is only meaningful once the rest exists. Slice 5 deliberately waits for the
DRUG decision rather than building one of two possible drug paths.

**Per-slice discipline** (the Phase 1 ladder): compile → targeted unit tests →
targeted integration tests → full `mvnw verify` → confirm the SPA bundle is still
intact → delete scratch logs.

**One generation trap to avoid.** Phase 1's entities were generated once and then
hand-tuned (`VisitStatus.ADMITTED`, `Visit.createdAt`, `@Version`, indexes). A
JDL that declares relationships to existing entities makes JHipster **rewrite
those existing entity files**, silently discarding the hand edits. So Phase 2's
JDL contains only the new entities, and after every generation step the diff is
inspected before anything is staged — which is now cheap, because the repository
is under version control.