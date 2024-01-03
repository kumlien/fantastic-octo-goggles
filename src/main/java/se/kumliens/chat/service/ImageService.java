package se.kumliens.chat.service;

import java.io.IOException;

public interface ImageService {

    String generateURI(String prompt);

    String generateData(String prompt) throws IOException;

}
