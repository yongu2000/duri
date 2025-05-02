interface LikeButtonProps {
    isLiked: boolean;
    likeCount: number;
    onClick: () => void;
}

export default function LikeButton({ isLiked, likeCount, onClick }: LikeButtonProps) {
    return (
        <button
            onClick={onClick}
            className={`
                flex items-center space-x-2 px-4 py-2 rounded-lg
                transition-all duration-300 ease-in-out
                transform hover:scale-105 active:scale-95
                ${isLiked 
                    ? 'bg-red-500 text-white hover:bg-red-600' 
                    : 'bg-gray-100 text-gray-700 hover:bg-gray-200'
                }
            `}
        >
            <svg 
                className={`
                    w-5 h-5 transition-transform duration-300
                    ${isLiked ? 'scale-110' : 'scale-100'}
                `}
                fill={isLiked ? 'currentColor' : 'none'} 
                stroke="currentColor" 
                viewBox="0 0 24 24"
            >
                <path 
                    strokeLinecap="round" 
                    strokeLinejoin="round" 
                    strokeWidth={2} 
                    d="M4.318 6.318a4.5 4.5 0 000 6.364L12 20.364l7.682-7.682a4.5 4.5 0 00-6.364-6.364L12 7.636l-1.318-1.318a4.5 4.5 0 00-6.364 0z" 
                />
            </svg>
            <span className={`
                transition-all duration-300
                ${isLiked ? 'transform scale-110' : ''}
            `}>
                {likeCount}
            </span>
        </button>
    );
}