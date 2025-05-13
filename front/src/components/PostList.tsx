import type { PostCard } from '@/types/post';
import PostCardComponent from './PostCard';

const dummyPosts: PostCard[] = [
  {
    id: 1,
    placeName: '스타벅스 강남점',
    title: '오늘의 데이트',
    date: '2024-05-01',
    images: [
      'https://images.unsplash.com/photo-1554118811-1e0d58224f24?w=500',
      'https://images.unsplash.com/photo-1514933651103-005eec06c04b?w=500'
    ],
    userLeft: {
      profile: {
        profileImage: 'https://images.unsplash.com/photo-1494790108377-be9c29b29330?w=100',
        gender: 'FEMALE',
        age: 25,
        name: '지은'
      },
      rating: 4.5,
      comment: '분위기가 좋았어요'
    },
    userRight: {
      profile: {
        profileImage: 'https://images.unsplash.com/photo-1507003211169-0a1dd7228f2d?w=100',
        gender: 'MALE',
        age: 27,
        name: '민수'
      },
      rating: 5,
      comment: '커피가 맛있었어요'
    },
    hashtags: ['카페', '데이트', '강남'],
    scope: 'PUBLIC',
    likeCount: 42,
    commentCount: 8,
    coupleName: '지은 ♥ 민수',
    address: '서울특별시 강남구 테헤란로 123'
  }
];

export default function PostList({ posts = dummyPosts }: { posts?: PostCard[] }) {
  const hasPosts = posts.length > 0;

  return (
    <div className="w-full flex flex-col items-center justify-center py-8">
      {hasPosts ? (
        <div className="w-full max-w-sm space-y-6">
          {posts.map(post => (
            <PostCardComponent key={post.id} post={post} />
          ))}
        </div>
      ) : (
        <div className="flex flex-col items-center justify-center py-16">
          <div className="text-center text-lg text-gray-700 mb-1">아직 리뷰가 없습니다</div>
          <div className="text-center text-base text-gray-500">두사람의 추억을 시작해보세요</div>
        </div>
      )}
    </div>
  );
} 