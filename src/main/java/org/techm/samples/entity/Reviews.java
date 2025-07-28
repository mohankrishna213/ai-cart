package org.techm.samples.entity;

import java.time.LocalDateTime;

import com.fasterxml.jackson.annotation.JsonBackReference;

import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.PrePersist;
import jakarta.persistence.PreUpdate;

@Entity
public class Reviews {
	
	@Id
	@GeneratedValue(strategy=GenerationType.IDENTITY)
	private Long id;
	@ManyToOne(optional = false)
	@JoinColumn(name = "product_id")
	@JsonBackReference("reviews-product")
	private Products product;
	
	@ManyToOne(optional = false)
	@JoinColumn(name = "user_id")
	@JsonBackReference("reviews-user")
	private User user;
	
	private String title;
	private String content;
	private double rating;
	private LocalDateTime created_at;
	private LocalDateTime updated_at;
	
	@PrePersist
	protected void onCreate() {
		created_at=updated_at=LocalDateTime.now();
	}
	
	@PreUpdate
	protected void onUpdate() {
		updated_at=LocalDateTime.now();
	}
	
	public Long getId() {
		return id;
	}
	public void setId(Long id) {
		this.id = id;
	}
	public User getUser() {
		return user;
	}
	public void setUser(User user) {
		this.user = user;
	}
	public String getTitle() {
		return title;
	}
	public void setTitle(String title) {
		this.title = title;
	}
	public String getContent() {
		return content;
	}
	public void setContent(String content) {
		this.content = content;
	}
	public double getRating() {
		return rating;
	}
	public void setRating(double rating) {
		this.rating = rating;
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
	
	public Products getProduct() {
		return product;
	}
	public void setProduct(Products product) {
		this.product = product;
	}
	public Reviews(Products product, User user, String title, String content, double rating,
			LocalDateTime created_at, LocalDateTime updated_at) {
		super();
		
		this.product = product;
		this.user = user;
		this.title = title;
		this.content = content;
		this.rating = rating;
		this.created_at = created_at;
		this.updated_at = updated_at;
	}
	public Reviews() {
		super();
	}

	public void setProductId(long l) {
		this.product.setId(l);
	}

	public void setUserId(long l) {
		this.user.setId(l);
		
	}
	
	
	
}
