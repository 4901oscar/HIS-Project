package com.medframe.clinical.infrastructure.persistence.adapter;

import com.medframe.clinical.domain.model.ManchesterDiscriminator;
import com.medframe.clinical.domain.model.ManchesterMotif;
import com.medframe.clinical.domain.port.out.ManchesterCatalogRepository;
import com.medframe.clinical.infrastructure.persistence.repository.JpaManchesterDiscriminatorRepository;
import com.medframe.clinical.infrastructure.persistence.repository.JpaManchesterMotifRepository;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

/**
 * Adapter that implements the ManchesterCatalogRepository output port using JPA.
 */
@Component
public class ManchesterCatalogRepositoryAdapter implements ManchesterCatalogRepository {

    private final JpaManchesterMotifRepository motifRepository;
    private final JpaManchesterDiscriminatorRepository discriminatorRepository;
    private final ManchesterMotifMapper motifMapper;
    private final ManchesterDiscriminatorMapper discriminatorMapper;

    public ManchesterCatalogRepositoryAdapter(
            JpaManchesterMotifRepository motifRepository,
            JpaManchesterDiscriminatorRepository discriminatorRepository,
            ManchesterMotifMapper motifMapper,
            ManchesterDiscriminatorMapper discriminatorMapper) {
        this.motifRepository = motifRepository;
        this.discriminatorRepository = discriminatorRepository;
        this.motifMapper = motifMapper;
        this.discriminatorMapper = discriminatorMapper;
    }

    @Override
    public List<ManchesterMotif> findAllActiveMotifs() {
        return motifRepository.findByActiveTrue().stream()
                .map(motifMapper::toDomain)
                .collect(Collectors.toList());
    }

    @Override
    public Optional<ManchesterMotif> findMotifById(String id) {
        return motifRepository.findById(id)
                .map(motifMapper::toDomain);
    }

    @Override
    public List<ManchesterDiscriminator> findDiscriminatorsByIds(List<String> ids) {
        return discriminatorRepository.findByIdInAndActiveTrue(ids).stream()
                .map(discriminatorMapper::toDomain)
                .collect(Collectors.toList());
    }
}
