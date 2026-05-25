package com.example.springbootapp.mapper;

import java.util.List;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import com.example.springbootapp.domain.EcmShipment;

@Mapper
public interface EcmShipmentMapper {

	int insert(EcmShipment shipment);

	EcmShipment findById(@Param("shipmentId") Long shipmentId);

	List<EcmShipment> findByOrderId(@Param("orderId") Long orderId);

	int countIssuedByOrderId(@Param("orderId") Long orderId);
}
