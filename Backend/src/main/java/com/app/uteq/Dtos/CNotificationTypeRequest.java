package com.app.uteq.Dtos;

import lombok.Data;

@Data
public class CNotificationTypeRequest {
    private String nameTypeNotification;
    private String templateCode;
    private String priorityLevel;
}
