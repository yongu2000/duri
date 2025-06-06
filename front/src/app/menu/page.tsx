"use client";

import { FiHome, FiUsers, FiHeart } from 'react-icons/fi';
import Link from 'next/link';
import { useRouter } from 'next/navigation';

export default function MenuPage() {
  const router = useRouter();

  return (
    <div className="min-h-screen flex flex-col items-center justify-center bg-gray-50">
      <div className="w-full max-w-md h-screen bg-white flex flex-col items-center p-8 relative">
        {/* X 버튼 */}
        <button 
          onClick={() => router.back()}
          className="absolute top-4 right-4 w-8 h-8 flex items-center justify-center text-gray-500 hover:text-gray-700"
        >
          <svg xmlns="http://www.w3.org/2000/svg" fill="none" viewBox="0 0 24 24" strokeWidth={2} stroke="currentColor" className="w-6 h-6">
            <path strokeLinecap="round" strokeLinejoin="round" d="M6 18L18 6M6 6l12 12" />
          </svg>
        </button>

        {/* 헤더 */}
        <div className="w-full mb-8">
          <h1 className="text-2xl font-bold text-gray-900">메뉴</h1>
        </div>

        {/* 메뉴 버튼들 */}
        <div className="w-full grid grid-cols-3 gap-4">
          <Link 
            href="/"
            className="aspect-square flex flex-col items-center justify-center gap-2 rounded-xl bg-gray-100 hover:bg-gray-200 text-gray-700 font-bold text-base"
          >
            <FiHome className="w-8 h-8" />
            <span className="text-sm">홈</span>
          </Link>

          <Link 
            href="/community"
            className="aspect-square flex flex-col items-center justify-center gap-2 rounded-xl bg-gray-100 hover:bg-gray-200 text-gray-700 font-bold text-base"
          >
            <FiUsers className="w-8 h-8" />
            <span className="text-sm">커뮤니티</span>
          </Link>

          <Link 
            href="/like/post"
            className="aspect-square flex flex-col items-center justify-center gap-2 rounded-xl bg-gray-100 hover:bg-gray-200 text-gray-700 font-bold text-base"
          >
            <FiHeart className="w-8 h-8" />
            <span className="text-sm">좋아요한 글</span>
          </Link>
        </div>
      </div>
    </div>
  );
} 