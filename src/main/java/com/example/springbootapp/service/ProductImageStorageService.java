package com.example.springbootapp.service;
import java.io.IOException;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;
import com.example.springbootapp.storage.NasMediaType;
import com.example.springbootapp.storage.NasStorageService;
import com.example.springbootapp.storage.NasStorageService.NasStoredFile;
/**
 * 상품 이미지 파일을 NAS 저장소에 업로드하는 서비스.
 */
@Service
public class ProductImageStorageService {
	private final NasStorageService nasStorageService;
	public ProductImageStorageService(NasStorageService nasStorageService) {
		this.nasStorageService = nasStorageService;
	}
	/**
	 * 상품 이미지 파일을 NAS에 저장한다.
	 *
	 * @param file  업로드 파일
	 * @param regId 등록자 ID
	 * @return 저장된 파일 정보 (ID, URL, 경로 등)
	 * @throws IOException 파일 저장 실패 시
	 */
	public NasStoredFile store(MultipartFile file, String regId) throws IOException {
		return nasStorageService.store(NasMediaType.PRODUCT, file, regId);
	}
	/**
	 * 상품 이미지 파일을 NAS에 저장하고 공개 URL만 반환한다.
	 *
	 * @param file  업로드 파일
	 * @param regId 등록자 ID
	 * @return 저장된 파일의 공개 URL
	 * @throws IOException 파일 저장 실패 시
	 */
	public String storeUrl(MultipartFile file, String regId) throws IOException {
		return store(file, regId).url();
	}
}
