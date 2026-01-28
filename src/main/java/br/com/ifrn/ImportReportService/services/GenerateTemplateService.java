package br.com.ifrn.ImportReportService.services;

import org.apache.commons.io.output.ByteArrayOutputStream;
import org.apache.poi.ss.usermodel.Cell;
import org.apache.poi.ss.usermodel.Row;
import org.apache.poi.xssf.usermodel.XSSFSheet;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;
import org.springframework.stereotype.Service;

@Service
public class GenerateTemplateService {

    public byte[] getTemplate() throws Exception {
        try (XSSFWorkbook workbook = new XSSFWorkbook()) {
            XSSFSheet sheet = workbook.createSheet("Modelo Importação Alunos");

            // 1. Cabeçalhos baseados EXATAMENTE nos índices do seu parseRowToImporterDTO
            String[] columns = {
                    "Matrícula (0)",           // 0 - getRawString
                    "Nome_Completo (1)",       // 1 - getString
                    "Turma_ID (2)",            // 2 - getString
                    "Turno (3)",               // 3 - getString
                    "Reprovações (4)",         // 4 - parseInt
                    "Presença_% (5)",          // 5 - parseFloat
                    "Notas_Baixas_Qtd (6)",    // 6 - parseInt
                    "Email (7)",               // 7 - getString
                    "IRA (8)",                 // 8 - parseFloat
                    "Curso_Completo (9)",      // 9 - getString (será simplificado pelo seu método)
                    "Detalhes_JSON (10)"       // 10 - getStringCellValue
            };

            Row header = sheet.createRow(0);
            for (int i = 0; i < columns.length; i++) {
                Cell cell = header.createCell(i);
                cell.setCellValue(columns[i]);
            }

            // 2. Dados de Exemplo seguindo a nova estrutura
            // Dica: O índice 10 precisa de um JSON válido para não quebrar o seu parseDisciplineDetails
            String jsonExemplo = "[{\"disciplina\": \"Matemática\", \"nota\": 8.5}]";

            Object[][] exampleData = {
                    {
                            "20231094040001", "Ana Beatriz Souza", "20231.1.09404.1V", "Vespertino",
                            "0", "92.5", "1", "ana.souza@academico.ifrn.edu.br", "9.1",
                            "09404 - Tecnologia em Análise e Desenvolvimento de Sistemas (2012)", jsonExemplo
                    },
                    {
                            "20231094040002", "Lucas Oliveira", "20231.1.09404.1M", "Matutino",
                            "2", "75.0", "3", "lucas.oliveira@academico.ifrn.edu.br", "6.7",
                            "09401 - Técnico em Informática Integrado", jsonExemplo
                    }
            };

            for (int rowIndex = 0; rowIndex < exampleData.length; rowIndex++) {
                Row row = sheet.createRow(rowIndex + 1);
                for (int colIndex = 0; colIndex < exampleData[rowIndex].length; colIndex++) {
                    Object value = exampleData[rowIndex][colIndex];
                    row.createCell(colIndex).setCellValue(value == null ? "" : value.toString());
                }
            }

            // Auto ajustar colunas para facilitar a leitura
            for (int i = 0; i < columns.length; i++) {
                sheet.autoSizeColumn(i);
            }

            ByteArrayOutputStream out = new ByteArrayOutputStream();
            workbook.write(out);
            return out.toByteArray();

        } catch (Exception e) {
            // Logar o erro real ajuda no debug
            e.printStackTrace();
            throw new Exception("Erro ao gerar o template: " + e.getMessage());
        }
    }
}