export interface Provider {
  id: number;
  npi: string;
  name: string;
  specialty: string;
  addressLine1: string;
  addressLine2?: string;
  city: string;
  state: string;
  zipCode: string;
  phone: string;
  fax?: string;
  email: string;
  active: boolean;
}

export interface ProviderAffiliation {
  id: number;
  userId: number;
  userEmail: string;
  userName: string;
  provider: Provider;
  status: 'PENDING' | 'APPROVED' | 'REJECTED';
  requestedAt: string;
  verifiedAt?: string;
  verifiedByEmail?: string;
  verificationReason?: string;
}

export interface ProviderAffiliationRequest {
  providerId: number;
  notes?: string;
}

export interface VerifyAffiliationRequest {
  approved: boolean;
  reason: string;
}

export interface PageResponse<T> {
  content: T[];
  totalElements: number;
  totalPages: number;
  size: number;
  number: number;
}
