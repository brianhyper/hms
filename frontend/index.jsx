import React, { useState } from "react";

/* ============================================================
   Riverside Hospital HMS — role-scoped frontend prototype
   Vite + React SPA structure, collapsed into one file for preview.
   Real repo layout: src/lib/roles.jsx (NAV config), src/pages/<role>/…,
   RoleGuard in main.jsx. Nav is GENERATED from the role config, so
   sections a role can't use never render — no disabled sections.
   ============================================================ */

const now = Date.now();
const H = 3600e3;
let _id = 1000;
const nid = (p) => `${p}-${++_id}`;
const t = (ts) => new Date(ts).toLocaleTimeString([], { hour: "2-digit", minute: "2-digit" });
const d = (ts) => new Date(ts).toLocaleDateString([], { day: "2-digit", month: "short" });
const KSh = (n) => "KSh " + n.toLocaleString();

const STAGE = { vitals: "Awaiting vitals", queued: "In doctor's queue", consult: "In consultation", pharmacy: "Awaiting pharmacy", billing: "Awaiting billing", admitted: "Admitted", closed: "Closed" };
const NEXT_OWNER = { vitals: "Nurse", queued: "Doctor", consult: "Doctor", pharmacy: "Pharmacy", billing: "Finance", admitted: "Doctor / Nurse" };

const ROLES = {
  reception: { label: "Reception", user: "Grace Wanjiru", tag: "Registration · Appointments · Opens visits. No clinical data.", color: "bg-sky-600" },
  nurse: { label: "Nurse", user: "Nurse Achieng", tag: "Vitals · OPD queue · Ward charting. Orders view-only.", color: "bg-teal-600" },
  doctor: { label: "Doctor", user: "Dr. Kimani", tag: "Queue · Consults · Orders · Discharge. No billing/admin.", color: "bg-indigo-600" },
  lab: { label: "Lab", user: "Lab Tech Otieno", tag: "Incoming lab orders · Results entry only.", color: "bg-rose-600" },
  radiology: { label: "Radiology", user: "Rad Tech Mutua", tag: "Incoming imaging orders · Results only.", color: "bg-orange-600" },
  pharmacy: { label: "Pharmacy", user: "Pharm. Njeri", tag: "Prescriptions · Dispensing · Stock.", color: "bg-emerald-600" },
  finance: { label: "Finance", user: "Finance Barasa", tag: "Billing · Payments. No clinical data.", color: "bg-amber-600" },
  admin: { label: "Administration", user: "Admin Mwangi", tag: "Read-only reporting & dashboards.", color: "bg-slate-600" },
  superadmin: { label: "Super Admin", user: "S. Admin Njoki", tag: "Users · Audit · Config. Not clinical.", color: "bg-zinc-800" },
  hr: { label: "HR", user: "HR Chebet", tag: "Staff · Leave · Roster. Zero patient data.", color: "bg-fuchsia-600" },
};

const NAV = {
  reception: [
    { k: "home", label: "Front Desk", icon: "🏥" },
    { k: "register", label: "Register Patient", icon: "➕" },
    { k: "search", label: "Patient Search", icon: "🔍" },
    { k: "appointments", label: "Appointments", icon: "📅", badge: (db) => db.appointments.filter((a) => a.status === "booked").length },
  ],
  nurse: [
    { k: "triage", label: "Triage — Vitals", icon: "❤️", badge: (db) => db.visits.filter((v) => v.type === "outpatient" && v.status === "vitals").length },
    { k: "wards", label: "Ward Charting", icon: "🛏", badge: (db) => db.visits.filter((v) => v.type === "admission" && v.status === "admitted").length },
    { k: "orders", label: "Doctor's Orders (view)", icon: "👁" },
  ],
  doctor: [
    { k: "queue", label: "My Queue", icon: "📋", badge: (db) => db.visits.filter((v) => v.status === "queued").length },
    { k: "active", label: "Active Consults", icon: "🩺", badge: (db) => db.visits.filter((v) => v.status === "consult").length },
    { k: "admitted", label: "Admitted Patients", icon: "🛏" },
  ],
  lab: [
    { k: "queue", label: "Incoming Lab Orders", icon: "🧪", badge: (db) => db.orders.filter((o) => o.kind === "lab" && o.status === "pending").length },
    { k: "done", label: "Posted Results", icon: "✅" },
  ],
  radiology: [
    { k: "queue", label: "Incoming Imaging Orders", icon: "🩻", badge: (db) => db.orders.filter((o) => o.kind === "radiology" && o.status === "pending").length },
    { k: "done", label: "Posted Results", icon: "✅" },
  ],
  pharmacy: [
    { k: "queue", label: "Prescription Queue", icon: "💊", badge: (db) => db.prescriptions.filter((p) => p.status === "pending").length },
    { k: "inventory", label: "Drug Inventory", icon: "📦" },
  ],
  finance: [
    { k: "billing", label: "Visits to Bill", icon: "🧾", badge: (db) => db.visits.filter((v) => v.status === "billing").length },
    { k: "receipts", label: "Payments Received", icon: "💰" },
  ],
  admin: [
    { k: "overview", label: "Hospital Overview", icon: "📊" },
    { k: "flow", label: "Visit Flow Monitor", icon: "🔀" },
    { k: "beds", label: "Bed Occupancy", icon: "🛏" },
  ],
  superadmin: [
    { k: "users", label: "Users & Roles", icon: "👥" },
    { k: "audit", label: "Audit Trail", icon: "📝" },
    { k: "config", label: "System Configuration", icon: "⚙️" },
  ],
  hr: [
    { k: "staff", label: "Staff Records", icon: "🪪" },
    { k: "leave", label: "Leave Requests", icon: "🌴", badge: (db) => db.leave.filter((l) => l.status === "pending").length },
    { k: "roster", label: "Rostering", icon: "📅" },
  ],
};

