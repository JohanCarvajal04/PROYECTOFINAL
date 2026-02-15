package com.app.uteq.Dtos;

import lombok.Data;

@Data
public class UNotificationRequest {
    private Integer idNotification;
    private String notificationName;
    private String message;
    private Integer notificationTypeIdNotificationType;
    private Integer applicationId;
    private Integer recipientUserId;
    private String deliveryStatus;
    private String deliveryChannel;
    private Integer retryCount;
}
