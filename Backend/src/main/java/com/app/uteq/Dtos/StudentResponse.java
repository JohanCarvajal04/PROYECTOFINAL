package com.app.uteq.Dtos;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDate;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class StudentResponse {
    private Integer idStudent;
    private String semester;
    private String parallel;
    private Integer usersIdUser;
    private Integer careersIdCareer;
    private LocalDate enrollmentDate;
    private String status;
}
