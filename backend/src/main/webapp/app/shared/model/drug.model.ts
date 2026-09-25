import { DrugClassification } from 'app/shared/model/enumerations/drug-classification.model';

export interface IDrug {
  id?: number;
  name?: string;
  unit?: string;
  currentStock?: number;
  reservedStock?: number;
  lowStockThreshold?: number;
  price?: number;
  classification?: keyof typeof DrugClassification | null;
  active?: boolean;
}

export const defaultValue: Readonly<IDrug> = {
  active: false,
};
