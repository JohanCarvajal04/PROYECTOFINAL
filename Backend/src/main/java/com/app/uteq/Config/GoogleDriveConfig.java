package com.app.uteq.Config;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.Collections;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.io.ClassPathResource;

import com.google.api.client.http.javanet.NetHttpTransport;
import com.google.api.client.json.gson.GsonFactory;
import com.google.api.services.drive.Drive;
import com.google.api.services.drive.DriveScopes;
import com.google.auth.http.HttpCredentialsAdapter;
import com.google.auth.oauth2.GoogleCredentials;

@Configuration
public class GoogleDriveConfig {
    @Value("${google.drive.credentials.path}")
    private String credentialsPath;

    @Bean
    public Drive googleDriveService() throws IOException {
        InputStream credentialsStream;

        // Si es ruta absoluta en el filesystem, leer como archivo
        File file = new File(credentialsPath);
        if (file.isAbsolute() && file.exists()) {
            credentialsStream = new FileInputStream(file);
        } else {
            // Si no, intentar como recurso del classpath
            String path = credentialsPath.startsWith("classpath:")
                    ? credentialsPath.substring("classpath:".length())
                    : credentialsPath;
            credentialsStream = new ClassPathResource(path).getInputStream();
        }

        GoogleCredentials credentials = GoogleCredentials.fromStream(credentialsStream)
                .createScoped(Collections.singleton(DriveScopes.DRIVE_FILE));

        return new Drive.Builder(
                new NetHttpTransport(),
                GsonFactory.getDefaultInstance(),
                new HttpCredentialsAdapter(credentials))
                .setApplicationName("SGTE-UTEQ")
                .build();
    }
}
