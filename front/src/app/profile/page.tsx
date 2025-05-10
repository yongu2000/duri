import Image from 'next/image';

// 더미 데이터
const coupleName = '홍길동 ♥ 김영희';
const coupleIntro = '함께한지 100일! 우리의 추억을 기록해요.';
const users = [
  {
    name: '홍길동',
    profileImageUrl: '/default-profile.png',
  },
  {
    name: '김영희',
    profileImageUrl: '/default-profile.png',
  },
];

export default function ProfilePage() {
  return (
    <div className="min-h-screen flex flex-col items-center justify-center bg-gray-50 px-4">
      <div className="w-full max-w-md bg-white rounded-2xl shadow-lg p-8 flex flex-col items-center">
        {/* 커플 이름 */}
        <div className="text-2xl font-extrabold text-gray-900 mb-4 text-center">{coupleName}</div>
        {/* 두 사람 프로필 */}
        <div className="flex items-center gap-6 mb-2">
          {users.map((user, idx) => (
            <div key={idx} className="flex flex-col items-center">
              <div className="w-20 h-20 rounded-full overflow-hidden bg-gray-100 relative mb-2">
                <Image
                  src={user.profileImageUrl}
                  alt={user.name}
                  fill
                  unoptimized
                  className="object-cover"
                />
              </div>
              <div className="text-base font-semibold text-gray-800">{user.name}</div>
            </div>
          ))}
        </div>
        {/* 커플 소개 */}
        <div className="text-center text-gray-500 text-sm mb-8">{coupleIntro}</div>
        {/* 버튼 목록 */}
        <div className="w-full flex flex-col gap-3">
          <button className="w-full py-3 rounded-xl bg-indigo-50 hover:bg-indigo-100 text-indigo-700 font-bold text-base">커플 프로필 수정</button>
          <button className="w-full py-3 rounded-xl bg-gray-100 hover:bg-gray-200 text-gray-700 font-bold text-base">개인 프로필 수정</button>
          <button className="w-full py-3 rounded-xl bg-gray-100 hover:bg-gray-200 text-gray-700 font-bold text-base">좋아요한 글</button>
          <button className="w-full py-3 rounded-xl bg-gray-100 hover:bg-gray-200 text-gray-700 font-bold text-base">쓴 댓글 보기</button>
          <button className="w-full py-3 rounded-xl bg-gray-100 hover:bg-gray-200 text-gray-700 font-bold text-base">개인 인증정보 수정</button>
          <button className="w-full py-3 rounded-xl bg-gray-100 hover:bg-gray-200 text-gray-700 font-bold text-base">설정</button>
        </div>
      </div>
    </div>
  );
} 