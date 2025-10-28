package br.com.ifrn.ClassService.repository;


import br.com.ifrn.ClassService.model.ClassComments;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface ClassCommentsRepository extends JpaRepository<ClassComments, Integer> {
    List<ClassComments> findByClasse_Id(Integer classeId);
}
