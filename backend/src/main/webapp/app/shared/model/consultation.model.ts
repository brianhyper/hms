import dayjs from 'dayjs';

import { IDiagnosis } from 'app/shared/model/diagnosis.model';
import { ConsultationStatus } from 'app/shared/model/enumerations/consultation-status.model';
import { IUser } from 'app/shared/model/user.model';

export interface IConsultation {
  id?: number;
  presentingComplaint?: string | null;
  examinationFindings?: string | null;
  diagnosisOther?: string | null;
  observations?: string | null;
  followUpInstructions?: string | null;
  status?: keyof typeof ConsultationStatus;
  startedAt?: dayjs.Dayjs;
  completedAt?: dayjs.Dayjs | null;
  doctor?: IUser;
  diagnoseses?: IDiagnosis[] | null;
}

export const defaultValue: Readonly<IConsultation> = {};
