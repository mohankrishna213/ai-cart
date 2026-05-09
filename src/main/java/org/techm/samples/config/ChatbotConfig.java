package org.techm.samples.config;

import org.springframework.ai.document.Document;
import org.springframework.ai.embedding.EmbeddingModel;
import org.springframework.ai.google.genai.GoogleGenAiEmbeddingConnectionDetails;
import org.springframework.ai.google.genai.text.GoogleGenAiTextEmbeddingModel;
import org.springframework.ai.google.genai.text.GoogleGenAiTextEmbeddingOptions;
import org.springframework.ai.vectorstore.SearchRequest;
import org.springframework.ai.vectorstore.VectorStore;
import org.springframework.ai.vectorstore.pinecone.PineconeVectorStore;
import org.springframework.beans.factory.ObjectProvider;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Lazy;
import org.springframework.context.event.EventListener;
import org.springframework.boot.context.event.ApplicationReadyEvent;
import org.techm.samples.service.chatbot.ProductDocumentLoader;

import java.util.List;
import java.util.concurrent.CompletableFuture;

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

    @Autowired
    private ObjectProvider<VectorStore> vectorStoreProvider;

    @Autowired
    private ObjectProvider<ProductDocumentLoader> documentLoaderProvider;

    @Bean
    @Lazy
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
    @Lazy
    public VectorStore vectorStore(EmbeddingModel embeddingModel) {
        return PineconeVectorStore.builder(embeddingModel)
                .apiKey(pineconeApiKey)
                .indexName(pineconeIndexName)
                .namespace(pineconeNamespace)
                .build();
    }

    @EventListener(ApplicationReadyEvent.class)
    public void initializeVectorStoreAsync(ApplicationReadyEvent ignored) {
        CompletableFuture.runAsync(() -> {
            VectorStore vectorStore = vectorStoreProvider.getIfAvailable();
            ProductDocumentLoader documentLoader = documentLoaderProvider.getIfAvailable();

            if (vectorStore == null || documentLoader == null) {
                System.err.println("⚠️  Vector store or document loader not available for async initialization.");
                return;
            }

            try {
                List<Document> probe = vectorStore.similaritySearch(
                        SearchRequest.builder()
                                .query("product")
                                .topK(1)
                                .similarityThreshold(0.0)
                                .build()
                );

                if (!probe.isEmpty()) {
                    System.out.println("⏭️  Pinecone already has vectors — skipping embedding & upsert.");
                    return;
                }

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
                System.err.println("❌ Error during async vector store initialization: " + e.getMessage());
                e.printStackTrace();
            }
        });
    }
}