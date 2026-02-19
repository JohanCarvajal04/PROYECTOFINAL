package com.app.uteq.Dtos;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class UserResponse {
    private Integer idUser;
    private String names;
    private String surnames;
    private String institutionalEmail;
    private Boolean active;
}
