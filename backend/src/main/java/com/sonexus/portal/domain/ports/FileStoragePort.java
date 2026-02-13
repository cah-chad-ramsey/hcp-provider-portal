package com.sonexus.portal.domain.ports;

import java.io.InputStream;

/**
 * Port for file storage operations.
 * Implementations: MinioFileStorageAdapter (local), S3FileStorageAdapter (cloud)
 */
public interface FileStoragePort {

    /**
     * Store file and return storage path
     * @param fileName original file name
     * @param contentType MIME type
     * @param inputStream file content
     * @param size file size in bytes
     * @return storage path (e.g., "forms/2024/01/uuid-filename.pdf")
     */
    String storeFile(String fileName, String contentType, InputStream inputStream, long size);

    /**
     * Retrieve file as input stream
     * @param filePath storage path returned from storeFile
     * @return input stream of file content
     */
    InputStream retrieveFile(String filePath);

    /**
     * Delete file
     * @param filePath storage path
     */
    void deleteFile(String filePath);

    /**
     * Check if file exists
     */
    boolean fileExists(String filePath);

    /**
     * Generate pre-signed URL for temporary access (optional, for cloud implementations)
     */
    default String generatePresignedUrl(String filePath, int expirationSeconds) {
        throw new UnsupportedOperationException("Pre-signed URLs not supported by this adapter");
    }
}
