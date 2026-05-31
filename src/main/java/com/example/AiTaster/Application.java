package com.example.AiTaster;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

import io.github.cdimascio.dotenv.Dotenv;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
@SpringBootApplication
public class Application {

	public static void main(String[] args) {
		Dotenv dotenv = Dotenv.configure()
				.ignoreIfMissing()
				.load();

		String geminiKey = dotenv.get("GEMINI_API_KEY");

		if (geminiKey != null) {
			System.setProperty("GEMINI_API_KEY", geminiKey);
			System.out.println("Gemini API key loaded: " + geminiKey.substring(0, 8) + "...");
		} else {
			System.out.println("Gemini API key NOT FOUND");
		}
		SpringApplication.run(Application.class, args);
	}

}
