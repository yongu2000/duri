import { useState, useEffect } from 'react';
import type { PostResponse, PostSearchOptions } from '@/types/post';
import PostCardComponent from './PostCard';
import { postService } from '@/services/post';
import { useAuth } from '@/hooks/useAuth';

interface PostListProps {
  searchOptions: PostSearchOptions;
}

export default function PostList({ searchOptions }: PostListProps) {
  const { user } = useAuth();
  const [posts, setPosts] = useState<PostResponse[]>([]);
  const [hasNext, setHasNext] = useState(false);
  const [nextCursor, setNextCursor] = useState<{ date: string; rate: number | null; idToken: string } | null>(null);
  const [isLoading, setIsLoading] = useState(false);

  const loadPosts = async (cursor?: { date: string; rate: number | null; idToken: string }) => {
    try {
      setIsLoading(true);
      if (!user?.coupleCode) {
        setPosts([]);
        return;
      }
      const response = await postService.getCompletePosts(user.coupleCode, cursor, 10, searchOptions);
      setPosts(prev => cursor ? [...prev, ...response.items] : response.items);
      setHasNext(response.hasNext);
      setNextCursor(response.nextCursor || null);
    } catch (error) {
      console.error('게시글 로딩 실패:', error);
      setPosts([]);
    } finally {
      setIsLoading(false);
    }
  };

  useEffect(() => {
    if (user?.coupleCode) {
      loadPosts();
    } else {
      setPosts([]);
    }
    // eslint-disable-next-line react-hooks/exhaustive-deps
  }, [searchOptions, user?.coupleCode]);

  const handleLoadMore = () => {
    if (nextCursor && !isLoading) {
      loadPosts(nextCursor);
    }
  };

  const hasPosts = Array.isArray(posts) && posts.length > 0;

  return (
    <div className="w-full flex flex-col items-center">
      <div className="w-full flex flex-col items-center justify-center py-8">
        {isLoading && !hasPosts ? (
          <div className="flex flex-col items-center justify-center py-16">
            <div className="animate-spin rounded-full h-8 w-8 border-b-2 border-blue-500 mb-4"></div>
            <div className="text-center text-base text-gray-500">게시글을 불러오는 중입니다</div>
          </div>
        ) : hasPosts ? (
          <div className="w-full max-w-sm space-y-6">
            {posts.map(post => (
              <PostCardComponent key={post.idToken} post={post} />
            ))}
            {hasNext && (
              <button
                onClick={handleLoadMore}
                disabled={isLoading}
                className="w-full py-2 text-center text-blue-600 hover:text-blue-700 disabled:text-gray-400"
              >
                {isLoading ? '로딩 중...' : '더 보기'}
              </button>
            )}
          </div>
        ) : (
          <div className="flex flex-col items-center justify-center py-16">
            <div className="text-center text-lg text-gray-700 mb-1">아직 리뷰가 없습니다</div>
            <div className="text-center text-base text-gray-500">두사람의 추억을 시작해보세요</div>
          </div>
        )}
      </div>
    </div>
  );
} 