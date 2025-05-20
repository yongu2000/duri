import { useEffect, useState } from 'react';
import { useRouter } from 'next/navigation';
import { useAuth } from '@/hooks/useAuth';
import { postService } from '@/services/post';

export default function PendingPostsAlert() {
  const { user } = useAuth();
  const router = useRouter();
  const [pendingCount, setPendingCount] = useState<number>(0);

  useEffect(() => {
    const fetchPendingCount = async () => {
      if (!user?.coupleCode) return;
      
      try {
        const response = await postService.getPendingPostsCount(user.coupleCode);
        setPendingCount(response.count);
      } catch (error) {
        console.error('미완성 게시물 수 조회 실패:', error);
      }
    };

    fetchPendingCount();
  }, [user?.coupleCode]);

  if (pendingCount === 0) return null;

  return (
    <div className="w-full flex justify-center bg-white">
      <div className="w-full max-w-md flex items-center px-4 my-2">
        <div
          className="flex-1 bg-gray-100 rounded-lg cursor-pointer hover:bg-gray-200 transition-colors p-2 text-center"
          onClick={() => router.push('/post/pending')}
        >
          <p className="text-gray-700 text-sm">
            미완성 게시물이 {pendingCount}개 있습니다
          </p>
        </div>
      </div>
    </div>
  );
} 