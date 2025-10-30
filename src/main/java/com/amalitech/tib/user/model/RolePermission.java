package com.amalitech.tib.user.model;

import com.amalitech.tib.util.BaseEntity;
import jakarta.persistence.*;
import lombok.Data;
import lombok.EqualsAndHashCode;

@Entity
@Data
@EqualsAndHashCode(
    callSuper = true,
    exclude = {"role", "permission"})
@Table(name = "role_permissions")
public class RolePermission extends BaseEntity {

  @ManyToOne(fetch = FetchType.LAZY)
  @JoinColumn(name = "role_id", nullable = false)
  private Role role;

  @ManyToOne(fetch = FetchType.LAZY)
  @JoinColumn(name = "permission_id", nullable = false)
  private Permission permission;
}
