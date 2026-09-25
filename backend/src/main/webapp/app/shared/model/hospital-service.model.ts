export interface IHospitalService {
  id?: number;
  name?: string;
  serviceType?: string | null;
  price?: number;
  active?: boolean;
}

export const defaultValue: Readonly<IHospitalService> = {
  active: false,
};
