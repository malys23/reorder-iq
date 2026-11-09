package io.github.malys23.reorderiq_api.domain;

import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.IdClass;
import jakarta.persistence.Table;

import java.time.LocalDate;

@Entity
@Table(name="demand_history")
@IdClass(DemandHistoryId.class)
public class DemandHistory {
    @Id
    private Integer itemId;

    @Id
    private LocalDate weekStart;

    private Integer quantity;

    private Boolean isHoldout;

    protected DemandHistory(){}

    public DemandHistory(Integer itemId, LocalDate weekStart, Integer quantity, Boolean isHoldout) {
        this.itemId = itemId;
        this.weekStart = weekStart;
        this.quantity = quantity;
        this.isHoldout = isHoldout;
    }

    public Integer getItemId() {
        return itemId;
    }

    public LocalDate getWeekStart() {
        return weekStart;
    }

    public Integer getQuantity() {
        return quantity;
    }

    public Boolean getHoldout() {
        return isHoldout;
    }
}
