package br.com.ifrn.ClassService.messaging.dto;

import lombok.Data;

import java.io.Serializable;

@Data
public class CreateClassMessageDTO implements Serializable {
    private static final long serialVersionUID = 1L;

    private String classId;
    private String userId;
    private float presence;
    private float average;
    private  float ira;
    private  Integer rejections;

}
