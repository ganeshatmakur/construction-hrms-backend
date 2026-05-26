package com.hrms.event;

import lombok.Getter;
import org.springframework.context.ApplicationEvent;

import java.math.BigDecimal;

@Getter
public class SettlementCompletedEvent extends ApplicationEvent {
	private final Long workerId;
	private final String workerName;
	private final String phoneNumber;
	private final String yearMonth;
	private final BigDecimal totalAmount;
	private final int entriesSettled;

	public SettlementCompletedEvent(Object source, Long workerId, String workerName, String phoneNumber,
			String yearMonth, BigDecimal totalAmount, int entriesSettled) {
		super(source);
		this.workerId = workerId;
		this.workerName = workerName;
		this.phoneNumber = phoneNumber;
		this.yearMonth = yearMonth;
		this.totalAmount = totalAmount;
		this.entriesSettled = entriesSettled;
	}

	public SettlementCompletedEvent(Long workerId, String workerName, String phoneNumber, String yearMonth,
			BigDecimal totalAmount, int entriesSettled) {
		this(new Object(), workerId, workerName, phoneNumber, yearMonth, totalAmount, entriesSettled);
	}
}
