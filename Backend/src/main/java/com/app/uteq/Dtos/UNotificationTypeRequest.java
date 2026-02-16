package com.app.uteq.Dtos;

import lombok.Data;

@Data
public class UNotificationTypeRequest {
    private Integer idNotificationType;
    private String nameTypeNotification;
    private String templateCode;
    private String priorityLevel;
}
