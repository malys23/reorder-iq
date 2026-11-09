package io.github.malys23.reorderiq_api.domain;

import jakarta.persistence.*;

import java.math.BigDecimal;
import java.time.LocalDate;

@Entity
@Table(name="forecasts")
public class Forecast {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Integer forecastId;

    private Integer itemId;

    private LocalDate weekStart;

    private BigDecimal predictedQuantity;

    private String method;

    private LocalDate createdAt;

    protected Forecast(){}

    public Forecast(Integer itemId, LocalDate weekStart, BigDecimal predictedQuantity, String method, LocalDate createdAt) {
        this.itemId = itemId;
        this.weekStart = weekStart;
        this.predictedQuantity = predictedQuantity;
        this.method = method;
        this.createdAt = createdAt;
    }

    public Integer getForecastId() {
        return forecastId;
    }

    public Integer getItemId() {
        return itemId;
    }

    public LocalDate getWeekStart() {
        return weekStart;
    }

    public BigDecimal getPredictedQuantity() {
        return predictedQuantity;
    }

    public String getMethod() {
        return method;
    }

    public LocalDate getCreatedAt() {
        return createdAt;
    }
}
