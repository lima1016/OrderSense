package com.lima.restaurantservice.domain.restaurant.job;

import com.lima.restaurantservice.domain.restaurant.model.entity.Restaurant;
import com.lima.restaurantservice.domain.restaurant.model.type.RestaurantStatus;
import com.lima.restaurantservice.domain.restaurant.repository.RestaurantRepository;
import lombok.extern.slf4j.Slf4j;
import org.quartz.JobExecutionContext;
import org.quartz.JobExecutionException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.scheduling.quartz.QuartzJobBean;
import org.springframework.stereotype.Component;

import java.util.List;

@Slf4j
@Component
public class DailyStatusCheckJob extends QuartzJobBean {

    @Autowired
    private RestaurantRepository restaurantRepository;

    @Override
    protected void executeInternal(JobExecutionContext context) throws JobExecutionException {
        log.info("=== 가맹점 일일 상태 점검 시작 ===");

        List<Restaurant> allRestaurants = restaurantRepository.findAll();
        long openCount = allRestaurants.stream()
                .filter(r -> r.getStatus() == RestaurantStatus.OPEN)
                .count();
        long closedCount = allRestaurants.stream()
                .filter(r -> r.getStatus() == RestaurantStatus.CLOSED)
                .count();
        long suspendedCount = allRestaurants.stream()
                .filter(r -> r.getStatus() == RestaurantStatus.SUSPENDED)
                .count();

        log.info("전체 가맹점: {}개, 영업중: {}개, 영업종료: {}개, 일시정지: {}개",
                allRestaurants.size(), openCount, closedCount, suspendedCount);

        log.info("=== 가맹점 일일 상태 점검 완료 ===");
    }
}
