package com.amalitech.tib.admin.model;

import com.amalitech.tib.shared.util.BaseEntity;
import com.amalitech.tib.auth.model.User;
import jakarta.persistence.*;

@Entity
@Table(name = "general_settings")
public class GeneralSettings  extends BaseEntity {

    private String language;
    private String theme;
    private String timeZone;
    private String dateFormat;

    @OneToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "account_id")
    private User user;
}
