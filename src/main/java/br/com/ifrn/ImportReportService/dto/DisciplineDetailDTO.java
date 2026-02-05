package br.com.ifrn.ImportReportService.dto;

import jakarta.validation.constraints.*;

public record DisciplineDetailDTO(
        @NotBlank(message = "Diário é obrigatório")
        @Pattern(regexp = "^\\d+$", message = "Diário deve ser numérico")
        String diario,

        @NotBlank(message = "Disciplina é obrigatória")
        String disciplina,

        @NotBlank(message = "Carga horária é obrigatória")
        String carga_horaria,

        @NotBlank(message = "Total de aulas é obrigatório")
        @Pattern(regexp = "^\\d+$", message = "Total de aulas deve ser numérico")
        String total_aulas,

        @NotBlank(message = "Total de faltas é obrigatório")
        @Pattern(regexp = "^\\d+$", message = "Total de faltas deve ser numérico")
        String total_faltas,

        @NotBlank(message = "Frequência é obrigatória")
        String frequencia,

        @NotBlank(message = "Situação é obrigatória")
        String situacao,

        @NotBlank(message = "MFD é obrigatório")
        String mfd
) {}