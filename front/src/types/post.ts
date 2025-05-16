export interface PostFormData {
  subject: string;
  title: string;
  date: string;
  rating: number;
  comment: string;
  scope: 'PUBLIC' | 'PRIVATE';
  images: File[];
  imageUrls: string[];
  placeUrl: string;
  category: string;
  address: string;
  roadAddress: string;
  phone: string;
  x: number;
  y: number;
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