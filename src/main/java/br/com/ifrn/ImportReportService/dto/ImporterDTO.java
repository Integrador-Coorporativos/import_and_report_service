package br.com.ifrn.ImportReportService.dto;

import jakarta.validation.Valid;
import jakarta.validation.constraints.*;
import lombok.Getter;
import lombok.Setter;

import java.util.List;

@Getter @Setter
public class ImporterDTO {

    @NotBlank(message = "O nome não pode estar vazio")
    @Size(min = 3, max = 100)
    private String name;

    @NotBlank(message = "A matrícula é obrigatória")
    @Pattern(regexp = "^\\d{10,20}$", message = "A matrícula deve conter apenas números (10 a 20 dígitos)")
    private String registration;

    @NotBlank(message = "O e-mail é obrigatório")
    @Email(message = "Formato de e-mail inválido")
    private String email;

    @NotBlank(message = "O classId não pode ser vazio")
    private String classId;

    @NotBlank(message = "O curso deve ser informado")
    private String course;

    @NotBlank(message = "O turno é obrigatório")
    private String shift;

    @NotBlank(message = "O semestre/série é obrigatório")
    private String semester;

    @DecimalMin(value = "0.0", message = "Presença não pode ser negativa")
    @DecimalMax(value = "100.0", message = "Presença não pode exceder 100%")
    private float presence;

    @NotEmpty(message = "A lista de disciplinas não pode estar vazia")
    @Valid // Essencial para validar os objetos dentro da lista!
    private List<DisciplineDetailDTO> disciplineDetails;

    @PositiveOrZero(message = "O IRA deve ser um valor positivo")
    private float ira;

    @Min(value = 0, message = "O número de reprovações não pode ser negativo")
    private Integer rejections;

    @Min(value = 0, message = "O total de notas baixas não pode ser negativo")
    private Integer totalLowGrades;
}
