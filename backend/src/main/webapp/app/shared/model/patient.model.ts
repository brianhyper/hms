import dayjs from 'dayjs';

import { IdentityDocumentType } from 'app/shared/model/enumerations/identity-document-type.model';
import { NextOfKinRelationship } from 'app/shared/model/enumerations/next-of-kin-relationship.model';
import { RegistrationStatus } from 'app/shared/model/enumerations/registration-status.model';
import { Sex } from 'app/shared/model/enumerations/sex.model';

export interface IPatient {
  id?: number;
  hospitalId?: string;
  fullName?: string;
  dateOfBirth?: dayjs.Dayjs | null;
  estimatedAge?: number | null;
  sex?: keyof typeof Sex;
  sexEstimated?: boolean;
  phone?: string | null;
  email?: string | null;
  identityDocumentType?: keyof typeof IdentityDocumentType | null;
  identityDocumentNumber?: string | null;
  occupation?: string | null;
  maritalStatus?: string | null;
  nextOfKinName?: string | null;
  nextOfKinPhone?: string | null;
  nextOfKinRelationship?: keyof typeof NextOfKinRelationship | null;
  knownAllergies?: string | null;
  knownConditions?: string | null;
  villageEstate?: string | null;
  registrationStatus?: keyof typeof RegistrationStatus;
  mergedIntoPatient?: IPatient | null;
}

export const defaultValue: Readonly<IPatient> = {
  sexEstimated: false,
};
