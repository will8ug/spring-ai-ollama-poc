package io.will.springai2poc.controller.model;

public record WebContentRequest(
        String url,
        String question
) {
    public static WebContentRequest withUrl(String url) {
        return new WebContentRequest(url, null);
    }

    public static WebContentRequest withQuestion(String question) {
        return new WebContentRequest(null, question);
    }
}
