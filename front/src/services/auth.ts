import { axiosInstance } from './axios';
import { LoginRequest, UserUpdateRequest, UserInfo, PublicUserInfo } from '@/types/auth';
import { useAuth } from '@/hooks/useAuth';

const API_URL = process.env.NEXT_PUBLIC_API_URL || 'http://localhost:8080';

export interface JoinRequest {
  email: string;
  username: string;
  password: string;
  name: string;
  gender: 'MALE' | 'FEMALE';
  birthday: string;
}

export const authService = {
  async login(data: LoginRequest): Promise<string> {
    const response = await axiosInstance.post('/login', {
      emailOrUsername: data.username,
      password: data.password,
      rememberMe: data.rememberMe || false
    });
    
    const authHeader = response.headers['authorization'];
    if (!authHeader || !authHeader.startsWith('Bearer ')) {
      throw new Error('Access Token이 없습니다.');
    }
    
    const accessToken = authHeader.split('Bearer ')[1];
    localStorage.setItem('accessToken', accessToken);
    
    return accessToken;
  },

  async getUserInfo(): Promise<UserInfo> {
    const response = await axiosInstance.get('/user/my');
    return response.data;
  },

  async updateUser(username: string, data: UserUpdateRequest): Promise<void> {
    const response = await axiosInstance.put(`/user/${username}`, {
      name: data.name,
      bio: data.bio,
      email: data.email,
      username: data.username,
      currentPassword: data.currentPassword,
      newPassword: data.newPassword,
      profileImageUrl: data.profileImageUrl,
    });
    
    // 응답으로 받은 유저 정보로 auth store 업데이트
    const authStore = useAuth.getState();
    if (authStore.user) {
      authStore.setAuth({
        ...authStore.user,
        ...response.data  // 서버에서 받은 최신 유저 정보로 업데이트
      });
    }
  },

  async getCurrentUser(): Promise<UserInfo> {
    const response = await axiosInstance.get('/user/my');
    return response.data;
  },

  async logout(): Promise<void> {
    try {
      await axiosInstance.post('/logout');
      // 로그아웃 후 인증 상태 초기화 및 리다이렉트
      const authStore = useAuth.getState();
      authStore.clearAuth();
      window.location.href = '/login';
    } catch (error) {
      console.error('로그아웃 실패:', error);
      throw error;
    }
  },

  async join(data: JoinRequest): Promise<void> {
    await axiosInstance.post('/join', data);
  },

  async refresh(): Promise<string> {
    const response = await axiosInstance.post('/auth/refresh');
    return response.data;
  },

  async reissueToken(): Promise<string> {
    const response = await axiosInstance.post('/token/reissue');
    
    const authHeader = response.headers['authorization'];
    if (!authHeader || !authHeader.startsWith('Bearer ')) {
      throw new Error('Access Token이 없습니다.');
    }
    
    const accessToken = authHeader.split('Bearer ')[1];
    localStorage.setItem('accessToken', accessToken);
    
    return accessToken;
  },

  async getUserByUsername(username: string): Promise<PublicUserInfo> {
    const response = await axiosInstance.get(`/user/${username}`);
    return response.data;
  },

  async checkUsernameDuplicate(username: string): Promise<boolean> {
    const response = await axiosInstance.get(`/user/check/username/${username}`);
    return response.data.isDuplicate;
  },

  async checkEmailDuplicate(email: string): Promise<boolean> {
    const response = await axiosInstance.get(`/user/check/email/${email}`);
    return response.data.isDuplicate;
  },

  deleteUser: async (username: string) => {
    const response = await axiosInstance.delete(`/user/${username}`);
    return response.data;
  },

  resetPassword: async (token: string, newPassword: string) => {
    const response = await fetch(`${API_URL}/user/password/reset`, {
      method: 'POST',
      headers: {
        'Content-Type': 'application/json',
      },
      body: JSON.stringify({ token, newPassword }),
    });

    if (!response.ok) {
      throw new Error('비밀번호 재설정에 실패했습니다.');
    }

    return;
  },

  async editUserProfile(username: string, data: { 
    profileImageUrl: string; 
    name: string;
    gender: 'MALE' | 'FEMALE';
    birthday: string | null;
  }): Promise<void> {
    await axiosInstance.put(`/user/profile/${username}/edit`, data);
  },
};

// API 요청 시 인터셉터를 통해 Access Token 자동 첨부 (Bearer 포함)
axiosInstance.interceptors.request.use(
  (config) => {
    const token = localStorage.getItem('accessToken');
    if (token) {
      config.headers['Authorization'] = `Bearer ${token}`;
    }
    return config;
  },
  (error) => {
    return Promise.reject(error);
  }
);

export default authService;