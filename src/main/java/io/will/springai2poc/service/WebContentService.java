package io.will.springai2poc.service;

import org.springframework.ai.document.Document;
import org.springframework.ai.reader.jsoup.JsoupDocumentReader;
import org.springframework.ai.reader.jsoup.config.JsoupDocumentReaderConfig;
import org.springframework.ai.transformer.splitter.TokenTextSplitter;
import org.springframework.core.io.UrlResource;
import org.springframework.stereotype.Service;

import java.net.MalformedURLException;
import java.net.URI;
import java.util.List;

@Service
public class WebContentService {

    private final VectorStoreService vectorStoreService;

    public WebContentService(VectorStoreService vectorStoreService) {
        this.vectorStoreService = vectorStoreService;
    }

    public List<Document> loadWebContent(String url) throws MalformedURLException {
        UrlResource resource = new UrlResource(URI.create(url));
        
        JsoupDocumentReaderConfig config = JsoupDocumentReaderConfig.builder()
                .selector("body")
                .charset("UTF-8")
                .build();
        
        JsoupDocumentReader reader = new JsoupDocumentReader(resource, config);
        return reader.read();
    }

    public int processAndStoreWebContent(String url) throws MalformedURLException {
        List<Document> documents = loadWebContent(url);
        List<Document> splitDocuments = TokenTextSplitter.builder().build().split(documents);
        
        vectorStoreService.storeDocuments(splitDocuments);
        return splitDocuments.size();
    }
}
