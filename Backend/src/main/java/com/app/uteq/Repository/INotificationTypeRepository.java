package com.app.uteq.Repository;

import com.app.uteq.Entity.NotificationType;
import org.springframework.data.jpa.repository.JpaRepository;

public interface INotificationTypeRepository extends JpaRepository<NotificationType, Integer> {
}
