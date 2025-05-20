import { MainNotificationResponse } from '@/types/notification';
import { axiosInstance } from './axios';
import { EventSourcePolyfill, NativeEventSource } from 'event-source-polyfill';

class NotificationService {
  private sseConnection: EventSource | null = null;
  private reconnectAttempts = 0;
  private maxReconnectAttempts = 5;
  private reconnectDelay = 1000; // 1초
  private isConnecting = false;
  private lastHeartbeat = Date.now();
  private heartbeatCheckInterval: NodeJS.Timeout | null = null;

  async getNotifications(): Promise<MainNotificationResponse> {
    const response = await axiosInstance.get<MainNotificationResponse>('/notification/main');
    return response.data;
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
        withCredentials: true,
        heartbeatTimeout: 30000 // 30초로 타임아웃 설정
      });

      if (this.sseConnection) {
        // 하트비트 메시지 처리
        this.sseConnection.addEventListener('heartbeat', (event) => {
          console.log('알림 SSE 하트비트 수신');
          this.lastHeartbeat = Date.now();
        });

        // 연결 성공 메시지 처리
        this.sseConnection.addEventListener('connect', (event) => {
          console.log('알림 SSE 연결 성공');
          this.lastHeartbeat = Date.now();
        });

        // 알림 이벤트 처리
        this.sseConnection.addEventListener('POST', (event) => {
          console.log('새로운 알림 수신:', event.data);
          onMessage(event);
        });

        // 하트비트 체크 시작
        this.startHeartbeatCheck();

        this.sseConnection.onerror = (error) => {
          console.error('알림 SSE 연결 에러:', {
            error,
            readyState: this.sseConnection?.readyState,
            url: sseUrl,
            headers: {
              'Authorization': 'Bearer ' + accessToken.substring(0, 10) + '...',
              'withCredentials': true
            }
          });
          
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

        this.sseConnection.onopen = () => {
          console.log('알림 SSE 연결 성공');
          this.reconnectAttempts = 0;
          this.isConnecting = false;
          this.lastHeartbeat = Date.now();
        };
      }
    } catch (error) {
      console.error('SSE 연결 생성 중 에러:', error);
      this.isConnecting = false;
    }
  }

  private startHeartbeatCheck() {
    if (this.heartbeatCheckInterval) {
      clearInterval(this.heartbeatCheckInterval);
    }

    this.heartbeatCheckInterval = setInterval(() => {
      const now = Date.now();
      if (now - this.lastHeartbeat > 35000) { // 35초 이상 하트비트가 없으면
        console.log('하트비트 타임아웃, 재연결 시도');
        this.disconnectSSE();
        this.connectSSE(() => {});
      }
    }, 5000); // 5초마다 체크
  }

  disconnectSSE() {
    if (this.heartbeatCheckInterval) {
      clearInterval(this.heartbeatCheckInterval);
      this.heartbeatCheckInterval = null;
    }

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