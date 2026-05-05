package com.everage.eshop.service;

import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;
import software.amazon.awssdk.services.s3.S3Client;

import java.util.Collections;
import java.util.List;

/**
 * No-op implementation of StorageService when R2 is not configured.
 * Returns empty lists and logs warnings.
 */
@Service
@Slf4j
@ConditionalOnMissingBean(S3Client.class)
public class NoOpStorageService extends StorageService {

    public NoOpStorageService() {
        super(null, null);
        log.warn("R2 storage is not configured. File uploads will be disabled.");
    }

    @Override
    public String upload(MultipartFile file, String folder) {
        log.warn("R2 not configured - skipping file upload: {}", file.getOriginalFilename());
        return "";
    }

    @Override
    public void delete(String keyOrUrl) {
        log.warn("R2 not configured - skipping file deletion: {}", keyOrUrl);
    }

    @Override
    public void deleteAll(List<String> keysOrUrls) {
        log.warn("R2 not configured - skipping {} file deletions", keysOrUrls.size());
    }

    @Override
    public String toPublicUrl(String key) {
        return key;
    }

    @Override
    public List<String> toPublicUrls(List<String> keys) {
        return keys;
    }
}
