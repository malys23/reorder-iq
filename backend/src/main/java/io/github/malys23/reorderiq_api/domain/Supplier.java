package io.github.malys23.reorderiq_api.domain;

import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.Table;

import java.math.BigDecimal;

@Entity
@Table(name = "suppliers")
public class Supplier {
    @Id
    private Integer supplierId;

    private String name;

    private String category;

    private BigDecimal leadTimeCv;

    private BigDecimal onTimeRate;

    protected Supplier(){}

    public Supplier(Integer supplierId, String name, String category, BigDecimal leadTimeCv, BigDecimal onTimeRate){
        this.supplierId = supplierId;
        this.name = name;
        this.category = category;
        this.leadTimeCv = leadTimeCv;
        this.onTimeRate = onTimeRate;
    }

    public Integer getSupplierId() {
        return supplierId;
    }

    public String getName() {
        return name;
    }

    public String getCategory() {
        return category;
    }

    public BigDecimal getLeadTimeCv() {
        return leadTimeCv;
    }

    public BigDecimal getOnTimeRate() {
        return onTimeRate;
    }
}
