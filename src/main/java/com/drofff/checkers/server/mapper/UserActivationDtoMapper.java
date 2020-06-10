package com.drofff.checkers.server.mapper;

import com.drofff.checkers.server.dto.UserActivationDto;
import com.drofff.checkers.server.type.UserActivation;
import org.modelmapper.ModelMapper;
import org.springframework.stereotype.Component;

@Component
public class UserActivationDtoMapper extends Mapper<UserActivation, UserActivationDto> {

    public UserActivationDtoMapper(ModelMapper modelMapper) {
        super(modelMapper);
    }

}
