import { FiBell, FiMenu } from 'react-icons/fi';
import { FaUserCircle } from 'react-icons/fa';
import { useAuth } from '@/hooks/useAuth';
import { useState } from 'react';
import Image from 'next/image';
import Link from 'next/link';
import NotificationBell from './NotificationBell';

export default function NavBar() {
  const { user } = useAuth();
  const profileImageUrl = user?.profileImageUrl;
  const [imgError, setImgError] = useState(false);

  return (
    <nav className="w-full flex justify-center bg-white">
      <div className="w-full max-w-md flex items-center justify-between px-6 py-4">
        {/* 로고 */}
        <Link href="/">
          <Image src="/logo_duri.png" alt="두리 로고" width={80} height={32} priority />
        </Link>
        {/* 오른쪽 아이콘: 프로필, 알림, 메뉴 순서 */}
        <div className="flex items-center gap-4">
          <Link href="/profile" className="block">
            {profileImageUrl && !imgError ? (
              <div className="w-9 h-9 rounded-full overflow-hidden bg-gray-100 relative">
                <Image
                  src={profileImageUrl}
                  alt="프로필"
                  fill
                  unoptimized={true}
                  className="object-cover"
                  onError={() => setImgError(true)}
                />
              </div>
            ) : (
              <FaUserCircle className="w-9 h-9 text-gray-300" />
            )}
          </Link>
          <NotificationBell />
          <button className="text-2xl text-gray-400 hover:text-indigo-500">
            <FiMenu />
          </button>
        </div>
      </div>
    </nav>
  );
} 