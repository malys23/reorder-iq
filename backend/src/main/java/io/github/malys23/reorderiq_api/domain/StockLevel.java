package io.github.malys23.reorderiq_api.domain;

import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.Table;

@Entity
@Table(name="stock_levels")
public class StockLevel {
    @Id
    private Integer itemId;

    private Integer onHand;

    private Integer onOrder;

    protected StockLevel(){}

    public StockLevel(Integer itemId, Integer onHand, Integer onOrder){
        this.itemId = itemId;
        this.onHand = onHand;
        this.onOrder = onOrder;
    }

    public Integer getItemId() {
        return itemId;
    }

    public Integer getOnHand() {
        return onHand;
    }

    public Integer getOnOrder() {
        return onOrder;
    }
}
