"use client";

import React, { useState } from "react";

export default function CoupleLinkPage() {
  const [showShareModal, setShowShareModal] = useState(false);

  return (
    <div className="min-h-screen flex flex-col items-center justify-center bg-white py-12 px-4 sm:px-6 lg:px-8">
      <div className="w-full max-w-4xl">
        {/* 모바일 전용 타이틀/부제목 + 구분선 */}
        <div className="block md:hidden w-full text-center mb-8">
          <h2 className="text-5xl font-black text-gray-900 tracking-tight" style={{ fontFamily: "'BMDOHYEON', sans-serif" }}>
            <span style={{ fontFamily: "'Cafe24Supermagic-Bold-v1.0', cursive", fontSize: '1.1em', verticalAlign: 'middle' }}>“</span>
            두리
            <span style={{ fontFamily: "'Cafe24Supermagic-Bold-v1.0', cursive", fontSize: '1.1em', verticalAlign: 'middle' }}>”</span>
          </h2>
          <p className="mt-1 text-base text-gray-500">연결하여 두사람의 이야기를 시작하세요</p>
          <div className="mx-auto w-4/5 border-t border-gray-200 mt-8 mb-0" />
        </div>
        {/* 데스크탑 전용 타이틀/부제목 */}
        <div className="hidden md:block text-center mb-10">
          <h2 className="text-5xl font-black text-gray-900 tracking-tight" style={{ fontFamily: "'BMDOHYEON', sans-serif" }}>
            <span style={{ fontFamily: "'Cafe24Supermagic-Bold-v1.0', cursive", fontSize: '1.2em', verticalAlign: 'middle' }}>“</span>
            두리
            <span style={{ fontFamily: "'Cafe24Supermagic-Bold-v1.0', cursive", fontSize: '1.2em', verticalAlign: 'middle' }}>”</span>
          </h2>
          <p className="mt-1 text-base text-gray-500">연결하여 두사람의 이야기를 시작하세요</p>
        </div>
        <div className="flex flex-col md:flex-row bg-white items-stretch" style={{minHeight:'340px'}}>
          {/* 내 인증코드 영역 */}
          <div className="flex-1 flex flex-col justify-center items-center py-0 mt-0">
            <div className="text-xl text-gray-700 mb-2 text-center">나의 인증코드</div>

            {/* 카드 + 공유하기 버튼을 감싸는 래퍼 */}
            <div className="relative w-full max-w-xs">
              {/* 인증 코드 카드 */}
              <div className="bg-white border border-gray-200 rounded-2xl shadow-sm p-6 flex flex-col items-center">
                <div
                  className="text-2xl md:text-3xl font-black tracking-widest text-gray-900 text-center"
                  style={{ letterSpacing: "0.15em" }}
                >
                  ABCD - EFGH
                </div>
              </div>

              {/* 공유하기 버튼 - 카드 바깥 우측 하단에 고정 */}
              <button
                className="absolute -bottom-5 right-2 text-xs text-gray-600 hover:text-gray-800 font-medium"
                onClick={() => setShowShareModal(true)}
              >
                공유하기
              </button>
            </div>

            {/* 안내 문구 */}
            <ol className="list-decimal list-inside text-base text-gray-700 mb-4 mt-12 space-y-1 text-center">
              <li>상대방 화면에서 나의 인증코드 입력하기</li>
              <li>상대방에게 코드 전송하기</li>
            </ol>

            {/* 모바일에서만 보이는 가로 구분선 */}
            <div className="block md:hidden mx-auto w-4/5 border-t border-gray-200 mt-4 mb-8" />
          </div>
          {/* 세로 구분선 (데스크탑만) */}
          <div className="hidden md:flex w-px bg-gray-200 mx-2" />
          {/* 인증코드 입력 영역 */}
          <div className="flex-1 flex flex-col justify-center items-center py-4">
            <div className="w-full max-w-xs flex items-center gap-2 mb-4">
              <input
                className="w-full px-4 py-3 border-b-2 border-gray-200 placeholder-gray-400 text-gray-900 focus:outline-none focus:border-black text-lg"
                placeholder="상대방 코드 입력"
                maxLength={9}
                disabled
              />
              <button className="shrink-0 px-6 py-2 bg-black text-white rounded-xl text-base font-bold hover:bg-gray-800" disabled>
                확인
              </button>
            </div>
          </div>
        </div>
      </div>
      {/* 공유 모달(바텀시트) */}
      {showShareModal && (
        <div
          className="fixed inset-0 z-50 flex items-end justify-center"
          style={{ backgroundColor: "rgba(0,0,0,0.2)" }}
          onClick={() => setShowShareModal(false)}
        >
          <div
            className="w-full max-w-md bg-white rounded-t-2xl p-6 pb-10 flex flex-col items-center"
            onClick={e => e.stopPropagation()}
          >
            <div className="text-lg font-bold text-gray-900 mb-4">공유하기</div>
            <div className="flex flex-row gap-8">
              {/* 카카오톡 */}
              <button className="flex flex-col items-center group">
                <img src="/kakao.png" alt="카카오톡" className="w-12 h-12 mb-1" />
                <span className="text-xs text-gray-900 font-medium mt-1">카카오톡</span>
              </button>
              {/* 코드 복사 */}
              <button className="flex flex-col items-center group">
                <span className="flex items-center justify-center w-12 h-12 rounded-full border border-gray-400 mb-1">
                  <img src="/copy.png" alt="코드 복사" className="w-7 h-7" />
                </span>
                <span className="text-xs text-gray-900 font-medium mt-1">코드 복사</span>
              </button>
            </div>
            <button
              className="mt-6 text-gray-400 text-sm"
              onClick={() => setShowShareModal(false)}
            >
              닫기
            </button>
          </div>
        </div>
      )}
    </div>
  );
} 