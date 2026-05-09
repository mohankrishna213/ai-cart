package org.techm.samples.controller.api;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Lazy;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import org.techm.samples.dto.ChatRequest;
import org.techm.samples.dto.ChatbotResponse;
import org.techm.samples.service.chatbot.ChatbotService;
import org.techm.samples.service.chatbot.ProductDocumentLoader;
import org.springframework.ai.vectorstore.VectorStore;

@RestController
@RequestMapping("/api/chatbot")
@CrossOrigin(origins = "*") // Configure appropriately for production
public class ChatbotController {

    @Autowired
    private ChatbotService chatbotService;

    @Autowired
    private ProductDocumentLoader documentLoader;

    @Autowired
    @Lazy
    private VectorStore vectorStore;

    @PostMapping("/chat")
    public ResponseEntity<ChatbotResponse> chat(@RequestBody ChatRequest request) {
        if (request.getMessage() == null || request.getMessage().trim().isEmpty()) {
            ChatbotResponse errorResponse = new ChatbotResponse();
            errorResponse.setError("Message cannot be empty");
            return ResponseEntity.badRequest().body(errorResponse);
        }

        ChatbotResponse response = chatbotService.chat(request);
        return ResponseEntity.ok(response);
    }

    @GetMapping("/suggest/{category}")
    public ResponseEntity<ChatbotResponse> getSuggestions(@PathVariable String category) {
        ChatbotResponse response = chatbotService.getProductSuggestions(category);
        return ResponseEntity.ok(response);
    }

    @GetMapping("/product/{productId}")
    public ResponseEntity<ChatbotResponse> getProductInfo(@PathVariable Long productId) {
        ChatbotResponse response = chatbotService.handleProductInquiry(productId);
        return ResponseEntity.ok(response);
    }

    @PostMapping("/refresh-knowledge")
    public ResponseEntity<String> refreshKnowledgeBase() {
        try {
            // Reload all documents into vector store
            var documents = documentLoader.loadProductDocuments();
            vectorStore.add(documents);
            return ResponseEntity.ok("Knowledge base refreshed with " + documents.size() + " documents");
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body("Failed to refresh knowledge base: " + e.getMessage());
        }
    }

    @GetMapping("/health")
    public ResponseEntity<String> health() {
        return ResponseEntity.ok("Chatbot service is running");
    }
}