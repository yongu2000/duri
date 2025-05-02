'use client';

import { useForm } from 'react-hook-form';
import { zodResolver } from '@hookform/resolvers/zod';
import { z } from 'zod';
import { useRouter } from 'next/navigation';
import Link from 'next/link';
import { authService } from '@/services/auth';
import { useAuth } from '@/hooks/useAuth';
import { toast } from 'react-hot-toast';

const API_URL = process.env.NEXT_PUBLIC_API_URL

const loginSchema = z.object({
  username: z.string().min(1, '아이디를 입력해주세요'),
  password: z.string().min(6, '비밀번호는 최소 6자 이상이어야 합니다'),
  rememberMe: z.boolean().optional(),
});

export default function LoginPage() {
  const router = useRouter();
  const { setAuth } = useAuth();
  const { register, handleSubmit, watch, formState: { errors } } = useForm({
    resolver: zodResolver(loginSchema)
  });
  
  const rememberMe = watch('rememberMe');

  const onSubmit = async (data: any) => {
    try {
      console.log('로그인 시도:', data);
      // 1. 먼저 로그인
      await authService.login({
        username: data.username,
        password: data.password,
        rememberMe: data.rememberMe
      });
      
      // 2. 로그인 성공 후 사용자 정보 요청
      const userInfo = await authService.getUserInfo();
      
      // 3. 전역 상태에 사용자 정보 저장
      setAuth(userInfo);
      
      toast.success('로그인에 성공했습니다!');
      router.push('/');
    } catch (error) {
      console.error('로그인 실패:', error);
      toast.error('로그인에 실패했습니다. 아이디와 비밀번호를 확인해주세요.');
    }
  };

  return (
    <div className="min-h-screen flex items-center justify-center bg-gray-50 py-12 px-4 sm:px-6 lg:px-8">
      <div className="max-w-md w-full space-y-8">
        <div>
          <h2 className="mt-6 text-center text-3xl font-bold text-gray-900">
            보일러플레이트
          </h2>
        </div>
        <form className="mt-8 space-y-6" onSubmit={handleSubmit(onSubmit)}>
          <div className="rounded-md shadow-sm space-y-4">
            <div>
              <input
                {...register('username')}
                type="text"
                placeholder="아이디 또는 이메일"
                className="appearance-none rounded-md relative block w-full px-3 py-2 border border-gray-300 placeholder-gray-500 text-gray-900 focus:outline-none focus:ring-indigo-500 focus:border-indigo-500 focus:z-10 sm:text-sm"
              />
              {errors.username && (
                <p className="mt-1 text-sm text-red-600">{errors.username.message as string}</p>
              )}
            </div>
            <div>
              <input
                {...register('password')}
                type="password"
                placeholder="비밀번호"
                className="appearance-none rounded-md relative block w-full px-3 py-2 border border-gray-300 placeholder-gray-500 text-gray-900 focus:outline-none focus:ring-indigo-500 focus:border-indigo-500 focus:z-10 sm:text-sm"
              />
              {errors.password && (
                <p className="mt-1 text-sm text-red-600">{errors.password.message as string}</p>
              )}
            </div>
          </div>

          <div className="flex items-center justify-between">
            <div className="flex items-center">
              <input
                {...register('rememberMe')}
                type="checkbox"
                className="h-4 w-4 text-indigo-600 focus:ring-indigo-500 border-gray-300 rounded"
              />
              <label htmlFor="remember-me" className="ml-2 block text-sm text-gray-900">
                로그인 상태 유지
              </label>
            </div>
            <div className="text-sm">
              <Link href="/find-password" className="text-indigo-600 hover:text-indigo-500">
                비밀번호 찾기
              </Link>
            </div>
          </div>

          {rememberMe && (
            <div className="bg-yellow-50 border-l-4 border-yellow-400 p-4">
              <div className="flex">
                <div className="flex-shrink-0">
                  <svg className="h-5 w-5 text-yellow-400" viewBox="0 0 20 20" fill="currentColor">
                    <path fillRule="evenodd" d="M8.257 3.099c.765-1.36 2.722-1.36 3.486 0l5.58 9.92c.75 1.334-.213 2.98-1.742 2.98H4.42c-1.53 0-2.493-1.646-1.743-2.98l5.58-9.92zM11 13a1 1 0 11-2 0 1 1 0 012 0zm-1-8a1 1 0 00-1 1v3a1 1 0 002 0V6a1 1 0 00-1-1z" clipRule="evenodd" />
                  </svg>
                </div>
                <div className="ml-3">
                  <p className="text-sm text-yellow-700">
                    로그인 상태 유지를 선택하면 30일 동안 자동 로그인이 유지됩니다. 
                    공용 컴퓨터에서는 보안을 위해 선택하지 마세요.
                  </p>
                </div>
              </div>
            </div>
          )}

          <div className="space-y-3">
            <button
              type="submit"
              className="group relative w-full flex justify-center py-2 px-4 border border-transparent text-sm font-medium rounded-md text-white bg-indigo-600 hover:bg-indigo-700 focus:outline-none focus:ring-2 focus:ring-offset-2 focus:ring-indigo-500"
            >
              로그인
            </button>

            <button
              type="button"
              onClick={() => window.location.href = `${API_URL}/oauth2/authorization/kakao`}
              className="w-full flex justify-center py-2 px-4 border border-transparent text-sm font-medium rounded-md text-black bg-[#FEE500] hover:bg-[#FDD800] focus:outline-none focus:ring-2 focus:ring-offset-2 focus:ring-yellow-500"
            >
              카카오 로그인
            </button>

            <button
              type="button"
              onClick={() => window.location.href = `${API_URL}/oauth2/authorization/google`}
              className="w-full flex justify-center py-2 px-4 border border-transparent text-sm font-medium rounded-md text-white bg-[#4285F4] hover:bg-[#3367D6] focus:outline-none focus:ring-2 focus:ring-offset-2 focus:ring-blue-500"
            >
              구글 로그인
            </button>

            <button
              type="button"
              onClick={() => window.location.href = `${API_URL}/oauth2/authorization/naver`}
              className="w-full flex justify-center py-2 px-4 border border-transparent text-sm font-medium rounded-md text-white bg-[#03C75A] hover:bg-[#02B150] focus:outline-none focus:ring-2 focus:ring-offset-2 focus:ring-green-500"
            >
              네이버 로그인
            </button>
          </div>
        </form>

        <div className="text-center">
          <Link 
            href="/join" 
            className="w-full inline-block py-3 px-4 border border-transparent text-sm font-medium rounded-md text-white bg-blue-600 hover:bg-blue-700 focus:outline-none focus:ring-2 focus:ring-offset-2 focus:ring-blue-500"
          >
            회원가입
          </Link>
        </div>
      </div>
    </div>
  );
}