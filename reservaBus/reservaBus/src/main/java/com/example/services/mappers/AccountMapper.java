package com.example.services.mappers;

import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.MappingTarget;

import com.example.api.dto.AccountDTOs;
import com.example.domain.entities.Account;

@Mapper(componentModel = "spring")
public interface AccountMapper {

    @Mapping(target = "id", ignore = true)
    @Mapping( source = "password", target = "passwordHash")
    Account toEntity(AccountDTOs.CreateAccountRequest dto);
    
    AccountDTOs.AccountResponse toResponse(Account entity);

    void patch(@MappingTarget Account entity, AccountDTOs.UpdateAccountRequest dto);
}
