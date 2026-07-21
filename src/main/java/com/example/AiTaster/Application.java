package com.example.AiTaster;

import com.example.AiTaster.config.QdrantProperties;
import io.swagger.v3.oas.annotations.OpenAPIDefinition;
import io.swagger.v3.oas.annotations.enums.SecuritySchemeIn;
import io.swagger.v3.oas.annotations.enums.SecuritySchemeType;
import io.swagger.v3.oas.annotations.info.Info;
import io.swagger.v3.oas.annotations.security.SecurityScheme;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.jackson.Jackson2ObjectMapperBuilderCustomizer;
import org.springframework.boot.autoconfigure.orm.jpa.HibernatePropertiesCustomizer;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.scheduling.annotation.EnableScheduling;

import java.util.TimeZone;

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

	private static final String VIETNAM_TIME_ZONE = "Asia/Ho_Chi_Minh";

	public static void main(String[] args) {
		TimeZone.setDefault(TimeZone.getTimeZone(VIETNAM_TIME_ZONE));
		SpringApplication.run(Application.class, args);
	}

	@Bean
	public HibernatePropertiesCustomizer hibernateTimeZoneCustomizer() {
		return hibernateProperties ->
				hibernateProperties.put("hibernate.jdbc.time_zone", VIETNAM_TIME_ZONE);
	}

	@Bean
	public Jackson2ObjectMapperBuilderCustomizer jacksonTimeZoneCustomizer() {
		return builder -> builder.timeZone(TimeZone.getTimeZone(VIETNAM_TIME_ZONE));
	}

}
