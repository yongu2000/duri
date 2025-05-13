import { EventSourcePolyfill, NativeEventSource } from 'event-source-polyfill';

class SseService {
  private eventSource: EventSource | null = null;
  private statusUpdateHandlers: (() => void)[] = [];
  private reconnectAttempts = 0;
  private maxReconnectAttempts = 5;
  private reconnectDelay = 1000; // 1초

  connect(username: string) {
    if (this.eventSource) {
      console.log('이미 연결되어 있습니다.');
      return;
    }

    const accessToken = localStorage.getItem('accessToken');
    if (!accessToken) {
      console.error('액세스 토큰이 없습니다.');
      return;
    }

    const sseUrl = `${process.env.NEXT_PUBLIC_API_URL || 'http://localhost:8080'}/sse/couple/status`;
    console.log('SSE 연결 시도:', sseUrl);

    const EventSource = EventSourcePolyfill || NativeEventSource;
    this.eventSource = new EventSource(sseUrl, {
      headers: {
        'Authorization': `Bearer ${accessToken}`
      },
      withCredentials: true,
      heartbeatTimeout: 30000 // 30초로 타임아웃 설정
    });

    if (this.eventSource) {
      // 하트비트 메시지 처리
      this.eventSource.addEventListener('heartbeat', (event) => {
        console.log('SSE 하트비트 수신');
      });

      this.eventSource.addEventListener('couple-status', (event) => {
        console.log('SSE 커플 상태 업데이트 수신:', event.data);
        this.statusUpdateHandlers.forEach(handler => {
          try {
            handler();
          } catch (error) {
            console.error('상태 업데이트 핸들러 실행 중 에러:', error);
          }
        });
      });

      this.eventSource.onerror = (error) => {
        console.error('SSE 연결 에러:', error);
        
        if (this.reconnectAttempts < this.maxReconnectAttempts) {
          this.reconnectAttempts++;
          console.log(`재연결 시도 ${this.reconnectAttempts}/${this.maxReconnectAttempts}`);
          
          setTimeout(() => {
            this.disconnect();
            this.connect(username);
          }, this.reconnectDelay * this.reconnectAttempts);
        } else {
          console.error('최대 재연결 시도 횟수 초과');
          this.disconnect();
        }
      };

      this.eventSource.onopen = () => {
        console.log('SSE 연결 성공');
        this.reconnectAttempts = 0;
      };
    }
  }

  disconnect() {
    if (this.eventSource) {
      this.eventSource.close();
      this.eventSource = null;
    }
  }

  addStatusUpdateHandler(handler: () => void) {
    this.statusUpdateHandlers.push(handler);
    return () => {
      this.statusUpdateHandlers = this.statusUpdateHandlers.filter(h => h !== handler);
    };
  }

  isConnected(): boolean {
    return this.eventSource?.readyState === EventSource.OPEN;
  }
}

export const sseService = new SseService(); 