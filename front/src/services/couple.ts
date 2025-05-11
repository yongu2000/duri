import { axiosInstance } from './axios';

export interface CoupleProfileResponse {
  coupleName: string;
  userLeftName: string;
  userLeftProfileImageUrl: string;
  userRightName: string;
  userRightProfileImageUrl: string;
  bio: string;
}

export const coupleService = {
  async getCoupleProfile(coupleCode: string): Promise<CoupleProfileResponse> {
    const res = await axiosInstance.get(`/couple/profile/${coupleCode}`);
    return res.data;
  },
}; 