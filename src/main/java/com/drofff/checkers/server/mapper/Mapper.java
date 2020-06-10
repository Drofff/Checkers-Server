package com.drofff.checkers.server.mapper;

import org.modelmapper.ModelMapper;

import java.lang.reflect.ParameterizedType;
import java.lang.reflect.Type;

public abstract class Mapper<E, D> {

    private final ModelMapper modelMapper;

    private Type entityType;
    private Type dtoType;

    public Mapper(ModelMapper modelMapper) {
        this.modelMapper = modelMapper;
        initGenericParamsTypes();
    }

    private void initGenericParamsTypes() {
        ParameterizedType parameterizedType = (ParameterizedType) getClass().getGenericSuperclass();
        this.entityType = parameterizedType.getActualTypeArguments()[0];
        this.dtoType = parameterizedType.getActualTypeArguments()[1];
    }

    public D toDto(E entity) {
        return modelMapper.map(entity, dtoType);
    }

    public E toEntity(D dto) {
        return modelMapper.map(dto, entityType);
    }

}