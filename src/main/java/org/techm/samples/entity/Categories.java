package org.techm.samples.entity;

import java.time.LocalDateTime;
import java.util.List;

import jakarta.persistence.CascadeType;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.OneToMany;
import jakarta.persistence.PrePersist;
import jakarta.persistence.PreUpdate;

@Entity
public class Categories {
	@Id
	@GeneratedValue(strategy = GenerationType.IDENTITY)
	private long Id;
	private String name;
	private String description;
	private LocalDateTime created_at;
	private LocalDateTime updated_at;
	
	@OneToMany(mappedBy = "category",cascade = CascadeType.ALL,orphanRemoval = true)
	private List<Products> products;
	
	@PrePersist
	protected void onCreate() {
		created_at=updated_at=LocalDateTime.now();
	}
	
	@PreUpdate
	protected void onUpdate() {
		updated_at=LocalDateTime.now();
	}
	
	public long getId() {
		return Id;
	}
	public void setId(long id) {
		Id = id;
	}
	public String getName() {
		return name;
	}
	public void setName(String name) {
		this.name = name;
	}
	public String getDescription() {
		return description;
	}
	public void setDescription(String description) {
		this.description = description;
	}
	public LocalDateTime getCreated_at() {
		return created_at;
	}
	public void setCreated_at(LocalDateTime created_at) {
		this.created_at = created_at;
	}
	public LocalDateTime getUpdated_at() {
		return updated_at;
	}
	public void setUpdated_at(LocalDateTime updated_at) {
		this.updated_at = updated_at;
	}
	public Categories(String name, String description, LocalDateTime created_at, LocalDateTime updated_at) {
		super();
		this.name = name;
		this.description = description;
		this.created_at = created_at;
		this.updated_at = updated_at;
	}
	public Categories() {
		super();
		
	}
		
}
