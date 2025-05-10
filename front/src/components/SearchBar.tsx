import { useState } from 'react';
import { FiChevronDown, FiChevronUp, FiSliders } from 'react-icons/fi';

export default function SearchBar() {
  const [search, setSearch] = useState('');
  const [detailSearch, setDetailSearch] = useState('');
  const [showDetail, setShowDetail] = useState(false);
  const [sort, setSort] = useState('최신순');
  const [dateRange, setDateRange] = useState('전체 기간');

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