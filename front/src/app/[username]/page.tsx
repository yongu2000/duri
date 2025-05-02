'use client';

import { useState, useEffect, use } from 'react';
import { useRouter } from 'next/navigation';
import { authService } from '@/services/auth';
import { useAuth } from '@/hooks/useAuth';
import { toast } from 'react-hot-toast';
import Image from 'next/image';
import { PublicUserInfo } from '@/types/auth';
import Link from 'next/link';
import { getProfileImageUrl } from '@/utils/image';

export default function ProfilePage({ params }: { params: Promise<{ username: string }> }) {
  const resolvedParams = use(params);
  const router = useRouter();
  const { user } = useAuth();
  const [isLoading, setIsLoading] = useState(true);
  const [profileUser, setProfileUser] = useState<PublicUserInfo>({
    username: '',
    name: '',
    bio: '',
    createdAt: '',
  });

  useEffect(() => {
    const loadUserInfo = async () => {
      try {
        const userInfo = await authService.getUserByUsername(resolvedParams.username);
        setProfileUser(userInfo);
      } catch (error) {
        console.error('사용자 정보 로드 실패:', error);
        toast.error('사용자 정보를 불러오는데 실패했습니다.');
      } finally {
        setIsLoading(false);
      }
    };

    loadUserInfo();
  }, [resolvedParams.username]);

  const isOwnProfile = user?.username === resolvedParams.username;

  if (isLoading) {
    return (
      <div className="min-h-screen flex items-center justify-center">
        <div className="animate-spin rounded-full h-12 w-12 border-t-2 border-b-2 border-indigo-500"></div>
      </div>
    );
  }

  return (
    <div className="min-h-screen bg-gray-50 py-12">
      <div className="max-w-3xl mx-auto px-4 sm:px-6 lg:px-8">
        <div className="bg-white shadow rounded-lg overflow-hidden">
          {/* 프로필 헤더 */}
          <div className="relative h-48 bg-gradient-to-r from-indigo-500 to-purple-600">
            <div className="absolute -bottom-12 left-1/2 transform -translate-x-1/2">
              <div className="relative h-32 w-32 rounded-full overflow-hidden border-4 border-white bg-white">
                <Image
                  src={getProfileImageUrl(profileUser.profileImageUrl)}
                  alt="프로필 이미지"
                  fill
                  unoptimized={true}
                  className="object-cover"
                />
              </div>
            </div>
          </div>

          {/* 프로필 정보 */}
          <div className="pt-16 pb-8 px-6 text-center">
            <h2 className="text-2xl font-bold text-gray-900">{profileUser.name}</h2>
            <p className="mt-1 text-gray-500">@{profileUser.username}</p>
            {profileUser.bio && (
              <p className="mt-4 text-gray-600 max-w-xl mx-auto">{profileUser.bio}</p>
            )}
          </div>

          {/* 탭 네비게이션 */}
          <div className="border-t border-gray-200">
            <div className="flex justify-center space-x-1">
              <Link
                href={`/${profileUser.username}/posts`}
                className="px-6 py-2 text-sm font-medium text-gray-500 hover:text-gray-700 border-b-2 border-transparent hover:border-indigo-500"
              >
                작성한 글
              </Link>
              <Link
                href={`/${profileUser.username}/likes`}
                className="px-6 py-2 text-sm font-medium text-gray-500 hover:text-gray-700 border-b-2 border-transparent hover:border-indigo-500"
              >
                좋아요한 글
              </Link>
            </div>
          </div>

          {/* 계정 정보 */}
          <div className="border-t border-gray-200 px-6 py-8">
            <div className="max-w-2xl mx-auto">
              <h3 className="text-lg font-medium text-gray-900 mb-6">계정 정보</h3>
              <dl className="grid grid-cols-1 gap-6 sm:grid-cols-2">
                <div>
                  <dt className="text-sm font-medium text-gray-500">이름</dt>
                  <dd className="mt-1 text-lg text-gray-900">{profileUser.name}</dd>
                </div>
                <div>
                  <dt className="text-sm font-medium text-gray-500">가입일</dt>
                  <dd className="mt-1 text-lg text-gray-900">
                    {new Date(profileUser.createdAt).toLocaleDateString('ko-KR', {
                      year: 'numeric',
                      month: 'long',
                      day: 'numeric'
                    })}
                  </dd>
                </div>
              </dl>
            </div>
          </div>

          {/* 자기소개 */}
          <div className="border-t border-gray-200 px-6 py-8">
            <div className="max-w-2xl mx-auto">
              <h3 className="text-lg font-medium text-gray-900 mb-6">자기소개</h3>
              <div className="bg-gray-50 rounded-lg p-6">
                <p className="text-gray-700 whitespace-pre-wrap">
                  {profileUser.bio || '자기소개를 작성해주세요.'}
                </p>
              </div>
            </div>
          </div>

          {/* 하단 버튼 */}
          <div className="border-t border-gray-200 px-6 py-4 bg-gray-50">
            <div className="flex justify-end">
              {isOwnProfile && (
                <Link
                  href={`/${profileUser.username}/edit`}
                  className="px-4 py-2 text-sm font-medium text-gray-700 bg-white border border-gray-300 rounded-md hover:bg-gray-50 focus:outline-none focus:ring-2 focus:ring-offset-2 focus:ring-indigo-500"
                >
                  프로필 수정
                </Link>
              )}
            </div>
          </div>
        </div>
      </div>
    </div>
  );
} 