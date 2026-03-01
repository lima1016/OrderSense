package com.lima.actionexecutorservice.domain.action.model.dto;

import com.lima.actionexecutorservice.domain.action.model.entity.ActionLog;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Getter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class ActionEventDto {

    private String eventType;
    private Long actionLogId;
    private String anomalyType;
    private String region;
    private String action;
    private String result;
    private LocalDateTime timestamp;

    public static ActionEventDto from(ActionLog actionLog) {
        return ActionEventDto.builder()
                .eventType("ACTION_EXECUTED")
                .actionLogId(actionLog.getId())
                .anomalyType(actionLog.getAnomalyType())
                .region(actionLog.getRegion())
                .action(actionLog.getAction())
                .result(actionLog.getResult())
                .timestamp(actionLog.getExecutedAt())
                .build();
    }
}
