'use client';

import { usePathname } from "next/navigation";
import ProtectedRoute from "@/components/ProtectedRoute";
import Link from 'next/link';
import { useAuth } from '@/hooks/useAuth';
import { authService } from '@/services/auth';
import { toast } from 'react-hot-toast';
import { useEffect } from 'react';

const publicRoutes = [
  "/", 
  "/login", 
  "/join", 
  /^\/posts\/\d+$/, 
  "/posts/grid", 
  "/posts/list", 
  /^\/[^/]+$/, 
  /^\/[^/]+\/posts$/  // 사용자의 게시글 목록 페이지
];

export default function ClientLayout({
  children,
}: {
  children: React.ReactNode
}) {
  const pathname = usePathname();
  const { user, isAuthenticated, isLoading, initializeAuth } = useAuth();
  
  useEffect(() => {
    initializeAuth();
  }, [initializeAuth]);

  const isPublicRoute = publicRoutes.some((route) =>
    typeof route === "string" ? route === pathname : route.test(pathname)
  );

  const handleLogout = async () => {
    try {
      await authService.logout();
    } catch (error) {
      console.error('로그아웃 실패:', error);
      toast.error('로그아웃에 실패했습니다.');
    }
  };

  const content = (
    <div>
      <header className="bg-white shadow-sm">
        <nav className="max-w-7xl mx-auto px-4 sm:px-6 lg:px-8 h-16">
          <div className="h-full flex items-center justify-between">
            <Link href="/" className="flex items-center space-x-3">
              <span className="text-2xl font-bold bg-gradient-to-r from-indigo-600 to-blue-500 bg-clip-text text-transparent">
                Boilerplate
              </span>
            </Link>
            <div>
              {isLoading ? (
                <div className="animate-spin rounded-full h-6 w-6 border-t-2 border-b-2 border-indigo-500"></div>
              ) : isAuthenticated ? (
                <div className="flex items-center gap-6">
                  <span className="text-gray-700 font-medium">
                    환영합니다, <span className="text-indigo-600">{user?.name}</span>님
                  </span>
                  <Link
                    href={`/${user?.username}`}
                    className="text-gray-700 hover:text-indigo-600 transition-colors duration-200 font-medium"
                  >
                    프로필
                  </Link>
                  <button
                    onClick={handleLogout}
                    className="bg-white text-gray-700 px-4 py-2 rounded-lg border border-gray-300 hover:bg-gray-50 transition-colors duration-200 font-medium"
                  >
                    로그아웃
                  </button>
                </div>
              ) : (
                <div className="flex items-center gap-4">
                  <Link 
                    href="/login"
                    className="text-gray-700 font-medium hover:text-indigo-600 transition-colors duration-200"
                  >
                    로그인
                  </Link>
                  <Link 
                    href="/join"
                    className="bg-indigo-600 text-white px-4 py-2 rounded-lg hover:bg-indigo-700 transition-colors duration-200 font-medium"
                  >
                    회원가입
                  </Link>
                </div>
              )}
            </div>
          </div>
        </nav>
      </header>
      <main>{children}</main>
    </div>
  );

  return isPublicRoute ? content : <ProtectedRoute>{content}</ProtectedRoute>;
} 