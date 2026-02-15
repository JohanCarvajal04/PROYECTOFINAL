package com.app.uteq.Services.Impl;

import com.app.uteq.Dtos.CStateRequest;
import com.app.uteq.Dtos.StateResponse;
import com.app.uteq.Dtos.UStateRequest;
import com.app.uteq.Repository.IStatesRepository;
import com.app.uteq.Services.IStatesService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
@RequiredArgsConstructor
public class StatesServiceImpl implements IStatesService {
    private final IStatesRepository repo;

    @Override
    public void createStates(CStateRequest request) {
        repo.spiState(
                request.getStatename(),
                request.getStatedescription(),
                request.getStatecategory()
        );
    }

    @Override
    public void updateStates(UStateRequest request) {
        repo.spuState(
                request.getIdstate(),
                request.getStatename(),
                request.getStatedescription(),
                request.getStatecategory()
        );
    }

    @Override
    public void deleteStates(Integer idstate) {
        repo.spdState(idstate);
    }

    @Override
    public List<StateResponse> listStates(String category) {
        List<Object[]> rows = repo.fnListStates(category);

        return rows.stream().map(r -> new StateResponse(
                toInt(r[0]),
                toStr(r[1]),
                toStr(r[2]),
                toStr(r[3])
        )).toList();
    }

    private Integer toInt(Object v) {
        if (v == null) return null;
        if (v instanceof Integer i) return i;
        if (v instanceof Number n) return n.intValue();
        throw new IllegalArgumentException("Tipo numérico no soportado: " + v.getClass());
    }

    private String toStr(Object v) {
        return v == null ? null : v.toString();
    }
}
