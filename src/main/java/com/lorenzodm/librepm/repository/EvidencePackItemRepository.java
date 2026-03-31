package com.lorenzodm.librepm.repository;

import com.lorenzodm.librepm.core.entity.EvidencePackItem;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface EvidencePackItemRepository extends JpaRepository<EvidencePackItem, String> {

    List<EvidencePackItem> findByPackIdOrderByItemOrderAsc(String packId);

    void deleteByPackIdAndAssetId(String packId, String assetId);

    void deleteByPackIdAndNoteId(String packId, String noteId);
}
