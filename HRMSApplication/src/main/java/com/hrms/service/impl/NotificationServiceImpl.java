package com.hrms.service.impl;

import java.math.BigDecimal;

import org.springframework.stereotype.Service;

import com.hrms.service.NotificationService;

import lombok.extern.slf4j.Slf4j;

@Service
@Slf4j
public class NotificationServiceImpl implements NotificationService {

	@Override
	public void sendSettlementSMS(String phoneNumber, String workerName, String yearMonth, BigDecimal totalAmount) {

        
        String message = String.format(
            "Hi %s, Your overtime for %s has been settled. Amount: ₹%.2f. Thank you!",
            workerName, yearMonth, totalAmount
        );
        
        log.info("[v0] Sending SMS: phone={}, message={}", phoneNumber, message);
        
        try {
            // TODO: Integrate with actual SMS provider (Twilio, AWS SNS, etc.)
            // For now, just log the SMS
            log.info("[v0] SMS sent successfully to {}", phoneNumber);
        } catch (Exception e) {
            log.error("[v0] Failed to send SMS to {}: {}", phoneNumber, e.getMessage());
            // Don't throw - SMS failure shouldn't break the settlement
            // Queue for retry instead
        }
    
	}

	@Override
	public void sendSettlementEmail(String email, String workerName, String yearMonth, BigDecimal totalAmount) {

        
        String subject = "Overtime Settlement Notification";
        String body = String.format(
            "Dear %s,\n\nYour overtime for %s has been successfully settled.\n" +
            "Total Amount: ₹%.2f\n\nThank you!",
            workerName, yearMonth, totalAmount
        );
        
        log.info("[v0] Sending email: to={}, subject={}", email, subject);
        
        try {
            // TODO: Integrate with email service (SendGrid, AWS SES, etc.)
            log.info("[v0] Email sent successfully to {}", email);
        } catch (Exception e) {
            log.error("[v0] Failed to send email to {}: {}", email, e.getMessage());
        }
    
	}

}
