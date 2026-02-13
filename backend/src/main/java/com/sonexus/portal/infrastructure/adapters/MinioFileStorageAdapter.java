package com.sonexus.portal.infrastructure.adapters;

import com.sonexus.portal.domain.ports.FileStoragePort;
import io.minio.*;
import io.minio.errors.*;
import io.minio.http.Method;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Profile;
import org.springframework.stereotype.Service;

import jakarta.annotation.PostConstruct;
import java.io.IOException;
import java.io.InputStream;
import java.security.InvalidKeyException;
import java.security.NoSuchAlgorithmException;
import java.time.LocalDate;
import java.util.UUID;

/**
 * MinIO implementation of file storage for local/MVP deployment.
 * In production, replace with S3FileStorageAdapter.
 */
@Slf4j
@Service
@Profile({"default", "local", "test"})
@RequiredArgsConstructor
public class MinioFileStorageAdapter implements FileStoragePort {

    private final MinioClient minioClient;

    @Value("${app.minio.bucket-name}")
    private String bucketName;

    @PostConstruct
    public void init() {
        try {
            // Create bucket if it doesn't exist
            boolean bucketExists = minioClient.bucketExists(
                    BucketExistsArgs.builder().bucket(bucketName).build()
            );

            if (!bucketExists) {
                minioClient.makeBucket(
                        MakeBucketArgs.builder().bucket(bucketName).build()
                );
                log.info("Created MinIO bucket: {}", bucketName);
            } else {
                log.info("MinIO bucket already exists: {}", bucketName);
            }
        } catch (Exception e) {
            log.warn("MinIO not available - file storage operations will fail. Start MinIO or use Docker Compose for full functionality: {}", e.getMessage());
            // Don't throw exception - allow app to start without MinIO
        }
    }

    @Override
    public String storeFile(String fileName, String contentType, InputStream inputStream, long size) {
        try {
            // Generate unique file path: forms/YYYY/MM/uuid-filename
            String filePath = generateFilePath(fileName);

            minioClient.putObject(
                    PutObjectArgs.builder()
                            .bucket(bucketName)
                            .object(filePath)
                            .stream(inputStream, size, -1)
                            .contentType(contentType)
                            .build()
            );

            log.info("File stored successfully: {}", filePath);
            return filePath;

        } catch (Exception e) {
            log.error("Error storing file: {}", fileName, e);
            throw new RuntimeException("Failed to store file: " + fileName, e);
        }
    }

    @Override
    public InputStream retrieveFile(String filePath) {
        try {
            return minioClient.getObject(
                    GetObjectArgs.builder()
                            .bucket(bucketName)
                            .object(filePath)
                            .build()
            );
        } catch (Exception e) {
            log.error("Error retrieving file: {}", filePath, e);
            throw new RuntimeException("Failed to retrieve file: " + filePath, e);
        }
    }

    @Override
    public void deleteFile(String filePath) {
        try {
            minioClient.removeObject(
                    RemoveObjectArgs.builder()
                            .bucket(bucketName)
                            .object(filePath)
                            .build()
            );
            log.info("File deleted successfully: {}", filePath);
        } catch (Exception e) {
            log.error("Error deleting file: {}", filePath, e);
            throw new RuntimeException("Failed to delete file: " + filePath, e);
        }
    }

    @Override
    public boolean fileExists(String filePath) {
        try {
            minioClient.statObject(
                    StatObjectArgs.builder()
                            .bucket(bucketName)
                            .object(filePath)
                            .build()
            );
            return true;
        } catch (ErrorResponseException e) {
            if (e.errorResponse().code().equals("NoSuchKey")) {
                return false;
            }
            log.error("Error checking file existence: {}", filePath, e);
            throw new RuntimeException("Failed to check file existence: " + filePath, e);
        } catch (Exception e) {
            log.error("Error checking file existence: {}", filePath, e);
            throw new RuntimeException("Failed to check file existence: " + filePath, e);
        }
    }

    @Override
    public String generatePresignedUrl(String filePath, int expirationSeconds) {
        try {
            return minioClient.getPresignedObjectUrl(
                    GetPresignedObjectUrlArgs.builder()
                            .method(Method.GET)
                            .bucket(bucketName)
                            .object(filePath)
                            .expiry(expirationSeconds)
                            .build()
            );
        } catch (Exception e) {
            log.error("Error generating presigned URL: {}", filePath, e);
            throw new RuntimeException("Failed to generate presigned URL: " + filePath, e);
        }
    }

    /**
     * Generate unique file path with date-based organization
     */
    private String generateFilePath(String fileName) {
        LocalDate now = LocalDate.now();
        String uuid = UUID.randomUUID().toString();

        // Sanitize filename
        String sanitizedFileName = fileName.replaceAll("[^a-zA-Z0-9._-]", "_");

        return String.format("forms/%d/%02d/%s-%s",
                now.getYear(),
                now.getMonthValue(),
                uuid,
                sanitizedFileName
        );
    }
}
