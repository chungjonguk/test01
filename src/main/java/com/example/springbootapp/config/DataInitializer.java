package com.example.springbootapp.config;

import java.math.BigDecimal;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.CommandLineRunner;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import com.example.springbootapp.entity.ShopOrder;
import com.example.springbootapp.repository.ShopOrderRepository;

@Configuration
public class DataInitializer {

	private static final Logger log = LoggerFactory.getLogger(DataInitializer.class);

	@Bean
	CommandLineRunner seedShopOrders(ShopOrderRepository shopOrderRepository) {
		return args -> {
			if (shopOrderRepository.count() > 0) {
				return;
			}
			ShopOrder order = new ShopOrder();
			order.setOrderNumber("2737");
			order.setCustomerName("Jonathan");
			order.setAmount(new BigDecimal("99.00"));
			order.setStatus("PAID");
			shopOrderRepository.save(order);
			log.info("샘플 주문 데이터 1건을 MySQL에 저장했습니다. (orderNumber={})", order.getOrderNumber());
		};
	}
}
