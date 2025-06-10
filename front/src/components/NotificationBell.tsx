import { useState, useEffect, useRef } from 'react';
import { FiBell } from 'react-icons/fi';
import { notificationService } from '@/services/notification';
import { NotificationResponse, NotificationCursor } from '@/types/notification';
import { formatDistanceToNow } from 'date-fns';
import { ko } from 'date-fns/locale';

type NotificationView = 'unconfirmed' | 'confirmed';

export default function NotificationBell() {
  const [isOpen, setIsOpen] = useState(false);
  const [notifications, setNotifications] = useState<NotificationResponse[]>([]);
  const [unreadCount, setUnreadCount] = useState(0);
  const [isConnected, setIsConnected] = useState(false);
  const [view, setView] = useState<NotificationView>('unconfirmed');
  const [hasNext, setHasNext] = useState(false);
  const [nextCursor, setNextCursor] = useState<NotificationCursor | null>(null);
  const dropdownRef = useRef<HTMLDivElement>(null);

  useEffect(() => {
    let mounted = true;

    const initialize = async () => {
      try {
        const count = await notificationService.getUnconfirmedNotificationsCount();
        if (mounted) {
          setUnreadCount(count);
        }
        setupSSE();
      } catch (error) {
        console.error('알림 초기화 실패:', error);
      }
    };

    initialize();

    // SSE 이벤트 수신
    const handleNotificationUpdate = (event: CustomEvent) => {
      console.log('알림 업데이트 이벤트 수신:', event.detail);
      setUnreadCount(event.detail.count);
    };

    window.addEventListener('notificationUpdate', handleNotificationUpdate as EventListener);

    return () => {
      mounted = false;
      notificationService.disconnectSSE();
      window.removeEventListener('notificationUpdate', handleNotificationUpdate as EventListener);
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

  useEffect(() => {
    if (isOpen) {
      loadNotifications();
    }
  }, [view, isOpen]);

  const setupSSE = () => {
    if (!isConnected) {
      notificationService.connectSSE((event) => {
        console.log('SSE 메시지 수신:', event.data);
      });
      setIsConnected(true);
    }
  };

  const loadNotifications = async () => {
    try {
      const response = view === 'unconfirmed' 
        ? await notificationService.getUnconfirmedNotifications()
        : await notificationService.getConfirmedNotifications();
      setNotifications(response.items || []);
      setHasNext(response.hasNext);
      setNextCursor(response.nextCursor);
    } catch (error) {
      console.error('알림 로딩 실패:', error);
      setNotifications([]);
    }
  };

  const loadMoreNotifications = async () => {
    if (!nextCursor) return;
    
    try {
      const response = view === 'unconfirmed'
        ? await notificationService.getUnconfirmedNotifications(nextCursor)
        : await notificationService.getConfirmedNotifications(nextCursor);
      
      setNotifications(prev => [...prev, ...(response.items || [])]);
      setHasNext(response.hasNext);
      setNextCursor(response.nextCursor);
    } catch (error) {
      console.error('추가 알림 로딩 실패:', error);
    }
  };

  const handleDeleteAll = async () => {
    try {
      await notificationService.deleteAllNotifications();
      setNotifications([]);
      setUnreadCount(0);
    } catch (error) {
      console.error('알림 삭제 실패:', error);
    }
  };

  const handleDeleteNotification = async (notificationId: string) => {
    try {
      await notificationService.deleteNotification(notificationId);
      setNotifications(notifications.filter(n => n.id !== notificationId));
      if (view === 'unconfirmed') {
        setUnreadCount(prev => Math.max(0, prev - 1));
      }
    } catch (error) {
      console.error('알림 삭제 실패:', error);
    }
  };

  const formatDate = (dateString: string) => {
    return formatDistanceToNow(new Date(dateString), { addSuffix: true, locale: ko });
  };

  const handleViewChange = (newView: NotificationView) => {
    setView(newView);
  };

  return (
    <div className="relative" ref={dropdownRef}>
      <button
        className="text-2xl text-gray-400 hover:text-indigo-500 relative"
        onClick={() => {
          setIsOpen(!isOpen);
        }}
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
          <div className="p-4 border-b">
            <div className="flex justify-between items-center">
              <h3 className="text-lg font-semibold">알림</h3>
              <button
                onClick={handleDeleteAll}
                className="text-sm text-red-500 hover:text-red-700"
              >
                모두 지우기
              </button>
            </div>
          </div>

          <div className="max-h-96 overflow-y-auto">
            {notifications && notifications.length > 0 ? (
              <>
                {notifications.map((notification) => (
                  <div
                    key={notification.id}
                    className={`p-4 border-b hover:bg-gray-50 ${
                      view === 'confirmed' ? 'bg-gray-50' : ''
                    }`}
                  >
                    <div className="flex justify-between items-start">
                      <div>
                        <p className="font-medium">{notification.fromUser}</p>
                        <p className="text-sm text-gray-600">{notification.content}</p>
                        <p className="text-xs text-gray-400">
                          {formatDate(notification.createdAt)}
                        </p>
                      </div>
                      <button
                        onClick={() => handleDeleteNotification(notification.id)}
                        className="text-gray-400 hover:text-gray-600"
                      >
                        ×
                      </button>
                    </div>
                  </div>
                ))}
                {hasNext && (
                  <button
                    onClick={loadMoreNotifications}
                    className="w-full p-2 text-center text-sm text-gray-500 hover:text-gray-700 hover:bg-gray-50"
                  >
                    더보기
                  </button>
                )}
              </>
            ) : (
              <p className="text-gray-500 text-center py-4">새로운 알림이 없습니다</p>
            )}
          </div>

          <div className="p-4 border-t">
            <button
              onClick={() => handleViewChange(view === 'unconfirmed' ? 'confirmed' : 'unconfirmed')}
              className="w-full text-center text-sm text-gray-500 hover:text-gray-700"
            >
              {view === 'unconfirmed' ? '읽은 알림 보기' : '읽지 않은 알림 보기'}
            </button>
          </div>
        </div>
      )}
    </div>
  );
} 