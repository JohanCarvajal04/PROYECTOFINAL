package com.app.uteq.Controllers;

import com.app.uteq.Dtos.IAcademicCalendarRequest;
import com.app.uteq.Dtos.UAcademicCalendarRequest;
import com.app.uteq.Services.IAcademicCalendarService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/academic-calendar")
@RequiredArgsConstructor
public class AcademicCalendarController {

    private final IAcademicCalendarService service;

    @PostMapping("/sp-create")
    public ResponseEntity<?> crearCalendario(@RequestBody IAcademicCalendarRequest request){

        service.createcalendar(
                request.getCalendarname(),
                request.getAcademicperiod(),
                request.getStartdate(),
                request.getEnddate(),
                request.getActive()
        );

        return ResponseEntity.ok("Calendario creado correctamente");
    }

    @PutMapping("/sp-update")
    public ResponseEntity<?> actualizar(@RequestBody UAcademicCalendarRequest request) {
        service.modifycalendar(request);
        return ResponseEntity.ok("Calendario actualizado correctamente");
    }


    @DeleteMapping("/sp-delete/{id}")
    public ResponseEntity<?> eliminar(@PathVariable Integer id) {
        service.deletecalendar(id);
        return ResponseEntity.ok("Calendario eliminado correctamente");
    }

    @GetMapping("/list")
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
