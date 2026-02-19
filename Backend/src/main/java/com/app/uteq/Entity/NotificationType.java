package com.app.uteq.Entity;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Entity
@Data
@Table(name = "notificationtype")
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class NotificationType {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "idnotificationtype")
    private Integer idNotificationType;

    @Column(name = "nametypenotification", nullable = false, length = 255)
    private String nameTypeNotification;

    @Column(name = "templatecode", length = 50)
    private String templateCode;

    @Column(name = "prioritylevel", length = 20)
    private String priorityLevel;
}
