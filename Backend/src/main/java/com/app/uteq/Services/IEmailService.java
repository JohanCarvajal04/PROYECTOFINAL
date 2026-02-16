package com.app.uteq.Services;

public interface IEmailService {
    void sendPlainText(String to, String subject, String body);
    void sendHtml(String to, String subject, String html);
    void sendCredentials(String to, String loginEmail, String tempPassword);
}
