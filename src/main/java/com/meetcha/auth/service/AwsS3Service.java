package com.meetcha.auth.service;

import com.amazonaws.services.s3.AmazonS3;
import com.amazonaws.services.s3.model.DeleteObjectRequest;
import com.amazonaws.services.s3.model.ObjectMetadata;
import com.amazonaws.services.s3.model.PutObjectRequest;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.web.server.ResponseStatusException;

import java.io.IOException;
import java.io.InputStream;
import java.net.URL;
import java.util.UUID;

@Slf4j
@Service
@RequiredArgsConstructor
public class AwsS3Service {

    @Value("${cloud.aws.s3.bucket}")
    private String bucket;

    private final AmazonS3 amazonS3;

    //파일 업로드
    public String uploadFromStream(InputStream inputStream, String fileName, String contentType) {
        try {
            ObjectMetadata metadata = new ObjectMetadata();
            metadata.setContentType(contentType);

            amazonS3.putObject(new PutObjectRequest(bucket, fileName, inputStream, metadata));

            URL url = amazonS3.getUrl(bucket, fileName);
            log.info("S3 업로드 성공: {}", url);
            return url.toString();
        } catch (Exception e) {
            log.error("S3 업로드 실패", e);
            throw new ResponseStatusException(HttpStatus.INTERNAL_SERVER_ERROR, "파일 업로드 실패");
        }
    }

    public void deleteFile(String fileName) {
        try {
            amazonS3.deleteObject(new DeleteObjectRequest(bucket, fileName));
            log.info("S3 파일 삭제 완료: {}", fileName);
        } catch (Exception e) {
            throw new ResponseStatusException(HttpStatus.INTERNAL_SERVER_ERROR, "파일 삭제 실패");
        }
    }

    // 고유 파일명 생성 */
    public String createUniqueFileName(String originalFileName) {
        return UUID.randomUUID() + getFileExtension(originalFileName);
    }

    private String getFileExtension(String fileName) {
        try {
            return fileName.substring(fileName.lastIndexOf("."));
        } catch (StringIndexOutOfBoundsException e) {
            return ".jpg"; // 기본 확장자
        }
    }
}
