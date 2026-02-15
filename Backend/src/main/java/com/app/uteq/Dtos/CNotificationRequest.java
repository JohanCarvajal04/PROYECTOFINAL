package com.app.uteq.Dtos;

import lombok.Data;

@Data
public class CNotificationRequest {
    private String notificationName;
    private String message;
    private Integer notificationTypeIdNotificationType;
    private Integer applicationId;
    private Integer recipientUserId;
    private String deliveryChannel;
}
