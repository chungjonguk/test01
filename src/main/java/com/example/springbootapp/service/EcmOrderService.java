package com.example.springbootapp.service;

import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import com.example.springbootapp.dto.EcmOrderListItem;
import com.example.springbootapp.mapper.EcmOrderMapper;
import com.example.springbootapp.util.AppDateTimeFormats;

@Service
@Transactional(readOnly = true)
public class EcmOrderService {

	private final EcmOrderMapper ecmOrderMapper;

	public EcmOrderService(EcmOrderMapper ecmOrderMapper) {
		this.ecmOrderMapper = ecmOrderMapper;
	}

	public List<Map<String, Object>> searchForGrid(String keyword, String statusFilter) {
		String statusCd = mapStatusFilterToCd(statusFilter);
		return ecmOrderMapper.searchOrders(keyword, statusCd).stream()
				.map(this::toRow)
				.collect(Collectors.toList());
	}

	private Map<String, Object> toRow(EcmOrderListItem item) {
		Map<String, Object> row = new LinkedHashMap<>();
		row.put("orderId", item.getOrderId());
		row.put("orderNo", item.getOrderNo());
		row.put("customerNm", item.getCustomerNm());
		row.put("customerEmail", item.getCustomerEmail());
		row.put("orderDt", AppDateTimeFormats.formatDate(item.getOrderDt()));
		row.put("shipTo", item.getShipTo());
		row.put("statusCd", item.getStatusCd());
		row.put("statusLabel", mapStatusCdToLabel(item.getStatusCd()));
		row.put("amount", item.getAmount());
		return row;
	}

	private static String mapStatusFilterToCd(String filter) {
		if (filter == null || filter.isBlank()) {
			return null;
		}
		return switch (filter.trim()) {
			case "Completed" -> "COMPLETED";
			case "Processing" -> "PROCESSING";
			case "On Hold" -> "ON_HOLD";
			case "Pending" -> "PENDING";
			default -> filter.trim().toUpperCase().replace(' ', '_');
		};
	}

	private static String mapStatusCdToLabel(String statusCd) {
		if (statusCd == null || statusCd.isBlank()) {
			return "";
		}
		return switch (statusCd) {
			case "COMPLETED" -> "Completed";
			case "PROCESSING" -> "Processing";
			case "ON_HOLD" -> "On Hold";
			case "PENDING" -> "Pending";
			default -> statusCd;
		};
	}
}
