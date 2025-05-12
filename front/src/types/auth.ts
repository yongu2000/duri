export interface PublicUserInfo {
  username: string;
  name: string;
  createdAt: string;
  bio?: string;
  profileImageUrl?: string;
}

export interface UserInfo extends PublicUserInfo {
  id: number;
  email: string;
  coupleCode?: string;
  gender?: 'MALE' | 'FEMALE';
  birthday?: string;
}

export interface LoginRequest {
  username: string;
  password: string;
  rememberMe : boolean;
}

export interface RegisterRequest {
  email: string;
  password: string;
}

export interface AuthResponse {
  user: UserInfo;
  // accessToken은 응답 헤더에서 받아옴
}

export interface UserUpdateRequest {
  name?: string;
  bio?: string;
  email?: string;
  username?: string;
  currentPassword?: string;
  newPassword?: string;
  profileImageUrl?: string;
}