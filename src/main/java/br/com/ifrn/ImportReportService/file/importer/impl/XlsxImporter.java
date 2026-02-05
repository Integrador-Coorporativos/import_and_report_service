package br.com.ifrn.ImportReportService.file.importer.impl;

import br.com.ifrn.ImportReportService.dto.DisciplineDetailDTO;
import br.com.ifrn.ImportReportService.dto.ImporterDTO;
import br.com.ifrn.ImportReportService.file.importer.contract.FileImporter;
import org.apache.poi.ss.usermodel.Cell;
import org.apache.poi.ss.usermodel.CellType;
import org.apache.poi.ss.usermodel.DataFormatter;
import org.apache.poi.ss.usermodel.Row;
import org.apache.poi.xssf.usermodel.XSSFSheet;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;
import org.springframework.stereotype.Component;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import java.io.InputStream;
import java.math.BigDecimal;
import java.util.*;

@Component
public class XlsxImporter implements FileImporter {
    private final DataFormatter formatter = new DataFormatter();
    private static final Map<String, String> CURSOS_MAP = Map.of(
            "09404", "ADS",
            "09401", "Informática"
    );

    @Override
    public List<ImporterDTO> importFile(InputStream inputStream) throws Exception {

        try (XSSFWorkbook workbook = new XSSFWorkbook(inputStream)) {
            XSSFSheet sheet = workbook.getSheetAt(0); //Pega a aba usada na planilha
            Iterator<Row> rowIterator = sheet.iterator();
            if (rowIterator.hasNext()) rowIterator.next(); //Pula a primeira linha da planilha

            return parseRowsToImportDTOList(rowIterator);

        }
    }

    private List<ImporterDTO> parseRowsToImportDTOList(Iterator<Row> rowIterator) {
        List<ImporterDTO>  importedData = new ArrayList<>();
        while (rowIterator.hasNext()) {
            Row row = rowIterator.next();
            if (isRowValid(row)) {
                importedData.add(parseRowToImporterDTO(row));
            } else {
                System.out.println("Linha ignorada, célula obrigatória ausente: " + row.getRowNum());
                continue; // pula linha inteira

                //Adicionar lógica para montar e retornar uma planilha com os dados incompletos para correção
            }
        }
        return importedData;
    }

    private ImporterDTO parseRowToImporterDTO(Row row) {
        ImporterDTO importerDTO = new ImporterDTO();
        importerDTO.setRegistration(getRawString(row, 0)); // Matrícula
        importerDTO.setName(getString(row, 1));         // Nome_Completo
        importerDTO.setClassId(getString(row, 2));      // Turma_ID
        importerDTO.setShift(getString(row, 3));        // Turno
        importerDTO.setSemester(getString(row, 4));
        importerDTO.setRejections(parseInt(row, 5));    // Reprovações
        importerDTO.setPresence(parseFloat(row, 6));    // Porcentagem_Presença
        importerDTO.setTotalLowGrades(parseInt(row, 7));    // Quantidade de Notas baixas
        importerDTO.setEmail(getString(row, 8));       // Email
        System.out.println("Email do Usuário: " + importerDTO.getEmail());
        importerDTO.setIra(parseFloat(row, 9));         // IRA
        importerDTO.setCourse(simplificarCurso(getString(row, 10)));       // Curso

        String jsonString = row.getCell(11).getStringCellValue();
        importerDTO.setDisciplineDetails(parseDisciplineDetails(jsonString));     // Detalhes da Disciplina

        System.out.println(importerDTO);

        return importerDTO;
    }

    private boolean isRowValid(Row row) {
        if (row == null) return false;

        // Definindo colunas obrigatórias
        int[] mandatoryColumns = {0, 1, 2, 3, 4, 5, 6, 7, 8, 9, 10};

        for (int col : mandatoryColumns) {
            Cell cell = row.getCell(col, Row.MissingCellPolicy.RETURN_BLANK_AS_NULL);
            if (cell == null) return false;

            String value = formatter.formatCellValue(cell);
            if (value == null || value.isBlank()) return false; // célula vazia
        }

        return true; // todas as células obrigatórias têm valor
    }

    private String getRawString(Row row, int index) {
        Cell cell = row.getCell(index, Row.MissingCellPolicy.RETURN_BLANK_AS_NULL);
        if (cell == null) return "";

        if (cell.getCellType() == CellType.NUMERIC) {
            // Converte o número sem notação científica
            return BigDecimal.valueOf(cell.getNumericCellValue())
                    .toPlainString()
                    .replace(".0", ""); // remove decimal se aparecer
        }

        return formatter.formatCellValue(cell);
    }

    private String getString(Row row, int index) {
        Cell cell = row.getCell(index, Row.MissingCellPolicy.CREATE_NULL_AS_BLANK);

        if (cell == null) {
            return "";
        }

        // Se a célula for uma fórmula, isso pega o resultado da fórmula
        if (cell.getCellType() == CellType.FORMULA) {
            return formatter.formatCellValue(cell);
        }

        // Garante que o conteúdo seja lido como texto puro
        cell.setCellType(CellType.STRING);
        return cell.getStringCellValue().trim();
    }

    private Float parseFloat(Row row, int index) {
        String value = getString(row, index);

        // Se estiver vazio, retorna 0.0f para evitar que o unboxing do Java quebre
        if (value == null || value.isBlank()) {
            return 0.0f;
        }

        try {
            // Limpeza agressiva: remove tudo que não for número, ponto ou sinal de menos
            value = value.replace(",", ".")
                    .replace("%", "")
                    .trim();

            return Float.parseFloat(value);
        } catch (NumberFormatException e) {
            System.err.println("ERRO DE CONVERSÃO: Coluna " + index + " tem valor inválido: [" + value + "]");
            return 0.0f; // Retorna padrão em caso de erro de digitação na planilha
        }
    }

    private Integer parseInt(Row row, int index) {
        String value = getString(row, index);
        if (value == null || value.isBlank()) return null;

        // em muitos Excel, números inteiros vêm como "12.0"
        if (value.contains(".")) {
            value = value.substring(0, value.indexOf("."));
        }

        return Integer.parseInt(value);
    }

    private List<DisciplineDetailDTO> parseDisciplineDetails(String jsonContent) {
        try {
            if (jsonContent == null || jsonContent.isEmpty()) {
                return Collections.emptyList();
            }

            ObjectMapper mapper = new ObjectMapper();
            return mapper.readValue(jsonContent, new TypeReference<List<DisciplineDetailDTO>>() {});
        } catch (Exception e) {
            // Logar o erro para saber qual linha da planilha falhou
            System.err.println("Erro ao converter detalhes da disciplina: " + e.getMessage());
            return Collections.emptyList();
        }
    }
    private String simplificarCurso(String cursoRaw) {
        if (cursoRaw == null || cursoRaw.isBlank()) {
            return "N/A";
        }
        String cursoTrim = cursoRaw.trim();
        if (cursoTrim.matches("^\\d+.*")) {
            String codigo = cursoTrim.split("\\s+")[0];
            return CURSOS_MAP.getOrDefault(codigo, cursoTrim);
        }
        return cursoTrim;
    }
}
