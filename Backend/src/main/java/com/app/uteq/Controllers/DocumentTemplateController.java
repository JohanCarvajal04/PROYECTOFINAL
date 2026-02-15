package com.app.uteq.Controllers;

import com.app.uteq.Dtos.CDocumentTemplateRequest;
import com.app.uteq.Dtos.UDocumentTemplateRequest;
import com.app.uteq.Services.IDocumentTemplatesService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/document-templates")
@RequiredArgsConstructor
public class DocumentTemplateController {
    private final IDocumentTemplatesService service;

    // CREATE -> spi_documenttemplate
    @PostMapping("/sp-create")
    public ResponseEntity<?> create(@RequestBody CDocumentTemplateRequest request) {
        service.createDocumenttemplate(request);
        return ResponseEntity.ok("Template creado correctamente");
    }

    // UPDATE -> spu_documenttemplate
    @PutMapping("/sp-update")
    public ResponseEntity<?> update(@RequestBody UDocumentTemplateRequest request) {
        service.updateDocumenttemplate(request);
        return ResponseEntity.ok("Template actualizado correctamente");
    }

    // DELETE lógico -> spd_documenttemplate
    @DeleteMapping("/sp-delete/{idtemplate}")
    public ResponseEntity<?> delete(@PathVariable Integer idtemplate) {
        service.deleteDocumenttemplate(idtemplate);
        return ResponseEntity.ok("Template eliminado correctamente");
    }

    // LIST -> fn_list_documenttemplates
    @GetMapping("/list")
    public ResponseEntity<?> list(@RequestParam(required = false) Boolean onlyActive) {
        return ResponseEntity.ok(service.listDocumenttemplate(onlyActive));
    }
}
