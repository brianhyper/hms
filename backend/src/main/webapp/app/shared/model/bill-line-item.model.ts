import { IBill } from 'app/shared/model/bill.model';
import { BillLineSourceType } from 'app/shared/model/enumerations/bill-line-source-type.model';

export interface IBillLineItem {
  id?: number;
  description?: string;
  amount?: number;
  sourceType?: keyof typeof BillLineSourceType;
  bill?: IBill;
}

export const defaultValue: Readonly<IBillLineItem> = {};
