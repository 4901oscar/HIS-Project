package com.medframe.clinical.infrastructure.rest.controller;

import com.medframe.clinical.domain.model.PriorityLevel;
import com.medframe.clinical.infrastructure.persistence.entity.ManchesterDiscriminatorEntity;
import com.medframe.clinical.infrastructure.persistence.entity.ManchesterMotifEntity;
import com.medframe.clinical.infrastructure.persistence.repository.JpaManchesterDiscriminatorRepository;
import com.medframe.clinical.infrastructure.persistence.repository.JpaManchesterMotifRepository;
import com.medframe.clinical.infrastructure.rest.dto.request.DiscriminatorRequest;
import com.medframe.clinical.infrastructure.rest.dto.request.MotifRequest;
import com.medframe.clinical.infrastructure.rest.dto.response.DiscriminatorCatalogResponse;
import com.medframe.clinical.infrastructure.rest.dto.response.MotifCatalogResponse;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/api/clinical/catalog")
@RequiredArgsConstructor
public class ManchesterCatalogController {

    private final JpaManchesterMotifRepository motifRepo;
    private final JpaManchesterDiscriminatorRepository discriminatorRepo;

    // ── Motivos ───────────────────────────────────────────────────────────────

    @GetMapping("/motifs")
    public ResponseEntity<List<MotifCatalogResponse>> getMotifs() {
        return ResponseEntity.ok(
            motifRepo.findAll().stream().map(MotifCatalogResponse::from).collect(Collectors.toList())
        );
    }

    @PostMapping("/motifs")
    public ResponseEntity<MotifCatalogResponse> createMotif(@Valid @RequestBody MotifRequest req) {
        ManchesterMotifEntity e = new ManchesterMotifEntity();
        e.setCode(req.getCode().toUpperCase());
        e.setDescription(req.getDescription());
        e.setCategory(req.getCategory());
        return ResponseEntity.status(HttpStatus.CREATED).body(MotifCatalogResponse.from(motifRepo.save(e)));
    }

    @PutMapping("/motifs/{id}")
    public ResponseEntity<MotifCatalogResponse> updateMotif(@PathVariable String id, @Valid @RequestBody MotifRequest req) {
        ManchesterMotifEntity e = motifRepo.findById(id)
                .orElseThrow(() -> new RuntimeException("Motivo no encontrado"));
        e.setCode(req.getCode().toUpperCase());
        e.setDescription(req.getDescription());
        e.setCategory(req.getCategory());
        if (req.getActive() != null) {
            e.setActive(req.getActive());
        }
        return ResponseEntity.ok(MotifCatalogResponse.from(motifRepo.save(e)));
    }

    @PatchMapping("/motifs/{id}/toggle")
    public ResponseEntity<MotifCatalogResponse> toggleMotif(@PathVariable String id) {
        ManchesterMotifEntity e = motifRepo.findById(id)
                .orElseThrow(() -> new RuntimeException("Motivo no encontrado"));
        e.setActive(!e.isActive());
        return ResponseEntity.ok(MotifCatalogResponse.from(motifRepo.save(e)));
    }

    // ── Discriminadores ───────────────────────────────────────────────────────

    @GetMapping("/discriminators")
    public ResponseEntity<List<DiscriminatorCatalogResponse>> getDiscriminators(
            @RequestParam(required = false) String motifId) {
        List<ManchesterDiscriminatorEntity> list = motifId != null
                ? discriminatorRepo.findAll().stream()
                    .filter(d -> d.getMotif() != null && motifId.equals(d.getMotif().getId()))
                    .collect(Collectors.toList())
                : discriminatorRepo.findAll();
        return ResponseEntity.ok(list.stream().map(DiscriminatorCatalogResponse::from).collect(Collectors.toList()));
    }

    @PostMapping("/discriminators")
    public ResponseEntity<DiscriminatorCatalogResponse> createDiscriminator(@Valid @RequestBody DiscriminatorRequest req) {
        ManchesterDiscriminatorEntity e = new ManchesterDiscriminatorEntity();
        e.setCode(req.getCode().toUpperCase());
        e.setDescription(req.getDescription());
        e.setPriorityLevel(PriorityLevel.valueOf(req.getPriorityLevel()));
        if (req.getMotifId() != null) {
            motifRepo.findById(req.getMotifId()).ifPresent(e::setMotif);
        }
        return ResponseEntity.status(HttpStatus.CREATED).body(DiscriminatorCatalogResponse.from(discriminatorRepo.save(e)));
    }

    @PutMapping("/discriminators/{id}")
    public ResponseEntity<DiscriminatorCatalogResponse> updateDiscriminator(@PathVariable String id, @Valid @RequestBody DiscriminatorRequest req) {
        ManchesterDiscriminatorEntity e = discriminatorRepo.findById(id)
                .orElseThrow(() -> new RuntimeException("Discriminador no encontrado"));
        e.setCode(req.getCode().toUpperCase());
        e.setDescription(req.getDescription());
        e.setPriorityLevel(PriorityLevel.valueOf(req.getPriorityLevel()));
        if (req.getMotifId() != null) {
            motifRepo.findById(req.getMotifId()).ifPresent(e::setMotif);
        }
        if (req.getActive() != null) {
            e.setActive(req.getActive());
        }
        return ResponseEntity.ok(DiscriminatorCatalogResponse.from(discriminatorRepo.save(e)));
    }

    @PatchMapping("/discriminators/{id}/toggle")
    public ResponseEntity<DiscriminatorCatalogResponse> toggleDiscriminator(@PathVariable String id) {
        ManchesterDiscriminatorEntity e = discriminatorRepo.findById(id)
                .orElseThrow(() -> new RuntimeException("Discriminador no encontrado"));
        e.setActive(!e.isActive());
        return ResponseEntity.ok(DiscriminatorCatalogResponse.from(discriminatorRepo.save(e)));
    }
}
