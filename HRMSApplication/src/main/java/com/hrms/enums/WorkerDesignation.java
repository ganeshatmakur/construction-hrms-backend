package com.hrms.enums;

public enum WorkerDesignation {
	MASON("Mason", "Skilled masonry work"), ELECTRICIAN("Electrician", "Electrical installation and maintenance"),
	PLUMBER("Plumber", "Plumbing work"), SUPERVISOR("Supervisor", "Site supervisor and team lead"),
	HELPER("Helper", "General site helper and labor");

	private final String displayName;
	private final String description;

	WorkerDesignation(String displayName, String description) {
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
