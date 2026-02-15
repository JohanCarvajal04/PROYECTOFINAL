package com.app.uteq.Dtos;

import lombok.Data;

@Data
public class CUserRequest {
    private String names;
    private String surnames;
    private String cardId;
    private String institutionalEmail;
    private String personalMail;
    private String phoneNumber;
    private Integer configurationsIdConfiguration;
}
