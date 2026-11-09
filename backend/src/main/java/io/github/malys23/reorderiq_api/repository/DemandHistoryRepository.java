package io.github.malys23.reorderiq_api.repository;

import io.github.malys23.reorderiq_api.domain.DemandHistory;
import io.github.malys23.reorderiq_api.domain.DemandHistoryId;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface DemandHistoryRepository extends JpaRepository<DemandHistory, DemandHistoryId> {
    List<DemandHistory> findByItemIdOrderByWeekStartDesc(Integer itemId, Pageable pageable);
}