package com.app.uteq.Dtos;

import lombok.Data;

@Data
public class UStudentRequest {
    private Integer idStudent;
    private String semester;
    private String parallel;
    private Integer usersIdUser;
    private Integer careersIdCareer;
    private String status;
}