const seed = () => {
  const patients = [
    { id: "p1", mrn: "MRN-2041", name: "Mary Njeri", sex: "F", age: 34, phone: "0722 111 001", allergies: [], chronic: ["Hypertension"], notes: "Last seen Mar 2026, BP controlled." },
    { id: "p2", mrn: "MRN-2052", name: "Joseph Kariuki", sex: "M", age: 51, phone: "0722 111 002", allergies: ["Penicillin"], chronic: ["Type 2 Diabetes"], notes: "On metformin 850mg BD." },
    { id: "p3", mrn: "MRN-2063", name: "Faith Auma", sex: "F", age: 27, phone: "0722 111 003", allergies: [], chronic: [], notes: "" },
    { id: "p4", mrn: "MRN-2074", name: "Peter Otieno", sex: "M", age: 45, phone: "0722 111 004", allergies: ["Sulfa drugs"], chronic: ["Asthma"], notes: "Salbutamol inhaler PRN." },
    { id: "p5", mrn: "MRN-2085", name: "Esther Mwikali", sex: "F", age: 60, phone: "0722 111 005", allergies: [], chronic: ["Osteoarthritis"], notes: "" },
    { id: "p6", mrn: "MRN-2096", name: "Samuel Baraka", sex: "M", age: 38, phone: "0722 111 006", allergies: ["Latex"], chronic: [], notes: "Admitted with severe malaria." },
    { id: "p7", mrn: "MRN-2107", name: "Alice Wambui", sex: "F", age: 29, phone: "0722 111 007", allergies: [], chronic: [], notes: "Post normal delivery." },
    { id: "p8", mrn: "MRN-2118", name: "Brian Mutiso", sex: "M", age: 8, phone: "0722 111 008", allergies: ["Peanuts"], chronic: [], notes: "Guardian: Mrs. Mutiso." },
  ];
  const visits = [
    { id: "v1", patientId: "p1", type: "outpatient", status: "vitals", openedAt: now - 0.4 * H, complaint: "Fever and headache x2 days", orders: [], billItems: [] },
    { id: "v2", patientId: "p2", type: "outpatient", status: "queued", openedAt: now - 1.2 * H, complaint: "Persistent cough x1 week", vitals: { bp: "138/86", temp: 37.2, hr: 88, spo2: 97, wt: 78 }, queuedAt: now - 0.8 * H, orders: [], billItems: [] },
    { id: "v3", patientId: "p3", type: "outpatient", status: "consult", openedAt: now - 2.5 * H, complaint: "Abdominal pain", vitals: { bp: "112/70", temp: 36.8, hr: 76, spo2: 99, wt: 60 }, assignedDoctor: "Dr. Kimani", diagnosis: "Suspected typhoid — awaiting labs", orders: [], billItems: [] },
    { id: "v4", patientId: "p4", type: "outpatient", status: "pharmacy", openedAt: now - 3 * H, complaint: "Asthma review", vitals: { bp: "120/78", temp: 36.6, hr: 80, spo2: 95, wt: 70 }, assignedDoctor: "Dr. Kimani", diagnosis: "Mild asthma exacerbation", closedConsultAt: now - 1.5 * H, orders: [], billItems: [] },
    { id: "v5", patientId: "p5", type: "outpatient", status: "billing", openedAt: now - 4 * H, complaint: "Knee pain", vitals: { bp: "130/80", temp: 36.7, hr: 72, spo2: 98, wt: 68 }, assignedDoctor: "Dr. Kimani", diagnosis: "Osteoarthritis — knee", closedConsultAt: now - 2 * H, orders: [], billItems: [] },
    { id: "v6", patientId: "p6", type: "admission", status: "admitted", openedAt: now - 30 * H, complaint: "Severe malaria", assignedDoctor: "Dr. Kimani", bedId: "G2", admissionNote: "Admitted via OPD. IV artesunate started.", charts: [{ at: now - 24 * H, vitals: "108/66 · 38.9°C · HR 98 · SpO2 96", note: "Febrile, alert. Tolerating oral fluids.", by: "Nurse Achieng" }, { at: now - 8 * H, vitals: "112/70 · 37.6°C · HR 88 · SpO2 98", note: "Afebrile trend, improving.", by: "Nurse Kamau" }], orders: [], billItems: [] },
    { id: "v7", patientId: "p7", type: "admission", status: "billing", openedAt: now - 52 * H, complaint: "Normal delivery", assignedDoctor: "Dr. Kimani", bedId: "M1", admissionNote: "Admitted in early labour.", charts: [{ at: now - 30 * H, vitals: "118/74 · 36.9°C · HR 82", note: "Delivered healthy baby girl.", by: "Nurse Kamau" }], orders: [], dischargeNote: "Stable post-partum day 2. Discharge with iron + folic acid. PNC review in 6 days.", billItems: [{ label: "Delivery package", amt: 12000 }, { label: "Bed — Maternity (2 nights)", amt: 6000 }, { label: "Medication", amt: 1350 }] },
    { id: "v8", patientId: "p3", type: "outpatient", status: "closed", openedAt: now - 200 * H, complaint: "Sore throat", assignedDoctor: "Dr. Kimani", diagnosis: "Viral pharyngitis", closedConsultAt: now - 199 * H, orders: [], billItems: [{ label: "Consultation", amt: 800 }] },
  ];
  const orders = [
    { id: nid("o"), visitId: "v3", patientId: "p3", kind: "lab", test: "Typhoid (Widal) + FBC", status: "resulted", result: "Widal TO antigen 1:160 — positive. FBC: WBC 12.4.", orderedAt: now - 2 * H, resultedAt: now - 1 * H },
    { id: nid("o"), visitId: "v3", patientId: "p3", kind: "lab", test: "Blood culture", status: "pending", orderedAt: now - 2 * H },
    { id: nid("o"), visitId: "v3", patientId: "p3", kind: "radiology", test: "Abdominal ultrasound", status: "pending", orderedAt: now - 0.5 * H },
    { id: nid("o"), visitId: "v6", patientId: "p6", kind: "lab", test: "Repeat malaria smear", status: "pending", orderedAt: now - 3 * H },
  ];
  const prescriptions = [
    { id: nid("rx"), visitId: "v3", patientId: "p3", drug: "Amoxicillin 250mg", dose: "500mg TDS x5 days", qty: 15, status: "pending" },
    { id: nid("rx"), visitId: "v3", patientId: "p3", drug: "Paracetamol 500mg", dose: "1g TDS x5 days", qty: 15, status: "pending" },
    { id: nid("rx"), visitId: "v4", patientId: "p4", drug: "Salbutamol inhaler", dose: "2 puffs PRN", qty: 1, status: "pending" },
    { id: nid("rx"), visitId: "v4", patientId: "p4", drug: "Prednisolone 5mg", dose: "30mg OD x5 days", qty: 30, status: "pending" },
    { id: nid("rx"), visitId: "v5", patientId: "p5", drug: "Paracetamol 500mg", dose: "1g TDS x5 days", qty: 15, status: "dispensed", dispensedAt: now - 1.8 * H },
  ];
  const inventory = [
    { id: nid("i"), drug: "Paracetamol 500mg", stock: 2400, reorder: 500, unit: "tabs" },
    { id: nid("i"), drug: "Amoxicillin 250mg", stock: 320, reorder: 400, unit: "caps" },
    { id: nid("i"), drug: "Salbutamol inhaler", stock: 14, reorder: 10, unit: "units" },
    { id: nid("i"), drug: "Prednisolone 5mg", stock: 180, reorder: 100, unit: "tabs" },
    { id: nid("i"), drug: "Metformin 850mg", stock: 640, reorder: 200, unit: "tabs" },
    { id: nid("i"), drug: "IV Artesunate", stock: 9, reorder: 12, unit: "vials" },
    { id: nid("i"), drug: "Iron + Folic acid", stock: 560, reorder: 150, unit: "tabs" },
  ];
  const wards = [
    { name: "General Ward", beds: ["G1", "G2", "G3", "G4", "G5", "G6"] },
    { name: "Maternity", beds: ["M1", "M2", "M3"] },
    { name: "Pediatrics", beds: ["P1", "P2", "P3", "P4"] },
    { name: "ICU", beds: ["I1", "I2"] },
  ];
  const bedState = {};
  wards.forEach((w) => w.beds.forEach((b) => { bedState[b] = b === "G2" ? "v6" : b === "M1" ? "v7" : null; }));
  const staff = [
    { id: "s1", name: "Grace Wanjiru", role: "Reception", dept: "Front Desk", phone: "0710 000 001", since: "2021" },
    { id: "s2", name: "Nurse Achieng", role: "Nurse", dept: "OPD / General", phone: "0710 000 002", since: "2019" },
    { id: "s3", name: "Nurse Kamau", role: "Nurse", dept: "General Ward", phone: "0710 000 003", since: "2022" },
    { id: "s4", name: "Dr. Kimani", role: "Doctor", dept: "General Medicine", phone: "0710 000 004", since: "2018" },
    { id: "s5", name: "Dr. Aisha Noor", role: "Doctor", dept: "Pediatrics", phone: "0710 000 005", since: "2020" },
    { id: "s6", name: "Lab Tech Otieno", role: "Lab", dept: "Laboratory", phone: "0710 000 006", since: "2020" },
    { id: "s7", name: "Rad Tech Mutua", role: "Radiology", dept: "Imaging", phone: "0710 000 007", since: "2023" },
    { id: "s8", name: "Pharm. Njeri", role: "Pharmacy", dept: "Pharmacy", phone: "0710 000 008", since: "2017" },
    { id: "s9", name: "Finance Barasa", role: "Finance", dept: "Cash Office", phone: "0710 000 009", since: "2021" },
    { id: "s10", name: "HR Chebet", role: "HR", dept: "Human Resources", phone: "0710 000 010", since: "2022" },
  ];
  const leave = [
    { id: nid("l"), staffId: "s2", type: "Annual", from: "2026-09-24", to: "2026-10-02", status: "pending" },
    { id: nid("l"), staffId: "s6", type: "Sick", from: "2026-09-16", to: "2026-09-18", status: "pending" },
    { id: nid("l"), staffId: "s10", type: "Maternity", from: "2026-11-01", to: "2027-01-30", status: "approved" },
  ];
  const roster = {};
  ["Mon", "Tue", "Wed", "Thu", "Fri"].forEach((dy) => { roster[dy] = { Morning: "Nurse Achieng", Evening: "Nurse Kamau" }; });
  const users = [
    { id: "u1", name: "Grace Wanjiru", role: "reception", active: true },
    { id: "u2", name: "Nurse Achieng", role: "nurse", active: true },
    { id: "u3", name: "Dr. Kimani", role: "doctor", active: true },
    { id: "u4", name: "Lab Tech Otieno", role: "lab", active: true },
    { id: "u5", name: "Rad Tech Mutua", role: "radiology", active: true },
    { id: "u6", name: "Pharm. Njeri", role: "pharmacy", active: true },
    { id: "u7", name: "Finance Barasa", role: "finance", active: true },
    { id: "u8", name: "Admin Mwangi", role: "admin", active: true },
    { id: "u9", name: "S. Admin Njoki", role: "superadmin", active: true },
    { id: "u10", name: "HR Chebet", role: "hr", active: true },
  ];
  const audit = [
    { ts: now - 5 * H, role: "Doctor", user: "Dr. Kimani", action: "Closed consult for Faith Auma (visit v8)" },
    { ts: now - 2 * H, role: "Lab", user: "Lab Tech Otieno", action: "Posted result for Widal/FBC (visit v3)" },
    { ts: now - 1.5 * H, role: "Finance", user: "Finance Barasa", action: "Received KSh 800 via M-PESA (visit v8)" },
  ];
  const appointments = [
    { id: nid("a"), name: "Zawadi Nzila", dept: "General Medicine", time: "09:30", status: "booked" },
    { id: nid("a"), name: "Kevin Mwendwa", dept: "Pediatrics", time: "11:00", status: "booked" },
    { id: nid("a"), name: "Rose Kilonzo", dept: "Obs & Gynae", time: "14:00", status: "booked" },
  ];
  const payments = [{ id: nid("pay"), visitId: "v8", patient: "Faith Auma", amt: 800, method: "M-PESA", at: now - 1.5 * H }];
  const config = { consultFee: 800, labFee: 500, radFee: 1500, bedPerNight: 3000, mpesaEnabled: true, cashEnabled: true, departments: ["General Medicine", "Pediatrics", "Obs & Gynae", "Imaging", "Laboratory"] };
  return { patients, visits, orders, prescriptions, inventory, wards, bedState, staff, leave, roster, users, audit, appointments, payments, config };
};

/* ---------- UI kit ---------- */
const Badge = ({ children, tone = "slate" }) => {
  const tones = { slate: "bg-slate-100 text-slate-700", amber: "bg-amber-100 text-amber-800", green: "bg-emerald-100 text-emerald-700", red: "bg-red-100 text-red-700", blue: "bg-blue-100 text-blue-700", violet: "bg-violet-100 text-violet-700" };
  return <span className={`text-[11px] px-2 py-0.5 rounded-full font-medium whitespace-nowrap ${tones[tone]}`}>{children}</span>;
};
const Btn = ({ children, onClick, tone = "primary", disabled }) => {
  const tones = { primary: "bg-blue-600 hover:bg-blue-700 text-white", subtle: "bg-slate-100 hover:bg-slate-200 text-slate-700", danger: "bg-red-600 hover:bg-red-700 text-white", green: "bg-emerald-600 hover:bg-emerald-700 text-white" };
  return <button onClick={onClick} disabled={disabled} className={`text-sm px-3 py-1.5 rounded-lg font-medium transition disabled:opacity-40 disabled:cursor-not-allowed ${tones[tone]}`}>{children}</button>;
};
const Card = ({ children, className = "" }) => <div className={`bg-white border border-slate-200 rounded-xl ${className}`}>{children}</div>;
const Empty = ({ children }) => <div className="text-sm text-slate-400 py-8 text-center">{children}</div>;
const Field = ({ label, children }) => <label className="block text-xs font-medium text-slate-500">{label}<div className="mt-1">{children}</div></label>;
const inputCls = "w-full text-sm border border-slate-300 rounded-lg px-3 py-1.5 focus:outline-none focus:ring-2 focus:ring-blue-200";

const FlowHint = ({ children }) => (
  <div className="mb-4 flex items-start gap-2 bg-blue-50 border border-blue-100 rounded-lg px-3 py-2 text-xs text-blue-800">
    <span className="font-semibold shrink-0">In the flow:</span><span>{children}</span>
  </div>
);
const Screen = ({ title, sub, flow, children }) => (
  <div>
    <div className="mb-3">
      <h2 className="text-xl font-semibold text-slate-800">{title}</h2>
      {sub && <p className="text-sm text-slate-500 mt-0.5">{sub}</p>}
    </div>
    {flow && <FlowHint>{flow}</FlowHint>}
    {children}
  </div>
);
const Stat = ({ label, value, tone }) => (
  <Card className="p-4">
    <div className={`text-2xl font-semibold ${tone || ""}`}>{value}</div>
    <div className="text-xs text-slate-500 mt-1">{label}</div>
  </Card>
);

