package com.example.services.definitions;

import com.example.api.dto.AssignmentDTOs;

public interface AssignmentService {
    AssignmentDTOs.AssignmentResponse createAssignment(AssignmentDTOs.CreateAssignmentRequest req);

    AssignmentDTOs.AssignmentResponse getAssignmentById(Long id);

    AssignmentDTOs.AssignmentResponse updateAssignment(Long id, AssignmentDTOs.UpdateAssignmentRequest req);

    void deleteAssignment(Long id);
}
