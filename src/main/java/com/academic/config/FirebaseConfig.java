package com.academic.config;

import com.google.auth.oauth2.GoogleCredentials;
import com.google.firebase.FirebaseApp;
import com.google.firebase.FirebaseOptions;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.io.ClassPathResource;

import jakarta.annotation.PostConstruct;
import java.io.IOException;
import java.io.InputStream;


@Configuration
@Slf4j
public class FirebaseConfig {

    private static final String SERVICE_ACCOUNT_PATH =
            "templates/helixion-innovation-firebase-adminsdk-fbsvc-49e59e63af.json";

    @PostConstruct
    public void initializeFirebase() {
        if (!FirebaseApp.getApps().isEmpty()) {
            log.info("Firebase already initialized – skipping");
            return;
        }

        try {
            InputStream serviceAccount =
                    new ClassPathResource(SERVICE_ACCOUNT_PATH).getInputStream();

            FirebaseOptions options = FirebaseOptions.builder()
                    .setCredentials(GoogleCredentials.fromStream(serviceAccount))
                    .build();

            FirebaseApp.initializeApp(options);
            log.info("Firebase Admin SDK initialized successfully from: {}", SERVICE_ACCOUNT_PATH);

        } catch (IOException e) {
            log.error("Failed to initialize Firebase Admin SDK: {}", e.getMessage(), e);
            throw new RuntimeException("Firebase initialization failed – check service-account file", e);
        }
    }
}
