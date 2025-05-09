import { Client, Message, StompSubscription } from '@stomp/stompjs';
import { ConnectionStatusResponse } from '@/types/coupleConnect';

class WebSocketService {
  private stompClient: Client | null = null;
  private messageHandlers: ((message: ConnectionStatusResponse) => void)[] = [];
  private statusUpdateHandlers: (() => void)[] = [];
  private username: string | null = null;
  private reconnectAttempts = 0;
  private maxReconnectAttempts = 5;
  private reconnectTimeout: NodeJS.Timeout | null = null;
  private subscriptions: StompSubscription[] = [];

  connect(username: string) {
    if (this.stompClient?.connected) {
      console.log('이미 연결되어 있습니다.');
      return;
    }
    this.username = username;

    // JWT 토큰 가져오기
    const accessToken = localStorage.getItem('accessToken');
    if (!accessToken) {
      console.error('액세스 토큰이 없습니다.');
      return;
    }

    console.log('웹소켓 연결 시도:', {
      username,
      wsUrl: process.env.NEXT_PUBLIC_WS_URL || 'ws://localhost:8080/ws',
      hasToken: !!accessToken
    });
    
    // STOMP 클라이언트 설정
    this.stompClient = new Client({
      brokerURL: `${process.env.NEXT_PUBLIC_WS_URL || 'ws://localhost:8080'}/ws`,
      connectHeaders: {
        Authorization: `Bearer ${accessToken}`
      },
      debug: (str: string) => {
        console.log('STOMP 디버그:', str);
      },
      reconnectDelay: 5000,
      heartbeatIncoming: 4000,
      heartbeatOutgoing: 4000,
    });

    // 연결 성공 시 콜백
    this.stompClient.onConnect = () => {
      console.log('STOMP 연결 성공');
      this.reconnectAttempts = 0;

      // 구독 설정
      if (this.username && this.stompClient) {
        // 상태 업데이트 구독
        const statusTopic = `/topic/couple/status${this.username}`;
        console.log('상태 업데이트 구독 시도:', statusTopic);
        
        const statusSubscription = this.stompClient.subscribe(
          statusTopic,
          (message: Message) => {
            console.log('상태 업데이트 메시지 수신:', {
              topic: statusTopic,
              body: message.body,
              headers: message.headers
            });
            
            // 상태 업데이트 핸들러 호출
            this.statusUpdateHandlers.forEach(handler => {
              try {
                handler();
              } catch (error) {
                console.error('상태 업데이트 핸들러 실행 중 에러:', error);
              }
            });
          }
        );
        this.subscriptions.push(statusSubscription);
      }
    };

    // 연결 실패 시 콜백
    this.stompClient.onStompError = (frame: { headers: { [key: string]: string }, body: string }) => {
      console.error('STOMP 에러:', {
        headers: frame.headers,
        body: frame.body
      });
    };

    // 연결 종료 시 콜백
    this.stompClient.onWebSocketClose = () => {
      console.log('웹소켓 연결 종료');
      this.subscriptions.forEach(sub => {
        try {
          sub.unsubscribe();
        } catch (error) {
          console.error('구독 해제 중 에러:', error);
        }
      });
      this.subscriptions = [];
      
      // 정상적인 종료가 아닌 경우에만 재연결 시도
      if (this.reconnectAttempts < this.maxReconnectAttempts) {
        this.reconnectAttempts++;
        const delay = Math.min(1000 * Math.pow(2, this.reconnectAttempts), 30000);
        console.log(`재연결 시도: ${delay}ms 후 (시도 ${this.reconnectAttempts}/${this.maxReconnectAttempts})`);
        
        this.reconnectTimeout = setTimeout(() => {
          if (this.username) {
            this.connect(this.username);
          }
        }, delay);
      }
    };

    // STOMP 클라이언트 연결
    console.log('STOMP 클라이언트 활성화 시도');
    this.stompClient.activate();
  }

  disconnect() {
    if (this.reconnectTimeout) {
      clearTimeout(this.reconnectTimeout);
      this.reconnectTimeout = null;
    }
    
    this.subscriptions.forEach(sub => sub.unsubscribe());
    this.subscriptions = [];
    
    if (this.stompClient) {
      this.stompClient.deactivate();
      this.stompClient = null;
      this.username = null;
      this.reconnectAttempts = 0;
    }
  }

  addMessageHandler(handler: (message: ConnectionStatusResponse) => void) {
    this.messageHandlers.push(handler);
    return () => {
      this.messageHandlers = this.messageHandlers.filter(h => h !== handler);
    };
  }

  addStatusUpdateHandler(handler: () => void) {
    this.statusUpdateHandlers.push(handler);
    return () => {
      this.statusUpdateHandlers = this.statusUpdateHandlers.filter(h => h !== handler);
    };
  }

  isConnected(): boolean {
    return this.stompClient?.connected ?? false;
  }
}

export const websocketService = new WebSocketService(); 