import './globals.css';
import { Toaster } from 'react-hot-toast';
import { Inter } from 'next/font/google';
import type { Metadata } from 'next';
import ClientLayout from '@/components/layout/ClientLayout';

const inter = Inter({ subsets: ['latin'] });

export const metadata: Metadata = {
  title: '두리',
  description: '우리만의 특별한 공간',
}

export default function RootLayout({
  children,
}: {
  children: React.ReactNode
}) {
  return (
    <html lang="ko">
      <head>
        <style>
          {`
            @font-face {
              font-family: 'BMDOHYEON';
              src: url('https://cdn.jsdelivr.net/gh/projectnoonnu/noonfonts_one@1.0/BMDOHYEON.woff') format('woff');
              font-weight: normal;
              font-style: normal;
            }
            @font-face {
                font-family: 'Cafe24Supermagic-Bold-v1.0';
                src: url('https://fastly.jsdelivr.net/gh/projectnoonnu/noonfonts_2307-2@1.0/Cafe24Supermagic-Bold-v1.0.woff2') format('woff2');
                font-weight: 700;
                font-style: normal;
            }
            @font-face {
              font-family: 'iceJaram-Rg';
              src: url('https://fastly.jsdelivr.net/gh/projectnoonnu/noonfonts_2307-2@1.0/iceJaram-Rg.woff2') format('woff2');
              font-weight: normal;
              font-style: normal;
            }
            @font-face {
                font-family: 'Pretendard-Regular';
                src: url('https://fastly.jsdelivr.net/gh/Project-Noonnu/noonfonts_2107@1.1/Pretendard-Regular.woff') format('woff');
                font-weight: 400;
                font-style: normal;
            }
            body { font-family: 'Pretendard-Regular', sans-serif; }
          `}
        </style>
        <link href="https://fonts.googleapis.com/css2?family=Nanum+Pen+Script&display=swap" rel="stylesheet" />
      </head>
      <body className={`${inter.className} antialiased`}>
        <ClientLayout>{children}</ClientLayout>
        <Toaster position="top-center" />
      </body>
    </html>
  );
}