package com.example.springbootapp.mapper;

import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import com.example.springbootapp.domain.EcmCustomer;

@Mapper
public interface EcmCustomerMapper {

	EcmCustomer findById(@Param("customerId") Long customerId);
}
