package com.app.uteq.Dtos;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class RejectionReasonResponse {
    private Integer idrejectionreason;
    private String reasoncode;
    private String reasondescription;
    private String category;
    private Boolean active;
}