/* ---------- App ---------- */
export default function App() {
  const [db, setDb] = useState(seed);
  const [sess, setSess] = useState(null);
  const [page, setPage] = useState(null);
  const [focus, setFocus] = useState(null);

  const mut = (fn, auditEntry) =>
    setDb((old) => {
      const next = {
        ...old,
        visits: [...old.visits],
        patients: [...old.patients],
        orders: [...old.orders],
        prescriptions: [...old.prescriptions],
        appointments: [...old.appointments],
        payments: [...old.payments],
        audit: [...old.audit],
        users: [...old.users],
        leave: [...old.leave],
        staff: [...old.staff],
        inventory: [...old.inventory],
        wards: [...old.wards],
        bedState: { ...old.bedState },
        roster: Object.fromEntries(Object.entries(old.roster).map(([k, v]) => [k, { ...v }])),
        config: { ...old.config, departments: [...old.config.departments] },
      };
      fn(next);
      if (auditEntry) next.audit = [{ ts: Date.now(), role: ROLES[auditEntry.role].label, user: ROLES[auditEntry.role].user, action: auditEntry.action }, ...next.audit];
      return next;
    });

  const go = (k, visitId) => { setPage(k); setFocus(visitId || null); };
  if (!sess) return <Login onPick={(role) => { setSess({ role }); setPage(NAV[role][0].k); setFocus(null); }} />;

  const role = sess.role;
  const rinfo = ROLES[role];
  const nav = NAV[role];
  const cur = page && nav.some((n) => n.k === page) ? page : nav[0].k;

  const props = { db, mut, go, focus };
  let body;
  switch (role + ":" + cur) {
    case "reception:home": body = <ReceptionHome {...props} />; break;
    case "reception:register": body = <RegisterPatient {...props} />; break;
    case "reception:search": body = <PatientSearch {...props} />; break;
    case "reception:appointments": body = <Appointments {...props} />; break;
    case "nurse:triage": body = <NurseTriage {...props} />; break;
    case "nurse:wards": body = <NurseWards {...props} />; break;
    case "nurse:orders": body = <NurseOrders {...props} />; break;
    case "doctor:queue": body = <DoctorQueue {...props} />; break;
    case "doctor:active": body = <DoctorActive {...props} />; break;
    case "doctor:admitted": body = <DoctorAdmitted {...props} />; break;
    case "lab:queue": body = <OrdersQueue {...props} kind="lab" />; break;
    case "lab:done": body = <OrdersDone {...props} kind="lab" />; break;
    case "radiology:queue": body = <OrdersQueue {...props} kind="radiology" />; break;
    case "radiology:done": body = <OrdersDone {...props} kind="radiology" />; break;
    case "pharmacy:queue": body = <PharmacyQueue {...props} />; break;
    case "pharmacy:inventory": body = <Inventory {...props} />; break;
    case "finance:billing": body = <FinanceBilling {...props} />; break;
    case "finance:receipts": body = <Receipts {...props} />; break;
    case "admin:overview": body = <AdminOverview {...props} />; break;
    case "admin:flow": body = <AdminFlow {...props} />; break;
    case "admin:beds": body = <AdminBeds {...props} />; break;
    case "superadmin:users": body = <UserMgmt {...props} />; break;
    case "superadmin:audit": body = <AuditTrail {...props} />; break;
    case "superadmin:config": body = <SysConfig {...props} />; break;
    case "hr:staff": body = <StaffRecords {...props} />; break;
    case "hr:leave": body = <LeaveMgmt {...props} />; break;
    case "hr:roster": body = <Roster {...props} />; break;
    default: body = <Empty>Screen not found</Empty>;
  }

  return (
    <div className="flex h-screen bg-slate-100 text-slate-800 font-sans">
      <aside className="w-64 bg-slate-900 text-slate-300 flex flex-col shrink-0">
        <div className="px-4 py-4 border-b border-slate-800">
          <div className="text-white font-semibold text-sm">Riverside Hospital</div>
          <div className={`mt-1.5 inline-flex text-[11px] px-2 py-0.5 rounded-full text-white ${rinfo.color}`}>{rinfo.label}</div>
        </div>
        <nav className="flex-1 py-2 overflow-y-auto">
          {nav.map((n) => {
            const count = n.badge ? n.badge(db) : 0;
            return (
              <button key={n.k} onClick={() => go(n.k)} className={`w-full flex items-center gap-2.5 px-4 py-2.5 text-sm text-left hover:bg-slate-800 ${cur === n.k ? "bg-slate-800 text-white border-l-2 border-blue-500" : "border-l-2 border-transparent"}`}>
                <span className="w-5 shrink-0">{n.icon}</span>
                <span className="flex-1">{n.label}</span>
                {count > 0 && <span className="bg-blue-600 text-white text-[10px] px-1.5 py-0.5 rounded-full">{count}</span>}
              </button>
            );
          })}
        </nav>
        <div className="px-4 py-3 border-t border-slate-800 text-xs text-slate-400">
          <div className="font-medium text-slate-200">{rinfo.user}</div>
          <button onClick={() => setSess(null)} className="mt-2 hover:text-white">← Switch role</button>
        </div>
      </aside>
      <main className="flex-1 overflow-y-auto p-6">{body}</main>
    </div>
  );
}

const Login = ({ onPick }) => (
  <div className="min-h-screen bg-slate-900 flex items-center justify-center p-8 font-sans">
    <div className="max-w-4xl w-full">
      <div className="text-center mb-8">
        <h1 className="text-2xl font-semibold text-white">Riverside Hospital — Management System</h1>
        <p className="text-slate-400 text-sm mt-1">Select a role to sign in. Each role sees only its own workspace — nothing shared, nothing disabled.</p>
      </div>
      <div className="grid grid-cols-2 md:grid-cols-5 gap-3">
        {Object.entries(ROLES).map(([k, r]) => (
          <button key={k} onClick={() => onPick(k)} className="text-left bg-slate-800 hover:bg-slate-700 border border-slate-700 rounded-xl p-4 transition">
            <div className={`w-8 h-8 rounded-lg ${r.color} flex items-center justify-center text-white text-[11px] font-bold mb-2`}>{r.label.slice(0, 2).toUpperCase()}</div>
            <div className="text-white text-sm font-medium">{r.label}</div>
            <div className="text-slate-400 text-[11px] mt-1 leading-snug">{r.tag}</div>
          </button>
        ))}
      </div>
    </div>
  </div>
);

/* ==================== RECEPTION ==================== */
const ReceptionHome = ({ db, go }) => {
  const open = db.visits.filter((v) => v.status !== "closed");
  const P = (id) => db.patients.find((p) => p.id === id);
  return (
    <Screen title="Front Desk" sub="Today at a glance" flow="You open every visit. Register or look up a patient, then start a visit — outpatient or admission. Clinical work happens elsewhere; your job is getting the patient into the flow.">
      <div className="grid grid-cols-3 gap-4 mb-6">
        <Stat label="Open visits" value={open.length} />
        <Stat label="Appointments today" value={db.appointments.filter((a) => a.status === "booked").length} />
        <Stat label="Registered patients" value={db.patients.length} />
      </div>
      <div className="grid grid-cols-2 gap-4">
        <Card className="p-4">
          <h3 className="font-medium text-sm mb-3">Quick actions</h3>
          <div className="flex flex-col gap-2 items-start">
            <Btn onClick={() => go("register")}>+ Register new patient</Btn>
            <Btn tone="subtle" onClick={() => go("search")}>Find patient & start visit</Btn>
            <Btn tone="subtle" onClick={() => go("appointments")}>Book an appointment</Btn>
          </div>
        </Card>
        <Card className="p-4">
          <h3 className="font-medium text-sm mb-3">Open visits</h3>
          {open.length === 0 ? <Empty>No open visits</Empty> : open.map((v) => (
            <div key={v.id} className="flex items-center justify-between py-2 border-b last:border-0 border-slate-100">
              <div><div className="text-sm font-medium">{P(v.patientId)?.name}</div><div className="text-xs text-slate-400">{v.type === "admission" ? "Admission" : "Outpatient"} · opened {t(v.openedAt)}</div></div>
              <Badge tone={v.type === "admission" ? "violet" : "blue"}>{STAGE[v.status]} → {NEXT_OWNER[v.status]}</Badge>
            </div>
          ))}
        </Card>
      </div>
    </Screen>
  );
};

const RegisterPatient = ({ mut, go }) => {
  const [f, setF] = useState({ name: "", sex: "F", age: "", phone: "", allergies: "", chronic: "" });
  const [done, setDone] = useState(false);
  const submit = () => {
    if (!f.name || !f.age) return;
    const name = f.name;
    mut((db) => {
      db.patients.unshift({ id: nid("p"), mrn: "MRN-" + (2119 + db.patients.length), name, sex: f.sex, age: +f.age, phone: f.phone, allergies: f.allergies.split(",").map((s) => s.trim()).filter(Boolean), chronic: f.chronic.split(",").map((s) => s.trim()).filter(Boolean), notes: "" });
    }, { role: "reception", action: `Registered new patient ${name}` });
    setDone(true);
    setF({ name: "", sex: "F", age: "", phone: "", allergies: "", chronic: "" });
  };
  return (
    <Screen title="Register Patient" flow="Step 1 of the visit. After registering, open a visit for the patient — outpatient or admission — which puts them into the clinical flow.">
      <Card className="p-5 max-w-xl">
        {done ? (
          <div className="text-center py-4">
            <div className="text-emerald-600 font-medium mb-1">Patient registered ✓</div>
            <p className="text-sm text-slate-500 mb-4">Next step: open a visit for them.</p>
            <div className="flex gap-2 justify-center"><Btn onClick={() => go("search")}>Find patient & start visit</Btn><Btn tone="subtle" onClick={() => setDone(false)}>Register another</Btn></div>
          </div>
        ) : (
          <div className="grid grid-cols-2 gap-3">
            <Field label="Full name"><input className={inputCls} value={f.name} onChange={(e) => setF({ ...f, name: e.target.value })} /></Field>
            <Field label="Phone"><input className={inputCls} value={f.phone} onChange={(e) => setF({ ...f, phone: e.target.value })} /></Field>
            <Field label="Sex"><select className={inputCls} value={f.sex} onChange={(e) => setF({ ...f, sex: e.target.value })}><option value="F">F</option><option value="M">M</option></select></Field>
            <Field label="Age"><input className={inputCls} value={f.age} onChange={(e) => setF({ ...f, age: e.target.value })} /></Field>
            <Field label="Known allergies (comma-separated)"><input className={inputCls} value={f.allergies} onChange={(e) => setF({ ...f, allergies: e.target.value })} placeholder="e.g. Penicillin" /></Field>
            <Field label="Chronic conditions (comma-separated)"><input className={inputCls} value={f.chronic} onChange={(e) => setF({ ...f, chronic: e.target.value })} /></Field>
            <div className="col-span-2"><Btn onClick={submit} disabled={!f.name || !f.age}>Register patient</Btn></div>
          </div>
        )}
      </Card>
    </Screen>
  );
};

