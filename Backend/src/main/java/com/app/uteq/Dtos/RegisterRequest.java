package com.app.uteq.Dtos;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class RegisterRequest {
    private String names;
    private String surnames;
    private String cardId;
    private String institutionalEmail;
    private String personalMail;
    private String phoneNumber;
    private String password;
}
