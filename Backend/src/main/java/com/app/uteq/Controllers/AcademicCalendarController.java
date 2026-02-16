package com.app.uteq.Controllers;

import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import com.app.uteq.Dtos.IAcademicCalendarRequest;
import com.app.uteq.Dtos.UAcademicCalendarRequest;
import com.app.uteq.Services.IAcademicCalendarService;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;

@RestController
@RequestMapping("/api/v1/academic-calendar")
@RequiredArgsConstructor
@PreAuthorize("isAuthenticated()")
public class AcademicCalendarController {

    private final IAcademicCalendarService service;

    @PostMapping
    @PreAuthorize("hasAuthority('CAL_CREAR')")
    public ResponseEntity<?> createCalendar(@Valid @RequestBody IAcademicCalendarRequest request){

        service.createcalendar(
                request.getCalendarname(),
                request.getAcademicperiod(),
                request.getStartdate(),
                request.getEnddate(),
                request.getActive()
        );

        return ResponseEntity.ok("Calendario creado correctamente");
    }

    @PutMapping("/{idacademiccalendar}")
    @PreAuthorize("hasAuthority('CAL_MODIFICAR')")
    public ResponseEntity<?> actualizar(
            @PathVariable Integer idacademiccalendar,
            @Valid @RequestBody UAcademicCalendarRequest request) {
        request.setIdacademiccalendar(idacademiccalendar);
        service.modifycalendar(request);
        return ResponseEntity.ok("Calendario actualizado correctamente");
    }


    @DeleteMapping("/{id}")
    @PreAuthorize("hasAuthority('CAL_ELIMINAR')")
    public ResponseEntity<?> eliminar(@PathVariable Integer id) {
        service.deletecalendar(id);
        return ResponseEntity.ok("Calendario eliminado correctamente");
    }

    @GetMapping()
    @PreAuthorize("hasAuthority('CAL_LISTAR')")
    public ResponseEntity<?> listar(@RequestParam(required = false) Boolean onlyActive) {
        return ResponseEntity.ok(service.listarCalendarios(onlyActive));
    }
}
