package io.will.springai2poc.controller;

import io.will.springai2poc.controller.model.WebContentRequest;
import io.will.springai2poc.controller.model.WebContentResponse;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.test.web.reactive.server.WebTestClient;

import static java.time.Duration.ofSeconds;
import static org.assertj.core.api.Assertions.assertThat;

@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
class WebContentControllerIT {

    @Value("${local.server.port}")
    private int port;

    private WebTestClient webTestClient;

    @BeforeEach
    void beforeEach() {
        webTestClient = WebTestClient.bindToServer()
                .baseUrl("http://localhost:" + port)
                .responseTimeout(ofSeconds(30))
                .build();
    }

    @Test
    void loadWebContentReturnsExpectedStructure() {
        String testUrl = "https://lilianweng.github.io/posts/2023-06-23-agent/";
        WebContentRequest request = WebContentRequest.withUrl(testUrl);

        webTestClient.post()
                .uri("/web-content/load")
                .contentType(MediaType.APPLICATION_JSON)
                .bodyValue(request)
                .exchange()
                .expectStatus().isOk()
                .expectBody(WebContentResponse.class)
                .value(response -> {
                    assertThat(response.url()).isEqualTo(testUrl);
                    assertThat(response.contents()).isNotNull();
                    assertThat(response.contents()).isNotEmpty();
                    String combinedContent = String.join(" ", response.contents());
                    assertThat(combinedContent)
                            .containsIgnoringCase("agent")
                            .containsIgnoringCase("LLM");
                });
    }

    @Test
    void processAndStoreWebContentReturnsSuccess() {
        String testUrl = "https://lilianweng.github.io/posts/2023-06-23-agent/";
        WebContentRequest request = WebContentRequest.withUrl(testUrl);

        webTestClient.post()
                .uri("/web-content/process-and-store")
                .contentType(MediaType.APPLICATION_JSON)
                .bodyValue(request)
                .exchange()
                .expectStatus().isOk()
                .expectBody()
                .jsonPath("$.url").isEqualTo(testUrl)
                .jsonPath("$.status").isEqualTo("success")
                .jsonPath("$.documentCount").isNumber();
    }

    @Test
    void retrieveDocumentsReturnsExpectedFlux() {
        String testUrl = "https://lilianweng.github.io/posts/2023-06-23-agent/";
        WebContentRequest storeRequest = WebContentRequest.withUrl(testUrl);
        
        webTestClient.post()
                .uri("/web-content/process-and-store")
                .contentType(MediaType.APPLICATION_JSON)
                .bodyValue(storeRequest)
                .exchange()
                .expectStatus().isOk();

        String testQuestion = "What is an agent?";
        WebContentRequest retrieveRequest = WebContentRequest.withQuestion(testQuestion);

        webTestClient.post()
                .uri("/web-content/retrieve")
                .contentType(MediaType.APPLICATION_JSON)
                .bodyValue(retrieveRequest)
                .exchange()
                .expectStatus().isOk()
                .expectBodyList(String.class)
                .value(contents -> {
                    assertThat(contents).isNotNull();
                    assertThat(contents).isNotEmpty();

                    boolean hasRelevantContent = contents.stream()
                            .anyMatch(content -> content.toLowerCase().contains("agent"));
                    assertThat(hasRelevantContent).isTrue();
                });
    }
}
