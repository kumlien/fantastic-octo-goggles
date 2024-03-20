package se.kumliens.chat.service;

import com.vaadin.flow.server.auth.AnonymousAllowed;
import dev.hilla.BrowserCallable;
import dev.langchain4j.memory.chat.ChatMemoryProvider;
import dev.langchain4j.memory.chat.MessageWindowChatMemory;
import dev.langchain4j.memory.chat.TokenWindowChatMemory;
import dev.langchain4j.model.azure.AzureOpenAiChatModel;
import dev.langchain4j.model.azure.AzureOpenAiStreamingChatModel;
import dev.langchain4j.model.embedding.EmbeddingModel;
import dev.langchain4j.model.input.PromptTemplate;
import dev.langchain4j.model.openai.OpenAiStreamingChatModel;
import dev.langchain4j.model.openai.OpenAiTokenizer;
import dev.langchain4j.rag.content.retriever.ContentRetriever;
import dev.langchain4j.service.*;
import jakarta.annotation.PostConstruct;
import lombok.RequiredArgsConstructor;
import lombok.SneakyThrows;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.tinylog.Logger;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Sinks;
import se.kumliens.chat.tools.ExampleTool;

import java.util.Map;

@BrowserCallable
@AnonymousAllowed
@Service
@RequiredArgsConstructor
public class TheChatService implements ChatService {

    private final ExampleTool exampleTool;

    //Used to retrieve relevant embeddings from the embedding store for our question
    private final ContentRetriever retriever;

    private final EmbeddingModel embeddingModel;

    @Value("${azure.openai.api.key}")
    private String AZURE_OPENAI_API_KEY;

    @Value("${azure.openai.api.base_url}")
    private String AZURE_OPENAI_API_BASE_URL;

    @Value("${azure.openai.api.version}")
    private String AZURE_OPENAI_API_VERSION;

    @Value("${openai.api.key}")
    private String OPENAI_API_KEY;

    private Assistant assistant;

    private StreamingAssistant azureStreamingAssistant;

    private StreamingAssistant openAIStreamingAssistant;

    interface Assistant {
        @SystemMessage("""
                You are a professional hr specialist working at a it consultancy firm called IT-HUSET.
                You are helping the internal administrative employees to answer questions about the consultants working at IT-HUSET.
                You will always respond in a polite tone.
                You will ask the user for more information if you need that in order to answer the question.
                Think step by step and take your time before answering any question.
                """)
        String chat(@MemoryId Object memoryId, @UserMessage String message);
    }

    interface StreamingAssistant {
        @SystemMessage("""
                You are a professional hr specialist working at a it consultancy firm called IT-HUSET.
                You are helping the internal administrative employees to answer questions about the consultants working at IT-HUSET.
                You will always respond in a polite tone.
                You will ask the user for more information if you need that in order to answer the question.
                Think step by step and take your time before answering any question.
                """)
        TokenStream chat(@MemoryId Object memoryId, @UserMessage String message);
    }

    @PostConstruct
    public void init() {

        var memory = TokenWindowChatMemory.withMaxTokens(2000, new OpenAiTokenizer("gpt-3.5-turbo"));
        var chatStore = new PersistentChatMemoryStore();
        ChatMemoryProvider memoryProvider = memoryId -> MessageWindowChatMemory.builder()
                .id(memoryId)
                .maxMessages(50)
                .chatMemoryStore(chatStore)
                .build();

        assistant = AiServices.builder(Assistant.class)
                .chatLanguageModel(AzureOpenAiChatModel.builder()
                        .apiKey(AZURE_OPENAI_API_KEY)
                        .endpoint(AZURE_OPENAI_API_BASE_URL)
                        .serviceVersion(AZURE_OPENAI_API_VERSION)
                        .logRequestsAndResponses(true)
                        .build())
                .chatMemoryProvider(memoryProvider)
                .tools(exampleTool)
                .build();

        azureStreamingAssistant = AiServices.builder(StreamingAssistant.class)
                .streamingChatLanguageModel(AzureOpenAiStreamingChatModel.builder()
                        .apiKey(AZURE_OPENAI_API_KEY)
                        .endpoint(AZURE_OPENAI_API_BASE_URL)
                        .serviceVersion(AZURE_OPENAI_API_VERSION)
                        .logRequestsAndResponses(true)
                        .build())
                .chatMemory(memory)
                //.chatMemoryProvider(memoryProvider)
                //.contentRetriever(retriever)
                //.tools(exampleTool)
                .build();


        openAIStreamingAssistant = AiServices.builder(StreamingAssistant.class)
                .streamingChatLanguageModel(OpenAiStreamingChatModel.builder()
                        .apiKey(OPENAI_API_KEY)
                        .logRequests(true)
                        .logResponses(true)
                        .build())
                //.chatMemory(memory)
                //.chatMemoryProvider(memoryProvider)
                //.tools(exampleTool)
                .build();


    }

    public String chat(String chatId, String message) {
        Logger.info("Sending '{}' to chat engine in non-streaming mode", message);
        var response = assistant.chat(chatId, message);
        Logger.info("Response from chat engine is '{}", response);
        return response;
    }

    @SneakyThrows
    public Flux<String> chatStream(String chatId, String message) {
        //Find the relevant embeddings for the message
        PromptTemplate promptTemplate = PromptTemplate.from(
                """
                        Answer the following question to the best of your ability. Take your time before answering
                        and think in multiple steps. Provide the answer in swedish.
                                              
                        Question:
                        {{question}}
                                       
                        """
        );
        var prompt = promptTemplate.apply(Map.of("question", message));

        Sinks.Many<String> sink = Sinks.many().unicast().onBackpressureBuffer();
        Logger.debug("Sending '{}' to chat assistant using streaming mode", message);
        Thread.ofVirtual().start(() ->
                azureStreamingAssistant.chat(chatId, prompt.text())
                .onNext(t -> {
                    Logger.info("Emitting '{}' with current subscriber count={}", t, sink.currentSubscriberCount());
                    sink.emitNext(t, (v1, v2) -> {
                        Logger.info("Failed to emit, signal type {}, Result: {}", v1, v2);
                        return true;
                    });
                })
                .onComplete(response -> {
                    Logger.info("On complete: {}", response.content().text());
                    sink.tryEmitComplete();
                })
                .onError(t -> {
                    Logger.warn(t, "On error: exception occurred: {}", t.getMessage());
                    sink.tryEmitError(t);
                }).start()
        );
        Logger.info("Efter start...");
        return sink.asFlux();
    }
}
