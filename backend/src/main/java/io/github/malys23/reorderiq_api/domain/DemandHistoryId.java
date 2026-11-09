package io.github.malys23.reorderiq_api.domain;

import java.io.Serializable;
import java.time.LocalDate;
import java.util.Objects;

public class DemandHistoryId implements Serializable{
    private Integer itemId;
    private LocalDate weekStart;

    protected DemandHistoryId(){}

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        DemandHistoryId that = (DemandHistoryId) o;
        return Objects.equals(itemId, that.itemId) && Objects.equals(weekStart, that.weekStart);
    }

    @Override
    public int hashCode() {
        return Objects.hash(itemId, weekStart);
    }

    public DemandHistoryId(Integer itemId, LocalDate weekStart) {
        this.itemId = itemId;
        this.weekStart = weekStart;
    }
}
