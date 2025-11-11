package com.example.services.definitions;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.example.api.dto.AssignmentDTOs;
import com.example.domain.entities.Assignment;
import com.example.domain.repositories.AssignmentRepository;
import com.example.exceptions.NotFoundException;
import com.example.services.mappers.AssignmentMapper;

import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
@Transactional
public class AssignmentServiceimpl implements AssignmentService {
    private final AssignmentRepository repo;
    private final AssignmentMapper mapper; 

    @Override
    public AssignmentDTOs.AssignmentResponse createAssignment(AssignmentDTOs.CreateAssignmentRequest req) {
        Assignment assignment = mapper.toEntity(req);
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
