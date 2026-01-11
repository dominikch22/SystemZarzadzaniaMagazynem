package com.example.magazyn.scheduler;

import com.example.magazyn.service.AlertService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
@Slf4j
public class AlertScheduler {

    private final AlertService alertService;

    @Scheduled(cron = "0 0 3 * * *")
    public void scheduledAlertCheck() {
        log.info("Scheduler uruchamia sprawdzanie alertów...");
        alertService.checkAndGenerateAllAlerts();
        log.info("Scheduler zakończył sprawdzanie alertów.");
    }
}