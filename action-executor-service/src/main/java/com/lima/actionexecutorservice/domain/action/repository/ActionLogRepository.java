package com.lima.actionexecutorservice.domain.action.repository;

import com.lima.actionexecutorservice.domain.action.model.entity.ActionLog;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.List;

@Repository
public interface ActionLogRepository extends JpaRepository<ActionLog, Long> {

    List<ActionLog> findByAnomalyType(String anomalyType);

    List<ActionLog> findByRegion(String region);

    List<ActionLog> findByExecutedAtBetween(LocalDateTime start, LocalDateTime end);
}
