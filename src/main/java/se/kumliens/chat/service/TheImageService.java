package se.kumliens.chat.service;

import com.vaadin.flow.server.auth.AnonymousAllowed;
import dev.hilla.BrowserCallable;

import java.io.IOException;
import java.io.InputStream;
import java.net.URI;
import java.util.Base64;
import java.util.Objects;

@AnonymousAllowed
@BrowserCallable
public class TheImageService implements ImageService {
    @Override
    public String generateURI(String prompt) {
        return URI.create("https://2.bp.blogspot.com/-IN8RAQxEjy0/U6XS5n3YeVI/AAAAAAAABjg/xtHoCOklAkA/s1600/moma_magritte_treacheryofimages_.jpg").toString();
    }

    /**
     * Generate an image and return the result as a Base64 encoded string.
     */
    @Override
    public String generateData(String prompt) {
        try (InputStream in = getClass().getResourceAsStream("/pipa.jpeg")) {
            return new String(Base64.getEncoder().encode(Objects.requireNonNull(in, "Didn't find the dummy image...").readAllBytes()));
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }
}
