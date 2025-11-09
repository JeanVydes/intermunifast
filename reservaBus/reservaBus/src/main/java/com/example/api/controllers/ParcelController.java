package com.example.api.controllers;

import org.springframework.http.ResponseEntity;
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

import com.example.api.dto.ParcelDTOs;

@RestController
@RequestMapping("/api/parcels")
public class ParcelController {
    private final ParcelService parcelService;

    public ParcelController(ParcelService parcelService) {
        this.parcelService = parcelService;
    }

    @PostMapping
    public ResponseEntity<ParcelDTOs.ParcelResponse> create(
            @Validated @RequestBody ParcelDTOs.CreateParcelRequest req,
            UriComponentsBuilder uriBuilder) {
        ParcelDTOs.ParcelResponse createdParcel = parcelService.createParcel(req);
        return ResponseEntity.created(
                uriBuilder.path("/api/parcels/{id}").buildAndExpand(createdParcel.id()).toUri())
                .body(createdParcel);
    }

    @GetMapping("/{id}")
    public ResponseEntity<ParcelDTOs.ParcelResponse> getById(@PathVariable Long id) {
        ParcelDTOs.ParcelResponse parcel = parcelService.getParcelById(id);
        return ResponseEntity.ok(parcel);
    }

    // we use this endpoint to modify parcel details, the required specs not follow the REST conventions
    @PatchMapping("/{id}")
    public ResponseEntity<ParcelDTOs.ParcelResponse> update(
            @PathVariable Long id,
            @Validated @RequestBody ParcelDTOs.UpdateParcelRequest req) {
        ParcelDTOs.ParcelResponse updatedParcel = parcelService.updateParcel(id, req);
        return ResponseEntity.ok(updatedParcel);
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> delete(@PathVariable Long id) {
        parcelService.deleteParcel(id);
        return ResponseEntity.noContent().build();
    }
}
