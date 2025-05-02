'use client';

import { useEffect, useState, useRef, useMemo } from 'react';
import { useRouter, useParams } from 'next/navigation';
import { useForm } from 'react-hook-form';
import { zodResolver } from '@hookform/resolvers/zod';
import { z } from 'zod';
import { postService } from '@/services/post';
import { CKEditor } from '@ckeditor/ckeditor5-react';
import {
  ClassicEditor,
  Autoformat,
  AutoImage,
  Autosave,
  BalloonToolbar,
  BlockQuote,
  Bold,
  CloudServices,
  Essentials,
  FontBackgroundColor,
  FontColor,
  FontFamily,
  FontSize,
  ImageBlock,
  ImageInline,
  ImageInsert,
  ImageInsertViaUrl,
  ImageResize,
  ImageStyle,
  ImageToolbar,
  ImageUpload,
  Indent,
  IndentBlock,
  Italic,
  Link,
  List,
  Mention,
  Paragraph,
  Table,
  TableCaption,
  TableCellProperties,
  TableColumnResize,
  TableProperties,
  TableToolbar,
  TextTransformation,
  Underline,
  WordCount
} from 'ckeditor5';
import translations from 'ckeditor5/translations/ko.js';
import CustomUploadAdapterPlugin from '../../new/CustomUploadAdapter';

import 'ckeditor5/ckeditor5.css';
import '../../new/editor.css';

const postSchema = z.object({
  title: z.string().min(1, '제목을 입력해주세요'),
  content: z.string().min(1, '내용을 입력해주세요'),
});

type PostFormData = z.infer<typeof postSchema>;

const LICENSE_KEY = 'GPL';

