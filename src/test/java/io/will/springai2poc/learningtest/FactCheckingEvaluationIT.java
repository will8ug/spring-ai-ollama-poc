package io.will.springai2poc.learningtest;

import org.junit.jupiter.api.Test;
import org.springframework.ai.chat.client.ChatClient;
import org.springframework.ai.chat.evaluation.FactCheckingEvaluator;
import org.springframework.ai.chat.model.ChatModel;
import org.springframework.ai.evaluation.EvaluationRequest;
import org.springframework.ai.evaluation.EvaluationResponse;
import org.springframework.ai.ollama.OllamaChatModel;
import org.springframework.ai.ollama.api.OllamaApi;
import org.springframework.ai.ollama.api.OllamaChatOptions;

import static org.junit.jupiter.api.Assertions.assertFalse;

public class FactCheckingEvaluationIT {
    public static final String BESPOKE_MINICHECK = "bespoke-minicheck:7b";

    @Test
    void testFactChecking() {
        OllamaApi ollamaApi = OllamaApi.builder().build();

        OllamaChatOptions ollamaChatOptions = OllamaChatOptions.builder()
                .model(BESPOKE_MINICHECK)
                .numPredict(2)
                .temperature(0.0d)
                .build();
        ChatModel ollamaChatModel = OllamaChatModel.builder()
                .ollamaApi(ollamaApi)
                .defaultOptions(ollamaChatOptions)
                .build();

        FactCheckingEvaluator factCheckingEvaluator = FactCheckingEvaluator
                .builder(ChatClient.builder(ollamaChatModel))
                .build();

        String context = "The Earth is the third planet from the Sun and the only astronomical object known to harbor life.";
        String claim = "The Earth is the fourth planet from the Sun.";

        EvaluationRequest evaluationRequest = new EvaluationRequest(context, claim);
        EvaluationResponse result = factCheckingEvaluator.evaluate(evaluationRequest);
        assertFalse(result.isPass(), "The claim should not be supported by the context");
    }
}
