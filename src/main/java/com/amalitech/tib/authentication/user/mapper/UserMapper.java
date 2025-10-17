package com.amalitech.tib.authentication.user.mapper;

import com.amalitech.tib.authentication.auth.dto.RegisterRequest;
import com.amalitech.tib.authentication.user.dto.UserDto;
import com.amalitech.tib.authentication.user.model.User;
import org.mapstruct.Mapper;

@Mapper(componentModel = "spring")
public interface UserMapper {
    User fromRegisterRequest(RegisterRequest request);

    UserDto toDto(User savedUser);
    User toEntity(UserDto savedUser);
}
