package com.amalitech.tib.auth.model;

import com.amalitech.tib.auth.enums.Permission;
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
