package com.app.uteq.Services.Impl;

import java.util.List;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.app.uteq.Dtos.CFacultyRequest;
import com.app.uteq.Dtos.FacultyResponse;
import com.app.uteq.Dtos.UFacultyRequest;
import com.app.uteq.Repository.IFacultiesRepository;
import com.app.uteq.Services.IFacultiesService;

import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
@Transactional
public class FacultiesServiceImpl implements IFacultiesService {
    private final IFacultiesRepository repository;

    @Override
    public void createFaculty(CFacultyRequest request) {
        repository.spiFaculty(
                request.getFacultyname(),
                request.getFacultycode(),
                request.getDeaniduser()
        );
    }

    @Override
    public void updateFaculty(UFacultyRequest request) {
        repository.spuFaculty(
                request.getIdfaculty(),
                request.getFacultyname(),
                request.getFacultycode(),
                request.getDeaniduser()
        );
    }

    @Override
    public void deleteFaculty(Integer idfaculty) {
        repository.spdFaculty(idfaculty);
    }

    @Override
    @Transactional(readOnly = true)
    public List<FacultyResponse> listFaculty() {
        List<Object[]> rows = repository.fnListFaculties();

        return rows.stream().map(r -> new FacultyResponse(
                toInt(r[0]),
                toStr(r[1]),
                toStr(r[2]),
                toInt(r[3]) // puede venir null
        )).toList();
    }

    private Integer toInt(Object v) {
        if (v == null) return null;
        if (v instanceof Integer i) return i;
        if (v instanceof Number n) return n.intValue(); // por si viene Long/BigInteger
        throw new IllegalArgumentException("Tipo numérico no soportado: " + v.getClass());
    }

    private String toStr(Object v) {
        return v == null ? null : v.toString();
    }
}
