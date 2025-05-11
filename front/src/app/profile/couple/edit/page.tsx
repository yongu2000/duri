"use client";

import { useEffect, useState } from "react";
import { useRouter } from "next/navigation";
import { useAuth } from '@/hooks/useAuth';
import { coupleService, CoupleEditProfileResponse } from '@/services/couple';
import { toast } from 'react-hot-toast';
import { FaCheckCircle, FaTimesCircle } from 'react-icons/fa';
import Link from 'next/link';
import { authService } from '@/services/auth';

export default function CoupleEditProfilePage() {
  const { user, setAuth } = useAuth();
  const coupleCode = user?.coupleCode;
  const router = useRouter();
  const [form, setForm] = useState<CoupleEditProfileResponse | null>(null);
  const [loading, setLoading] = useState(true);
  const [submitting, setSubmitting] = useState(false);
  const [codeCheckMessage, setCodeCheckMessage] = useState<string | null>(null);
  const [isCodeAvailable, setIsCodeAvailable] = useState<boolean | null>(null);
  const [isCodeChecked, setIsCodeChecked] = useState(false);
  const [initialCoupleCode, setInitialCoupleCode] = useState<string | null>(null);
  const [initialForm, setInitialForm] = useState<CoupleEditProfileResponse | null>(null);

  useEffect(() => {
    if (!coupleCode) return;
    const fetchProfile = async () => {
      setLoading(true);
      try {
        const data = await coupleService.getCoupleEditProfile(coupleCode);
        setForm(data);
        setInitialForm(data);
        setInitialCoupleCode(data.coupleCode);
      } catch (e) {
        toast.error('프로필 정보를 불러올 수 없습니다.');
        router.push('/profile');
      } finally {
        setLoading(false);
      }
    };
    fetchProfile();
  }, [coupleCode, router]);

  const handleChange = (e: React.ChangeEvent<HTMLInputElement | HTMLTextAreaElement>) => {
    if (!form) return;
    setForm({ ...form, [e.target.name]: e.target.value });
  };

  const handleCodeChange = (e: React.ChangeEvent<HTMLInputElement>) => {
    if (!form) return;
    setForm({ ...form, coupleCode: e.target.value });
    setIsCodeChecked(false);
    setCodeCheckMessage(null);
    setIsCodeAvailable(null);
  };

  const handleCheckCodeDuplicate = async () => {
    if (!form?.coupleCode || form.coupleCode.length < 3) {
      setCodeCheckMessage('커플 아이디는 3자 이상이어야 합니다.');
      setIsCodeAvailable(false);
      setIsCodeChecked(false);
      return;
    }
    try {
      const isDuplicate = await coupleService.checkCoupleCodeDuplicate(form.coupleCode);
      if (isDuplicate) {
        setCodeCheckMessage('이미 사용 중인 커플 아이디입니다.');
        setIsCodeAvailable(false);
        setIsCodeChecked(false);
        return;
      }
      setCodeCheckMessage('사용 가능한 커플 아이디입니다.');
      setIsCodeAvailable(true);
      setIsCodeChecked(true);
    } catch (e) {
      setCodeCheckMessage('중복 확인에 실패했습니다.');
      setIsCodeAvailable(false);
      setIsCodeChecked(false);
    }
  };

  const handleSubmit = async (e: React.FormEvent) => {
    e.preventDefault();
    if (!form || !coupleCode) return;
    if (isCodeChanged && !isCodeChecked) {
      setCodeCheckMessage('커플 아이디 중복확인을 해주세요.');
      setIsCodeAvailable(false);
      return;
    }
    setSubmitting(true);
    try {
      await coupleService.updateCoupleProfile(coupleCode, {
        coupleName: form.coupleName,
        bio: form.bio,
        coupleCode: form.coupleCode,
      });
      if (form.coupleCode !== initialCoupleCode) {
        const newUser = await authService.getUserInfo();
        setAuth(newUser);
      }
      toast.success('커플 프로필이 변경되었습니다!');
      router.push('/profile');
    } catch (e) {
      toast.error('수정에 실패했습니다.');
    } finally {
      setSubmitting(false);
    }
  };

  const isCodeChanged = form && form.coupleCode !== initialCoupleCode;
  const isFormChanged = form && initialForm && (
    form.coupleName !== initialForm.coupleName ||
    form.coupleCode !== initialForm.coupleCode ||
    form.bio !== initialForm.bio
  );

  if (loading || !form) {
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
        <form onSubmit={handleSubmit} className="w-full flex flex-col gap-6">
          <h2 className="text-2xl font-extrabold text-gray-900 mb-2 text-center">커플 프로필 수정</h2>
          <div>
            <label className="block text-gray-700 text-sm mb-1">커플 이름</label>
            <input
              name="coupleName"
              value={form.coupleName}
              onChange={handleChange}
              className="w-full px-0 py-3 border-0 border-b-2 border-gray-200 text-gray-900 focus:outline-none focus:border-black bg-transparent"
              maxLength={30}
              required
            />
          </div>
          <div>
            <label className="block text-gray-700 text-sm mb-1">커플 아이디</label>
            <div className="flex gap-2 items-center">
              <input
                name="coupleCode"
                value={form.coupleCode}
                onChange={handleCodeChange}
                className="w-full px-0 py-3 border-0 border-b-2 border-gray-200 text-gray-900 focus:outline-none focus:border-black bg-transparent"
                maxLength={20}
                required
              />
              <button
                type="button"
                onClick={handleCheckCodeDuplicate}
                disabled={form.coupleCode.length < 3}
                className="min-w-[90px] px-4 py-2 text-sm font-medium text-white bg-black rounded-xl hover:bg-gray-800 disabled:bg-gray-300"
              >
                중복 확인
              </button>
              {isCodeChecked && isCodeAvailable && <FaCheckCircle className="text-green-500 ml-1" />}
              {isCodeChecked && !isCodeAvailable && <FaTimesCircle className="text-red-500 ml-1" />}
            </div>
            {codeCheckMessage && (
              <p className={`mt-1 text-sm ${isCodeAvailable ? 'text-green-600' : 'text-red-600'}`}>{codeCheckMessage}</p>
            )}
          </div>
          <div>
            <label className="block text-gray-700 text-sm mb-1">커플 소개</label>
            <textarea
              name="bio"
              value={form.bio || ''}
              onChange={handleChange}
              className="w-full px-4 py-3 border-b-2 border-gray-200 text-gray-900 focus:outline-none focus:border-black rounded-md min-h-[80px]"
              maxLength={200}
              placeholder="커플 소개를 입력하세요"
            />
          </div>
          <button
            type="submit"
            disabled={submitting || (!!isCodeChanged && !isCodeChecked) || !isFormChanged}
            className="w-full py-3 rounded-xl bg-indigo-500 hover:bg-indigo-600 text-white font-bold text-base disabled:bg-gray-300"
          >
            {submitting ? '저장 중...' : '저장하기'}
          </button>
        </form>
      </div>
    </div>
  );
} 