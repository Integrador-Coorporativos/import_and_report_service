package br.com.ifrn.ClassService.controller;

import br.com.ifrn.ClassService.model.ClassComments;
import br.com.ifrn.ClassService.model.Classes;
import br.com.ifrn.ClassService.services.ClassCommentsService;
import br.com.ifrn.ClassService.services.ClassesService;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.server.ResponseStatusException;

import java.util.List;

@RestController
@RequestMapping("/api/classes/{id}/comments")
@Tag(name = "Comments", description = "Operações relacionadas a comentários")
public class ClassCommentsController {

    @Autowired
    private ClassCommentsService commentService;

    @Autowired
    ClassesService classesService;

    @GetMapping
    public ResponseEntity<List<ClassComments>> getByClass(@PathVariable Integer id) {
        List<ClassComments> comments = commentService.getByTurma(id);
        if (comments.isEmpty()) {
            return ResponseEntity.noContent().build();
        }
        return ResponseEntity.ok(comments);
    }

    @PostMapping
    public ResponseEntity<ClassComments> create(@PathVariable Integer id, @RequestBody ClassComments comment) {
        // Recupera a classe pelo ID
        Classes classe = classesService.getById(id)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Classe não encontrada"));

        // Seta a classe no comentário
        comment.setClasse(classe);

        // Cria o comentário
        ClassComments createdComment = commentService.create(comment);

        // Retorna resposta 201 CREATED
        return ResponseEntity.status(HttpStatus.CREATED).body(createdComment);
    }
}
