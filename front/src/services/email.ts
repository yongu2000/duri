import { axiosInstance } from './axios';


interface VerifyCodeResponse {
  verified: boolean;
}

export const emailService = {
  sendVerificationCode: async (email: string) => {
    const response = await axiosInstance.post<null>('/email/send/code', { email });
    return response.data;
  },

  verifyCode: async (email: string, code: string): Promise<VerifyCodeResponse> => {
    const response = await axiosInstance.post('/email/verify/code', { email, code });
    return response.data;
  },

  sendPasswordResetEmail: async (email: string): Promise<null> => {
    await axiosInstance.post('/email/send/password/reset', { email });
    return null;
  },
}; 