package org.techm.samples.entity;

import java.time.LocalDateTime;

import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.PrePersist;

@Entity
public class Wishlist_items {
	@Id
	@GeneratedValue(strategy = GenerationType.IDENTITY)
	private long Id;
	
	@ManyToOne(optional = false)
	@JoinColumn(name="user_id")
	private User user;
	
	@ManyToOne(optional = false)
	@JoinColumn(name = "product_id")
	private Products product;
	
	private LocalDateTime created_at;
	
	@PrePersist
	protected void onCreate() {
		created_at=LocalDateTime.now();
	}
	
	public long getId() {
		return Id;
	}
	public void setId(long id) {
		Id = id;
	}
	public User getUser() {
		return user;
	}
	public void setUser(User user) {
		this.user = user;
	}
	public Products getProduct() {
		return product;
	}
	public void setProduct(Products product) {
		this.product = product;
	}
	
	public LocalDateTime getCreated_at() {
		return created_at;
	}

	public void setCreated_at(LocalDateTime created_at) {
		this.created_at = created_at;
	}

	public Wishlist_items(User user, Products product) {
		super();
		this.user = user;
		this.product = product;
	}
	public Wishlist_items() {
		super();
		
	}

	public void setUserId(long l) {
		this.user.setId(l);
		
	}

	public void setProductId(long l) {
		this.product.setId(l);
		
	}
	
	
	
}
