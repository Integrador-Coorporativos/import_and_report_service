package br.com.ifrn.ImportReportService.services;

import br.com.ifrn.ImportReportService.config.minio.MinioClientConfig;
import br.com.ifrn.ImportReportService.config.security.SecurityContextService;
import br.com.ifrn.ImportReportService.file.ContentTypes;
import io.minio.MinioClient;
import io.minio.ObjectWriteResponse;
import io.minio.PutObjectArgs;
import lombok.SneakyThrows;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.MediaType;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.oauth2.core.oidc.user.OidcUser;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.security.oauth2.server.resource.authentication.JwtAuthenticationToken;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.io.InputStream;
import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

@Service
public class MinioService {

    @Autowired
    private MinioClientConfig  minioClientConfig;

    @Autowired
    SecurityContextService securityContextService;

    @Autowired
    KeycloakAdminService  keycloakAdminService;

    @SneakyThrows
    public ObjectWriteResponse uploadFile(InputStream uploadStream, String fileName) throws IOException {
        MinioClient minioClient = minioClientConfig.createMinioClient();

        String userId = securityContextService.getCurrentUserId(); // identificação do usuário que enviou

        String objectName = userId + "/" + UUID.randomUUID() + "_" + fileName;
        // pesquisar metodo de pegar o id do usuario apos implementar autenticacao

        Map<String, String> meta = new HashMap<>();
        meta.put("uploaded-by", userId);
        ObjectWriteResponse response = minioClient.putObject(
                PutObjectArgs.builder()
                        .bucket(minioClientConfig.getEnvMinio().bucketFiles())
                        .object(objectName)
                        .stream(uploadStream, uploadStream.available(), -1)
                        .contentType(ContentTypes.APPLICATION_XLSX_VALUE)
                        .headers(meta)
                        .build()
        );
        return response;
    }

    @SneakyThrows
    public ObjectWriteResponse uploadImgage(MultipartFile file, String fileName) throws IOException {
        MinioClient minioClient = minioClientConfig.createMinioClient();
        String userId = securityContextService.getCurrentUserId();

        String objectName = userId + "/" + fileName;

        // 1. Extraia o Content-Type real do arquivo vindo do frontend
        String contentType = file.getContentType();
        // Se 'file.getContentType()' for nulo, use "image/jpeg" como fallback
        if (contentType == null) contentType = "image/jpeg";

        Map<String, String> meta = new HashMap<>();
        meta.put("uploaded-by", userId);

        return minioClient.putObject(
                PutObjectArgs.builder()
                        .bucket(minioClientConfig.getEnvMinio().bucketImages())
                        .object(objectName)
                        .stream(file.getInputStream(), file.getSize(), -1) // 2. Use file.getSize() em vez de available()
                        .contentType(contentType) // 3. O segredo está aqui!
                        .userMetadata(meta) // Use userMetadata para metadados personalizados
                        .build()
        );
    }
}
