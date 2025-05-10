import { useState } from 'react';
import { FiEdit2 } from 'react-icons/fi';

const FILTERS = ['전체', '음식', '영화', '애니', '여행', '카페', '기타'];

export default function SubBar() {
  const [selectedFilter, setSelectedFilter] = useState('전체');

  return (
    <div className="w-full flex justify-center bg-white">
      <div className="w-full max-w-md flex items-center px-4 py-3 gap-2">
        <button className="min-w-[96px] px-6 py-2 rounded-xl bg-black text-white font-extrabold text-lg shrink-0 shadow-md flex items-center gap-2">
          글쓰기
          <FiEdit2 className="text-xl" />
        </button>
        <div className="flex-1 flex gap-2 overflow-x-auto scrollbar-hide whitespace-nowrap">
          {FILTERS.map((filter) => (
            <button
              key={filter}
              className={`min-w-[72px] px-4 py-1.5 rounded-lg font-semibold text-base shrink-0 whitespace-nowrap ${selectedFilter === filter ? 'bg-black text-white' : 'bg-gray-200 text-gray-700'}`}
              onClick={() => setSelectedFilter(filter)}
            >
              {filter}
            </button>
          ))}
        </div>
      </div>
    </div>
  );
} 