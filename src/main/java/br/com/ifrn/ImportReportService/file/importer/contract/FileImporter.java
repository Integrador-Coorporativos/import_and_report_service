package br.com.ifrn.ImportReportService.file.importer.contract;

import br.com.ifrn.ImportReportService.dto.ImporterDTO;

import java.io.InputStream;
import java.util.List;

public interface FileImporter {
    List<ImporterDTO> importFile(InputStream inputStream) throws Exception;
}
