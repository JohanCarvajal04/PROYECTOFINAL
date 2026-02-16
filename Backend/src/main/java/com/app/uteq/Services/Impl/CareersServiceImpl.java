package com.app.uteq.Services.Impl;

import java.util.List;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.app.uteq.Dtos.CCareersRequest;
import com.app.uteq.Dtos.CareersResponse;
import com.app.uteq.Dtos.UCareersRequest;
import com.app.uteq.Repository.ICareersRepository;
import com.app.uteq.Services.ICareersService;

import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
@Transactional
public class CareersServiceImpl implements ICareersService {
    private final ICareersRepository repository;

    @Override
    public void createCareers(CCareersRequest request) {
        repository.spiCareer(
                request.getCareername(),
                request.getCareercode(),
                request.getFacultiesidfaculty(),
                request.getCoordinatoriduser()
        );
    }

    @Override
    public void updateCareers(UCareersRequest request) {
        repository.spuCareer(
                request.getIdcareer(),
                request.getCareername(),
                request.getCareercode(),
                request.getFacultiesidfaculty(),
                request.getCoordinatoriduser()
        );
    }

    @Override
    public void deleteCareers(Integer idcareer) {
        repository.spdCareer(idcareer);
    }

    @Override
    @Transactional(readOnly = true)
    public List<CareersResponse> listCareers(Integer facultyid) {
        List<Object[]> rows = repository.fnListCareers(facultyid);

        return rows.stream().map(r -> new CareersResponse(
                toInt(r[0]),  // idcareer
                toStr(r[1]),  // careername
                toStr(r[2]),  // careercode
                toInt(r[3]),  // facultiesidfaculty
                toStr(r[4]),  // facultyname (JOIN)
                toInt(r[5])   // coordinatoriduser (puede null)
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
