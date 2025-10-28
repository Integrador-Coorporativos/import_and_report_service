package br.com.ifrn.ClassService.services;

import br.com.ifrn.ClassService.model.Courses;
import br.com.ifrn.ClassService.repository.CoursesRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Optional;

@Service
public class CoursesService {

    @Autowired
    private CoursesRepository coursesRepository;


    public List<Courses> getAll() { return coursesRepository.findAll(); }
    public Optional<Courses> getById(Integer id) { return coursesRepository.findById(id); }
    public Courses create(Courses course) { return coursesRepository.save(course); }
    public Courses update(Integer id, Courses courseDetails) {
        Courses course = coursesRepository.findById(id).orElseThrow();
        course.setName(courseDetails.getName());
        course.setDescription(courseDetails.getDescription());
        return coursesRepository.save(course);
    }
    public boolean delete(Integer id) { coursesRepository.deleteById(id);
        return false;
    }

}
