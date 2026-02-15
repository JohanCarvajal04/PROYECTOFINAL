package com.app.uteq.Dtos;

import lombok.Data;

@Data
public class UDocumentTemplateRequest {
    private Integer idtemplate;
    private String templatename;
    private String templatecode;
    private String templatepath;
    private String documenttype;
    private String version;
    private Boolean requiressignature;
    private Boolean active;
}
