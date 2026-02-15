package com.app.uteq.Entity;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Entity
@Data
@Table
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class Users {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "iduser")
    private Integer idUser;

    @Column(name = "names", nullable = false, length = 255)
    private String names;

    @Column(name = "surnames", nullable = false, length = 255)
    private String surnames;

    @Column(name = "cardid", nullable = false, length = 10)
    private String cardId;

    @Column(name = "institutionalemail", nullable = false, length = 255)
    private String institutionalEmail;

    @Column(name = "personalmail", length = 255, unique = true)
    private String personalMail;

    @Column(name = "phonenumber", length = 15)
    private String phoneNumber;

    @Column(name = "statement", nullable = false)
    private Boolean statement = true;



    @Column(name = "createdat", nullable = false)
    private LocalDateTime createdAt;

    @Column(name = "updatedat")
    private LocalDateTime updatedAt;

    @Column(name = "active", nullable = false)
    private Boolean active = true;
}
