package com.example.api.controllers;

import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.util.UriComponentsBuilder;

import com.example.api.dto.AssignmentDTOs;
import com.example.services.definitions.AssignmentService;

@RestController
@RequestMapping("/api/assignments")
public class AssigmentController {
    private final AssignmentService assignmentService;

    public AssigmentController(AssignmentService assignmentService) {
        this.assignmentService = assignmentService;
    }

    @PreAuthorize("hasAnyAuthority('DRIVER', 'DISPATCHER', 'ADMIN')")
    @PostMapping
    public ResponseEntity<AssignmentDTOs.AssignmentResponse> create(
            @Validated @RequestBody AssignmentDTOs.CreateAssignmentRequest req,
            UriComponentsBuilder uriBuilder) {
        AssignmentDTOs.AssignmentResponse createdAssignment = assignmentService.createAssignment(req);
        return ResponseEntity.created(
                uriBuilder.path("/api/assignments/{id}").buildAndExpand(createdAssignment.id()).toUri())
                .body(createdAssignment);
    }

    @PreAuthorize("hasAnyAuthority('DRIVER', 'DISPATCHER', 'ADMIN')")
    @GetMapping("/{id}")
    public ResponseEntity<AssignmentDTOs.AssignmentResponse> getById(@PathVariable Long id) {
        AssignmentDTOs.AssignmentResponse assignment = assignmentService.getAssignmentById(id);
        return ResponseEntity.ok(assignment);
    }

    @PreAuthorize("hasAnyAuthority('DRIVER', 'DISPATCHER', 'ADMIN')")
    @PatchMapping("/{id}")
    public ResponseEntity<AssignmentDTOs.AssignmentResponse> update(
            @PathVariable Long id,
            @Validated @RequestBody AssignmentDTOs.UpdateAssignmentRequest req) {
        AssignmentDTOs.AssignmentResponse updatedAssignment = assignmentService.updateAssignment(id, req);
        return ResponseEntity.ok(updatedAssignment);
    }

    @PreAuthorize("hasAnyAuthority('DRIVER', 'DISPATCHER', 'ADMIN')")
    @DeleteMapping("/{id}")
    public ResponseEntity<Void> delete(@PathVariable Long id) {
        assignmentService.deleteAssignment(id);
        return ResponseEntity.noContent().build();
    }
}
