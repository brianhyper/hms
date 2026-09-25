import { IDrug } from 'app/shared/model/drug.model';
import { IPrescription } from 'app/shared/model/prescription.model';

export interface IPrescriptionLine {
  id?: number;
  dosage?: string;
  duration?: string;
  quantity?: number;
  prescription?: IPrescription;
  drug?: IDrug;
}

export const defaultValue: Readonly<IPrescriptionLine> = {};
