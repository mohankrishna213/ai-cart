package org.techm.samples.entity;

import java.time.LocalDateTime;
import java.util.List;

import jakarta.persistence.CascadeType;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.OneToMany;
import jakarta.persistence.PrePersist;
import jakarta.persistence.PreUpdate;

@Entity
public class Products {
	
	@Id
	@GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
	
    private String name;
    private String description;
    private double price;
    private boolean available=true;
    private Integer stockQuantity=0;
    private String imageUrl;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
    
    @ManyToOne(optional = false)
    @JoinColumn(name = "category_id")
    private Categories category;
    
    @OneToMany(mappedBy = "product",cascade = CascadeType.ALL,orphanRemoval = true)
    private List<Reviews> reviews;
    
    @OneToMany(mappedBy = "product",cascade = CascadeType.ALL,orphanRemoval = true)
    private List<Wishlist_items> wishlist_items;
    
    @PrePersist
    protected void onCreate() {
    	createdAt=updatedAt=LocalDateTime.now();
    }
    
    @PreUpdate
    protected void onUpdate() {
    	updatedAt=LocalDateTime.now();
    }
    
	public Long getId() {
		return id;
	}
	public void setId(Long id) {
		this.id = id;
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
	public double getPrice() {
		return price;
	}
	public void setPrice(double price) {
		this.price = price;
	}
	
	public Products( String name, String description, double price, boolean available, int stockQuantity,
			String imageUrl, LocalDateTime createdAt, LocalDateTime updatedAt) {
		super();
		
		this.name = name;
		this.description = description;
		this.price = price;
		this.available = available;
		this.stockQuantity = stockQuantity;
		this.imageUrl = imageUrl;
		this.createdAt = createdAt;
		this.updatedAt = updatedAt;
	}
	
	@Override
	public String toString() {
		return "Product [id=" + id + ", name=" + name + ", description=" + description + ", price=" + price
				+ ", available=" + available + ", stockQuantity=" + stockQuantity + ", imageUrl=" + imageUrl
				+ ", createdAt=" + createdAt + ", updatedAt=" + updatedAt + "]";
	}
	public boolean isAvailable() {
		return available;
	}
	public void setAvailable(boolean available) {
		this.available = available;
	}
	public int getStockQuantity() {
		return stockQuantity;
	}
	public String getImageUrl() {
		return imageUrl;
	}
	public void setImageUrl(String imageUrl) {
		this.imageUrl = imageUrl;
	}
	public LocalDateTime getCreatedAt() {
		return createdAt;
	}
	public void setCreatedAt(LocalDateTime createdAt) {
		this.createdAt = createdAt;
	}
	public LocalDateTime getUpdatedAt() {
		return updatedAt;
	}
	public void setUpdatedAt(LocalDateTime updatedAt) {
		this.updatedAt = updatedAt;
	}
	
	public Categories getCategory() {
		return category;
	}

	public void setCategory(Categories category) {
		this.category = category;
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

	public void setStockQuantity(Integer stockQuantity) {
		this.stockQuantity = stockQuantity;
	}

	public Products() {
		super();
	}
}
