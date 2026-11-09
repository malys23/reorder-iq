package io.github.malys23.reorderiq_api.repository;

import io.github.malys23.reorderiq_api.domain.PurchaseRequest;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface PurchaseRequestRepository extends JpaRepository<PurchaseRequest, Integer> {
}
