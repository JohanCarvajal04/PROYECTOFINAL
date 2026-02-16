package com.app.uteq.Dtos;

import java.time.LocalDate;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.Data;

@Data
public class IAcademicCalendarRequest {

    @NotBlank(message = "El nombre del calendario es obligatorio")
    @Size(min = 3, max = 100, message = "El nombre debe tener entre 3 y 100 caracteres")
    private String calendarname;

    @NotBlank(message = "El período académico es obligatorio")
    private String academicperiod;

    @NotNull(message = "La fecha de inicio es obligatoria")
    private LocalDate startdate;

    @NotNull(message = "La fecha de fin es obligatoria")
    private LocalDate enddate;

    private Boolean active;
}
