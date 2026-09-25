export interface IRadiologyExam {
  id?: number;
  name?: string;
  price?: number;
  active?: boolean;
}

export const defaultValue: Readonly<IRadiologyExam> = {
  active: false,
};
