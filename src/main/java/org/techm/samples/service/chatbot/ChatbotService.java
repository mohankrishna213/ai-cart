package org.techm.samples.service.chatbot;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.ai.chat.model.ChatModel;
import org.springframework.ai.chat.model.ChatResponse;
import org.springframework.ai.chat.messages.Message;
import org.springframework.ai.chat.messages.SystemMessage;
import org.springframework.ai.chat.messages.UserMessage;
import org.springframework.ai.chat.prompt.Prompt;
import org.springframework.ai.document.Document;
import org.springframework.ai.vectorstore.SearchRequest;
import org.springframework.ai.vectorstore.VectorStore;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Lazy;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.stereotype.Service;
import org.techm.samples.dto.ChatRequest;
import org.techm.samples.dto.ChatbotResponse;
import org.techm.samples.dto.ProductRecommendation;
import org.techm.samples.entity.Products;
import org.techm.samples.repository.ProductsRepository;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Service
public class ChatbotService {

    private static final Logger log = LoggerFactory.getLogger(ChatbotService.class);

    @Autowired
    @Lazy
    private ChatModel chatClient;

    @Autowired
    @Lazy
    private VectorStore vectorStore;

    @Autowired
    private ProductsRepository productsRepository;

    @Value("${chatbot.context.max-results:5}")
    private int maxResults;

    @Value("${chatbot.similarity.threshold:0.5}")
    private double similarityThreshold;

    private static final ObjectMapper objectMapper = new ObjectMapper();

    private static final String SYSTEM_PROMPT = """
            You are a helpful product catalog assistant.
            The system retrieves matching products and displays them visually to the user as cards.
            
            Your ONLY job is to provide a single, friendly introductory sentence to accompany the product cards.
            
            CRITICAL INSTRUCTIONS:
            1. You MUST output ONLY a valid, raw JSON object.
            2. DO NOT output any reasoning, chain-of-thought, bullet points, checklists, or markdown.
            3. The JSON object must contain exactly one key named "message".
            
            EXAMPLE OUTPUT:
            {"message": "Here are some ergonomic products that can help relieve your back pain:"}
            """;

    public ChatbotResponse chat(ChatRequest request) {
        long start = System.currentTimeMillis();
        try {
            // Input validation — reject messages that are too short
            if (request.getMessage().trim().length() < 3) {
                throw new IllegalArgumentException(
                    "Message too short for processing: minimum 3 characters required, got " + request.getMessage().trim().length());
            }

            // 1. Search for relevant documents using RAG
            List<Document> relevantDocs = searchRelevantDocuments(request.getMessage());
            log.info("Chatbot question received, vector search returned {} documents", relevantDocs.size());

            // DEBUG: log metadata to verify Pinecone is returning correct documents
            relevantDocs.forEach(doc ->
                    log.debug("Doc metadata: {} | text preview: {}", doc.getMetadata(),
                            doc.getText() != null ? doc.getText().substring(0, Math.min(60, doc.getText().length())) : "null")
            );

            String context = buildContext(relevantDocs);

            // 2. Prepare messages for the LLM
            List<Message> messages = new ArrayList<>();
            messages.add(new SystemMessage(SYSTEM_PROMPT));

            if (request.getConversationHistory() != null) {
                request.getConversationHistory().forEach(msg -> messages.add(new UserMessage(msg)));
            }

            String userMessageWithContext = String.format(
                    "Context (for info only, do not list these):\n%s\n\nUser Question: %s",
                    context, request.getMessage()
            );
            messages.add(new UserMessage(userMessageWithContext));

            // 3. Get response from LLM
            Prompt prompt = new Prompt(messages);
            ChatResponse response = chatClient.call(prompt);

            // 4. Parse the JSON response from LLM
            String rawText = response.getResult().getOutput().getText();
            log.debug("LLM raw output: {}", rawText);
            String cleanedText = "Here are some products you might like:";

            try {
                if (rawText != null) {
                    String jsonStr = rawText
                            .replaceAll("(?i)```json", "")
                            .replaceAll("```", "")
                            .trim();

                    int startIdx = jsonStr.indexOf('{');
                    int endIdx = jsonStr.lastIndexOf('}');

                    if (startIdx != -1 && endIdx != -1 && endIdx >= startIdx) {
                        jsonStr = jsonStr.substring(startIdx, endIdx + 1);
                        JsonNode rootNode = objectMapper.readTree(jsonStr);
                        if (rootNode.has("message")) {
                            cleanedText = rootNode.get("message").asText().trim();
                        }
                    }
                }
            } catch (Exception e) {
                log.warn("Failed to parse LLM JSON response, using default message.", e);
            }

            // 5. Extract product recommendations from retrieved docs
            List<ProductRecommendation> recommendations = extractRecommendations(relevantDocs);
            log.info("Chatbot answered in {} ms with {} recommendations (question='{}')",
                    System.currentTimeMillis() - start, recommendations.size(), request.getMessage());

            if (recommendations.isEmpty()
                    && !request.getMessage().toLowerCase().matches(".*\\b(hi|hello|hey)\\b.*")) {
                cleanedText = "Sorry, I couldn't find any products matching your request.";
            }

            // 6. Build the final response
            ChatbotResponse customResponse = new ChatbotResponse();
            customResponse.setMessage(cleanedText);
            customResponse.setRecommendations(recommendations.stream().limit(3).collect(Collectors.toList()));
            customResponse.setContext(context);

            return customResponse;

        } catch (Exception e) {
            log.error("ChatbotService error: {}: {}", e.getClass().getSimpleName(), e.getMessage(), e);
            ChatbotResponse errorResponse = new ChatbotResponse();
            errorResponse.setMessage("I apologize, but I encountered an error processing your request. Please try again.");
            errorResponse.setError(e.getMessage() + "\n" + (e.getStackTrace().length > 0 ? e.getStackTrace()[0].toString() : ""));
            return errorResponse;
        }
    }

