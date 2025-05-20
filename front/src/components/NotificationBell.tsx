import { useState, useEffect, useRef } from 'react';
import { FiBell } from 'react-icons/fi';
import { notificationService } from '@/services/notification';
import type { Notification } from '@/types/notification';
import { formatDistanceToNow } from 'date-fns';
import { ko } from 'date-fns/locale';

export default function NotificationBell() {
  const [isOpen, setIsOpen] = useState(false);
  const [notifications, setNotifications] = useState<Notification[]>([]);
  const [unreadCount, setUnreadCount] = useState(0);
  const [isConnected, setIsConnected] = useState(false);
  const dropdownRef = useRef<HTMLDivElement>(null);
  const isConnectingRef = useRef(false);

  useEffect(() => {
    let mounted = true;

    const initialize = async () => {
      if (isConnectingRef.current) return;
      isConnectingRef.current = true;

      try {
        await loadNotifications();
        setupSSE();
      } finally {
        if (mounted) {
          isConnectingRef.current = false;
        }
      }
    };

    initialize();

    return () => {
      mounted = false;
      notificationService.disconnectSSE();
    };
  }, []);

  useEffect(() => {
    const handleClickOutside = (event: MouseEvent) => {
      if (dropdownRef.current && !dropdownRef.current.contains(event.target as Node)) {
        setIsOpen(false);
      }
    };

    document.addEventListener('mousedown', handleClickOutside);
    return () => document.removeEventListener('mousedown', handleClickOutside);
  }, []);

  const loadNotifications = async () => {
    try {
      const response = await notificationService.getNotifications();
      setNotifications(response.notifications);
      setUnreadCount(response.count);
    } catch (error) {
      console.error('알림 로딩 실패:', error);
    }
  };

  const setupSSE = () => {
    if (!isConnected) {
      notificationService.connectSSE((event) => {
        loadNotifications();
      });
      setIsConnected(true);
    }
  };

  const formatDate = (dateString: string) => {
    return formatDistanceToNow(new Date(dateString), { addSuffix: true, locale: ko });
  };

  return (
    <div className="relative" ref={dropdownRef}>
      <button
        className="text-2xl text-gray-400 hover:text-indigo-500 relative"
        onClick={() => setIsOpen(!isOpen)}
      >
        <FiBell />
        {unreadCount > 0 && (
          <span className="absolute -top-1 -right-1 bg-red-500 text-white text-xs rounded-full w-5 h-5 flex items-center justify-center">
            {unreadCount}
          </span>
        )}
        {!isConnected && (
          <span className="absolute -bottom-1 -right-1 w-2 h-2 bg-red-500 rounded-full" />
        )}
      </button>

      {isOpen && (
        <div className="absolute right-0 mt-2 w-80 bg-white rounded-lg shadow-lg border border-gray-200 z-50">
          <div className="p-4">
            <div className="flex justify-between items-center mb-4">
              <h3 className="text-lg font-semibold">알림</h3>
              {!isConnected && (
                <span className="text-xs text-red-500">연결 끊김</span>
              )}
            </div>
            {notifications.length > 0 ? (
              <div className="space-y-4">
                {notifications.map((notification, index) => (
                  <div
                    key={index}
                    className={`p-3 rounded-lg ${
                      !notification.confirmed ? 'bg-indigo-50' : 'bg-gray-50'
                    }`}
                  >
                    <p className="text-sm text-gray-800">{notification.content}</p>
                    <p className="text-xs text-gray-500 mt-1">
                      {formatDate(notification.createdAt)}
                    </p>
                  </div>
                ))}
              </div>
            ) : (
              <p className="text-gray-500 text-center py-4">새로운 알림이 없습니다</p>
            )}
          </div>
        </div>
      )}
    </div>
  );
} 