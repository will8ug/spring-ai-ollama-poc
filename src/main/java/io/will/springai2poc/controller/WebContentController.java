package io.will.springai2poc.controller;

import io.will.springai2poc.controller.model.WebContentRequest;
import io.will.springai2poc.controller.model.WebContentResponse;
import io.will.springai2poc.service.WebContentService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.ai.document.Document;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;
import reactor.core.scheduler.Schedulers;

import java.net.MalformedURLException;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/web-content")
public class WebContentController {
    private static final Logger logger = LoggerFactory.getLogger(WebContentController.class);

    private final WebContentService webContentService;

    public WebContentController(WebContentService webContentService) {
        this.webContentService = webContentService;
    }

    @PostMapping(value = "/load", produces = MediaType.APPLICATION_JSON_VALUE)
    public ResponseEntity<WebContentResponse> loadWebContent(@RequestBody WebContentRequest request) throws MalformedURLException {
        logger.info("POST /load");

        List<Document> documents = webContentService.loadWebContent(request.url());
        List<String> contents = documents.stream()
                .map(Document::getText)
                .toList();

        WebContentResponse response = new WebContentResponse(request.url(), contents);
        return ResponseEntity.ok(response);
    }

    @PostMapping(value = "/process-and-store", produces = MediaType.APPLICATION_JSON_VALUE)
    public Mono<Map<String, Object>> processAndStoreWebContent(@RequestBody WebContentRequest request) {
        logger.info("POST /process-and-store");

        return Mono.fromCallable(() -> webContentService
                        .processAndStoreWebContent(request.url()))
                .subscribeOn(Schedulers.boundedElastic())
                .map(documentCount -> Map.of(
                        "url", request.url(),
                        "documentCount", documentCount,
                        "status", "success"
                ));
    }

    @PostMapping(value = "/retrieve", produces = MediaType.APPLICATION_JSON_VALUE)
    public Flux<WebContentResponse> retrieve(@RequestBody WebContentRequest request) {
        logger.info("POST /retrieve");

        return webContentService.retrieveDocuments(request.question())
                .map(content -> new WebContentResponse(null, List.of(content)));
    }
}
