package br.com.ifrn.ImportReportService.services;

import com.github.javafaker.Faker;
import org.apache.commons.io.output.ByteArrayOutputStream;
import org.apache.poi.ss.usermodel.*;
import org.apache.poi.xssf.usermodel.XSSFSheet;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;
import org.springframework.stereotype.Service;
import java.util.Locale;
import java.util.Random;

/**
 * Serviço responsável por gerar um template Excel de alunos do IFRN com dados fictícios.
 * A lógica de geração segue distribuições estatísticas específicas para simular alunos
 * com diferentes desempenhos acadêmicos.
 */
@Service
public class GenerateTemplateService {

    private final Faker faker = new Faker(new Locale("pt-BR"));
    private final Random random = new Random();

    // Dados reais do IFRN para diversificar o mock
    private final String[] CURSOS = {"09401 - Informática", "09404 - ADS", "09408 - Edificações", "09410 - Alimentos"};
    private final String[] TURNOS = {"Matutino", "Vespertino", "Noturno"};
    private final String[] CAMPUS_CODES = {"1M", "1V", "1N", "2M"};

    /**
     * Gera um arquivo Excel (.xlsx) contendo 50 registros de alunos mockados.
     * * @return byte[] Conteúdo do arquivo Excel para download.
     * @throws Exception Caso ocorra erro na escrita do arquivo.
     */
    public byte[] getTemplate() throws Exception {
        try (XSSFWorkbook workbook = new XSSFWorkbook()) {
            XSSFSheet sheet = workbook.createSheet("Modelo Importação Alunos");

            String[] columns = {
                    "Matrícula", "Nome_Completo", "Turma_ID", "Turno",
                    "periodo_referencia", "Reprovações",
                    "Presença_%", "Notas_Baixas_Qtd", "Email", "IRA", "Curso_Completo", "Detalhes_JSON"
            };

            // Estilo de Cabeçalho
            Font headerFont = workbook.createFont();
            headerFont.setBold(true);
            CellStyle headerStyle = workbook.createCellStyle();
            headerStyle.setFont(headerFont);

            Row headerRow = sheet.createRow(0);
            for (int i = 0; i < columns.length; i++) {
                Cell cell = headerRow.createCell(i);
                cell.setCellValue(columns[i]);
                cell.setCellStyle(headerStyle);
            }

            // Gerar 50 registros para testar bem a volumetria
            for (int i = 1; i <= 50; i++) {
                generateStudentRow(sheet.createRow(i));
            }

            for (int i = 0; i < columns.length; i++) {
                sheet.autoSizeColumn(i);
            }

            ByteArrayOutputStream out = new ByteArrayOutputStream();
            workbook.write(out);
            return out.toByteArray();

        } catch (Exception e) {
            e.printStackTrace();
            throw new Exception("Erro ao gerar template: " + e.getMessage());
        }
    }

    /**
     * Preenche uma linha do Excel com dados de um aluno baseando-se em probabilidades:
     * <ul>
     * <li><b>Reprovações/Notas Baixas:</b> 10% de chance de ocorrer (1 em 10).</li>
     * <li><b>IRA Baixo (< 80):</b> 12.5% de chance de ocorrer (1 em 8).</li>
     * <li><b>Presença Baixa (< 80%):</b> 20% de chance de ocorrer (1 em 5).</li>
     * <li><b>Bom Desempenho:</b> Alunos que não caem nas chances acima possuem notas altas e frequência > 90%.</li>
     * </ul>
     * * @param row A linha do Excel a ser preenchida.
     */
    private void generateStudentRow(Row row) {
        String cursoInfo = CURSOS[random.nextInt(CURSOS.length)];
        String codCurso = cursoInfo.split(" - ")[0];
        String turno = TURNOS[random.nextInt(TURNOS.length)];

        // Gera um ClassId único: Ano + Semestre + CodCurso + TurmaAleatoria + TurnoCampus
        String classId = String.format("20251.1.%s.%03d.%s",
                codCurso, random.nextInt(999), CAMPUS_CODES[random.nextInt(CAMPUS_CODES.length)]);

        // --- Lógica de Reprovações e Notas Baixas (1 em 10) ---
        int rejections = (random.nextInt(10) == 0) ? random.nextInt(4) + 1 : 0;
        int lowGrades = (random.nextInt(10) == 0) ? random.nextInt(3) + 1 : 0;

        // --- Lógica de Presença (1 em 5 de ser baixa) ---
        double presenceValue;
        if (random.nextInt(5) == 0) {
            presenceValue = 50 + (random.nextDouble() * 29); // Baixa: 50 a 79
        } else {
            presenceValue = 85 + (random.nextDouble() * 15); // Alta: 85 a 100
        }

        // --- Lógica de IRA (1 em 8 de ser baixo) ---
        double iraValue;
        if (random.nextInt(8) == 0) {
            iraValue = 20 + (random.nextDouble() * 59); // Baixo: 20 a 79
        } else {
            iraValue = 80 + (random.nextDouble() * 20); // Alto: 80 a 100
        }

        row.createCell(0).setCellValue("20251" + codCurso + faker.number().digits(4)); // Matrícula
        row.createCell(1).setCellValue(faker.name().fullName().toUpperCase());        // Nome
        row.createCell(2).setCellValue(classId);                                      // Turma_ID
        row.createCell(3).setCellValue(turno);                                        // Turno
        row.createCell(4).setCellValue(random.nextInt(8) + 1 + "°");                  // Período
        row.createCell(5).setCellValue(String.valueOf(rejections));                   // Reprovações
        row.createCell(6).setCellValue(String.format("%.2f%%", presenceValue));       // Presença %
        row.createCell(7).setCellValue(String.valueOf(lowGrades));                    // Notas Baixas
        row.createCell(8).setCellValue(faker.internet().emailAddress());               // Email
        row.createCell(9).setCellValue(String.format("%.2f", iraValue));              // IRA
        row.createCell(10).setCellValue(cursoInfo);                                     // Nome Curso
        row.createCell(11).setCellValue(generateDisciplinesJson());                   // JSON
    }

    private String generateDisciplinesJson() {
        StringBuilder json = new StringBuilder("[");
        int numDiscs = random.nextInt(3) + 3;

        for (int i = 0; i < numDiscs; i++) {
            json.append("{")
                    .append("\"diario\":\"").append(faker.number().digits(6)).append("\",")
                    .append("\"disciplina\":\"").append(faker.educator().course()).append("\",")
                    .append("\"carga_horaria\":\"60 aulas\",")
                    .append("\"total_aulas\":\"60\",")
                    .append("\"total_faltas\":\"").append(random.nextInt(12)).append("\",")
                    .append("\"frequencia\":\"").append(random.nextInt(20) + 80).append("%\",")
                    .append("\"situacao\":\"").append(random.nextBoolean() ? "Aprovado" : "Cursando").append("\",")
                    .append("\"mfd\":\"").append(random.nextInt(40) + 60).append("\"")
                    .append("}");
            if (i < numDiscs - 1) json.append(",");
        }
        json.append("]");
        return json.toString();
    }
}