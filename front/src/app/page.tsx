'use client';

import { useEffect } from 'react';
import { useRouter } from 'next/navigation';
import { useAuth } from '@/hooks/useAuth';
import { authService } from '@/services/auth';
import Link from 'next/link';
import { getCookie } from 'cookies-next';
import NavBar from '@/components/NavBar';
import SubBar from '@/components/SubBar';
import SearchBar from '@/components/SearchBar';
import PostList from '@/components/PostList';

export default function HomePage() {
  const { user, setAuth } = useAuth();
  const router = useRouter();

  useEffect(() => {
    const initializeAuth = async () => {
      try {
        // 이미 인증 정보가 있다면 스킵
        if (user) return;

        // refreshToken 쿠키가 있는지 확인
        const refreshToken = getCookie('REFRESH_TOKEN');
        if (!refreshToken) return;

        // 사용자 정보 요청 (토큰 재발급은 axiosInstance에서 자동으로 처리)
        const userInfo = await authService.getUserInfo();
        
        // 전역 상태에 사용자 정보 저장
        setAuth(userInfo);

        // 커플 ID 체크
        if (!userInfo.coupleId) {
          router.push('/couple/link');
        }
      } catch (error) {
        console.error('인증 초기화 실패:', error);
        // 에러 발생 시 조용히 실패 (이미 로그인 페이지로의 리다이렉트는 인터셉터에서 처리)
      }
    };

    initializeAuth();
  }, [user, setAuth, router]);



  return (
    <div className="min-h-screen flex flex-col bg-white">
      <NavBar />
      <SubBar />
      <SearchBar />
      <PostList />

    </div>
  );
}