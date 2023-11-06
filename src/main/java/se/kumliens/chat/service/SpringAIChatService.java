package se.kumliens.chat.service;

import com.vaadin.flow.server.auth.AnonymousAllowed;
import dev.hilla.BrowserCallable;
import org.springframework.stereotype.Service;
import reactor.core.publisher.Flux;

@BrowserCallable
@AnonymousAllowed
@Service
public class SpringAIChatService implements ChatService {
    @Override
    public String chat(String message) {
        return "dummy";
    }

    @Override
    public Flux<String> chatStream(String message) {
        return Flux.just("Hi", "\nthere");
    }
}
