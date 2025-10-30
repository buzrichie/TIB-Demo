package com.amalitech.tib.user.model;

import com.amalitech.tib.util.BaseEntity;
import jakarta.persistence.*;
import java.util.Set;
import lombok.Data;

@Entity
@Data
@Table(name = "permission")
public class Permission extends BaseEntity {

  private String name;
  private String description;

  @OneToMany(mappedBy = "permission", cascade = CascadeType.ALL, orphanRemoval = true)
  private Set<RolePermission> roles;
}
