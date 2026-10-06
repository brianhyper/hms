# Resolving the blockers left by Phase 3

Engineering research, 2026-10-05. Nothing here is decided: each item names the standard practice, the trade-offs, the
safe recommendation, and the smallest first step. Every blocker below is one I verified in this codebase during the
Phase 3 slices, not one I inferred.

---

## 1. S3.5, the override mechanism — blocked twice

**What blocks it.** (a) The phase requires an override to identify its actor and role and carry a mandatory reason, but
never says **who is authorized** or whether a **second approver** is needed; the phase document lists this as its own
open question 6 and states that S3.5 cannot start without it. (b) The only required plug-in — the billing-gate
override — belongs to the **discharge operation**, which does not exist. The gate today is a status hold on the visit
(`WAITING_PAYMENT`), not a call that can be intercepted.

**How this is normally done.** Three shapes exist in practice:

| Shape | How it works | Where it fits |
|---|---|---|
| **Break-glass / emergency access** | One authorized person acts, with a mandatory reason, fully audited, and the access is **reviewed afterwards** by someone else | Clinical emergencies. The audit and the retrospective review are the control; the second person comes after, not before |
| **Two-person rule (four eyes)** | A second authorized person approves before the action takes effect | Financial and irreversible actions: theatre counts, large payments, discharges against an unsettled bill |
| **Post-hoc attestation** | The action proceeds and is justified in writing within a window, or it is escalated | Regulatory and compliance contexts |

**Recommendation: break-glass — single-step now, with the review made possible.** For a hospital the failure mode that
matters is a patient waiting while two people are located; the control that actually holds is the reason plus the
audit, which is exactly what the phase requires. Two design points make it safe rather than merely convenient:

- **The override is a first-class audited event**, distinct from the ordinary action: its own action type, naming what
  was overridden, by whom, in what role, and why. An override that is indistinguishable from a normal write in the
  trail is the thing to avoid.
- **The trail must be reviewable in one query** — "every override in the last month" is the report that makes
  break-glass accountable, and it is cheap to provide.

Two-person approval can be added later **without rework** if the reason and the audit entry are the entry point, which
is the argument for building the mechanism now rather than waiting for discharge.

**Decision (2026-10-06).** The client named the case — emergency medicine release before the bill is settled — with the
shape recorded under "Client rulings (2026-10-06)" in `phase3.md`. The plug-in point that now exists is the
**dispensing** gate (`DispenseWorkflowServiceImpl.dispense`), not discharge, so the mechanism's first caller is
pharmacy rather than the ward.

**Smallest safe first step.** Build the mechanism with no route and no caller: a value type carrying actor, role,
what is overridden and the mandatory reason, plus an audit entry type. It is testable on its own and cannot change any
user-facing behaviour while it has no callers. Then wire the discharge operation to it when discharge lands.

---

## 2. S3.6, the two remaining holes

### 2a. Creating records by hand is still open

**What blocks it.** The nine generated CRUDs still accept `POST`, so a payment, bill, execution, dispense, order,
prescription line or ward cover can be created outright — including `/api/ward-covers`, which sidesteps the
open-ward rule. Refusing them is safe: I verified that nothing outside the generated resources calls those `save`
methods. The cost is that the generated create tests assert the old behaviour and must be updated.

**How this is normally done.** Two accepted approaches: **refuse the write** and point at the domain operation, or
**keep the write but make it a domain operation** by adding the missing validation to the service. Refusing is right
where a domain operation already exists; adding validation is right where it does not.

**Recommendation: refuse, service by service, updating the generated tests as each one lands.** Not in one sweep: the
mixed state is safer than a single large change, and each service can be verified on its own. This is the same shape
the bill-line precedent already uses.

### 2b. References are not guarded

**What blocks it.** `WorkflowOwnedFields` compares references **by identity**, deliberately, because reading an id off a
Hibernate-backed reference threw when it was tried. So re-pointing a ward cover at another ward, or a prescription line
at another drug, is still possible by hand.

**Why it threw, and what is safe instead.** Reading a property off an uninitialised proxy forces a load, and a guard is
the worst place for a database hit — a guard that throws refuses a legitimate edit, which is the cautious direction
they chose, but it is still wrong. The standard resolutions, in order of safety:

1. **Compare identifiers carried as values, never through a proxy.** Both sides already have an id as a plain `Long`:
   the request DTO (`billId`, `drugId`, `wardId`) and a **scalar projection** of the stored row. Comparing two `Long`
   values touches no proxy and cannot throw. This is the recommendation.
