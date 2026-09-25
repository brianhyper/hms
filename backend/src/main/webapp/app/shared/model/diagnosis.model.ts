export interface IDiagnosis {
  id?: number;
  code?: string;
  name?: string;
  active?: boolean;
}

export const defaultValue: Readonly<IDiagnosis> = {
  active: false,
};
