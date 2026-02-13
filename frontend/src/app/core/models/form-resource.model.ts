export interface FormResource {
  id: number;
  title: string;
  description: string;
  programId: number;
  programName: string;
  category: string;

  // File information
  fileName: string;
  fileSize: number;
  mimeType: string;

  // Version control
  version: number;
  parentId?: number;

  // Compliance
  complianceApproved: boolean;

  // Audit fields
  uploadedById: number;
  uploadedByEmail: string;
  uploadedAt: string;
  updatedAt: string;

  // Statistics
  downloadCount: number;
}

export interface FormResourceRequest {
  title: string;
  description?: string;
  programId?: number;
  category?: string;
  complianceApproved?: boolean;
}

export interface FormResourceSearchParams {
  programId?: number;
  category?: string;
  searchTerm?: string;
  page?: number;
  size?: number;
}
