import { axiosInstance } from './axios';
import { PostFormData } from '@/types/post';

interface PostCreateRequest {
  title: string;
  placeName: string;
  placeUrl: string;
  category: string;
  phone: string;
  address: string;
  roadAddress: string;
  x: number;
  y: number;
  date: string;
  rate: number;
  comment: string;
  scope: 'PUBLIC' | 'PRIVATE';
}

class PostService {
  async createPost(formData: PostFormData): Promise<void> {
    const requestData: PostCreateRequest = {
      title: formData.title.trim() || formData.subject,
      placeName: formData.subject,
      placeUrl: formData.placeUrl,
      category: formData.category,
      phone: formData.phone,
      address: formData.address,
      roadAddress: formData.roadAddress,
      x: formData.x,
      y: formData.y,
      date: formData.date,
      rate: formData.rating,
      comment: formData.comment,
      scope: formData.scope
    };

    try {
      await axiosInstance.post('/post/create', requestData, {
        headers: {
          'Content-Type': 'application/json',
        },
      });
    } catch (error) {
      console.error('게시글 작성 중 오류 발생:', error);
      throw error;
    }
  }
}

export const postService = new PostService(); 