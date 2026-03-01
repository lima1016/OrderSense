package com.lima.riderservice.domain.rider.controller;

import com.lima.riderservice.domain.rider.model.dto.RiderLocationUpdate;
import com.lima.riderservice.domain.rider.model.dto.RiderResponse;
import com.lima.riderservice.domain.rider.service.RiderService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.messaging.handler.annotation.MessageMapping;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.stereotype.Controller;

@Controller
@RequiredArgsConstructor
@Slf4j
public class RiderLocationWebSocketController {

    private final RiderService riderService;
    private final SimpMessagingTemplate messagingTemplate;

    @MessageMapping("/rider/location")
    public void updateLocation(RiderLocationUpdate request) {
        log.debug("WebSocket 위치 업데이트 수신: riderId={}, lat={}, lng={}",
                request.getRiderId(), request.getLat(), request.getLng());

        RiderResponse response = riderService.updateLocation(request.getRiderId(), request);

        messagingTemplate.convertAndSend(
                "/topic/rider/" + request.getRiderId(),
                response
        );
    }
}
