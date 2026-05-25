package com.example.springbootapp.config;

import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Configuration;

@Configuration
@EnableConfigurationProperties(ShippingProperties.class)
public class ShippingConfiguration {
}
