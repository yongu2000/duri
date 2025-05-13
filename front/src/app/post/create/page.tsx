'use client';

import { useState, useEffect, useRef } from 'react';
import { PostFormData } from '@/types/post';
import { FiUpload, FiArrowLeft, FiGlobe, FiLock, FiMapPin, FiX } from 'react-icons/fi';
import { FaStar, FaStarHalfAlt } from 'react-icons/fa';
import NavBar from '@/components/NavBar';
import { useRouter } from 'next/navigation';
import { searchService, SearchResult } from '@/services/search';
import { useDebounce } from '@/hooks/useDebounce';

export default function CreatePostPage() {
  const router = useRouter();
  const [formData, setFormData] = useState<PostFormData>({
    subject: '',
    title: '',
    date: '',
    rating: 0,
    comment: '',
    scope: 'PUBLIC',
  });
  const [imagePreview, setImagePreview] = useState<string | null>(null);
  const [hoverRating, setHoverRating] = useState<number>(0);
  const [searchResults, setSearchResults] = useState<SearchResult[]>([]);
  const [isSearching, setIsSearching] = useState(false);
  const [hasSelectedPlace, setHasSelectedPlace] = useState(false);
  const searchContainerRef = useRef<HTMLDivElement>(null);
  const debouncedSubject = useDebounce(formData.subject, 300);

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

  const handleSelectPlace = (placeName: string) => {
    setFormData(prev => ({ ...prev, subject: placeName }));
    setSearchResults([]);
    setIsSearching(false);
    setHasSelectedPlace(true);
  };

  const handleImageChange = (e: React.ChangeEvent<HTMLInputElement>) => {
    const file = e.target.files?.[0];
    if (file) {
      setFormData(prev => ({ ...prev, image: file }));
      const reader = new FileReader();
      reader.onloadend = () => {
        setImagePreview(reader.result as string);
      };
      reader.readAsDataURL(file);
    }
  };

  const handleSubmit = (e: React.FormEvent) => {
    e.preventDefault();
    // TODO: API 연동
    console.log(formData);
  };

  const renderStar = (index: number) => {
    const rating = hoverRating || formData.rating;
    const starValue = index * 2;
    const isHalfStar = rating >= starValue - 1 && rating < starValue;
    const isFullStar = rating >= starValue;

    return (
      <div className="relative w-8 h-8">
        <button
          type="button"
          onClick={() => setFormData(prev => ({ ...prev, rating: starValue - 2 }))}
          onMouseEnter={() => setHoverRating(starValue - 2)}
          onMouseLeave={() => setHoverRating(0)}
          className="absolute inset-0 w-1/3"
        />
        <button
          type="button"
          onClick={() => setFormData(prev => ({ ...prev, rating: starValue - 1 }))}
          onMouseEnter={() => setHoverRating(starValue - 1)}
          onMouseLeave={() => setHoverRating(0)}
          className="absolute inset-0 w-1/3 left-1/3"
        />
        <button
          type="button"
          onClick={() => setFormData(prev => ({ ...prev, rating: starValue }))}
          onMouseEnter={() => setHoverRating(starValue)}
          onMouseLeave={() => setHoverRating(0)}
          className="absolute inset-0 w-1/3 left-2/3"
        />
        {isHalfStar ? (
          <FaStarHalfAlt className="text-2xl text-yellow-400" />
        ) : isFullStar ? (
          <FaStar className="text-2xl text-yellow-400" />
        ) : (
          <FaStar className="text-2xl text-gray-300" />
        )}
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
              <h1 className="text-2xl font-bold">새 게시글 작성</h1>
            </div>
            
            <form onSubmit={handleSubmit} className="space-y-6">
              <div className="relative" ref={searchContainerRef}>
                <label className="block text-sm font-medium text-gray-700 mb-2">주제</label>
                <div className="relative">
                  <input
                    type="text"
                    value={formData.subject}
                    onChange={handleSubjectChange}
                    placeholder="장소를 검색해주세요"
                    className="w-full px-4 py-2 border rounded-lg focus:ring-2 focus:ring-blue-500 focus:border-blue-500"
                    required
                  />
                  {formData.subject && (
                    <button
                      type="button"
                      onClick={() => {
                        setFormData(prev => ({ ...prev, subject: '' }));
                        setSearchResults([]);
                      }}
                      className="absolute right-3 top-1/2 -translate-y-1/2 text-gray-400 hover:text-gray-600"
                    >
                      <FiX className="text-lg" />
                    </button>
                  )}
                </div>
                {!hasSelectedPlace && searchResults.length > 0 && (
                  <div className="absolute z-10 w-full mt-1 bg-white border rounded-lg shadow-lg">
                    {searchResults.map((result, index) => (
                      <button
                        key={index}
                        type="button"
                        onClick={() => handleSelectPlace(result.placeName)}
                        className="w-full px-4 py-2 text-left hover:bg-gray-50 flex items-center gap-2"
                      >
                        <FiMapPin className="text-gray-400" />
                        <div>
                          <div className="font-medium">{result.placeName}</div>
                          <div className="text-sm text-gray-500">{result.addressName}</div>
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
                  required
                />
              </div>

              <div>
                <label className="block text-sm font-medium text-gray-700 mb-2">날짜 (선택)</label>
                <input
                  type="date"
                  value={formData.date}
                  onChange={(e) => setFormData(prev => ({ ...prev, date: e.target.value }))}
                  className="w-full px-4 py-2 border rounded-lg focus:ring-2 focus:ring-blue-500 focus:border-blue-500"
                />
              </div>

              <div>
                <label className="block text-sm font-medium text-gray-700 mb-2">이미지</label>
                <div className="mt-1 flex justify-center px-6 pt-5 pb-6 border-2 border-gray-300 border-dashed rounded-lg">
                  <div className="space-y-1 text-center">
                    {imagePreview ? (
                      <img src={imagePreview} alt="Preview" className="mx-auto h-32 w-auto" />
                    ) : (
                      <FiUpload className="mx-auto h-12 w-12 text-gray-400" />
                    )}
                    <div className="flex text-sm text-gray-600">
                      <label className="relative cursor-pointer bg-white rounded-md font-medium text-blue-600 hover:text-blue-500 focus-within:outline-none">
                        <span>이미지 업로드</span>
                        <input
                          type="file"
                          className="sr-only"
                          accept="image/*"
                          onChange={handleImageChange}
                        />
                      </label>
                    </div>
                  </div>
                </div>
              </div>

              <div>
                <label className="block text-sm font-medium text-gray-700 mb-2">별점 (0-10)</label>
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

              <div>
                <label className="block text-sm font-medium text-gray-700 mb-2">한마디</label>
                <textarea
                  value={formData.comment}
                  onChange={(e) => setFormData(prev => ({ ...prev, comment: e.target.value }))}
                  className="w-full px-4 py-2 border rounded-lg focus:ring-2 focus:ring-blue-500 focus:border-blue-500"
                  rows={4}
                  required
                />
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
                  작성하기
                </button>
              </div>
            </form>
          </div>
        </div>
      </div>
    </div>
  );
}