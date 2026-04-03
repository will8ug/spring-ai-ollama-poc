package io.will.springai2poc.learningtest;

import io.will.springai2poc.controller.model.WebContentRequest;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;
import org.springframework.ai.chat.client.ChatClient;
import org.springframework.ai.chat.evaluation.RelevancyEvaluator;
import org.springframework.ai.chat.model.ChatModel;
import org.springframework.ai.chat.model.ChatResponse;
import org.springframework.ai.document.Document;
import org.springframework.ai.evaluation.EvaluationRequest;
import org.springframework.ai.evaluation.EvaluationResponse;
import org.springframework.ai.ollama.OllamaChatModel;
import org.springframework.ai.ollama.api.OllamaApi;
import org.springframework.ai.ollama.api.OllamaChatOptions;
import org.springframework.ai.rag.advisor.RetrievalAugmentationAdvisor;
import org.springframework.ai.rag.retrieval.search.VectorStoreDocumentRetriever;
import org.springframework.ai.vectorstore.VectorStore;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.web.server.LocalServerPort;
import org.springframework.http.MediaType;
import org.springframework.test.web.reactive.server.WebTestClient;

import java.util.List;

import static java.time.Duration.ofSeconds;
import static org.assertj.core.api.Assertions.assertThat;

@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
@Tag("with-containers")
public class RelevancyEvaluationIT {
    @Autowired
    private VectorStore vectorStore;

    @LocalServerPort
    private int port;

    @Test
    void testRelevancy() {
        setupBasicVectorData();

        RetrievalAugmentationAdvisor ragAdvisor = RetrievalAugmentationAdvisor.builder()
                .documentRetriever(VectorStoreDocumentRetriever.builder()
                        .vectorStore(vectorStore)
                        .topK(1)
                        .build())
                .build();

        String query = "What is the standard method for Task Decomposition?";
        ChatResponse chatResponse = ChatClient.builder(simpleModel()).build()
                .prompt()
                .user(query)
                .advisors(ragAdvisor)
                .call()
                .chatResponse();

        List<Document> docs = chatResponse.getMetadata().get(RetrievalAugmentationAdvisor.DOCUMENT_CONTEXT);
        assertThat(docs).isNotNull();
        assertThat(docs).isNotEmpty();

        System.out.println(">==================<");
        System.out.println(chatResponse.getResult().getOutput().getText());
        System.out.println(">==================<");

        EvaluationRequest evaluationRequest = new EvaluationRequest(query,
                docs,
                chatResponse.getResult().getOutput().getText());
        RelevancyEvaluator relevancyEvaluator = new RelevancyEvaluator(ChatClient.builder(complexModel()));

        EvaluationResponse evaluationResponse = relevancyEvaluator.evaluate(evaluationRequest);
        assertThat(evaluationResponse.isPass()).isTrue();
    }

    private void setupBasicVectorData() {
        WebTestClient webTestClient = WebTestClient.bindToServer()
                .baseUrl("http://localhost:" + port)
                .responseTimeout(ofSeconds(30))
                .build();

        String testUrl = "https://lilianweng.github.io/posts/2023-06-23-agent/";
        WebContentRequest storeRequest = WebContentRequest.withUrl(testUrl);

        webTestClient.post()
                .uri("/web-content/process-and-store")
                .contentType(MediaType.APPLICATION_JSON)
                .bodyValue(storeRequest)
                .exchange()
                .expectStatus().isOk();
    }

    private ChatModel simpleModel() {
        OllamaApi ollamaApi = OllamaApi.builder().build();

        OllamaChatOptions chatOptions = OllamaChatOptions.builder()
                .model("gemma3:270m")
                .temperature(0.0d)
                .build();

        return OllamaChatModel.builder()
                .ollamaApi(ollamaApi)
                .defaultOptions(chatOptions)
                .build();
    }

    private ChatModel complexModel() {
        OllamaApi ollamaApi = OllamaApi.builder().build();

        OllamaChatOptions chatOptions = OllamaChatOptions.builder()
                .model("gemma3:4b")
                .temperature(0.0d)
                .build();

        return OllamaChatModel.builder()
                .ollamaApi(ollamaApi)
                .defaultOptions(chatOptions)
                .build();
    }
}
