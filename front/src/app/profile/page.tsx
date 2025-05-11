'use client';

import Image from 'next/image';
import Link from 'next/link';
import { useEffect, useState } from 'react';
import { useAuth } from '@/hooks/useAuth';
import { coupleService, CoupleProfileResponse } from '@/services/couple';
import { FaUserCircle } from 'react-icons/fa';
import { authService } from '@/services/auth';

export default function ProfilePage() {
  const { user, setAuth } = useAuth();
  const coupleCode = user?.coupleCode;
  const [profile, setProfile] = useState<CoupleProfileResponse | null>(null);
  const [loading, setLoading] = useState(true);

  useEffect(() => {
    if (!coupleCode) return;
    const fetchProfile = async () => {
      setLoading(true);
      try {
        const data = await coupleService.getCoupleProfile(coupleCode);
        setProfile(data);
      } catch (e) {
        setProfile(null);
      } finally {
        setLoading(false);
      }
    };
    fetchProfile();
  }, [coupleCode]);

  useEffect(() => {
    // 항상 최신 유저 정보로 갱신
    const fetchUser = async () => {
      try {
        const userInfo = await authService.getUserInfo();
        setAuth(userInfo);
      } catch (e) {
        // ignore
      }
    };
    fetchUser();
  }, [setAuth]);

  if (loading) {
    return (
      <div className="min-h-screen flex items-center justify-center bg-gray-50">
        <div className="animate-spin rounded-full h-12 w-12 border-t-2 border-b-2 border-gray-900"></div>
      </div>
    );
  }

  if (!profile) {
    return (
      <div className="min-h-screen flex items-center justify-center bg-gray-50">
        <div className="text-gray-500">프로필 정보를 불러올 수 없습니다.</div>
      </div>
    );
  }

  return (
    <div className="min-h-screen flex flex-col items-center justify-center bg-gray-50">
      <div className="w-full max-w-md h-screen bg-white flex flex-col items-center p-8 relative">
        <Link 
          href="/"
          className="absolute top-4 right-4 w-8 h-8 flex items-center justify-center text-gray-500 hover:text-gray-700"
        >
          <svg xmlns="http://www.w3.org/2000/svg" fill="none" viewBox="0 0 24 24" strokeWidth={2} stroke="currentColor" className="w-6 h-6">
            <path strokeLinecap="round" strokeLinejoin="round" d="M6 18L18 6M6 6l12 12" />
          </svg>
        </Link>
        {/* 커플 이름 */}
        <div className="text-2xl font-extrabold text-gray-900 mb-1 text-center">{profile.coupleName}</div>
        {/* 커플 코드 */}
        <div className="text-center text-gray-400 text-xs mb-3">커플 아이디: {profile.coupleCode}</div>
        {/* 두 사람 프로필 */}
        <div className="flex items-center gap-6 mb-2">
          <div className="flex flex-col items-center">
            <div className="w-20 h-20 rounded-full overflow-hidden bg-gray-100 relative mb-2 flex items-center justify-center">
              {profile.userLeftProfileImageUrl ? (
                <Image
                  src={profile.userLeftProfileImageUrl}
                  alt={profile.userLeftName}
                  fill
                  unoptimized
                  className="object-cover"
                />
              ) : (
                <FaUserCircle className="w-20 h-20 text-gray-300" />
              )}
            </div>
            <div className="text-base font-semibold text-gray-800">{profile.userLeftName}</div>
          </div>
          <div className="flex flex-col items-center">
            <div className="w-20 h-20 rounded-full overflow-hidden bg-gray-100 relative mb-2 flex items-center justify-center">
              {profile.userRightProfileImageUrl ? (
                <Image
                  src={profile.userRightProfileImageUrl}
                  alt={profile.userRightName}
                  fill
                  unoptimized
                  className="object-cover"
                />
              ) : (
                <FaUserCircle className="w-20 h-20 text-gray-300" />
              )}
            </div>
            <div className="text-base font-semibold text-gray-800">{profile.userRightName}</div>
          </div>
        </div>
        {/* 커플 소개 */}
        <div className="text-center text-gray-500 text-sm mb-8">{profile.bio ? profile.bio : '커플 소개가 없습니다'}</div>
        {/* 버튼 목록 */}
        <div className="w-full flex flex-col gap-3">
          <Link href="/profile/couple/edit">
            <button className="w-full py-3 rounded-xl bg-indigo-50 hover:bg-indigo-100 text-indigo-700 font-bold text-base">커플 프로필 수정</button>
          </Link>
          <Link href="/profile/my/edit">
            <button className="w-full py-3 rounded-xl bg-gray-100 hover:bg-gray-200 text-gray-700 font-bold text-base">개인 프로필 수정</button>
          </Link>
          <button className="w-full py-3 rounded-xl bg-gray-100 hover:bg-gray-200 text-gray-700 font-bold text-base">좋아요한 글</button>
          <button className="w-full py-3 rounded-xl bg-gray-100 hover:bg-gray-200 text-gray-700 font-bold text-base">쓴 댓글 보기</button>
          <button className="w-full py-3 rounded-xl bg-gray-100 hover:bg-gray-200 text-gray-700 font-bold text-base">개인 인증정보 수정</button>
          <button className="w-full py-3 rounded-xl bg-gray-100 hover:bg-gray-200 text-gray-700 font-bold text-base">설정</button>
        </div>
      </div>
    </div>
  );
} 