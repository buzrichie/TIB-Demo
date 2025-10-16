package com.amalitech.tib.role.model;

import com.amalitech.tib.permission.model.Permission;
import com.amalitech.tib.shared.BaseEntity;
import com.amalitech.tib.user.model.User;
import jakarta.persistence.*;
import lombok.Data;

import java.util.Set;

@Entity
@Data
@Table(name = "role")
public class Role extends BaseEntity {

    private String name;
    private Set<Permission> permissions;
}
