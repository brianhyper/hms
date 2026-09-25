import dayjs from 'dayjs';

import { BillStatus } from 'app/shared/model/enumerations/bill-status.model';
import { IPayment } from 'app/shared/model/payment.model';

export interface IBill {
  id?: number;
  totalAmount?: number;
  status?: keyof typeof BillStatus;
  paidAt?: dayjs.Dayjs | null;
  payment?: IPayment | null;
}

export const defaultValue: Readonly<IBill> = {};
