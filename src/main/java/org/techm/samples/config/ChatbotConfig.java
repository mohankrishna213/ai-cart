package org.techm.samples.config;

import org.springframework.ai.document.Document;
import org.springframework.ai.embedding.EmbeddingModel;
import org.springframework.ai.google.genai.GoogleGenAiEmbeddingConnectionDetails;
import org.springframework.ai.google.genai.text.GoogleGenAiTextEmbeddingModel;
import org.springframework.ai.google.genai.text.GoogleGenAiTextEmbeddingOptions;
import org.springframework.ai.vectorstore.SearchRequest;
import org.springframework.ai.vectorstore.VectorStore;
import org.springframework.ai.vectorstore.pinecone.PineconeVectorStore;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.CommandLineRunner;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.techm.samples.service.chatbot.ProductDocumentLoader;

import java.util.List;

@Configuration
public class ChatbotConfig {

    @Value("${spring.ai.google.genai.embedding.api-key}")
    private String embeddingApiKey;

    @Value("${spring.ai.google.genai.embedding.text.options.model:gemini-embedding-001}")
    private String embeddingModelName;

    @Value("${spring.ai.vectorstore.pinecone.api-key}")
    private String pineconeApiKey;

    @Value("${spring.ai.vectorstore.pinecone.index-name:product-catalog}")
    private String pineconeIndexName;

    @Value("${spring.ai.vectorstore.pinecone.namespace:}")
    private String pineconeNamespace;

    @Bean
    public EmbeddingModel embeddingModel() {
        GoogleGenAiEmbeddingConnectionDetails connectionDetails =
                GoogleGenAiEmbeddingConnectionDetails.builder()
                        .apiKey(embeddingApiKey)
                        .build();

        GoogleGenAiTextEmbeddingOptions options =
                GoogleGenAiTextEmbeddingOptions.builder()
                        .model(embeddingModelName)
                        .build();

        return new GoogleGenAiTextEmbeddingModel(connectionDetails, options);
    }

    @Bean
    public VectorStore vectorStore(EmbeddingModel embeddingModel) {
        return PineconeVectorStore.builder(embeddingModel)
                .apiKey(pineconeApiKey)
                .indexName(pineconeIndexName)
                .namespace(pineconeNamespace)
                .build();
    }

    @Bean
    public CommandLineRunner initializeVectorStore(
            VectorStore vectorStore,
            @Autowired ProductDocumentLoader documentLoader) {
        return args -> {
            try {
                // CHECK: probe Pinecone with a broad low-threshold search.
                // If even 1 result comes back, vectors already exist — skip embedding entirely.
                List<Document> probe = vectorStore.similaritySearch(
                        SearchRequest.builder()
                                .query("product")
                                .topK(1)
                                .similarityThreshold(0.0)  // 0.0 = accept any match
                                .build()
                );

                if (!probe.isEmpty()) {
                    System.out.println("⏭️  Pinecone already has vectors — skipping embedding & upsert.");
                    return;  // EXIT EARLY — embedding model never gets called
                }

                // Only reaches here if Pinecone index is empty (first run or after manual wipe)
                System.out.println("📭 Pinecone index is empty — loading documents...");
                List<Document> documents = documentLoader.loadProductDocuments();

                if (!documents.isEmpty()) {
                    vectorStore.add(documents);
                    System.out.println("✅ Loaded " + documents.size()
                            + " documents into Pinecone (index: " + pineconeIndexName + ")");
                } else {
                    System.out.println("⚠️  No documents found in database to load.");
                }

            } catch (Exception e) {
                System.err.println("❌ Error during vector store initialization: " + e.getMessage());
                e.printStackTrace();
                throw new RuntimeException("Failed to initialize Pinecone vector store", e);
            }
        };
    }
}