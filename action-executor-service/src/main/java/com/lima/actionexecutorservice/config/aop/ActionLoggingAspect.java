package com.lima.actionexecutorservice.config.aop;

import com.lima.actionexecutorservice.domain.action.model.dto.AnomalyEventDto;
import lombok.extern.slf4j.Slf4j;
import org.aspectj.lang.ProceedingJoinPoint;
import org.aspectj.lang.annotation.Around;
import org.aspectj.lang.annotation.Aspect;
import org.springframework.stereotype.Component;

@Aspect
@Component
@Slf4j
public class ActionLoggingAspect {

    @Around("execution(* com.lima.actionexecutorservice.domain.action.service.ActionExecutorService.executeAction(..))")
    public Object logActionExecution(ProceedingJoinPoint joinPoint) throws Throwable {
        Object[] args = joinPoint.getArgs();
        AnomalyEventDto anomaly = (AnomalyEventDto) args[0];

        log.info("[ACTION START] anomalyType={}, region={}, severity={}",
                anomaly.getAnomalyType(), anomaly.getRegion(), anomaly.getSeverity());

        long startTime = System.currentTimeMillis();
        try {
            Object result = joinPoint.proceed();
            long elapsed = System.currentTimeMillis() - startTime;
            log.info("[ACTION COMPLETE] anomalyType={}, region={}, elapsed={}ms",
                    anomaly.getAnomalyType(), anomaly.getRegion(), elapsed);
            return result;
        } catch (Exception e) {
            long elapsed = System.currentTimeMillis() - startTime;
            log.error("[ACTION FAILED] anomalyType={}, region={}, elapsed={}ms, error={}",
                    anomaly.getAnomalyType(), anomaly.getRegion(), elapsed, e.getMessage());
            throw e;
        }
    }
}