export default function EditPostPage() {
  const router = useRouter();
  const params = useParams();
  const postId = Number(params.id) as number;
  const [isLoading, setIsLoading] = useState(true);
  const [content, setContent] = useState('');
  const editorContainerRef = useRef<HTMLDivElement>(null);
  const editorRef = useRef<HTMLDivElement>(null);
  const editorWordCountRef = useRef<HTMLDivElement>(null);
  const [isLayoutReady, setIsLayoutReady] = useState(false);

  const { register, handleSubmit, formState: { errors }, setValue } = useForm<PostFormData>({
    resolver: zodResolver(postSchema)
  });

  useEffect(() => {
    setIsLayoutReady(true);
    return () => setIsLayoutReady(false);
  }, []);

  useEffect(() => {
    const loadPost = async () => {
      try {
        const post = await postService.getPost(postId);
        setValue('title', post.title);
        setContent(post.content);
      } catch (error) {
        console.error('게시글 로드 실패:', error);
        router.push('/my-posts');
      } finally {
        setIsLoading(false);
      }
    };

    loadPost();
  }, [postId, router, setValue]);

  const onSubmit = async (data: PostFormData) => {
    try {
      if (!content) {
        return;
      }
      
      await postService.updatePost(postId, {
        title: data.title,
        content: content
      });
      router.push(`/posts/${postId}`);
    } catch (error) {
      console.error('게시글 수정 실패:', error);
      alert('게시글 수정에 실패했습니다.');
    }
  };

  const { editorConfig } = useMemo(() => {
    if (!isLayoutReady) {
      return {};
    }

    return {
      editorConfig: {
        toolbar: {
          items: [
            'fontSize',
            'fontFamily',
            'fontColor',
            'fontBackgroundColor',
            '|',
            'bold',
            'italic',
            'underline',
            '|',
            'link',
            'insertImage',
            'insertTable',
            'blockQuote',
            '|',
            'bulletedList',
            'numberedList',
            'outdent',
            'indent'
          ],
          shouldNotGroupWhenFull: false
        },
        plugins: [
          Autoformat,
          AutoImage,
          Autosave,
          BalloonToolbar,
          BlockQuote,
          Bold,
          CloudServices,
          Essentials,
          FontBackgroundColor,
          FontColor,
          FontFamily,
          FontSize,
          ImageBlock,
          ImageInline,
          ImageInsert,
          ImageInsertViaUrl,
          ImageResize,
          ImageStyle,
          ImageToolbar,
          ImageUpload,
          Indent,
          IndentBlock,
          Italic,
          Link,
          List,
          Mention,
          Paragraph,
          Table,
          TableCaption,
          TableCellProperties,
          TableColumnResize,
          TableProperties,
          TableToolbar,
          TextTransformation,
          Underline,
          WordCount,
          CustomUploadAdapterPlugin
        ],
        balloonToolbar: ['bold', 'italic', '|', 'link', 'insertImage', '|', 'bulletedList', 'numberedList'],
        fontFamily: {
          supportAllValues: true,
          options: [
            'default',
            'Arial',
            '맑은 고딕',
            '나눔고딕',
            '나눔명조',
            '돋움',
            '굴림',
            '바탕',
            'Times New Roman'
          ]
        },
        fontSize: {
          options: [10, 12, 14, 'default', 18, 20, 22],
          supportAllValues: true
        },
        image: {
          toolbar: ['imageTextAlternative', '|', 'imageStyle:inline', 'imageStyle:wrapText', 'imageStyle:breakText', '|', 'resizeImage']
        },
        language: 'ko',
        licenseKey: LICENSE_KEY,
        link: {
          addTargetToExternalLinks: true,
          defaultProtocol: 'https://',
          decorators: {
            toggleDownloadable: {
              mode: 'manual' as const,
              label: 'Downloadable',
              attributes: {
                download: 'file'
              }
            }
          }
        },
        mention: {
          feeds: [
            {
              marker: '@',
              feed: []
            }
          ]
        },
        menuBar: {
          isVisible: true
        },
        placeholder: '내용을 입력하세요!',
        table: {
          contentToolbar: ['tableColumn', 'tableRow', 'mergeTableCells', 'tableProperties', 'tableCellProperties']
        },
        translations: [translations]
      }
    };
  }, [isLayoutReady]);

  if (isLoading) {
    return <div className="flex justify-center items-center h-full">로딩 중...</div>;
  }

  return (
    <div className="min-h-screen bg-gray-50 py-12">
      <div className="max-w-4xl mx-auto px-4 sm:px-6 lg:px-8">
        <div className="mb-6">
          <button
            onClick={() => router.back()}
            className="flex items-center text-gray-600 hover:text-indigo-600 transition-colors duration-200"
          >
            <svg className="w-5 h-5 mr-2" fill="none" stroke="currentColor" viewBox="0 0 24 24">
              <path strokeLinecap="round" strokeLinejoin="round" strokeWidth={2} d="M10 19l-7-7m0 0l7-7m-7 7h18" />
            </svg>
            이전으로
          </button>
        </div>

        <div className="bg-white rounded-xl shadow-lg overflow-hidden">
          <div className="p-8">
            <h1 className="text-2xl font-bold text-gray-900 mb-8">게시글 수정</h1>
            
            <form onSubmit={handleSubmit(onSubmit)} className="space-y-6">
              <div>
                <label htmlFor="title" className="block text-sm font-medium text-gray-700">
                  제목
                </label>
                <input
                  type="text"
                  id="title"
                  {...register('title')}
                  className="mt-1 block w-full rounded-md border-gray-300 shadow-sm focus:border-indigo-500 focus:ring-indigo-500"
                />
                {errors.title && (
                  <p className="mt-1 text-sm text-red-600">{errors.title.message}</p>
                )}
              </div>

              <div>
                <label htmlFor="content" className="block text-sm font-medium text-gray-700">
                  내용
                </label>
                <div className="mt-1 w-full">
                  <div className="editor-container" ref={editorContainerRef}>
                    <div className="editor-container__editor">
                      <div ref={editorRef}>
                        {editorConfig && (
                          <CKEditor
                            onReady={(editor) => {
                              try {
                                if (editorWordCountRef.current) {
                                  const wordCount = editor.plugins.get('WordCount');
                                  editorWordCountRef.current.appendChild(wordCount.wordCountContainer);
                                }
                                if (content) {
                                  editor.setData(content);
                                }
                              } catch (error) {
                                console.error('Error in onReady:', error);
                              }
                            }}
                            onError={(error) => {
                              console.error('CKEditor error:', error);
                            }}
                            onAfterDestroy={() => {
                              try {
                                if (editorWordCountRef.current) {
                                  while (editorWordCountRef.current.firstChild) {
                                    editorWordCountRef.current.removeChild(editorWordCountRef.current.firstChild);
                                  }
                                }
                              } catch (error) {
                                console.error('Error in onAfterDestroy:', error);
                              }
                            }}
                            editor={ClassicEditor}
                            config={editorConfig}
                            data={content}
                            onChange={(event, editor) => {
                              try {
                                const data = editor.getData();
                                setContent(data);
                                setValue('content', data || ' ');
                              } catch (error) {
                                console.error('Error in onChange:', error);
                              }
                            }}
                          />
                        )}
                      </div>
                    </div>
                    <div className="editor_container__word-count" ref={editorWordCountRef}></div>
                  </div>
                </div>
                {errors.content && (
                  <p className="mt-1 text-sm text-red-600">{errors.content.message}</p>
                )}
              </div>

              <div className="flex justify-end space-x-4">
                <button
                  type="button"
                  onClick={() => router.back()}
                  className="px-4 py-2 text-sm font-medium text-gray-700 bg-white border border-gray-300 rounded-md hover:bg-gray-50"
                >
                  취소
                </button>
                <button
                  type="submit"
                  className="px-4 py-2 text-sm font-medium text-white bg-indigo-600 rounded-md hover:bg-indigo-700"
                >
                  수정하기
                </button>
              </div>
            </form>
          </div>
        </div>
      </div>
    </div>
  );
}