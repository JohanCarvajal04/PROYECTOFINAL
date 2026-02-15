package com.app.uteq.Dtos;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class FacultyResponse {
    private Integer idfaculty;
    private String facultyname;
    private String facultycode;
    private Integer deaniduser;
}
