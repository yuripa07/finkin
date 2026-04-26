package com.finkin.infrastructure.adapter.output.persistence.account;
import com.finkin.domain.model.account.enums.*;

import com.finkin.domain.model.account.*;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;

@Mapper(componentModel = "spring")
public interface IAccountMapper {

    @Mapping(target = "number", expression = "java(new com.finkin.domain.model.account.AccountNumberModel(entity.getAccountNumber(), entity.getAccountNumberDv()))")
    @Mapping(target = "type", expression = "java(com.finkin.domain.model.account.enums.AccountTypeEnum.valueOf(entity.getType()))")
    @Mapping(target = "status", expression = "java(com.finkin.domain.model.account.enums.AccountStatusEnum.valueOf(entity.getStatus()))")
    @Mapping(target = "balance", expression = "java(com.finkin.domain.model.account.MoneyModel.of(entity.getBalance()))")
    AccountModel toDomain(AccountJpaEntity entity);

    @Mapping(target = "accountNumber", expression = "java(domain.getNumber().getNumber())")
    @Mapping(target = "accountNumberDv", expression = "java(domain.getNumber().getCheckDigit())")
    @Mapping(target = "type", expression = "java(domain.getType().name())")
    @Mapping(target = "status", expression = "java(domain.getStatus().name())")
    @Mapping(target = "balance", expression = "java(domain.getBalance().getAmount())")
    @Mapping(target = "createdBy", ignore = true)
    @Mapping(target = "updatedBy", ignore = true)
    AccountJpaEntity toEntity(AccountModel domain);
}
