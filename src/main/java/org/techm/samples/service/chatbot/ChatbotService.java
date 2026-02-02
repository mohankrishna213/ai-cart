package org.techm.samples.service.chatbot;

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
import java.util.Map;
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

    private static final String SYSTEM_PROMPT = """
            You are an expert product catalog assistant. Your task is to help users find products based on their requests.

            Here is your process:
            1.  **Analyze the User's Query**: Carefully read the user's question to identify all constraints. This includes the product category (e.g., "T-shirts", "bags"), price limits (e.g., "under 800"), and any other keywords (e.g., "stylish", "gaming").
            2.  **Strictly Filter the Context**: You will be given a block of "Relevant Product Information". You MUST ignore any and all products in this context that do not strictly meet ALL of the user's constraints.
                * Example 1: If the user asks for "T-shirts under 800", you MUST ignore any T-shirts with a price over 800 and you MUST ignore any items that are not T-shirts (like caps or bags).
                * Example 2: If the user asks for "bags", you MUST ignore all items that are not bags.
            3.  **Format the Output**:
                * If you find matching products after filtering, present them in an HTML unordered list (`<ul>` and `<li>`).
                * For each product, bold the name with `<b>` tags and include the price.
                * Do not invent products or information. Only use what is provided in the context.
            4.  **Handle No Results**: If, after filtering, you find no products that match the user's request, politely inform them that you couldn't find any matching items.
        """;

    public ChatbotResponse chat(ChatRequest request) {
        try {
            // Search for relevant documents using RAG.
            List<Document> relevantDocs = searchRelevantDocuments(request.getMessage());

            // Build context from relevant documents
            String context = buildContext(relevantDocs);

            // Create messages for the chat
            List<Message> messages = new ArrayList<>();
            messages.add(new SystemMessage(SYSTEM_PROMPT));

            if (request.getConversationHistory() != null) {
                request.getConversationHistory().forEach(msg -> messages.add(new UserMessage(msg)));
            }

            String userMessageWithContext = String.format("Context:\n%s\n\nUser Question: %s", context, request.getMessage());
            messages.add(new UserMessage(userMessageWithContext));

            // Get response from Ollama
            Prompt prompt = new Prompt(messages);
            ChatResponse response = chatClient.call(prompt);

            // Extract recommendations from the relevant documents for the UI
            List<ProductRecommendation> recommendations = extractRecommendations(relevantDocs);

            // Build custom response
            ChatbotResponse customResponse = new ChatbotResponse();
            customResponse.setMessage(response.getResult().getOutput().getContent());
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
        SearchRequest searchRequest = SearchRequest.query(query)
                .withTopK(maxResults)
                .withSimilarityThreshold(similarityThreshold);
        return vectorStore.similaritySearch(searchRequest);
    }

    private String buildContext(List<Document> documents) {
        if (documents.isEmpty()) {
            return "No specific product information available.";
        }
        StringBuilder context = new StringBuilder();
        context.append("Relevant Product Information:\n\n");
        for (Document doc : documents) {
            context.append(doc.getContent()).append("\n---\n");
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