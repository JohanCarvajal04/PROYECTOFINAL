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

import com.app.uteq.Dtos.CDocumentTemplateRequest;
import com.app.uteq.Dtos.UDocumentTemplateRequest;
import com.app.uteq.Services.IDocumentTemplatesService;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;

@RestController
@RequestMapping("/api/v1/document-templates")
@RequiredArgsConstructor
@PreAuthorize("isAuthenticated()")
public class DocumentTemplateController {
    private final IDocumentTemplatesService service;

    // CREATE -> spi_documenttemplate
    @PostMapping
    public ResponseEntity<?> create(@Valid @RequestBody CDocumentTemplateRequest request) {
        service.createDocumenttemplate(request);
        return ResponseEntity.ok("Template creado correctamente");
    }

    // UPDATE -> spu_documenttemplate
    @PutMapping("/{id}")
    public ResponseEntity<?> update(
            @PathVariable Integer id,
            @Valid @RequestBody UDocumentTemplateRequest request) {
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
