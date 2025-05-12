"use client";

import { useEffect, useState } from 'react';
import { useRouter } from 'next/navigation';
import { useAuth } from '@/hooks/useAuth';
import { authService } from '@/services/auth';
import { imageService } from '@/services/image';
import { getProfileImageUrl } from '@/utils/image';
import { toast } from 'react-hot-toast';
import Image from 'next/image';
import { FaUserCircle } from 'react-icons/fa';
import Link from 'next/link';

export default function MyProfileEditPage() {
  const { user } = useAuth();
  const router = useRouter();
  const [isLoading, setIsLoading] = useState(true);
  const [isSubmitting, setIsSubmitting] = useState(false);
  const [form, setForm] = useState({
    name: '',
    profileImage: null as File | null,
    profileImageUrl: '',
    gender: 'MALE' as 'MALE' | 'FEMALE',
    birthday: '',
  });
  const [previewUrl, setPreviewUrl] = useState<string>('');

  useEffect(() => {
    const fetchUser = async () => {
      setIsLoading(true);
      try {
        const userInfo = await authService.getUserInfo();
        setForm({
          name: userInfo.name,
          profileImage: null,
          profileImageUrl: userInfo.profileImageUrl || '',
          gender: userInfo.gender || 'MALE',
          birthday: userInfo.birthday ? new Date(userInfo.birthday).toISOString().split('T')[0] : '',
        });
        setPreviewUrl(userInfo.profileImageUrl || '');
      } catch (e) {
        toast.error('내 정보를 불러올 수 없습니다.');
        router.push('/profile');
      } finally {
        setIsLoading(false);
      }
    };
    fetchUser();
  }, [router]);

  const handleImageChange = (e: React.ChangeEvent<HTMLInputElement>) => {
    const file = e.target.files?.[0];
    if (file) {
      setForm(prev => ({ ...prev, profileImage: file }));
      const reader = new FileReader();
      reader.onloadend = () => {
        setPreviewUrl(reader.result as string);
      };
      reader.readAsDataURL(file);
    }
  };

  const handleSubmit = async (e: React.FormEvent) => {
    e.preventDefault();
    setIsSubmitting(true);
    try {
      let imageUrl = form.profileImageUrl;
      if (form.profileImage) {
        imageUrl = await imageService.uploadImage(form.profileImage);
      }
      await authService.editUserProfile(user!.username, {
        name: form.name,
        profileImageUrl: imageUrl,
        gender: form.gender,
        birthday: form.birthday ? new Date(form.birthday).toISOString() : null,
      });
      toast.success('프로필이 변경되었습니다!');
      router.push('/profile');
    } catch (e) {
      toast.error('프로필 변경에 실패했습니다.');
    } finally {
      setIsSubmitting(false);
    }
  };

  if (isLoading) {
    return (
      <div className="min-h-screen flex items-center justify-center bg-gray-50">
        <div className="animate-spin rounded-full h-12 w-12 border-t-2 border-b-2 border-gray-900"></div>
      </div>
    );
  }

  return (
    <div className="min-h-screen flex flex-col items-center justify-center bg-gray-50">
      <div className="w-full max-w-md h-screen bg-white flex flex-col items-center p-8 relative">
        <Link 
          href="/profile"
          className="absolute top-4 right-4 w-8 h-8 flex items-center justify-center text-gray-500 hover:text-gray-700"
        >
          <svg xmlns="http://www.w3.org/2000/svg" fill="none" viewBox="0 0 24 24" strokeWidth={2} stroke="currentColor" className="w-6 h-6">
            <path strokeLinecap="round" strokeLinejoin="round" d="M6 18L18 6M6 6l12 12" />
          </svg>
        </Link>
        <h2 className="text-2xl font-extrabold text-gray-900 mb-6 text-center">내 프로필 수정</h2>
        <form onSubmit={handleSubmit} className="w-full flex flex-col gap-6">
          <div className="flex flex-col items-center">
            <div className="relative h-32 w-32 rounded-full overflow-hidden border-4 border-gray-200 mb-2 flex items-center justify-center">
              {previewUrl || form.profileImageUrl ? (
                <Image
                  src={previewUrl || getProfileImageUrl(form.profileImageUrl)}
                  alt="프로필 이미지"
                  unoptimized={true}
                  fill
                  className="object-cover"
                />
              ) : (
                <FaUserCircle className="w-32 h-32 text-gray-300" />
              )}
            </div>
            <label className="mt-2 px-4 py-2 bg-white border border-gray-300 rounded-md shadow-sm text-sm font-medium text-gray-700 hover:bg-gray-50 cursor-pointer">
              이미지 변경
              <input
                type="file"
                className="hidden"
                accept="image/*"
                onChange={handleImageChange}
              />
            </label>
            <button
              type="button"
              className="mt-2 ml-2 px-4 py-2 bg-gray-100 border border-gray-300 rounded-md shadow-sm text-sm font-medium text-gray-500 hover:bg-gray-200"
              onClick={() => {
                setForm(prev => ({ ...prev, profileImage: null, profileImageUrl: '' }));
                setPreviewUrl('');
              }}
            >
              기본 이미지 사용
            </button>
          </div>
          <div>
            <label className="block text-gray-700 text-sm mb-1">이름</label>
            <input
              name="name"
              value={form.name}
              onChange={e => setForm(prev => ({ ...prev, name: e.target.value }))}
              className="w-full px-0 py-3 border-0 border-b-2 border-gray-200 text-gray-900 focus:outline-none focus:border-black bg-transparent"
              maxLength={20}
              required
            />
          </div>
          <div>
            <label className="block text-gray-700 text-sm mb-1">성별</label>
            <select
              name="gender"
              value={form.gender}
              onChange={e => setForm(prev => ({ ...prev, gender: e.target.value as 'MALE' | 'FEMALE' }))}
              className="w-full px-0 py-3 border-0 border-b-2 border-gray-200 text-gray-900 focus:outline-none focus:border-black bg-transparent"
              required
            >
              <option value="MALE">남성</option>
              <option value="FEMALE">여성</option>
            </select>
          </div>
          <div>
            <label className="block text-gray-700 text-sm mb-1">생일</label>
            <input
              type="date"
              name="birthday"
              value={form.birthday}
              onChange={e => setForm(prev => ({ ...prev, birthday: e.target.value }))}
              className="w-full px-0 py-3 border-0 border-b-2 border-gray-200 text-gray-900 focus:outline-none focus:border-black bg-transparent"
              required
            />
          </div>
          <button
            type="submit"
            disabled={isSubmitting}
            className="w-full py-3 rounded-xl bg-indigo-500 hover:bg-indigo-600 text-white font-bold text-base disabled:bg-gray-300"
          >
            {isSubmitting ? '저장 중...' : '저장하기'}
          </button>
        </form>
      </div>
    </div>
  );
} 