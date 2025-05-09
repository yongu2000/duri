import React from 'react';
import { ConnectionStatus, ConnectionStatusResponse } from '@/types/coupleConnect';

interface CoupleConnectionModalProps {
  isOpen: boolean;
  onClose: () => void;
  type: 'request' | 'response';
  status: ConnectionStatusResponse['status'];
  requesterName?: string;
  respondentName?: string;
  onAccept: () => Promise<void>;
  onReject: () => Promise<void>;
  onCancel: () => Promise<void>;
  showCancelButton: boolean;
}

export default function CoupleConnectionModal({
  isOpen,
  onClose,
  type,
  status,
  requesterName,
  respondentName,
  onAccept,
  onReject,
  onCancel,
  showCancelButton
}: CoupleConnectionModalProps) {
  if (!isOpen) return null;

  const getModalContent = () => {
    if (type === 'request') {
      switch (status) {
        case 'PENDING':
          return {
            title: '요청 대기 중',
            message: `${respondentName || '상대방'}님에게 요청을 보냈습니다.`,
            buttons: (
              <>
                {showCancelButton && (
                  <button
                    onClick={onCancel}
                    className="w-full py-3 bg-gray-200 text-gray-800 rounded-xl font-bold hover:bg-gray-300"
                  >
                    요청 취소
                  </button>
                )}
              </>
            )
          };
        case 'ACCEPT':
          return {
            title: '요청 수락됨',
            message: `${respondentName || '상대방'}님이 요청을 수락했습니다.`,
            buttons: (
              <button
                onClick={onClose}
                className="w-full py-3 bg-black text-white rounded-xl font-bold hover:bg-gray-800"
              >
                확인
              </button>
            )
          };
        case 'REJECT':
          return {
            title: '요청 거절됨',
            message: `${respondentName || '상대방'}님이 요청을 거절했습니다.`,
            buttons: (
              <button
                onClick={onClose}
                className="w-full py-3 bg-black text-white rounded-xl font-bold hover:bg-gray-800"
              >
                확인
              </button>
            )
          };
        case 'CANCEL':
          return {
            title: '요청 취소됨',
            message: '요청이 취소되었습니다.',
            buttons: (
              <button
                onClick={onClose}
                className="w-full py-3 bg-black text-white rounded-xl font-bold hover:bg-gray-800"
              >
                확인
              </button>
            )
          };
        default:
          return {
            title: '알 수 없는 상태',
            message: '잠시 후 다시 시도해주세요.',
            buttons: (
              <button
                onClick={onClose}
                className="w-full py-3 bg-black text-white rounded-xl font-bold hover:bg-gray-800"
              >
                확인
              </button>
            )
          };
      }
    } else {
      switch (status) {
        case 'PENDING':
          return {
            title: '연결 요청',
            message: `${requesterName || '상대방'}님이 연결을 요청했습니다.`,
            buttons: (
              <>
                <button
                  onClick={onAccept}
                  className="w-full py-3 bg-black text-white rounded-xl font-bold hover:bg-gray-800 mb-2"
                >
                  수락
                </button>
                <button
                  onClick={onReject}
                  className="w-full py-3 bg-gray-200 text-gray-800 rounded-xl font-bold hover:bg-gray-300"
                >
                  거절
                </button>
              </>
            )
          };
        case 'CANCEL':
          return {
            title: '요청 취소됨',
            message: '상대방이 요청을 취소했습니다.',
            buttons: (
              <button
                onClick={onClose}
                className="w-full py-3 bg-black text-white rounded-xl font-bold hover:bg-gray-800"
              >
                확인
              </button>
            )
          };
        default:
          return {
            title: '알 수 없는 상태',
            message: '잠시 후 다시 시도해주세요.',
            buttons: (
              <button
                onClick={onClose}
                className="w-full py-3 bg-black text-white rounded-xl font-bold hover:bg-gray-800"
              >
                확인
              </button>
            )
          };
      }
    }
  };

  const content = getModalContent();

  return (
    <div className="fixed inset-0 z-50 flex items-center justify-center">
      <div className="fixed inset-0 bg-black opacity-50"></div>
      <div className="relative bg-white rounded-2xl p-6 w-full max-w-sm mx-4">
        <h3 className="text-xl font-bold text-center mb-2">{content.title}</h3>
        <p className="text-gray-600 text-center mb-6">{content.message}</p>
        {content.buttons}
      </div>
    </div>
  );
} 