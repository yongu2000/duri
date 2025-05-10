const IMAGE_STORAGE_URL = process.env.IMAGE_STORAGE_URL || 'http://localhost:8080';

// Local
export const getProfileImageUrl = (profileImageUrl: string | null | undefined): string => {
  if (!profileImageUrl) return '/exampleProfile.jpg';
  return `${IMAGE_STORAGE_URL}${profileImageUrl}`;
}; 

// Production
// export const getProfileImageUrl = (profileImageUrl: string | null | undefined): string => {
//   if (!profileImageUrl) return '/exampleProfile.jpg';
//   return `${profileImageUrl}`;
// }; 