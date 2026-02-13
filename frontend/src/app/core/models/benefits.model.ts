export interface BenefitsInvestigationRequest {
  programId: number;
  investigationType: 'MEDICAL' | 'PHARMACY';
  payerName: string;
  payerPlanId?: string;
  memberId: string;
  patientState?: string;
  patientDob?: string;
  medicationName?: string;
}

export interface BenefitsInvestigation {
  id: number;
  patientId: number;
  patientReferenceId: string;
  patientName: string;
  programId: number;
  programName: string;
  investigationType: 'MEDICAL' | 'PHARMACY';

  // Input fields
  payerName: string;
  payerPlanId?: string;
  memberId: string;
  patientState?: string;
  medicationName?: string;

  // Result fields
  coverageStatus: string;
  coverageType: string;
  priorAuthRequired: boolean;
  deductibleApplies: boolean;
  specialtyPharmacyRequired: boolean;
  copayAmount?: number;
  coinsurancePercentage?: number;
  notes: string;
  resultPayload?: Record<string, any>;

  expiresAt: string;
  createdById: number;
  createdByEmail: string;
  createdAt: string;
  isExpired: boolean;
}
