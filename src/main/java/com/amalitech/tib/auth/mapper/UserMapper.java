package com.amalitech.tib.auth.mapper;

import com.amalitech.tib.auth.dto.RegisterRequest;
import com.amalitech.tib.auth.dto.UserDto;
import com.amalitech.tib.auth.model.User;
import org.mapstruct.Mapper;

@Mapper(componentModel = "spring")
public interface UserMapper {
    User fromRegisterRequest(RegisterRequest request);

    UserDto toDto(User savedUser);
    User toEntity(UserDto savedUser);
}
