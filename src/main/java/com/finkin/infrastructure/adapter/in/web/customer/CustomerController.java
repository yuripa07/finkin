package com.finkin.infrastructure.adapter.in.web.customer;

import com.finkin.domain.model.customer.CustomerModel;
import com.finkin.domain.port.in.IRegisterCustomerUseCase;
import com.finkin.domain.port.out.ICustomerRepository;
import com.finkin.domain.exception.CustomerNotFoundException;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

import java.util.UUID;

@RestController
@RequestMapping("/customers")
@RequiredArgsConstructor
@Tag(name = "Customers", description = "Consulta de dados do titular")
@SecurityRequirement(name = "bearerAuth")
public class CustomerController {

    private final ICustomerRepository customerRepository;

    @GetMapping("/me")
    @Operation(summary = "Dados do customer logado")
    public CustomerResponse getMe(Authentication auth) {
        UUID customerId = UUID.fromString(auth.getName());
        var customer = customerRepository.findById(customerId)
            .orElseThrow(() -> new CustomerNotFoundException(customerId));
        return toResponse(customer);
    }

    private CustomerResponse toResponse(CustomerModel c) {
        return new CustomerResponse(
            c.getId().toString(),
            c.getCpf().formatted(),
            c.getFullName(),
            c.getBirthDate().toString(),
            c.getEmail().getValue(),
            c.getPhone().getValue(),
            c.getKycStatus().name()
        );
    }

    record CustomerResponse(
        String id, String cpf, String fullName,
        String birthDate, String email, String phone, String kycStatus
    ) {}
}
