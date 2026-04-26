package com.finkin.application.service.pix;

import com.finkin.domain.exception.AccountNotFoundException;
import com.finkin.domain.model.pix.PixKeyModel;
import com.finkin.domain.model.pix.PixKeyType;
import com.finkin.domain.port.in.IRegisterPixKeyUseCase;
import com.finkin.domain.port.out.IAccountRepository;
import com.finkin.domain.port.out.IPixKeyRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Slf4j
@Service
@RequiredArgsConstructor
public class RegisterPixKeyService implements IRegisterPixKeyUseCase {

    private final IPixKeyRepository pixKeyRepository;
    private final IAccountRepository accountRepository;

    @Override
    @Transactional
    public PixKeyModel register(Command command) {
        accountRepository.findById(command.accountId())
            .orElseThrow(() -> new AccountNotFoundException(command.accountId()));

        PixKeyModel key = command.keyType() == PixKeyType.RANDOM
            ? PixKeyModel.createRandom(command.accountId())
            : PixKeyModel.create(command.accountId(), command.keyType(), command.keyValue());

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
