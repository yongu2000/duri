import { 
  NotificationCursor, 
  NotificationResponse, 
  CursorResponse,
  UnconfirmedNotificationsCountResponse 
} from '@/types/notification';
import { axiosInstance } from './axios';
import { EventSourcePolyfill, NativeEventSource } from 'event-source-polyfill';

class NotificationService {
  private sseConnection: EventSource | null = null;
  private reconnectAttempts = 0;
  private maxReconnectAttempts = 5;
  private reconnectDelay = 1000; // 1초
  private isConnecting = false;

  async getUnconfirmedNotifications(cursor?: NotificationCursor, size: number = 10): Promise<CursorResponse<NotificationResponse, NotificationCursor>> {
    const params = new URLSearchParams();
    if (cursor) {
      params.append('createdAt', cursor.createdAt);
      params.append('id', cursor.id);
    }
    params.append('size', size.toString());
    
    const response = await axiosInstance.get<CursorResponse<NotificationResponse, NotificationCursor>>(
      `/notification/unconfirmed?${params.toString()}`
    );
    return response.data;
  }

  async getConfirmedNotifications(cursor?: NotificationCursor, size: number = 10): Promise<CursorResponse<NotificationResponse, NotificationCursor>> {
    const params = new URLSearchParams();
    if (cursor) {
      params.append('createdAt', cursor.createdAt);
      params.append('id', cursor.id);
    }
    params.append('size', size.toString());
    
    const response = await axiosInstance.get<CursorResponse<NotificationResponse, NotificationCursor>>(
      `/notification/confirmed?${params.toString()}`
    );
    return response.data;
  }

  async getUnconfirmedNotificationsCount(): Promise<number> {
    const response = await axiosInstance.get<UnconfirmedNotificationsCountResponse>('/notification/unconfirmed/count');
    return response.data.count;
  }

  async getPendingPostsCount(coupleCode: string): Promise<number> {
    const response = await axiosInstance.get<{ count: number }>(`/post/pending/${coupleCode}/count`);
    return response.data.count;
  }

  async deleteNotification(notificationId: string): Promise<void> {
    await axiosInstance.delete(`/notification/delete/${notificationId}`);
  }

  async deleteAllNotifications(): Promise<void> {
    await axiosInstance.delete('/notification/delete/all');
  }

  connectSSE(onMessage: (event: MessageEvent) => void) {
    if (this.sseConnection || this.isConnecting) {
      console.log('이미 SSE가 연결되어 있거나 연결 중입니다.');
      return;
    }

    const accessToken = localStorage.getItem('accessToken');
    if (!accessToken) {
      console.error('액세스 토큰이 없습니다.');
      return;
    }

    this.isConnecting = true;
    const sseUrl = `${process.env.NEXT_PUBLIC_API_URL || 'http://localhost:8080'}/notification/sse`;
    console.log('알림 SSE 연결 시도:', sseUrl);

    try {
      const EventSource = EventSourcePolyfill || NativeEventSource;
      this.sseConnection = new EventSource(sseUrl, {
        headers: {
          'Authorization': `Bearer ${accessToken}`,
          'Content-Type': 'text/event-stream',
          'Cache-Control': 'no-cache',
          'Connection': 'keep-alive'
        },
        withCredentials: true
      });

      if (this.sseConnection) {
        // 연결 성공 메시지 처리
        this.sseConnection.addEventListener('connect', (event) => {
          console.log('알림 SSE 연결 성공');
          this.reconnectAttempts = 0;
          this.isConnecting = false;
        });

        // 모든 이벤트를 수신
        this.sseConnection.onmessage = async (event) => {
          console.log('새로운 알림 수신:', event.data);
          
          try {
            // 알림 카운트 업데이트
            const unconfirmedCount = await this.getUnconfirmedNotificationsCount();
            window.dispatchEvent(new CustomEvent('notificationUpdate', {
              detail: { count: unconfirmedCount }
            }));

            // 미완성 게시물 카운트 업데이트
            const coupleCode = localStorage.getItem('coupleCode');
            if (coupleCode) {
              const pendingCount = await this.getPendingPostsCount(coupleCode);
              window.dispatchEvent(new CustomEvent('pendingPostsUpdate', {
                detail: { count: pendingCount }
              }));
            }
          } catch (error) {
            console.error('카운트 업데이트 실패:', error);
          }

          onMessage(event);
        };

        this.sseConnection.onerror = (error) => {
          console.error('알림 SSE 연결 에러:', error);
          
          if (this.reconnectAttempts < this.maxReconnectAttempts) {
            this.reconnectAttempts++;
            console.log(`재연결 시도 ${this.reconnectAttempts}/${this.maxReconnectAttempts}`);
            
            setTimeout(() => {
              this.disconnectSSE();
              this.connectSSE(onMessage);
            }, this.reconnectDelay * this.reconnectAttempts);
          } else {
            console.error('최대 재연결 시도 횟수 초과');
            this.disconnectSSE();
          }
        };
      }
    } catch (error) {
      console.error('SSE 연결 생성 중 에러:', error);
      this.isConnecting = false;
    }
  }

  disconnectSSE() {
    if (this.sseConnection) {
      this.sseConnection.close();
      this.sseConnection = null;
    }
    this.isConnecting = false;
  }

  isConnected(): boolean {
    return this.sseConnection?.readyState === EventSource.OPEN;
  }
}

export const notificationService = new NotificationService(); 