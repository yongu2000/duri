import { axiosInstance } from './axios';
import { PostFormData, PostSearchOptions, CursorResponse, PostResponse, PostImageUrlResponse } from '@/types/post';
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
    cursor?: { date: string; rate: number | null; idToken: string },
    size: number = 10,
    searchOptions?: PostSearchOptions
  ): Promise<CursorResponse<PostResponse>> {
    try {
      const params = new URLSearchParams();
      if (cursor) {
        params.append('date', cursor.date);
        params.append('rate', cursor.rate?.toString() ?? '');
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

      const response = await axiosInstance.get<CursorResponse<PostResponse>>(
        `/post/complete/${coupleCode}?${params.toString()}`
      );
      return response.data;
    } catch (error) {
      console.error('게시글 목록 조회 중 오류 발생:', error);
      throw error;
    }
  }

  async getCompleteCommunityPosts(
    cursor?: { date: string; rate: number | null; idToken: string },
    size: number = 10,
    searchOptions?: PostSearchOptions
  ): Promise<CursorResponse<PostResponse>> {
    try {
      const params = new URLSearchParams();
      if (cursor) {
        params.append('date', cursor.date);
        params.append('rate', cursor.rate?.toString() ?? '');
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

      const response = await axiosInstance.get<CursorResponse<PostResponse>>(
        `/post/complete?${params.toString()}`
      );
      return response.data;
    } catch (error) {
      console.error('커뮤니티 게시글 목록 조회 중 오류 발생:', error);
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
    cursor?: { date: string; rate: number | null; idToken: string },
    size: number = 10,
    searchOptions?: PostSearchOptions
  ): Promise<CursorResponse<PostResponse>> {
    try {
      const params = new URLSearchParams();
      if (cursor) {
        if (cursor.date) params.append('date', cursor.date);
        params.append('rate', (cursor.rate ?? 0).toString());
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

      console.log('Request URL:', `/post/pending/${coupleCode}?${params.toString()}`);
      const response = await axiosInstance.get<CursorResponse<PostResponse>>(
        `/post/pending/${coupleCode}?${params.toString()}`
      );
      return response.data;
    } catch (error) {
      console.error('미완성 게시글 목록 조회 중 오류 발생:', error);
      throw error;
    }
  }

  async getPost(postIdToken: string): Promise<PostResponse> {
    try {
      const response = await axiosInstance.get<PostResponse>(`/post/edit`, {
        params: { postIdToken }
      });
      return response.data;
    } catch (error) {
      console.error('게시글 조회 중 오류 발생:', error);
      throw error;
    }
  }

  async updatePost(postId: string, formData: PostFormData): Promise<void> {
    const requestData: Record<string, any> = {
      idToken: postId
    };

    if (formData.subject?.trim()) requestData.placeName = formData.subject;
    if (formData.title?.trim()) requestData.title = formData.title;
    if (formData.placeUrl?.trim()) requestData.placeUrl = formData.placeUrl;
    if (formData.category?.trim()) requestData.category = formData.category;
    if (formData.phone?.trim()) requestData.phone = formData.phone;
    if (formData.address?.trim()) requestData.address = formData.address;
    if (formData.roadAddress?.trim()) requestData.roadAddress = formData.roadAddress;
    if (formData.x !== 0) requestData.x = formData.x;
    if (formData.y !== 0) requestData.y = formData.y;
    if (formData.date?.trim()) requestData.date = formData.date;
    if (formData.rating !== 0) requestData.rate = formData.rating;
    if (formData.comment?.trim()) requestData.comment = formData.comment;
    if (formData.scope) requestData.scope = formData.scope;
    if (formData.imageUrls.length > 0) requestData.imageUrls = formData.imageUrls;

    try {
      await axiosInstance.put('/post/edit', requestData, {
        headers: {
          'Content-Type': 'application/json',
        },
      });
    } catch (error) {
      console.error('게시글 수정 중 오류 발생:', error);
      throw error;
    }
  }

  async getLikedPosts(
    cursor?: { date: string; rate: number | null; idToken: string },
    size: number = 10,
    searchOptions?: PostSearchOptions
  ): Promise<CursorResponse<PostResponse>> {
    // TODO: 백엔드 API 구현 후 구현 예정
    throw new Error('아직 구현되지 않은 기능입니다.');
  }

  async likePost(postIdToken: string): Promise<void> {
    await axiosInstance.post('/post/like', { postIdToken });
  }

  async dislikePost(postIdToken: string): Promise<void> {
    await axiosInstance.post('/post/dislike', { postIdToken });
  }

  async getLikeStatus(postIdToken: string): Promise<{ liked: boolean }> {
    const response = await axiosInstance.get('/post/like/status', {
      params: { postIdToken }
    });
    return response.data;
  }
}

export const postService = new PostService(); 