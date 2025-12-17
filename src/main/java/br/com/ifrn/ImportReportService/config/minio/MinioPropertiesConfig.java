package br.com.ifrn.ImportReportService.config.minio;


import org.springframework.boot.context.properties.ConfigurationProperties;

@ConfigurationProperties(prefix = "minio")
public record MinioPropertiesConfig(
        String serverUrl,
        String adminUser,
        String adminPassword,
        String bucketFiles,
        String bucketImages
) {}