const PatientSearch = ({ db, mut }) => {
  const [q, setQ] = useState("");
  const [starting, setStarting] = useState(null);
  const results = db.patients.filter((p) => (p.name + p.mrn + p.phone).toLowerCase().includes(q.toLowerCase()));
  const freeBeds = Object.entries(db.bedState).filter(([, occ]) => !occ).map(([b]) => b);
  const openVisit = (pid, type, bedId) => {
    const vid = nid("v");
    const pname = db.patients.find((x) => x.id === pid)?.name;
    mut((db) => {
      db.visits.unshift({ id: vid, patientId: pid, type, status: type === "admission" ? "admitted" : "vitals", openedAt: Date.now(), complaint: "", assignedDoctor: null, orders: [], billItems: [], charts: type === "admission" ? [] : undefined, admissionNote: type === "admission" ? "Admitted via front desk." : undefined, bedId });
      if (bedId) db.bedState[bedId] = vid;
    }, { role: "reception", action: `Opened ${type} visit for ${pname}${bedId ? ` — bed ${bedId}` : ""}` });
    setStarting(null);
  };
  return (
    <Screen title="Patient Search" sub="Look up a patient, check their visit status, or start a new visit" flow="Reception's core action: find patient → start visit → outpatient (goes to nurse triage) or admission (a bed must be assigned now).">
      <input className={inputCls + " mb-4 max-w-md"} placeholder="Search by name, MRN or phone…" value={q} onChange={(e) => setQ(e.target.value)} />
      <div className="space-y-3">
        {results.map((p) => {
          const active = db.visits.find((v) => v.patientId === p.id && v.status !== "closed");
          return (
            <Card key={p.id} className="p-4">
              <div className="flex items-center justify-between gap-4">
                <div>
                  <div className="flex items-center gap-2 flex-wrap"><span className="font-medium">{p.name}</span><span className="text-xs text-slate-400">{p.mrn} · {p.sex} · {p.age}y · {p.phone}</span></div>
                  {p.allergies.length > 0 && <div className="mt-1"><Badge tone="red">⚠ Allergies: {p.allergies.join(", ")}</Badge></div>}
                </div>
                <div className="flex items-center gap-2">
                  {active ? <Badge tone="amber">Active visit — {STAGE[active.status]} (next: {NEXT_OWNER[active.status]})</Badge> : <Btn tone="subtle" onClick={() => setStarting(starting === p.id ? null : p.id)}>Start visit ▾</Btn>}
                </div>
              </div>
              {starting === p.id && !active && (
                <div className="mt-3 pt-3 border-t border-slate-100">
                  <div className="flex gap-2">
                    <Btn onClick={() => openVisit(p.id, "outpatient")}>Outpatient visit</Btn>
                    <Btn tone="subtle" onClick={() => setStarting("bed-" + p.id)}>Admission — choose bed…</Btn>
                  </div>
                  {starting === "bed-" + p.id && (
                    <div className="mt-3">
                      {freeBeds.length === 0 ? <div className="text-xs text-red-600">No free beds — admission cannot proceed.</div> : (
                        <div className="flex flex-wrap gap-2 items-center">
                          <span className="text-xs text-slate-500">Assign bed:</span>
                          {freeBeds.map((b) => <button key={b} onClick={() => openVisit(p.id, "admission", b)} className="text-xs border border-emerald-300 bg-emerald-50 text-emerald-700 px-2 py-1 rounded hover:bg-emerald-100">{b}</button>)}
                        </div>
                      )}
                    </div>
                  )}
                </div>
              )}
            </Card>
          );
        })}
        {results.length === 0 && <Empty>No patients match "{q}"</Empty>}
      </div>
    </Screen>
  );
};

const Appointments = ({ db, mut }) => {
  const [f, setF] = useState({ name: "", dept: "General Medicine", time: "09:00" });
  const book = () => {
    if (!f.name) return;
    mut((db) => { db.appointments.push({ id: nid("a"), name: f.name, dept: f.dept, time: f.time, status: "booked" }); }, { role: "reception", action: `Booked appointment: ${f.name}, ${f.dept} at ${f.time}` });
    setF({ ...f, name: "" });
  };
  return (
    <Screen title="Appointments" flow="Appointments feed the front desk: arriving patients are checked in here, then a visit is opened from Patient Search.">
      <div className="grid grid-cols-3 gap-4">
        <Card className="p-4">
          <h3 className="font-medium text-sm mb-3">Book appointment</h3>
          <div className="space-y-2">
            <Field label="Patient name"><input className={inputCls} value={f.name} onChange={(e) => setF({ ...f, name: e.target.value })} /></Field>
            <Field label="Department"><select className={inputCls} value={f.dept} onChange={(e) => setF({ ...f, dept: e.target.value })}>{db.config.departments.map((dp) => <option key={dp}>{dp}</option>)}</select></Field>
            <Field label="Time"><input className={inputCls} value={f.time} onChange={(e) => setF({ ...f, time: e.target.value })} /></Field>
            <Btn onClick={book} disabled={!f.name}>Book</Btn>
          </div>
        </Card>
        <Card className="p-4 col-span-2">
          <h3 className="font-medium text-sm mb-3">Today's schedule</h3>
          {db.appointments.length === 0 ? <Empty>No appointments booked</Empty> : [...db.appointments].sort((a, b) => a.time.localeCompare(b.time)).map((a) => (
            <div key={a.id} className="flex items-center justify-between py-2 border-b last:border-0 border-slate-100">
              <div><span className="text-sm font-medium">{a.name}</span><span className="text-xs text-slate-400 ml-2">{a.dept}</span></div>
              <div className="flex items-center gap-2"><span className="text-sm">{a.time}</span><Badge tone={a.status === "booked" ? "blue" : "green"}>{a.status}</Badge></div>
            </div>
          ))}
        </Card>
      </div>
    </Screen>
  );
};

/* ==================== NURSE ==================== */
const NurseTriage = ({ db, mut }) => {
  const [formFor, setFormFor] = useState(null);
  const [v, setV] = useState({ bp: "", temp: "", hr: "", spo2: "", wt: "" });
  const list = db.visits.filter((x) => x.type === "outpatient" && x.status === "vitals");
  const save = (vt) => {
    if (!v.bp || !v.temp) return;
    const pname = db.patients.find((x) => x.id === vt.patientId)?.name;
    mut((db) => {
      const x = db.visits.find((y) => y.id === vt.id);
      x.vitals = { bp: v.bp, temp: +v.temp, hr: +v.hr, spo2: +v.spo2, wt: +v.wt };
      x.status = "queued"; x.queuedAt = Date.now();
    }, { role: "nurse", action: `Recorded vitals, sent ${pname} to doctor's queue` });
    setFormFor(null); setV({ bp: "", temp: "", hr: "", spo2: "", wt: "" });
  };
  return (
    <Screen title="Triage — Awaiting Vitals" sub="Outpatients opened by reception, waiting for vitals before joining the doctor's queue" flow="You are the first clinical step for outpatients. Take vitals → the patient enters the doctor's queue. Admitted patients are charted under Ward Charting.">
      {list.length === 0 ? <Empty>No patients waiting for vitals</Empty> : list.map((vt) => {
        const p = db.patients.find((x) => x.id === vt.patientId);
        return (
          <Card key={vt.id} className="p-4 mb-3">
            <div className="flex items-center justify-between">
              <div><div className="font-medium">{p.name} <span className="text-xs text-slate-400">{p.mrn}</span> {p.allergies.length > 0 && <Badge tone="red">⚠ {p.allergies.join(", ")}</Badge>}</div><div className="text-xs text-slate-500">Complaint: {vt.complaint || "—"}</div></div>
              <Btn onClick={() => setFormFor(formFor === vt.id ? null : vt.id)}>Take vitals</Btn>
            </div>
            {formFor === vt.id && (
              <div className="mt-3 pt-3 border-t">
                <div className="grid grid-cols-5 gap-2">
                  {[["bp", "BP"], ["temp", "Temp °C"], ["hr", "HR"], ["spo2", "SpO₂ %"], ["wt", "Weight kg"]].map(([k, l]) => <Field key={k} label={l}><input className={inputCls} value={v[k]} onChange={(e) => setV({ ...v, [k]: e.target.value })} /></Field>)}
                </div>
                <div className="mt-3"><Btn tone="green" onClick={() => save(vt)}>Send to doctor's queue</Btn></div>
              </div>
            )}
          </Card>
        );
      })}
    </Screen>
  );
};

const NurseWards = ({ db, mut }) => {
  const adm = db.visits.filter((v) => v.type === "admission" && v.status === "admitted");
  const [open, setOpen] = useState(null);
  const [c, setC] = useState({ vitals: "", note: "" });
  const chart = (vt) => {
    if (!c.vitals) return;
    const pname = db.patients.find((x) => x.id === vt.patientId)?.name;
    mut((db) => { db.visits.find((x) => x.id === vt.id).charts.push({ at: Date.now(), vitals: c.vitals, note: c.note, by: "Nurse Achieng" }); }, { role: "nurse", action: `Charted ward observation for ${pname} (${vt.bedId})` });
    setOpen(null); setC({ vitals: "", note: "" });
  };
  return (
    <Screen title="Ward Charting" sub="Admitted patients — chart observations repeatedly over the stay" flow="For admissions you chart at intervals. Doctors read your charts on rounds and issue daily orders against them.">
      {adm.length === 0 ? <Empty>No admitted patients</Empty> : adm.map((vt) => {
        const p = db.patients.find((x) => x.id === vt.patientId);
        return (
          <Card key={vt.id} className="p-4 mb-3">
            <div className="flex items-center justify-between">
              <div><div className="font-medium">{p.name} <span className="text-xs text-slate-400">Bed {vt.bedId} · day {Math.max(1, Math.ceil((Date.now() - vt.openedAt) / 86400e3))}</span> {p.allergies.length > 0 && <Badge tone="red">⚠ {p.allergies.join(", ")}</Badge>}</div><div className="text-xs text-slate-500">{vt.complaint}</div></div>
              <Btn onClick={() => setOpen(open === vt.id ? null : vt.id)}>Add chart entry</Btn>
            </div>
            {open === vt.id && (
              <div className="mt-3 pt-3 border-t space-y-2">
                <Field label="Vitals summary"><input className={inputCls} value={c.vitals} onChange={(e) => setC({ ...c, vitals: e.target.value })} placeholder="112/70 · 37.0°C · HR 84 · SpO2 98" /></Field>
                <Field label="Observation note"><textarea className={inputCls} rows={2} value={c.note} onChange={(e) => setC({ ...c, note: e.target.value })} /></Field>
                <Btn tone="green" onClick={() => chart(vt)}>Save chart entry</Btn>
              </div>
            )}
            <div className="mt-2 space-y-1">{(vt.charts || []).slice(-2).reverse().map((ch, i) => <div key={i} className="text-xs text-slate-500 bg-slate-50 rounded px-2 py-1">{t(ch.at)} · {ch.by} — {ch.vitals} — {ch.note}</div>)}</div>
          </Card>
        );
      })}
    </Screen>
  );
};

const NurseOrders = ({ db }) => {
  const all = [...db.orders].reverse();
  return (
    <Screen title="Doctor's Orders" sub="View-only — orders are entered by doctors and cannot be edited here" flow="You execute and monitor orders for your patients. Any change must come from a doctor; this list is deliberately read-only.">
      {all.length === 0 ? <Empty>No orders</Empty> : all.map((o) => (
        <Card key={o.id} className="p-3 mb-2 flex items-center justify-between">
          <div><div className="text-sm font-medium">{o.test} <span className="text-xs text-slate-400">· {db.patients.find((p) => p.id === o.patientId)?.name}</span></div><div className="text-xs text-slate-400">{o.kind === "lab" ? "Lab" : "Radiology"} · ordered {t(o.orderedAt)}</div></div>
          <div className="flex items-center gap-2 max-w-md"><span className="text-xs text-slate-500 truncate">{o.result || ""}</span><Badge tone={o.status === "pending" ? "amber" : "green"}>{o.status}</Badge></div>
        </Card>
      ))}
    </Screen>
  );
};

