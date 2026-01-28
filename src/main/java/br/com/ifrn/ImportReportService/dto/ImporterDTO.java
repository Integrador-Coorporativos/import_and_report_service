package br.com.ifrn.ImportReportService.dto;

import jakarta.validation.constraints.Email;
import lombok.Getter;
import lombok.Setter;

import java.util.List;

@Getter @Setter
public class ImporterDTO {

    private String name;
    private String registration;
    @Email
    private String email;
    private String classId;
    private String course;
    private String shift;
    private String semester;
    private float presence;
    private List<DisciplineDetailDTO> disciplineDetails;
    private float ira;
    private Integer rejections;
    private Integer totalLowGrades;

}
