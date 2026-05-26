package com.hrms.entity;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import java.time.LocalDateTime;

@Entity
@Table(name = "sites", indexes = { @Index(name = "idx_site_name", columnList = "name"),
		@Index(name = "idx_site_active", columnList = "is_active") })
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Site {

	@Id
	@GeneratedValue(strategy = GenerationType.IDENTITY)
	private Long id;

	@Column(nullable = false, length = 150)
	private String name;

	@Column(nullable = false, length = 255)
	private String location;

	@Column(length = 500)
	private String description;

	@Column(nullable = false)
	private Boolean isActive = true;

	@Column(nullable = false, updatable = false)
	private LocalDateTime createdAt = LocalDateTime.now();

	@Column(nullable = false)
	private LocalDateTime updatedAt = LocalDateTime.now();

	public void deactivate() {
		this.isActive = false;
		this.updatedAt = LocalDateTime.now();
	}

	public boolean isActiveAndValid() {
		return this.isActive != null && this.isActive;
	}
}
