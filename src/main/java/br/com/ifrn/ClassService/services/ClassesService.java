package br.com.ifrn.ClassService.services;

import br.com.ifrn.ClassService.model.*;
import br.com.ifrn.ClassService.repository.ClassesRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Optional;

@Service
public class ClassesService {

    @Autowired
    ClassesRepository classesRepository;

    public List<Classes> getAll() { return classesRepository.findAll(); }
    public Optional<Classes> getById(Integer id) { return classesRepository.findById(id); }
    public Classes create(Classes turma) { return classesRepository.save(turma); }
    public Classes update(Integer id, Classes turmaDetails) {
        Classes turma = classesRepository.findById(id).orElseThrow();
        turma.setName(turmaDetails.getName());
        turma.setCourse(turmaDetails.getCourse());
        return classesRepository.save(turma);
    }
    public boolean delete(Integer id) { classesRepository.deleteById(id);
        return false;
    }

}
