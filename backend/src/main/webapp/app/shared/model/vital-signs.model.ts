export interface IVitalSigns {
  id?: number;
  temperature?: number | null;
  pulseRate?: number | null;
  systolicBp?: number | null;
  diastolicBp?: number | null;
  oxygenSaturation?: number | null;
  weight?: number | null;
  height?: number | null;
  bmi?: number | null;
  nutritionalStatus?: string | null;
  pregnancyScreening?: boolean | null;
  triageNotes?: string | null;
  otherMeasurements?: string | null;
}

export const defaultValue: Readonly<IVitalSigns> = {
  pregnancyScreening: false,
};
