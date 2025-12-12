package br.com.ifrn.ClassService.repository;

import br.com.ifrn.ClassService.model.Classes;
import org.springframework.data.jpa.repository.JpaRepository;

public interface ClassesRepository extends JpaRepository<Classes, Integer> {
    Classes findByClassId(String classId);
}
