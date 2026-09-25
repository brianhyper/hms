import dayjs from 'dayjs';

import { IUser } from 'app/shared/model/user.model';

export interface IAuditLog {
  id?: number;
  action?: string;
  entityName?: string;
  entityId?: string;
  reason?: string | null;
  oldValue?: string | null;
  newValue?: string | null;
  details?: string | null;
  performedAt?: dayjs.Dayjs;
  actor?: IUser | null;
}

export const defaultValue: Readonly<IAuditLog> = {};
