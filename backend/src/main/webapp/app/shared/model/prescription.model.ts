import { PrescriptionSource } from 'app/shared/model/enumerations/prescription-source.model';
import { PrescriptionStatus } from 'app/shared/model/enumerations/prescription-status.model';
import { IUser } from 'app/shared/model/user.model';
import { IVisit } from 'app/shared/model/visit.model';

export interface IPrescription {
  id?: number;
  source?: keyof typeof PrescriptionSource;
  prescribingSource?: string | null;
  status?: keyof typeof PrescriptionStatus;
  visit?: IVisit | null;
  doctor?: IUser | null;
}

export const defaultValue: Readonly<IPrescription> = {};