/* ==================== DOCTOR ==================== */
const DoctorQueue = ({ db, mut, go }) => {
  const q = db.visits.filter((v) => v.status === "queued").sort((a, b) => a.queuedAt - b.queuedAt);
  const pick = (vt) => {
    const pname = db.patients.find((x) => x.id === vt.patientId)?.name;
    mut((db) => {
      const x = db.visits.find((y) => y.id === vt.id);
      x.status = "consult"; x.assignedDoctor = "Dr. Kimani";
    }, { role: "doctor", action: `Picked ${pname} from queue` });
    go("active", vt.id);
  };
  return (
    <Screen title="My Queue" sub="Outpatients with vitals taken, waiting in order" flow="Nurses send vitals-complete patients here. Pick the next patient → the consult opens with history and allergy flags.">
      {q.length === 0 ? <Empty>Queue is empty</Empty> : q.map((vt, i) => {
        const p = db.patients.find((x) => x.id === vt.patientId);
        return (
          <Card key={vt.id} className="p-4 mb-3">
            <div className="flex items-center justify-between">
              <div className="flex items-center gap-3">
                <div className="w-8 h-8 rounded-full bg-blue-600 text-white flex items-center justify-center text-sm font-semibold shrink-0">{i + 1}</div>
                <div><div className="font-medium">{p.name} {p.allergies.length > 0 && <Badge tone="red">⚠ {p.allergies.join(", ")}</Badge>}</div><div className="text-xs text-slate-500">{vt.complaint} · BP {vt.vitals.bp}, {vt.vitals.temp}°C, HR {vt.vitals.hr} · waiting {Math.round((Date.now() - vt.queuedAt) / 60000)} min</div></div>
              </div>
              <Btn onClick={() => pick(vt)}>Start consultation</Btn>
            </div>
          </Card>
        );
      })}
    </Screen>
  );
};

const Consult = ({ db, mut, vid, onDone }) => {
  const vt = db.visits.find((x) => x.id === vid);
  const p = db.patients.find((x) => x.id === vt.patientId);
  const history = db.visits.filter((x) => x.patientId === p.id && x.id !== vt.id);
  const [dx, setDx] = useState(vt.diagnosis || "");
  const [panel, setPanel] = useState(null);
  const [orderTest, setOrderTest] = useState("");
  const [rx, setRx] = useState({ drug: "", dose: "", qty: "" });
  const [refTo, setRefTo] = useState("");

  const addOrder = (kind) => {
    if (!orderTest) return;
    mut((db) => { db.orders.push({ id: nid("o"), visitId: vt.id, patientId: p.id, kind, test: orderTest, status: "pending", orderedAt: Date.now() }); }, { role: "doctor", action: `Ordered ${kind} "${orderTest}" for ${p.name}` });
    setOrderTest(""); setPanel(null);
  };
  const prescribe = () => {
    if (!rx.drug) return;
    mut((db) => { db.prescriptions.push({ id: nid("rx"), visitId: vt.id, patientId: p.id, drug: rx.drug, dose: rx.dose, qty: +rx.qty || 1, status: "pending" }); }, { role: "doctor", action: `Prescribed ${rx.drug} for ${p.name}` });
    setRx({ drug: "", dose: "", qty: "" });
  };
  const refer = () => {
    if (!refTo) return;
    mut((db) => { db.visits.find((x) => x.id === vt.id).referral = refTo; }, { role: "doctor", action: `Referred ${p.name} to ${refTo}` });
    setRefTo(""); setPanel(null);
  };
  const closeConsult = () => {
    const myRxPending = db.prescriptions.some((r) => r.visitId === vt.id && r.status === "pending");
    mut((db) => {
      const x = db.visits.find((y) => y.id === vt.id);
      x.diagnosis = dx; x.closedConsultAt = Date.now();
      x.status = myRxPending ? "pharmacy" : "billing";
    }, { role: "doctor", action: `Closed consult for ${p.name} (${vt.id})` });
    onDone();
  };
  const myOrders = db.orders.filter((o) => o.visitId === vt.id);
  const myRx = db.prescriptions.filter((x) => x.visitId === vt.id);
  const pendingResults = myOrders.filter((o) => o.status === "pending");

  return (
    <div>
      {p.allergies.length > 0 && (
        <div className="mb-4 bg-red-50 border border-red-200 text-red-800 rounded-xl px-4 py-3 text-sm font-medium">
          ⚠ ALLERGY ALERT — {p.allergies.join(", ")}. Check every prescription against this before sending it to pharmacy.
        </div>
      )}
      <div className="grid grid-cols-3 gap-4">
        <div className="col-span-2 space-y-4">
          <Card className="p-4">
            <div className="flex items-center justify-between mb-2">
              <div><div className="font-semibold">{p.name}</div><div className="text-xs text-slate-400">{p.mrn} · {p.sex}, {p.age} · {p.phone}</div></div>
              <Badge tone="blue">{STAGE[vt.status]}</Badge>
            </div>
            <div className="text-sm text-slate-600">Complaint: {vt.complaint || "—"}</div>
            {vt.vitals && <div className="text-xs text-slate-500 mt-1">Vitals: BP {vt.vitals.bp} · {vt.vitals.temp}°C · HR {vt.vitals.hr} · SpO₂ {vt.vitals.spo2}% · {vt.vitals.wt}kg</div>}
          </Card>
          <Card className="p-4">
            <h3 className="font-medium text-sm mb-2">Diagnosis & clinical notes</h3>
            <textarea className={inputCls} rows={3} placeholder="Working diagnosis, clinical notes…" value={dx} onChange={(e) => setDx(e.target.value)} onBlur={() => mut((db) => { db.visits.find((x) => x.id === vt.id).diagnosis = dx; })} />
          </Card>
          <Card className="p-4">
            <h3 className="font-medium text-sm mb-2">This visit's orders & prescriptions</h3>
            {myOrders.map((o) => (
              <div key={o.id} className="flex items-center justify-between py-1.5 text-sm border-b last:border-0 border-slate-100">
                <span>{o.kind === "lab" ? "🧪" : "🩻"} {o.test}</span>
                {o.status === "resulted" ? <Badge tone="green">Result: {o.result}</Badge> : <Badge tone="amber">awaiting {o.kind}</Badge>}
              </div>
            ))}
            {myRx.map((x) => <div key={x.id} className="flex items-center justify-between py-1.5 text-sm border-b last:border-0 border-slate-100"><span>💊 {x.drug} — {x.dose} ×{x.qty}</span><Badge tone={x.status === "pending" ? "amber" : "green"}>{x.status}</Badge></div>)}
            {myOrders.length + myRx.length === 0 && <Empty>No orders yet</Empty>}
            {pendingResults.length > 0 && <div className="mt-2 text-xs text-amber-700 bg-amber-50 rounded px-2 py-1.5">{pendingResults.length} result(s) outstanding — the visit cannot be closed until they are reviewed.</div>}
          </Card>
        </div>
        <div className="space-y-3">
          <Card className="p-4">
            <h3 className="font-medium text-sm mb-2">Next action</h3>
            <div className="flex flex-col gap-2 items-stretch">
              <Btn tone="subtle" onClick={() => setPanel(panel === "lab" ? null : "lab")}>🧪 Order lab</Btn>
              <Btn tone="subtle" onClick={() => setPanel(panel === "rad" ? null : "rad")}>🩻 Order radiology</Btn>
              <Btn tone="subtle" onClick={() => setPanel(panel === "rx" ? null : "rx")}>💊 Prescribe</Btn>
              <Btn tone="subtle" onClick={() => setPanel(panel === "ref" ? null : "ref")}>↗ Refer</Btn>
              <Btn tone="green" disabled={pendingResults.length > 0} onClick={closeConsult}>✓ Close consult</Btn>
            </div>
            {panel === "lab" && <div className="mt-2 flex gap-1"><input className={inputCls} placeholder="Test name" value={orderTest} onChange={(e) => setOrderTest(e.target.value)} /><Btn onClick={() => addOrder("lab")}>Add</Btn></div>}
            {panel === "rad" && <div className="mt-2 flex gap-1"><input className={inputCls} placeholder="Imaging study" value={orderTest} onChange={(e) => setOrderTest(e.target.value)} /><Btn onClick={() => addOrder("radiology")}>Add</Btn></div>}
            {panel === "rx" && <div className="mt-2 space-y-1"><input className={inputCls} placeholder="Drug name" value={rx.drug} onChange={(e) => setRx({ ...rx, drug: e.target.value })} /><input className={inputCls} placeholder="Dose, e.g. 500mg TDS x5d" value={rx.dose} onChange={(e) => setRx({ ...rx, dose: e.target.value })} /><input className={inputCls} placeholder="Quantity" value={rx.qty} onChange={(e) => setRx({ ...rx, qty: e.target.value })} /><Btn tone="green" onClick={prescribe}>Send to pharmacy</Btn></div>}
            {panel === "ref" && <div className="mt-2 flex gap-1"><input className={inputCls} placeholder="Refer to (dept / hospital)" value={refTo} onChange={(e) => setRefTo(e.target.value)} /><Btn onClick={refer}>Refer</Btn></div>}
          </Card>
          <Card className="p-4">
            <h3 className="font-medium text-sm mb-2">History & safety</h3>
            <div className="text-xs space-y-1">
              {p.chronic.length > 0 && <div><span className="text-slate-400">Chronic:</span> {p.chronic.join(", ")}</div>}
              {p.allergies.length > 0 && <div className="text-red-600 font-medium">Allergies: {p.allergies.join(", ")}</div>}
              {p.notes && <div className="text-slate-400">{p.notes}</div>}
              {history.filter((h) => h.diagnosis).map((h) => <div key={h.id} className="bg-slate-50 rounded px-2 py-1"><span className="text-slate-400">{d(h.openedAt)}</span> — {h.diagnosis}</div>)}
            </div>
          </Card>
        </div>
      </div>
      <div className="mt-4"><Btn tone="subtle" onClick={onDone}>← Back</Btn></div>
    </div>
  );
};

const DoctorActive = ({ db, mut, focus }) => {
  const [sel, setSel] = useState(focus);
  const mine = db.visits.filter((v) => v.assignedDoctor === "Dr. Kimani" && v.status === "consult");
  if (sel && mine.some((m) => m.id === sel)) {
    return <Screen title="Consultation" flow="You are mid-consult: review history and the allergy flag, then order, prescribe, refer, or close. Results posted by lab/radiology land back on this screen."><Consult db={db} mut={mut} vid={sel} onDone={() => setSel(null)} /></Screen>;
  }
  return (
    <Screen title="Active Consults" sub="Patients you are seeing now" flow="Results posted by lab/radiology arrive back on these cards — review them, then decide: more orders, prescription, referral, or close.">
      {mine.length === 0 ? <Empty>No active consults</Empty> : mine.map((vt) => {
        const p = db.patients.find((x) => x.id === vt.patientId);
        const hasResults = db.orders.some((o) => o.visitId === vt.id && o.status === "resulted");
        return (
          <Card key={vt.id} className="p-4 mb-3">
            <div className="flex items-center justify-between">
              <div><div className="font-medium">{p.name} {p.allergies.length > 0 && <Badge tone="red">⚠ allergies</Badge>}</div><div className="text-xs text-slate-500">{vt.complaint}{vt.diagnosis ? ` · ${vt.diagnosis}` : ""}</div></div>
              <div className="flex items-center gap-2">{hasResults && <Badge tone="blue">new result to review</Badge>}<Btn onClick={() => setSel(vt.id)}>Open consult</Btn></div>
            </div>
          </Card>
        );
      })}
    </Screen>
  );
};

