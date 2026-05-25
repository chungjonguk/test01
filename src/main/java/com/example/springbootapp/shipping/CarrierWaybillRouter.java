package com.example.springbootapp.shipping;

import java.util.EnumMap;
import java.util.List;
import java.util.Map;
import org.springframework.stereotype.Component;

@Component
public class CarrierWaybillRouter {

	private final Map<CarrierCd, CarrierWaybillClient> clients;

	public CarrierWaybillRouter(List<CarrierWaybillClient> clientList) {
		Map<CarrierCd, CarrierWaybillClient> map = new EnumMap<>(CarrierCd.class);
		for (CarrierWaybillClient client : clientList) {
			map.put(client.carrier(), client);
		}
		this.clients = Map.copyOf(map);
	}

	public CarrierWaybillClient resolve(CarrierCd carrierCd) {
		CarrierWaybillClient client = clients.get(carrierCd);
		if (client == null) {
			throw new IllegalArgumentException("지원하지 않는 택배사입니다: " + carrierCd);
		}
		return client;
	}
}
