package se.kumliens.chat.service;

import reactor.core.publisher.Flux;


public interface ChatService {

    String chat(String chatId, String message);

    Flux<String> chatStream(String chatId, String message);
}
