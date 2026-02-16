package com.app.uteq.Controllers;

import com.app.uteq.Dtos.EmailRequest;
import com.app.uteq.Dtos.EmailResponse;
import com.app.uteq.Services.IEmailService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.time.LocalDateTime;

import org.springframework.security.access.prepost.PreAuthorize;

@RestController
@RequestMapping("/api/v1/mail")
@RequiredArgsConstructor
@PreAuthorize("isAuthenticated()")
public class EmailController {
    private final IEmailService emailService;

    @PostMapping("/plain")
    @PreAuthorize("hasAuthority('EMAIL_ENVIAR')")
    public ResponseEntity<EmailResponse> sendPlain(@Valid @RequestBody EmailRequest req) {
        emailService.sendPlainText(req.to(), req.subject(), req.body());
        return ResponseEntity.ok(new EmailResponse(true, "Correo texto enviado", LocalDateTime.now()));
    }

    @PostMapping("/html")
    @PreAuthorize("hasAuthority('EMAIL_ENVIAR')")
    public ResponseEntity<EmailResponse> sendHtml(@Valid @RequestBody EmailRequest req) {
        emailService.sendHtml(req.to(), req.subject(), req.body());
        return ResponseEntity.ok(new EmailResponse(true, "Correo HTML enviado", LocalDateTime.now()));
    }
}
