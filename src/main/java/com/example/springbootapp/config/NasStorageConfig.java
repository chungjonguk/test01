package com.example.springbootapp.config;
import java.nio.file.Path;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.servlet.config.annotation.ResourceHandlerRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;
@Configuration
public class NasStorageConfig implements WebMvcConfigurer {
	private final NasStorageProperties nasStorage;
	public NasStorageConfig(NasStorageProperties nasStorage) {
		this.nasStorage = nasStorage;
	}
	@Override
	public void addResourceHandlers(ResourceHandlerRegistry registry) {
		if (!nasStorage.isEnabled()) {
			return;
		}
		Path uploadRoot = nasStorage.resolveUploadRoot();
		String fileLocation = "file:" + uploadRoot.toString().replace('\\', '/') + "/";
		registry.addResourceHandler(nasStorage.normalizedUrlPrefix() + "/**")
				.addResourceLocations(fileLocation)
				.setCachePeriod(0);
	}
}
