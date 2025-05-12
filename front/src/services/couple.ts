import { axiosInstance } from './axios';

export interface CoupleProfileResponse {
  coupleName: string;
  coupleCode: string;
  userLeftName: string;
  userLeftGender: 'MALE' | 'FEMALE';
  userLeftBirthday: string;
  userLeftProfileImageUrl: string;
  userRightName: string;
  userRightGender: 'MALE' | 'FEMALE';
  userRightBirthday: string;
  userRightProfileImageUrl: string;
  bio: string;
}

export interface CoupleEditProfileResponse {
  coupleName: string;
  coupleCode: string;
  bio: string;
}

export const coupleService = {
  async getCoupleProfile(coupleCode: string): Promise<CoupleProfileResponse> {
    const res = await axiosInstance.get(`/couple/profile/${coupleCode}`);
    return res.data;
  },
  async getCoupleEditProfile(coupleCode: string): Promise<CoupleEditProfileResponse> {
    const res = await axiosInstance.get(`/couple/profile/${coupleCode}/edit`);
    return res.data;
  },
  async updateCoupleProfile(coupleCode: string, data: { coupleName: string; bio: string; coupleCode: string }): Promise<void> {
    await axiosInstance.put(`/couple/profile/${coupleCode}/edit`, data);
  },
  async checkCoupleCodeDuplicate(coupleCode: string): Promise<boolean> {
    const res = await axiosInstance.get(`/couple/check/code/${coupleCode}`);
    return res.data === true;
  },
}; 