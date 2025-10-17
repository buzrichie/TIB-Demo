package com.amalitech.tib.authentication.user.mapper;

import com.amalitech.tib.authentication.user.dto.CreateRoleDto;
import com.amalitech.tib.authentication.user.model.Role;
import org.mapstruct.Mapper;

@Mapper(componentModel = "spring")
public interface RoleMapper {
    Role fromCreateRoleDto(CreateRoleDto createRoleDto);
}
