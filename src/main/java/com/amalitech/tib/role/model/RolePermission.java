//package com.amalitech.tib.role.model;
//
//import com.amalitech.tib.shared.BaseEntity;
//import com.amalitech.tib.permission.model.Permission;
//import jakarta.persistence.*;
//import lombok.Data;
//
//@Entity
//@Data
//@Table(name = "role_permissions")
//public class RolePermission extends BaseEntity {
//
//    @ManyToOne(fetch = FetchType.LAZY)
//    @JoinColumn(name = "role_id", insertable = false, updatable = false)
//    private Role role;
//
//    @ManyToOne(fetch = FetchType.LAZY)
//    @JoinColumn(name = "permission_id", insertable = false, updatable = false)
//    private Permission permission;
//}
