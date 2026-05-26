package com.hrms.enums;

public enum SettlementStatus {
	PENDING("Pending", "Overtime not yet settled"), SETTLED("Settled", "Overtime payment settled");

	private final String displayName;
	private final String description;

	SettlementStatus(String displayName, String description) {
		this.displayName = displayName;
		this.description = description;
	}

	public String getDisplayName() {
		return displayName;
	}

	public String getDescription() {
		return description;
	}
}
