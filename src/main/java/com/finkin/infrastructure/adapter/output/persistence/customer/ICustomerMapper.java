package com.finkin.infrastructure.adapter.output.persistence.customer;
import com.finkin.domain.model.customer.enums.*;

import com.finkin.domain.model.customer.*;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;

/**
 * MapStruct mapper: CustomerJpaEntity ⇄ CustomerModel (domínio).
 * Código gerado em compile-time — zero overhead em runtime.
 *
 * componentModel = "spring": gera @Component, injetável via @Autowired/@RequiredArgsConstructor.
 */
@Mapper(componentModel = "spring")
public interface ICustomerMapper {

    @Mapping(target = "cpf", expression = "java(new com.finkin.domain.model.customer.CpfModel(entity.getCpf()))")
    @Mapping(target = "email", expression = "java(new com.finkin.domain.model.customer.EmailModel(entity.getEmail()))")
    @Mapping(target = "phone", expression = "java(new com.finkin.domain.model.customer.PhoneModel(entity.getPhone()))")
    @Mapping(target = "kycStatus", expression = "java(com.finkin.domain.model.customer.enums.KycStatusEnum.valueOf(entity.getKycStatus()))")
    CustomerModel toDomain(CustomerJpaEntity entity);

    @Mapping(target = "cpf", expression = "java(domain.getCpf().getValue())")
    @Mapping(target = "email", expression = "java(domain.getEmail().getValue())")
    @Mapping(target = "phone", expression = "java(domain.getPhone().getValue())")
    @Mapping(target = "kycStatus", expression = "java(domain.getKycStatus().name())")
    @Mapping(target = "createdBy", ignore = true)
    @Mapping(target = "updatedBy", ignore = true)
    CustomerJpaEntity toEntity(CustomerModel domain);
}
