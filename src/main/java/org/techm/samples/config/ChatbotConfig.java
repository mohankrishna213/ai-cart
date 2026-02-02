package org.techm.samples.config;

import org.springframework.ai.chat.model.ChatModel;
import org.springframework.ai.document.Document;
import org.springframework.ai.embedding.EmbeddingModel;
import org.springframework.ai.ollama.OllamaChatModel;
import org.springframework.ai.ollama.OllamaEmbeddingModel;
import org.springframework.ai.ollama.api.OllamaApi;
import org.springframework.ai.ollama.api.OllamaOptions;
import org.springframework.ai.vectorstore.SimpleVectorStore;
import org.springframework.ai.vectorstore.VectorStore;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.boot.CommandLineRunner;
import org.springframework.beans.factory.annotation.Autowired;
import org.techm.samples.service.chatbot.ProductDocumentLoader;

import java.util.List;

@Configuration
public class ChatbotConfig {

    @Value("${spring.ai.ollama.base-url}")
    private String ollamaBaseUrl;

    @Value("${spring.ai.ollama.chat.model}")
    private String chatModel;

    @Value("${spring.ai.ollama.embedding.model}")
    private String embeddingModel;

    @Bean
    public OllamaApi ollamaApi() {
        return new OllamaApi(ollamaBaseUrl);
    }

    @Bean
    public ChatModel chatClient(OllamaApi ollamaApi) {
        return new OllamaChatModel(ollamaApi, OllamaOptions.create()
                .withModel(chatModel)
                .withTemperature(0.7));
    }

    @Bean
    public EmbeddingModel embeddingClient(OllamaApi ollamaApi) {
        return new OllamaEmbeddingModel(ollamaApi, OllamaOptions.create()
                .withModel(embeddingModel));
    }

    @Bean
    public VectorStore vectorStore(EmbeddingModel embeddingClient) {
        return new SimpleVectorStore(embeddingClient);
    }

    @Bean
    public CommandLineRunner initializeVectorStore(
            VectorStore vectorStore,
            @Autowired ProductDocumentLoader documentLoader) {
        return args -> {
            // Load product documents into vector store on startup
            List<Document> documents = documentLoader.loadProductDocuments();
            if (!documents.isEmpty()) {
                vectorStore.add(documents);
                System.out.println("✅ Loaded " + documents.size() + " documents into vector store");
            }
        };
    }
}