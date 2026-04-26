package com.finkin.infrastructure.config;

import lombok.Getter;
import lombok.Setter;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.validation.annotation.Validated;

import jakarta.validation.constraints.NotBlank;

@Getter
@Setter
@Validated
@ConfigurationProperties(prefix = "finkin.bank")
public class BankProperties {

    @NotBlank
    private String ispb;

    @NotBlank
    private String agency;

    @NotBlank
    private String name;
}
