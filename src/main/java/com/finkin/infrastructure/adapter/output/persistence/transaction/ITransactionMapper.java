package com.finkin.infrastructure.adapter.output.persistence.transaction;
import com.finkin.domain.model.transaction.enums.*;

import com.finkin.domain.model.account.MoneyModel;
import com.finkin.domain.model.transaction.*;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;

@Mapper(componentModel = "spring")
public interface ITransactionMapper {

    @Mapping(target = "type", expression = "java(com.finkin.domain.model.transaction.enums.TransactionTypeEnum.valueOf(entity.getType()))")
    @Mapping(target = "status", expression = "java(com.finkin.domain.model.transaction.enums.TransactionStatusEnum.valueOf(entity.getStatus()))")
    @Mapping(target = "amount", expression = "java(com.finkin.domain.model.account.MoneyModel.of(entity.getAmount()))")
    @Mapping(target = "endToEndId", expression = "java(new com.finkin.domain.model.transaction.EndToEndIdModel(entity.getEndToEndId()))")
    TransactionModel toDomain(TransactionJpaEntity entity);

    @Mapping(target = "type", expression = "java(domain.getType().name())")
    @Mapping(target = "status", expression = "java(domain.getStatus().name())")
    @Mapping(target = "amount", expression = "java(domain.getAmount().getAmount())")
    @Mapping(target = "endToEndId", expression = "java(domain.getEndToEndId().getValue())")
    TransactionJpaEntity toEntity(TransactionModel domain);
}
