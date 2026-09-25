import dayjs from 'dayjs';

import { PaymentConfirmationStatus } from 'app/shared/model/enumerations/payment-confirmation-status.model';
import { PaymentMethod } from 'app/shared/model/enumerations/payment-method.model';
import { IUser } from 'app/shared/model/user.model';

export interface IPayment {
  id?: number;
  method?: keyof typeof PaymentMethod;
  mpesaReference?: string | null;
  insurerName?: string | null;
  confirmationStatus?: keyof typeof PaymentConfirmationStatus;
  receiptNumber?: string;
  amount?: number;
  recordedAt?: dayjs.Dayjs;
  recordedBy?: IUser;
}

export const defaultValue: Readonly<IPayment> = {};
