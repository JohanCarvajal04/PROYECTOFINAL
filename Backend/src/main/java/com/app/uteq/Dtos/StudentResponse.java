package com.app.uteq.Dtos;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDate;

@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class StudentResponse {
    private Integer idStudent;
    private String semester;
    private String parallel;
    private Integer userId;
    private String userName;
    private String userEmail;
    private Integer careerId;
    private String careerName;
    private LocalDate enrollmentDate;
    private String status;
}
