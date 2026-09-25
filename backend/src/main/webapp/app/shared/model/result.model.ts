import dayjs from 'dayjs';

import { IUser } from 'app/shared/model/user.model';

export interface IResult {
  id?: number;
  resultValue?: string;
  notes?: string | null;
  enteredAt?: dayjs.Dayjs;
  imageReference?: string | null;
  enteredBy?: IUser;
}

export const defaultValue: Readonly<IResult> = {};
