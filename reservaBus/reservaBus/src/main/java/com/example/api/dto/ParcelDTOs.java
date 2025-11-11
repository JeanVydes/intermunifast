package com.example.api.dto;

import java.io.Serializable;

public class ParcelDTOs {
    public record CreateParcelRequest(
        String code,
        String senderName,
        String senderPhone,
        String receiverName,
        String receiverPhone,
        Long fromStopId,
        Long toStopId
    ) implements Serializable {}

    public record UpdateParcelRequest(
        String code,
        String senderName,
        String senderPhone,
        String receiverName,
        String receiverPhone,
        Long fromStopId,
        Long toStopId,
        String status,
        String proofPhotoUrl,
        Double price
    ) implements Serializable {}

    public record ParcelResponse(
        Long id,
        String code,
        String senderName,
        String senderPhone,
        String receiverName,
        String receiverPhone,
        Long fromStopId,
        Long toStopId,
        String status,
        String proofPhotoUrl,
        Double price
    ) implements Serializable {}
}
