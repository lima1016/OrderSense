package com.lima.restaurantservice.config.quartz;

import com.lima.restaurantservice.domain.restaurant.job.DailyStatusCheckJob;
import org.quartz.CronScheduleBuilder;
import org.quartz.JobBuilder;
import org.quartz.JobDetail;
import org.quartz.Trigger;
import org.quartz.TriggerBuilder;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class QuartzConfig {

    @Bean
    public JobDetail dailyStatusCheckJobDetail() {
        return JobBuilder.newJob(DailyStatusCheckJob.class)
                .withIdentity("dailyStatusCheckJob")
                .withDescription("매일 자정에 가맹점 상태를 점검하는 작업")
                .storeDurably()
                .build();
    }

    @Bean
    public Trigger dailyStatusCheckTrigger(JobDetail dailyStatusCheckJobDetail) {
        return TriggerBuilder.newTrigger()
                .forJob(dailyStatusCheckJobDetail)
                .withIdentity("dailyStatusCheckTrigger")
                .withDescription("매일 자정에 실행")
                .withSchedule(CronScheduleBuilder.cronSchedule("0 0 0 * * ?"))
                .build();
    }
}
