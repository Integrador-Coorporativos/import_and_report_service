package br.com.ifrn.ClassService.model;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import jakarta.persistence.*;

import java.util.List;

@Entity
@JsonIgnoreProperties({"hibernateLazyInitializer", "handler"})
public class Classes {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private int id;


    private String name;
    private String semester;
    private int gradleLevel;
    private String shift;

    @ManyToOne
    private Courses course;


    @OneToMany(mappedBy = "classe")
    private List<ClassComments> comments;

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getSemester() {
        return semester;
    }

    public void setSemester(String semester) {
        this.semester = semester;
    }

    public int getGradleLevel() {
        return gradleLevel;
    }

    public void setGradleLevel(int gradleLevel) {
        this.gradleLevel = gradleLevel;
    }

    public String getShift() {
        return shift;
    }

    public void setShift(String shift) {
        this.shift = shift;
    }

    public Courses getCourse() {
        return course;
    }

    public void setCourse(Courses course) {
        this.course = course;
    }

    public List<ClassComments> getComments() {
        return comments;
    }

    public void setComments(List<ClassComments> comments) {
        this.comments = comments;
    }

    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

}
