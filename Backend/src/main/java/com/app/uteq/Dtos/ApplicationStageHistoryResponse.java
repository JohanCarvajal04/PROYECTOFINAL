package com.app.uteq.Dtos;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class ApplicationStageHistoryResponse {
    private Integer idHistory;
    private Integer applicationIdApplication;
    private Integer stageTrackingId;
    private LocalDateTime enteredAt;
    private LocalDateTime exitedAt;
    private Integer processedByUserId;
    private String comments;
}