    private List<Document> searchRelevantDocuments(String query) {
        SearchRequest searchRequest = SearchRequest.builder()
                .query(query)
                .topK(maxResults)
                .similarityThreshold(similarityThreshold)
                .build();
        return vectorStore.similaritySearch(searchRequest);
    }

    private String buildContext(List<Document> documents) {
        if (documents.isEmpty()) {
            return "No specific product information available.";
        }
        StringBuilder context = new StringBuilder();
        for (Document doc : documents) {
            context.append(doc.getText()).append("\n---\n");
        }
        return context.toString();
    }

    private List<ProductRecommendation> extractRecommendations(List<Document> documents) {
        List<ProductRecommendation> recommendations = new ArrayList<>();

        for (Document doc : documents) {
            Map<String, Object> metadata = doc.getMetadata();
            String text = doc.getText() != null ? doc.getText() : "";

            // FIX: Determine product type via metadata OR text content as fallback.
            // Pinecone may not always return all metadata fields.
            boolean isProduct = "product".equals(metadata.get("type"))
                    || (text.contains("Product:") && text.contains("Price:") && text.contains("Stock:"));

            if (!isProduct) continue;

            ProductRecommendation rec = new ProductRecommendation();

            Object idObj = metadata.get("id");
            if (idObj instanceof Number) {
                rec.setProductId(((Number) idObj).longValue());
            } else if (idObj != null) {
                try {
                    rec.setProductId(Long.parseLong(idObj.toString()));
                } catch (NumberFormatException ignored) {}
            }
            
            if (rec.getProductId() == null && doc.getId() != null && doc.getId().startsWith("product-")) {
                try {
                    rec.setProductId(Long.parseLong(doc.getId().replace("product-", "")));
                } catch (NumberFormatException ignored) {}
            }

            // Name — fallback to text parsing if metadata missing
            Object nameObj = metadata.get("name");
            if (nameObj instanceof String) {
                rec.setProductName((String) nameObj);
            } else {
                rec.setProductName(extractLineValue(text, "Product:"));
            }

            // Price — fallback to text parsing if metadata missing
            Object priceObj = metadata.get("price");
            if (priceObj instanceof Number) {
                rec.setPrice(((Number) priceObj).doubleValue());
            } else {
                String priceStr = extractLineValue(text, "Price:");
                if (priceStr != null) {
                    try {
                        rec.setPrice(Double.parseDouble(
                                priceStr.replace("$", "").replace("₹", "").trim()));
                    } catch (NumberFormatException ignored) {}
                }
            }

            // Image URL
            Object imageObj = metadata.get("imageUrl");
            if (imageObj instanceof String) {
                rec.setImageUrl((String) imageObj);
            }

            // Available — Pinecone may return as String "true"/"false"
            Object availableObj = metadata.get("available");
            if (availableObj instanceof Boolean) {
                rec.setAvailable((Boolean) availableObj);
            } else if (availableObj instanceof String) {
                rec.setAvailable(Boolean.parseBoolean((String) availableObj));
            } else {
                String availStr = extractLineValue(text, "Available:");
                rec.setAvailable(availStr != null && availStr.trim().equalsIgnoreCase("Yes"));
            }

            // Rating
            Object ratingObj = metadata.get("averageRating");
            if (ratingObj instanceof Number) {
                rec.setRating(((Number) ratingObj).doubleValue());
            }

            recommendations.add(rec);
        }

        return recommendations;
    }

    /**
     * Extracts the value after a label from a multiline text block.
     * e.g. extractLineValue("Product: Dumbbell\nPrice: $25", "Product:") → "Dumbbell"
     */
    private String extractLineValue(String text, String label) {
        if (text == null || label == null) return null;
        for (String line : text.split("\n")) {
            if (line.startsWith(label)) {
                return line.substring(label.length()).trim();
            }
        }
        return null;
    }

    @Cacheable(value = "product-suggestions", key = "#category")
    public ChatbotResponse getProductSuggestions(String category) {
        String query = String.format("Suggest products from the %s category", category);
        return chat(new ChatRequest(query));
    }

    public ChatbotResponse handleProductInquiry(Long productId) {
        Products product = productsRepository.findById(productId).orElse(null);
        if (product == null) {
            ChatbotResponse response = new ChatbotResponse();
            response.setMessage("Product not found with ID: " + productId);
            return response;
        }
        String query = String.format("Tell me about the product: %s", product.getName());
        return chat(new ChatRequest(query));
    }
}