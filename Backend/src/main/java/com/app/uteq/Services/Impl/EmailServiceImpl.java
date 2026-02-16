package com.app.uteq.Services.Impl;

import com.app.uteq.Services.IEmailService;
import jakarta.mail.MessagingException;
import jakarta.mail.internet.MimeMessage;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.mail.SimpleMailMessage;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.mail.javamail.MimeMessageHelper;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class EmailServiceImpl implements IEmailService {
    private final JavaMailSender mailSender;

    // Usa el mismo correo autenticado para el From
    @Value("${spring.mail.username}")
    private String from;

    @Override
    public void sendPlainText(String to, String subject, String body) {
        SimpleMailMessage msg = new SimpleMailMessage();
        msg.setFrom(from);
        msg.setTo(to);
        msg.setSubject(subject);
        msg.setText(body);

        mailSender.send(msg);
    }

    @Override
    public void sendHtml(String to, String subject, String html) {
        try {
            MimeMessage mimeMessage = mailSender.createMimeMessage();

            // true = multipart (por si luego agregas adjuntos)
            MimeMessageHelper helper = new MimeMessageHelper(mimeMessage, true, "UTF-8");

            helper.setFrom(from);
            helper.setTo(to);
            helper.setSubject(subject);
            helper.setText(html, true); // true = HTML

            mailSender.send(mimeMessage);
        } catch (MessagingException e) {
            throw new RuntimeException("No se pudo enviar el correo HTML", e);
        }
    }

    @Override
    public void sendCredentials(String to, String loginEmail, String tempPassword) {
        SimpleMailMessage msg = new SimpleMailMessage();
        msg.setTo(to);
        msg.setSubject("Credenciales de acceso");

        msg.setText("""
                Hola,
                
                Se han generado tus credenciales:
                Usuario (email): %s
                Contraseña temporal: %s
                
                Recomendación: cambia la contraseña al ingresar.
                """.formatted(loginEmail, tempPassword));

        mailSender.send(msg);
    }
}
