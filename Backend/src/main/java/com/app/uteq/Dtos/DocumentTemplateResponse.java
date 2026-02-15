package com.app.uteq.Dtos;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class DocumentTemplateResponse {
    private Integer idtemplate;
    private String templatename;
    private String templatecode;
    private String templatepath;
    private String documenttype;
    private String version;
    private Boolean requiressignature;
    private Boolean active;
    private LocalDateTime createdat;
    private LocalDateTime updatedat;
}
