package org.techm.samples.entity;

import java.time.LocalDateTime;
import java.util.List;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonManagedReference;

import jakarta.persistence.CascadeType;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.OneToMany;
import jakarta.persistence.PrePersist;
import jakarta.persistence.PreUpdate;
import jakarta.persistence.Table;

@Entity
@Table(name="users")
public class User {
	@Id
	@GeneratedValue(strategy = GenerationType.IDENTITY)
	private Long id;
	
	private String username;
	private String password;
	private String email;
	private LocalDateTime created_at;
	private LocalDateTime updated_at;
	
	@OneToMany(mappedBy = "user",cascade = CascadeType.ALL,orphanRemoval = true)
	@JsonManagedReference("reviews-user")
    @JsonIgnore
	private List<Reviews> reviews;
	
	@OneToMany(mappedBy = "user",cascade = CascadeType.ALL,orphanRemoval = true)
	@JsonManagedReference("wishlist-user")
    @JsonIgnore
	private List<Wishlist_items> wishlist_items;
	
	@Enumerated(EnumType.STRING)
	private Role role;
	
	public User() {
		super();
	}

	public User(String username, String email, LocalDateTime created_at, LocalDateTime updated_at, Role role) {
		super();
		this.username = username;
		this.email = email;
		this.created_at = created_at;
		this.updated_at = updated_at;
		this.role = role;
	}

	public String getEmail() {
		return email;
	}

	public void setEmail(String email) {
		this.email = email;
	}

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

	public String getUsername() {
		return username;
	}

	public void setUsername(String username) {
		this.username = username;
	}

	public String getPassword() {
		return password;
	}

	public void setPassword(String password) {
		this.password = password;
	}

	public Role getRole() {
		return role;
	}

	public void setRole(Role role) {
		this.role = role;
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

	public List<Reviews> getReviews() {
		return reviews;
	}

	public void setReviews(List<Reviews> reviews) {
		this.reviews = reviews;
	}

	public List<Wishlist_items> getWishlist_items() {
		return wishlist_items;
	}

	public void setWishlist_items(List<Wishlist_items> wishlist_items) {
		this.wishlist_items = wishlist_items;
	}

	@Override
	public String toString() {
		return "User [username=" + username + ", role=" + role + "]";
	}
	
}
