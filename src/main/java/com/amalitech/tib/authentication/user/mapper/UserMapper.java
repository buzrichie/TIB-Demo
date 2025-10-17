package com.amalitech.tib.mapper;

import com.amalitech.tib.authentication.dto.RegisterRequest;
import com.amalitech.tib.user.dto.UserDto;
import com.amalitech.tib.user.model.User;
import org.mapstruct.Mapper;

@Mapper(componentModel = "spring")
public interface UserMapper {
    User fromRegisterRequest(RegisterRequest request);

    UserDto toDto(User savedUser);
    User toEntity(UserDto savedUser);
}
