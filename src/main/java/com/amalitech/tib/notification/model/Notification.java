package com.amalitech.tib.notification.model;

import com.amalitech.tib.notification.enums.NotificationStatus;
import com.amalitech.tib.notification.enums.NotificationType;
import com.amalitech.tib.shared.util.BaseEntity;
import com.amalitech.tib.authentication.user.model.User;
import jakarta.persistence.*;
import lombok.Data;

@Entity
@Data
@Table(name = "notification")
public class Notification extends BaseEntity {

    private String title;
    @Lob
    private String message;
    @Enumerated(EnumType.STRING)
    private NotificationType type;
    @Enumerated(EnumType.STRING)
    private NotificationStatus status;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id")
    private User user;
}
