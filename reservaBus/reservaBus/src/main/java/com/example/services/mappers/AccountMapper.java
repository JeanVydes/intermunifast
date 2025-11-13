package com.example.services.mappers;

import org.mapstruct.Mapper;
import org.mapstruct.Mapping;

import com.example.api.dto.AccountDTOs;
import com.example.domain.entities.Account;

@Mapper(componentModel = "spring")
public interface AccountMapper {

    @Mapping(target = "id", ignore = true)
    @Mapping(target = "passwordHash", ignore = true)
    Account toEntity(AccountDTOs.CreateAccountRequest dto);

    AccountDTOs.AccountResponse toResponse(Account entity);
}
