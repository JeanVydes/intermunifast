package com.example.services;

import com.example.api.dto.FareRuleDTOs;
import com.example.domain.entities.FareRule;
import com.example.domain.entities.Route;
import com.example.domain.repositories.FareRuleRepository;
import com.example.domain.repositories.RouteRepository;
import com.example.exceptions.NotFoundException;
import com.example.services.definitions.FareRuleServiceImpl;
import com.example.services.mappers.FareRuleMapper;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
@DisplayName("FareRule Service Unit Tests")
class FareRuleServiceTest {

    @Mock
    private FareRuleRepository fareRuleRepository;

    @Mock
    private FareRuleMapper fareRuleMapper;

    @Mock
    private RouteRepository routeRepository;

    @InjectMocks
    private FareRuleServiceImpl fareRuleService;

    private FareRule fareRule;
    private Route route;
    private FareRuleDTOs.FareRuleResponse fareRuleResponse;
    private FareRuleDTOs.CreateFareRuleRequest createRequest;
    private FareRuleDTOs.UpdateFareRuleRequest updateRequest;

    @BeforeEach
    void setUp() {
        route = Route.builder().id(1L).name("Test Route").build();

        fareRule = FareRule.builder()
                .id(1L)
                .route(route)
                .dynamicPricing(true)
                .childrenDiscount(0.5)
                .seniorDiscount(0.3)
                .studentDiscount(0.2)
                .build();

        fareRuleResponse = new FareRuleDTOs.FareRuleResponse(1L, 1L, 0.5, 0.3, 0.2, true);
        createRequest = new FareRuleDTOs.CreateFareRuleRequest(1L, true, 0.5, 0.3, 0.2);
        updateRequest = new FareRuleDTOs.UpdateFareRuleRequest(1L, false, 0.6, 0.4, 0.25);
    }

    @Test
    @DisplayName("Should create fare rule successfully")
    void shouldCreateFareRule() {
        // Given
        when(routeRepository.findById(1L)).thenReturn(Optional.of(route));
        when(fareRuleRepository.save(any(FareRule.class))).thenReturn(fareRule);
        when(fareRuleMapper.toResponse(fareRule)).thenReturn(fareRuleResponse);

        // When
        FareRuleDTOs.FareRuleResponse result = fareRuleService.createFareRule(createRequest);

        // Then
        assertThat(result).isNotNull();
        assertThat(result.id()).isEqualTo(1L);
        verify(fareRuleRepository).save(any(FareRule.class));
    }

    @Test
    @DisplayName("Should get fare rule by ID successfully")
    void shouldGetFareRuleById() {
        // Given
        when(fareRuleRepository.findById(1L)).thenReturn(Optional.of(fareRule));
        when(fareRuleMapper.toResponse(fareRule)).thenReturn(fareRuleResponse);

        // When
        FareRuleDTOs.FareRuleResponse result = fareRuleService.getFareRuleById(1L);

        // Then
        assertThat(result).isNotNull();
        assertThat(result.dynamicPricing()).isTrue();
        verify(fareRuleRepository).findById(1L);
    }

    @Test
    @DisplayName("Should throw NotFoundException when fare rule not found by ID")
    void shouldThrowNotFoundExceptionWhenFareRuleNotFoundById() {
        // Given
        when(fareRuleRepository.findById(999L)).thenReturn(Optional.empty());

        // When & Then
        assertThatThrownBy(() -> fareRuleService.getFareRuleById(999L))
                .isInstanceOf(NotFoundException.class)
                .hasMessageContaining("FareRule 999 not found");
    }

    @Test
    @DisplayName("Should update fare rule successfully")
    void shouldUpdateFareRule() {
        // Given
        when(fareRuleRepository.findById(1L)).thenReturn(Optional.of(fareRule));
        when(routeRepository.findById(1L)).thenReturn(Optional.of(route));
        when(fareRuleRepository.save(fareRule)).thenReturn(fareRule);
        when(fareRuleMapper.toResponse(fareRule)).thenReturn(fareRuleResponse);

        // When
        FareRuleDTOs.FareRuleResponse result = fareRuleService.updateFareRule(1L, updateRequest);

        // Then
        assertThat(result).isNotNull();
        verify(fareRuleMapper).patch(fareRule, updateRequest);
        verify(fareRuleRepository).save(fareRule);
    }

    @Test
    @DisplayName("Should delete fare rule successfully")
    void shouldDeleteFareRule() {
        // Given
        when(fareRuleRepository.findById(1L)).thenReturn(Optional.of(fareRule));

        // When
        fareRuleService.deleteFareRule(1L);

        // Then
        verify(fareRuleRepository).delete(fareRule);
    }

    @Test
    @DisplayName("Should throw NotFoundException when deleting non-existent fare rule")
    void shouldThrowNotFoundExceptionWhenDeletingNonExistentFareRule() {
        // Given
        when(fareRuleRepository.findById(999L)).thenReturn(Optional.empty());

        // When & Then
        assertThatThrownBy(() -> fareRuleService.deleteFareRule(999L))
                .isInstanceOf(NotFoundException.class);
    }
}
