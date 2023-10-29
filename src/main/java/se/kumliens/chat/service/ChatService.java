package se.kumliens.chat.service;

import com.vaadin.flow.server.auth.AnonymousAllowed;
import dev.hilla.BrowserCallable;
import reactor.core.publisher.Flux;


public interface ChatService {

    String chat(String message);

    Flux<String> chatStream(String message);
}
