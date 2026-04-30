package com.medframe.clinical.domain.port.out;

import com.medframe.clinical.domain.model.ManchesterDiscriminator;
import com.medframe.clinical.domain.model.ManchesterMotif;

import java.util.List;
import java.util.Optional;

public interface ManchesterCatalogRepository {
    List<ManchesterMotif> findAllActiveMotifs();
    Optional<ManchesterMotif> findMotifById(String id);
    List<ManchesterDiscriminator> findDiscriminatorsByIds(List<String> ids);
}
