package com.leo.pillpathbackend;

import org.modelmapper.ModelMapper;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.Bean;

import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;

@SpringBootApplication
public class PillpathBackendApplication {

    public static void main(String[] args) {
        // Load .env file for local development
        try {
            loadEnvFile();
        } catch (Exception e) {
            System.out.println("No .env file found, using system environment variables");
        }

        SpringApplication.run(PillpathBackendApplication.class, args);
    }

    @Bean
    public ModelMapper modelMapper() {
        return new ModelMapper();
    }

    private static void loadEnvFile() throws Exception {
        Path envFile = Paths.get(".env");
        if (Files.exists(envFile)) {
            Files.lines(envFile)
                    .filter(line -> line.contains("=") && !line.startsWith("#"))
                    .forEach(line -> {
                        String[] parts = line.split("=", 2);
                        System.setProperty(parts[0].trim(), parts[1].trim());
                    });
        }
    }
}