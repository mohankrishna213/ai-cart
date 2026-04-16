package org.techm.samples.config;

import org.springframework.ai.document.Document;
import org.springframework.ai.embedding.EmbeddingModel;
import org.springframework.ai.google.genai.GoogleGenAiEmbeddingConnectionDetails;
import org.springframework.ai.google.genai.text.GoogleGenAiTextEmbeddingModel;
import org.springframework.ai.google.genai.text.GoogleGenAiTextEmbeddingOptions;
import org.springframework.ai.vectorstore.SimpleVectorStore;
import org.springframework.ai.vectorstore.VectorStore;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.CommandLineRunner;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.techm.samples.service.chatbot.ProductDocumentLoader;

import java.util.List;

@Configuration
public class ChatbotConfig {

    @Value("${spring.ai.google.genai.api-key}")
    private String apiKey;

    @Value("${spring.ai.google.genai.embedding.text.options.model:text-embedding-004}")
    private String embeddingModelName;

    /**
     * GoogleGenAiApi does NOT exist in Spring AI's Google GenAI module.
     * The embedding module uses its own GoogleGenAiEmbeddingConnectionDetails
     * for authentication — completely separate from the chat module.
     *
     * Dependency required: spring-ai-google-genai-embedding
     */
    @Bean
    public EmbeddingModel embeddingModel() {
        GoogleGenAiEmbeddingConnectionDetails connectionDetails =
                GoogleGenAiEmbeddingConnectionDetails.builder()
                        .apiKey(apiKey)
                        .build();

        GoogleGenAiTextEmbeddingOptions options =
                GoogleGenAiTextEmbeddingOptions.builder()
                        .model(embeddingModelName)
                        .build();

        return new GoogleGenAiTextEmbeddingModel(connectionDetails, options);
    }

    /**
     * SimpleVectorStore(EmbeddingModel) constructor is protected in Spring AI 2.x.
     * Must use the static builder: SimpleVectorStore.builder(embeddingModel).build()
     */
    @Bean
    public VectorStore vectorStore(EmbeddingModel embeddingModel) {
        return SimpleVectorStore.builder(embeddingModel).build();
    }

    @Bean
    public CommandLineRunner initializeVectorStore(
            VectorStore vectorStore,
            @Autowired ProductDocumentLoader documentLoader) {
        return args -> {
            List<Document> documents = documentLoader.loadProductDocuments();
            if (!documents.isEmpty()) {
                vectorStore.add(documents);
                System.out.println("✅ Loaded " + documents.size() + " product documents into vector store (text-embedding-004)");
            }
        };
    }
}