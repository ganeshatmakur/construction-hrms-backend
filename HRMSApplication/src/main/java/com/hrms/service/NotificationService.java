package com.hrms.service;

import java.math.BigDecimal;

public interface NotificationService {

	void sendSettlementSMS(String phoneNumber, String workerName, String yearMonth, BigDecimal totalAmount);

	void sendSettlementEmail(String email, String workerName, String yearMonth, BigDecimal totalAmount);
}