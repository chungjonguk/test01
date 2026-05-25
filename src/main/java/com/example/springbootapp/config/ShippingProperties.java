package com.example.springbootapp.config;

import org.springframework.boot.context.properties.ConfigurationProperties;

/**
 * 택배사 운송장 API 설정 ({@code app.shipping.*}).
 */
@ConfigurationProperties(prefix = "app.shipping")
public class ShippingProperties {

	private boolean enabled = true;
	private boolean mockEnabled = true;
	private Sender sender = new Sender();
	private CarrierEndpoint cj = new CarrierEndpoint();
	private CarrierEndpoint epost = new CarrierEndpoint();
	private CarrierEndpoint lotte = new CarrierEndpoint();

	public boolean isEnabled() {
		return enabled;
	}

	public void setEnabled(boolean enabled) {
		this.enabled = enabled;
	}

	public boolean isMockEnabled() {
		return mockEnabled;
	}

	public void setMockEnabled(boolean mockEnabled) {
		this.mockEnabled = mockEnabled;
	}

	public Sender getSender() {
		return sender;
	}

	public void setSender(Sender sender) {
		this.sender = sender;
	}

	public CarrierEndpoint getCj() {
		return cj;
	}

	public void setCj(CarrierEndpoint cj) {
		this.cj = cj;
	}

	public CarrierEndpoint getEpost() {
		return epost;
	}

	public void setEpost(CarrierEndpoint epost) {
		this.epost = epost;
	}

	public CarrierEndpoint getLotte() {
		return lotte;
	}

	public void setLotte(CarrierEndpoint lotte) {
		this.lotte = lotte;
	}

	public static class Sender {
		private String name = "PrintMall";
		private String phone = "0212345678";
		private String zipcode = "04524";
		private String address = "서울특별시 중구 세종대로 110";

		public String getName() {
			return name;
		}

		public void setName(String name) {
			this.name = name;
		}

		public String getPhone() {
			return phone;
		}

		public void setPhone(String phone) {
			this.phone = phone;
		}

		public String getZipcode() {
			return zipcode;
		}

		public void setZipcode(String zipcode) {
			this.zipcode = zipcode;
		}

		public String getAddress() {
			return address;
		}

		public void setAddress(String address) {
			this.address = address;
		}
	}

	public static class CarrierEndpoint {
		private String apiUrl = "";
		private String apiKey = "";
		private String customerId = "";

		public String getApiUrl() {
			return apiUrl;
		}

		public void setApiUrl(String apiUrl) {
			this.apiUrl = apiUrl;
		}

		public String getApiKey() {
			return apiKey;
		}

		public void setApiKey(String apiKey) {
			this.apiKey = apiKey;
		}

		public String getCustomerId() {
			return customerId;
		}

		public void setCustomerId(String customerId) {
			this.customerId = customerId;
		}

		public boolean isConfigured() {
			return apiUrl != null && !apiUrl.isBlank() && apiKey != null && !apiKey.isBlank();
		}
	}
}
