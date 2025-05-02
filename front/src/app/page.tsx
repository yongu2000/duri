'use client';

import { useEffect } from 'react';
import { useRouter } from 'next/navigation';
import { useAuth } from '@/hooks/useAuth';
import { authService } from '@/services/auth';
import Link from 'next/link';
import { getCookie } from 'cookies-next';

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

        // 1. 토큰 재발급 요청
        const accessToken = await authService.reissueToken();
        
        // 2. 사용자 정보 요청
        const userInfo = await authService.getUserInfo();
        
        // 3. 전역 상태에 사용자 정보 저장
        setAuth(userInfo);
      } catch (error) {
        console.error('인증 초기화 실패:', error);
        // 에러 발생 시 조용히 실패 (이미 로그인 페이지로의 리다이렉트는 인터셉터에서 처리)
      }
    };

    initializeAuth();
  }, [user, setAuth]);

  // useEffect(() => {
  //   if (user && !user.coupleId) {
  //     router.replace('/couple/link');
  //   }
  // }, [user, router]);

  // 커플 연결이 안 된 경우엔 아무것도 렌더링하지 않음 (리다이렉트)
  // if (!user || !user.coupleId) return null;

  // 하드코딩된 리뷰 데이터 (2번 그림 참고)
  const review = {
    date: '2025-04-03',
    place: '맛있는 한식당',
    tags: ['맛집', '서울'],
    male: {
      stars: 4,
      text: '맛있고 분위기도 좋았어요. 특히 갈비찜이 일품이었습니다.'
    },
    female: {
      stars: 5,
      text: '서비스도 좋고 음식도 맛있어요. 데이트하기 좋은 분위기였습니다.'
    }
  };

  return (
    <main className="max-w-7xl mx-auto px-4 sm:px-6 lg:px-8 py-12">
      <div className="flex flex-col space-y-4">
        <Link 
          href="/posts/grid" 
          className="text-xl font-semibold text-indigo-600 hover:text-indigo-500"
        >
          카드형 게시판 보기
        </Link>
        <Link 
          href="/posts/list" 
          className="text-xl font-semibold text-indigo-600 hover:text-indigo-500"
        >
          테이블형 게시판 보기
        </Link>
      </div>
      {user && (
        <div className="mt-4">
          <p>안녕하세요, {user.name}님!</p>
        </div>
      )}
      <main className="max-w-md mx-auto px-2 py-4">
        {/* 필터 영역 */}
        <div className="flex flex-col gap-2 mb-4">
          <select className="w-full rounded-lg border px-3 py-2 text-sm">
            <option>카테고리</option>
          </select>
          <select className="w-full rounded-lg border px-3 py-2 text-sm">
            <option>지역</option>
          </select>
          <select className="w-full rounded-lg border px-3 py-2 text-sm">
            <option>정렬</option>
          </select>
        </div>
        {/* 리뷰 카드 */}
        <div className="bg-white rounded-2xl shadow p-4 mb-4">
          <div className="text-lg font-bold mb-1">{review.date}</div>
          <div className="text-base font-semibold mb-1">{review.place}</div>
          <div className="flex gap-2 mb-2">
            {review.tags.map(tag => (
              <span key={tag} className="bg-gray-100 text-gray-700 rounded px-2 py-0.5 text-xs">{tag}</span>
            ))}
          </div>
          <div className="mb-2">
            <div className="font-semibold text-sm mb-0.5">남자 리뷰</div>
            <div className="text-yellow-400 text-base mb-0.5">{'★'.repeat(review.male.stars)}{'☆'.repeat(5 - review.male.stars)}</div>
            <div className="text-sm">{review.male.text}</div>
          </div>
          <div className="mb-2">
            <div className="font-semibold text-sm mb-0.5">여자 리뷰</div>
            <div className="text-yellow-400 text-base mb-0.5">{'★'.repeat(review.female.stars)}{'☆'.repeat(5 - review.female.stars)}</div>
            <div className="text-sm">{review.female.text}</div>
          </div>
          <button className="w-full mt-2 py-2 rounded-lg border text-sm bg-gray-50 hover:bg-gray-100">자세히 보기</button>
        </div>
      </main>
    </main>
  );
}