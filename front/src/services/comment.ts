import { axiosInstance } from './axios';

export interface Comment {
  commentIdToken: string;
  content: string;
  author: string;
  createdAt: string;
  commentCount?: number;
}

export interface CommentReply {
  commentIdToken: string;
  parentCommentIdToken: string;
  content: string;
  author: string;
  replyTo: string;
  createdAt: string;
}

export interface CommentCreateRequest {
  content: string;
}

export interface CommentUpdateRequest {
  content: string;
}

export interface CommentUpdateResponse {
  content: string;
}

export interface CommentReplyCreateRequest {
  content: string;
}

class CommentService {
  async getPostComments(postIdToken: string): Promise<Comment[]> {
    const response = await axiosInstance.get(`/comment/${postIdToken}`);
    return response.data;
  }

  async createComment(postIdToken: string, request: CommentCreateRequest): Promise<void> {
    await axiosInstance.post(`/comment/${postIdToken}`, request);
  }

  async updateComment(commentIdToken: string, request: CommentUpdateRequest): Promise<CommentUpdateResponse> {
    const response = await axiosInstance.put(`/comment/${commentIdToken}`, request);
    return response.data;
  }

  async deleteComment(commentIdToken: string): Promise<void> {
    await axiosInstance.delete(`/comment/${commentIdToken}`);
  }

  async getCommentReplies(commentIdToken: string): Promise<CommentReply[]> {
    const response = await axiosInstance.get(`/comment/reply/${commentIdToken}`);
    return response.data;
  }

  async createReply(commentIdToken: string, request: CommentReplyCreateRequest): Promise<void> {
    await axiosInstance.post(`/comment/reply/${commentIdToken}`, request);
  }
}

export const commentService = new CommentService(); 