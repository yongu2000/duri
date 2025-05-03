'use client';

import { usePathname } from "next/navigation";
import ProtectedRoute from "@/components/ProtectedRoute";
import Link from 'next/link';
import { useAuth } from '@/hooks/useAuth';
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


  const content = (
    <div>
      <header className="bg-white shadow-sm">
        
      </header>
      <main>{children}</main>
    </div>
  );

  return isPublicRoute ? content : <ProtectedRoute>{content}</ProtectedRoute>;
} 