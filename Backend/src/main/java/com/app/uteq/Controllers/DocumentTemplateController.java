package com.app.uteq.Controllers;

import com.app.uteq.Dtos.CDocumentTemplateRequest;
import com.app.uteq.Dtos.UDocumentTemplateRequest;
import com.app.uteq.Services.IDocumentTemplatesService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.security.access.prepost.PreAuthorize;

@RestController
@RequestMapping("/api/v1/document-templates")
@RequiredArgsConstructor
@PreAuthorize("isAuthenticated()")
public class DocumentTemplateController {
    private final IDocumentTemplatesService service;

    // CREATE -> spi_documenttemplate
    @PostMapping
    public ResponseEntity<?> create(@RequestBody CDocumentTemplateRequest request) {
        service.createDocumenttemplate(request);
        return ResponseEntity.ok("Template creado correctamente");
    }

    // UPDATE -> spu_documenttemplate
    @PutMapping("/{id}")
    public ResponseEntity<?> update(
            @PathVariable Integer id,
            @RequestBody UDocumentTemplateRequest request) {
        request.setIdtemplate(id);
        service.updateDocumenttemplate(request);
        return ResponseEntity.ok("Template actualizado correctamente");
    }

    // DELETE lógico -> spd_documenttemplate
    @DeleteMapping("/{idtemplate}")
    public ResponseEntity<?> delete(@PathVariable Integer idtemplate) {
        service.deleteDocumenttemplate(idtemplate);
        return ResponseEntity.ok("Template eliminado correctamente");
    }

    // LIST -> fn_list_documenttemplates
    @GetMapping
    public ResponseEntity<?> list(@RequestParam(required = false) Boolean onlyActive) {
        return ResponseEntity.ok(service.listDocumenttemplate(onlyActive));
    }
}
