package com.amalitech.tib.user.model;

import com.amalitech.tib.permission.model.Permission;
import com.amalitech.tib.util.BaseEntity;
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