const DoctorAdmitted = ({ db, mut }) => {
  const adm = db.visits.filter((v) => v.type === "admission" && v.status === "admitted");
  const [sel, setSel] = useState(null);
  const [note, setNote] = useState("");
  const [orderTest, setOrderTest] = useState("");
  return (
    <Screen title="Admitted Patients" sub="Ward rounds, daily orders and discharge decisions" flow="For admissions the visit runs for days: read nurse charting, issue daily orders, and when the patient is ready, write the discharge note — only then can Finance finalize the bill and the bed is released.">
      {adm.length === 0 ? <Empty>No admitted patients</Empty> : adm.map((x) => {
        const p = db.patients.find((q) => q.id === x.patientId);
        return (
          <Card key={x.id} className="p-4 mb-3">
            <div className="flex items-center justify-between">
              <div><div className="font-medium">{p.name} {p.allergies.length > 0 && <Badge tone="red">⚠ {p.allergies.join(", ")}</Badge>}</div><div className="text-xs text-slate-500">Bed {x.bedId} · day {Math.max(1, Math.ceil((Date.now() - x.openedAt) / 86400e3))} · {x.complaint}</div></div>
              <Btn tone={sel === x.id ? "subtle" : "primary"} onClick={() => { setSel(sel === x.id ? null : x.id); setNote(""); }}>{sel === x.id ? "Close panel" : "Open ward round"}</Btn>
            </div>
            {sel === x.id && (
              <div className="mt-3 pt-3 border-t border-slate-100 space-y-3">
                <div>
                  <h4 className="text-xs font-semibold text-slate-500 mb-1">Nurse charting & doctor rounds (latest first)</h4>
                  <div className="space-y-1">{[...(x.charts || [])].reverse().map((ch, i) => <div key={i} className="text-xs text-slate-500 bg-slate-50 rounded px-2 py-1">{t(ch.at)} · {ch.by} — {ch.vitals} {ch.vitals && "—"} {ch.note}</div>)}</div>
                </div>
                <div>
                  <Field label="Add daily order / round note"><textarea className={inputCls} rows={2} value={note} onChange={(e) => setNote(e.target.value)} placeholder="Continue IV artesunate, repeat smear tomorrow…" /></Field>
                  <div className="mt-2"><Btn tone="subtle" disabled={!note} onClick={() => { mut((db) => { db.visits.find((y) => y.id === x.id).charts.push({ at: Date.now(), vitals: "", note, by: "Dr. Kimani" }); }, { role: "doctor", action: `Daily order recorded for ${p.name} (bed ${x.bedId})` }); setNote(""); }}>Save round note</Btn></div>
                </div>
                <div className="flex gap-1 items-end">
                  <Field label="Order lab / radiology"><input className={inputCls} value={orderTest} onChange={(e) => setOrderTest(e.target.value)} placeholder="e.g. Repeat FBC" /></Field>
                  <Btn tone="subtle" disabled={!orderTest} onClick={() => { mut((db) => { db.orders.push({ id: nid("o"), visitId: x.id, patientId: x.patientId, kind: "lab", test: orderTest, status: "pending", orderedAt: Date.now() }); }, { role: "doctor", action: `Ordered "${orderTest}" for admitted patient ${p.name}` }); setOrderTest(""); }}>Order</Btn>
                </div>
                <div className="pt-2 border-t border-slate-100">
                  <p className="text-xs text-slate-500 mb-2">Discharge requires a doctor's note. Writing it moves the stay to Finance for bill finalization; the bed releases after payment.</p>
                  <Btn tone="danger" onClick={() => { mut((db) => { const y = db.visits.find((z) => z.id === x.id); y.dischargeNote = (y.diagnosis || x.complaint) + " — fit for discharge. Follow-up in 7 days."; y.status = "billing"; }, { role: "doctor", action: `Discharge note written for ${p.name} (bed ${x.bedId}) — awaiting billing` }); setSel(null); }}>Write discharge note & send to billing</Btn>
                </div>
              </div>
            )}
          </Card>
        );
      })}
    </Screen>
  );
};

/* ==================== LAB / RADIOLOGY (shared) ==================== */
const OrdersQueue = ({ db, mut, kind }) => {
  const [resFor, setResFor] = useState(null);
  const [result, setResult] = useState("");
  const list = db.orders.filter((o) => o.kind === kind && o.status === "pending");
  const post = (o) => {
    if (!result) return;
    const pname = db.patients.find((p) => p.id === o.patientId)?.name;
    mut((db) => {
      const x = db.orders.find((y) => y.id === o.id);
      x.status = "resulted"; x.result = result; x.resultedAt = Date.now();
    }, { role: kind, action: `Posted ${kind} result for ${pname}: ${o.test}` });
    setResFor(null); setResult("");
  };
  return (
    <Screen title={kind === "lab" ? "Incoming Lab Orders" : "Incoming Imaging Orders"} sub="Orders placed by doctors — post results back to the ordering doctor" flow="You only see orders of your own department. Enter results; they go straight back to the doctor's consult screen. No other clinical data is visible to you.">
      {list.length === 0 ? <Empty>No pending {kind} orders</Empty> : list.map((o) => {
        const p = db.patients.find((x) => x.id === o.patientId);
        return (
          <Card key={o.id} className="p-4 mb-3">
            <div className="flex items-center justify-between">
              <div><div className="font-medium">{o.test}</div><div className="text-xs text-slate-500">{p.name} · {p.sex}, {p.age} · ordered {t(o.orderedAt)}</div></div>
              <Btn onClick={() => setResFor(resFor === o.id ? null : o.id)}>Enter result</Btn>
            </div>
            {resFor === o.id && (
              <div className="mt-3 pt-3 border-t space-y-2">
                <Field label="Result"><textarea className={inputCls} rows={2} value={result} onChange={(e) => setResult(e.target.value)} /></Field>
                <Btn tone="green" onClick={() => post(o)}>Post result to doctor</Btn>
              </div>
            )}
          </Card>
        );
      })}
    </Screen>
  );
};

const OrdersDone = ({ db, kind }) => {
  const list = db.orders.filter((o) => o.kind === kind && o.status === "resulted").reverse();
  return (
    <Screen title="Posted Results" sub="Everything you have resulted" flow="Archive of your department's completed work.">
      {list.length === 0 ? <Empty>No results posted yet</Empty> : list.map((o) => (
        <Card key={o.id} className="p-3 mb-2">
          <div className="flex items-center justify-between">
            <div><div className="text-sm font-medium">{o.test} <span className="text-xs text-slate-400">· {db.patients.find((p) => p.id === o.patientId)?.name}</span></div><div className="text-xs text-slate-400">resulted {t(o.resultedAt)}</div></div>
            <Badge tone="green">{o.result}</Badge>
          </div>
        </Card>
      ))}
    </Screen>
  );
};

/* ==================== PHARMACY ==================== */
const PharmacyQueue = ({ db, mut }) => {
  const list = db.prescriptions.filter((x) => x.status === "pending");
  const stockFor = (drug) => db.inventory.find((i) => i.drug === drug);
  const dispense = (rx) => {
    const inv = stockFor(rx.drug);
    const pname = db.patients.find((p) => p.id === rx.patientId)?.name;
    mut((db) => {
      const x = db.prescriptions.find((y) => y.id === rx.id);
      x.status = "dispensed"; x.dispensedAt = Date.now();
      if (inv) db.inventory.find((i) => i.id === inv.id).stock -= rx.qty;
    }, { role: "pharmacy", action: `Dispensed ${rx.drug} ×${rx.qty} for ${pname}${inv ? ` (stock left: ${inv.stock - rx.qty})` : ""}` });
  };
  return (
    <Screen title="Prescription Queue" sub="Prescriptions sent by doctors — dispense against stock" flow="You are the fulfilment step after the doctor. Dispense, then the visit moves to Finance for billing. You see only what is prescribed — no diagnoses, no history.">
      {list.length === 0 ? <Empty>No prescriptions waiting</Empty> : list.map((rx) => {
        const p = db.patients.find((x) => x.id === rx.patientId);
        const inv = stockFor(rx.drug);
        const short = inv && inv.stock < rx.qty;
        return (
          <Card key={rx.id} className="p-4 mb-3">
            <div className="flex items-center justify-between">
              <div><div className="font-medium">{rx.drug} <span className="text-xs text-slate-400">— {rx.dose} · qty {rx.qty}</span></div><div className="text-xs text-slate-500">{p.name}{inv ? ` · in stock: ${inv.stock} ${inv.unit}` : " · not tracked in inventory"}</div></div>
              <div className="flex items-center gap-2">
                {short && <Badge tone="red">insufficient stock</Badge>}
                <Btn tone="green" disabled={!!short} onClick={() => dispense(rx)}>Dispense</Btn>
              </div>
            </div>
          </Card>
        );
      })}
    </Screen>
  );
};

const Inventory = ({ db, mut }) => {
  const restock = (item) => mut((db) => { db.inventory.find((i) => i.id === item.id).stock += 100; }, { role: "pharmacy", action: `Restocked ${item.drug} +100` });
  return (
    <Screen title="Drug Inventory" sub="Stock levels with reorder thresholds" flow="Dispensing decrements stock automatically. Items below the reorder level are flagged.">
      <Card className="p-4">
        <table className="w-full text-sm">
          <thead><tr className="text-left text-xs text-slate-400 border-b border-slate-200"><th className="py-2">Drug</th><th>Stock</th><th>Reorder at</th><th>Status</th><th></th></tr></thead>
          <tbody>
            {db.inventory.map((i) => (
              <tr key={i.id} className="border-b last:border-0 border-slate-100">
                <td className="py-2 font-medium">{i.drug}</td>
                <td>{i.stock} {i.unit}</td>
                <td className="text-slate-400">{i.reorder}</td>
                <td>{i.stock < i.reorder ? <Badge tone="red">below reorder level</Badge> : <Badge tone="green">ok</Badge>}</td>
                <td className="text-right"><Btn tone="subtle" onClick={() => restock(i)}>+100</Btn></td>
              </tr>
            ))}
          </tbody>
        </table>
      </Card>
    </Screen>
  );
};

