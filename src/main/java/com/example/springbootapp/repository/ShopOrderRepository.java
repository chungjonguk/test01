package com.example.springbootapp.repository;

import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;

import com.example.springbootapp.entity.ShopOrder;

public interface ShopOrderRepository extends JpaRepository<ShopOrder, Long> {

	Optional<ShopOrder> findByOrderNumber(String orderNumber);
}
