package br.com.ifrn.ImportReportService.services;

import org.apache.commons.io.output.ByteArrayOutputStream;
import org.apache.poi.ss.usermodel.Cell;
import org.apache.poi.ss.usermodel.Row;
import org.apache.poi.xssf.usermodel.XSSFSheet;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;
import org.springframework.stereotype.Service;
import java.io.FileNotFoundException;

@Service
public class GenerateTemplateService {

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
}
