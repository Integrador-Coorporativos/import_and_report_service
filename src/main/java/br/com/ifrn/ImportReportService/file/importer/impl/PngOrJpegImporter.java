package br.com.ifrn.ImportReportService.file.importer.impl;



import br.com.ifrn.ImportReportService.dto.ImporterDTO;
import br.com.ifrn.ImportReportService.file.importer.contract.FileImporter;

import java.io.InputStream;
import java.util.List;

public class PngOrJpegImporter implements FileImporter {
    @Override
    public List<ImporterDTO> importFile(InputStream inputStream) throws Exception {
        return List.of();
    }
}
