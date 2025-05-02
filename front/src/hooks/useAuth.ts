import { create } from 'zustand';
import { persist } from 'zustand/middleware';
import { UserInfo } from '@/types/auth';
import { authService } from '@/services/auth';

interface AuthStore {
  user: UserInfo | null;
  isAuthenticated: boolean;
  isLoading: boolean;
  setAuth: (user: UserInfo) => void;
  clearAuth: () => void;
  initializeAuth: () => Promise<void>;
}

export const useAuth = create(
  persist<AuthStore>(
    (set) => ({
      user: null,
      isAuthenticated: false,
      isLoading: true,
      setAuth: (user) => set({ user, isAuthenticated: true, isLoading: false }),
      clearAuth: () => {
        localStorage.removeItem('accessToken'); // localStorage에서 토큰 삭제
        localStorage.removeItem('auth-storage');
        set({ user: null, isAuthenticated: false, isLoading: false });
      },
      initializeAuth: async () => {
        try {
          // 이미 인증 정보가 있다면 스킵
          const currentState = useAuth.getState();
          if (currentState.user) {
            set({ isLoading: false });
            return;
          }

          // 토큰이 있는지 확인
          const accessToken = localStorage.getItem('accessToken');
          if (!accessToken) {
            set({ isLoading: false });
            return;
          }

          // 사용자 정보 요청
          const userInfo = await authService.getUserInfo();
          set({ user: userInfo, isAuthenticated: true, isLoading: false });
        } catch (error) {
          console.error('인증 초기화 실패:', error);
          set({ user: null, isAuthenticated: false, isLoading: false });
        }
      },
    }),
    {
      name: 'auth-storage', // localStorage에 저장될 키 이름
    }
  )
);