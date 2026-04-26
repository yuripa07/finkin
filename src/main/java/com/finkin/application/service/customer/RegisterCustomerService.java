package com.finkin.application.service.customer;

import com.finkin.domain.exception.CustomerAlreadyExistsException;
import com.finkin.domain.model.customer.*;
import com.finkin.domain.port.in.IRegisterCustomerUseCase;
import com.finkin.domain.port.out.ICustomerRepository;
import com.finkin.infrastructure.config.KycProperties;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.ZonedDateTime;
import java.util.UUID;

@Slf4j
@Service
@RequiredArgsConstructor
public class RegisterCustomerService implements IRegisterCustomerUseCase {

    private final ICustomerRepository customerRepository;
    private final KycProperties kycProperties;

    @Override
    @Transactional
    public CustomerModel register(Command command) {
        var cpf = new CpfModel(command.cpf());

        if (customerRepository.existsByCpf(cpf)) {
            throw new CustomerAlreadyExistsException(command.cpf());
        }

        // KYC mock: em dev auto-approve=true, em produção inicia como PENDING
        KycStatus initialKyc = kycProperties.isAutoApprove()
            ? KycStatus.APPROVED
            : KycStatus.PENDING;

        var now = ZonedDateTime.now();
        var customer = CustomerModel.builder()
            .id(UUID.randomUUID())
            .cpf(cpf)
            .fullName(command.fullName().strip())
            .birthDate(command.birthDate())
            .email(new EmailModel(command.email()))
            .phone(new PhoneModel(command.phone()))
            .kycStatus(initialKyc)
            .createdAt(now)
            .updatedAt(now)
            .build();

        var saved = customerRepository.save(customer);
        log.info("CustomerModel registrado com KYC={}: id={}", initialKyc, saved.getId());
        return saved;
    }
}
