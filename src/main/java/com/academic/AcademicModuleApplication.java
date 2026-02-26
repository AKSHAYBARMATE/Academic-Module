package com.academic;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

import java.util.TimeZone;

@SpringBootApplication
public class AcademicModuleApplication {

	static {
		// Force JVM timezone to IST (UTC+5:30) so all LocalDate.now() / LocalTime.now()
		// calls always return India time regardless of the server's OS timezone.
		TimeZone.setDefault(TimeZone.getTimeZone("Asia/Kolkata"));
	}

	public static void main(String[] args) {
		SpringApplication.run(AcademicModuleApplication.class, args);
	}

}
