'use client';

import { useEffect, useState } from 'react';
import { useInView } from 'react-intersection-observer';
import { PostResponse, PostSearchOptions } from '@/types/post';
import { postService } from '@/services/post';
import PostCard from './PostCard';

interface CommunityPostListProps {
  searchOptions: PostSearchOptions;
}

export default function CommunityPostList({ searchOptions }: CommunityPostListProps) {
  const [posts, setPosts] = useState<PostResponse[]>([]);
  const [hasMore, setHasMore] = useState(true);
  const [isLoading, setIsLoading] = useState(false);
  const [cursor, setCursor] = useState<{ date: string; rate: number | null; idToken: string } | undefined>();
  const { ref, inView } = useInView();

  const fetchPosts = async (isInitial: boolean = false) => {
    if (isLoading || (!hasMore && !isInitial)) return;

    setIsLoading(true);
    try {
      const response = await postService.getCompleteCommunityPosts(
        isInitial ? undefined : cursor,
        10,
        searchOptions
      );

      if (isInitial) {
        setPosts(response.items);
      } else {
        setPosts(prev => [...prev, ...response.items]);
      }

      setHasMore(response.hasNext);
      if (response.nextCursor) {
        setCursor({
          date: response.nextCursor.date,
          rate: response.nextCursor.rate,
          idToken: response.nextCursor.idToken
        });
      }
    } catch (error) {
      console.error('게시글 로딩 중 오류 발생:', error);
    } finally {
      setIsLoading(false);
    }
  };

  useEffect(() => {
    setPosts([]);
    setCursor(undefined);
    setHasMore(true);
    fetchPosts(true);
  }, [searchOptions]);

  useEffect(() => {
    if (inView && hasMore && !isLoading) {
      fetchPosts();
    }
  }, [inView, hasMore, isLoading]);

  if (posts.length === 0 && !isLoading) {
    return (
      <div className="flex justify-center items-center h-40 text-gray-500">
        게시글이 없습니다.
      </div>
    );
  }

  return (
    <div className="space-y-4">
      {posts.map((post) => (
        <PostCard key={post.idToken} post={post} />
      ))}
      {hasMore && (
        <div ref={ref} className="h-10 flex justify-center items-center">
          {isLoading && (
            <div className="animate-spin rounded-full h-6 w-6 border-b-2 border-gray-900"></div>
          )}
        </div>
      )}
    </div>
  );
} 