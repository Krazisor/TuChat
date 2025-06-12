package com.thr.tuchat.service;

import com.thr.tuchat.config.MinioConfig;
import com.thr.tuchat.exception.ServiceDeniedException;
import io.minio.GetPresignedObjectUrlArgs;
import io.minio.MinioClient;
import io.minio.PutObjectArgs;
import io.minio.errors.*;
import io.minio.http.Method;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.net.URI;
import java.net.URL;
import java.util.Optional;
import java.util.UUID;

@Slf4j
@Service
@RequiredArgsConstructor
public class MinioService {

    private final MinioClient minioClient;
    private final MinioConfig minioConfig;

    /**
     * 获取临时访问链接
     * --------------只对minio的相关数据提供临时访问连接，其他来源的不提供----------------
     *
     * @param url 永久链接
     * @return String 临时链接
     */
    public String getTemporaryURL(String url) {
        try {
            URL input = new URI(url).toURL();
            String protocol = input.getProtocol();
            String host = input.getHost();
            StringBuilder base = new StringBuilder();
            base.append(protocol).append("://").append(host);
            if (!base.toString().equals(minioConfig.getEndpoint())) {
                return url;
            } else {
                String filePath = Optional.ofNullable(url)
                        .filter(u -> u.contains("/"))
                        .map(u -> u.substring(u.lastIndexOf('/') + 1))
                        .orElse("");
                return minioClient.getPresignedObjectUrl(
                        GetPresignedObjectUrlArgs.builder()
                                .method(Method.GET)
                                .bucket(minioConfig.getBucketName())
                                .object(filePath)
                                .expiry(60 * 60 * 2)
                                .build()
                );
            }
        } catch (Exception e) {
            log.error("URL解析失败: {}", url, e);
            throw new ServiceDeniedException("无效的URL地址");
        }
    }

    // 上传文件并返回访问URL
    public String upload(MultipartFile file) {
        try {
            // 生成 UUID 文件名并保留后缀
            String originalFilename = file.getOriginalFilename();
            String ext = "";

            if (originalFilename != null && originalFilename.contains(".")) {
                ext = originalFilename.substring(originalFilename.lastIndexOf("."));
            }
            String uuidFileName = UUID.randomUUID().toString().replace("-", "") + ext;

            // 上传
            minioClient.putObject(
                    PutObjectArgs.builder()
                            .bucket(minioConfig.getBucketName())
                            .object(uuidFileName)
                            .stream(file.getInputStream(), file.getSize(), -1)
                            .contentType(file.getContentType())
                            .build()
            );

            // 构建文件URL（外链）
            return minioConfig.getEndpoint() + "/" + minioConfig.getBucketName() + "/" + uuidFileName;
        } catch (Exception e) {
            throw new ServiceDeniedException(e.getMessage());
        }
    }
}