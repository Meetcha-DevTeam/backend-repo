package com.meetcha.auth.controller;

import com.meetcha.auth.service.AwsS3Service;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

@RestController
@RequiredArgsConstructor
@RequestMapping("/file")
public class AwsS3Controller {

    private final AwsS3Service awsS3Service;

    @PostMapping
    public ResponseEntity<?> uploadFile(MultipartFile multipartFile){
        return ResponseEntity.ok((awsS3Service.uploadFile(multipartFile)));
    }

    @DeleteMapping
    public ResponseEntity<?> deleteFile(@RequestParam String fileName){
        awsS3Service.deleteFile(fileName);
        return ResponseEntity.ok(fileName);
    }

}