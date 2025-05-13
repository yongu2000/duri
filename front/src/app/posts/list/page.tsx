'use client';

import { useEffect, useState } from 'react';
import Link from 'next/link';
import { postService, PostSearchParams } from '@/services/posts';
import { PostSummary, PostPage } from '@/types/posts';
import { toast } from 'react-hot-toast';
import { format, subDays, subMonths, subYears, startOfDay } from 'date-fns';
import { ko } from 'date-fns/locale';

function Pagination({ 
    currentPage, 
    totalPages, 
    onPageChange 
}: { 
    currentPage: number; 
    totalPages: number; 
    onPageChange: (page: number) => void;
}) {
    const groupSize = 9;
    const currentGroup = Math.floor(currentPage / groupSize);
    const startPage = currentGroup * groupSize;
    
    const pageNumbers = Array.from(
        { length: Math.min(groupSize, totalPages - startPage) }, 
        (_, i) => startPage + i
    );

    const hasNextGroup = (currentGroup + 1) * groupSize < totalPages;
    const hasPrevGroup = currentGroup > 0;

    const handlePrevGroup = () => {
        const prevGroupLastPage = (currentGroup * groupSize) - 1;
        onPageChange(prevGroupLastPage);
    };

    const handleNextGroup = () => {
        onPageChange((currentGroup + 1) * groupSize);
    };

    return (
        <div className="mt-4 flex justify-center">
            <nav className="flex items-center gap-2">
                <button 
                    onClick={handlePrevGroup}
                    disabled={!hasPrevGroup}
                    className="px-3 py-1 rounded border disabled:opacity-50 disabled:bg-gray-100"
                >
                    &lt;
                </button>
                
                {pageNumbers.map((pageNum) => (
                    <button
                        key={pageNum}
                        onClick={() => onPageChange(pageNum)}
                        disabled={pageNum === currentPage}
                        className={`px-3 py-1 rounded border ${
                            pageNum === currentPage 
                            ? 'bg-indigo-600 text-white' 
                            : 'hover:bg-gray-50'
                        }`}
                    >
                        {pageNum + 1}
                    </button>
                ))}
                
                <button 
                    onClick={handleNextGroup}
                    disabled={!hasNextGroup}
                    className="px-3 py-1 rounded border disabled:opacity-50 disabled:bg-gray-100"
                >
                    &gt;
                </button>
            </nav>
        </div>
    );
}

const PAGE_SIZE_OPTIONS = [10, 20, 30, 50];
const MIN_COUNT_OPTIONS = [0, 10, 50, 100, 500];
const DATE_RANGE_OPTIONS = [
    { value: 'all', label: '전체 기간' },
    { value: 'week', label: '최근 1주' },
    { value: 'month', label: '최근 1달' },
    { value: 'year', label: '최근 1년' },
    { value: 'custom', label: '날짜선택' }
] as const;

