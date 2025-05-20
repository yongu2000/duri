import { axiosInstance } from './axios';
import { PostFormData, PostSearchOptions, CursorResponse, CompletePostResponse, PostImageUrlResponse } from '@/types/post';
import { imageService } from './image';

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
  imageUrls: string[];
}

interface PendingPostCountResponse {
  count: number;
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
      scope: formData.scope,
      imageUrls: formData.imageUrls || []
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

  async getCompletePosts(
    coupleCode: string,
    cursor?: { date: string; rate: number; idToken: string },
    size: number = 10,
    searchOptions?: PostSearchOptions
  ): Promise<CursorResponse<CompletePostResponse>> {
    try {
      const params = new URLSearchParams();
      if (cursor) {
        params.append('date', cursor.date);
        params.append('rate', cursor.rate.toString());
        params.append('idToken', cursor.idToken);
      }
      params.append('size', size.toString());
      
      if (searchOptions) {
        if (searchOptions.searchKeyword) params.append('searchKeyword', searchOptions.searchKeyword);
        if (searchOptions.startDate) params.append('startDate', searchOptions.startDate);
        if (searchOptions.endDate) params.append('endDate', searchOptions.endDate);
        if (searchOptions.sortBy) params.append('sortBy', searchOptions.sortBy);
        if (searchOptions.sortDirection) params.append('sortDirection', searchOptions.sortDirection);
      }

      const response = await axiosInstance.get<CursorResponse<CompletePostResponse>>(
        `/post/complete/${coupleCode}?${params.toString()}`
      );
      return response.data;
    } catch (error) {
      console.error('게시글 목록 조회 중 오류 발생:', error);
      throw error;
    }
  }

  async getPostImages(postIdToken: string): Promise<string[]> {
    try {
      const response = await axiosInstance.get<PostImageUrlResponse[]>(`/post/image`, {
        params: { postIdToken }
      });
      return response.data.map(img => img.imageUrl);
    } catch (error) {
      console.error('이미지 로드 실패:', error);
      throw error;
    }
  }

  async getPendingPostsCount(coupleCode: string): Promise<PendingPostCountResponse> {
    try {
      const response = await axiosInstance.get<PendingPostCountResponse>(`/post/pending/${coupleCode}/count`);
      return response.data;
    } catch (error) {
      console.error('미완성 게시물 수 조회 중 오류 발생:', error);
      throw error;
    }
  }

  async getPendingPosts(
    coupleCode: string,
    cursor?: { date: string; rate: number; idToken: string },
    size: number = 10,
    searchOptions?: PostSearchOptions
  ): Promise<CursorResponse<CompletePostResponse>> {
    try {
      const params = new URLSearchParams();
      if (cursor) {
        params.append('date', cursor.date);
        params.append('rate', cursor.rate.toString());
        params.append('idToken', cursor.idToken);
      }
      params.append('size', size.toString());
      
      if (searchOptions) {
        if (searchOptions.searchKeyword) params.append('searchKeyword', searchOptions.searchKeyword);
        if (searchOptions.startDate) params.append('startDate', searchOptions.startDate);
        if (searchOptions.endDate) params.append('endDate', searchOptions.endDate);
        if (searchOptions.sortBy) params.append('sortBy', searchOptions.sortBy);
        if (searchOptions.sortDirection) params.append('sortDirection', searchOptions.sortDirection);
      }

      const response = await axiosInstance.get<CursorResponse<CompletePostResponse>>(
        `/post/pending/${coupleCode}?${params.toString()}`
      );
      return response.data;
    } catch (error) {
      console.error('미완성 게시글 목록 조회 중 오류 발생:', error);
      throw error;
    }
  }
}

export const postService = new PostService(); 