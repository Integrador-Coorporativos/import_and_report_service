package br.com.ifrn.ClassService.dto.response;


import br.com.ifrn.ClassService.model.ClassComments;
import br.com.ifrn.ClassService.model.Courses;
import lombok.Getter;
import lombok.Setter;

import java.util.List;

@Getter @Setter
public class ResponseClassDTO {
    private int id;
    private String name;
    private String semester;
    private int gradleLevel;
    private String shift;
    private Courses course;
    private List<ClassComments> comments;
}
