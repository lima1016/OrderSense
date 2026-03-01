package com.lima.riderservice.domain.rider.repository;

import com.lima.riderservice.domain.rider.model.entity.Rider;
import com.lima.riderservice.domain.rider.model.type.RiderStatus;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface RiderRepository extends JpaRepository<Rider, Long> {

    // 상태별 라이더 조회
    List<Rider> findByStatus(RiderStatus status);

    // 지역별 라이더 조회
    List<Rider> findByCurrentRegion(String currentRegion);

    // 지역 + 상태별 라이더 조회
    List<Rider> findByCurrentRegionAndStatus(String currentRegion, RiderStatus status);

    // 지역별 배달 가능 라이더 수 조회
    @Query("SELECT COUNT(r) FROM Rider r WHERE r.currentRegion = :region AND r.status = 'AVAILABLE'")
    Long countAvailableRidersByRegion(@Param("region") String region);
}
