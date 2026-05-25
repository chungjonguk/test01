package com.example.springbootapp.shipping;

import org.springframework.stereotype.Component;
import com.example.springbootapp.config.ShippingProperties;

/** 롯데택배 연동. */
@Component
public class LotteLogisticsClient extends AbstractCarrierWaybillClient {

	public LotteLogisticsClient(ShippingProperties properties) {
		super(properties);
	}

	@Override
	public CarrierCd carrier() {
		return CarrierCd.LOTTE;
	}

	@Override
	protected ShippingProperties.CarrierEndpoint endpoint() {
		return properties.getLotte();
	}

	@Override
	protected String mockInvoicePrefix() {
		return "3";
	}
}