/* ==================== FINANCE ==================== */
const billFor = (db, v) => {
  if (v.billItems && v.billItems.length) return { items: v.billItems, total: v.billItems.reduce((s, i) => s + i.amt, 0) };
  const items = [];
  if (v.assignedDoctor) items.push({ label: "Consultation", amt: db.config.consultFee });
  db.orders.filter((o) => o.visitId === v.id).forEach((o) => items.push({ label: (o.kind === "lab" ? "Lab: " : "Imaging: ") + o.test, amt: o.kind === "lab" ? db.config.labFee : db.config.radFee }));
  db.prescriptions.filter((x) => x.visitId === v.id).forEach((x) => items.push({ label: `Rx: ${x.drug} ×${x.qty}`, amt: x.qty * 30 }));
  if (v.type === "admission" && v.bedId) items.push({ label: `Bed ${v.bedId} (${Math.max(1, Math.ceil((Date.now() - v.openedAt) / 86400e3))} night(s))`, amt: Math.max(1, Math.ceil((Date.now() - v.openedAt) / 86400e3)) * db.config.bedPerNight });
  return { items, total: items.reduce((s, i) => s + i.amt, 0) };
};

const FinanceBilling = ({ db, mut }) => {
  const list = db.visits.filter((v) => v.status === "billing");
  const [method, setMethod] = useState({});
  const pay = (v) => {
    const m = method[v.id] || "Cash";
    const bill = billFor(db, v);
    const pname = db.patients.find((p) => p.id === v.patientId)?.name;
    mut((db) => {
      const x = db.visits.find((y) => y.id === v.id);
      x.status = "closed"; x.billItems = bill.items; x.billPaid = true; x.payMethod = m; x.paidAt = Date.now();
      if (x.type === "admission" && x.bedId) db.bedState[x.bedId] = null;
      db.payments.unshift({ id: nid("pay"), visitId: x.id, patient: pname, amt: bill.total, method: m, at: Date.now() });
    }, { role: "finance", action: `Received ${KSh(bill.total)} via ${m} for ${pname}${v.bedId ? " — bed " + v.bedId + " released" : ""}` });
  };
  return (
    <Screen title="Visits to Bill" sub="Visits closed by doctors, awaiting payment to finalize" flow="You are the last step of every visit. For admissions, payment also releases the bed. You see billing data only — no clinical notes.">
      {list.length === 0 ? <Empty>Nothing to bill</Empty> : list.map((v) => {
        const p = db.patients.find((x) => x.id === v.patientId);
        const bill = billFor(db, v);
        const methods = [db.config.cashEnabled && "Cash", db.config.mpesaEnabled && "M-PESA"].filter(Boolean);
        return (
          <Card key={v.id} className="p-4 mb-3">
            <div className="font-medium">{p.name} <span className="text-xs text-slate-400">{v.type === "admission" ? `Admission · bed ${v.bedId}` : "Outpatient"}</span></div>
            <div className="text-xs text-slate-500 mt-1">
              {v.type === "admission"
                ? "Admission stay — cleared for billing; discharge note on file."
                : "Outpatient visit — cleared for billing."}
            </div>
            <div className="mt-2 border-t border-slate-100 pt-2">
              {bill.items.map((i, idx) => <div key={idx} className="flex justify-between text-sm py-0.5"><span className="text-slate-500">{i.label}</span><span>{KSh(i.amt)}</span></div>)}
              <div className="flex justify-between text-sm font-semibold py-1 border-t border-slate-100 mt-1"><span>Total</span><span>{KSh(bill.total)}</span></div>
            </div>
            <div className="mt-2 flex items-center gap-2">
              <select className={inputCls + " !w-40"} value={method[v.id] || "Cash"} onChange={(e) => setMethod({ ...method, [v.id]: e.target.value })}>
                {methods.map((m) => <option key={m}>{m}</option>)}
              </select>
              <Btn tone="green" onClick={() => pay(v)}>Receive payment & close visit</Btn>
            </div>
          </Card>
        );
      })}
    </Screen>
  );
};

const Receipts = ({ db }) => (
  <Screen title="Payments Received" sub="All payments taken at the cash office" flow="Your audit of money in. Each payment closes a visit.">
    <Card className="p-4">
      {db.payments.length === 0 ? <Empty>No payments yet</Empty> : db.payments.map((p) => (
        <div key={p.id} className="flex items-center justify-between py-2 border-b last:border-0 border-slate-100">
          <div><div className="text-sm font-medium">{p.patient}</div><div className="text-xs text-slate-400">visit {p.visitId} · {d(p.at)} {t(p.at)}</div></div>
          <div className="flex items-center gap-2"><span className="text-sm font-medium">{KSh(p.amt)}</span><Badge tone={p.method === "M-PESA" ? "green" : "slate"}>{p.method}</Badge></div>
        </div>
      ))}
      <div className="flex justify-between pt-2 text-sm font-semibold"><span>Total collected</span><span>{KSh(db.payments.reduce((s, p) => s + p.amt, 0))}</span></div>
    </Card>
  </Screen>
);

/* ==================== ADMIN (read-only) ==================== */
const AdminOverview = ({ db }) => {
  const open = db.visits.filter((v) => v.status !== "closed");
  const counts = {};
  open.forEach((v) => { counts[v.status] = (counts[v.status] || 0) + 1; });
  const max = Math.max(1, ...Object.values(counts));
  const revenue = db.payments.reduce((s, p) => s + p.amt, 0);
  const beds = Object.entries(db.bedState);
  const occupied = beds.filter(([, o]) => o).length;
  return (
    <Screen title="Hospital Overview" sub="Read-only reporting — this role has no data-entry actions anywhere" flow="You watch the whole flow, but you cannot touch it: every screen under Administration is read-only by design.">
      <div className="grid grid-cols-4 gap-4 mb-6">
        <Stat label="Open visits" value={open.length} />
        <Stat label="Admitted" value={db.visits.filter((v) => v.status === "admitted").length} />
        <Stat label="Bed occupancy" value={`${occupied}/${beds.length}`} />
        <Stat label="Revenue collected" value={KSh(revenue)} tone="text-emerald-600" />
      </div>
      <div className="grid grid-cols-2 gap-4">
        <Card className="p-4">
          <h3 className="font-medium text-sm mb-3">Visits by stage</h3>
          {Object.keys(counts).length === 0 ? <Empty>No open visits</Empty> : Object.entries(counts).map(([k, n]) => (
            <div key={k} className="mb-2">
              <div className="flex justify-between text-xs mb-1"><span>{STAGE[k]} <span className="text-slate-400">(next: {NEXT_OWNER[k]})</span></span><span>{n}</span></div>
              <div className="h-2 bg-slate-100 rounded"><div className="h-2 bg-blue-500 rounded" style={{ width: `${(n / max) * 100}%` }} /></div>
            </div>
          ))}
        </Card>
        <Card className="p-4">
          <h3 className="font-medium text-sm mb-3">Recent payments</h3>
          {db.payments.slice(0, 5).map((p) => <div key={p.id} className="flex justify-between text-sm py-1 border-b last:border-0 border-slate-100"><span>{p.patient} · {p.method}</span><span>{KSh(p.amt)}</span></div>)}
        </Card>
      </div>
    </Screen>
  );
};

const AdminFlow = ({ db }) => {
  const open = db.visits.filter((v) => v.status !== "closed");
  const P = (id) => db.patients.find((p) => p.id === id);
  return (
    <Screen title="Visit Flow Monitor" sub="Every open visit and where it is waiting" flow="The whole hospital as one pipeline: each row is a patient, the stage they are at, and who owes the next action.">
      <Card className="p-4">
        <table className="w-full text-sm">
          <thead><tr className="text-left text-xs text-slate-400 border-b border-slate-200"><th className="py-2">Patient</th><th>Type</th><th>Stage</th><th>Waiting with</th><th>Opened</th></tr></thead>
          <tbody>
            {open.map((v) => (
              <tr key={v.id} className="border-b last:border-0 border-slate-100">
                <td className="py-2 font-medium">{P(v.patientId)?.name}</td>
                <td><Badge tone={v.type === "admission" ? "violet" : "blue"}>{v.type}</Badge></td>
                <td>{STAGE[v.status]}</td>
                <td className="text-slate-500">{NEXT_OWNER[v.status]}</td>
                <td className="text-slate-400 text-xs">{t(v.openedAt)}</td>
              </tr>
            ))}
            {open.length === 0 && <tr><td colSpan="5"><Empty>No open visits</Empty></td></tr>}
          </tbody>
        </table>
      </Card>
    </Screen>
  );
};

const AdminBeds = ({ db }) => (
  <Screen title="Bed Occupancy" sub="Live ward state" flow="Beds free when Finance receives payment on a discharged admission.">
    <div className="grid grid-cols-2 gap-4">
      {db.wards.map((w) => (
        <Card key={w.name} className="p-4">
          <h3 className="font-medium text-sm mb-2">{w.name}</h3>
          <div className="flex flex-wrap gap-2">
            {w.beds.map((b) => {
              const occ = db.bedState[b];
              const v = occ ? db.visits.find((x) => x.id === occ) : null;
              const patient = v ? db.patients.find((p) => p.id === v.patientId) : null;
              return (
                <div key={b} className={`text-xs px-2.5 py-1.5 rounded-lg border ${occ ? "bg-violet-50 border-violet-200 text-violet-700" : "bg-emerald-50 border-emerald-200 text-emerald-700"}`}>
                  {b}{patient ? ` — ${patient.name}` : " — free"}
                </div>
              );
            })}
          </div>
        </Card>
      ))}
    </div>
  </Screen>
);

/* ==================== SUPER ADMIN ==================== */
const UserMgmt = ({ db, mut }) => {
  const [f, setF] = useState({ name: "", role: "reception" });
  const addUser = () => {
    if (!f.name) return;
    mut((db) => { db.users.push({ id: nid("u"), name: f.name, role: f.role, active: true }); }, { role: "superadmin", action: `Created user ${f.name} with role ${f.role}` });
    setF({ name: "", role: "reception" });
  };
  return (
    <Screen title="Users & Roles" sub="Who can sign in, and as what" flow="Role determines everything a user can see — adding a user to a role grants exactly that role's nav, nothing more. Super Admin itself gets no clinical data.">
      <div className="grid grid-cols-3 gap-4">
        <Card className="p-4">
          <h3 className="font-medium text-sm mb-3">Add user</h3>
          <div className="space-y-2">
            <Field label="Name"><input className={inputCls} value={f.name} onChange={(e) => setF({ ...f, name: e.target.value })} /></Field>
            <Field label="Role"><select className={inputCls} value={f.role} onChange={(e) => setF({ ...f, role: e.target.value })}>{Object.entries(ROLES).map(([k, r]) => <option key={k} value={k}>{r.label}</option>)}</select></Field>
            <Btn onClick={addUser} disabled={!f.name}>Create user</Btn>
          </div>
        </Card>
        <Card className="p-4 col-span-2">
          <h3 className="font-medium text-sm mb-3">Users ({db.users.filter((u) => u.active).length} active)</h3>
          {db.users.map((u) => (
            <div key={u.id} className="flex items-center justify-between py-2 border-b last:border-0 border-slate-100">
              <div><div className="text-sm font-medium">{u.name}</div><div className="text-xs text-slate-400">{ROLES[u.role]?.tag}</div></div>
              <div className="flex items-center gap-2">
                <select className="text-xs border border-slate-300 rounded px-1.5 py-1" value={u.role} onChange={(e) => mut((db) => { db.users.find((x) => x.id === u.id).role = e.target.value; }, { role: "superadmin", action: `Changed ${u.name}'s role to ${e.target.value}` })}>
                  {Object.entries(ROLES).map(([k, r]) => <option key={k} value={k}>{r.label}</option>)}
                </select>
                <Btn tone={u.active ? "danger" : "green"} onClick={() => mut((db) => { db.users.find((x) => x.id === u.id).active = !u.active; }, { role: "superadmin", action: `${u.active ? "Deactivated" : "Reactivated"} user ${u.name}` })}>{u.active ? "Deactivate" : "Activate"}</Btn>
              </div>
            </div>
          ))}
        </Card>
      </div>
    </Screen>
  );
};

