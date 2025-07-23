package org.techm.samples.dto;

public class ProductsDTO {
    private Long id;
    private String name;
    private String description;
    private Double price;
    private Long categoryId;
    // Add other fields as needed

    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }
    public String getName() { return name; }
    public void setName(String name) { this.name = name; }
    public String getDescription() { return description; }
    public void setDescription(String description) { this.description = description; }
    public Double getPrice() { return price; }
    public void setPrice(Double price) { this.price = price; }
    public Long getCategoryId() { return categoryId; }
    public void setCategoryId(Long categoryId) { this.categoryId = categoryId; }
    // Add other getters/setters as needed
}
