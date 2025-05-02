"use client";

import React from "react";

export default function CoupleLinkPage() {
  return (
    <main className="max-w-md mx-auto px-2 py-6">
      <div className="bg-white rounded-2xl shadow p-6 mb-6">
        <h2 className="text-2xl font-bold text-center mb-4">커플 연결</h2>
        <div className="flex justify-center mb-3">
          <span className="text-5xl text-red-400">❤️</span>
        </div>
        <p className="text-center text-gray-500 mb-6">현재 연결된 커플이 없습니다</p>
        <div className="mb-4">
          <div className="font-semibold mb-1">파트너 이메일</div>
          <input
            className="w-full rounded-lg border px-3 py-2 text-sm mb-3"
            placeholder="파트너의 이메일을 입력하세요"
          />
          <div className="font-semibold mb-1">메시지</div>
          <textarea
            className="w-full rounded-lg border px-3 py-2 text-sm mb-3 resize-none"
            rows={3}
            placeholder="파트너에게 보낼 메시지를 입력하세요"
          />
          <button className="w-full py-3 rounded-lg bg-red-400 text-white font-bold text-base mt-2">연결 요청 보내기</button>
        </div>
      </div>
      <div>
        <div className="font-semibold mb-2">대기 중인 요청</div>
        <div className="bg-gray-50 rounded-xl p-4 mb-2">
          <div className="font-bold mb-1">홍길동</div>
          <div className="mb-1">"함께 추억을 만들어요!"</div>
          <div className="text-xs text-gray-400 mb-2">2024-03-15 14:30</div>
          <div className="flex gap-2">
            <button className="flex-1 py-2 rounded-lg bg-green-600 text-white">수락</button>
            <button className="flex-1 py-2 rounded-lg bg-red-400 text-white">거절</button>
          </div>
        </div>
      </div>
    </main>
  );
} 