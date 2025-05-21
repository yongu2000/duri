'use client';

import { useState, useEffect, useRef } from 'react';
import { PostFormData } from '@/types/post';
import { FiUpload, FiArrowLeft, FiGlobe, FiLock, FiMapPin, FiX } from 'react-icons/fi';
import { FaStar, FaStarHalfAlt } from 'react-icons/fa';
import NavBar from '@/components/NavBar';
import { useRouter, useSearchParams } from 'next/navigation';
import { searchService, SearchResult } from '@/services/search';
import { useDebounce } from '@/hooks/useDebounce';
import { postService } from '@/services/post';
import { imageService } from '@/services/image';
import { useAuth } from '@/hooks/useAuth';

export default function EditPostPage() {
  const router = useRouter();
  const searchParams = useSearchParams();
  const postId = searchParams.get('id');
  const { user } = useAuth();
  const isLeftUser = user?.position === 'LEFT';

  const [formData, setFormData] = useState<PostFormData>({
    subject: '',
    title: '',
    date: '',
    rating: 0,
    comment: '',
    scope: 'PUBLIC',
    imageUrls: [],
    placeUrl: '',
    category: '',
    address: '',
    roadAddress: '',
    phone: '',
    x: 0,
    y: 0
  });
  const [otherUserRating, setOtherUserRating] = useState<number>(0);
  const [otherUserComment, setOtherUserComment] = useState<string>('');
  const [imagePreviews, setImagePreviews] = useState<string[]>([]);
  const [hoverRating, setHoverRating] = useState<number>(0);
  const [searchResults, setSearchResults] = useState<SearchResult[]>([]);
  const [isSearching, setIsSearching] = useState(false);
  const [hasSelectedPlace, setHasSelectedPlace] = useState(false);
  const searchContainerRef = useRef<HTMLDivElement>(null);
  const debouncedSubject = useDebounce(formData.subject, 300);

  useEffect(() => {
    const fetchPost = async () => {
      if (!postId) {
        router.push('/');
        return;
      }

      try {
        const post = await postService.getPost(postId);
        setFormData({
          subject: post.placeName,
          title: post.title,
          date: post.date,
          rating: isLeftUser ? post.userLeftRate : post.userRightRate,
          comment: isLeftUser ? post.userLeftComment : post.userRightComment,
          scope: post.scope,
          imageUrls: [],
          placeUrl: '',
          category: post.category,
          address: post.address,
          roadAddress: '',
          phone: '',
          x: 0,
          y: 0
        });
        setOtherUserRating(isLeftUser ? post.userRightRate : post.userLeftRate);
        setOtherUserComment(isLeftUser ? post.userRightComment : post.userLeftComment);
        setHasSelectedPlace(true);

        // 게시글 정보를 먼저 설정한 후 이미지를 불러옵니다
        try {
          const images = await postService.getPostImages(postId);
          setFormData(prev => ({
            ...prev,
            imageUrls: images
          }));
          setImagePreviews(images);
        } catch (error) {
          console.error('이미지 로드 실패:', error);
          // 이미지 로드 실패는 전체 게시글 로드 실패로 처리하지 않습니다
        }
      } catch (error) {
        console.error('게시글 로딩 실패:', error);
        alert('게시글을 불러오는데 실패했습니다.');
        router.push('/');
      }
    };

    fetchPost();
  }, [postId, router, isLeftUser]);

  useEffect(() => {
    const handleClickOutside = (event: MouseEvent) => {
      if (searchContainerRef.current && !searchContainerRef.current.contains(event.target as Node)) {
        setSearchResults([]);
      }
    };

    document.addEventListener('mousedown', handleClickOutside);
    return () => document.removeEventListener('mousedown', handleClickOutside);
  }, []);

  useEffect(() => {
    const searchPlaces = async () => {
      if (!debouncedSubject.trim()) {
        setSearchResults([]);
        return;
      }

      setIsSearching(true);
      try {
        const results = await searchService.search(debouncedSubject);
        setSearchResults(results);
      } catch (error) {
        console.error('검색 중 오류 발생:', error);
        setSearchResults([]);
      } finally {
        setIsSearching(false);
      }
    };

    searchPlaces();
  }, [debouncedSubject]);

  const handleSubjectChange = (e: React.ChangeEvent<HTMLInputElement>) => {
    const value = e.target.value;
    setFormData(prev => ({ ...prev, subject: value }));
    setHasSelectedPlace(false);
  };

  const handleSelectPlace = (place: SearchResult) => {
    setFormData(prev => ({
      ...prev,
      subject: place.placeName,
      placeUrl: place.placeUrl,
      category: place.category,
      address: place.address,
      roadAddress: place.roadAddress,
      phone: place.phone,
      x: place.x,
      y: place.y
    }));
    setSearchResults([]);
    setIsSearching(false);
    setHasSelectedPlace(true);
  };

  const handleImageChange = async (e: React.ChangeEvent<HTMLInputElement>) => {
    const files = Array.from(e.target.files || []);
    if (files.length > 0) {
      const newFiles = files.slice(0, 5 - formData.imageUrls.length);
      
      for (const file of newFiles) {
        try {
          const imageUrl = await imageService.uploadImage(file);
          setFormData(prev => ({
            ...prev,
            imageUrls: [...prev.imageUrls, imageUrl]
          }));
          
          const reader = new FileReader();
          reader.onloadend = () => {
            setImagePreviews(prev => [...prev, reader.result as string]);
          };
          reader.readAsDataURL(file);
        } catch (error) {
          console.error('이미지 업로드 실패:', error);
          alert('이미지 업로드에 실패했습니다. 다시 시도해주세요.');
        }
      }
    }
  };

  const removeImage = async (index: number) => {
    try {
      await imageService.deleteImage(formData.imageUrls[index]);
      
      setFormData(prev => ({
        ...prev,
        imageUrls: prev.imageUrls.filter((_, i) => i !== index)
      }));
      setImagePreviews(prev => prev.filter((_, i) => i !== index));
    } catch (error) {
      console.error('이미지 삭제 실패:', error);
      alert('이미지 삭제에 실패했습니다. 다시 시도해주세요.');
    }
  };

  const handleSubmit = async (e: React.FormEvent) => {
    e.preventDefault();
    if (!hasSelectedPlace) {
      alert('장소를 검색하여 선택해주세요.');
      return;
    }

    try {
      const updateData = {
        ...formData,
        userLeftRate: isLeftUser ? formData.rating : undefined,
        userRightRate: !isLeftUser ? formData.rating : undefined,
        userLeftComment: isLeftUser ? formData.comment : undefined,
        userRightComment: !isLeftUser ? formData.comment : undefined,
      };
      await postService.updatePost(postId!, updateData);
      router.push('/'); 
    } catch (error) {
      console.error('게시글 수정 실패:', error);
      alert('게시글 수정에 실패했습니다. 다시 시도해주세요.');
    }
  };

  const renderStar = (index: number) => {
    const rating = hoverRating || formData.rating;
    const starValue = index * 2;
    const isHalfStar = rating >= starValue - 1 && rating < starValue;
    const isFullStar = rating >= starValue;

    return (
      <div className="relative w-8 h-8">
        <div className="relative w-8 h-8">
          <FaStar className="text-2xl text-gray-300" />
          {isHalfStar && (
            <div className="absolute inset-0" style={{ width: '37%', overflow: 'hidden' }}>
              <FaStar className="text-2xl text-yellow-400" style={{ position: 'absolute', left: 0 }} />
            </div>
          )}
          {isFullStar && (
            <div className="absolute inset-0">
              <FaStar className="text-2xl text-yellow-400" />
            </div>
          )}
        </div>
        <div className="absolute inset-0 flex">
          <button
            type="button"
            onClick={() => setFormData(prev => ({ ...prev, rating: starValue - 2 }))}
            onMouseEnter={() => setHoverRating(starValue - 2)}
            onMouseLeave={() => setHoverRating(0)}
            className="w-1/3 h-full"
          />
          <button
            type="button"
            onClick={() => setFormData(prev => ({ ...prev, rating: starValue - 1 }))}
            onMouseEnter={() => setHoverRating(starValue - 1)}
            onMouseLeave={() => setHoverRating(0)}
            className="w-1/3 h-full"
          />
          <button
            type="button"
            onClick={() => setFormData(prev => ({ ...prev, rating: starValue }))}
            onMouseEnter={() => setHoverRating(starValue)}
            onMouseLeave={() => setHoverRating(0)}
            className="w-1/3 h-full"
          />
        </div>
      </div>
    );
  };

  return (
    <div className="min-h-screen flex flex-col bg-white">
      <NavBar />
      <div className="flex-1 flex justify-center">
        <div className="w-full max-w-md px-4 py-6">
          <div className="bg-white rounded-xl shadow-lg p-6">
            <div className="flex items-center gap-4 mb-6">
              <button
                onClick={() => router.back()}
                className="p-2 hover:bg-gray-100 rounded-full transition-colors"
              >
                <FiArrowLeft className="text-2xl" />
              </button>
              <h1 className="text-2xl font-bold">게시글 수정</h1>
            </div>
            
            <form onSubmit={handleSubmit} className="space-y-6">
              <div className="relative" ref={searchContainerRef}>
                <label className="block text-sm font-medium text-gray-700 mb-2">
                  주제 <span className="text-red-500">*</span>
                </label>
                <div className="relative">
                  <input
                    type="text"
                    value={formData.subject}
                    onChange={handleSubjectChange}
                    placeholder="장소를 검색해주세요"
                    className={`w-full px-4 py-2 border rounded-lg focus:ring-2 focus:ring-blue-500 focus:border-blue-500 ${
                      !hasSelectedPlace && formData.subject ? 'border-red-500' : ''
                    }`}
                    required
                  />
                  {formData.subject && (
                    <button
                      type="button"
                      onClick={() => {
                        setFormData(prev => ({ ...prev, subject: '' }));
                        setSearchResults([]);
                        setHasSelectedPlace(false);
                      }}
                      className="absolute right-3 top-1/2 -translate-y-1/2 text-gray-400 hover:text-gray-600"
                    >
                      <FiX className="text-lg" />
                    </button>
                  )}
                </div>
                {!hasSelectedPlace && formData.subject && (
                  <p className="mt-1 text-sm text-red-500">
                    검색 결과에서 장소를 선택해주세요
                  </p>
                )}
                {!hasSelectedPlace && searchResults.length > 0 && (
                  <div className="absolute z-10 w-full mt-1 bg-white border rounded-lg shadow-lg">
                    {searchResults.map((result, index) => (
                      <button
                        key={index}
                        type="button"
                        onClick={() => handleSelectPlace(result)}
                        className="w-full px-4 py-2 text-left hover:bg-gray-50 flex items-center gap-2"
                      >
                        <FiMapPin className="text-gray-400" />
                        <div>
                          <div className="font-medium">{result.placeName}</div>
                          <div className="text-sm text-gray-500">{result.address}</div>
                        </div>
                      </button>
                    ))}
                  </div>
                )}
                {!hasSelectedPlace && isSearching && (
                  <div className="absolute right-3 top-1/2 -translate-y-1/2">
                    <div className="animate-spin rounded-full h-5 w-5 border-b-2 border-blue-500"></div>
                  </div>
                )}
                {!hasSelectedPlace && !isSearching && debouncedSubject && searchResults.length === 0 && (
                  <div className="absolute w-full mt-1 px-4 py-2 text-sm text-gray-500">
                    검색 결과가 없습니다
                  </div>
                )}
              </div>

              <div>
                <label className="block text-sm font-medium text-gray-700 mb-2">제목</label>
                <input
                  type="text"
                  value={formData.title}
                  onChange={(e) => setFormData(prev => ({ ...prev, title: e.target.value }))}
                  className="w-full px-4 py-2 border rounded-lg focus:ring-2 focus:ring-blue-500 focus:border-blue-500"
                />
              </div>

              <div>
                <label className="block text-sm font-medium text-gray-700 mb-2">날짜 (선택)</label>
                <input
                  type="date"
                  value={formData.date || ''}
                  onChange={(e) => setFormData(prev => ({ ...prev, date: e.target.value }))}
                  className="w-full rounded-lg border-0 bg-gray-50 py-2 text-gray-700 focus:ring-2 focus:ring-indigo-400"
                />
              </div>

              <div>
                <label className="block text-sm font-medium text-gray-700 mb-2">이미지 (최대 5개)</label>
                <div className="mt-1 flex flex-wrap gap-4">
                  {imagePreviews.map((preview, index) => (
                    <div key={index} className="relative">
                      <img
                        src={preview}
                        alt={`Preview ${index + 1}`}
                        className="h-32 w-32 object-cover rounded-lg"
                      />
                      <button
                        type="button"
                        onClick={() => removeImage(index)}
                        className="absolute -top-2 -right-2 bg-red-500 text-white rounded-full p-1 hover:bg-red-600"
                      >
                        <FiX className="w-4 h-4" />
                      </button>
                    </div>
                  ))}
                  {imagePreviews.length < 5 && (
                    <div className="h-32 w-32 border-2 border-gray-300 border-dashed rounded-lg flex items-center justify-center">
                      <div className="text-center">
                        <FiUpload className="mx-auto h-8 w-8 text-gray-400" />
                        <label className="mt-2 cursor-pointer text-sm text-blue-600 hover:text-blue-500">
                          <span>이미지 추가</span>
                          <input
                            type="file"
                            className="sr-only"
                            accept="image/*"
                            multiple
                            onChange={handleImageChange}
                          />
                        </label>
                      </div>
                    </div>
                  )}
                </div>
                <p className="mt-2 text-sm text-gray-500">
                  {imagePreviews.length}/5 이미지 업로드됨
                </p>
              </div>

              <div>
                <label className="block text-sm font-medium text-gray-700 mb-2">별점 (0-10)</label>
                <div className="space-y-4">
                  {/* 현재 유저의 별점 */}
                  <div>
                    <div className="flex items-center gap-2 mb-2">
                      <span className="text-sm font-medium">{isLeftUser ? '내 별점' : '내 별점'}</span>
                      <span className="text-sm text-gray-500">수정 가능</span>
                    </div>
                    <div className="flex items-center gap-1">
                      {[1, 2, 3, 4, 5].map((index) => (
                        <div key={index}>
                          {renderStar(index)}
                        </div>
                      ))}
                      <span className="ml-2 text-sm text-gray-500">
                        {formData.rating}점
                      </span>
                    </div>
                  </div>

                  {/* 상대방의 별점 */}
                  <div>
                    <div className="flex items-center gap-2 mb-2">
                      <span className="text-sm font-medium">{isLeftUser ? '상대방 별점' : '상대방 별점'}</span>
                      <span className="text-sm text-gray-500">수정 불가</span>
                    </div>
                    <div className="flex items-center gap-1">
                      {[1, 2, 3, 4, 5].map((index) => (
                        <div key={index} className="relative w-8 h-8">
                          <div className="relative w-8 h-8">
                            <FaStar className="text-2xl text-gray-300" />
                            {otherUserRating >= index * 2 - 1 && otherUserRating < index * 2 && (
                              <div className="absolute inset-0" style={{ width: '37%', overflow: 'hidden' }}>
                                <FaStar className="text-2xl text-yellow-400" style={{ position: 'absolute', left: 0 }} />
                              </div>
                            )}
                            {otherUserRating >= index * 2 && (
                              <div className="absolute inset-0">
                                <FaStar className="text-2xl text-yellow-400" />
                              </div>
                            )}
                          </div>
                        </div>
                      ))}
                      <span className="ml-2 text-sm text-gray-500">
                        {otherUserRating}점
                      </span>
                    </div>
                  </div>
                </div>
              </div>

              <div>
                <label className="block text-sm font-medium text-gray-700 mb-2">한마디</label>
                <div className="space-y-4">
                  {/* 현재 유저의 코멘트 */}
                  <div>
                    <div className="flex items-center gap-2 mb-2">
                      <span className="text-sm font-medium">{isLeftUser ? '내 평가' : '내 평가'}</span>
                      <span className="text-sm text-gray-500">수정 가능</span>
                    </div>
                    <textarea
                      value={formData.comment || ''}
                      onChange={(e) => setFormData(prev => ({ ...prev, comment: e.target.value }))}
                      className="w-full px-4 py-2 border rounded-lg focus:ring-2 focus:ring-blue-500 focus:border-blue-500"
                      rows={4}
                      required
                    />
                  </div>

                  {/* 상대방의 코멘트 */}
                  <div>
                    <div className="flex items-center gap-2 mb-2">
                      <span className="text-sm font-medium">{isLeftUser ? '상대방 평가' : '상대방 평가'}</span>
                      <span className="text-sm text-gray-500">수정 불가</span>
                    </div>
                    <div className="w-full px-4 py-2 border rounded-lg bg-gray-50">
                      <p className="text-gray-700">{otherUserComment}</p>
                    </div>
                  </div>
                </div>
              </div>

              <div className="flex items-center justify-between pt-4 border-t">
                <div className="flex items-center gap-2">
                  <button
                    type="button"
                    onClick={() => setFormData(prev => ({ 
                      ...prev, 
                      scope: prev.scope === 'PUBLIC' ? 'PRIVATE' : 'PUBLIC' 
                    }))}
                    className={`flex items-center gap-2 px-4 py-2 rounded-lg transition-colors ${
                      formData.scope === 'PUBLIC'
                        ? 'bg-blue-100 text-blue-600'
                        : 'bg-gray-100 text-gray-600'
                    }`}
                  >
                    {formData.scope === 'PUBLIC' ? (
                      <>
                        <FiGlobe className="text-lg" />
                        <span>공개</span>
                      </>
                    ) : (
                      <>
                        <FiLock className="text-lg" />
                        <span>비공개</span>
                      </>
                    )}
                  </button>
                </div>
                <button
                  type="submit"
                  className="px-6 py-2 bg-blue-600 text-white rounded-lg hover:bg-blue-700 focus:outline-none focus:ring-2 focus:ring-blue-500 focus:ring-offset-2"
                >
                  수정하기
                </button>
              </div>
            </form>
          </div>
        </div>
      </div>
    </div>
  );
} 