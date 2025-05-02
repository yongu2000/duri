import { imageService } from '@/services/image';

const IMAGE_STORAGE_URL = process.env.IMAGE_STORAGE_URL;

class CustomUploadAdapter {
	private loader: any;

	constructor(loader: any) {
		this.loader = loader;
	}

	async upload() {
		try {
			const file = await this.loader.file;
			
			// 업로드 진행 상태 표시
			this.loader.uploaded = 0;
			this.loader.uploadTotal = 100;
			
			// 이미지 업로드 시작
			const imageUrl = await imageService.uploadImage(file);
			
			// 업로드 진행 상태 업데이트
			this.loader.uploaded = 100;
			
			// 개발 환경에서는 localhost:8080, 프로덕션에서는 실제 도메인으로 변경 필요
			// const fullImageUrl = `${IMAGE_STORAGE_URL}${imageUrl}`;
			const fullImageUrl = `${imageUrl}`;

			// 이미지 크기 정보 추가 (비동기로 처리)
			const img = new Image();
			img.src = fullImageUrl;
			
			// 이미지 로드가 완료되면 크기 정보 반환
			return new Promise((resolve) => {
				img.onload = () => {
					resolve({
						default: fullImageUrl,
						width: img.width,
						height: img.height,
						aspectRatio: img.width / img.height
					});
				};
				
				// 이미지 로드 실패 시에도 기본 정보 반환
				img.onerror = () => {
					resolve({
						default: fullImageUrl
					});
				};
			});
		} catch (error) {
			console.error('Upload failed:', error);
			throw error;
		}
	}

	abort() {
		// 업로드 중단 처리
	}
}

export default function CustomUploadAdapterPlugin(editor: any) {
	editor.plugins.get('FileRepository').createUploadAdapter = (loader: any) => {
		return new CustomUploadAdapter(loader);
	};
} 