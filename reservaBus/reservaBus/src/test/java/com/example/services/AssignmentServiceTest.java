package com.example.services;

import com.example.api.dto.AssignmentDTOs;
import com.example.domain.entities.Account;
import com.example.domain.entities.Assignment;
import com.example.domain.entities.Trip;
import com.example.domain.enums.AccountRole;
import com.example.domain.repositories.AccountRepository;
import com.example.domain.repositories.AssignmentRepository;
import com.example.domain.repositories.TripRepository;
import com.example.exceptions.NotFoundException;
import com.example.services.implementations.AssignmentServiceimpl;
import com.example.services.mappers.AssignmentMapper;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.LocalDateTime;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
@DisplayName("Assignment Service Unit Tests")
class AssignmentServiceTest {

    @Mock
    private AssignmentRepository assignmentRepository;

    @Mock
    private AssignmentMapper assignmentMapper;

    @Mock
    private AccountRepository accountRepository;

    @Mock
    private TripRepository tripRepository;

    @InjectMocks
    private AssignmentServiceimpl assignmentService;

    private Assignment assignment;
    private Account driver;
    private Account dispatcher;
    private Trip trip;
    private AssignmentDTOs.AssignmentResponse assignmentResponse;
    private AssignmentDTOs.CreateAssignmentRequest createRequest;
    private AssignmentDTOs.UpdateAssignmentRequest updateRequest;

    @BeforeEach
    void setUp() {
        driver = Account.builder()
                .id(1L)
                .email("driver@test.com")
                .role(AccountRole.DRIVER)
                .build();

        dispatcher = Account.builder()
                .id(2L)
                .email("dispatcher@test.com")
                .role(AccountRole.DISPATCHER)
                .build();

        trip = Trip.builder().id(1L).build();

        assignment = Assignment.builder()
                .id(1L)
                .checklistOk(true)
                .assignedAt(LocalDateTime.now())
                .driver(driver)
                .dispatcher(dispatcher)
                .trip(trip)
                .build();

        assignmentResponse = new AssignmentDTOs.AssignmentResponse(1L, true, assignment.getAssignedAt(), 1L, 2L, 1L);
        createRequest = new AssignmentDTOs.CreateAssignmentRequest(true, LocalDateTime.now(), 1L, 2L, 1L);
        updateRequest = new AssignmentDTOs.UpdateAssignmentRequest(1L, false, LocalDateTime.now(), 1L, 2L, 1L);
    }

    @Test
    @DisplayName("Should create assignment successfully")
    void shouldCreateAssignment() {
        // Given
        when(accountRepository.findById(1L)).thenReturn(Optional.of(driver));
        when(accountRepository.findById(2L)).thenReturn(Optional.of(dispatcher));
        when(tripRepository.findById(1L)).thenReturn(Optional.of(trip));
        when(assignmentRepository.save(any(Assignment.class))).thenReturn(assignment);
        when(assignmentMapper.toResponse(assignment)).thenReturn(assignmentResponse);

        // When
        AssignmentDTOs.AssignmentResponse result = assignmentService.createAssignment(createRequest);

        // Then
        assertThat(result).isNotNull();
        assertThat(result.id()).isEqualTo(1L);
        verify(assignmentRepository).save(any(Assignment.class));
    }

    @Test
    @DisplayName("Should get assignment by ID successfully")
    void shouldGetAssignmentById() {
        // Given
        when(assignmentRepository.findById(1L)).thenReturn(Optional.of(assignment));
        when(assignmentMapper.toResponse(assignment)).thenReturn(assignmentResponse);

        // When
        AssignmentDTOs.AssignmentResponse result = assignmentService.getAssignmentById(1L);

        // Then
        assertThat(result).isNotNull();
        assertThat(result.checklistOk()).isTrue();
        verify(assignmentRepository).findById(1L);
    }

    @Test
    @DisplayName("Should throw NotFoundException when assignment not found by ID")
    void shouldThrowNotFoundExceptionWhenAssignmentNotFoundById() {
        // Given
        when(assignmentRepository.findById(999L)).thenReturn(Optional.empty());

        // When & Then
        assertThatThrownBy(() -> assignmentService.getAssignmentById(999L))
                .isInstanceOf(NotFoundException.class)
                .hasMessageContaining("Assignment 999 not found");
    }

    @Test
    @DisplayName("Should update assignment successfully")
    void shouldUpdateAssignment() {
        // Given
        when(assignmentRepository.findById(1L)).thenReturn(Optional.of(assignment));
        when(accountRepository.findById(1L)).thenReturn(Optional.of(driver));
        when(accountRepository.findById(2L)).thenReturn(Optional.of(dispatcher));
        when(tripRepository.findById(1L)).thenReturn(Optional.of(trip));
        when(assignmentRepository.save(assignment)).thenReturn(assignment);
        when(assignmentMapper.toResponse(assignment)).thenReturn(assignmentResponse);

        // When
        AssignmentDTOs.AssignmentResponse result = assignmentService.updateAssignment(1L, updateRequest);

        // Then
        assertThat(result).isNotNull();
        verify(assignmentMapper).patch(assignment, updateRequest);
        verify(assignmentRepository).save(assignment);
    }

    @Test
    @DisplayName("Should delete assignment successfully")
    void shouldDeleteAssignment() {
        // Given
        when(assignmentRepository.findById(1L)).thenReturn(Optional.of(assignment));

        // When
        assignmentService.deleteAssignment(1L);

        // Then
        verify(assignmentRepository).delete(assignment);
    }

    @Test
    @DisplayName("Should throw NotFoundException when deleting non-existent assignment")
    void shouldThrowNotFoundExceptionWhenDeletingNonExistentAssignment() {
        // Given
        when(assignmentRepository.findById(999L)).thenReturn(Optional.empty());

        // When & Then
        assertThatThrownBy(() -> assignmentService.deleteAssignment(999L))
                .isInstanceOf(NotFoundException.class);
    }
}
