'use client';

import { useEffect } from 'react';
import Link from 'next/link';

export default function Error({
  error,
  reset,
}: {
  error: Error & { digest?: string };
  reset: () => void;
}) {
  useEffect(() => {
    console.error(error);
  }, [error]);

  return (
    <div className="min-h-screen bg-gray-50 flex items-center justify-center py-12">
      <div className="max-w-xl w-full px-4">
        <div className="bg-white rounded-xl shadow-lg p-8 text-center">
          <div className="mb-6">
            <svg
              className="mx-auto h-24 w-24 text-red-400"
              fill="none"
              viewBox="0 0 24 24"
              stroke="currentColor"
            >
              <path
                strokeLinecap="round"
                strokeLinejoin="round"
                strokeWidth="2"
                d="M12 9v2m0 4h.01m-6.938 4h13.856c1.54 0 2.502-1.667 1.732-3L13.732 4c-.77-1.333-2.694-1.333-3.464 0L3.34 16c-.77 1.333.192 3 1.732 3z"
              />
            </svg>
          </div>
          <h1 className="text-3xl font-bold text-gray-900 mb-4">
            오류가 발생했습니다
          </h1>
          <p className="text-gray-600 mb-6">
            죄송합니다. 문제가 발생했습니다. 다시 시도해주세요.
          </p>
          <div className="flex justify-center space-x-4">
            <button
              onClick={reset}
              className="inline-flex items-center px-4 py-2 border border-transparent text-base font-medium rounded-md shadow-sm text-white bg-indigo-600 hover:bg-indigo-700"
            >
              다시 시도
            </button>
            <Link
              href="/"
              className="inline-flex items-center px-4 py-2 border border-transparent text-base font-medium rounded-md shadow-sm text-gray-700 bg-gray-100 hover:bg-gray-200"
            >
              메인으로
            </Link>
          </div>
        </div>
      </div>
    </div>
  );
}
