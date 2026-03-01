package com.lima.actionexecutorservice.domain.action.model.entity;

import com.lima.actionexecutorservice.domain.action.model.type.ActionResult;
import jakarta.persistence.*;
import lombok.*;
import org.springframework.data.annotation.CreatedDate;
import org.springframework.data.jpa.domain.support.AuditingEntityListener;

import java.time.LocalDateTime;

@Entity
@Table(name = "tb_action_logs")
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@AllArgsConstructor
@Builder
@EntityListeners(AuditingEntityListener.class)
public class ActionLog {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false, length = 50)
    private String anomalyType;

    @Column(length = 50)
    private String region;

    @Column(nullable = false, columnDefinition = "TEXT")
    private String action;

    @Column(columnDefinition = "TEXT")
    private String result;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 20)
    private ActionResult actionResult;

    @Column(nullable = false)
    private LocalDateTime executedAt;

    @CreatedDate
    @Column(nullable = false, updatable = false)
    private LocalDateTime createdAt;
}
