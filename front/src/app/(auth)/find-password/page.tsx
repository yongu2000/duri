'use client';

import { useState } from 'react';
import { useForm } from 'react-hook-form';
import { zodResolver } from '@hookform/resolvers/zod';
import { z } from 'zod';
import Link from 'next/link';
import { emailService } from '@/services/email';
import { toast } from 'react-hot-toast';

const findPasswordSchema = z.object({
  email: z.string()
    .email('올바른 이메일 형식이 아닙니다')
    .min(5, '이메일은 최소 5자 이상이어야 합니다')
    .max(100, '이메일은 최대 100자까지 가능합니다')
    .regex(/^[a-zA-Z0-9._%+-]+@[a-zA-Z0-9.-]+\.[a-zA-Z]{2,}$/, '올바른 이메일 형식이 아닙니다'),
});

export default function FindPasswordPage() {
  const [isSubmitted, setIsSubmitted] = useState(false);
  const [email, setEmail] = useState('');
  const { register, handleSubmit, formState: { errors } } = useForm({
    resolver: zodResolver(findPasswordSchema)
  });

  const onSubmit = async (data: any) => {
    emailService.sendPasswordResetEmail(data.email);
    setIsSubmitted(true);
    toast.success('비밀번호 재설정 링크가 이메일로 전송되었습니다.');
  };

  const handleEmailChange = (e: React.ChangeEvent<HTMLInputElement>) => {
    setEmail(e.target.value);
  };

  return (
    <div className="min-h-screen flex flex-col items-center justify-center bg-white px-4 py-12 font-gmarket">
      {/* 타이틀/부제목 */}
      <div className="w-full max-w-md text-center mb-10">
        <h1 className="text-5xl font-black text-gray-900 tracking-tight flex items-center justify-center gap-2" style={{ fontFamily: "'BMDOHYEON', sans-serif" }}>
          <span style={{ fontFamily: "'Cafe24Supermagic-Bold-v1.0', cursive", fontSize: '1.3em', verticalAlign: 'middle' }}>“</span>
          두리
          <span style={{ fontFamily: "'Cafe24Supermagic-Bold-v1.0', cursive", fontSize: '1.3em', verticalAlign: 'middle' }}>”</span>
        </h1>
        <p className="mt-1 text-2xl text-gray-900 iceJaram-Rg-important">
          추억도 별점도, 둘이
        </p>
      </div>

      {/* 본문 */}
      <div className="w-full max-w-md">
        <h2 className="text-2xl font-bold text-gray-900 text-center mb-2">
          비밀번호 찾기
        </h2>
        <p className="text-sm text-gray-500 text-center mb-8">
          가입한 이메일 주소를 입력하시면 비밀번호 재설정 링크를 보내드립니다.
        </p>

        {!isSubmitted ? (
          <form className="space-y-6" onSubmit={handleSubmit(onSubmit)}>
            <div>
              <input
                {...register('email')}
                type="email"
                placeholder="이메일"
                className="w-full px-3 py-3 border-0 border-b-2 border-gray-200 placeholder-gray-400 text-gray-900 focus:outline-none focus:border-black text-lg bg-transparent font-medium transition"
                onChange={handleEmailChange}
              />
              {errors.email && (
                <p className="mt-2 text-sm text-red-500">{errors.email.message as string}</p>
              )}
            </div>

            <button
              type="submit"
              disabled={!email}
              className={`w-full px-6 py-3 text-white font-bold text-base rounded-l-xl rounded-r-xl transition ${
                email ? 'bg-black hover:bg-gray-800' : 'bg-gray-300 cursor-not-allowed'
              }`}
            >
              재설정 링크 받기
            </button>
          </form>
        ) : (
          <div className="bg-white border border-gray-200 rounded-2xl p-6 text-center">
            <div className="flex flex-col items-center">
              <svg className="w-12 h-12 text-green-500 mb-4" fill="none" stroke="currentColor" viewBox="0 0 24 24">
                <path strokeLinecap="round" strokeLinejoin="round" strokeWidth="2" d="M5 13l4 4L19 7" />
              </svg>
              <p className="text-base text-gray-700">
                비밀번호 재설정 링크가 이메일로 전송되었습니다.<br />
                이메일을 확인해주세요.
              </p>
            </div>
          </div>
        )}

        <div className="text-center mt-6">
          <Link href="/login" className="text-sm text-gray-500 hover:text-gray-700 transition">
            로그인으로 돌아가기
          </Link>
        </div>
      </div>
    </div>
  );
} 