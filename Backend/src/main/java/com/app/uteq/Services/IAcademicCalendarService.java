package com.app.uteq.Services;

import com.app.uteq.Dtos.AcademicCalendarResponse;
import com.app.uteq.Dtos.UAcademicCalendarRequest;

import java.time.LocalDate;
import java.util.List;

public interface IAcademicCalendarService {

    List<AcademicCalendarResponse> listarCalendarios(Boolean onlyActive);
    void createcalendar(String calendarname,
                        String academicperiod,
                        LocalDate startdate,
                        LocalDate enddate,
                        Boolean active);

    void modifycalendar(UAcademicCalendarRequest request);
    void deletecalendar(Integer idacademiccalendar);
}
