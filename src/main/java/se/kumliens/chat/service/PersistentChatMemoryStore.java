package se.kumliens.chat.service;

import dev.langchain4j.data.message.ChatMessage;
import dev.langchain4j.store.memory.chat.ChatMemoryStore;
import kotlin.collections.ArrayDeque;
import org.tinylog.Logger;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Basic chat memory store. Store everything in memory.
 */
public class PersistentChatMemoryStore implements ChatMemoryStore {

    private final Map<Object, List<ChatMessage>> store = new HashMap<>();
    @Override
    public List<ChatMessage> getMessages(Object memoryId) {
        Logger.info("Fetching messages for id {}", memoryId);
        return store.computeIfAbsent(memoryId, k -> new ArrayList<>());
    }

    @Override
    public void updateMessages(Object memoryId, List<ChatMessage> messages) {
        Logger.info("Updating messages for id {}: {}", memoryId, messages);
        store.put(memoryId, messages);
    }

    @Override
    public void deleteMessages(Object memoryId) {
        Logger.info("Deleting messages for id {}", memoryId);
        store.remove(memoryId);
    }
}
