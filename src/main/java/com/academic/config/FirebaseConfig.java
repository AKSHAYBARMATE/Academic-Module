package com.academic.config;

import com.google.auth.oauth2.GoogleCredentials;
import com.google.firebase.FirebaseApp;
import com.google.firebase.FirebaseOptions;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.io.ClassPathResource;
import org.springframework.core.io.FileSystemResource;
import org.springframework.core.io.Resource;

import jakarta.annotation.PostConstruct;
import java.io.InputStream;

@Configuration
@Slf4j
public class FirebaseConfig {

    @Value("${firebase.service-account-json-path:templates/helixion-innovation-firebase-adminsdk-fbsvc-49e59e63af.json}")
    private String serviceAccountPath;

    @PostConstruct
    public void initializeFirebase() {
        if (!FirebaseApp.getApps().isEmpty()) {
            log.info("Firebase already initialized – skipping");
            return;
        }

        try {
            Resource resource = new ClassPathResource(serviceAccountPath);
            if (!resource.exists()) {
                resource = new FileSystemResource(serviceAccountPath);
            }

            if (!resource.exists()) {
                log.warn("⚠️ Firebase service account file [{}] not found. Firebase Push Notifications will be disabled.", serviceAccountPath);
                return;
            }

            try (InputStream serviceAccount = resource.getInputStream()) {
                FirebaseOptions options = FirebaseOptions.builder()
                        .setCredentials(GoogleCredentials.fromStream(serviceAccount))
                        .build();

                FirebaseApp.initializeApp(options);
                log.info("✅ Firebase Admin SDK initialized successfully from: {}", serviceAccountPath);
            }

        } catch (Exception e) {
            log.warn("⚠️ Firebase initialization skipped: {}. Push notifications will be disabled.", e.getMessage());
        }
    }
}
