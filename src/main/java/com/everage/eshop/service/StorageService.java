package com.everage.eshop.service;

import com.everage.eshop.config.R2Properties;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.autoconfigure.condition.ConditionalOnBean;
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
@ConditionalOnBean(S3Client.class)
public class StorageService {

    private final S3Client s3Client;
    private final R2Properties r2Properties;

    /**
     * Uploads a file to R2 under the given folder prefix.
     *
     * @param file   multipart file to upload
     * @param folder e.g. "items" or "collections"
     * @return storage key of the uploaded file (e.g. "items/uuid_filename.jpg")
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

        return key;
    }

    /**
     * Converts a storage key to a full public URL.
     *
     * @param key storage key e.g. "items/uuid_filename.jpg"
     * @return full public URL e.g. "https://media.everage.com/items/uuid_filename.jpg"
     */
    public String toPublicUrl(String key) {
        if (key == null) return null;
        // Already a full URL (legacy data) — return as-is
        if (key.startsWith("http://") || key.startsWith("https://")) return key;
        return r2Properties.publicUrl() + "/" + key;
    }

    /**
     * Converts a list of storage keys to full public URLs.
     */
    public List<String> toPublicUrls(List<String> keys) {
        if (keys == null) return List.of();
        return keys.stream().map(this::toPublicUrl).toList();
    }

    /**
     * Deletes a file from R2 by its storage key or legacy public URL.
     *
     * @param keyOrUrl storage key (e.g. "items/uuid_file.jpg") or legacy full URL
     */
    public void delete(String keyOrUrl) {
        if (keyOrUrl == null) return;
        String key;
        String prefix = r2Properties.publicUrl() + "/";
        if (keyOrUrl.startsWith("http://") || keyOrUrl.startsWith("https://")) {
            if (!keyOrUrl.startsWith(prefix)) {
                log.warn("Skipping delete — URL not managed by R2: {}", keyOrUrl);
                return;
            }
            key = keyOrUrl.substring(prefix.length());
        } else {
            key = keyOrUrl;
        }
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
