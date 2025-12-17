package br.com.ifrn.ImportReportService.config.minio;

import io.minio.BucketExistsArgs;
import io.minio.MakeBucketArgs;
import io.minio.MinioClient;
import jakarta.annotation.PostConstruct;
import lombok.Data;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Data // gera getter/setter automaticamente
@Configuration
public class MinioClientConfig {

    private MinioPropertiesConfig envMinio;

    // Spring vai injetar via setter
    public MinioClientConfig(MinioPropertiesConfig envMinio) {
        this.envMinio = envMinio;
    }

    @Bean
    public MinioClient minioClient() {
        return MinioClient.builder()
                .endpoint(envMinio.serverUrl())
                .credentials(envMinio.adminUser(), envMinio.adminPassword())
                .build();
    }

    // Bean para criar os buckets automaticamente
    @Bean
    public boolean createBuckets(MinioClient minioClient) throws Exception {
        if (!minioClient.bucketExists(BucketExistsArgs.builder().bucket(envMinio.bucketFiles()).build())) {
            minioClient.makeBucket(MakeBucketArgs.builder().bucket(envMinio.bucketFiles()).build());
            System.out.println("Bucket criado: " + envMinio.bucketFiles());
        } else {
            System.out.println("Bucket já existe: " + envMinio.bucketFiles());
        }

        if (!minioClient.bucketExists(BucketExistsArgs.builder().bucket(envMinio.bucketImages()).build())) {
            minioClient.makeBucket(MakeBucketArgs.builder().bucket(envMinio.bucketImages()).build());
            System.out.println("Bucket criado: " + envMinio.bucketImages());
        } else {
            System.out.println("Bucket já existe: " + envMinio.bucketImages());
        }

        return true;
    }
    public MinioClient createMinioClient() {
        return minioClient();
    }

}
