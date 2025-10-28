package br.com.ifrn.ClassService.repository;

import br.com.ifrn.ClassService.model.Courses;
import org.springframework.data.jpa.repository.JpaRepository;

public interface CoursesRepository extends JpaRepository<Courses, Integer> {
}
