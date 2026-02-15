package com.app.uteq.Dtos;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class NotificationResponse {
    private Integer idNotification;
    private String notificationName;
    private String message;
    private Integer notificationTypeIdNotificationType;
    private Integer applicationId;
    private Integer recipientUserId;
    private LocalDateTime sentAt;
    private String deliveryStatus;
    private String deliveryChannel;
    private LocalDateTime readAt;
    private String errorMessage;
    private Integer retryCount;
}
