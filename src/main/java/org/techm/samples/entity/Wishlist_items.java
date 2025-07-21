package org.techm.samples.entity;

import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;

@Entity
public class Wishlist_items {
	@Id
	@GeneratedValue(strategy = GenerationType.IDENTITY)
	private long Id;
	private User user;
	private Product product;
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
	public Product getProduct() {
		return product;
	}
	public void setProduct(Product product) {
		this.product = product;
	}
	public Wishlist_items(User user, Product product) {
		super();
		this.user = user;
		this.product = product;
	}
	public Wishlist_items() {
		super();
		
	}
	
	
	
}
