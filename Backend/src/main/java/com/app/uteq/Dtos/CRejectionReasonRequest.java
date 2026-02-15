package com.app.uteq.Dtos;

import lombok.Data;

@Data
public class CRejectionReasonRequest {
    private String reasoncode;
    private String reasondescription;
    private String category;
    private Boolean active;
}
