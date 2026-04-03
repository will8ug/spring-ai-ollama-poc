package io.will.springai2poc.service;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.ai.document.Document;
import org.springframework.ai.reader.jsoup.JsoupDocumentReader;
import org.springframework.ai.reader.jsoup.config.JsoupDocumentReaderConfig;
import org.springframework.ai.transformer.splitter.TokenTextSplitter;
import org.springframework.core.io.UrlResource;
import org.springframework.stereotype.Service;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;
import reactor.core.scheduler.Schedulers;

import java.net.MalformedURLException;
import java.net.URI;
import java.util.List;

@Service
public class WebContentService {
    private static final Logger logger = LoggerFactory.getLogger(WebContentService.class);

    private final VectorStoreService vectorStoreService;

    public WebContentService(VectorStoreService vectorStoreService) {
        this.vectorStoreService = vectorStoreService;
    }

    public List<Document> loadWebContent(String url) throws MalformedURLException {
        logger.info("Loading web content from {}", url);

        UrlResource resource = new UrlResource(URI.create(url));
        
        JsoupDocumentReaderConfig config = JsoupDocumentReaderConfig.builder()
                .selector("body")
                .charset("UTF-8")
                .build();
        
        JsoupDocumentReader reader = new JsoupDocumentReader(resource, config);
        List<Document> documents = reader.read();
        logger.info("{} documents loaded", documents.size());
        return documents;
    }

    public int processAndStoreWebContent(String url) throws MalformedURLException {
        List<Document> documents = loadWebContent(url);

        List<Document> splitDocuments = TokenTextSplitter.builder().build().split(documents);
        logger.info("{} documents splitted", splitDocuments.size());
        vectorStoreService.storeDocuments(splitDocuments);
        return splitDocuments.size();
    }

    public Flux<String> retrieveDocuments(String question) {
        return Mono.fromCallable(() -> vectorStoreService.searchDocuments(question))
                .subscribeOn(Schedulers.boundedElastic())
                .flatMapMany(Flux::fromIterable)
                .map(Document::getText);
    }
}
