import { axiosInstance } from './axios';
import { 
  ConnectionStatusResponse, 
  ConnectionCodeResponse,
  ConnectionRequestResponse 
} from '@/types/coupleConnect';

export const coupleConnectService = {
  // 인증코드 발급/조회
  async getCode(): Promise<string> {
    const response = await axiosInstance.get<ConnectionCodeResponse>('/couple/connect/code');
    return response.data.code;
  },

  // 보낸 요청 상태 조회
  async getSentStatus(): Promise<ConnectionStatusResponse> {
    const response = await axiosInstance.get<ConnectionStatusResponse>('/couple/connect/status/send');
    return response.data;
  },

  // 받은 요청 상태 조회
  async getReceivedStatus(): Promise<ConnectionStatusResponse> {
    const response = await axiosInstance.get<ConnectionStatusResponse>('/couple/connect/status/receive');
    return response.data;
  },

  // 상태 확인
  async confirmStatus(): Promise<void> {
    await axiosInstance.post('/couple/connect/status/confirm');
  },

  // 연결 요청 보내기
  async sendRequest(code: string): Promise<ConnectionRequestResponse> {
    const response = await axiosInstance.post<ConnectionRequestResponse>('/couple/connect', { code });
    return response.data;
  },

  // 연결 요청 수락
  async acceptRequest(): Promise<void> {
    await axiosInstance.post('/couple/connect/accept');
  },

  // 연결 요청 거절
  async rejectRequest(): Promise<void> {
    await axiosInstance.post('/couple/connect/reject');
  },

  // 연결 요청 취소
  async cancelRequest(): Promise<void> {
    await axiosInstance.post('/couple/connect/cancel');
  }
};

export default coupleConnectService; 