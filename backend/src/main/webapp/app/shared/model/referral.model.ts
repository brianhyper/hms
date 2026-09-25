import dayjs from 'dayjs';

import { IDepartment } from 'app/shared/model/department.model';
import { ReferralStatus } from 'app/shared/model/enumerations/referral-status.model';
import { ReferralType } from 'app/shared/model/enumerations/referral-type.model';
import { IUser } from 'app/shared/model/user.model';
import { IVisit } from 'app/shared/model/visit.model';

export interface IReferral {
  id?: number;
  type?: keyof typeof ReferralType;
  destination?: string;
  destinationEmail?: string | null;
  reason?: string;
  notes?: string | null;
  status?: keyof typeof ReferralStatus;
  createdAt?: dayjs.Dayjs;
  visit?: IVisit;
  referredBy?: IUser;
  department?: IDepartment | null;
}

export const defaultValue: Readonly<IReferral> = {};
