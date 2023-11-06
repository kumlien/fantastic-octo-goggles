package se.kumliens.chat.service;

import com.vaadin.flow.server.auth.AnonymousAllowed;
import dev.hilla.BrowserCallable;
import reactor.core.publisher.Flux;

@AnonymousAllowed
@BrowserCallable
public class TheChatService implements ChatService{
    @Override
    public String chat(String message) {
        return "hej";
    }

    @Override
    public Flux<String> chatStream(String message) {
        return Flux.just("hopp");
    }
}