export default function ListPostsPage() {
    const [posts, setPosts] = useState<PostSummary[]>([]);
    const [page, setPage] = useState(0);
    const [totalPages, setTotalPages] = useState(0);
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

    useEffect(() => {
        const fetchPosts = async () => {
            try {
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
                    page,
                    searchType,
                    searchKeyword,
                    startDate: calculatedStartDate,
                    endDate: calculatedEndDate,
                    minViewCounts: searchParams.minViewCounts || undefined,
                    minCommentCounts: searchParams.minCommentCounts || undefined,
                    minLikes: searchParams.minLikes || undefined
                };

                // 파라미터 설정 전 로깅
                console.log('Search params before API call:', params);
                console.log('minViewCounts:', params.minViewCounts);
                console.log('minCommentCounts:', params.minCommentCounts);
                console.log('minLikes:', params.minLikes);

                const response = await postService.getFilteredPosts(params);
                setPosts(response.content);
                setTotalPages(response.totalPages);
            } catch (error) {
                console.error('글 목록 로드 실패:', error);
                toast.error('글 목록을 불러오는데 실패했습니다.');
            }
        };

        fetchPosts();
    }, [page, searchParams, dateRange, startDate, endDate, searchType, searchKeyword]);

    const handleSearch = (e: React.FormEvent) => {
        e.preventDefault();
        const newParams: PostSearchParams = {
            ...searchParams,
            searchType,
            searchKeyword,
            page: 0  // 검색 시 첫 페이지로 이동
        };
        setSearchParams(newParams);
        setPage(0);
    };

    const handleSortChange = (sortBy: 'date' | 'likes' | 'comments' | 'views', sortDirection: 'asc' | 'desc') => {
        setSearchParams(prev => ({
            ...prev,
            sortBy,
            sortDirection
        }));
        setPage(0);
    };

    const handleFilterChange = (key: keyof PostSearchParams, value: number) => {
        setSearchParams(prev => ({
            ...prev,
            [key]: value
        }));
        setPage(0);
    };

    const handleDateRangeChange = (value: typeof DATE_RANGE_OPTIONS[number]['value']) => {
        setDateRange(value);
        if (value !== 'custom') {
            setStartDate('');
            setEndDate('');
        }
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
        setPage(0);
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
                                        setPage(0);
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
                                <select
                                    value={searchParams.minViewCounts || 0}
                                    onChange={(e) => handleFilterChange('minViewCounts', Number(e.target.value))}
                                    className="w-full rounded-lg border-0 bg-gray-50 py-2 text-gray-700 focus:ring-2 focus:ring-indigo-400"
                                >
                                    {MIN_COUNT_OPTIONS.map(count => (
                                        <option key={count} value={count}>
                                            {count === 0 ? '전체' : `${count}회 이상`}
                                        </option>
                                    ))}
                                </select>
                            </div>

                            <div className="space-y-1.5">
                                <label className="block text-sm font-medium text-gray-700">댓글수</label>
                                <select
                                    value={searchParams.minCommentCounts || 0}
                                    onChange={(e) => handleFilterChange('minCommentCounts', Number(e.target.value))}
                                    className="w-full rounded-lg border-0 bg-gray-50 py-2 text-gray-700 focus:ring-2 focus:ring-indigo-400"
                                >
                                    {MIN_COUNT_OPTIONS.map(count => (
                                        <option key={count} value={count}>
                                            {count === 0 ? '전체' : `${count}개 이상`}
                                        </option>
                                    ))}
                                </select>
                            </div>

                            <div className="space-y-1.5">
                                <label className="block text-sm font-medium text-gray-700">좋아요</label>
                                <select
                                    value={searchParams.minLikes || 0}
                                    onChange={(e) => handleFilterChange('minLikes', Number(e.target.value))}
                                    className="w-full rounded-lg border-0 bg-gray-50 py-2 text-gray-700 focus:ring-2 focus:ring-indigo-400"
                                >
                                    {MIN_COUNT_OPTIONS.map(count => (
                                        <option key={count} value={count}>
                                            {count === 0 ? '전체' : `${count}개 이상`}
                                        </option>
                                    ))}
                                </select>
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

            {/* 테이블 영역 */}
            <div className="overflow-x-auto">
                {posts.length === 0 ? (
                    <div className="text-center py-12">
                        <p className="text-gray-500 text-lg">게시글이 없습니다.</p>
                    </div>
                ) : (
                    <table className="min-w-full divide-y divide-gray-200">
                        <thead className="bg-gray-50">
                            <tr>
                                <th scope="col" className="px-6 py-3 text-left text-xs font-medium text-gray-500 uppercase tracking-wider">
                                    번호
                                </th>
                                <th scope="col" className="px-6 py-3 text-left text-xs font-medium text-gray-500 uppercase tracking-wider">
                                    제목
                                </th>
                                <th scope="col" className="px-6 py-3 text-left text-xs font-medium text-gray-500 uppercase tracking-wider">
                                    작성자
                                </th>
                                <th scope="col" className="px-6 py-3 text-left text-xs font-medium text-gray-500 uppercase tracking-wider">
                                    작성일
                                </th>
                                <th scope="col" className="px-6 py-3 text-left text-xs font-medium text-gray-500 uppercase tracking-wider">
                                    조회수
                                </th>
                                <th scope="col" className="px-6 py-3 text-left text-xs font-medium text-gray-500 uppercase tracking-wider">
                                    좋아요
                                </th>
                            </tr>
                        </thead>
                        <tbody className="bg-white divide-y divide-gray-200">
                            {posts.map((post) => (
                                <tr key={post.id} className="hover:bg-gray-50">
                                    <td className="px-6 py-4 whitespace-nowrap text-sm text-gray-500">
                                        {post.id}
                                    </td>
                                    <td className="px-6 py-4 whitespace-nowrap">
                                        <Link href={`/posts/${post.id}`} className="text-indigo-600 hover:text-indigo-900">
                                            {post.title}
                                            {post.commentCounts > 0 && (
                                                <span className="ml-2 text-gray-500">
                                                    [{post.commentCounts}]
                                                </span>
                                            )}
                                        </Link>
                                    </td>
                                    <td className="px-6 py-4 whitespace-nowrap text-sm text-gray-500">
                                        <Link href={`/${post.user.username}`} className="text-indigo-600 hover:text-indigo-900">
                                            {post.user.name}
                                        </Link>
                                    </td>
                                    <td className="px-6 py-4 whitespace-nowrap text-sm text-gray-500">
                                        {format(new Date(post.createdAt), 'yyyy.MM.dd', { locale: ko })}
                                    </td>
                                    <td className="px-6 py-4 whitespace-nowrap text-sm text-gray-500">
                                        {post.viewCounts}
                                    </td>
                                    <td className="px-6 py-4 whitespace-nowrap text-sm text-gray-500">
                                        {post.likes}
                                    </td>
                                </tr>
                            ))}
                        </tbody>
                    </table>
                )}
            </div>
            <Pagination 
                currentPage={page} 
                totalPages={totalPages} 
                onPageChange={setPage} 
            />
        </main>
    );
}