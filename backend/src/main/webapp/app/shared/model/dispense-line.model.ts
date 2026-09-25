import { IDispense } from 'app/shared/model/dispense.model';
import { IDrug } from 'app/shared/model/drug.model';
import { IPrescriptionLine } from 'app/shared/model/prescription-line.model';

export interface IDispenseLine {
  id?: number;
  quantity?: number;
  substitutionReason?: string | null;
  dispense?: IDispense;
  prescriptionLine?: IPrescriptionLine;
  drug?: IDrug;
}

export const defaultValue: Readonly<IDispenseLine> = {};
