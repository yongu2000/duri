import axios, { AxiosError, AxiosHeaders } from 'axios';
import { useAuth } from '@/hooks/useAuth';

const API_URL = process.env.NEXT_PUBLIC_API_URL || 'http://localhost:8080';

// 토큰 재발급 상태 관리
let isRefreshing = false;
let refreshSubscribers: ((token: string) => void)[] = [];
let refreshPromise: Promise<string> | null = null;

// 토큰 재발급 완료 후 대기 중인 요청 실행
const onRefreshed = (token: string) => {
  refreshSubscribers.forEach(callback => callback(token));
  refreshSubscribers = [];
  isRefreshing = false;
  refreshPromise = null;
};

// 토큰 재발급 중인 요청을 큐에 추가
const addRefreshSubscriber = (callback: (token: string) => void) => {
  refreshSubscribers.push(callback);
};

export const axiosInstance = axios.create({
  baseURL: API_URL,
  withCredentials: true,
  headers: {
    'Content-Type': 'application/json',
  },
});

// 인증 관련 처리를 위한 함수
export const handleAuthError = () => {
  localStorage.removeItem('accessToken');
  const authStore = useAuth.getState();
  authStore.clearAuth();
  
  // 로그인 페이지가 아닐 때만 새로고침
  if (typeof window !== 'undefined' && window.location.pathname !== '/login') {
    window.location.href = '/login';
  }
};

// 토큰 재발급을 위한 함수
const reissueAccessToken = async () => {
  try {
    const response = await axiosInstance.post(
      `/token/reissue`,
      {},
    );
    
    // 응답 헤더에서 Authorization 토큰 추출
    const authHeader = response.headers['authorization'];
    if (!authHeader) {
      throw new Error('토큰 재발급 응답에 Authorization 헤더가 없습니다.');
    }
    
    // "Bearer " 접두사 제거
    const accessToken = authHeader.replace('Bearer ', '');
    
    // 새 토큰을 localStorage에 저장
    localStorage.setItem('accessToken', accessToken);

    // axiosInstance의 기본 헤더에 새 토큰 설정
    axiosInstance.defaults.headers.common['Authorization'] = `Bearer ${accessToken}`;
    
    return accessToken;
  } catch (error) {
    console.error('토큰 재발급 실패:', error);
    throw error;
  }
};

// 요청 인터셉터 추가
axiosInstance.interceptors.request.use(
  (config) => {
    // localStorage에서 accessToken 가져오기
    const accessToken = localStorage.getItem('accessToken');
    
    // accessToken이 있으면 Authorization 헤더에 추가
    if (accessToken) {
      const headers = new AxiosHeaders(config.headers);
      headers.set('Authorization', `Bearer ${accessToken}`);
      config.headers = headers;
    }
    
    return config;
  },
  (error) => {
    return Promise.reject(error);
  }
);

// 에러 처리 인터셉터
axiosInstance.interceptors.response.use(
  (response) => response,
  async (error: AxiosError) => {
    const originalRequest = error.config!;

    // 1. 토큰 재발급이 필요한 경우 (401 + 재발급 헤더)
    if (
      error.response?.status === 401 && 
      error.response.headers['x-reissue-token'] === 'true' &&
      !(originalRequest as any)._retry
    ) {
      if (isRefreshing) {
        // 토큰 재발급 중이면 Promise를 반환하여 대기
        return new Promise((resolve) => {
          addRefreshSubscriber((token: string) => {
            const headers = new AxiosHeaders(originalRequest.headers);
            headers.set('Authorization', `Bearer ${token}`);
            originalRequest.headers = headers;
            resolve(axiosInstance(originalRequest));
          });
        });
      }

      (originalRequest as any)._retry = true;
      isRefreshing = true;

      try {
        // 이미 진행 중인 재발급 Promise가 있다면 그것을 사용
        if (!refreshPromise) {
          refreshPromise = reissueAccessToken();
        }
        
        const newAccessToken = await refreshPromise;
        onRefreshed(newAccessToken);
        
        // 원래 요청의 헤더에 새 토큰 설정
        const headers = new AxiosHeaders(originalRequest.headers);
        headers.set('Authorization', `Bearer ${newAccessToken}`);
        originalRequest.headers = headers;
        
        return axiosInstance(originalRequest);
      } catch (reissueError) {
        isRefreshing = false;
        refreshPromise = null;
        // 토큰 재발급 실패 시에만 로그인 페이지로 리다이렉트
        handleAuthError();
        return Promise.reject(reissueError);
      }
    }

    // 2. 인증 관련 에러 처리 (401, 403)
    if (error.response?.status === 401 || error.response?.status === 403) {
      // 토큰 재발급 헤더가 없는 경우에만 로그인 페이지로 리다이렉트
      if (!error.response.headers['x-reissue-token']) {
        // 로그인 페이지가 아닐 때만 현재 URL 저장
        if (typeof window !== 'undefined' && window.location.pathname !== '/login') {
          localStorage.setItem('redirectAfterLogin', window.location.pathname);
        }
        handleAuthError();
      }
      return Promise.reject(error);
    }

    // 3. 404 에러는 단순히 에러 메시지만 표시
    if (error.response?.status === 404) {
      return Promise.reject(new Error('요청하신 페이지를 찾을 수 없습니다.'));
    }

    // 4. 그 외 모든 에러는 컴포넌트에서 처리
    return Promise.reject(error);
  }
);

export default axiosInstance;