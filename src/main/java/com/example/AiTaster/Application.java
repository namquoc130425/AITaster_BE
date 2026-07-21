package com.example.AiTaster;

import com.example.AiTaster.config.QdrantProperties;
import io.swagger.v3.oas.annotations.OpenAPIDefinition;
import io.swagger.v3.oas.annotations.enums.SecuritySchemeIn;
import io.swagger.v3.oas.annotations.enums.SecuritySchemeType;
import io.swagger.v3.oas.annotations.info.Info;
import io.swagger.v3.oas.annotations.security.SecurityScheme;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.EnableAutoConfiguration;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.scheduling.annotation.EnableScheduling;

import io.github.cdimascio.dotenv.Dotenv;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.context.properties.EnableConfigurationProperties;

@SpringBootApplication
// cho phép swagger nhận accessToken
@SecurityScheme(
		name = "api",
		scheme = "bearer",
		type = SecuritySchemeType.HTTP,
		in = SecuritySchemeIn.HEADER
)
@OpenAPIDefinition(info = @Info(title = "AITasker SWAGGER", version = "1.0"))
@EnableConfigurationProperties(QdrantProperties.class)
<<<<<<< HEAD
=======
@EnableScheduling
>>>>>>> 4ceb432e65237a7ca034898d24e678aac4935384
public class Application {

	public static void main(String[] args) {
		SpringApplication.run(Application.class, args);
	}

}
