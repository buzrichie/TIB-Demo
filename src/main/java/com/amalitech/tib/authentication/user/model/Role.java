package com.amalitech.tib.authentication.user.model;

import com.amalitech.tib.authentication.user.enums.Permission;
import com.amalitech.tib.shared.util.BaseEntity;
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
