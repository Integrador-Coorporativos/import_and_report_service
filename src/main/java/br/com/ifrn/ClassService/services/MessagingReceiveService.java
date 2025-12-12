package br.com.ifrn.ClassService.services;

import br.com.ifrn.ClassService.messaging.dto.CreateClassMessageDTO;
import br.com.ifrn.ClassService.model.Classes;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
@Service
public class MessagingReceiveService {

//    @Autowired
//    private ClassesService classesService;
//    @Autowired
//    private CoursesService coursesService;
//
//    public Classes procMessage(CreateClassMessageDTO classMessageDTO) {
//        Classes classes = classesService.createOrUpdateClassByClassId(
//                classMessageDTO.getCourseName(),
//                classMessageDTO.getSemester(),
//                1, //definir futuramente
//                classMessageDTO.getClassId(),
//                classMessageDTO.getShift(),
//                classMessageDTO.getUserId()
//        );
//        System.out.println("Classe Criada: " + classes);
//        return  classes;
//    }
}
