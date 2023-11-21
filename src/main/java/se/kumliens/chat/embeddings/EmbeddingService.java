package se.kumliens.chat.embeddings;

import dev.langchain4j.data.document.Document;
import dev.langchain4j.data.document.DocumentType;
import dev.langchain4j.data.document.Metadata;
import dev.langchain4j.data.document.splitter.DocumentSplitters;
import dev.langchain4j.data.segment.TextSegment;
import dev.langchain4j.model.Tokenizer;
import dev.langchain4j.model.embedding.EmbeddingModel;
import dev.langchain4j.store.embedding.EmbeddingStore;
import dev.langchain4j.store.embedding.EmbeddingStoreIngestor;
import jakarta.annotation.PostConstruct;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.tinylog.Logger;

import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.List;
import java.util.Map;

import static dev.langchain4j.data.document.FileSystemDocumentLoader.loadDocument;
import static dev.langchain4j.data.document.FileSystemDocumentLoader.loadDocuments;


@Service
@RequiredArgsConstructor
public class EmbeddingService {

    private final EmbeddingModel embeddingModel;

    private final EmbeddingStore<TextSegment> embeddingStore;

    private final Tokenizer tokenizer;

    @PostConstruct
    public void init() {
        Logger.info("Start by embedding stuff on startup since we have an in-memory embedding store");
        var path = "src/main/resources/embedd_this/";
        List<Document> documents = loadDocuments(path);
        for(var doc:documents) {
            var fileName = doc.metadata().get("file_name");
            var names = fileName.split("_|\\.");
            doc.metadata().add("first name", names[0]);
            doc.metadata().add("last name", names[1]);
            Logger.info("Handle document {} for {} {}", doc, doc.metadata().get("first name"), doc.metadata().get("last name"));
            var ingester =  EmbeddingStoreIngestor.builder()
                    .documentSplitter(DocumentSplitters.recursive(500,0, tokenizer))
                    .embeddingModel(embeddingModel)
                    .embeddingStore(embeddingStore)
                    .textSegmentTransformer(ts -> {
                        ts.metadata().add("förnamn", names[0]);
                        ts.metadata().add("efternamn", names[1]);
                        return ts;
                    })
                    .build();
            ingester.ingest(doc);
        }
    }

    public void ingestDocument(String path, DocumentType documentType, Map<String, String> metadata) {
        Logger.info("Ingesting a {} file from {} using metadata {}", documentType, path, metadata);
        var doc = loadDocument(Paths.get(path), documentType);
        var ingester =  EmbeddingStoreIngestor.builder()
                .documentSplitter(DocumentSplitters.recursive(100,0, tokenizer))
                .embeddingModel(embeddingModel)
                .embeddingStore(embeddingStore)
                .textSegmentTransformer(ts -> {
                    metadata.forEach(ts.metadata()::add);
                    return ts;
                })
                .build();

        ingester.ingest(doc);
    }
}
