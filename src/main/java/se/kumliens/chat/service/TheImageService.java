package se.kumliens.chat.service;

import com.vaadin.flow.server.auth.AnonymousAllowed;
import dev.hilla.BrowserCallable;
import dev.langchain4j.model.azure.AzureOpenAiImageModel;
import jakarta.annotation.PostConstruct;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.tinylog.Logger;

import java.io.IOException;
import java.io.InputStream;
import java.net.URI;
import java.util.Base64;
import java.util.Objects;

@AnonymousAllowed
@BrowserCallable
@Service
public class TheImageService implements ImageService {

    private AzureOpenAiImageModel aiModel;

    @Value("${azure.openai.api.key}")
    private String AZURE_OPENAI_API_KEY;

    @Value("${azure.openai.api.base_url}")
    private String AZURE_OPENAI_API_BASE_URL;

    @Value("${azure.openai.api.version}")
    private String AZURE_OPENAI_API_VERSION;


    @PostConstruct
    public void init() {
       aiModel = AzureOpenAiImageModel.builder()
                .endpoint(AZURE_OPENAI_API_BASE_URL)
                .deploymentName("Dalle3")
                .apiKey(AZURE_OPENAI_API_KEY)
                .logRequestsAndResponses(true)
                .build();
    }

    @Override
    public String generateURI(String prompt) {
        Logger.info("Will ask model to generate image for prompt '{}'", prompt);
        var response = aiModel.generate(prompt);
        Logger.info("Got a response back: {}", response);

        return response.content().url().toString();
    }

    /**
     * Generate an image and return the result as a Base64 encoded string.
     */
    @Override
    public String generateData(String prompt) {
        throw new RuntimeException("Not implemented...");
    }
}
