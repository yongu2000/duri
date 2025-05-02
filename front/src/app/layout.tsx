import './globals.css';
import { Toaster } from 'react-hot-toast';
import { Inter } from 'next/font/google';
import type { Metadata } from 'next';
import ClientLayout from '@/components/layout/ClientLayout';

const inter = Inter({ subsets: ['latin'] });

export const metadata: Metadata = {
  title: 'Boilerplate',
  description: 'Next.js boilerplate with authentication',
}

export default function RootLayout({
  children,
}: {
  children: React.ReactNode
}) {
  return (
    <html lang="ko">
      <body className={`${inter.className} antialiased`}>
        <ClientLayout>{children}</ClientLayout>
        <Toaster position="top-center" />
      </body>
    </html>
  );
}