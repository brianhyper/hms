import dayjs from 'dayjs';

import { OrderStatus } from 'app/shared/model/enumerations/order-status.model';
import { OrderType } from 'app/shared/model/enumerations/order-type.model';
import { ILabTest } from 'app/shared/model/lab-test.model';
import { IRadiologyExam } from 'app/shared/model/radiology-exam.model';
import { IResult } from 'app/shared/model/result.model';
import { IUser } from 'app/shared/model/user.model';
import { IVisit } from 'app/shared/model/visit.model';

export interface IDiagnosticOrder {
  id?: number;
  type?: keyof typeof OrderType;
  testName?: string;
  status?: keyof typeof OrderStatus;
  notes?: string | null;
  orderedAt?: dayjs.Dayjs;
  result?: IResult | null;
  visit?: IVisit;
  orderedBy?: IUser;
  labTest?: ILabTest | null;
  radiologyExam?: IRadiologyExam | null;
}

export const defaultValue: Readonly<IDiagnosticOrder> = {};
