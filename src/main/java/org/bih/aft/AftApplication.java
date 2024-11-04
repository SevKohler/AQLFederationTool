package org.bih.aft;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.context.properties.EnableConfigurationProperties;

@SpringBootApplication
@EnableConfigurationProperties
public class AftApplication{
	public static void main(String[] args) {
		SpringApplication.run(AftApplication.class, args);
	}
}

