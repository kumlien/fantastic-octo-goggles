package se.kumliens.chat.embeddings;

import dev.langchain4j.data.document.parser.apache.pdfbox.ApachePdfBoxDocumentParser;
import dev.langchain4j.data.segment.TextSegment;
import dev.langchain4j.model.Tokenizer;
import dev.langchain4j.model.embedding.AllMiniLmL6V2EmbeddingModel;
import dev.langchain4j.model.embedding.EmbeddingModel;
import dev.langchain4j.model.openai.OpenAiTokenizer;
import dev.langchain4j.store.embedding.EmbeddingStore;
import dev.langchain4j.store.embedding.inmemory.InMemoryEmbeddingStore;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Spy;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.Map;

import static dev.langchain4j.model.openai.OpenAiModelName.GPT_3_5_TURBO;
import static org.junit.jupiter.api.Assertions.assertFalse;

@ExtendWith(MockitoExtension.class)
class EmbeddingServiceTest {

    @Spy
    EmbeddingModel embeddingModel = new AllMiniLmL6V2EmbeddingModel();

    @Spy
    EmbeddingStore<TextSegment> embeddingStore = new InMemoryEmbeddingStore<>();

    @Spy
    Tokenizer tokenizer = new OpenAiTokenizer(GPT_3_5_TURBO);

    @InjectMocks
    EmbeddingService embeddingService;

    @Test
    @DisplayName("when asked to ingest a document the service should call the embedding store")
    void ingestDocument() {
        embeddingService.ingestDocument("src/test/files/svante_kumlien.pdf", new ApachePdfBoxDocumentParser(), Map.of("name", "Svante Kumlien"));
        var q = "Kan Svante java?";
        var qEmbedding = embeddingModel.embed(q).content();
        var relevantEmbeddings = embeddingStore.findRelevant(qEmbedding, 5, 0.7);
        assertFalse(relevantEmbeddings.isEmpty());
    }
}