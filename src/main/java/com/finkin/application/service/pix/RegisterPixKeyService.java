package com.finkin.application.service.pix;

import com.finkin.domain.exception.AccountNotFoundException;
import com.finkin.domain.model.pix.PixKey;
import com.finkin.domain.model.pix.PixKeyType;
import com.finkin.domain.port.in.RegisterPixKeyUseCase;
import com.finkin.domain.port.out.AccountRepository;
import com.finkin.domain.port.out.PixKeyRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Slf4j
@Service
@RequiredArgsConstructor
public class RegisterPixKeyService implements RegisterPixKeyUseCase {

    private final PixKeyRepository pixKeyRepository;
    private final AccountRepository accountRepository;

    @Override
    @Transactional
    public PixKey register(Command command) {
        accountRepository.findById(command.accountId())
            .orElseThrow(() -> new AccountNotFoundException(command.accountId()));

        PixKey key = command.keyType() == PixKeyType.RANDOM
            ? PixKey.createRandom(command.accountId())
            : PixKey.create(command.accountId(), command.keyType(), command.keyValue());

        if (pixKeyRepository.existsByKeyValue(key.getKeyValue())) {
            throw new com.finkin.domain.exception.DomainException(
                "Chave Pix já cadastrada: " + command.keyValue()
            ) {};
        }

        var saved = pixKeyRepository.save(key);
        log.info("Chave Pix registrada: tipo={}, account={}", saved.getKeyType(), saved.getAccountId());
        return saved;
    }
}
