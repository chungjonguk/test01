package com.example.springbootapp.mapper;

import java.util.List;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import com.example.springbootapp.domain.EcmCustomer;
import com.example.springbootapp.dto.EcmCustomerListItem;

@Mapper
public interface EcmCustomerMapper {

	EcmCustomer findById(@Param("customerId") Long customerId);

	List<EcmCustomerListItem> searchCustomers(@Param("keyword") String keyword);
}
