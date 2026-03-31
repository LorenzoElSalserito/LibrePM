package com.lorenzodm.librepm.service;

import com.lorenzodm.librepm.core.entity.EvidencePack;
import com.lorenzodm.librepm.core.entity.EvidencePackItem;

import java.util.List;

public interface EvidencePackService {

    EvidencePack create(String name, String description, String projectId,
                        String packType, String createdBy);

    EvidencePack finalize(String packId);

    List<EvidencePack> listByProject(String projectId);

    EvidencePackItem addAssetItem(String packId, String assetId, int order);

    EvidencePackItem addNoteItem(String packId, String noteId, int order);

    List<EvidencePackItem> listItems(String packId);

    void removeItem(String itemId);

    void delete(String packId);
}
