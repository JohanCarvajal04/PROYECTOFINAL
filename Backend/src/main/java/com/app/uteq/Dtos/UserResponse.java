package com.app.uteq.Dtos;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class UserResponse {
    private Integer idUser;
    private String names;
    private String surnames;
    private String cardId;
    private String institutionalEmail;
    private String personalMail;
    private String phoneNumber;
    private Boolean statement;
    private Integer configurationsIdConfiguration;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
    private Boolean active;
}
