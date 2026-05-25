package com.example.springbootapp.shipping;

import org.springframework.stereotype.Component;
import com.example.springbootapp.config.ShippingProperties;

/** CJ대한통운 스마트택배 연동 (계약 API URL·키 설정 시 실연동 확장). */
@Component
public class CjLogisticsClient extends AbstractCarrierWaybillClient {

	public CjLogisticsClient(ShippingProperties properties) {
		super(properties);
	}

	@Override
	public CarrierCd carrier() {
		return CarrierCd.CJ;
	}

	@Override
	protected ShippingProperties.CarrierEndpoint endpoint() {
		return properties.getCj();
	}

	@Override
	protected String mockInvoicePrefix() {
		return "5";
	}
}
