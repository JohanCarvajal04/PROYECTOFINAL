package com.app.uteq.Entity;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Entity
@Data
@Table(name = "notification")
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class Notification {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "idnotification")
    private Integer id;

    @Column(name = "notificationname", nullable = false, length = 255)
    private String notificationName;

    @Column(name = "message", columnDefinition = "TEXT")
    private String message;

    @ManyToOne
    @JoinColumn(name = "notificationtypeidnotificationtype", nullable = false)
    private NotificationType notificationType;

    @ManyToOne
    @JoinColumn(name = "applicationid")
    private Applications application;

    @ManyToOne
    @JoinColumn(name = "recipientuserid", nullable = false)
    private Users recipientUser;

    @Column(name = "sentat")
    private LocalDateTime sentAt;

    @Column(name = "deliverystatus", nullable = false, length = 50)
    @Builder.Default
    private String deliveryStatus = "pending";

    @Column(name = "deliverychannel", length = 50)
    private String deliveryChannel;

    @Column(name = "readat")
    private LocalDateTime readAt;

    @Column(name = "errormessage", columnDefinition = "TEXT")
    private String errorMessage;

    @Column(name = "retrycount", nullable = false)
    @Builder.Default
    private Integer retryCount = 0;
}
