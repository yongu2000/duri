import { Post } from '@/types/posts';
import Link from 'next/link';
import LikeButton from './LikeButton';

interface PostHeaderProps {
    post: Post;
    isLiked: boolean;
    likeCount: number;
    onLikeClick: () => void;
    onDelete: () => void;
    isAuthor: boolean;
}

export default function PostHeader({ 
    post, 
    isLiked, 
    likeCount, 
    onLikeClick, 
    onDelete,
    isAuthor 
}: PostHeaderProps) {
    return (
        <div className="border-b pb-4 mb-6">
            <div className="flex justify-between items-start">
                <h1 className="text-3xl font-bold text-gray-900">
                    {post.title}
                </h1>
                {isAuthor && (
                    <div className="flex space-x-4">
                        <Link
                            href={`/posts/${post.id}/edit`}
                            className="text-indigo-600 hover:text-indigo-500"
                        >
                            수정
                        </Link>
                        <button
                            onClick={onDelete}
                            className="text-red-600 hover:text-red-500"
                        >
                            삭제
                        </button>
                    </div>
                )}
            </div>
            <div className="mt-4 flex items-center justify-between text-sm text-gray-500">
                <div className="flex items-center space-x-4">
                    <span>작성자: <Link href={`/${post.user.username}`} className="text-indigo-600 hover:text-indigo-500">{post.user.name}</Link></span>
                    <span>작성일: {new Date(post.createdAt).toLocaleDateString()}</span>
                    {post.modifiedAt !== post.createdAt && (
                        <span>수정일: {new Date(post.modifiedAt).toLocaleDateString()}</span>
                    )}
                </div>
                <div className="flex items-center space-x-4">
                    <span className="flex items-center">
                        <svg className="w-5 h-5 mr-1" fill="currentColor" viewBox="0 0 20 20">
                            <path d="M10 12a2 2 0 100-4 2 2 0 000 4z" />
                            <path fillRule="evenodd" d="M.458 10C1.732 5.943 5.522 3 10 3s8.268 2.943 9.542 7c-1.274 4.057-5.064 7-9.542 7S1.732 14.057.458 10zM14 10a4 4 0 11-8 0 4 4 0 018 0z" clipRule="evenodd" />
                        </svg>
                        <span>{post.viewCounts}</span>
                    </span>
                    <LikeButton 
                        isLiked={isLiked} 
                        likeCount={likeCount} 
                        onClick={onLikeClick}
                    />
                </div>
            </div>
        </div>
    );
}