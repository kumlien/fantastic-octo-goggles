package se.kumliens.chat.service;

import com.vaadin.flow.server.auth.AnonymousAllowed;
import dev.hilla.BrowserCallable;
import dev.langchain4j.memory.chat.TokenWindowChatMemory;
import dev.langchain4j.model.azure.AzureOpenAiChatModel;
import dev.langchain4j.model.azure.AzureOpenAiStreamingChatModel;
import dev.langchain4j.model.openai.OpenAiTokenizer;
import dev.langchain4j.service.AiServices;
import dev.langchain4j.service.TokenStream;
import jakarta.annotation.PostConstruct;
import org.apache.juli.logging.Log;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.tinylog.Logger;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Sinks;
import se.kumliens.chat.tools.ExampleTool;

@BrowserCallable
@AnonymousAllowed
@Service
public class TheChatService implements ChatService {

    @Autowired
    private ExampleTool exampleTool;

    @Value("${azure.openai.api.key}")
    private String AZURE_OPENAI_API_KEY;

    @Value("${azure.openai.api.base_url}")
    private String AZURE_OPENAI_API_BASE_URL;

    @Value("${azure.openai.api.version}")
    private String AZURE_OPENAI_API_VERSION;

    private Assistant assistant;

    private StreamingAssistant azureStreamingAssistant;

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
                        .baseUrl(AZURE_OPENAI_API_BASE_URL)
                        .apiVersion(AZURE_OPENAI_API_VERSION)
                        .logRequests(true)
                        .logResponses(true)
                        .build())
                .chatMemory(memory)
                .tools(exampleTool)
                .build();

        azureStreamingAssistant = AiServices.builder(StreamingAssistant.class)
                .streamingChatLanguageModel(AzureOpenAiStreamingChatModel.builder()
                        .apiKey(AZURE_OPENAI_API_KEY)
                        .baseUrl(AZURE_OPENAI_API_BASE_URL)
                        .apiVersion(AZURE_OPENAI_API_VERSION)
                        .logRequests(true)
                        .logResponses(true)
                        .build())
                .chatMemory(memory)
                .tools(exampleTool)
                .build();
    }

    public String chat(String message) {
        Logger.info("Sending '{}' to chat engine in non-streaming mode", message);
        var response = assistant.chat(message);
        Logger.info("Response from chat engine is '{}", response);
        return response;
    }

    public Flux<String> chatStream(String message) {
        Sinks.Many<String> sink = Sinks.many().unicast().onBackpressureBuffer();
        Logger.info("Sending '{}' to chat engine using streaming mode", message);
        azureStreamingAssistant.chat(message)
                .onNext(s -> {
                    Logger.info("On next: got a chunk of the response: '{}'", s);
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
