package com.app.uteq.Services;

import com.app.uteq.Dtos.CStateRequest;
import com.app.uteq.Dtos.StateResponse;
import com.app.uteq.Dtos.UStateRequest;

import java.util.List;

public interface IStatesService {
    void createStates(CStateRequest request);
    void updateStates(UStateRequest request);
    void deleteStates(Integer idstate);
    List<StateResponse> listStates(String category);
}
