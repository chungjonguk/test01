package com.example.springbootapp.service;

import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import com.example.springbootapp.dto.EcmCustomerListItem;
import com.example.springbootapp.mapper.EcmCustomerMapper;
import com.example.springbootapp.util.AppDateTimeFormats;

@Service
@Transactional(readOnly = true)
public class EcmCustomerService {

	private final EcmCustomerMapper ecmCustomerMapper;

	public EcmCustomerService(EcmCustomerMapper ecmCustomerMapper) {
		this.ecmCustomerMapper = ecmCustomerMapper;
	}

	public List<Map<String, Object>> searchForGrid(String keyword) {
		return ecmCustomerMapper.searchCustomers(keyword).stream()
				.map(this::toRow)
				.collect(Collectors.toList());
	}

	private Map<String, Object> toRow(EcmCustomerListItem item) {
		Map<String, Object> row = new LinkedHashMap<>();
		row.put("customerId", item.getCustomerId());
		row.put("customerNm", item.getCustomerNm());
		row.put("email", item.getEmail());
		row.put("phone", item.getPhone());
		row.put("address", item.getAddress());
		row.put("joinedDt", AppDateTimeFormats.formatDate(
				item.getRegDt() != null ? item.getRegDt().toLocalDate() : null));
		return row;
	}
}
