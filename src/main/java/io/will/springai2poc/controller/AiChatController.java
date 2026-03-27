package io.will.springai2poc.controller;

import io.will.springai2poc.controller.model.ChatRequest;
import io.will.springai2poc.controller.model.ChatResponse;
import org.springframework.ai.chat.client.ChatClient;
import org.springframework.ai.chat.client.advisor.vectorstore.QuestionAnswerAdvisor;
import org.springframework.ai.vectorstore.VectorStore;
import org.springframework.http.MediaType;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RestController;
import reactor.core.publisher.Mono;
import reactor.core.scheduler.Schedulers;

@RestController
public class AiChatController {

    private final ChatClient chatClient;

    private final VectorStore vectorStore;

    public AiChatController(ChatClient chatClient, VectorStore vectorStore) {
        this.chatClient = chatClient;
        this.vectorStore = vectorStore;
    }

    @PostMapping(value = "/chat", produces = MediaType.APPLICATION_JSON_VALUE)
    public Mono<ChatResponse> chat(@RequestBody ChatRequest request) {
        return Mono.fromCallable(() -> chatClient.prompt()
                        .user(request.message())
                        .call()
                        .content())
                .subscribeOn(Schedulers.boundedElastic())
                .map(ChatResponse::new);
    }

    @PostMapping(value = "/chat-with-rag", produces = MediaType.APPLICATION_JSON_VALUE)
    public Mono<ChatResponse> chatWithRag(@RequestBody ChatRequest request) {
        return Mono.fromCallable(() -> chatClient.prompt()
                        .advisors(QuestionAnswerAdvisor.builder(vectorStore).build())
                        .user(request.message())
                        .call()
                        .content())
                .subscribeOn(Schedulers.boundedElastic())
                .map(ChatResponse::new);
    }
}
