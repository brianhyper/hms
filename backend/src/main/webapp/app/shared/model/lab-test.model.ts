export interface ILabTest {
  id?: number;
  name?: string;
  price?: number;
  specimenType?: string | null;
  turnaroundTimeMinutes?: number | null;
  active?: boolean;
}

export const defaultValue: Readonly<ILabTest> = {
  active: false,
};
