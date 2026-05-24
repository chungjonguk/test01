package com.example.springbootapp.config;
import java.nio.charset.StandardCharsets;
import java.util.List;
import java.util.Locale;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.converter.HttpMessageConverter;
import org.springframework.http.converter.StringHttpMessageConverter;
import org.springframework.web.servlet.LocaleResolver;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;
import org.springframework.web.servlet.i18n.FixedLocaleResolver;
@Configuration
public class EncodingConfig implements WebMvcConfigurer {
	@Bean
	public LocaleResolver localeResolver() {
		FixedLocaleResolver resolver = new FixedLocaleResolver();
		resolver.setDefaultLocale(Locale.KOREA);
		return resolver;
	}
	@Override
	public void extendMessageConverters(List<HttpMessageConverter<?>> converters) {
		converters.stream()
				.filter(StringHttpMessageConverter.class::isInstance)
				.map(StringHttpMessageConverter.class::cast)
				.forEach(converter -> converter.setDefaultCharset(StandardCharsets.UTF_8));
	}
}
