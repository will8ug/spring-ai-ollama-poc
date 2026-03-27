package io.will.springai2poc.controller;

import io.will.springai2poc.controller.model.ChatRequest;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.web.server.LocalServerPort;
import org.springframework.http.MediaType;
import org.springframework.test.web.reactive.server.WebTestClient;

import static org.junit.jupiter.api.Assertions.assertNotNull;

@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
class AiChatControllerIT {
    @LocalServerPort
    private int port;

    private WebTestClient webTestClient;

    @BeforeEach
    void beforeEach() {
        webTestClient = WebTestClient.bindToServer()
                .baseUrl("http://localhost:" + port)
                .build();
    }

    @Test
    void chatReturnsResponse() {
        webTestClient.post().uri("/chat")
                .contentType(MediaType.APPLICATION_JSON)
                .bodyValue("{\"message\": \"This is a test message. Please simply let me know if you are working.\"}")
                .exchange()
                .expectStatus().isOk()
                .expectBody()
                .jsonPath("$.response")
                .isNotEmpty();
    }

    @Test
    void chatWithRagReturnsExpectedContent() {
        webTestClient.post().uri("/chat-with-rag")
                .contentType(MediaType.APPLICATION_JSON)
                .bodyValue(new ChatRequest("What is an agent?"))
                .exchange()
                .expectStatus().isOk()
                .expectBody()
                .jsonPath("$.response")
                .value(contents -> {
                    assertNotNull(contents);
                    System.out.println("===========response contents below=========");
                    System.out.println(contents);
                });
    }
}
