import { axiosInstance } from './axios';

export interface SearchResult {
  placeName: string;
  placeUrl: string;
  category: string;
  address: string;
  roadAddress: string;
  phone: string;
  x: number;
  y: number;
}

class SearchService {
  async search(query: string): Promise<SearchResult[]> {
    try {
      const response = await axiosInstance.get(`/api/search?keyword=${encodeURIComponent(query)}`);
      return response.data;
    } catch (error) {
      console.error('검색 중 오류 발생:', error);
      return [];
    }
  }
}

export const searchService = new SearchService(); 