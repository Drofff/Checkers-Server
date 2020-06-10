package com.drofff.checkers.server.mapper;

import com.drofff.checkers.server.document.User;
import com.drofff.checkers.server.dto.UserDto;
import org.modelmapper.ModelMapper;
import org.springframework.stereotype.Component;

@Component
public class UserDtoMapper extends Mapper<User, UserDto> {

    public UserDtoMapper(ModelMapper modelMapper) {
        super(modelMapper);
    }

}
