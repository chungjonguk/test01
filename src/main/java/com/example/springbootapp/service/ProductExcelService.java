package com.example.springbootapp.service;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import org.apache.poi.ss.usermodel.Cell;
import org.apache.poi.ss.usermodel.CellStyle;
import org.apache.poi.ss.usermodel.DataFormatter;
import org.apache.poi.ss.usermodel.Font;
import org.apache.poi.ss.usermodel.Row;
import org.apache.poi.ss.usermodel.Sheet;
import org.apache.poi.ss.usermodel.Workbook;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import com.example.springbootapp.domain.EcmProduct;
import com.example.springbootapp.dto.EcmProductFormDto;
import com.example.springbootapp.dto.ProductExcelImportResult;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpSession;
/**
 * 상품 xlsx 내보내기·가져오기 (Apache POI).
 * 컬럼: 상품ID, 상품명, 카테고리코드, 가격, 재고, 상태코드, 이미지URL, 설명
 */
@Service
public class ProductExcelService {
	private static final String[] HEADERS = {
			"상품ID", "상품명", "카테고리코드", "가격", "재고", "상태코드", "이미지URL", "설명"
	};
	private static final String[] HEADER_KEYS = {
			"productId", "productNm", "categoryCd", "price", "stockQty", "statusCd", "imgUrl", "description"
	};
	private final EcmProductService ecmProductService;
	private final DataFormatter dataFormatter = new DataFormatter(Locale.KOREA);
	public ProductExcelService(EcmProductService ecmProductService) {
		this.ecmProductService = ecmProductService;
	}
	/**
	 * 검색 조건에 맞는 상품 목록을 xlsx 바이트 배열로 내보낸다.
	 *
	 * @param productNm  상품명 (null 허용)
	 * @param categoryCd 카테고리 코드 (null 허용)
	 * @param statusCd   판매 상태 코드 (null 허용)
	 * @return xlsx 파일 바이트 배열
	 * @throws IOException 워크북 생성·쓰기 실패 시
	 */
	public byte[] export(
			String productNm, String categoryCd, String statusCd, HttpServletRequest request, HttpSession session)
			throws IOException {
		List<EcmProduct> products = ecmProductService.search(productNm, categoryCd, statusCd, request, session);
		try (Workbook workbook = new XSSFWorkbook()) {
			Sheet sheet = workbook.createSheet("상품목록");
			writeHeaderRow(workbook, sheet);
			int rowIndex = 1;
			for (EcmProduct product : products) {
				Row row = sheet.createRow(rowIndex++);
				writeProductRow(row, product);
			}
			autoSizeColumns(sheet, HEADERS.length);
			return toBytes(workbook);
		}
	}
	/**
	 * 상품 일괄 등록용 xlsx 템플릿(헤더·샘플 행)을 생성한다.
	 *
	 * @return xlsx 템플릿 바이트 배열
	 * @throws IOException 워크북 생성·쓰기 실패 시
	 */
	public byte[] exportTemplate() throws IOException {
		try (Workbook workbook = new XSSFWorkbook()) {
			Sheet sheet = workbook.createSheet("상품목록");
			writeHeaderRow(workbook, sheet);
			Row sample = sheet.createRow(1);
			sample.createCell(0).setCellValue("");
			sample.createCell(1).setCellValue("샘플 상품");
			sample.createCell(2).setCellValue("FASHION");
			sample.createCell(3).setCellValue(10000);
			sample.createCell(4).setCellValue(100);
			sample.createCell(5).setCellValue("ON_SALE");
			sample.createCell(6).setCellValue("/uploads/products/sample.jpg");
			sample.createCell(7).setCellValue("엑셀 일괄 등록 예시 (업로드 전 내용을 수정하세요)");
			autoSizeColumns(sheet, HEADERS.length);
			return toBytes(workbook);
		}
	}
	/**
	 * 내보내기 파일명을 생성한다 (products_yyyyMMdd_HHmmss.xlsx).
	 *
	 * @return 생성된 파일명
	 */
	public String buildExportFileName() {
		String timestamp = LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyyMMdd_HHmmss"));
		return "products_" + timestamp + ".xlsx";
	}
	/**
	 * xlsx 파일에서 2행부터 상품 데이터를 읽어 등록·수정한다. 상품ID 없음=등록, DB 존재=수정, 빈 행=skipped.
	 *
	 * @param inputStream 업로드된 xlsx 입력 스트림
	 * @param session     HTTP 세션 (저장 시 등록·수정자 ID 추출용)
	 * @return 가져오기 결과 (생성·수정·건너뜀·오류 건수)
	 * @throws IOException 파일 읽기 실패 시
	 */
	@Transactional
	public ProductExcelImportResult importExcel(
			InputStream inputStream, HttpSession session, HttpServletRequest request) throws IOException {
		ProductExcelImportResult result = new ProductExcelImportResult();
		try (Workbook workbook = new XSSFWorkbook(inputStream)) {
			Sheet sheet = workbook.getNumberOfSheets() > 0 ? workbook.getSheetAt(0) : null;
			if (sheet == null) {
				result.addError("엑셀 시트가 비어 있습니다.");
				return result;
			}
			Row headerRow = sheet.getRow(0);
			if (headerRow == null) {
				result.addError("헤더 행(1행)이 없습니다.");
				return result;
			}
			Map<String, Integer> columnIndex = mapHeaderColumns(headerRow);
			if (!columnIndex.containsKey("productNm")) {
				result.addError("'상품명' 컬럼을 찾을 수 없습니다. 템플릿 형식을 확인해 주세요.");
				return result;
			}
			int lastRow = sheet.getLastRowNum();
			for (int rowNum = 1; rowNum <= lastRow; rowNum++) {
				Row row = sheet.getRow(rowNum);
				if (row == null || isEmptyRow(row, columnIndex)) {
					result.incrementSkipped();
					continue;
				}
				int excelRow = rowNum + 1;
				try {
					EcmProductFormDto dto = toFormDto(row, columnIndex);
					if (isBlank(dto.getProductNm())) {
						result.incrementSkipped();
						continue;
					}
					boolean isUpdate = dto.getProductId() != null
							&& ecmProductService.findById(dto.getProductId()) != null;
					ecmProductService.save(dto, session, request);
					if (isUpdate) {
						result.incrementUpdated();
					} else {
						result.incrementCreated();
					}
				} catch (IllegalArgumentException ex) {
					result.addError(excelRow + "행: " + ex.getMessage());
				} catch (RuntimeException ex) {
					result.addError(excelRow + "행: 처리 중 오류가 발생했습니다.");
				}
			}
		}
		return result;
	}
	// --- xlsx 작성 ---
	private void writeHeaderRow(Workbook workbook, Sheet sheet) {
		CellStyle headerStyle = workbook.createCellStyle();
		Font font = workbook.createFont();
		font.setBold(true);
		headerStyle.setFont(font);
		Row headerRow = sheet.createRow(0);
		for (int i = 0; i < HEADERS.length; i++) {
			Cell cell = headerRow.createCell(i);
			cell.setCellValue(HEADERS[i]);
			cell.setCellStyle(headerStyle);
		}
	}
	private void writeProductRow(Row row, EcmProduct product) {
		if (product.getProductId() != null) {
			row.createCell(0).setCellValue(product.getProductId());
		}
		row.createCell(1).setCellValue(nullToEmpty(product.getProductNm()));
		row.createCell(2).setCellValue(nullToEmpty(product.getCategoryCd()));
		if (product.getPrice() != null) {
			row.createCell(3).setCellValue(product.getPrice().doubleValue());
		}
		if (product.getStockQty() != null) {
			row.createCell(4).setCellValue(product.getStockQty());
		}
		row.createCell(5).setCellValue(nullToEmpty(product.getStatusCd()));
		String imgUrl = product.getImgUrl();
		if (imgUrl == null || imgUrl.isBlank()) {
			List<String> urls = ecmProductService.findImageUrls(product.getProductId());
			imgUrl = urls.isEmpty() ? "" : urls.get(0);
		}
		row.createCell(6).setCellValue(nullToEmpty(imgUrl));
		row.createCell(7).setCellValue(nullToEmpty(product.getDescription()));
	}
	// --- xlsx 파싱 ---
	/** 한글·영문 헤더 → 필드키·컬럼 인덱스 */
	private Map<String, Integer> mapHeaderColumns(Row headerRow) {
		Map<String, Integer> index = new HashMap<>();
		for (Cell cell : headerRow) {
			String header = readCell(headerRow, cell.getColumnIndex()).trim();
			if (header.isEmpty()) {
				continue;
			}
			for (int i = 0; i < HEADERS.length; i++) {
				if (header.equals(HEADERS[i]) || header.equalsIgnoreCase(HEADER_KEYS[i])) {
					index.put(HEADER_KEYS[i], cell.getColumnIndex());
					break;
				}
			}
		}
		return index;
	}
	private EcmProductFormDto toFormDto(Row row, Map<String, Integer> columnIndex) {
		EcmProductFormDto dto = new EcmProductFormDto();
		dto.setProductId(readLong(row, columnIndex.get("productId")));
		dto.setProductNm(readString(row, columnIndex.get("productNm")));
		dto.setCategoryCd(readString(row, columnIndex.get("categoryCd")));
		dto.setPrice(readBigDecimal(row, columnIndex.get("price")));
		dto.setStockQty(readInteger(row, columnIndex.get("stockQty")));
		dto.setStatusCd(readString(row, columnIndex.get("statusCd")));
		String imgUrl = readString(row, columnIndex.get("imgUrl"));
		dto.setImgUrl(imgUrl);
		if (imgUrl != null && !imgUrl.isBlank()) {
			dto.setImageUrls(List.of(imgUrl.trim()));
		}
		dto.setDescription(readString(row, columnIndex.get("description")));
		return dto;
	}
	private boolean isEmptyRow(Row row, Map<String, Integer> columnIndex) {
		for (String key : HEADER_KEYS) {
			Integer col = columnIndex.get(key);
			if (col == null) {
				continue;
			}
			if (!readCell(row, col).isBlank()) {
				return false;
			}
		}
		return true;
	}
	private String readCell(Row row, int columnIndex) {
		if (row == null) {
			return "";
		}
		Cell cell = row.getCell(columnIndex);
		if (cell == null) {
			return "";
		}
		return dataFormatter.formatCellValue(cell).trim();
	}
	private String readString(Row row, Integer columnIndex) {
		if (columnIndex == null) {
			return null;
		}
		String value = readCell(row, columnIndex);
		return value.isEmpty() ? null : value;
	}
	private Long readLong(Row row, Integer columnIndex) {
		if (columnIndex == null) {
			return null;
		}
		String value = readCell(row, columnIndex);
		if (value.isEmpty()) {
			return null;
		}
		try {
			return Long.parseLong(value.replace(",", ""));
		} catch (NumberFormatException ex) {
			throw new IllegalArgumentException("상품ID 형식이 올바르지 않습니다.");
		}
	}
	private Integer readInteger(Row row, Integer columnIndex) {
		if (columnIndex == null) {
			return null;
		}
		String value = readCell(row, columnIndex);
		if (value.isEmpty()) {
			return null;
		}
		try {
			return (int) Math.round(Double.parseDouble(value.replace(",", "")));
		} catch (NumberFormatException ex) {
			throw new IllegalArgumentException("재고 수량 형식이 올바르지 않습니다.");
		}
	}
	private BigDecimal readBigDecimal(Row row, Integer columnIndex) {
		if (columnIndex == null) {
			return null;
		}
		String value = readCell(row, columnIndex);
		if (value.isEmpty()) {
			return null;
		}
		try {
			return new BigDecimal(value.replace(",", ""));
		} catch (NumberFormatException ex) {
			throw new IllegalArgumentException("가격 형식이 올바르지 않습니다.");
		}
	}
	private void autoSizeColumns(Sheet sheet, int columnCount) {
		for (int i = 0; i < columnCount; i++) {
			sheet.autoSizeColumn(i);
			int width = sheet.getColumnWidth(i);
			sheet.setColumnWidth(i, Math.min(width + 512, 256 * 60));
		}
	}
	private byte[] toBytes(Workbook workbook) throws IOException {
		ByteArrayOutputStream out = new ByteArrayOutputStream();
		workbook.write(out);
		return out.toByteArray();
	}
	private static String nullToEmpty(String value) {
		return value == null ? "" : value;
	}
	private static boolean isBlank(String value) {
		return value == null || value.trim().isEmpty();
	}
}
