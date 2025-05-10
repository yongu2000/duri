type Post = {
  id: number;
  title: string;
  content: string;
  date: string;
  tags: string[];
  author: string;
};

const dummyPosts: Post[] = [
  // 예시 데이터, 실제 데이터로 대체 가능
  // { id: 1, title: '첫 번째 리뷰', content: '정말 좋았어요!', date: '2024-05-01', tags: ['맛집', '데이트'], author: '홍길동' },
];

export default function PostList({ posts = dummyPosts }: { posts?: Post[] }) {
  const hasPosts = posts.length > 0;

  return (
    <div className="w-full flex flex-col items-center justify-center py-8">
      {hasPosts ? (
        <div className="w-full max-w-md space-y-4">
          {posts.map(post => (
            <div key={post.id} className="bg-white rounded-2xl shadow p-5">
              <div className="flex items-center justify-between mb-1">
                <div className="text-base font-bold text-gray-900">{post.title}</div>
                <div className="text-xs text-gray-400">{post.date}</div>
              </div>
              <div className="mb-2 text-sm text-gray-700 line-clamp-2">{post.content}</div>
              <div className="flex gap-2 mb-1">
                {post.tags.map(tag => (
                  <span key={tag} className="bg-gray-100 text-gray-700 rounded px-2 py-0.5 text-xs">#{tag}</span>
                ))}
              </div>
              <div className="text-xs text-gray-400 text-right">by {post.author}</div>
            </div>
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