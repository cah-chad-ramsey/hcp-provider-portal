package com.sonexus.portal.config;

import io.minio.MinioClient;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Profile;

@Slf4j
@Configuration
@Profile({"default", "local", "test"})
public class MinioConfig {

    @Value("${app.minio.endpoint}")
    private String minioUrl;

    @Value("${app.minio.access-key}")
    private String accessKey;

    @Value("${app.minio.secret-key}")
    private String secretKey;

    @Bean
    public MinioClient minioClient() {
        log.info("Initializing MinIO client with URL: {}", minioUrl);

        return MinioClient.builder()
                .endpoint(minioUrl)
                .credentials(accessKey, secretKey)
                .build();
    }
}
