package com.app.uteq.Services;

import com.app.uteq.Dtos.CCareersRequest;
import com.app.uteq.Dtos.CareersResponse;
import com.app.uteq.Dtos.UCareersRequest;

import java.util.List;

public interface ICareersService {
    void createCareers(CCareersRequest request);
    void updateCareers(UCareersRequest request);
    void deleteCareers(Integer idcareer);
    List<CareersResponse> listCareers(Integer facultyid); // null => todos
}
