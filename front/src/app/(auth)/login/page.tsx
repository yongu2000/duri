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
    <div className="min-h-screen flex items-center justify-center bg-white py-12 px-4 sm:px-6 lg:px-8">
      <div className="max-w-md w-full space-y-8">
        <div className="text-center">
          <h2 className="text-5xl font-black text-gray-900 tracking-tight" style={{ fontFamily: "'BMDOHYEON', sans-serif" }}>
            <span style={{ fontFamily: "'Cafe24Supermagic-Bold-v1.0', cursive", fontSize: '1.2em', verticalAlign: 'middle' }}>“</span>
            두리
            <span style={{ fontFamily: "'Cafe24Supermagic-Bold-v1.0', cursive", fontSize: '1.2em', verticalAlign: 'middle' }}>”</span>
          </h2>
          <p className="mt-1 text-2xl text-gray-900 iceJaram-Rg-important">추억도 별점도, 둘이</p>
        </div>
        <form className="mt-8 space-y-6 px-2 sm:px-0" onSubmit={handleSubmit(onSubmit)}>
          <div className="rounded-md space-y-4">
            <div>
              <input
                {...register('username')}
                type="text"
                placeholder="아이디 또는 이메일"
                className="appearance-none relative block w-full px-4 py-3 border-b-2 border-gray-200 placeholder-gray-400 text-gray-900 focus:outline-none focus:border-black focus:z-10 sm:text-sm"
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
                className="appearance-none relative block w-full px-4 py-3 border-b-2 border-gray-200 placeholder-gray-400 text-gray-900 focus:outline-none focus:border-black focus:z-10 sm:text-sm"
              />
              {errors.password && (
                <p className="mt-1 text-sm text-red-600">{errors.password.message as string}</p>
              )}
            </div>
          </div>
          <button
            type="submit"
            className="group relative w-full flex justify-center py-3 px-4 border border-transparent text-sm font-medium text-white bg-black hover:bg-gray-800 focus:outline-none focus:ring-2 focus:ring-offset-2 focus:ring-black rounded-l-xl rounded-r-xl mt-4"
          >
            로그인
          </button>
          <div className="w-full flex items-center gap-2 text-sm justify-center mb-3 mt-2">
            <Link href="/find-password" className="font-medium text-gray-400 hover:text-black">
              비밀번호 찾기
            </Link>
            <span className="text-gray-300">|</span>
            <Link href="/join" className="font-medium text-gray-400 hover:text-black">
              회원가입
            </Link>
          </div>
          <div className="flex items-center my-8">
            <div className="flex-grow border-t border-gray-200"></div>
          </div>
          <div className="my-4">
            <div className="text-center mb-6 text-sm font-medium text-gray-400">간편 로그인</div>
            <div className="flex justify-center gap-8">
              <button
                type="button"
                aria-label="카카오 로그인"
                onClick={() => window.location.href = `${API_URL}/oauth2/authorization/kakao`}
                className="p-0 bg-transparent border-none shadow-none hover:bg-transparent focus:outline-none"
              >
                <img src="/kakao.png" alt="카카오" className="w-14 h-14" />
              </button>
              <button
                type="button"
                aria-label="구글 로그인"
                onClick={() => window.location.href = `${API_URL}/oauth2/authorization/google`}
                className="p-0 bg-transparent border-none shadow-none hover:bg-transparent focus:outline-none"
              >
                <img src="/google.png" alt="구글" className="w-14 h-14" />
              </button>
              <button
                type="button"
                aria-label="네이버 로그인"
                onClick={() => window.location.href = `${API_URL}/oauth2/authorization/naver`}
                className="p-0 bg-transparent border-none shadow-none hover:bg-transparent focus:outline-none"
              >
                <img src="/naver.png" alt="네이버" className="w-14 h-14" />
              </button>
            </div>
          </div>
        </form>
      </div>
    </div>
  );
}