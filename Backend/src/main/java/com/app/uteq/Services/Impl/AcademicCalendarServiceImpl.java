package com.app.uteq.Services.Impl;

import java.sql.Date;
import java.time.LocalDate;
import java.util.List;

import org.springframework.stereotype.Service;

import com.app.uteq.Dtos.AcademicCalendarResponse;
import com.app.uteq.Dtos.UAcademicCalendarRequest;
import com.app.uteq.Exceptions.BadRequestException;
import com.app.uteq.Repository.IAcademicCalendarRepository;
import com.app.uteq.Services.IAcademicCalendarService;

import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class AcademicCalendarServiceImpl implements IAcademicCalendarService {

    private final IAcademicCalendarRepository repository;

    @Override
    public void createcalendar(String calendarname,
                               String academicperiod,
                               LocalDate startdate,
                               LocalDate enddate,
                               Boolean active) {

        // Validaciones de capa backend (rápidas)
        if(calendarname == null || calendarname.isBlank())
            throw new BadRequestException("Debe ingresar el nombre del calendario");

        if(academicperiod == null || academicperiod.isBlank())
            throw new BadRequestException("Debe ingresar el periodo académico");

        // Llamada al Stored Procedure
        repository.spiCreateCalendar(
                calendarname,
                academicperiod,
                startdate,
                enddate,
                active
        );
    }

    @Override
    public void modifycalendar(UAcademicCalendarRequest request) {
        repository.spuUpdateCalendar(
                request.getIdacademiccalendar(),
                request.getCalendarname(),
                request.getAcademicperiod(),
                request.getStartdate(),
                request.getEnddate(),
                request.getActive()
        );
    }

    @Override
    public List<AcademicCalendarResponse> listarCalendarios(Boolean onlyActive) {

        List<Object[]> rows = repository.fnListAcademicCalendar(onlyActive);

        return rows.stream().map(r -> new AcademicCalendarResponse(
                (Integer) r[0],
                (String) r[1],
                (String) r[2],
                toLocalDate(r[3]),
                toLocalDate(r[4]),
                (Boolean) r[5]
        )).toList();
    }
    private LocalDate toLocalDate(Object value) {
        if (value == null) return null;
        if (value instanceof LocalDate ld) return ld;
        if (value instanceof Date d) return d.toLocalDate();
        throw new IllegalArgumentException("Tipo de fecha no soportado: " + value.getClass());
    }


    @Override
    public void deletecalendar(Integer idacademiccalendar) {
        repository.spdAcademicCalendar(idacademiccalendar);
    }
}
