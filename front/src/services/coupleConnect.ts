import { axiosInstance } from './axios';
import { 
  ConnectionStatusResponse, 
  ConnectionCodeResponse,
  ConnectionRequestResponse 
} from '@/types/coupleConnect';
import { EventSourcePolyfill, NativeEventSource } from 'event-source-polyfill';

class CoupleConnectService {
  private sseConnection: EventSource | null = null;
  private reconnectAttempts = 0;
  private maxReconnectAttempts = 5;
  private reconnectDelay = 1000; // 1초
  private isConnecting = false;
  private lastHeartbeat = Date.now();
  private heartbeatCheckInterval: NodeJS.Timeout | null = null;
  private statusUpdateHandlers: (() => void)[] = [];

  // 인증코드 발급/조회
  async getCode(): Promise<string> {
    const response = await axiosInstance.get<ConnectionCodeResponse>('/couple/connect/code');
    return response.data.code;
  }

  // 보낸 요청 상태 조회
  async getSentStatus(): Promise<ConnectionStatusResponse> {
    const response = await axiosInstance.get<ConnectionStatusResponse>('/couple/connect/status/send');
    return response.data;
  }

  // 받은 요청 상태 조회
  async getReceivedStatus(): Promise<ConnectionStatusResponse> {
    const response = await axiosInstance.get<ConnectionStatusResponse>('/couple/connect/status/receive');
    return response.data;
  }

  // 상태 확인
  async confirmStatus(): Promise<void> {
    await axiosInstance.post('/couple/connect/status/confirm');
  }

  // 연결 요청 보내기
  async sendRequest(code: string): Promise<ConnectionRequestResponse> {
    const response = await axiosInstance.post<ConnectionRequestResponse>('/couple/connect', { code });
    return response.data;
  }

  // 연결 요청 수락
  async acceptRequest(): Promise<void> {
    await axiosInstance.post('/couple/connect/accept');
  }

  // 연결 요청 거절
  async rejectRequest(): Promise<void> {
    await axiosInstance.post('/couple/connect/reject');
  }

  // 연결 요청 취소
  async cancelRequest(): Promise<void> {
    await axiosInstance.post('/couple/connect/cancel');
  }

  connectSSE() {
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
    const sseUrl = `${process.env.NEXT_PUBLIC_API_URL || 'http://localhost:8080'}/sse/couple/status`;
    console.log('커플 상태 SSE 연결 시도:', sseUrl);

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
        heartbeatTimeout: 35000
      });

      if (this.sseConnection) {
        this.sseConnection.addEventListener('heartbeat', (event) => {
          console.log('커플 상태 SSE 하트비트 수신');
          this.lastHeartbeat = Date.now();
        });
        
        // 연결 성공 메시지 처리
        this.sseConnection.addEventListener('connect', (event) => {
          console.log('알림 SSE 연결 성공');
          this.lastHeartbeat = Date.now();
        });

        this.sseConnection.addEventListener('couple-status', (event) => {
          console.log('커플 상태 업데이트 수신:', event.data);
          this.statusUpdateHandlers.forEach(handler => {
            try {
              handler();
            } catch (error) {
              console.error('상태 업데이트 핸들러 실행 중 에러:', error);
            }
          });
        });

        this.startHeartbeatCheck();

        this.sseConnection.onerror = (error) => {
          console.error('커플 상태 SSE 연결 에러:', error);
          
          if (this.reconnectAttempts < this.maxReconnectAttempts) {
            this.reconnectAttempts++;
            console.log(`재연결 시도 ${this.reconnectAttempts}/${this.maxReconnectAttempts}`);
            
            setTimeout(() => {
              this.disconnectSSE();
              this.connectSSE();
            }, this.reconnectDelay * this.reconnectAttempts);
          } else {
            console.error('최대 재연결 시도 횟수 초과');
            this.disconnectSSE();
          }
        };

        this.sseConnection.onopen = () => {
          console.log('커플 상태 SSE 연결 성공');
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
      if (now - this.lastHeartbeat > 35000) {
        console.log('하트비트 타임아웃, 재연결 시도');
        this.disconnectSSE();
        this.connectSSE();
      }
    }, 5000);
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

  addStatusUpdateHandler(handler: () => void) {
    this.statusUpdateHandlers.push(handler);
    return () => {
      this.statusUpdateHandlers = this.statusUpdateHandlers.filter(h => h !== handler);
    };
  }

  isConnected(): boolean {
    return this.sseConnection?.readyState === EventSource.OPEN;
  }
}

export const coupleConnectService = new CoupleConnectService();
export default coupleConnectService; 