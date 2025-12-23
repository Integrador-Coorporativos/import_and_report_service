package br.com.ifrn.ImportReportService;

import io.minio.ListObjectsArgs;
import io.minio.MinioClient;
import io.minio.Result;
import io.minio.messages.Item;
import org.junit.jupiter.api.MethodOrderer;
import org.junit.jupiter.api.Order;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.TestMethodOrder;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.mock.web.MockMultipartFile;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.MvcResult;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;
// Adicione este import estático
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.csrf;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.jwt;
import static org.junit.jupiter.api.Assertions.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.multipart;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.header;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;
import br.com.ifrn.ImportReportService.messaging.dto.ImportMessageDTO;


@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
@TestMethodOrder(MethodOrderer.OrderAnnotation.class)
@AutoConfigureMockMvc
public class ProcessingControllerTest extends BaseIntegrationTest{

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private RabbitTemplate rabbitTemplate;

    @Autowired
    private MinioClient minioClient;

    private static Path cachedTemplatePath;

    @Test
    @Order(1)
    void shouldDownloadTemplateAndCacheFile() throws Exception {
        MvcResult result = mockMvc.perform(get("/api/processing/template").with(jwt()))
                .andExpect(status().isOk())
                .andExpect(header().string(
                        HttpHeaders.CONTENT_DISPOSITION,
                        "attachment; filename=template_importacao.xlsx"
                ))
                .andExpect(content().contentType(MediaType.APPLICATION_OCTET_STREAM))
                .andReturn();

        byte[] responseBytes = result.getResponse().getContentAsByteArray();

        cachedTemplatePath = Files.createTempFile("template-", ".xlsx");
        Files.write(cachedTemplatePath, responseBytes);

        assertTrue(Files.exists(cachedTemplatePath));
        assertTrue(Files.size(cachedTemplatePath) > 0);
    }

    @Test
    @Order(2)
    void shouldReuseCachedTemplateFileInAnotherTest() throws Exception {
        assertNotNull(cachedTemplatePath, "Template não foi gerado no teste anterior");
        assertTrue(Files.exists(cachedTemplatePath));

        byte[] cachedFile = Files.readAllBytes(cachedTemplatePath);
        assertTrue(cachedFile.length > 0);
    }
    @Test
    @Order(3)
    void shouldUploadCachedTemplateFileSuccessfully() throws Exception {
        // Verifica se o arquivo gerado pelo teste anterior existe
        assertNotNull(cachedTemplatePath, "O arquivo do teste anterior não foi gerado");
        assertTrue(Files.exists(cachedTemplatePath), "O arquivo do teste anterior não existe");

        // Cria o MockMultipartFile a partir do arquivo gerado
        MockMultipartFile multipartFile = new MockMultipartFile(
                "file", // nome do parâmetro do endpoint
                cachedTemplatePath.getFileName().toString(), // nome do arquivo
                MediaType.APPLICATION_OCTET_STREAM_VALUE,
                Files.readAllBytes(cachedTemplatePath) // conteúdo do arquivo
        );

        mockMvc.perform(multipart("/api/processing/uploadFile")
                        .file(multipartFile)
                        .with(jwt().jwt(j -> j.claim("sub", "usuario-teste")))
                )
                .andExpect(status().isCreated()) // Espera status 201 CREATED
                .andExpect(content().contentType(MediaType.APPLICATION_JSON)); // Retorno esperado: JSON
    }



    @Test
    @Order(4)
    void shouldConsumeMessageFromQueue() {
        // Tenta receber a mensagem publicada na fila
        ImportMessageDTO received = (ImportMessageDTO) rabbitTemplate.receiveAndConvert("planilha.class-service-producer.queue");

        // Valida que a mensagem existe
        assertNotNull(received, "Nenhuma mensagem foi recebida da fila");

        // Aqui você pode verificar campos específicos
        assertEquals("20231094040001", received.getRegistration());
        // continue com os asserts necessários
    }
    @Test
    @Order(5)
    void shouldHaveSavedUploadedFileInMinio() throws Exception {
        // arquivo original enviado no teste 3
        String originalFileName = cachedTemplatePath.getFileName().toString();

        MinioClient minioClient = getMinioClient();

        boolean found = false;

        Iterable<Result<Item>> objects = minioClient.listObjects(
                ListObjectsArgs.builder()
                        .bucket("test-files")
                        .recursive(true)
                        .build()
        );

        for (Result<Item> result : objects) {
            Item item = result.get();
            // verifica se termina com _<nomeOriginal>.xlsx
            if (item.objectName().endsWith("_" + originalFileName)) {
                found = true;
                System.out.println("Arquivo encontrado no MinIO: " + item.objectName());
                break;
            }
        }

        assertTrue(found, "Arquivo enviado não foi encontrado no MinIO");
    }

    @Test
    @Order(6)
    void shouldUploadImageToMinio() throws Exception {
        String originalName = "imagem-teste.png";
        MockMultipartFile mockFile = new MockMultipartFile(
                "image",
                originalName,
                MediaType.IMAGE_PNG_VALUE,
                new byte[]{0x0, 0x1, 0x2, 0x3}
        );

        mockMvc.perform(
                        multipart("/api/processing/uploadImage")
                                .file(mockFile) // Usando o objeto mockFile completo
                                .with(jwt().jwt(j -> j.claim("sub", "usuario-teste")))
                                .with(csrf())
                )
                .andExpect(status().isCreated());

        MinioClient minioClient = getMinioClient();
        Iterable<Result<Item>> objects = minioClient.listObjects(
                ListObjectsArgs.builder()
                        .bucket("test-images")
                        .recursive(true)
                        .build()
        );

        boolean found = false;
        for (Result<Item> result : objects) {
            Item item = result.get();
            String objectName = item.objectName();
            System.out.println("Arquivo real no MinIO: " + objectName);

            if (objectName.contains(originalName)) {
                found = true;
                break;
            }
        }
        assertTrue(found, "O arquivo foi salvo mas o teste não o encontrou (provavelmente caminho ou nome divergente)");
    }
}
