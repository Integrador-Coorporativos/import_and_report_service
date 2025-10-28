package br.com.ifrn.ClassService.controller;

import br.com.ifrn.ClassService.model.Classes;
import br.com.ifrn.ClassService.services.ClassesService;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Optional;

@RestController
@RequestMapping("/api/classes")
@Tag(name = "Classes", description = "Operações relacionadas a Turmas")
public class ClassesController {

    @Autowired
    private ClassesService classesService;

    @GetMapping
    public ResponseEntity<List<Classes>> getAll() {
        List<Classes> classesList = classesService.getAll();
        if (classesList.isEmpty()) {
            return ResponseEntity.noContent().build();
        }
        return ResponseEntity.ok(classesList);
    }

    @GetMapping("/{id}")
    public ResponseEntity<Classes> getById(@PathVariable Integer id) {
        Optional<Classes> classes = classesService.getById(id);
        return classes.map(ResponseEntity::ok)
                .orElseGet(() -> ResponseEntity.status(HttpStatus.NOT_FOUND).body(null));
    }

    @PostMapping
    public ResponseEntity<Classes> create(@RequestBody Classes classes) {
        Classes createdClasses = classesService.create(classes);
        return ResponseEntity.status(HttpStatus.CREATED).body(createdClasses);
    }

    @PutMapping("/{id}")
    public ResponseEntity<Classes> update(@PathVariable Integer id, @RequestBody Classes classes) {
        Optional<Classes> updatedClasses = Optional.ofNullable(classesService.update(id, classes));
        return updatedClasses.map(ResponseEntity::ok)
                .orElseGet(() -> ResponseEntity.status(HttpStatus.NOT_FOUND).body(null));
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> delete(@PathVariable Integer id) {
        boolean deleted = classesService.delete(id);
        if (deleted) {
            return ResponseEntity.noContent().build();
        } else {
            return ResponseEntity.status(HttpStatus.NOT_FOUND).build();
        }
    }
}
