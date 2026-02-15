package com.app.uteq.Services;

import com.app.uteq.Dtos.CProcessingStageRequest;
import com.app.uteq.Dtos.ProcessingStageResponse;
import com.app.uteq.Dtos.UProcessingStageRequest;

import java.util.List;

public interface IProcessingStageService {
    void createProcessingstage(CProcessingStageRequest request);
    void updateProcessingstage(UProcessingStageRequest request);
    void deleteProcessingstage(Integer idprocessingstage);
    List<ProcessingStageResponse> listProcessingstage();
}
