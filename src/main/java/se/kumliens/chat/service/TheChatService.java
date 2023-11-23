package se.kumliens.chat.service;

import com.vaadin.flow.server.auth.AnonymousAllowed;
import dev.hilla.BrowserCallable;
import org.tinylog.Logger;
import reactor.core.publisher.Flux;

@AnonymousAllowed
@BrowserCallable
public class TheChatService implements ChatService {
    @Override
    public String chat(String message) {
        Logger.info("Dummy chat service, I'm a dummy!!");
        return "I'm a dummy chat service and you said '" + message + "'";
    }

    @Override
    public Flux<String> chatStream(String message) {
        Logger.info("Dummy Streaming chat service, I'm a dummy!");
        return Flux.just("I'm a\n", "dummy\n", "streaming\n", "chat service\n", "and you said\n", message);
    }
}
