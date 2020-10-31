package ru.netology;

import ru.netology.server.Server;

import java.util.List;

public class Main {
    public static void main(String[] args) {
        var validPath = List.of("/index.html", "/spring.svg", "/spring.png", "/resources.html", "/styles.css", "/app.js", "/links.html", "/forms.html", "/classic.html", "/events.html", "/events.js");
        Server server = new Server(64, validPath);
        server.listen(9999);
    }
}


