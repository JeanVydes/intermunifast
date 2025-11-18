package com.example.services.implementations;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.example.api.dto.AssignmentDTOs;
import com.example.domain.entities.Account;
import com.example.domain.entities.Assignment;
import com.example.domain.entities.Trip;
import com.example.domain.repositories.AccountRepository;
import com.example.domain.repositories.AssignmentRepository;
import com.example.domain.repositories.TripRepository;
import com.example.exceptions.NotFoundException;
import com.example.services.definitions.AssignmentService;
import com.example.services.mappers.AssignmentMapper;

import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
@Transactional
public class AssignmentServiceimpl implements AssignmentService {
    private final AssignmentRepository repo;
    private final AccountRepository accountRepo;
    private final TripRepository tripRepo;
    private final AssignmentMapper mapper;

    @Override
    public AssignmentDTOs.AssignmentResponse createAssignment(AssignmentDTOs.CreateAssignmentRequest req) {
        Account dispatcher = accountRepo.findById(req.dispatcherId())
                .orElseThrow(() -> new NotFoundException("Account %d not found".formatted(req.dispatcherId())));
        Account driver = accountRepo.findById(req.driverId())
                .orElseThrow(() -> new NotFoundException("Account %d not found".formatted(req.driverId())));
        Trip trip = tripRepo.findById(req.tripId())
                .orElseThrow(() -> new NotFoundException("Trip %d not found".formatted(req.tripId())));
        Assignment assignment = Assignment.builder()
                .checklistOk(req.checklistOk()).assignedAt(req.assignedAt())
                .dispatcher(dispatcher)
                .driver(driver)
                .trip(trip)
                .build();
        return mapper.toResponse(repo.save(assignment));
    }

    @Override
    @Transactional(readOnly = true)
    public AssignmentDTOs.AssignmentResponse getAssignmentById(Long id) {
        return repo.findById(id).map(mapper::toResponse)
                .orElseThrow(() -> new NotFoundException("Assignment %d not found".formatted(id)));
    }

    @Override
    public AssignmentDTOs.AssignmentResponse updateAssignment(Long id, AssignmentDTOs.UpdateAssignmentRequest req) {
        Assignment assignment = repo.findById(id)
                .orElseThrow(() -> new NotFoundException("Assignment %d not found".formatted(id)));
        if (req.dispatcherId() != null) {
            accountRepo.findById(req.dispatcherId())
                    .orElseThrow(() -> new NotFoundException("Account %d not found".formatted(req.dispatcherId())));
        }
        if (req.driverId() != null) {
            accountRepo.findById(req.driverId())
                    .orElseThrow(() -> new NotFoundException("Account %d not found".formatted(req.driverId())));
        }
        if (req.tripId() != null) {
            Trip trip = tripRepo.findById(req.tripId())
                    .orElseThrow(() -> new NotFoundException("Trip %d not found".formatted(req.tripId())));
            assignment.setTrip(trip);
        }
        if (req.tripId() != null) {
            tripRepo.findById(req.tripId())
                    .orElseThrow(() -> new NotFoundException("Trip %d not found".formatted(req.tripId())));

        }
        mapper.patch(assignment, req);
        return mapper.toResponse(repo.save(assignment));
    }

    @Override
    public void deleteAssignment(Long id) {
        Assignment assignment = repo.findById(id)
                .orElseThrow(() -> new NotFoundException("Assignment %d not found".formatted(id)));
        repo.delete(assignment);
    }

}
