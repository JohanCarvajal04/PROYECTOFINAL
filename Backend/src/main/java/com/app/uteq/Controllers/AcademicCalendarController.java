package com.app.uteq.Controllers;

import com.app.uteq.Dtos.IAcademicCalendarRequest;
import com.app.uteq.Dtos.UAcademicCalendarRequest;
import com.app.uteq.Services.IAcademicCalendarService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.security.access.prepost.PreAuthorize;

@RestController
@RequestMapping("/api/v1/academic-calendar")
@RequiredArgsConstructor
@PreAuthorize("isAuthenticated()")
public class AcademicCalendarController {

    private final IAcademicCalendarService service;

    @PostMapping
    public ResponseEntity<?> createCalendar(@RequestBody IAcademicCalendarRequest request){

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
    public ResponseEntity<?> actualizar(
            @PathVariable Integer idacademiccalendar,
            @RequestBody UAcademicCalendarRequest request) {
        request.setIdacademiccalendar(idacademiccalendar);
        service.modifycalendar(request);
        return ResponseEntity.ok("Calendario actualizado correctamente");
    }


    @DeleteMapping("/{id}")
    public ResponseEntity<?> eliminar(@PathVariable Integer id) {
        service.deletecalendar(id);
        return ResponseEntity.ok("Calendario eliminado correctamente");
    }

    @GetMapping()
    public ResponseEntity<?> listar(@RequestParam(required = false) Boolean onlyActive) {
        return ResponseEntity.ok(service.listarCalendarios(onlyActive));
    }
//    public ResponseEntity<?> createCalendar(
//            @RequestParam String name,
//            @RequestParam String period,
//            @RequestParam LocalDate start,
//            @RequestParam LocalDate end,
//            @RequestParam(required = false) Boolean active
//    ){
//        service.createcalendar(name, period, start, end, active);
//        return ResponseEntity.ok("Calendario creado correctamente");
//    }
}
