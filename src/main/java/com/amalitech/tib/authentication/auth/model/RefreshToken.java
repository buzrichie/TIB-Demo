package com.amalitech.tib.authentication.model;

import com.amalitech.tib.shared.util.BaseEntity;
import com.amalitech.tib.authentication.user.model.User;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.OneToOne;
import lombok.Data;

@Entity
@Data
public class RefreshToken extends BaseEntity {

    @OneToOne
    private User user;

    @Column(nullable = false, unique = true)
    private String token;

    @Column(name = "is_revoked", nullable = false)
    private Boolean isRevoked = false;
}
