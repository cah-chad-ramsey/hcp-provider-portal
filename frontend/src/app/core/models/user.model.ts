export interface User {
  id: number;
  email: string;
  firstName: string;
  lastName: string;
  roles: string[];
  createdAt: string;
}

export interface LoginRequest {
  email: string;
  password: string;
}

export interface LoginResponse {
  token: string;
  email: string;
  firstName: string;
  lastName: string;
  roles: string[];
}

export interface RegisterRequest {
  email: string;
  password: string;
  firstName: string;
  lastName: string;
}
