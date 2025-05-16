import { axiosInstance } from './axios';

export interface ImageUploadResponse {
  imageUrl: string;
}

export const imageService = {
  async uploadImage(file: File): Promise<string> {
    const formData = new FormData();
    formData.append('imageFile', file);
    
    const response = await axiosInstance.post<ImageUploadResponse>('/image/upload', formData, {
      headers: {
        'Content-Type': 'multipart/form-data',
      },
    });
    
    return response.data.imageUrl;
  },

  async deleteImage(imageUrl: string): Promise<void> {
    try {
      await axiosInstance.delete(`/image/delete/${encodeURIComponent(imageUrl)}`);
    } catch (error) {
      console.error('이미지 삭제 중 오류 발생:', error);
      throw error;
    }
  }
};

export default imageService; 