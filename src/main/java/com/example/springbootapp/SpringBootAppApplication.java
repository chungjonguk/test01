package com.example.springbootapp;
import org.mybatis.spring.annotation.MapperScan;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import com.example.springbootapp.config.MysqlBootstrap;
@SpringBootApplication
@MapperScan("com.example.springbootapp.mapper")
public class SpringBootAppApplication {
	public static void main(String[] args) {
		MysqlBootstrap.ensureReadyBeforeStartup(args);
		SpringApplication.run(SpringBootAppApplication.class, args);
	}
}
