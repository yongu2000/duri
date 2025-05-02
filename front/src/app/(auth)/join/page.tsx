'use client';

import { useForm } from 'react-hook-form';
import { zodResolver } from '@hookform/resolvers/zod';
import { z } from 'zod';
import { useRouter } from 'next/navigation';
import Link from 'next/link';
import { authService } from '@/services/auth';
import { emailService } from '@/services/email';
import { toast } from 'react-hot-toast';
import { useState, useEffect } from 'react';
import { axiosInstance } from '@/services/axios';

const registerSchema = z.object({
  email: z.string()
    .email('올바른 이메일 형식이 아닙니다')
    .min(5, '이메일은 최소 5자 이상이어야 합니다')
    .max(100, '이메일은 최대 100자까지 가능합니다')
    .regex(/^[a-zA-Z0-9._%+-]+@[a-zA-Z0-9.-]+\.[a-zA-Z]{2,}$/, '올바른 이메일 형식이 아닙니다'),
  verificationCode: z.string()
    .length(6, '인증코드는 6자리여야 합니다')
    .regex(/^\d+$/, '인증코드는 숫자만 입력 가능합니다'),
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

export default function RegisterPage() {
  const router = useRouter();
  const [isEmailVerified, setIsEmailVerified] = useState(false);
  const [endTime, setEndTime] = useState<number | null>(null);
  const [isCodeSent, setIsCodeSent] = useState(false);
  const [isTimerExpired, setIsTimerExpired] = useState(false);
  const [remainingTime, setRemainingTime] = useState<number>(0);
  const { register, handleSubmit, formState: { errors }, watch } = useForm({
    resolver: zodResolver(registerSchema),
  });

  const email = watch('email');
  const verificationCode = watch('verificationCode');

  useEffect(() => {
    if (endTime === null) return;

    const updateTimer = () => {
      const now = Date.now();
      const remaining = Math.max(0, Math.floor((endTime - now) / 1000));
      setRemainingTime(remaining);
      
      if (remaining === 0 && !isEmailVerified) {
        setIsTimerExpired(true);
      }
    };

    // 1초마다 업데이트
    const interval = setInterval(updateTimer, 1000);
    // 초기 업데이트
    updateTimer();

    return () => clearInterval(interval);
  }, [endTime, isEmailVerified]);

  const formatTime = (seconds: number) => {
    const minutes = Math.floor(seconds / 60);
    const remainingSeconds = seconds % 60;
    return `${minutes}:${remainingSeconds.toString().padStart(2, '0')}`;
  };

  const handleSendVerificationCode = async () => {
    try {
      // 이메일 형식 검증
      const emailRegex = /^[a-zA-Z0-9._%+-]+@[a-zA-Z0-9.-]+\.[a-zA-Z]{2,}$/;
      if (!email || !emailRegex.test(email)) {
        toast.error('올바른 이메일 형식이 아닙니다.');
        return;
      }

      // 5분 후의 시간을 설정
      setEndTime(Date.now() + 5 * 60 * 1000);
      setIsCodeSent(true);
      setIsTimerExpired(false);
      toast.success('인증 코드가 이메일로 전송되었습니다.');
      
      emailService.sendVerificationCode(email).catch((error) => {
        console.error('인증 코드 전송 실패:', error);
        toast.error('인증 코드 전송에 실패했습니다. 다시 시도해주세요.');
      });
    } catch (error) {
      console.error('인증 코드 전송 실패:', error);
      toast.error('인증 코드 전송에 실패했습니다.');
    }
  };

  const handleVerifyCode = async () => {
    try {
      const response = await emailService.verifyCode(email, verificationCode);
      if (response.verified) {
        setIsEmailVerified(true);
        toast.success('이메일이 성공적으로 인증되었습니다!');
      } else {
        toast.error('잘못된 인증 코드입니다.');
      }
    } catch (error) {
      console.error('인증 코드 확인 실패:', error);
      toast.error('잘못된 인증 코드입니다.');
    }
  };

  const onSubmit = async (data: any) => {
    if (!isEmailVerified) {
      toast.error('이메일 인증이 필요합니다.');
      return;
    }

    try {
      await authService.join({
        email: data.email,
        password: data.password,
      });
      
      toast.success('회원가입이 성공적으로 완료되었습니다!');
      router.push('/login');
    } catch (error) {
      console.error('회원가입 실패:', error);
      toast.error('회원가입에 실패했습니다. 다시 시도해주세요.');
    }
  };

  return (
    <div className="min-h-screen flex items-center justify-center bg-gray-50">
      <div className="max-w-md w-full space-y-8 p-8 bg-white rounded-lg shadow">
        <h2 className="text-center text-3xl font-bold">회원가입</h2>
        <form className="mt-8 space-y-6" onSubmit={handleSubmit(onSubmit)}>
          <div>
            <label htmlFor="email" className="block text-sm font-medium text-gray-700">
              이메일
            </label>
            <div className="mt-1 flex space-x-2">
              <input
                {...register('email')}
                type="email"
                className="block w-full rounded-md border-gray-300 shadow-sm"
              />
              <button
                type="button"
                onClick={handleSendVerificationCode}
                disabled={!email || isEmailVerified}
                className="px-4 py-2 text-sm font-medium text-white bg-indigo-600 rounded-md hover:bg-indigo-700 disabled:bg-gray-400"
              >
                인증코드 전송
              </button>
            </div>
            {errors.email && (
              <p className="mt-1 text-sm text-red-600">{errors.email.message as string}</p>
            )}
          </div>

          {isCodeSent && (
            <div>
              <label htmlFor="verificationCode" className="block text-sm font-medium text-gray-700">
                인증코드
                {endTime !== null && (
                  <span className="ml-2 text-indigo-600">{formatTime(remainingTime)}</span>
                )}
              </label>
              <div className="mt-1 flex space-x-2">
                <input
                  {...register('verificationCode')}
                  type="text"
                  maxLength={6}
                  pattern="[0-9]*"
                  inputMode="numeric"
                  className="block w-full rounded-md border-gray-300 shadow-sm"
                  placeholder="인증코드 6자리를 입력하세요"
                />
                <button
                  type="button"
                  onClick={handleVerifyCode}
                  disabled={!verificationCode || isEmailVerified}
                  className="px-4 py-2 text-sm font-medium text-white bg-indigo-600 rounded-md hover:bg-indigo-700 disabled:bg-gray-400"
                >
                  확인
                </button>
              </div>
              {errors.verificationCode && (
                <p className="mt-1 text-sm text-red-600">{errors.verificationCode.message as string}</p>
              )}
              {isEmailVerified && (
                <p className="mt-1 text-sm text-green-600">이메일이 인증되었습니다!</p>
              )}
              {isTimerExpired && !isEmailVerified && (
                <div className="mt-2 p-3 bg-red-50 border border-red-200 rounded-md">
                  <p className="text-sm text-red-600">
                    인증 시간이 만료되었습니다. 인증코드를 다시 전송해주세요.
                  </p>
                  <button
                    type="button"
                    onClick={handleSendVerificationCode}
                    className="mt-2 text-sm font-medium text-red-600 hover:text-red-700"
                  >
                    인증코드 다시 전송하기
                  </button>
                </div>
              )}
            </div>
          )}

          <div>
            <label htmlFor="password" className="block text-sm font-medium text-gray-700">
              비밀번호
            </label>
            <input
              {...register('password')}
              type="password"
              className="mt-1 block w-full rounded-md border-gray-300 shadow-sm"
            />
            {errors.password && (
              <p className="mt-1 text-sm text-red-600">{errors.password.message as string}</p>
            )}
          </div>

          <div>
            <label htmlFor="passwordConfirm" className="block text-sm font-medium text-gray-700">
              비밀번호 확인
            </label>
            <input
              {...register('passwordConfirm')}
              type="password"
              className="mt-1 block w-full rounded-md border-gray-300 shadow-sm"
            />
            {errors.passwordConfirm && (
              <p className="mt-1 text-sm text-red-600">{errors.passwordConfirm.message as string}</p>
            )}
          </div>

          <button
            type="submit"
            disabled={!isEmailVerified}
            className="w-full flex justify-center py-2 px-4 border border-transparent rounded-md shadow-sm text-sm font-medium text-white bg-indigo-600 hover:bg-indigo-700 disabled:bg-gray-400"
          >
            회원가입
          </button>

          <div className="text-center">
            <Link href="/login" className="text-indigo-600 hover:text-indigo-500">
              이미 계정이 있으신가요? 로그인하기
            </Link>
          </div>
        </form>
      </div>
    </div>
  );
}