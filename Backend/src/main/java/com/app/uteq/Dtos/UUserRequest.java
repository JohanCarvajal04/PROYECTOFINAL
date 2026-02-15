package com.app.uteq.Dtos;

import lombok.Data;

@Data
public class UUserRequest {
    private Integer idUser;
    private String names;
    private String surnames;
    private String cardId;
    private String institutionalEmail;
    private String personalMail;
    private String phoneNumber;
    private Boolean statement;
    private Integer configurationsIdConfiguration;
    private Boolean active;
}
