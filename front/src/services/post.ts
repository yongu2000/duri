import { axiosInstance } from './axios';
import { Post, PostPage, PostSummary, CursorResponse, CommentResponse, CommentRepliesResponse } from '@/types/post';

export interface PostSearchParams {
  page?: number;
  size?: number;
  searchType?: 'title' | 'content' | 'author';
  searchKeyword?: string;
  sortBy?: 'date' | 'likes' | 'comments' | 'views';
  sortDirection?: 'asc' | 'desc';
  minViewCounts?: number;
  minCommentCounts?: number;
  minLikes?: number;
  startDate?: string;  // YYYY-MM-DD 형식
  endDate?: string;    // YYYY-MM-DD 형식
}

export const postService = {
  async getMyPosts(): Promise<Post[]> {
    const response = await axiosInstance.get('/posts/my');
    return response.data;
  },

  async getAllPostsByPage(page = 0, size = 10): Promise<PostPage> {
    const response = await axiosInstance.get(`/posts/list?page=${page}&size=${size}`);
    return response.data;
},

async getAllPostsByCursor(cursor?: number, searchParams?: PostSearchParams) {
  const params = new URLSearchParams();
  if (cursor) params.append('cursor', cursor.toString());
  if (searchParams) {
    Object.entries(searchParams).forEach(([key, value]) => {
      if (value !== undefined && value !== '') {
        params.append(key, value.toString());
      }
    });
  }
  const response = await axiosInstance.get(`/posts/grid?${params.toString()}`);
  return response.data;
},

  async getPost(id: number): Promise<Post> {
    const response = await axiosInstance.get(`/posts/${id}`);
    return response.data;
  },

  async createPost(data: { title: string; content: string }): Promise<Post> {
    const response = await axiosInstance.post('/posts', data);
    return response.data;
  },

  async updatePost(id: number, data: { title: string; content: string }): Promise<Post> {
    const response = await axiosInstance.put(`/posts/${id}`, data);
    return response.data;
  },

  async deletePost(id: number): Promise<void> {
    await axiosInstance.delete(`/posts/${id}`);
  },

  async likePost(postId: number): Promise<void> {
    await axiosInstance.post(`/posts/${postId}/like`);
  },

  async dislikePost(postId: number): Promise<void> {
    await axiosInstance.post(`/posts/${postId}/dislike`);
  },

  async getLikeStatus(postId: number): Promise<boolean> {
    try {
        const response = await axiosInstance.get(`/posts/${postId}/like/status`);
        return response.data.liked;
    } catch (error) {
        return false;
    }
  },

  async createComment(postId: number, data: { content: string; parentCommentId: number | null }): Promise<void> {
    await axiosInstance.post(`/comments/${postId}`, data);
  },

  async updateComment(commentId: number, content: string): Promise<void> {
    await axiosInstance.put(`/comments/${commentId}`, { content });
  },

  async deleteComment(commentId: number): Promise<void> {
    await axiosInstance.delete(`/comments/${commentId}`);
  },

  async getFilteredPosts(params: PostSearchParams): Promise<PostPage> {
    const searchParams = new URLSearchParams();
    
    // 페이지네이션
    searchParams.append('page', (params.page || 0).toString());
    searchParams.append('size', (params.size || 10).toString());
    
    // 검색 조건
    if (params.searchType) {
      searchParams.append('searchType', params.searchType);
    }
    if (params.searchKeyword) {
      searchParams.append('searchKeyword', params.searchKeyword);
    }
    
    // 정렬 조건
    if (params.sortBy) {
      searchParams.append('sortBy', params.sortBy);
      searchParams.append('sortDirection', params.sortDirection || 'desc');
    }
    
    // 필터 조건 - 파라미터 이름 일치시키기
    if (params.minViewCounts && params.minViewCounts > 0) {
      searchParams.append('minViewCounts', params.minViewCounts.toString());
    }
    if (params.minCommentCounts && params.minCommentCounts > 0) {
      searchParams.append('minCommentCounts', params.minCommentCounts.toString());
    }
    if (params.minLikes && params.minLikes > 0) {
      searchParams.append('minLikes', params.minLikes.toString());
    }
    
    // 검색 기간
    if (params.startDate) {
      searchParams.append('startDate', params.startDate);
    }
    if (params.endDate) {
      searchParams.append('endDate', params.endDate);
    }

    // 디버깅용 로그
    console.log('Request parameters:', Object.fromEntries(searchParams.entries()));
    
    const response = await axiosInstance.get(`/posts/list?${searchParams}`);
    return response.data;
  },

  async getUserPosts(username: string, params: PostSearchParams): Promise<PostPage> {
    const response = await axiosInstance.get(`/posts/${username}/list`, { params });
    return response.data;
  },

  async getUserLikes(username: string, params: PostSearchParams): Promise<CursorResponse<PostSummary>> {
    const response = await axiosInstance.get(`/posts/${username}/like/grid`, { params });
    return response.data;
  },

  async getCommentReplies(commentId: number): Promise<CommentRepliesResponse[]> {
    const response = await axiosInstance.get<CommentRepliesResponse[]>(`/comments/${commentId}/replies`);
    return response.data;
  },

  async getComments(postId: number): Promise<CommentResponse[]> {
    const response = await axiosInstance.get<CommentResponse[]>(`/comments/${postId}`);
    return response.data;
  },
};

export default postService;