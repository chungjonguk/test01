package com.example.springbootapp;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.web.servlet.MockMvc;

import static org.hamcrest.Matchers.containsString;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.redirectedUrl;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest
@AutoConfigureMockMvc
class OrderDetailsMvcTest {

	@Autowired
	private MockMvc mockMvc;

	@Test
	void orderDetails_withoutHtmlSuffix_ok() throws Exception {
		mockMvc.perform(get("/app/e-commerce/orders/order-details"))
				.andExpect(status().isOk())
				.andExpect(content().string(containsString("Order Details: #2737")));
	}

	@Test
	void orderDetails_withHtmlSuffix_ok() throws Exception {
		mockMvc.perform(get("/app/e-commerce/orders/order-details.html"))
				.andExpect(status().isOk())
				.andExpect(content().string(containsString("Order Details: #2737")));
	}

	@Test
	void doubleAppPrefix_redirectsToCorrectPath() throws Exception {
		mockMvc.perform(get("/app/app/e-commerce/orders/order-details.html"))
				.andExpect(status().is3xxRedirection())
				.andExpect(redirectedUrl("/app/e-commerce/orders/order-details.html"));
	}
}
