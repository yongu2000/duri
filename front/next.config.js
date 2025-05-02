/** @type {import('next').NextConfig} */
require('dotenv').config();

const nextConfig = {
  eslint: {
    ignoreDuringBuilds: true,
  },
  reactStrictMode: false,
  images: {
    remotePatterns: [
      {
        protocol: 'https',
        hostname: "https://boilerplate-image.s3.ap-southeast-2.amazonaws.com",
        pathname: '/**',
      },
      {
        protocol: 'http',
        hostname: 'localhost',
        port: '8080',
        pathname: '/uploads/image/**',
      },
    ],
  },
  async rewrites() {
    return [
      {
        source: '/api/:path*',
        destination: 'http://localhost:8080/api/:path*',
      },
      {
        source: '/uploads/image/:path*',
        destination: 'http://localhost:8080/uploads/image/:path*',
      },
    ];
  },
};

module.exports = nextConfig; 