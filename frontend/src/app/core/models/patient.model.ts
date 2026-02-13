export interface Patient {
  id: number;
  referenceId: string;
  firstName: string;
  lastName: string;
  dateOfBirth: string;
  gender?: string;
  phone?: string;
  email?: string;
  addressLine1?: string;
  addressLine2?: string;
  city?: string;
  state?: string;
  zipCode?: string;
  createdById: number;
  createdByEmail: string;
  createdAt: string;
  updatedAt: string;
}

export interface PatientRequest {
  firstName: string;
  lastName: string;
  dateOfBirth: string;
  gender?: string;
  phone?: string;
  email?: string;
  addressLine1?: string;
  addressLine2?: string;
  city?: string;
  state?: string;
  zipCode?: string;
}

export interface Program {
  id: number;
  name: string;
  description: string;
  active: boolean;
  services?: SupportService[];
}

export interface SupportService {
  id: number;
  programId: number;
  name: string;
  description?: string;
  serviceType: string;
  active: boolean;
}

export interface Enrollment {
  id: number;
  patientId: number;
  patientName: string;
  program: Program;
  prescriber?: {
    id: number;
    npi: string;
    name: string;
    specialty: string;
  };
  status: string;
  diagnosisCode?: string;
  diagnosisDescription?: string;
  medicationName?: string;
  notes?: string;
  createdById: number;
  createdByEmail: string;
  submittedAt?: string;
  createdAt: string;
  updatedAt: string;
}

export interface EnrollmentRequest {
  programId: number;
  prescriberId?: number;
  diagnosisCode?: string;
  diagnosisDescription?: string;
  medicationName?: string;
  notes?: string;
  submit: boolean;
  consents?: ConsentRequest[];
}

export interface ConsentRequest {
  consentType: string;
  granted: boolean;
  notes?: string;
}

export interface PageResponse<T> {
  content: T[];
  totalElements: number;
  totalPages: number;
  size: number;
  number: number;
}
