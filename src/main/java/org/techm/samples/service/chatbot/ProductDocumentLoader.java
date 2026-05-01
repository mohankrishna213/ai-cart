package org.techm.samples.service.chatbot;

import org.springframework.ai.document.Document;
import org.springframework.ai.vectorstore.VectorStore;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.techm.samples.entity.Categories;
import org.techm.samples.entity.Products;
import org.techm.samples.entity.Reviews;
import org.techm.samples.repository.CategoriesRepository;
import org.techm.samples.repository.ProductsRepository;
import org.techm.samples.repository.ReviewsRepository;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Service
public class ProductDocumentLoader {

    @Autowired
    private ProductsRepository productsRepository;

    @Autowired
    private CategoriesRepository categoriesRepository;

    @Autowired
    private ReviewsRepository reviewsRepository;

    public List<Document> loadProductDocuments() {
        List<Document> documents = new ArrayList<>();

        List<Products> products = productsRepository.findAll();
        for (Products product : products) {
            documents.add(createProductDocument(product));
        }

        List<Categories> categories = categoriesRepository.findAll();
        for (Categories category : categories) {
            documents.add(createCategoryDocument(category));
        }

        return documents;
    }

    private Document createProductDocument(Products product) {
        Map<String, Object> metadata = new HashMap<>();
        metadata.put("id", product.getId());
        metadata.put("type", "product");
        metadata.put("name", product.getName());
        metadata.put("price", product.getPrice());
        metadata.put("available", product.isAvailable());
        metadata.put("stockQuantity", product.getStockQuantity());
        metadata.put("imageUrl", product.getImageUrl());

        if (product.getCategory() != null) {
            metadata.put("categoryId", product.getCategory().getId());
            metadata.put("categoryName", product.getCategory().getName());
        }

        List<Reviews> reviews = reviewsRepository.findAllByProduct(product);
        if (!reviews.isEmpty()) {
            double avgRating = reviews.stream()
                    .mapToDouble(Reviews::getRating)
                    .average()
                    .orElse(0.0);
            metadata.put("averageRating", avgRating);
            metadata.put("reviewCount", reviews.size());
        }

        StringBuilder content = new StringBuilder();
        content.append("Product: ").append(product.getName()).append("\n");
        content.append("Description: ").append(product.getDescription()).append("\n");
        content.append("Price: $").append(product.getPrice()).append("\n");
        content.append("Stock: ").append(product.getStockQuantity()).append(" units\n");
        content.append("Available: ").append(product.isAvailable() ? "Yes" : "No").append("\n");

        if (product.getCategory() != null) {
            content.append("Category: ").append(product.getCategory().getName()).append("\n");
        }

        if (!reviews.isEmpty()) {
            double avgRating = reviews.stream()
                    .mapToDouble(Reviews::getRating)
                    .average()
                    .orElse(0.0);
            content.append("Customer Reviews: ").append(reviews.size()).append(" reviews");
            content.append(" with average rating of ").append(String.format("%.1f", avgRating)).append(" stars\n");
        }

        // FIX: Use deterministic ID "product-{dbId}" so Pinecone upserts on re-add
        // instead of creating a new duplicate document on every app startup.
        String deterministicId = "product-" + product.getId();
        return new Document(deterministicId, content.toString(), metadata);
    }

    private Document createCategoryDocument(Categories category) {
        Map<String, Object> metadata = new HashMap<>();
        metadata.put("id", category.getId());
        metadata.put("type", "category");
        metadata.put("name", category.getName());

        List<Products> categoryProducts = productsRepository.findByCategoryId(category.getId());
        metadata.put("productCount", categoryProducts.size());

        StringBuilder content = new StringBuilder();
        content.append("Category: ").append(category.getName()).append("\n");
        content.append("Description: ").append(category.getDescription()).append("\n");
        content.append("Number of products: ").append(categoryProducts.size()).append("\n");

        if (!categoryProducts.isEmpty()) {
            content.append("Products in this category: ");
            content.append(categoryProducts.stream()
                    .limit(10)
                    .map(Products::getName)
                    .collect(Collectors.joining(", ")));
        }

        // FIX: deterministic ID for categories too
        String deterministicId = "category-" + category.getId();
        return new Document(deterministicId, content.toString(), metadata);
    }

    public void updateProductDocument(Products product, VectorStore vectorStore) {
        // With deterministic IDs, add() will upsert — no need to delete first
        Document updatedDoc = createProductDocument(product);
        vectorStore.add(List.of(updatedDoc));
        System.out.println("✅ Updated product document: " + product.getName());
    }

    public void deleteProductDocument(Long productId, VectorStore vectorStore) {
        try {
            // FIX: delete by the correct deterministic ID, not the raw DB numeric id
            vectorStore.delete(List.of("product-" + productId));
            System.out.println("✅ Deleted product document from vector store (ID: product-" + productId + ")");
        } catch (Exception e) {
            System.err.println("❌ Error deleting product document: " + e.getMessage());
        }
    }
}