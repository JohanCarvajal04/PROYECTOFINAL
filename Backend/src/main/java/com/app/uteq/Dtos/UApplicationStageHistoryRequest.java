package com.app.uteq.Dtos;

import lombok.Data;

@Data
public class UApplicationStageHistoryRequest {
    private Integer idHistory;
    private Integer applicationIdApplication;
    private Integer stageTrackingId;
    private Integer processedByUserId;
    private String comments;
}
