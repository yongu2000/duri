import { useState, useEffect } from 'react';
import type { PostResponse } from '@/types/post';
import { FiChevronLeft, FiChevronRight, FiHeart, FiMessageCircle, FiEdit2, FiTrash2, FiX, FiCornerDownRight } from 'react-icons/fi';
import { FaStar, FaMars, FaVenus, FaUserCircle } from 'react-icons/fa';
import { useSwipeable } from 'react-swipeable';
import { postService } from '@/services/post';
import { commentService, type Comment, type CommentReply } from '@/services/comment';
import { useAuth } from '@/hooks/useAuth';
import { useRouter } from 'next/navigation';

interface PostCardProps {
  post: PostResponse;
  showInteractionInfo?: boolean;
}

export default function PostCard({ post, showInteractionInfo = false }: PostCardProps) {
  const router = useRouter();
  const { user } = useAuth();
  const [currentImageIndex, setCurrentImageIndex] = useState(0);
  const [isSwiping, setIsSwiping] = useState(false);
  const [swipeDelta, setSwipeDelta] = useState(0);
  const [images, setImages] = useState<string[]>([]);
  const [isLoadingImages, setIsLoadingImages] = useState(true);
  const [isLiked, setIsLiked] = useState(false);
  const [likeCount, setLikeCount] = useState(post.likeCount || 0);
  const [showComments, setShowComments] = useState(false);
  const [comments, setComments] = useState<Comment[]>([]);
  const [newComment, setNewComment] = useState('');
  const [editingCommentId, setEditingCommentId] = useState<string | null>(null);
  const [editContent, setEditContent] = useState('');
  const [isLoadingComments, setIsLoadingComments] = useState(false);
  const [showReplies, setShowReplies] = useState<{ [key: string]: boolean }>({});
  const [showReplyForm, setShowReplyForm] = useState<{ [key: string]: boolean }>({});
  const [replies, setReplies] = useState<{ [key: string]: CommentReply[] }>({});
  const [newReply, setNewReply] = useState<{ [key: string]: string }>({});
  const [isLoadingReplies, setIsLoadingReplies] = useState<{ [key: string]: boolean }>({});

  const canEdit = user?.coupleCode === post.coupleCode;

  useEffect(() => {
    const loadImages = async () => {
      try {
        const imageUrls = await postService.getPostImages(post.idToken);
        setImages(imageUrls);
      } catch (error) {
        console.error('이미지 로드 실패:', error);
      } finally {
        setIsLoadingImages(false);
      }
    };

    loadImages();
  }, [post.idToken]);

  useEffect(() => {
    const checkLikeStatus = async () => {
      try {
        const { liked } = await postService.getLikeStatus(post.idToken);
        setIsLiked(liked);
      } catch (error) {
        console.error('좋아요 상태 확인 실패:', error);
      }
    };

    checkLikeStatus();
  }, [post.idToken]);

  const getPrevImageIndex = (currentIndex: number) => {
    return currentIndex === 0 ? images.length - 1 : currentIndex - 1;
  };

  const getNextImageIndex = (currentIndex: number) => {
    return currentIndex === images.length - 1 ? 0 : currentIndex + 1;
  };

  const handlePrevImage = () => {
    setCurrentImageIndex(getPrevImageIndex(currentImageIndex));
  };

  const handleNextImage = () => {
    setCurrentImageIndex(getNextImageIndex(currentImageIndex));
  };

  const swipeHandlers = useSwipeable({
    onSwiping: (e) => {
      setIsSwiping(true);
      setSwipeDelta(e.deltaX);
    },
    onSwipedLeft: () => {
      setIsSwiping(false);
      setSwipeDelta(0);
      handleNextImage();
    },
    onSwipedRight: () => {
      setIsSwiping(false);
      setSwipeDelta(0);
      handlePrevImage();
    },
    onSwiped: () => {
      setIsSwiping(false);
      setSwipeDelta(0);
    },
    trackMouse: true
  });

  const calculateAge = (birthday: string) => {
    const birthDate = new Date(birthday);
    const today = new Date();
    let age = today.getFullYear() - birthDate.getFullYear();
    const monthDiff = today.getMonth() - birthDate.getMonth();
    
    if (monthDiff < 0 || (monthDiff === 0 && today.getDate() < birthDate.getDate())) {
      age--;
    }
    
    return age;
  };

  const handleLikeClick = async () => {
    try {
      if (isLiked) {
        await postService.dislikePost(post.idToken);
        setLikeCount(prev => prev - 1);
      } else {
        await postService.likePost(post.idToken);
        setLikeCount(prev => prev + 1);
      }
      setIsLiked(!isLiked);
    } catch (error) {
      console.error('좋아요 처리 실패:', error);
    }
  };

  const handleCommentClick = async () => {
    if (!showComments) {
      setIsLoadingComments(true);
      try {
        const data = await commentService.getPostComments(post.idToken);
        setComments(data.map(comment => ({
          ...comment,
          commentCount: comment.commentCount || 0
        })));
        setShowReplies({});
        setShowReplyForm({});
        setReplies({});
        setNewReply({});
        setIsLoadingReplies({});
      } catch (error) {
        console.error('댓글 로드 실패:', error);
      } finally {
        setIsLoadingComments(false);
      }
    }
    setShowComments(!showComments);
  };

  const handleCommentSubmit = async (e: React.FormEvent) => {
    e.preventDefault();
    if (!newComment.trim()) return;

    try {
      await commentService.createComment(post.idToken, { content: newComment });
      const updatedComments = await commentService.getPostComments(post.idToken);
      setComments(updatedComments);
      setNewComment('');
      post.commentCount = (post.commentCount || 0) + 1;
    } catch (error) {
      console.error('댓글 작성 실패:', error);
    }
  };

  const handleCommentEdit = async (commentId: string) => {
    if (!editContent.trim()) return;

    try {
      await commentService.updateComment(commentId, { content: editContent });
      const updatedComments = await commentService.getPostComments(post.idToken);
      setComments(updatedComments);
      setEditingCommentId(null);
      setEditContent('');
    } catch (error) {
      console.error('댓글 수정 실패:', error);
    }
  };

  const handleCommentDelete = async (commentId: string) => {
    try {
      await commentService.deleteComment(commentId);
      const updatedComments = await commentService.getPostComments(post.idToken);
      setComments(updatedComments);
      post.commentCount = Math.max((post.commentCount || 0) - 1, 0);
    } catch (error) {
      console.error('댓글 삭제 실패:', error);
    }
  };

  const handleReplyClick = async (commentId: string) => {
    if (!showReplies[commentId]) {
      setIsLoadingReplies(prev => ({ ...prev, [commentId]: true }));
      try {
        const data = await commentService.getCommentReplies(commentId);
        setReplies(prev => ({ ...prev, [commentId]: data }));
      } catch (error) {
        console.error('대댓글 로드 실패:', error);
      } finally {
        setIsLoadingReplies(prev => ({ ...prev, [commentId]: false }));
      }
    }
    setShowReplies(prev => ({ ...prev, [commentId]: !prev[commentId] }));
  };

  const handleReplySubmit = async (e: React.FormEvent, commentId: string, replyTo?: string) => {
    e.preventDefault();
    const content = newReply[commentId];
    if (!content.trim()) return;

    try {
      // 답글 작성 API 호출
      await commentService.createReply(commentId, { content });
      setNewReply(prev => ({ ...prev, [commentId]: '' }));
      setShowReplyForm(prev => ({ ...prev, [commentId]: false }));

      // 답글 목록 새로고침
      const updatedReplies = await commentService.getCommentReplies(commentId);
      if (updatedReplies.length > 0) {
        // 첫 번째 답글의 parentCommentIdToken을 사용하여 최상위 부모 댓글의 답글 목록 새로고침
        const parentCommentId = updatedReplies[0].parentCommentIdToken;
        const parentReplies = await commentService.getCommentReplies(parentCommentId);
        setReplies(prev => ({ ...prev, [parentCommentId]: parentReplies }));
      }

      // 댓글 목록 새로고침 (답글 수 업데이트)
      const updatedComments = await commentService.getPostComments(post.idToken);
      setComments(updatedComments.map(comment => ({
        ...comment,
        commentCount: comment.commentCount || 0
      })));

      // 게시글의 commentCount 업데이트
      post.commentCount = (post.commentCount || 0) + 1;

      // 답글 목록이 보이도록 설정
      setShowReplies(prev => ({ ...prev, [commentId]: true }));
    } catch (error) {
      console.error('답글 작성 실패:', error);
      alert('답글 작성에 실패했습니다.');
    }
  };

  const handleReplyFormToggle = (commentId: string) => {
    setShowReplyForm(prev => ({ ...prev, [commentId]: !prev[commentId] }));
  };

  const formatDate = (dateString: string) => {
    const date = new Date(dateString);
    return date.toLocaleDateString('ko-KR', {
      year: 'numeric',
      month: 'long',
      day: 'numeric',
      hour: '2-digit',
      minute: '2-digit',
    });
  };

  return (
    <div className="bg-white rounded-lg shadow-md overflow-hidden max-w-sm mx-auto">
      {/* 장소, 제목, 날짜 */}
      <div className="p-3">
        <div className="flex items-center justify-between mb-1">
          <div>
            <h3 className="text-base font-semibold text-gray-800">{post.placeName}</h3>
            <p className="text-xs text-gray-500 mt-0.5">{post.address}</p>
          </div>
          <div className="flex items-center gap-2">
            <span className="text-sm text-gray-500">{post.coupleName}</span>
            {canEdit && post.coupleCode === user?.coupleCode && (
              <button
                onClick={() => router.push(`/post/edit?id=${encodeURIComponent(post.idToken)}`)}
                className="p-1.5 text-gray-600 hover:text-blue-600 hover:bg-blue-50 rounded-full transition-colors"
                title="게시글 수정"
              >
                <FiEdit2 className="w-4 h-4" />
              </button>
            )}
          </div>
        </div>
        <h2 className="text-lg font-bold text-gray-900 mt-1">{post.title}</h2>
        <p className="text-xs text-gray-500 mt-1">{post.date}</p>
      </div>

      {/* 이미지 슬라이더 */}
      {isLoadingImages ? (
        <div className="flex justify-center items-center h-64 bg-gray-100">
          <div className="animate-spin rounded-full h-8 w-8 border-b-2 border-blue-500"></div>
        </div>
      ) : images.length > 0 ? (
        <div className="relative overflow-hidden" {...swipeHandlers}>
          <div
            className="flex transition-transform duration-300 ease-out"
            style={{
              transform: `translateX(calc(-${currentImageIndex * 100}% + ${swipeDelta}px))`,
              transition: isSwiping ? 'none' : 'transform 300ms ease-out'
            }}
          >
            {images.map((image, index) => (
              <div
                key={index}
                className="w-full flex-shrink-0"
                style={{ width: '100%' }}
              >
                <img
                  src={image}
                  alt={`Post image ${index + 1}`}
                  className="w-full h-64 object-cover"
                />
              </div>
            ))}
          </div>
          {images.length > 1 && (
            <>
              <button
                onClick={handlePrevImage}
                className="absolute left-2 top-1/2 -translate-y-1/2 bg-black/30 text-white p-1.5 rounded-full hover:bg-black/50"
              >
                <FiChevronLeft className="w-4 h-4" />
              </button>
              <button
                onClick={handleNextImage}
                className="absolute right-2 top-1/2 -translate-y-1/2 bg-black/30 text-white p-1.5 rounded-full hover:bg-black/50"
              >
                <FiChevronRight className="w-4 h-4" />
              </button>
              <div className="absolute bottom-2 left-1/2 -translate-x-1/2 flex gap-1">
                {images.map((_, index) => (
                  <div
                    key={index}
                    className={`w-1.5 h-1.5 rounded-full ${
                      index === currentImageIndex ? 'bg-white' : 'bg-white/50'
                    }`}
                  />
                ))}
              </div>
            </>
          )}
        </div>
      ) : (
        <div className="flex justify-center items-center h-64 bg-gray-100">
          <p className="text-gray-500">이미지가 없습니다</p>
        </div>
      )}

      {/* 평점과 코멘트 */}
      <div className="p-3 space-y-3">
        <div className="flex items-start gap-3">
          <div className="flex-1">
            <div className="flex items-center gap-2 mb-1.5">
              {post.userLeftProfileImageUrl ? (
                <img
                  src={post.userLeftProfileImageUrl}
                  alt="Left user profile"
                  className="w-6 h-6 rounded-full object-cover"
                />
              ) : (
                <FaUserCircle className="w-9 h-9 text-gray-300" />
              )}
              <div className="flex items-center gap-1">
                {post.userLeftGender === 'MALE' ? (
                  <FaMars className="text-blue-500 w-3 h-3" />
                ) : (
                  <FaVenus className="text-pink-500 w-3 h-3" />
                )}
                <span className="text-xs text-gray-600">{post.userLeftName}, {calculateAge(post.userLeftBirthday)}세</span>
              </div>
            </div>
            <div className="flex items-center gap-1">
              <FaStar className="text-yellow-400 w-3 h-3" />
              <span className="text-sm font-medium">{post.userLeftRate}</span>
            </div>
            <p className="text-xs text-gray-600 mt-1">{post.userLeftComment}</p>
          </div>
          <div className="flex-1">
            <div className="flex items-center gap-2 mb-1.5">
              {post.userRightProfileImageUrl ? (
                <img
                  src={post.userRightProfileImageUrl}
                  alt="Right user profile"
                  className="w-6 h-6 rounded-full object-cover"
                />
              ) : (
                <FaUserCircle className="w-9 h-9 text-gray-300" />
              )}
              <div className="flex items-center gap-1">
                {post.userRightGender === 'MALE' ? (
                  <FaMars className="text-blue-500 w-3 h-3" />
                ) : (
                  <FaVenus className="text-pink-500 w-3 h-3" />
                )}
                <span className="text-xs text-gray-600">{post.userRightName}, {calculateAge(post.userRightBirthday)}세</span>
              </div>
            </div>
            <div className="flex items-center gap-1">
              <FaStar className="text-yellow-400 w-3 h-3" />
              <span className="text-sm font-medium">{post.userRightRate}</span>
            </div>
            <p className="text-xs text-gray-600 mt-1">{post.userRightComment}</p>
          </div>
        </div>

        {/* 해시태그 */}
        <div className="flex flex-wrap gap-1">
          {post.hashtags?.map((tag, index) => (
            <span
              key={index}
              className="text-xs text-blue-500 bg-blue-50 px-2 py-0.5 rounded-full"
            >
              #{tag}
            </span>
          ))}
        </div>

        {/* 좋아요, 댓글 수 */}
        {showInteractionInfo && (
          <div className="flex items-center gap-4 text-gray-500">
            <button
              onClick={handleLikeClick}
              className={`flex items-center gap-1 transition-colors ${
                isLiked ? 'text-red-500' : 'text-gray-500 hover:text-red-500'
              }`}
            >
              <FiHeart className={`w-4 h-4 ${isLiked ? 'fill-current stroke-current' : ''}`} />
              <span className="text-xs">{likeCount}</span>
            </button>
            <button
              onClick={handleCommentClick}
              className="flex items-center gap-1 hover:text-blue-500 transition-colors"
            >
              <FiMessageCircle className="w-4 h-4" />
              <span className="text-xs">{post.commentCount || 0}</span>
            </button>
          </div>
        )}

        {/* 댓글 섹션 */}
        {showComments && (
          <div className="mt-4 border-t pt-4">
            {/* 댓글 입력 폼 */}
            <form onSubmit={handleCommentSubmit} className="mb-4">
              <div className="flex gap-2">
                <input
                  type="text"
                  value={newComment}
                  onChange={(e) => setNewComment(e.target.value)}
                  placeholder="댓글을 입력하세요..."
                  className="flex-1 px-3 py-2 border rounded-lg focus:outline-none focus:ring-2 focus:ring-blue-500"
                />
                <button
                  type="submit"
                  className="px-4 py-2 bg-blue-500 text-white rounded-lg hover:bg-blue-600 transition-colors"
                >
                  작성
                </button>
              </div>
            </form>

            {/* 댓글 목록 */}
            {isLoadingComments ? (
              <div className="flex justify-center py-4">
                <div className="animate-spin rounded-full h-6 w-6 border-b-2 border-blue-500"></div>
              </div>
            ) : (
              <div className="space-y-4">
                {comments.map((comment) => (
                  <div key={comment.commentIdToken} className="bg-gray-50 rounded-lg p-3">
                    {editingCommentId === comment.commentIdToken ? (
                      <div className="flex gap-2">
                        <input
                          type="text"
                          value={editContent}
                          onChange={(e) => setEditContent(e.target.value)}
                          className="flex-1 px-3 py-2 border rounded-lg focus:outline-none focus:ring-2 focus:ring-blue-500"
                        />
                        <button
                          onClick={() => handleCommentEdit(comment.commentIdToken)}
                          className="px-3 py-2 bg-blue-500 text-white rounded-lg hover:bg-blue-600 transition-colors"
                        >
                          저장
                        </button>
                        <button
                          onClick={() => {
                            setEditingCommentId(null);
                            setEditContent('');
                          }}
                          className="px-3 py-2 bg-gray-500 text-white rounded-lg hover:bg-gray-600 transition-colors"
                        >
                          취소
                        </button>
                      </div>
                    ) : (
                      <>
                        <div className="flex justify-between items-start">
                          <div>
                            <span className="font-medium text-sm">{comment.author}</span>
                            <p className="text-sm mt-1">{comment.content}</p>
                            <span className="text-xs text-gray-500 mt-1 block">
                              {formatDate(comment.createdAt)}
                            </span>
                          </div>
                          {canEdit && (
                            <div className="flex gap-2">
                              <button
                                onClick={() => {
                                  setEditingCommentId(comment.commentIdToken);
                                  setEditContent(comment.content);
                                }}
                                className="text-gray-500 hover:text-blue-500 transition-colors"
                              >
                                <FiEdit2 className="w-4 h-4" />
                              </button>
                              <button
                                onClick={() => handleCommentDelete(comment.commentIdToken)}
                                className="text-gray-500 hover:text-red-500 transition-colors"
                              >
                                <FiTrash2 className="w-4 h-4" />
                              </button>
                            </div>
                          )}
                        </div>

                        {/* 답글 섹션 */}
                        <div className="mt-2 space-y-2">
                          {/* 답글 작성 버튼 */}
                          <button
                            onClick={() => handleReplyFormToggle(comment.commentIdToken)}
                            className="flex items-center gap-1 text-xs text-gray-500 hover:text-blue-500 transition-colors"
                          >
                            <FiCornerDownRight className="w-3 h-3" />
                            <span>답글쓰기</span>
                          </button>

                          {/* 답글 입력 폼 */}
                          {showReplyForm[comment.commentIdToken] && (
                            <div className="mt-2 pl-4">
                              <form onSubmit={(e) => handleReplySubmit(e, comment.commentIdToken, comment.author)} className="mb-2">
                                <div className="flex gap-2">
                                  <input
                                    type="text"
                                    value={newReply[comment.commentIdToken] || ''}
                                    onChange={(e) => setNewReply(prev => ({ ...prev, [comment.commentIdToken]: e.target.value }))}
                                    placeholder={`${comment.author}님에게 답글 작성...`}
                                    className="flex-1 px-3 py-1.5 text-sm border rounded-lg focus:outline-none focus:ring-2 focus:ring-blue-500"
                                  />
                                  <button
                                    type="submit"
                                    className="px-3 py-1.5 text-sm bg-blue-500 text-white rounded-lg hover:bg-blue-600 transition-colors"
                                  >
                                    작성
                                  </button>
                                </div>
                              </form>
                            </div>
                          )}

                          {/* 답글 목록 표시 버튼 */}
                          {(comment.commentCount || 0) > 0 && (
                            <button
                              onClick={() => handleReplyClick(comment.commentIdToken)}
                              className="flex items-center gap-1 text-xs text-gray-500 hover:text-blue-500 transition-colors"
                            >
                              <FiCornerDownRight className="w-3 h-3" />
                              <span>답글 {comment.commentCount}개 보기</span>
                            </button>
                          )}

                          {/* 답글 목록 */}
                          {showReplies[comment.commentIdToken] && (
                            <div className="pl-4 border-l-2 border-gray-200">
                              {isLoadingReplies[comment.commentIdToken] ? (
                                <div className="flex justify-center py-2">
                                  <div className="animate-spin rounded-full h-4 w-4 border-b-2 border-blue-500"></div>
                                </div>
                              ) : (
                                <div className="space-y-2">
                                  {replies[comment.commentIdToken]?.map((reply) => (
                                    <div key={reply.commentIdToken} className="bg-white rounded-lg p-2">
                                      <div className="flex items-center gap-1">
                                        <span className="font-medium text-xs">{reply.author}</span>
                                        <span className="text-xs text-gray-500">→</span>
                                        <span className="text-xs text-gray-500">{reply.replyTo}</span>
                                      </div>
                                      <p className="text-xs mt-1">{reply.content}</p>
                                      <div className="flex justify-between items-center mt-1">
                                        <span className="text-xs text-gray-500">
                                          {formatDate(reply.createdAt)}
                                        </span>
                                        <button
                                          onClick={() => handleReplyFormToggle(reply.commentIdToken)}
                                          className="flex items-center gap-1 text-xs text-gray-500 hover:text-blue-500 transition-colors"
                                        >
                                          <FiCornerDownRight className="w-3 h-3" />
                                          <span>답글쓰기</span>
                                        </button>
                                      </div>

                                      {/* 답글의 답글 입력 폼 */}
                                      {showReplyForm[reply.commentIdToken] && (
                                        <div className="mt-2 pl-4">
                                          <form onSubmit={(e) => handleReplySubmit(e, reply.commentIdToken, reply.author)} className="mb-2">
                                            <div className="flex gap-2">
                                              <input
                                                type="text"
                                                value={newReply[reply.commentIdToken] || ''}
                                                onChange={(e) => setNewReply(prev => ({ ...prev, [reply.commentIdToken]: e.target.value }))}
                                                placeholder={`${reply.author}님에게 답글 작성...`}
                                                className="flex-1 px-3 py-1.5 text-sm border rounded-lg focus:outline-none focus:ring-2 focus:ring-blue-500"
                                              />
                                              <button
                                                type="submit"
                                                className="px-3 py-1.5 text-sm bg-blue-500 text-white rounded-lg hover:bg-blue-600 transition-colors"
                                              >
                                                작성
                                              </button>
                                            </div>
                                          </form>
                                        </div>
                                      )}

                                      {/* 답글의 답글 목록 */}
                                      {showReplies[reply.commentIdToken] && (
                                        <div className="mt-2 pl-4 border-l-2 border-gray-200">
                                          {isLoadingReplies[reply.commentIdToken] ? (
                                            <div className="flex justify-center py-2">
                                              <div className="animate-spin rounded-full h-4 w-4 border-b-2 border-blue-500"></div>
                                            </div>
                                          ) : (
                                            <div className="space-y-2">
                                              {replies[reply.commentIdToken]?.map((nestedReply) => (
                                                <div key={nestedReply.commentIdToken} className="bg-white rounded-lg p-2">
                                                  <div className="flex items-center gap-1">
                                                    <span className="font-medium text-xs">{nestedReply.author}</span>
                                                    <span className="text-xs text-gray-500">→</span>
                                                    <span className="text-xs text-gray-500">{nestedReply.replyTo}</span>
                                                  </div>
                                                  <p className="text-xs mt-1">{nestedReply.content}</p>
                                                  <span className="text-xs text-gray-500 mt-1 block">
                                                    {formatDate(nestedReply.createdAt)}
                                                  </span>
                                                </div>
                                              ))}
                                            </div>
                                          )}
                                        </div>
                                      )}
                                    </div>
                                  ))}
                                </div>
                              )}
                            </div>
                          )}
                        </div>
                      </>
                    )}
                  </div>
                ))}
              </div>
            )}
          </div>
        )}
      </div>
    </div>
  );
} 