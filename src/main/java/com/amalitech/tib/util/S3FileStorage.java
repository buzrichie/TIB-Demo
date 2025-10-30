package com.amalitech.tib.util;

import com.amalitech.tib.config.AWSConfig;
import com.amalitech.tib.exception.FileStorageException;
import java.io.IOException;
import java.util.Objects;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;
import org.springframework.web.multipart.MultipartFile;
import software.amazon.awssdk.core.sync.RequestBody;
import software.amazon.awssdk.services.s3.S3Client;
import software.amazon.awssdk.services.s3.model.DeleteObjectRequest;
import software.amazon.awssdk.services.s3.model.PutObjectRequest;
import software.amazon.awssdk.services.s3.model.S3Exception;

/** Service class for managing file uploads to S3. */
@Slf4j
@Service
@RequiredArgsConstructor
public class S3FileStorage {

  private final S3Client s3Client;
  private final AWSConfig awsConfig;

  public String uploadFile(MultipartFile file, String s3Key) {
    try {
      validateFile(file);

      uploadToS3(file, s3Key);

      return generateUrl(s3Key);

    } catch (IllegalArgumentException e) {
      log.warn("File validation failed: {}", e.getMessage());
      throw e;
    } catch (IOException e) {
      log.error("Failed to read file content: {}", e.getMessage());
      throw new FileStorageException("Failed to process file content");
    } catch (Exception e) {
      log.error("Unexpected error during file upload: {}", e.getMessage(), e);
      throw new FileStorageException("An unexpected error occurred during file upload");
    }
  }

  public void deleteFile(String fileKey) {
    try {
      DeleteObjectRequest deleteObjectRequest =
          DeleteObjectRequest.builder().bucket(awsConfig.getS3Bucket()).key(fileKey).build();

      s3Client.deleteObject(deleteObjectRequest);
      log.info("Successfully deleted file from S3: {}", fileKey);
    } catch (S3Exception e) {
      log.error(
          "Error deleting file from S3: {} - {}", fileKey, e.awsErrorDetails().errorMessage());
      throw new FileStorageException("Failed to delete file");
    }
  }

  private static void validateFile(MultipartFile file) {
    if (Objects.isNull(file) || file.isEmpty() || StringUtils.isEmpty(file.getOriginalFilename())) {
      throw new IllegalArgumentException("File must not be null, empty, or have a blank filename");
    }
  }

  private void uploadToS3(MultipartFile file, String s3Key) throws IOException {
    PutObjectRequest putObjectRequest =
        PutObjectRequest.builder()
            .bucket(awsConfig.getS3Bucket())
            .key(s3Key)
            .contentType(file.getContentType())
            .contentLength(file.getSize())
            .build();

    s3Client.putObject(
        putObjectRequest, RequestBody.fromInputStream(file.getInputStream(), file.getSize()));
  }

  private String generateUrl(String s3Key) {
    if ("cloudfront".equals(awsConfig.getGetEndpoint()) && awsConfig.getDistributionId() != null) {
      return String.format("https://%s.cloudfront.net/%s", awsConfig.getDistributionId(), s3Key);
    } else {
      return String.format(
          "https://%s.s3.%s.amazonaws.com/%s",
          awsConfig.getS3Bucket(), awsConfig.getRegion(), s3Key);
    }
  }

  public String extractS3Key(String imageUrl, String imagePath) {
    if (!imageUrl.contains(imagePath)) {
      throw new IllegalArgumentException("Image path not found in image url");
    }
    return imageUrl.substring(imageUrl.indexOf(imagePath));
  }
}
