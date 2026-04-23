package com.everage.eshop.service;

import com.everage.eshop.config.R2Properties;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;
import software.amazon.awssdk.core.sync.RequestBody;
import software.amazon.awssdk.services.s3.S3Client;
import software.amazon.awssdk.services.s3.model.DeleteObjectRequest;
import software.amazon.awssdk.services.s3.model.PutObjectRequest;

import java.io.IOException;
import java.util.List;
import java.util.UUID;

@Service
@Slf4j
@RequiredArgsConstructor
public class StorageService {

    private final S3Client s3Client;
    private final R2Properties r2Properties;

    /**
     * Uploads a file to R2 under the given folder prefix.
     *
     * @param file   multipart file to upload
     * @param folder e.g. "items" or "collections"
     * @return public URL of the uploaded file
     */
    public String upload(MultipartFile file, String folder) {
        String key = folder + "/" + UUID.randomUUID() + "_" + sanitizeFilename(file.getOriginalFilename());
        try {
            PutObjectRequest request = PutObjectRequest.builder()
                    .bucket(r2Properties.bucket())
                    .key(key)
                    .contentType(file.getContentType())
                    .contentLength(file.getSize())
                    .build();

            s3Client.putObject(request, RequestBody.fromInputStream(file.getInputStream(), file.getSize()));
            log.info("Uploaded file to R2: {}", key);
        } catch (IOException e) {
            throw new RuntimeException("Failed to upload file: " + file.getOriginalFilename(), e);
        }

        return r2Properties.publicUrl() + "/" + key;
    }

    /**
     * Deletes a file from R2 by its public URL.
     *
     * @param publicUrl full public URL of the file
     */
    public void delete(String publicUrl) {
        String prefix = r2Properties.publicUrl() + "/";
        if (!publicUrl.startsWith(prefix)) {
            log.warn("Skipping delete — URL not managed by R2: {}", publicUrl);
            return;
        }
        String key = publicUrl.substring(prefix.length());
        s3Client.deleteObject(DeleteObjectRequest.builder()
                .bucket(r2Properties.bucket())
                .key(key)
                .build());
        log.info("Deleted file from R2: {}", key);
    }

    /**
     * Deletes multiple files from R2 by their public URLs.
     */
    public void deleteAll(List<String> publicUrls) {
        publicUrls.forEach(this::delete);
    }

    private String sanitizeFilename(String filename) {
        if (filename == null) return "file";
        return filename.replaceAll("[^a-zA-Z0-9._-]", "_");
    }
}
