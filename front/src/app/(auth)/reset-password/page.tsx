'use client';

import { useState, Suspense } from 'react';
import { useForm } from 'react-hook-form';
import { zodResolver } from '@hookform/resolvers/zod';
import { z } from 'zod';
import { useRouter, useSearchParams } from 'next/navigation';
import Link from 'next/link';
import { authService } from '@/services/auth';
import { toast } from 'react-hot-toast';

const resetPasswordSchema = z.object({
  password: z.string()
    .min(6, '비밀번호는 최소 6자 이상이어야 합니다')
    .max(20, '비밀번호는 최대 20자까지 가능합니다')
    .regex(/^(?=.*[a-z])(?=.*[A-Z])(?=.*\d)(?=.*[@$!%*?&])[A-Za-z\d@$!%*?&]{6,20}$/, 
      '비밀번호는 대문자, 소문자, 숫자, 특수문자를 포함해야 합니다'),
  passwordConfirm: z.string()
}).refine((data) => data.password === data.passwordConfirm, {
  message: "비밀번호가 일치하지 않습니다",
  path: ["passwordConfirm"],
});

// 비밀번호 재설정 폼 컴포넌트
function ResetPasswordForm({ token }: { token: string | null }) {
  const router = useRouter();
  const [isSubmitted, setIsSubmitted] = useState(false);
  const { register, handleSubmit, formState: { errors } } = useForm({
    resolver: zodResolver(resetPasswordSchema)
  });

  const onSubmit = async (data: any) => {
    if (!token) {
      toast.error('유효하지 않은 링크입니다.');
      return;
    }

    try {
      await authService.resetPassword(token, data.password);
      setIsSubmitted(true);
      toast.success('비밀번호가 성공적으로 재설정되었습니다.');
    } catch (error) {
      console.error('비밀번호 재설정 실패:', error);
      toast.error('비밀번호 재설정에 실패했습니다.');
    }
  };

  if (!token) {
    return (
      <div className="min-h-screen flex items-center justify-center bg-gray-50 py-12 px-4 sm:px-6 lg:px-8">
        <div className="max-w-md w-full space-y-8">
          <div className="bg-red-50 border-l-4 border-red-400 p-4">
            <div className="flex">
              <div className="flex-shrink-0">
                <svg className="h-5 w-5 text-red-400" viewBox="0 0 20 20" fill="currentColor">
                  <path fillRule="evenodd" d="M10 18a8 8 0 100-16 8 8 0 000 16zM8.707 7.293a1 1 0 00-1.414 1.414L8.586 10l-1.293 1.293a1 1 0 101.414 1.414L10 11.414l1.293 1.293a1 1 0 001.414-1.414L11.414 10l1.293-1.293a1 1 0 00-1.414-1.414L10 8.586 8.707 7.293z" clipRule="evenodd" />
                </svg>
              </div>
              <div className="ml-3">
                <p className="text-sm text-red-700">
                  유효하지 않은 링크입니다.<br />
                  비밀번호 찾기를 다시 시도해주세요.
                </p>
              </div>
            </div>
          </div>
          <div className="text-center">
            <Link href="/find-password" className="text-sm text-indigo-600 hover:text-indigo-500">
              비밀번호 찾기로 돌아가기
            </Link>
          </div>
        </div>
      </div>
    );
  }

  return (
    <div className="min-h-screen flex items-center justify-center bg-gray-50 py-12 px-4 sm:px-6 lg:px-8">
      <div className="max-w-md w-full space-y-8">
        <div>
          <h2 className="mt-6 text-center text-3xl font-bold text-gray-900">
            비밀번호 재설정
          </h2>
          <p className="mt-2 text-center text-sm text-gray-600">
            새로운 비밀번호를 입력해주세요.
          </p>
        </div>

        {!isSubmitted ? (
          <form className="mt-8 space-y-6" onSubmit={handleSubmit(onSubmit)}>
            <div>
              <label htmlFor="password" className="block text-sm font-medium text-gray-700">
                새 비밀번호
              </label>
              <div className="mt-1">
                <input
                  {...register('password')}
                  type="password"
                  className="appearance-none block w-full px-3 py-2 border border-gray-300 rounded-md shadow-sm placeholder-gray-400 focus:outline-none focus:ring-indigo-500 focus:border-indigo-500 sm:text-sm"
                />
                {errors.password && (
                  <p className="mt-1 text-sm text-red-600">{errors.password.message as string}</p>
                )}
              </div>
            </div>

            <div>
              <label htmlFor="passwordConfirm" className="block text-sm font-medium text-gray-700">
                새 비밀번호 확인
              </label>
              <div className="mt-1">
                <input
                  {...register('passwordConfirm')}
                  type="password"
                  className="appearance-none block w-full px-3 py-2 border border-gray-300 rounded-md shadow-sm placeholder-gray-400 focus:outline-none focus:ring-indigo-500 focus:border-indigo-500 sm:text-sm"
                />
                {errors.passwordConfirm && (
                  <p className="mt-1 text-sm text-red-600">{errors.passwordConfirm.message as string}</p>
                )}
              </div>
            </div>

            <div>
              <button
                type="submit"
                className="w-full flex justify-center py-2 px-4 border border-transparent rounded-md shadow-sm text-sm font-medium text-white bg-indigo-600 hover:bg-indigo-700 focus:outline-none focus:ring-2 focus:ring-offset-2 focus:ring-indigo-500"
              >
                비밀번호 재설정
              </button>
            </div>
          </form>
        ) : (
          <div className="mt-8 space-y-6">
            <div className="bg-green-50 border-l-4 border-green-400 p-4">
              <div className="flex">
                <div className="flex-shrink-0">
                  <svg className="h-5 w-5 text-green-400" viewBox="0 0 20 20" fill="currentColor">
                    <path fillRule="evenodd" d="M10 18a8 8 0 100-16 8 8 0 000 16zm3.707-9.293a1 1 0 00-1.414-1.414L9 10.586 7.707 9.293a1 1 0 00-1.414 1.414l2 2a1 1 0 001.414 0l4-4z" clipRule="evenodd" />
                  </svg>
                </div>
                <div className="ml-3">
                  <p className="text-sm text-green-700">
                    비밀번호가 성공적으로 재설정되었습니다.<br />
                    로그인 페이지로 이동하여 새 비밀번호로 로그인해주세요.
                  </p>
                </div>
              </div>
            </div>
            <div className="text-center">
              <Link href="/login" className="text-sm text-indigo-600 hover:text-indigo-500">
                로그인 페이지로 이동
              </Link>
            </div>
          </div>
        )}
      </div>
    </div>
  );
}

// 메인 컴포넌트
export default function ResetPasswordPage() {
  return (
    <Suspense fallback={
      <div className="min-h-screen flex items-center justify-center bg-gray-50">
        <div className="animate-spin rounded-full h-12 w-12 border-t-2 border-b-2 border-indigo-500"></div>
      </div>
    }>
      <ResetPasswordContent />
    </Suspense>
  );
}

// useSearchParams를 사용하는 컴포넌트
function ResetPasswordContent() {
  const searchParams = useSearchParams();
  const token = searchParams.get('token');
  
  return <ResetPasswordForm token={token} />;
} 