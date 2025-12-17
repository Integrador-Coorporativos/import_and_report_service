package br.com.ifrn.ImportReportService.services;

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
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

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
    public ObjectWriteResponse uploadImage(@RequestParam("image") MultipartFile image) {
        try (InputStream uploadStream = image.getInputStream()) {
            String fileName = Optional.ofNullable(image.getOriginalFilename())
                    .orElseThrow(() -> new BadRequestException("Image mame cannot be null"));
            ObjectWriteResponse response = minioService.uploadImgage(uploadStream, fileName);
            return response;
        }catch (Exception e) {throw new Exception("Erro ao processar o arquivo: " + e);}
    }

    public ResponseEntity<?> getImports(){
        return ResponseEntity.ok().build();
    }

    public ResponseEntity<?> getImports(String id){
        return ResponseEntity.ok().build();
    }
}
