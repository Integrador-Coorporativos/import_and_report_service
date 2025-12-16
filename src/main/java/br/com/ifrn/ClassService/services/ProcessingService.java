package br.com.ifrn.ClassService.services;

import br.com.ifrn.ClassService.dto.ImporterDTO;
import br.com.ifrn.ClassService.file.importer.contract.FileImporter;
import br.com.ifrn.ClassService.file.importer.factory.FileImporterFactory;
import br.com.ifrn.ClassService.messaging.dto.CreateClassMessageDTO;
import br.com.ifrn.ClassService.messaging.producer.MessageProducer;
import io.minio.ObjectWriteResponse;
import jakarta.ws.rs.core.Response;
import lombok.SneakyThrows;
import org.apache.commons.io.output.ByteArrayOutputStream;
import org.apache.coyote.BadRequestException;
import org.apache.poi.ss.usermodel.Cell;
import org.apache.poi.ss.usermodel.Row;
import org.apache.poi.xssf.usermodel.XSSFSheet;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;
import org.keycloak.representations.idm.UserRepresentation;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;
import java.io.FileNotFoundException;
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

    public byte[] getTemplate() throws Exception {

        try (XSSFWorkbook workbook = new XSSFWorkbook()) {

            XSSFSheet sheet = workbook.createSheet("Modelo Importação Alunos");

            // Criar cabeçalho
            Row header = sheet.createRow(0);

            String[] columns = {
                    "Nome_Completo",
                    "Matricula",
                    "Turma_ID",
                    "Curso",
                    "Turno",
                    "Semestre",
                    "Porcentagem_Presença",
                    "Média_Geral",
                    "IRA",
                    "Reprovações"
            };

            for (int i = 0; i < columns.length; i++) {
                Cell cell = header.createCell(i);
                cell.setCellValue(columns[i]);
            }

            // Criar linhas de exemplo
            Object[][] exampleData = {
                    {"Ana Beatriz Souza", "20231094040001", "20231.1.09404.1V", "ADS", "Vespertino", "5º", 92, 84, 91, 0},
                    {"Lucas Oliveira", "20231094040002", "20231.1.09404.1V", "Informática", "Matutino", "3º", 74, 61, 67, 2},
                    {"Maria Ferreira", "20231094040003", "20231.1.09404.1M", "Química", "Vespertino", "2º", 88, 75, 83, 1},
                    {"João Santos", "20231094040004", "20231.1.09404.1V", "Alimentos", "Matutino", "6º", 65, 52, 57, 3}
            };

            for (int rowIndex = 0; rowIndex < exampleData.length; rowIndex++) {
                Row row = sheet.createRow(rowIndex + 1); // começa na linha 1

                for (int colIndex = 0; colIndex < exampleData[rowIndex].length; colIndex++) {
                    row.createCell(colIndex).setCellValue(
                            exampleData[rowIndex][colIndex] == null
                                    ? ""
                                    : exampleData[rowIndex][colIndex].toString()
                    );
                }
            }

            // Auto ajustar colunas
            for (int i = 0; i < columns.length; i++) {
                sheet.autoSizeColumn(i);
            }

            // Converter para array de bytes
            ByteArrayOutputStream out = new ByteArrayOutputStream();
            workbook.write(out);
            return out.toByteArray();

        } catch (Exception e) {
            throw new FileNotFoundException("Erro ao gerar o arquivo: " + e);
            // TODO: substituir por exceção personalizada
        }
    }


    public ResponseEntity<?> getImports(){
        return ResponseEntity.ok().build();
    }

    public ResponseEntity<?> getImports(String id){
        return ResponseEntity.ok().build();
    }

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
        String userId = findOrCreateUser(importerDTO);


//        Classes classes = classesService.createOrUpdateClassByClassId(
//                importerDTO.getCourse(),
//                importerDTO.getSemester(),
//                1,
//                importerDTO.getClassId(),
//                importerDTO.getShift(),
//                userId
//                );

        CreateClassMessageDTO producerMessageDTO = new CreateClassMessageDTO();
        producerMessageDTO.setAverage(importerDTO.getAverage());
        producerMessageDTO.setClassId(importerDTO.getClassId());
        producerMessageDTO.setIra(importerDTO.getIra());
        producerMessageDTO.setRejections(importerDTO.getRejections());
        producerMessageDTO.setUserId(userId);

        messageProducer.sendMessage(producerMessageDTO);

        ImporterDTO responseImporterDTO = new ImporterDTO();
//        responseImporterDTO.setClasses(classes);



        //Adicionar chamadas para criação de outros Objetos

        return responseImporterDTO;

    }

    private String findOrCreateUser(ImporterDTO importerDTO) {
        String userId = "";
        UserRepresentation user = keycloakAdminService.findKeycloakUser(importerDTO.getRegistration());
        if (user == null){
            Response response = keycloakAdminService.createKeycloakUser(importerDTO.getRegistration(), importerDTO.getName());

            if (response.getStatus() == 201) {//fazer lógica de atualização de dados do usuário

                userId = response.getLocation().getPath().replaceAll(".*/([^/]+)$", "$1");
                System.out.println("Usuário criado com ID: " + userId);
            } else {
                System.out.println("Erro ao criar usuário: " + response.getStatus() + " " + response.getStatusInfo());
                System.out.println(response.readEntity(String.class));
            }
        }else{
            userId = user.getId();
        }
        return userId;
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
}
