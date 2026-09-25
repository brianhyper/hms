export interface IDepartment {
  id?: number;
  name?: string;
  code?: string;
  active?: boolean;
}

export const defaultValue: Readonly<IDepartment> = {
  active: false,
};
