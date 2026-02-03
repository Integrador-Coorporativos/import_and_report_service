package br.com.ifrn.ImportReportService.controller;

import br.com.ifrn.ImportReportService.controller.docs.ProcessingControllerDocs;
import br.com.ifrn.ImportReportService.dto.ImporterDTO;
import br.com.ifrn.ImportReportService.services.GenerateTemplateService;
import br.com.ifrn.ImportReportService.services.ProcessingService;
import lombok.SneakyThrows;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/processing")
public class ProcessingController implements ProcessingControllerDocs {

    @Autowired
    private ProcessingService processingService;

    @Autowired
    private GenerateTemplateService  generateTemplateService;

    @SneakyThrows
    @GetMapping("/template")
    public ResponseEntity<byte[]> getTemplate() {
        HttpHeaders headers = new HttpHeaders();
        headers.add("Content-Disposition", "attachment; filename=template_importacao.xlsx");
        return ResponseEntity.ok()
                .headers(headers)
                .contentType(MediaType.APPLICATION_OCTET_STREAM)
                .body(generateTemplateService.getTemplate());
    }

    @GetMapping("/imports")
    public ResponseEntity<?> getImports(){
        return ResponseEntity.ok().build();
    }

    @GetMapping("/imports/{id}")
    public ResponseEntity<?> getImports(@PathVariable String id){
        return ResponseEntity.ok().build();
    }

    @SneakyThrows
    @PostMapping(
            value="/uploadFile",
            consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    public ResponseEntity<List<ImporterDTO>> uploadFile(@RequestParam("file") MultipartFile file) {
        return ResponseEntity.status(HttpStatus.CREATED).body(processingService.uploadFile(file));
    }

    @SneakyThrows
    @PostMapping(
            value="/uploadImage",
            consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    public ResponseEntity<Map<String, String>> uploadImage(@RequestParam("image") MultipartFile image) {
        return ResponseEntity.status(HttpStatus.CREATED).body(processingService.uploadImage(image));
    }

    @DeleteMapping("/imports/{id}")
    public ResponseEntity<?> deleteFile(@PathVariable String id) {
        return ResponseEntity.ok().build();
    }
}
