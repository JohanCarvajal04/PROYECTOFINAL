package com.app.uteq.Services;

import com.app.uteq.Dtos.CFacultyRequest;
import com.app.uteq.Dtos.FacultyResponse;
import com.app.uteq.Dtos.UFacultyRequest;

import java.util.List;

public interface IFacultiesService {
    void createFaculty(CFacultyRequest request);
    void updateFaculty(UFacultyRequest request);
    void deleteFaculty(Integer idfaculty);
    List<FacultyResponse> listFaculty();
}
