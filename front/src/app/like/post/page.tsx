'use client';

import { useEffect, useState } from 'react';
import { useRouter } from 'next/navigation';
import { useAuth } from '@/hooks/useAuth';
import NavBar from '@/components/NavBar';
import SubBar from '@/components/SubBar';
import SearchBar from '@/components/SearchBar';
import type { PostSearchOptions } from '@/types/post';
import CommunityPostList from '@/components/CommunityPostList';

export default function LikedPostsPage() {
  const { user } = useAuth();
  const router = useRouter();
  const [isLoading, setIsLoading] = useState(true);
  const [searchOptions, setSearchOptions] = useState<PostSearchOptions>({});

  useEffect(() => {
    if (!user?.coupleCode) {
      router.push('/login');
      return;
    }
    setIsLoading(false);
  }, [user?.coupleCode, router]);

  const handleSearchOptionsChange = (options: PostSearchOptions) => {
    setSearchOptions(options);
  };

  if (isLoading) {
    return (
      <div className="min-h-screen flex items-center justify-center bg-white">
        <div className="animate-spin rounded-full h-12 w-12 border-b-2 border-blue-500"></div>
      </div>
    );
  }

  return (
    <div className="min-h-screen flex flex-col bg-white">
      <NavBar />
      <SubBar />
      <SearchBar onSearchOptionsChange={handleSearchOptionsChange} />
      <div className="flex-1 p-4">
        <CommunityPostList searchOptions={searchOptions} />
      </div>
    </div>
  );
} 