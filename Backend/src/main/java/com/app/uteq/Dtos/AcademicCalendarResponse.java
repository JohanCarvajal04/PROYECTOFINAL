package com.app.uteq.Dtos;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDate;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class AcademicCalendarResponse {
    private Integer idacademiccalendar;
    private String calendarname;
    private String academicperiod;
    private LocalDate startdate;
    private LocalDate enddate;
    private Boolean active;
}
