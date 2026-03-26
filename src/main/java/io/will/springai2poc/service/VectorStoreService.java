package io.will.springai2poc.service;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.ai.document.Document;
import org.springframework.ai.vectorstore.SearchRequest;
import org.springframework.ai.vectorstore.VectorStore;
import org.springframework.ai.vectorstore.VectorStoreRetriever;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class VectorStoreService {
    private static final Logger logger = LoggerFactory.getLogger(VectorStoreService.class);

    private final VectorStore vectorStore;

    private final VectorStoreRetriever vectorStoreRetriever;

    public VectorStoreService(VectorStore vectorStore, VectorStoreRetriever vectorStoreRetriever) {
        this.vectorStore = vectorStore;
        this.vectorStoreRetriever = vectorStoreRetriever;
    }

    public void storeDocuments(List<Document> documents) {
        vectorStore.add(documents);
        logger.info("Stored {} documents", documents.size());
    }

    public List<Document> searchDocuments(String question) {
        SearchRequest searchRequest = SearchRequest.builder()
                .query(question)
                .topK(3)
                .build();

        return vectorStoreRetriever.similaritySearch(searchRequest);
    }
}
