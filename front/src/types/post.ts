export interface PostFormData {
  title: string;
  subject: string;
  placeUrl: string;
  category: string;
  phone: string;
  address: string;
  roadAddress: string;
  x: number;
  y: number;
  date: string;
  rating: number;
  comment: string;
  scope: 'PUBLIC' | 'PRIVATE';
  imageUrls: string[];
}

interface UserProfile {
  profileImage: string;
  gender: 'MALE' | 'FEMALE';
  age: number;
  name: string;
}

export interface PostCard {
  id: number;
  placeName: string;
  title: string;
  date: string;
  images: string[];
  userLeft: {
    profile: UserProfile;
    rating: number;
    comment: string;
  };
  userRight: {
    profile: UserProfile;
    rating: number;
    comment: string;
  };
  hashtags: string[];
  scope: 'PUBLIC' | 'PRIVATE';
  likeCount: number;
  commentCount: number;
  coupleName: string;
  address: string;
}

export interface PostSearchOptions {
  searchKeyword?: string;
  startDate?: string;
  endDate?: string;
  sortBy?: 'DATE' | 'RATE';
  sortDirection?: 'ASC' | 'DESC';
}

export interface PostCursor {
  date: string;
  rate: number;
  idToken: string;
}

export interface CursorResponse<T> {
  items: T[];
  nextCursor: PostCursor | null;
  hasNext: boolean;
}

export interface PostImageUrlResponse {
  imageUrl: string;
}

export interface PostUser {
  id: string;
  nickname: string;
  profileImageUrl: string | null;
  gender?: 'MALE' | 'FEMALE';
  birthday?: string;
}

export interface CompletePostResponse {
  idToken: string;
  title: string;
  placeName: string;
  address: string;
  category: string;
  date: string;
  rate: number;
  userLeftProfileImageUrl: string | null;
  userLeftGender: 'MALE' | 'FEMALE';
  userLeftBirthday: string;
  userLeftName: string;
  userLeftRate: number;
  userLeftComment: string;
  userRightProfileImageUrl: string | null;
  userRightGender: 'MALE' | 'FEMALE';
  userRightBirthday: string;
  userRightName: string;
  userRightRate: number;
  userRightComment: string;
  coupleCode: string;
  coupleName: string;
  scope: 'PUBLIC' | 'PRIVATE';
  images: string[];
  hashtags?: string[];
  likeCount?: number;
  commentCount?: number;
}

export interface PostImageRequest {
  postIdToken: string;
}