package com.app.uteq.Dtos;

import lombok.Data;

import java.time.LocalDate;
@Data
public class UAcademicCalendarRequest {
    private Integer idacademiccalendar;
    private String calendarname;
    private String academicperiod;
    private LocalDate startdate;
    private LocalDate enddate;
    private Boolean active;
}
