package com.example.services.mappers;

import org.mapstruct.Mapper;
import org.mapstruct.ReportingPolicy;

import com.example.api.dto.AccountDTOs;
import com.example.domain.entities.Account;

@Mapper(componentModel = "spring", unmappedTargetPolicy = ReportingPolicy.IGNORE)
public interface AccountMapper {

    Account toEntity(AccountDTOs.CreateAccountRequest dto);

    AccountDTOs.AccountResponse toResponse(Account entity);
}
