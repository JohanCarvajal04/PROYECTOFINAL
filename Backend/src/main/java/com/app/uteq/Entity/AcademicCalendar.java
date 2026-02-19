package com.app.uteq.Entity;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.Date;

@Entity
@Table(name = "academiccalendar")
@AllArgsConstructor
@NoArgsConstructor
@Data
@Builder
public class AcademicCalendar {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "idacademiccalendar")
    private Integer idAcademicCalendar;

    @Column(name = "calendarname", nullable = false, length = 255)
    private String calendarName;

    @Column(name = "academicperiod", nullable = false, length = 100)
    private String academicPeriod;

    @Column(name = "startdate", nullable = false)
    private LocalDate startDate;

    @Column(name = "enddate", nullable = false)
    private LocalDate endDate;

    @Column(name = "active", nullable = false)
    private Boolean active = true;

    @Column(name = "createdat")
    private LocalDateTime createdAt;

    @Column(name = "updatedat")
    private LocalDateTime updatedAt;

}
