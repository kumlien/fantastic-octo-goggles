package se.kumliens.chat.service;

import com.vaadin.flow.server.auth.AnonymousAllowed;
import dev.hilla.BrowserCallable;
import dev.langchain4j.data.segment.TextSegment;
import dev.langchain4j.memory.chat.TokenWindowChatMemory;
import dev.langchain4j.model.azure.AzureOpenAiChatModel;
import dev.langchain4j.model.azure.AzureOpenAiStreamingChatModel;
import dev.langchain4j.model.embedding.EmbeddingModel;
import dev.langchain4j.model.input.PromptTemplate;
import dev.langchain4j.model.openai.OpenAiStreamingChatModel;
import dev.langchain4j.model.openai.OpenAiTokenizer;
import dev.langchain4j.retriever.Retriever;
import dev.langchain4j.service.AiServices;
import dev.langchain4j.service.TokenStream;
import jakarta.annotation.PostConstruct;
import lombok.RequiredArgsConstructor;
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
    private final Retriever<TextSegment> retriever;

    private final EmbeddingModel embeddingModel;

    @Value("${azure.openai.api.key}")
    private String AZURE_OPENAI_API_KEY;

    @Value("${azure.openai.api.base_url}")
    private String AZURE_OPENAI_API_BASE_URL;

    @Value("${azure.openai.api.version}")
    private String AZURE_OPENAI_API_VERSION;

    @Value("$openai.api.key}")
    private String OPENAI_API_KEY;

    private Assistant assistant;

    private StreamingAssistant azureStreamingAssistant;

    private StreamingAssistant streamingAssistant;

    interface Assistant {
        String chat(String message);
    }

    interface StreamingAssistant {
        TokenStream chat(String message);
    }

    @PostConstruct
    public void init() {

        var memory = TokenWindowChatMemory.withMaxTokens(2000, new OpenAiTokenizer("gpt-3.5-turbo"));

        assistant = AiServices.builder(Assistant.class)
                .chatLanguageModel(AzureOpenAiChatModel.builder()
                        .apiKey(AZURE_OPENAI_API_KEY)
                        .endpoint(AZURE_OPENAI_API_BASE_URL)
                        .serviceVersion(AZURE_OPENAI_API_VERSION)
                        .logRequestsAndResponses(true)
                        .build())
                .chatMemory(memory)
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
                .retriever(retriever)
                .tools(exampleTool)
                .build();


        streamingAssistant = AiServices.builder(StreamingAssistant.class)
                .streamingChatLanguageModel(OpenAiStreamingChatModel.builder()
                        .apiKey(OPENAI_API_KEY)
                        .build())
                .chatMemory(memory)
                .tools(exampleTool)
                .build();


    }

    public String chat(String chatId, String message) {
        Logger.info("Sending '{}' to chat engine in non-streaming mode", message);
        var response = assistant.chat(message);
        Logger.info("Response from chat engine is '{}", response);
        return response;
    }

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
        Logger.debug("Sending '{}' to chat engine using streaming mode", message);
        azureStreamingAssistant.chat(prompt.toUserMessage().text())
                .onNext(s -> {
                    sink.tryEmitNext(s);
                })
                .onComplete(response -> {
                    Logger.info("On complete: {}", response.content().text());
                    sink.tryEmitComplete();
                })
                .onError(t -> {
                    Logger.warn(t, "On error: exception occurred: {}", t.getMessage());
                    sink.tryEmitError(t);
                })
                .start();

        return sink.asFlux();
    }
}
