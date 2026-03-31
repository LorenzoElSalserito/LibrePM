package com.lorenzodm.librepm.service.impl;

import com.lorenzodm.librepm.api.exception.BadRequestException;
import com.lorenzodm.librepm.api.exception.ResourceNotFoundException;
import com.lorenzodm.librepm.core.entity.EvidencePack;
import com.lorenzodm.librepm.core.entity.EvidencePackItem;
import com.lorenzodm.librepm.repository.*;
import com.lorenzodm.librepm.service.EvidencePackService;
import jakarta.transaction.Transactional;
import org.springframework.stereotype.Service;

import java.time.Instant;
import java.util.List;

@Service
@Transactional
public class EvidencePackServiceImpl implements EvidencePackService {

    private final EvidencePackRepository packRepository;
    private final EvidencePackItemRepository itemRepository;
    private final ProjectRepository projectRepository;
    private final AssetRepository assetRepository;
    private final NoteRepository noteRepository;
    private final UserRepository userRepository;

    public EvidencePackServiceImpl(EvidencePackRepository packRepository,
                                   EvidencePackItemRepository itemRepository,
                                   ProjectRepository projectRepository,
                                   AssetRepository assetRepository,
                                   NoteRepository noteRepository,
                                   UserRepository userRepository) {
        this.packRepository = packRepository;
        this.itemRepository = itemRepository;
        this.projectRepository = projectRepository;
        this.assetRepository = assetRepository;
        this.noteRepository = noteRepository;
        this.userRepository = userRepository;
    }

    @Override
    public EvidencePack create(String name, String description, String projectId,
                               String packType, String createdBy) {
        EvidencePack pack = new EvidencePack();
        pack.setName(name);
        pack.setDescription(description);
        if (projectId != null) {
            projectRepository.findById(projectId).ifPresent(pack::setProject);
        }
        if (packType != null) {
            pack.setPackType(EvidencePack.PackType.valueOf(packType));
        }
        if (createdBy != null) {
            userRepository.findById(createdBy).ifPresent(pack::setCreatedBy);
        }
        return packRepository.save(pack);
    }

    @Override
    public EvidencePack finalize(String packId) {
        EvidencePack pack = packRepository.findById(packId)
                .orElseThrow(() -> new ResourceNotFoundException("Evidence pack not found: " + packId));
        if (pack.getStatus() != EvidencePack.Status.DRAFT) {
            throw new BadRequestException("Can only finalize a DRAFT pack");
        }
        pack.setStatus(EvidencePack.Status.FINALIZED);
        pack.setFinalizedAt(Instant.now());
        return packRepository.save(pack);
    }

    @Override
    public List<EvidencePack> listByProject(String projectId) {
        return packRepository.findByProjectId(projectId);
    }

    @Override
    public EvidencePackItem addAssetItem(String packId, String assetId, int order) {
        EvidencePack pack = packRepository.findById(packId)
                .orElseThrow(() -> new ResourceNotFoundException("Evidence pack not found: " + packId));
        if (pack.getStatus() != EvidencePack.Status.DRAFT) {
            throw new BadRequestException("Cannot modify a finalized pack");
        }

        EvidencePackItem item = new EvidencePackItem();
        item.setPack(pack);
        assetRepository.findById(assetId).ifPresent(item::setAsset);
        item.setItemOrder(order);
        return itemRepository.save(item);
    }

    @Override
    public EvidencePackItem addNoteItem(String packId, String noteId, int order) {
        EvidencePack pack = packRepository.findById(packId)
                .orElseThrow(() -> new ResourceNotFoundException("Evidence pack not found: " + packId));
        if (pack.getStatus() != EvidencePack.Status.DRAFT) {
            throw new BadRequestException("Cannot modify a finalized pack");
        }

        EvidencePackItem item = new EvidencePackItem();
        item.setPack(pack);
        noteRepository.findById(noteId).ifPresent(item::setNote);
        item.setItemOrder(order);
        return itemRepository.save(item);
    }

    @Override
    public List<EvidencePackItem> listItems(String packId) {
        return itemRepository.findByPackIdOrderByItemOrderAsc(packId);
    }

    @Override
    public void removeItem(String itemId) {
        itemRepository.deleteById(itemId);
    }

    @Override
    public void delete(String packId) {
        packRepository.deleteById(packId);
    }
}
