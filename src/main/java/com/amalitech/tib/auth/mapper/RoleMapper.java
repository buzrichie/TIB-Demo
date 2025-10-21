package com.amalitech.tib.auth.mapper;

import com.amalitech.tib.auth.dto.CreateRoleDto;
import com.amalitech.tib.auth.model.Role;
import org.mapstruct.Mapper;

@Mapper(componentModel = "spring")
public interface RoleMapper {
    Role fromCreateRoleDto(CreateRoleDto createRoleDto);
}
