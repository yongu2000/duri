import { useState, useEffect } from 'react';
import type { PostResponse } from '@/types/post';
import { FiChevronLeft, FiChevronRight, FiHeart, FiMessageCircle, FiEdit2 } from 'react-icons/fi';
import { FaStar, FaMars, FaVenus, FaUserCircle } from 'react-icons/fa';
import { useSwipeable } from 'react-swipeable';
import { postService } from '@/services/post';
import { useAuth } from '@/hooks/useAuth';
import { useRouter } from 'next/navigation';

interface PostCardProps {
  post: PostResponse;
}

export default function PostCard({ post }: PostCardProps) {
  const router = useRouter();
  const { user } = useAuth();
  const [currentImageIndex, setCurrentImageIndex] = useState(0);
  const [isSwiping, setIsSwiping] = useState(false);
  const [swipeDelta, setSwipeDelta] = useState(0);
  const [images, setImages] = useState<string[]>([]);
  const [isLoadingImages, setIsLoadingImages] = useState(true);

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
            {canEdit && (
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
        <div className="flex items-center gap-4 text-gray-500">
          <div className="flex items-center gap-1">
            <FiHeart className="w-4 h-4" />
            <span className="text-xs">{post.likeCount || 0}</span>
          </div>
          <div className="flex items-center gap-1">
            <FiMessageCircle className="w-4 h-4" />
            <span className="text-xs">{post.commentCount || 0}</span>
          </div>
        </div>
      </div>
    </div>
  );
} 