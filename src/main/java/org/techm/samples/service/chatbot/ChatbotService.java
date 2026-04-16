package org.techm.samples.service.chatbot;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
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
import org.springframework.cache.annotation.Cacheable;
import org.springframework.stereotype.Service;
import org.techm.samples.dto.ChatRequest;
import org.techm.samples.dto.ChatbotResponse;
import org.techm.samples.dto.ProductRecommendation;
import org.techm.samples.entity.Products;
import org.techm.samples.repository.ProductsRepository;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

@Service
public class ChatbotService {

    @Autowired
    private ChatModel chatClient;

    @Autowired
    private VectorStore vectorStore;

    @Autowired
    private ProductsRepository productsRepository;

    @Value("${chatbot.context.max-results:5}")
    private int maxResults;

    @Value("${chatbot.similarity.threshold:0.7}")
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
            {
              "message": "Here are some ergonomic products that can help relieve your back pain:"
            }
            """;

    public ChatbotResponse chat(ChatRequest request) {
        try {
            // 1. Search for relevant documents using RAG
            List<Document> relevantDocs = searchRelevantDocuments(request.getMessage());
            String context = buildContext(relevantDocs);

            // 2. Prepare messages for the LLM
            List<Message> messages = new ArrayList<>();
            messages.add(new SystemMessage(SYSTEM_PROMPT));

            if (request.getConversationHistory() != null) {
                request.getConversationHistory().forEach(msg -> messages.add(new UserMessage(msg)));
            }

            // We still pass context so the LLM understands the domain, but it won't list the products
            String userMessageWithContext = String.format(
                    "Context (For info only, do not list these):\n%s\n\nUser Question: %s",
                    context, request.getMessage()
            );
            messages.add(new UserMessage(userMessageWithContext));

            // 3. Get response from LLM
            Prompt prompt = new Prompt(messages);
            ChatResponse response = chatClient.call(prompt);

            // 4. Force the LLM output to be extremely clean using JSON parsing
            String rawText = response.getResult().getOutput().getText();
            System.out.println(rawText);
            String cleanedText = "Here are some products you might like:"; // Safe default fallback

            try {
                if (rawText != null) {
                    // Strip markdown code blocks if the LLM adds them around the JSON
                    String jsonStr = rawText.replaceAll("(?i)```json", "").replaceAll("```", "").trim();

                    // Extract just the JSON part in case there's leading/trailing text
                    int startIdx = jsonStr.indexOf('{');
                    int endIdx = jsonStr.lastIndexOf('}');

                    if (startIdx != -1 && endIdx != -1 && endIdx >= startIdx) {
                        jsonStr = jsonStr.substring(startIdx, endIdx + 1);
                        JsonNode rootNode = objectMapper.readTree(jsonStr);
                        if (rootNode.has("message")) {
                            cleanedText = rootNode.get("message").asText().trim();
                        }
                    } else {
                        // Failsafe: if JSON format completely failed, try to find a sentence in quotes
                        String[] lines = rawText.split("\n");
                        for (int i = lines.length - 1; i >= 0; i--) {
                            if (lines[i].contains("\"")) {
                                int firstQ = lines[i].indexOf('"');
                                int lastQ = lines[i].lastIndexOf('"');
                                if (lastQ > firstQ) {
                                    cleanedText = lines[i].substring(firstQ + 1, lastQ).trim();
                                    break;
                                }
                            }
                        }
                    }
                }
            } catch (Exception e) {
                System.err.println("Failed to parse LLM JSON response. Falling back to default string.");
            }

            // 5. Extract the actual product data for the UI cards
            List<ProductRecommendation> recommendations = extractRecommendations(relevantDocs);

            // Override message if no products were found in the database
            if (recommendations.isEmpty() && !request.getMessage().toLowerCase().matches(".*\\b(hi|hello|hey)\\b.*")) {
                cleanedText = "Sorry, I couldn't find any products matching your request.";
            }

            // 6. Build the final response
            ChatbotResponse customResponse = new ChatbotResponse();
            customResponse.setMessage(cleanedText);
            customResponse.setRecommendations(recommendations.stream().limit(3).collect(Collectors.toList()));
            customResponse.setContext(context);

            return customResponse;

        } catch (Exception e) {
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
        return documents.stream()
                .filter(doc -> "product".equals(doc.getMetadata().get("type")))
                .map(doc -> {
                    ProductRecommendation rec = new ProductRecommendation();
                    rec.setProductId((Long) doc.getMetadata().get("id"));
                    rec.setProductName((String) doc.getMetadata().get("name"));
                    rec.setPrice((Double) doc.getMetadata().get("price"));
                    rec.setImageUrl((String) doc.getMetadata().get("imageUrl"));
                    rec.setAvailable((Boolean) doc.getMetadata().get("available"));
                    if (doc.getMetadata().containsKey("averageRating")) {
                        rec.setRating((Double) doc.getMetadata().get("averageRating"));
                    }
                    return rec;
                })
                .collect(Collectors.toList());
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