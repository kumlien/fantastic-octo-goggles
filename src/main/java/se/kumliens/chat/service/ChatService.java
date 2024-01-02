package se.kumliens.chat.service;

import com.vaadin.flow.server.auth.AnonymousAllowed;
import dev.hilla.BrowserCallable;
import dev.langchain4j.service.SystemMessage;
import reactor.core.publisher.Flux;


public interface ChatService {

    String chat(String message);

    @SystemMessage("""
            You are a professional hr specialist working at a it consultancy firm called IT-HUSET.
            You are helping the internal administrative employees to answer questions about the consultants working at IT-HUSET.
            You will always respond in a polite tone. 
            You will ask the user for more information if you need that in order to answer the question.
            Think step by step and take your time before answering any question.
            """)
    Flux<String> chatStream(String message);
}
