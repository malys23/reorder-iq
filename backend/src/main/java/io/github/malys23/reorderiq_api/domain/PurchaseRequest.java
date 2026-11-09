package io.github.malys23.reorderiq_api.domain;

import jakarta.persistence.*;

import java.time.LocalDate;

@Entity
@Table(name="purchase_requests")
public class PurchaseRequest {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Integer requestId;

    private Integer itemId;

    private Integer quantity;

    private LocalDate neededBy;

    private LocalDate createdAt;

    private Integer requestedBy;

    private String status;

    private LocalDate submittedAt;

    private LocalDate approvedAt;

    private Integer approvedBy;

    private LocalDate orderedAt;

    private LocalDate receivedAt;

    private Integer receivedBy;

    protected PurchaseRequest(){}

    public PurchaseRequest(Integer itemId, Integer quantity, LocalDate neededBy, LocalDate createdAt, Integer requestedBy, String status, LocalDate submittedAt, LocalDate approvedAt, Integer approvedBy, LocalDate orderedAt, LocalDate receivedAt, Integer receivedBy) {
        this.itemId = itemId;
        this.quantity = quantity;
        this.neededBy = neededBy;
        this.createdAt = createdAt;
        this.requestedBy = requestedBy;
        this.status = status;
        this.submittedAt = submittedAt;
        this.approvedAt = approvedAt;
        this.approvedBy = approvedBy;
        this.orderedAt = orderedAt;
        this.receivedAt = receivedAt;
        this.receivedBy = receivedBy;
    }

    public Integer getRequestId() {
        return requestId;
    }

    public Integer getItemId() {
        return itemId;
    }

    public Integer getQuantity() {
        return quantity;
    }

    public LocalDate getNeededBy() {
        return neededBy;
    }

    public LocalDate getCreatedAt() {
        return createdAt;
    }

    public Integer getRequestedBy() {
        return requestedBy;
    }

    public String getStatus() {
        return status;
    }

    public LocalDate getSubmittedAt() {
        return submittedAt;
    }

    public LocalDate getApprovedAt() {
        return approvedAt;
    }

    public Integer getApprovedBy() {
        return approvedBy;
    }

    public LocalDate getOrderedAt() {
        return orderedAt;
    }

    public LocalDate getReceivedAt() {
        return receivedAt;
    }

    public Integer getReceivedBy() {
        return receivedBy;
    }
}
