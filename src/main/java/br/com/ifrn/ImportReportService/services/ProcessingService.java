package br.com.ifrn.ImportReportService.services;

import br.com.ifrn.ImportReportService.config.security.SecurityContextService;
import br.com.ifrn.ImportReportService.dto.ImporterDTO;
import br.com.ifrn.ImportReportService.file.importer.contract.FileImporter;
import br.com.ifrn.ImportReportService.file.importer.factory.FileImporterFactory;
import br.com.ifrn.ImportReportService.mapper.ImporterMapper;
import br.com.ifrn.ImportReportService.messaging.dto.ImportMessageDTO;
import br.com.ifrn.ImportReportService.messaging.producer.MessageProducer;
import io.minio.ObjectWriteResponse;
import lombok.SneakyThrows;
import org.apache.coyote.BadRequestException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;
import java.io.IOException;
import java.io.InputStream;
import java.util.*;

@Service
public class ProcessingService {

    @Autowired
    FileImporterFactory importer;

    @Autowired
    MinioService minioService;

    @Autowired
    MessageProducer messageProducer;

    @Autowired
    KeycloakAdminService keycloakAdminService;

    @Autowired
    SecurityContextService securityContextService;

    @Autowired
    ImporterMapper mapper;

    public List<ImporterDTO> uploadFile(MultipartFile file) throws IOException {
        if (file.isEmpty()) throw new BadRequestException("Please set a valid file");

        try (InputStream inputStream = file.getInputStream()) {
            String fileName = Optional.ofNullable(file.getOriginalFilename())
                    .orElseThrow(() -> new BadRequestException("File mame cannot be null"));

            FileImporter importer = this.importer.getFileImporter(fileName);

            List<ImporterDTO> dataImporter = importer.importFile(inputStream).stream().toList();

            List<ImporterDTO> responseImporterDTOList = new ArrayList<>();
            for (ImporterDTO importerDTO : dataImporter){
                responseImporterDTOList.add(processImporterDTO(importerDTO));
            };

            try (InputStream uploadStream = file.getInputStream()) {
                minioService.uploadFile(uploadStream, fileName);
            }
            return responseImporterDTOList;

        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    public ResponseEntity<?> deleteFile(String id) {
        return ResponseEntity.ok().build();
    }

    public ImporterDTO processImporterDTO(ImporterDTO importerDTO) throws Exception {
        String userId = keycloakAdminService.findOrCreateUser(importerDTO);

        ImportMessageDTO producerMessageDTO = mapper.toCreateClassMessageDTO(importerDTO);
        producerMessageDTO.setUserId(userId);

        messageProducer.sendMessage(producerMessageDTO);

        ImporterDTO responseImporterDTO = mapper.toImporterDTO(producerMessageDTO);

        return responseImporterDTO;

    }

    @SneakyThrows
    public Map<String, String> uploadImage(MultipartFile file) {
        String fileName = file.getOriginalFilename();
        ObjectWriteResponse response = minioService.uploadImgage(file, fileName);

        // Monta um mapinha básico para o Front
        Map<String, String> data = new HashMap<>();
        data.put("bucket", response.bucket());
        data.put("object", response.object());
        data.put("etag", response.etag());

        // Opcional: Atualiza o Keycloak apenas com o path
        String userId = securityContextService.getCurrentUserId();
        keycloakAdminService.updateKeycloakPicture(userId, response.object());

        return data;
    }

    public ResponseEntity<?> getImports(){
        return ResponseEntity.ok().build();
    }

    public ResponseEntity<?> getImports(String id){
        return ResponseEntity.ok().build();
    }
}
