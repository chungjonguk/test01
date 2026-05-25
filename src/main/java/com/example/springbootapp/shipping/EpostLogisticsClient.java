package com.example.springbootapp.shipping;

import org.springframework.stereotype.Component;
import com.example.springbootapp.config.ShippingProperties;

/** 우체국 택배 연동. */
@Component
public class EpostLogisticsClient extends AbstractCarrierWaybillClient {

	public EpostLogisticsClient(ShippingProperties properties) {
		super(properties);
	}

	@Override
	public CarrierCd carrier() {
		return CarrierCd.EPOST;
	}

	@Override
	protected ShippingProperties.CarrierEndpoint endpoint() {
		return properties.getEpost();
	}

	@Override
	protected String mockInvoicePrefix() {
		return "6";
	}
}
