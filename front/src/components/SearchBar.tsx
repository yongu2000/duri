import { useState, useEffect, useCallback, useRef } from 'react';
import { FiChevronDown, FiChevronUp, FiSliders } from 'react-icons/fi';
import { PostSearchOptions } from '@/types/post';

interface SearchBarProps {
  onSearchOptionsChange: (options: PostSearchOptions) => void;
}

export default function SearchBar({ onSearchOptionsChange }: SearchBarProps) {
  const [search, setSearch] = useState('');
  const [showDetail, setShowDetail] = useState(false);
  const [sort, setSort] = useState('최신순');
  const [dateRange, setDateRange] = useState('전체 기간');
  const onSearchOptionsChangeRef = useRef(onSearchOptionsChange);

  // onSearchOptionsChange가 변경될 때마다 ref 업데이트
  useEffect(() => {
    onSearchOptionsChangeRef.current = onSearchOptionsChange;
  }, [onSearchOptionsChange]);

  const updateSearchOptions = useCallback(() => {
    const searchOptions: PostSearchOptions = {
      searchKeyword: search || undefined,
      sortBy: sort === '최신순' || sort === '오래된순' ? 'DATE' : 'RATE',
      sortDirection: sort === '오래된순' || sort === '별점낮은순' ? 'ASC' : 'DESC'
    };

    // 날짜 범위 설정
    const today = new Date();
    if (dateRange !== '전체 기간') {
      const startDate = new Date();
      if (dateRange === '최근 1주') {
        startDate.setDate(today.getDate() - 7);
      } else if (dateRange === '최근 1달') {
        startDate.setMonth(today.getMonth() - 1);
      } else if (dateRange === '최근 1년') {
        startDate.setFullYear(today.getFullYear() - 1);
      }
      searchOptions.startDate = startDate.toISOString().split('T')[0];
      searchOptions.endDate = today.toISOString().split('T')[0];
    }

    onSearchOptionsChangeRef.current(searchOptions);
  }, [search, sort, dateRange]);

  useEffect(() => {
    updateSearchOptions();
  }, [updateSearchOptions]);

  return (
    <div className="w-full flex flex-col items-center bg-white">
      <div className="w-full max-w-md flex gap-3 px-4 pt-4 pb-2">
        <input
          type="text"
          value={search}
          onChange={e => setSearch(e.target.value)}
          placeholder="검색어를 입력하세요"
          className="flex-1 bg-white border-0 border-b-2 border-gray-200 placeholder-gray-400 text-gray-900 px-0 py-3 focus:outline-none focus:border-black text-base"
        />
        <button
          type="button"
          className="flex items-center gap-1 px-3 py-2 text-sm text-gray-500 hover:text-black font-medium border-0 bg-transparent"
          onClick={() => setShowDetail(v => !v)}
        >
          <FiSliders className="text-lg" />
          상세검색
          {showDetail ? <FiChevronUp /> : <FiChevronDown />}
        </button>
      </div>
      
      {showDetail && (
        <div className="w-full max-w-md px-4 pb-4 animate-fade-in">
          <div className="flex gap-2 mb-2">
            <select
              className="flex-1 bg-white border-0 border-b-2 border-gray-200 text-gray-700 px-0 py-2 focus:outline-none focus:border-black text-base"
              value={sort}
              onChange={e => setSort(e.target.value)}
            >
              <option value="최신순">최신순</option>
              <option value="오래된순">오래된순</option>
              <option value="별점높은순">별점 높은순</option>
              <option value="별점낮은순">별점 낮은순</option>
            </select>
            <select
              className="flex-1 bg-white border-0 border-b-2 border-gray-200 text-gray-700 px-0 py-2 focus:outline-none focus:border-black text-base"
              value={dateRange}
              onChange={e => setDateRange(e.target.value)}
            >
              <option value="전체 기간">전체 기간</option>
              <option value="최근 1주">최근 1주</option>
              <option value="최근 1달">최근 1달</option>
              <option value="최근 1년">최근 1년</option>
            </select>
          </div>
        </div>
      )}
    </div>
  );
} 