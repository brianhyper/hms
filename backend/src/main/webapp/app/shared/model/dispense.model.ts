import dayjs from 'dayjs';

import { IPrescription } from 'app/shared/model/prescription.model';
import { IUser } from 'app/shared/model/user.model';

export interface IDispense {
  id?: number;
  dispensedAt?: dayjs.Dayjs;
  note?: string | null;
  prescription?: IPrescription;
  recordedBy?: IUser;
}

export const defaultValue: Readonly<IDispense> = {};
