package com.meetcha.auth.controller;

import com.meetcha.auth.service.AwsS3Service;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.io.InputStream;

@Slf4j
@RestController
@RequiredArgsConstructor
@RequestMapping("/file")
public class AwsS3Controller {

    private final AwsS3Service awsS3Service;

    @PostMapping
    public ResponseEntity<String> uploadFile(@RequestParam("file") MultipartFile file) {
        try (InputStream inputStream = file.getInputStream()) {
            String fileName = awsS3Service.createUniqueFileName(file.getOriginalFilename());
            String url = awsS3Service.uploadFromStream(inputStream, fileName, file.getContentType());
            return ResponseEntity.ok(url);
        } catch (Exception e) {
            log.error("파일 업로드 실패", e);
            throw new RuntimeException("파일 업로드 실패", e);
        }
    }

    @DeleteMapping
    public ResponseEntity<String> deleteFile(@RequestParam String fileName) {
        awsS3Service.deleteFile(fileName);
        return ResponseEntity.ok(fileName);
    }
}
