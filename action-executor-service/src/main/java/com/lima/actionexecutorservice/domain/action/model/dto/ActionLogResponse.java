package com.lima.actionexecutorservice.domain.action.model.dto;

import com.lima.actionexecutorservice.domain.action.model.entity.ActionLog;
import com.lima.actionexecutorservice.domain.action.model.type.ActionResult;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Getter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class ActionLogResponse {

    private Long id;
    private String anomalyType;
    private String region;
    private String action;
    private String result;
    private ActionResult actionResult;
    private LocalDateTime executedAt;
    private LocalDateTime createdAt;

    public static ActionLogResponse from(ActionLog actionLog) {
        return ActionLogResponse.builder()
                .id(actionLog.getId())
                .anomalyType(actionLog.getAnomalyType())
                .region(actionLog.getRegion())
                .action(actionLog.getAction())
                .result(actionLog.getResult())
                .actionResult(actionLog.getActionResult())
                .executedAt(actionLog.getExecutedAt())
                .createdAt(actionLog.getCreatedAt())
                .build();
    }
}
