export interface Post {
    id: number;
    title: string;
    content: string;
    likes: number;
    viewCounts: number;
    user: {
      id: number;
      username: string;
      name: string;
    };
    comments: CommentResponse[];
    createdAt: string;
    modifiedAt: string;
  }

    // types/post.ts
  export interface PostSummary {
    id: number;
    title: string;
    content: string;
    likes: number;
    viewCounts: number;  // 추가
    user: {
        id: number;
        username: string;
        name: string;
    };
    commentCounts: number;
    createdAt: string;
    modifiedAt: string;
}
  
export interface CommentResponse {
  id: number;
  content: string;
  user: {
    id: number;
    name: string;
    username: string;
  };
  parentCommentId: number | null;
  repliesCount: number;
  createdAt: string;
  modifiedAt: string;
}

export interface CursorResponse<T> {
  items: T[];
  nextCursor: number | null;
  hasNext: boolean;
}

export interface PostPage {
  content: PostSummary[];
  totalElements: number;
  totalPages: number;
  size: number;
  number: number;
}

export type PostSearchType = 'title' | 'content' | 'author';
export type PostSortBy = 'date' | 'views' | 'likes' | 'comments';
export type PostSortDirection = 'asc' | 'desc';

export interface PostSearchParams {
    page?: number;
    size?: number;
    searchType?: PostSearchType;
    searchKeyword?: string;
    sortBy?: PostSortBy;
    sortDirection?: PostSortDirection;
    minViewCounts?: number;
    minCommentCounts?: number;
    minLikes?: number;
    startDate?: string;
    endDate?: string;
}

export interface CommentRepliesResponse {
  id: number;
  content: string;
  user: {
    id: number;
    name: string;
    username: string;
  };
  parentCommentId: number | null;
  createdAt: string;
  modifiedAt: string;
}