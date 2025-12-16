package br.com.ifrn.ClassService.config.minio;

import io.minio.MinioClient;
import lombok.Data;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Data
@Configuration
public class MinioClientConfig {

    @Autowired
    MinioPropertiesConfig envMinio;

    @Bean
    MinioClient minioClient() {
        return MinioClient.builder().endpoint(envMinio.serverUrl())
                .credentials(envMinio.adminUser(), envMinio.adminPassword())
                .build();
    }

    public MinioClient createMinioClient() {
        return minioClient();
    }

}