const AuditTrail = ({ db }) => (
  <Screen title="Audit Trail" sub="Every clinical and financial action, in order" flow="Super Admin sees who did what and when — but never patient clinical data itself. This is the accountability layer.">
    <Card className="p-4">
      {db.audit.map((a, i) => (
        <div key={i} className="flex items-start gap-3 py-2 border-b last:border-0 border-slate-100">
          <span className="text-xs text-slate-400 w-28 shrink-0">{d(a.ts)} {t(a.ts)}</span>
          <Badge tone="blue">{a.role}</Badge>
          <span className="text-sm flex-1">{a.action}</span>
          <span className="text-xs text-slate-400">{a.user}</span>
        </div>
      ))}
    </Card>
  </Screen>
);

const SysConfig = ({ db, mut }) => {
  const [dep, setDep] = useState("");
  return (
    <Screen title="System Configuration" sub="Fees, payment channels and departments" flow="Configuration feeds directly into the flow: fees feed Finance's auto-bill; payment toggles change what Finance can offer at the counter.">
      <div className="grid grid-cols-2 gap-4">
        <Card className="p-4">
          <h3 className="font-medium text-sm mb-3">Fees (KSh)</h3>
          <div className="grid grid-cols-2 gap-3">
            <Field label="Consultation"><input type="number" className={inputCls} value={db.config.consultFee} onChange={(e) => mut((db) => { db.config.consultFee = +e.target.value; })} /></Field>
            <Field label="Bed / night"><input type="number" className={inputCls} value={db.config.bedPerNight} onChange={(e) => mut((db) => { db.config.bedPerNight = +e.target.value; })} /></Field>
            <Field label="Lab item"><input type="number" className={inputCls} value={db.config.labFee} onChange={(e) => mut((db) => { db.config.labFee = +e.target.value; })} /></Field>
            <Field label="Imaging item"><input type="number" className={inputCls} value={db.config.radFee} onChange={(e) => mut((db) => { db.config.radFee = +e.target.value; })} /></Field>
          </div>
        </Card>
        <Card className="p-4">
          <h3 className="font-medium text-sm mb-3">Payment channels</h3>
          {["mpesaEnabled", "cashEnabled"].map((k) => (
            <label key={k} className="flex items-center gap-2 py-1.5 text-sm">
              <input type="checkbox" checked={db.config[k]} onChange={() => mut((db) => { db.config[k] = !db.config[k]; }, { role: "superadmin", action: `Toggled payment channel ${k}` })} />
              {k === "mpesaEnabled" ? "M-PESA" : "Cash"}
            </label>
          ))}
          <h3 className="font-medium text-sm mt-4 mb-2">Departments</h3>
          <div className="flex flex-wrap gap-1.5 mb-2">
            {db.config.departments.map((dp) => (
              <span key={dp} className="text-xs bg-slate-100 border border-slate-200 rounded px-2 py-1 flex items-center gap-1">
                {dp}
                <button className="text-slate-400 hover:text-red-600" onClick={() => mut((db) => { db.config.departments = db.config.departments.filter((x) => x !== dp); }, { role: "superadmin", action: `Removed department ${dp}` })}>×</button>
              </span>
            ))}
          </div>
          <div className="flex gap-1"><input className={inputCls} value={dep} onChange={(e) => setDep(e.target.value)} placeholder="New department" /><Btn onClick={() => { if (dep) { mut((db) => { db.config.departments.push(dep); }, { role: "superadmin", action: `Added department ${dep}` }); setDep(""); } }}>Add</Btn></div>
        </Card>
      </div>
    </Screen>
  );
};

/* ==================== HR ==================== */
const StaffRecords = ({ db, mut }) => {
  const [f, setF] = useState({ name: "", role: "Nurse", dept: "", phone: "" });
  const add = () => {
    if (!f.name) return;
    mut((db) => { db.staff.push({ id: nid("s"), name: f.name, role: f.role, dept: f.dept, phone: f.phone, since: "2026" }); }, { role: "hr", action: `Added staff record ${f.name}` });
    setF({ name: "", role: "Nurse", dept: "", phone: "" });
  };
  return (
    <Screen title="Staff Records" sub="Employees of the hospital — a fully separate module" flow="HR never touches patient data: this module has no patient screens, no clinical fields, and its own navigation.">
      <div className="grid grid-cols-3 gap-4">
        <Card className="p-4">
          <h3 className="font-medium text-sm mb-3">Add staff</h3>
          <div className="space-y-2">
            <Field label="Name"><input className={inputCls} value={f.name} onChange={(e) => setF({ ...f, name: e.target.value })} /></Field>
            <Field label="Role"><input className={inputCls} value={f.role} onChange={(e) => setF({ ...f, role: e.target.value })} /></Field>
            <Field label="Department"><input className={inputCls} value={f.dept} onChange={(e) => setF({ ...f, dept: e.target.value })} /></Field>
            <Field label="Phone"><input className={inputCls} value={f.phone} onChange={(e) => setF({ ...f, phone: e.target.value })} /></Field>
            <Btn onClick={add} disabled={!f.name}>Add</Btn>
          </div>
        </Card>
        <Card className="p-4 col-span-2">
          <h3 className="font-medium text-sm mb-3">All staff ({db.staff.length})</h3>
          <table className="w-full text-sm">
            <thead><tr className="text-left text-xs text-slate-400 border-b border-slate-200"><th className="py-2">Name</th><th>Role</th><th>Department</th><th>Phone</th><th>Since</th></tr></thead>
            <tbody>
              {db.staff.map((s) => (
                <tr key={s.id} className="border-b last:border-0 border-slate-100">
                  <td className="py-1.5 font-medium">{s.name}</td><td>{s.role}</td><td>{s.dept}</td><td className="text-slate-400">{s.phone}</td><td className="text-slate-400">{s.since}</td>
                </tr>
              ))}
            </tbody>
          </table>
        </Card>
      </div>
    </Screen>
  );
};

const LeaveMgmt = ({ db, mut }) => {
  const [f, setF] = useState({ staffId: "s2", type: "Annual", from: "", to: "" });
  const S = (id) => db.staff.find((s) => s.id === id)?.name;
  const set = (l, status) => mut((db) => { db.leave.find((x) => x.id === l.id).status = status; }, { role: "hr", action: `${status} leave for ${S(l.staffId)} (${l.type})` });
  const add = () => {
    if (!f.from || !f.to) return;
    mut((db) => { db.leave.push({ id: nid("l"), staffId: f.staffId, type: f.type, from: f.from, to: f.to, status: "pending" }); }, { role: "hr", action: `Logged leave request for ${S(f.staffId)}` });
    setF({ ...f, from: "", to: "" });
  };
  return (
    <Screen title="Leave Requests" flow="Approve or reject leave; approved leave feeds the roster so shifts are covered.">
      <div className="grid grid-cols-3 gap-4">
        <Card className="p-4">
          <h3 className="font-medium text-sm mb-3">New request</h3>
          <div className="space-y-2">
            <Field label="Staff"><select className={inputCls} value={f.staffId} onChange={(e) => setF({ ...f, staffId: e.target.value })}>{db.staff.map((s) => <option key={s.id} value={s.id}>{s.name}</option>)}</select></Field>
            <Field label="Type"><select className={inputCls} value={f.type} onChange={(e) => setF({ ...f, type: e.target.value })}><option>Annual</option><option>Sick</option><option>Maternity</option><option>Study</option></select></Field>
            <Field label="From"><input type="date" className={inputCls} value={f.from} onChange={(e) => setF({ ...f, from: e.target.value })} /></Field>
            <Field label="To"><input type="date" className={inputCls} value={f.to} onChange={(e) => setF({ ...f, to: e.target.value })} /></Field>
            <Btn onClick={add}>Submit request</Btn>
          </div>
        </Card>
        <Card className="p-4 col-span-2">
          <h3 className="font-medium text-sm mb-3">Requests</h3>
          {db.leave.map((l) => (
            <div key={l.id} className="flex items-center justify-between py-2 border-b last:border-0 border-slate-100">
              <div><div className="text-sm font-medium">{S(l.staffId)} <span className="text-xs text-slate-400">{l.type} · {l.from} → {l.to}</span></div></div>
              <div className="flex items-center gap-2">
                {l.status === "pending" ? (<><Btn tone="green" onClick={() => set(l, "approved")}>Approve</Btn><Btn tone="danger" onClick={() => set(l, "rejected")}>Reject</Btn></>) : <Badge tone={l.status === "approved" ? "green" : "red"}>{l.status}</Badge>}
              </div>
            </div>
          ))}
        </Card>
      </div>
    </Screen>
  );
};

const Roster = ({ db, mut }) => {
  const days = Object.keys(db.roster);
  const names = [...new Set(db.staff.filter((s) => s.role === "Nurse").map((s) => s.name))];
  return (
    <Screen title="Rostering" sub="Weekly nurse shifts — edit inline" flow="The roster is who is actually on the floor; it determines who triage and charting work falls to each shift.">
      <Card className="p-4">
        <table className="w-full text-sm">
          <thead><tr className="text-left text-xs text-slate-400 border-b border-slate-200"><th className="py-2">Day</th><th>Morning</th><th>Evening</th></tr></thead>
          <tbody>
            {days.map((dy) => (
              <tr key={dy} className="border-b last:border-0 border-slate-100">
                <td className="py-2 font-medium">{dy}</td>
                {["Morning", "Evening"].map((shift) => (
                  <td key={shift}>
                    <select className="text-sm border border-slate-300 rounded px-2 py-1" value={db.roster[dy][shift]} onChange={(e) => mut((db) => { db.roster[dy][shift] = e.target.value; }, { role: "hr", action: `Roster: ${dy} ${shift} → ${e.target.value}` })}>
                      {names.map((n) => <option key={n}>{n}</option>)}
                    </select>
                  </td>
                ))}
              </tr>
            ))}
          </tbody>
        </table>
      </Card>
    </Screen>
  );
};