package com.hrms.listener;

import com.hrms.event.SettlementCompletedEvent;
import com.hrms.service.NotificationService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;
import org.springframework.transaction.event.TransactionalEventListener;
import org.springframework.transaction.event.TransactionPhase;

@Slf4j
@Component
@RequiredArgsConstructor
public class SettlementNotificationListener {

	private final NotificationService notificationService;

	@TransactionalEventListener(phase = TransactionPhase.AFTER_COMMIT)
	public void onSettlementCompleted(SettlementCompletedEvent event) {
		log.info("[v0] Settlement notification triggered: worker={}, month={}", event.getWorkerId(),
				event.getYearMonth());

		try {
			// Send SMS notification
			notificationService.sendSettlementSMS(event.getPhoneNumber(), event.getWorkerName(), event.getYearMonth(),
					event.getTotalAmount());

			log.info("[v0] Settlement notifications sent: worker={}", event.getWorkerId());
		} catch (Exception e) {
			log.error("[v0] Failed to send settlement notification: {}", e.getMessage(), e);
			// Don't re-throw - notification failure should not affect API response
			// Consider logging to error queue for manual intervention
		}
	}
}
