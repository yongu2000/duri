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
  }
};

export default imageService; 