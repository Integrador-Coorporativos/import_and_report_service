package br.com.ifrn.ClassService.services;

import br.com.ifrn.ClassService.model.ClassComments;
import br.com.ifrn.ClassService.repository.ClassCommentsRepository;
import br.com.ifrn.ClassService.repository.ClassesRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class ClassCommentsService {
    @Autowired
    private ClassCommentsRepository commentRepository;

    @Autowired
    private ClassesRepository classesRepository;

    public List<ClassComments> getByTurma(Integer turmaId) {
        return commentRepository.findAll().stream()
                .filter(c -> c.getClasse().getId() == turmaId)
                .toList();
    }

    public ClassComments create(ClassComments comment) {
        if (comment.getComment() == null) {
            throw new IllegalArgumentException("Comentário não pode ser nulo");
        }
        if (comment.getComment().isEmpty()) {
            throw new IllegalArgumentException("Comentário não pode ser vazio");
        }
        if (comment.getComment().length() > 255) {
            throw new IllegalArgumentException("Comentário não pode exceder 255 caracteres");
        }
        if (comment.getProfessorId() <= 0) {
            throw new IllegalArgumentException("Professor ID deve ser maior que zero");
        }
        return commentRepository.save(comment);
    }
    public void delete(Integer id) { commentRepository.deleteById(id); }

}
