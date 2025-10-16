package com.amalitech.tib.role.mapper;

import com.amalitech.tib.role.dto.CreateRoleDto;
import com.amalitech.tib.role.model.Role;
import org.mapstruct.Mapper;

@Mapper(componentModel = "spring")
public interface RoleMapper {
    Role fromCreateRoleDto(CreateRoleDto createRoleDto);
}
