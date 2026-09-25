import dayjs from 'dayjs';

import { IBill } from 'app/shared/model/bill.model';
import { IConsultation } from 'app/shared/model/consultation.model';
import { VisitPriority } from 'app/shared/model/enumerations/visit-priority.model';
import { VisitStatus } from 'app/shared/model/enumerations/visit-status.model';
import { VisitType } from 'app/shared/model/enumerations/visit-type.model';
import { IPatient } from 'app/shared/model/patient.model';
import { IVitalSigns } from 'app/shared/model/vital-signs.model';

export interface IVisit {
  id?: number;
  type?: keyof typeof VisitType;
  priority?: keyof typeof VisitPriority;
  reasonForVisit?: string;
  status?: keyof typeof VisitStatus;
  queueSkipReason?: string | null;
  createdAt?: dayjs.Dayjs;
  startedVitalsAt?: dayjs.Dayjs | null;
  startedConsultationAt?: dayjs.Dayjs | null;
  closedAt?: dayjs.Dayjs | null;
  vitals?: IVitalSigns | null;
  consultation?: IConsultation | null;
  bill?: IBill | null;
  patient?: IPatient;
}

export const defaultValue: Readonly<IVisit> = {};
