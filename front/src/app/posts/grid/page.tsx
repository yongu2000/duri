'use client';

import { useState, useEffect } from 'react';
import Link from 'next/link';
import { postService } from '@/services/post';
import { PostSummary, PostSearchParams, PostSearchType, PostSortBy, PostSortDirection } from '@/types/post';
import { toast } from 'react-hot-toast';
import { format, subDays, subMonths, subYears, startOfDay } from 'date-fns';
import { ko } from 'date-fns/locale';

const PAGE_SIZE_OPTIONS = [10, 20, 30, 50];
const MIN_COUNT_OPTIONS = [0, 10, 50, 100, 500];
const DATE_RANGE_OPTIONS = [
    { value: 'all', label: '전체 기간' },
    { value: 'week', label: '최근 1주' },
    { value: 'month', label: '최근 1달' },
    { value: 'year', label: '최근 1년' },
    { value: 'custom', label: '날짜선택' }
] as const;

export default function GridPostsPage() {
    const [posts, setPosts] = useState<PostSummary[]>([]);
    const [nextCursor, setNextCursor] = useState<number | null>(null);
    const [hasMore, setHasMore] = useState(true);
    const [isLoading, setIsLoading] = useState(false);
    const [searchParams, setSearchParams] = useState<PostSearchParams>({
        page: 0,
        size: 10,
        sortBy: 'date',
        sortDirection: 'desc'
    });
    const [searchType, setSearchType] = useState<'title' | 'content' | 'author'>('title');
    const [searchKeyword, setSearchKeyword] = useState('');
    const [isAdvancedSearchOpen, setIsAdvancedSearchOpen] = useState(false);
    const [startDate, setStartDate] = useState('');
    const [endDate, setEndDate] = useState('');
    const [dateRange, setDateRange] = useState<typeof DATE_RANGE_OPTIONS[number]['value']>('all');
    const [customMinViewCounts, setCustomMinViewCounts] = useState('');
    const [customMinCommentCounts, setCustomMinCommentCounts] = useState('');
    const [customMinLikes, setCustomMinLikes] = useState('');

    // 컴포넌트가 마운트될 때 한 번만 실행
    useEffect(() => {
        fetchPosts();
    }, []);

    const fetchPosts = async (cursor?: number) => {
        if (isLoading) return;
        
        try {
            setIsLoading(true);
            let calculatedStartDate: string | undefined;
            let calculatedEndDate: string | undefined;

            if (dateRange !== 'all') {
                calculatedEndDate = new Date().toISOString().split('T')[0];
                
                const today = startOfDay(new Date());
                let startDateValue: Date;
                
                switch (dateRange) {
                    case 'week':
                        startDateValue = subDays(today, 7);
                        break;
                    case 'month':
                        startDateValue = subMonths(today, 1);
                        break;
                    case 'year':
                        startDateValue = subYears(today, 1);
                        break;
                    case 'custom':
                        calculatedStartDate = startDate || undefined;
                        calculatedEndDate = endDate || undefined;
                        break;
                }
                
                if (dateRange !== 'custom') {
                    calculatedStartDate = startDateValue!.toISOString().split('T')[0];
                }
            }

            const params = {
                ...searchParams,
                searchType,
                searchKeyword,
                startDate: calculatedStartDate,
                endDate: calculatedEndDate,
                minViewCounts: customMinViewCounts ? Number(customMinViewCounts) : searchParams.minViewCounts,
                minCommentCounts: customMinCommentCounts ? Number(customMinCommentCounts) : searchParams.minCommentCounts,
                minLikes: customMinLikes ? Number(customMinLikes) : searchParams.minLikes
            };

            const response = await postService.getAllPostsByCursor(cursor, params);
            
            if (cursor) {
                setPosts(prev => [...prev, ...response.items]);
            } else {
                setPosts(response.items);
            }
            
            setNextCursor(response.nextCursor);
            setHasMore(response.hasNext);
        } catch (error) {
            console.error('글 목록 로드 실패:', error);
            toast.error('글 목록을 불러오는데 실패했습니다.');
        } finally {
            setIsLoading(false);
        }
    };

    const handleSearch = (e: React.FormEvent) => {
        e.preventDefault();
        setPosts([]);
        setNextCursor(null);
        setHasMore(true);
        fetchPosts();
    };

    const handleSortChange = (sortBy: 'date' | 'likes' | 'comments' | 'views', sortDirection: 'asc' | 'desc') => {
        setSearchParams(prev => ({
            ...prev,
            sortBy,
            sortDirection
        }));
        setPosts([]);
        setNextCursor(null);
        setHasMore(true);
        fetchPosts();
    };

    const handleFilterChange = (key: keyof PostSearchParams, value: number) => {
        setSearchParams(prev => ({
            ...prev,
            [key]: value
        }));
        setPosts([]);
        setNextCursor(null);
        setHasMore(true);
        fetchPosts();
    };

    const handleDateRangeChange = (value: typeof DATE_RANGE_OPTIONS[number]['value']) => {
        setDateRange(value);
        if (value !== 'custom') {
            setStartDate('');
            setEndDate('');
        }
        setPosts([]);
        setNextCursor(null);
        setHasMore(true);
        fetchPosts();
    };

    const handleResetFilters = () => {
        setSearchParams({
            page: 0,
            size: 10,
            sortBy: 'date',
            sortDirection: 'desc'
        });
        setDateRange('all');
        setStartDate('');
        setEndDate('');
        setCustomMinViewCounts('');
        setCustomMinCommentCounts('');
        setCustomMinLikes('');
        setPosts([]);
        setNextCursor(null);
        setHasMore(true);
        fetchPosts();
    };

    return (
        <main className="max-w-7xl mx-auto px-4 sm:px-6 lg:px-8 py-12">
            {/* 검색 영역 */}
            <div className="mb-6 bg-white rounded-lg">
                <div className="p-5">
                    <form onSubmit={handleSearch} className="flex gap-3">
                        <select
                            value={searchType}
                            onChange={(e) => setSearchType(e.target.value as typeof searchType)}
                            className="w-28 rounded-lg border-0 bg-gray-50 py-2.5 text-gray-700 focus:ring-2 focus:ring-indigo-400"
                        >
                            <option value="title">제목</option>
                            <option value="content">내용</option>
                            <option value="author">작성자</option>
                        </select>
                        <div className="relative flex-1">
                            <input
                                type="text"
                                value={searchKeyword}
                                onChange={(e) => setSearchKeyword(e.target.value)}
                                placeholder="검색어를 입력하세요"
                                className="w-full rounded-lg border-0 bg-gray-50 py-2.5 pr-10 text-gray-700 focus:ring-2 focus:ring-indigo-400"
                            />
                            <button
                                type="submit"
                                className="absolute right-2 top-1/2 -translate-y-1/2 rounded-md p-2 text-gray-400 hover:text-gray-600"
                            >
                                <svg className="h-5 w-5" fill="none" stroke="currentColor" viewBox="0 0 24 24">
                                    <path strokeLinecap="round" strokeLinejoin="round" strokeWidth={2} d="M21 21l-6-6m2-5a7 7 0 11-14 0 7 7 0 0114 0z" />
                                </svg>
                            </button>
                        </div>
                    </form>
                </div>
                <div className="border-t border-gray-100">
                    <div className="flex justify-end px-5">
                        <button
                            onClick={() => setIsAdvancedSearchOpen(!isAdvancedSearchOpen)}
                            className="flex items-center gap-1 py-3 text-sm text-gray-500 hover:text-gray-700 transition-colors"
                        >
                            <span className="font-medium">상세검색</span>
                            <svg
                                className={`w-5 h-5 transform transition-transform ${isAdvancedSearchOpen ? 'rotate-180' : ''}`}
                                fill="none"
                                stroke="currentColor"
                                viewBox="0 0 24 24"
                            >
                                <path strokeLinecap="round" strokeLinejoin="round" strokeWidth={2} d="M19 9l-7 7-7-7" />
                            </svg>
                        </button>
                    </div>
                </div>

                {/* 필터 및 정렬 영역 */}
                <div className={`border-t border-gray-100 ${isAdvancedSearchOpen ? '' : 'hidden'}`}>
                    <div className="p-5 space-y-4">
                        <div className="flex justify-end">
                            <button
                                onClick={handleResetFilters}
                                className="inline-flex items-center px-3 py-1.5 text-sm text-gray-600 hover:text-gray-900 transition-colors"
                            >
                                <svg className="w-4 h-4 mr-1" fill="none" stroke="currentColor" viewBox="0 0 24 24">
                                    <path strokeLinecap="round" strokeLinejoin="round" strokeWidth={2} d="M4 4v5h.582m15.356 2A8.001 8.001 0 004.582 9m0 0H9m11 11v-5h-.581m0 0a8.003 8.003 0 01-15.357-2m15.357 2H15" />
                                </svg>
                                초기화
                            </button>
                        </div>
                        <div className="grid grid-cols-1 sm:grid-cols-2 lg:grid-cols-3 gap-4">
                            <div className="space-y-1.5">
                                <label className="block text-sm font-medium text-gray-700">검색기간</label>
                                <select
                                    value={dateRange}
                                    onChange={(e) => handleDateRangeChange(e.target.value as typeof dateRange)}
                                    className="w-full rounded-lg border-0 bg-gray-50 py-2 text-gray-700 focus:ring-2 focus:ring-indigo-400"
                                >
                                    {DATE_RANGE_OPTIONS.map(option => (
                                        <option key={option.value} value={option.value}>
                                            {option.label}
                                        </option>
                                    ))}
                                </select>
                                {dateRange === 'custom' && (
                                    <div className="flex gap-2 mt-2">
                                        <input
                                            type="date"
                                            value={startDate}
                                            onChange={(e) => setStartDate(e.target.value)}
                                            className="flex-1 rounded-lg border-0 bg-gray-50 py-1.5 text-sm text-gray-700 focus:ring-2 focus:ring-indigo-400"
                                        />
                                        <span className="flex items-center text-gray-400">~</span>
                                        <input
                                            type="date"
                                            value={endDate}
                                            onChange={(e) => setEndDate(e.target.value)}
                                            className="flex-1 rounded-lg border-0 bg-gray-50 py-1.5 text-sm text-gray-700 focus:ring-2 focus:ring-indigo-400"
                                        />
                                    </div>
                                )}
                            </div>

                            <div className="space-y-1.5">
                                <label className="block text-sm font-medium text-gray-700">정렬</label>
                                <select
                                    value={`${searchParams.sortBy}-${searchParams.sortDirection}`}
                                    onChange={(e) => {
                                        const [sortBy, sortDirection] = e.target.value.split('-');
                                        handleSortChange(
                                            sortBy as 'date' | 'likes' | 'comments' | 'views',
                                            sortDirection as 'asc' | 'desc'
                                        );
                                    }}
                                    className="w-full rounded-lg border-0 bg-gray-50 py-2 text-gray-700 focus:ring-2 focus:ring-indigo-400"
                                >
                                    <option value="date-desc">최신순</option>
                                    <option value="date-asc">오래된순</option>
                                    <option value="views-desc">조회수 많은순</option>
                                    <option value="views-asc">조회수 적은순</option>
                                    <option value="likes-desc">좋아요 많은순</option>
                                    <option value="likes-asc">좋아요 적은순</option>
                                    <option value="comments-desc">댓글 많은순</option>
                                    <option value="comments-asc">댓글 적은순</option>
                                </select>
                            </div>

                            <div className="space-y-1.5">
                                <label className="block text-sm font-medium text-gray-700">페이지당 글</label>
                                <select
                                    value={searchParams.size}
                                    onChange={(e) => {
                                        setSearchParams(prev => ({
                                            ...prev,
                                            size: Number(e.target.value)
                                        }));
                                        setNextCursor(null);
                                        setHasMore(true);
                                    }}
                                    className="w-full rounded-lg border-0 bg-gray-50 py-2 text-gray-700 focus:ring-2 focus:ring-indigo-400"
                                >
                                    {PAGE_SIZE_OPTIONS.map(size => (
                                        <option key={size} value={size}>
                                            {size}개
                                        </option>
                                    ))}
                                </select>
                            </div>

                            <div className="space-y-1.5">
                                <label className="block text-sm font-medium text-gray-700">조회수</label>
                                <div className="flex gap-2">
                                    <select
                                        value={customMinViewCounts ? 'custom' : (searchParams.minViewCounts || 0)}
                                        onChange={(e) => {
                                            if (e.target.value === 'custom') {
                                                setCustomMinViewCounts('');
                                            } else {
                                                handleFilterChange('minViewCounts', Number(e.target.value));
                                                setCustomMinViewCounts('');
                                            }
                                        }}
                                        className="flex-1 rounded-lg border-0 bg-gray-50 py-2 text-gray-700 focus:ring-2 focus:ring-indigo-400"
                                    >
                                        {MIN_COUNT_OPTIONS.map(count => (
                                            <option key={count} value={count}>
                                                {count === 0 ? '전체' : `${count}회 이상`}
                                            </option>
                                        ))}
                                        <option value="custom">직접입력</option>
                                    </select>
                                    {customMinViewCounts !== '' && (
                                        <input
                                            type="number"
                                            value={customMinViewCounts}
                                            onChange={(e) => setCustomMinViewCounts(e.target.value)}
                                            className="w-24 rounded-lg border-0 bg-gray-50 py-2 text-gray-700 focus:ring-2 focus:ring-indigo-400"
                                            placeholder="회"
                                        />
                                    )}
                                </div>
                            </div>

                            <div className="space-y-1.5">
                                <label className="block text-sm font-medium text-gray-700">댓글수</label>
                                <div className="flex gap-2">
                                    <select
                                        value={customMinCommentCounts ? 'custom' : (searchParams.minCommentCounts || 0)}
                                        onChange={(e) => {
                                            if (e.target.value === 'custom') {
                                                setCustomMinCommentCounts('');
                                            } else {
                                                handleFilterChange('minCommentCounts', Number(e.target.value));
                                                setCustomMinCommentCounts('');
                                            }
                                        }}
                                        className="flex-1 rounded-lg border-0 bg-gray-50 py-2 text-gray-700 focus:ring-2 focus:ring-indigo-400"
                                    >
                                        {MIN_COUNT_OPTIONS.map(count => (
                                            <option key={count} value={count}>
                                                {count === 0 ? '전체' : `${count}개 이상`}
                                            </option>
                                        ))}
                                        <option value="custom">직접입력</option>
                                    </select>
                                    {customMinCommentCounts !== '' && (
                                        <input
                                            type="number"
                                            value={customMinCommentCounts}
                                            onChange={(e) => setCustomMinCommentCounts(e.target.value)}
                                            className="w-24 rounded-lg border-0 bg-gray-50 py-2 text-gray-700 focus:ring-2 focus:ring-indigo-400"
                                            placeholder="개"
                                        />
                                    )}
                                </div>
                            </div>

                            <div className="space-y-1.5">
                                <label className="block text-sm font-medium text-gray-700">좋아요</label>
                                <div className="flex gap-2">
                                    <select
                                        value={customMinLikes ? 'custom' : (searchParams.minLikes || 0)}
                                        onChange={(e) => {
                                            if (e.target.value === 'custom') {
                                                setCustomMinLikes('');
                                            } else {
                                                handleFilterChange('minLikes', Number(e.target.value));
                                                setCustomMinLikes('');
                                            }
                                        }}
                                        className="flex-1 rounded-lg border-0 bg-gray-50 py-2 text-gray-700 focus:ring-2 focus:ring-indigo-400"
                                    >
                                        {MIN_COUNT_OPTIONS.map(count => (
                                            <option key={count} value={count}>
                                                {count === 0 ? '전체' : `${count}개 이상`}
                                            </option>
                                        ))}
                                        <option value="custom">직접입력</option>
                                    </select>
                                    {customMinLikes !== '' && (
                                        <input
                                            type="number"
                                            value={customMinLikes}
                                            onChange={(e) => setCustomMinLikes(e.target.value)}
                                            className="w-24 rounded-lg border-0 bg-gray-50 py-2 text-gray-700 focus:ring-2 focus:ring-indigo-400"
                                            placeholder="개"
                                        />
                                    )}
                                </div>
                            </div>
                        </div>
                    </div>
                </div>
            </div>

            {/* 글쓰기 버튼 */}
            <div className="mb-6 flex justify-end">
                <Link
                    href="/posts/new"
                    className="inline-flex items-center px-4 py-2 bg-indigo-600 text-white rounded-lg hover:bg-indigo-700 transition-colors"
                >
                    <svg className="w-5 h-5 mr-2" fill="none" stroke="currentColor" viewBox="0 0 24 24">
                        <path strokeLinecap="round" strokeLinejoin="round" strokeWidth={2} d="M12 4v16m8-8H4" />
                    </svg>
                    글쓰기
                </Link>
            </div>

            {/* 게시글 그리드 */}
            <div className="grid grid-cols-1 md:grid-cols-2 lg:grid-cols-3 gap-6">
                {posts.length === 0 ? (
                    <div className="col-span-full text-center py-12">
                        <p className="text-gray-500 text-lg">게시글이 없습니다.</p>
                    </div>
                ) : (
                    posts.map((post) => (
                        <div key={post.id} className="bg-white rounded-xl shadow-lg overflow-hidden">
                            <div className="p-6">
                                <h3 className="text-xl font-semibold text-gray-900 mb-2">
                                    {post.title}
                                </h3>
                                <p className="text-gray-600 line-clamp-3">
                                    {post.content.replace(/<[^>]*>/g, '')}
                                </p>
                                <div className="mt-4 flex items-center justify-between">
                                    <div className="flex items-center space-x-4">
                                        <span className="flex items-center text-gray-500">
                                            <svg className="w-5 h-5 mr-1" fill="currentColor" viewBox="0 0 20 20">
                                                <path d="M2 10.5a1.5 1.5 0 113 0v6a1.5 1.5 0 01-3 0v-6zM6 10.333v5.43a2 2 0 001.106 1.79l.05.025A4 4 0 008.943 18h5.416a2 2 0 001.962-1.608l1.2-6A2 2 0 0015.56 8H12V4a2 2 0 00-2-2 1 1 0 00-1 1v.667a4 4 0 01-.8 2.4L6.8 7.933a4 4 0 00-.8 2.4z" />
                                            </svg>
                                            {post.likes}
                                        </span>
                                        <span className="flex items-center text-gray-500">
                                            <svg className="w-5 h-5 mr-1" fill="currentColor" viewBox="0 0 20 20">
                                                <path fillRule="evenodd" d="M18 10c0 3.866-3.582 7-8 7a8.841 8.841 0 01-4.083-.98L2 17l1.338-3.123C2.493 12.767 2 11.434 2 10c0-3.866 3.582-7 8-7s8 3.134 8 7zM7 9H5v2h2V9zm8 0h-2v2h2V9zM9 9h2v2H9V9z" clipRule="evenodd" />
                                            </svg>
                                            {post.commentCounts}
                                        </span>
                                        <span className="flex items-center text-gray-500">
                                            <svg className="w-5 h-5 mr-1" fill="currentColor" viewBox="0 0 20 20">
                                                <path d="M10 12a2 2 0 100-4 2 2 0 000 4z" />
                                                <path fillRule="evenodd" d="M.458 10C1.732 5.943 5.522 3 10 3s8.268 2.943 9.542 7c-1.274 4.057-5.064 7-9.542 7S1.732 14.057.458 10zM14 10a4 4 0 11-8 0 4 4 0 018 0z" clipRule="evenodd" />
                                            </svg>
                                            {post.viewCounts}
                                        </span>
                                    </div>
                                    <Link
                                        href={`/posts/${post.id}`}
                                        className="text-indigo-600 hover:text-indigo-500 font-medium"
                                    >
                                        자세히 보기
                                    </Link>
                                </div>
                            </div>
                        </div>
                    ))
                )}
            </div>
            
            {hasMore && (
                <div className="mt-8 flex justify-center">
                    <button
                        onClick={() => nextCursor && fetchPosts(nextCursor)}
                        disabled={isLoading}
                        className="px-4 py-2 bg-indigo-600 text-white rounded-md hover:bg-indigo-700 disabled:opacity-50"
                    >
                        {isLoading ? '로딩 중...' : '더 보기'}
                    </button>
                </div>
            )}
        </main>
    );
}