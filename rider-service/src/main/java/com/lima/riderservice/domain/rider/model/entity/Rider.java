package com.lima.riderservice.domain.rider.model.entity;

import com.lima.riderservice.domain.rider.model.type.RiderStatus;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EntityListeners;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import org.springframework.data.annotation.CreatedDate;
import org.springframework.data.annotation.LastModifiedDate;
import org.springframework.data.jpa.domain.support.AuditingEntityListener;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Entity
@Table(name = "tb_riders")
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@AllArgsConstructor
@Builder
@EntityListeners(AuditingEntityListener.class)
public class Rider {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false, length = 100)
    private String name;

    @Column(length = 20)
    private String phone;

    @Column(precision = 10, scale = 8)
    private BigDecimal currentLat;

    @Column(precision = 11, scale = 8)
    private BigDecimal currentLng;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 20)
    private RiderStatus status;

    @Column(length = 50)
    private String currentRegion;

    @CreatedDate
    @Column(nullable = false, updatable = false)
    private LocalDateTime createdAt;

    @LastModifiedDate
    private LocalDateTime updatedAt;

    // 비즈니스 메서드
    public void updateLocation(BigDecimal lat, BigDecimal lng, String region) {
        this.currentLat = lat;
        this.currentLng = lng;
        this.currentRegion = region;
    }

    public void changeStatus(RiderStatus newStatus) {
        this.status = newStatus;
    }
}
