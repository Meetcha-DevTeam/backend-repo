package com.meetcha.auth.service;

import com.amazonaws.services.s3.AmazonS3;
import com.amazonaws.services.s3.model.CannedAccessControlList;
import com.amazonaws.services.s3.model.DeleteObjectRequest;
import com.amazonaws.services.s3.model.ObjectMetadata;
import com.amazonaws.services.s3.model.PutObjectRequest;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.web.server.ResponseStatusException;

import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

@Slf4j
@Service
@RequiredArgsConstructor
public class AwsS3Service {

    @Value("${cloud.aws.s3.bucket}")
    private String bucket;

    private final AmazonS3 amazonS3;

    /**
     * 다중 파일 업로드
     */
    public List<String> uploadFiles(List<MultipartFile> multipartFiles) {
        List<String> uploadedUrls = new ArrayList<>();

        if (multipartFiles == null || multipartFiles.isEmpty()) {
            return uploadedUrls;
        }

        for (MultipartFile file : multipartFiles) {
            uploadedUrls.add(uploadFile(file));
        }

        return uploadedUrls;
    }

    /**
     * 단일 파일 업로드
     */
    public String uploadFile(MultipartFile multipartFile) {
        if (multipartFile == null || multipartFile.isEmpty()) {
            return null;
        }

        String fileName = createUniqueFileName(multipartFile.getOriginalFilename());
        ObjectMetadata metadata = new ObjectMetadata();
        metadata.setContentLength(multipartFile.getSize());
        metadata.setContentType(multipartFile.getContentType());

        putObjectToS3(fileName, multipartFile, metadata);

        // 업로드된 파일의 접근 가능한 URL 반환
        return amazonS3.getUrl(bucket, fileName).toString();
    }

    /**
     * S3 업로드 공통 로직
     */
    private void putObjectToS3(String fileName, MultipartFile file, ObjectMetadata metadata) {
        try (InputStream inputStream = file.getInputStream()) {
            amazonS3.putObject(new PutObjectRequest(bucket, fileName, inputStream, metadata));

            log.info("✅ S3 업로드 성공: {}", fileName);
        } catch (IOException e) {
            log.error("❌ S3 업로드 실패 (file: {})", fileName, e);
            throw new ResponseStatusException(HttpStatus.INTERNAL_SERVER_ERROR, "파일 업로드에 실패했습니다: " + fileName);
        }
    }

    /**
     * S3 파일 삭제
     */
    public void deleteFile(String fileName) {
        try {
            amazonS3.deleteObject(new DeleteObjectRequest(bucket, fileName));
            log.info("🗑️ S3 파일 삭제 완료: {}", fileName);
        } catch (Exception e) {
            log.error("❌ S3 파일 삭제 실패 (file: {})", fileName, e);
            throw new ResponseStatusException(HttpStatus.INTERNAL_SERVER_ERROR, "파일 삭제에 실패했습니다: " + fileName);
        }
    }

    /**
     * 고유한 파일 이름 생성
     */
    private String createUniqueFileName(String originalFileName) {
        return UUID.randomUUID() + getFileExtension(originalFileName);
    }

    /**
     * 파일 확장자 추출
     */
    private String getFileExtension(String fileName) {
        try {
            return fileName.substring(fileName.lastIndexOf("."));
        } catch (StringIndexOutOfBoundsException e) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "잘못된 파일 형식입니다: " + fileName);
        }
    }
}
