package com.amalitech.tib.user.mapper;

import com.amalitech.tib.user.dto.RegisterRequestDto;
import com.amalitech.tib.user.dto.UserDto;
import com.amalitech.tib.user.model.User;
import org.mapstruct.Mapper;

@Mapper(componentModel = "spring")
public interface UserMapper {
  User fromRegisterRequest(RegisterRequestDto request);

  UserDto toDto(User savedUser);

  User toEntity(UserDto savedUser);
}
