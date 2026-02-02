package org.techm.samples.dto;

import java.util.List;

public class ChatbotResponse {
    private String message;
    private List<ProductRecommendation> recommendations;
    private String context;
    private String error;

    public String getMessage() {
        return message;
    }

    public void setMessage(String message) {
        this.message = message;
    }

    public List<ProductRecommendation> getRecommendations() {
        return recommendations;
    }

    public void setRecommendations(List<ProductRecommendation> recommendations) {
        this.recommendations = recommendations;
    }

    public String getContext() {
        return context;
    }

    public void setContext(String context) {
        this.context = context;
    }

    public String getError() {
        return error;
    }

    public void setError(String error) {
        this.error = error;
    }
}