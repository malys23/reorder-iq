package io.github.malys23.reorderiq_api.domain;

import jakarta.persistence.*;

import java.math.BigDecimal;

@Entity
@Table(name = "items")
public class Item {
    @Id
    private Integer itemId;

    private String name;

    private String category;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "supplier_id")
    private Supplier supplier;

    private BigDecimal unitCost;

    private Integer leadTimeDays;

    private String abcClass;

    protected Item() {}

    public Item(Integer itemId, String name, String category, Supplier supplier, BigDecimal unitCost, Integer leadTimeDays, String abcClass){
        this.itemId = itemId;
        this.name = name;
        this.category = category;
        this.supplier = supplier;
        this.unitCost = unitCost;
        this.leadTimeDays = leadTimeDays;
        this.abcClass = abcClass;
    }

    public Integer getItemId(){
        return itemId;
    }

    public String getName() {
        return name;
    }

    public String getCategory() {
        return category;
    }

    public Supplier getSupplier() {
        return supplier;
    }

    public BigDecimal getUnitCost(){
        return unitCost;
    }

    public Integer getLeadTimeDays() {
        return leadTimeDays;
    }

    public String getAbcClass() {
        return abcClass;
    }
}