2. **Declare the identifier field explicitly** and read it through Hibernate's `getIdentifier()` on the reference,
   guarded by `Hibernate.isInitialized`. Correct, but it makes the rule layer depend on Hibernate internals, which the
   rules package currently avoids.
3. **Eager-fetch the reference** in the update path. Simple and safe, at the cost of a join on every edit.

**Smallest safe first step.** Add identifier comparison to `WorkflowOwnedFields` as an *additional* method, leaving the
existing identity comparison untouched, and use it only where a reference must be guarded — starting with the one with
the clearest consequence, a prescription line re-pointed at another drug.

---

## 3. The roster (`Shift`) and `inForce` — blocked on a decision and on non-existence

**What blocks it.** There is no `Shift` entity and no migration to fold `inForce` into, and the underlying question is
unanswered: does the roster become the single source of truth for who is on duty, or do roster and `WardCover` stay
separate?

**How this is normally done.** Two sources that both claim to say who is on duty always diverge, and the divergence is
found during an incident. The standard resolutions are:

1. **One source of truth, the other derived.** The roster owns *who is on duty when*; "who is covering this ward now"
   becomes a query over it. Historical cover rows are then a view, not a table.
2. **Two tables, one writer.** The roster writes; the ward-cover table is written only by the roster. Their rules
   cannot disagree because only one path exists.
3. **Two tables, reconciled.** Both are writable and a job reports disagreements. This is the shape to avoid: it turns
   a policy question into a data-quality question.

**Recommendation: (1), with the roster as the source.** Safety, escalation and clinical responsibility all read "who is
on duty", and a second writable table is a second answer. The migration is the interesting part: existing `WardCover`
rows become roster shifts, and the existing `inForce` rule moves onto the shift rather than being duplicated.

**What is needed before any of this: the decision, not the code.** This is the critical open question in the Phase 4
plan and it changes an existing security rule, which is why it has not been started.

**Decision (2026-10-06): option 1, the roster, as the single source of truth (client ruling 1a).** The decision this
section was waiting for is made; it now runs as its own gate and gates Phase 4. See decision 4 in `phase4.md`.

---

## 4. The handoff items

Each is a decision or a small change, not a design problem.

| Item | Standard practice | Recommendation |
|---|---|---|
| `DELETE /api/patients/{id}` blocked by `fk_patient_access_log__patient_id` | A security log outlives its subject: audit records keep the identifier as a **value**, not a foreign key, so retention and erasure of the subject cannot erase the record of access to it | Drop the foreign key and keep the patient id as a plain value — the same reasoning already used to store the actor's **login** rather than a reference to their account. Alternative: refuse the delete route the way the user-delete route already does |
| Reset key stored in clear text | Reset tokens are treated as credentials: store a **hash**, high-entropy so no salt or slow KDF is needed, and never log them | Hash it. The only user-visible cost is that links issued before the deploy stop working |
| Expired or unknown reset link answers **500** | A client error is a client error: a spent or expired token is a **400**, with a message that does not confirm whether the account exists | Change the status; it is one handler |
| Reset link lifetime of **one day** | One-time reset tokens are short-lived: single use, minutes to a few hours | Shorten to one hour. Visible to anyone who opens their mail the next morning |
| Single-node in-process cache decides revocation | Shared cache (Redis) or a stamp read that bypasses the cache entirely | Either is correct; the stamp read is smaller and removes the dependency on the cache being right. Only relevant on a second node, which does not exist today. Note that Redis is **not currently a dependency** — the project has `spring-boot-starter-cache` and Ehcache only — so the shared-cache route adds an infrastructure component, while the bypass read is the pattern already used for the sign-in state |
| Staff erasure deferred | **Anonymise, do not delete**: blank the identifying fields, keep the row and its key, so payroll and audit references survive | Anonymisation, once the Employment Act retention question is answered |
| `PatientAccessLog` records `VIEW` only | Record the action type the phase names: an edit of the record is its own event | Add `EDIT`. Implemented and tested during this session; reverted only because it exposed the patient-delete conflict above, and it can be reapplied in minutes once that is settled |
| `PUT /api/patients/{id}` can change identity fields with no reason | The corrected record keeps both values and a reason: the raw update is refused and the correction operation is the way in | Refuse the raw update, as `BillLineItem` already does, and point at `POST /api/patient-corrections` |

---

## The one thing that would unblock the most

**Answer the roster question.** S3.6's create-by-hand sweep, the reference guard and the handoff table are all
independent and can proceed in any order. S3.5 needs only the single-step versus second-approver call, and the roster
needs the source-of-truth decision — and that one is on the critical path for Phase 4 as well as for `inForce`.
