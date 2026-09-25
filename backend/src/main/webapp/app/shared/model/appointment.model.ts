import dayjs from 'dayjs';

import { IDepartment } from 'app/shared/model/department.model';
import { AppointmentStatus } from 'app/shared/model/enumerations/appointment-status.model';
import { IPatient } from 'app/shared/model/patient.model';
import { IUser } from 'app/shared/model/user.model';
import { IVisit } from 'app/shared/model/visit.model';

export interface IAppointment {
  id?: number;
  scheduledDate?: dayjs.Dayjs;
  scheduledTime?: string;
  reason?: string | null;
  status?: keyof typeof AppointmentStatus;
  visit?: IVisit | null;
  patient?: IPatient;
  department?: IDepartment;
  doctor?: IUser | null;
}

export const defaultValue: Readonly<IAppointment> = {};
