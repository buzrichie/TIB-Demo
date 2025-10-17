package com.amalitech.tib.user.mapper;

import com.amalitech.tib.user.dto.CreateRoleDto;
import com.amalitech.tib.user.model.Role;
import org.mapstruct.Mapper;

@Mapper(componentModel = "spring")
public interface RoleMapper {
    Role fromCreateRoleDto(CreateRoleDto createRoleDto);
}
