package ru.netology;

import ru.netology.server.Handler;
import ru.netology.server.Server;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.time.LocalDateTime;
import java.util.List;

public class Main {
    public static void main(String[] args) {
        final var validPaths = List.of("/index.html", "/spring.svg", "/spring.png", "/resources.html", "/styles.css", "/app.js", "/links.html", "/forms.html", "/formspost.html", "/events.html", "/events.js");

        Server server = new Server(64);

        for (String path : validPaths) {
            server.addHandler("GET", path, Handler::defaultHandler);
            server.addHandler("POST", path, Handler::defaultHandler);
        }

        server.addHandler("GET", "/classic.html", (request, out) -> {
            try {
                final var filePath = Path.of(".", "public", request.getPath());
                final var mimeType = Files.probeContentType(filePath);

                // special case for classic
                final var template = Files.readString(filePath);
                final var content = template.replace(
                        "{time}",
                        LocalDateTime.now().toString()
                ).getBytes();
                out.write((
                        "HTTP/1.1 200 OK\r\n" +
                                "Content-Type: " + mimeType + "\r\n" +
                                "Content-Length: " + content.length + "\r\n" +
                                "Connection: close\r\n" +
                                "\r\n"
                ).getBytes());
                out.write(content);
                out.flush();
            } catch (IOException e) {
                e.printStackTrace();
            }
        });

        server.listen(9999);
    }
}


