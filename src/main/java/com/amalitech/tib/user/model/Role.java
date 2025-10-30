package com.amalitech.tib.user.model;

import com.amalitech.tib.user.enums.Permission;
import com.amalitech.tib.util.BaseEntity;
import jakarta.persistence.*;
import java.util.Set;
import lombok.Data;

@Entity
@Data
@Table(name = "role")
public class Role extends BaseEntity {

  private String name;
  private Set<Permission> permissions;
}
