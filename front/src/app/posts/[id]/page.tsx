'use client';

import { useEffect, useState } from 'react';
import { useParams, useRouter } from 'next/navigation';
import { useAuth } from '@/hooks/useAuth';
import { postService } from '@/services/post';
import { Post, CommentResponse, CommentRepliesResponse } from '@/types/post';
import { toast } from 'react-hot-toast';
import Link from 'next/link';
import CommentComponent from '@/components/post/Comment';
import PostHeader from '@/components/post/PostHeader';
import Image from 'next/image';
import DOMPurify from 'dompurify';
import 'ckeditor5/ckeditor5.css';
import '../new/editor.css';

export default function PostDetailPage() {
  const params = useParams();
  const router = useRouter();
  const { user, isAuthenticated } = useAuth();
  const [post, setPost] = useState<Post | null>(null);
  const [comments, setComments] = useState<CommentResponse[]>([]);
  const [isLoading, setIsLoading] = useState(true);
  const [commentContent, setCommentContent] = useState('');
  const [isLiked, setIsLiked] = useState(false);
  const [likeCount, setLikeCount] = useState(0);

  useEffect(() => {
    const fetchData = async () => {
      try {
        const postId = Number(params.id);
        const [postResponse, commentsResponse] = await Promise.all([
          postService.getPost(postId),
          postService.getComments(postId)
        ]);
        setPost(postResponse);
        setComments(commentsResponse);
      } catch (error) {
        console.error('데이터 로드 실패:', error);
        toast.error('데이터를 불러오는데 실패했습니다.');
        router.push('/');
      } finally {
        setIsLoading(false);
      }
    };

    fetchData();
  }, [params.id, router]);

  useEffect(() => {
    const fetchLikeStatus = async () => {
      if (post && isAuthenticated) {
        const liked = await postService.getLikeStatus(post.id);
        setIsLiked(liked);
      }
    };

    if (post) {
      setLikeCount(post.likes);
      fetchLikeStatus();
    }
  }, [post, isAuthenticated]);

  const handleDelete = async () => {
    if (!post || !window.confirm('정말로 이 글을 삭제하시겠습니까?')) return;

    try {
      await postService.deletePost(post.id);
      toast.success('글이 삭제되었습니다.');
      router.push('/');
    } catch (error) {
      console.error('글 삭제 실패:', error);
      toast.error('글 삭제에 실패했습니다.');
    }
  };

  const handleLikeClick = async () => {
    if (!isAuthenticated) {
        toast.error('좋아요를 하려면 로그인이 필요합니다.');
        router.push('/login');
        return;
    }

    // 즉각적인 UI 업데이트
    setIsLiked(prev => !prev);
    setLikeCount(prev => prev + (isLiked ? -1 : 1));

    try {
        if (isLiked) {
            await postService.dislikePost(post!.id);
        } else {
            await postService.likePost(post!.id);
        }
        toast.success(isLiked ? '좋아요가 취소되었습니다.' : '좋아요를 눌렀습니다.');
    } catch (error) {
        // 실패 시 상태 롤백
        setIsLiked(prev => !prev);
        setLikeCount(prev => prev + (isLiked ? 1 : -1));
        console.error('좋아요 처리 실패:', error);
        toast.error('좋아요 처리에 실패했습니다.');
    }
  };

  // 댓글 작성 핸들러
  const handleCommentSubmit = async (e: React.FormEvent) => {
    e.preventDefault();
    
    if (!commentContent.trim()) {
      toast.error('댓글 내용을 입력해주세요.');
      return;
    }

    try {
      await postService.createComment(post!.id, {
        content: commentContent.trim(),
        parentCommentId: null
      });
      
      // 댓글 작성 후 댓글 목록 새로 불러오기
      const response = await postService.getComments(post!.id);
      setComments(response);
      setCommentContent(''); // 입력창 초기화
      toast.success('댓글이 작성되었습니다.');
    } catch (error) {
      console.error('댓글 작성 실패:', error);
      toast.error('댓글 작성에 실패했습니다.');
    }
  };

  // 댓글 삭제 핸들러
  const handleCommentDelete = async (commentId: number) => {
    if (!window.confirm('정말로 이 댓글을 삭제하시겠습니까?')) return;

    try {
      await postService.deleteComment(commentId);
      
      // 댓글 삭제 후 댓글 목록 새로 불러오기
      const response = await postService.getComments(post!.id);
      setComments(response);
      toast.success('댓글이 삭제되었습니다.');
    } catch (error) {
      console.error('댓글 삭제 실패:', error);
      toast.error('댓글 삭제에 실패했습니다.');
    }
  };

  // 대댓글 작성 핸들러
  const handleReplySubmit = async (parentCommentId: number, content: string) => {
    try {
      await postService.createComment(post!.id, {
        content: content.trim(),
        parentCommentId: parentCommentId
      });
      
      // 대댓글 작성 후 댓글 목록 새로 불러오기
      const response = await postService.getComments(post!.id);
      setComments(response);
      toast.success('답글이 작성되었습니다.');
    } catch (error) {
      console.error('답글 작성 실패:', error);
      toast.error('답글 작성에 실패했습니다.');
    }
  };

  // 댓글 수정 후 댓글 목록 새로 불러오기를 위한 핸들러
  const handleCommentUpdate = async (commentId: number) => {
    try {
      const response = await postService.getComments(post!.id);
      setComments(response);
    } catch (error) {
      console.error('댓글 목록 새로고침 실패:', error);
      toast.error('최신 댓글을 불러오는데 실패했습니다.');
    }
  };

  if (isLoading) {
    return (
      <div className="min-h-screen flex items-center justify-center">
        <div className="animate-spin rounded-full h-12 w-12 border-t-2 border-b-2 border-indigo-600"></div>
      </div>
    );
  }

  if (!post) {
    return (
      <div className="min-h-screen flex items-center justify-center">
        <div className="text-center">
          <h1 className="text-2xl font-bold text-gray-900 mb-4">글을 찾을 수 없습니다</h1>
          <Link href="/" className="text-indigo-600 hover:text-indigo-500">
            메인 페이지로 돌아가기
          </Link>
        </div>
      </div>
    );
  }

  return (
    <div className="min-h-screen bg-gray-50 py-12">
      <div className="max-w-4xl mx-auto px-4 sm:px-6 lg:px-8">
        <div className="mb-6">
          <button
            onClick={() => router.back()}
            className="flex items-center text-gray-600 hover:text-indigo-600 transition-colors duration-200"
          >
            <svg className="w-5 h-5 mr-2" fill="none" stroke="currentColor" viewBox="0 0 24 24">
              <path strokeLinecap="round" strokeLinejoin="round" strokeWidth={2} d="M10 19l-7-7m0 0l7-7m-7 7h18" />
            </svg>
            이전으로
          </button>
        </div>

        <div className="bg-white rounded-xl shadow-lg overflow-hidden">
          <div className="p-8">
            <PostHeader 
              post={post}
              isLiked={isLiked}
              likeCount={likeCount}
              onLikeClick={handleLikeClick}
              onDelete={handleDelete}
              isAuthor={user?.id === post.user.id}
            />

            <div className="prose max-w-none ck-content">
              <div dangerouslySetInnerHTML={{ __html: DOMPurify.sanitize(post.content) }} />
            </div>

            <div className="mt-12">
              <h2 className="text-2xl font-bold text-gray-900 mb-6">댓글</h2>
              {comments.length > 0 ? (
                <div className="space-y-6">
                  {comments
                    .filter(comment => comment.parentCommentId === null) // 최상위 댓글만 필터링
                    .map((comment) => (
                      <CommentComponent
                        key={comment.id}
                        comment={comment}
                        postId={post.id}
                        onDelete={handleCommentDelete}
                        onReply={handleReplySubmit}
                        onUpdate={handleCommentUpdate}
                      />
                    ))}
                </div>
              ) : (
                <p className="text-gray-500">아직 댓글이 없습니다.</p>
              )}

              <form onSubmit={handleCommentSubmit} className="mt-6">
                <textarea
                  rows={3}
                  value={commentContent}
                  onChange={(e) => setCommentContent(e.target.value)}
                  placeholder={isAuthenticated ? "댓글을 작성해주세요" : "댓글을 작성하려면 로그인이 필요합니다"}
                  className="w-full rounded-lg border-gray-300 shadow-sm focus:border-indigo-500 focus:ring-indigo-500"
                  disabled={!isAuthenticated}
                />
                <div className="mt-2 flex justify-end">
                  {isAuthenticated ? (
                    <button
                      type="submit"
                      className="px-4 py-2 rounded-lg bg-indigo-600 text-white hover:bg-indigo-700"
                    >
                      댓글 작성
                    </button>
                  ) : (
                    <button
                      type="button"
                      onClick={() => router.push('/login')}
                      className="px-4 py-2 rounded-lg bg-gray-100 text-gray-700 hover:bg-gray-200"
                    >
                      로그인하고 댓글 작성
                    </button>
                  )}
                </div>
              </form>
            </div>
          </div>
        </div>
      </div>
    </div>
  );
} 
 