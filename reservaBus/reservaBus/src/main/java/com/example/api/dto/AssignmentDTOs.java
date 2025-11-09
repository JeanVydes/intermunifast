package com.example.api.dto;

import java.io.Serializable;
import java.time.LocalDateTime;

public class AssignmentDTOs {
    public record CreateAssignmentRequest(boolean checklistOk, LocalDateTime assignedAt, Long driverId, Long dispatcherId, Long tripId) implements Serializable {}
    public record AssignmentResponse(Long id, boolean checklistOk, LocalDateTime assignedAt, Long driverId, Long dispatcherId, Long tripId) implements Serializable {}
    public record UpdateAssignmentRequest(Long id, Boolean checklistOk, LocalDateTime assignedAt, Long driverId, Long dispatcherId, Long tripId) implements Serializable {}
}