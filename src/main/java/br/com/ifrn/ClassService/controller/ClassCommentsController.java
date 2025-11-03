package br.com.ifrn.ClassService.controller;

import br.com.ifrn.ClassService.model.ClassComments;
import br.com.ifrn.ClassService.model.Classes;
import br.com.ifrn.ClassService.services.ClassCommentsService;
import br.com.ifrn.ClassService.services.ClassesService;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.annotation.security.PermitAll;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.server.ResponseStatusException;

import java.util.List;

@RestController
@RequestMapping("/api/classes/{classId}/comments")
@Tag(name = "Comments", description = "Operações relacionadas a comentários")
public class ClassCommentsController {

    @Autowired
    private ClassCommentsService commentService;

    @Autowired
    private ClassesService classesService;

    @GetMapping
    public ResponseEntity<List<ClassComments>> getByClass(@PathVariable Integer classId) {
        List<ClassComments> comments = commentService.getByTurma(classId);
        if (comments.isEmpty()) {
            return ResponseEntity.noContent().build();
        }
        return ResponseEntity.ok(comments);
    }

    @PostMapping
    public ResponseEntity<ClassComments> create(@PathVariable Integer classId, @RequestBody ClassComments comment) {
        Classes classe = classesService.getById(classId)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Classe não encontrada"));

        comment.setClasse(classe);

        ClassComments createdComment = commentService.create(comment);
        return ResponseEntity.status(HttpStatus.CREATED).body(createdComment);
    }

    @PutMapping("/{commentId}")
    public ResponseEntity<ClassComments> update(@PathVariable Integer classId, @PathVariable Integer commentId, @RequestBody ClassComments comment) {
        comment.setId(commentId);
        return ResponseEntity.ok(commentService.update(comment));
    }

    @DeleteMapping("/{commentId}")
    public ResponseEntity<Void> delete(@PathVariable Integer classId, @PathVariable Integer commentId) {

        commentService.delete(commentId);
        return ResponseEntity.noContent().build();
    }
}
