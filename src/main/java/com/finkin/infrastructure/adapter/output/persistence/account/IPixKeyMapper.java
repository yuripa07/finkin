package com.finkin.infrastructure.adapter.output.persistence.account;

import com.finkin.domain.model.pix.PixKeyModel;
import com.finkin.domain.model.pix.enums.PixKeyTypeEnum;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;

@Mapper(componentModel = "spring")
public interface IPixKeyMapper {

    @Mapping(target = "keyType", expression = "java(com.finkin.domain.model.pix.enums.PixKeyTypeEnum.valueOf(entity.getKeyType()))")
    PixKeyModel toDomain(PixKeyJpaEntity entity);

    @Mapping(target = "keyType", expression = "java(domain.getKeyType().name())")
    PixKeyJpaEntity toEntity(PixKeyModel domain);
}
