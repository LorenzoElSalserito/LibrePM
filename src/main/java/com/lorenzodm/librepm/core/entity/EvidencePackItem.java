package com.lorenzodm.librepm.core.entity;

import jakarta.persistence.*;

import java.util.UUID;

@Entity
@Table(name = "evidence_pack_items")
public class EvidencePackItem {

    @Id
    @Column(length = 36)
    private String id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "pack_id", nullable = false)
    private EvidencePack pack;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "asset_id")
    private Asset asset;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "note_id")
    private Note note;

    @Column(name = "item_order")
    private int itemOrder;

    public EvidencePackItem() {
        this.id = UUID.randomUUID().toString();
    }

    // Getters and Setters
    public String getId() { return id; }
    public void setId(String id) { this.id = id; }
    public EvidencePack getPack() { return pack; }
    public void setPack(EvidencePack pack) { this.pack = pack; }
    public Asset getAsset() { return asset; }
    public void setAsset(Asset asset) { this.asset = asset; }
    public Note getNote() { return note; }
    public void setNote(Note note) { this.note = note; }
    public int getItemOrder() { return itemOrder; }
    public void setItemOrder(int itemOrder) { this.itemOrder = itemOrder; }
}
