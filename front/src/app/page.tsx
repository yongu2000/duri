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
        // refreshToken 쿠키가 없으면 로그인 페이지로 리다이렉트
        const refreshToken = getCookie('REFRESH_TOKEN');
        if (!refreshToken) {
          router.push('/login');
          return;
        }

        // 사용자 정보 요청
        const userInfo = await authService.getUserInfo();
        
        // 프로필 정보가 없으면 프로필 수정 페이지로 리다이렉트
        if (!userInfo.gender || !userInfo.birthday) {
          router.push('/profile/my/edit');
          return;
        }

        // 커플 코드가 없으면 커플 연결 페이지로 리다이렉트
        if (!userInfo.coupleCode) {
          router.push('/couple/link');
          return;
        }

        // 모든 검증이 통과되면 사용자 정보 업데이트
        setAuth(userInfo);
        
      } catch (error) {
        console.error('인증 초기화 실패:', error);
        router.push('/login');
      }
    };

    initializeAuth();
  }, []); // 빈 의존성 배열로 변경

  // user 상태 변경 감지를 위한 별도의 useEffect
  useEffect(() => {
    if (user && !user.coupleCode) {
      router.push('/couple/link');
    }
  }, [user?.coupleCode, router]);

  return (
    <div className="min-h-screen flex flex-col bg-white">
      <NavBar />
      <SubBar />
      <SearchBar />
      <PostList />

    </div>
  );
}