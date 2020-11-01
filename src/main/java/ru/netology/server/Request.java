package ru.netology.server;

import java.io.*;
import java.util.*;

public class Request {
    public static final String GET = "GET";
    public static final String POST = "POST";
    private static final List<String> allowedMethods = List.of(GET, POST);

    private final String method;
    private final String path;
    private final Map<String, String> headers;
    //    private final Map<String, String> queryParams;
    private final InputStream in;

    private Request(String method, String path, Map<String, String> headers, InputStream in) {
        this.method = method;
        this.path = path;
        this.headers = headers;
        this.in = in;
    }

    public String getMethod() {
        return method;
    }

    public String getPath() {
        return path;
    }

    public Map<String, String> getHeaders() {
        return headers;
    }

    public InputStream getIn() {
        return in;
    }

    public static Request fromInputStream(InputStream in) throws IOException {
        var stream = new BufferedInputStream(in);

        // лимит на request line + заголовки
        final var limit = 4096;

        stream.mark(limit);
        final var buffer = new byte[limit];
        final var read = stream.read(buffer);

        // ищем request line
        final var requestLineDelimiter = new byte[]{'\r', '\n'};
        final var requestLineEnd = indexOf(buffer, requestLineDelimiter, 0, read);
        if (requestLineEnd == -1) {
            throw new IOException("Invalid request");
        }

        // читаем request line
        final var requestLine = new String(Arrays.copyOf(buffer, requestLineEnd)).split(" ");
        if (requestLine.length != 3) {
            throw new IOException("Invalid request");
        }

        final var method = requestLine[0];
        if (!allowedMethods.contains(method)) {
            throw new IOException(String.format("Unsupported method in request (%s)", method));
        }

        final var path = requestLine[1];
        if (!path.startsWith("/")) {
            throw new IOException(String.format("Invalid path in request (%s)", path));
        }

        // ищем заголовки
        final var headersDelimiter = new byte[]{'\r', '\n', '\r', '\n'};
        final var headersStart = requestLineEnd + requestLineDelimiter.length;
        final var headersEnd = indexOf(buffer, headersDelimiter, headersStart, read);
        if (headersEnd == -1) {
            throw new IOException("Headers not found in request");
        }

        // отматываем на начало буфера
        stream.reset();
        // пропускаем requestLine
        stream.skip(headersStart);

        final var headersBytes = stream.readNBytes(headersEnd - headersStart);
        final var headerList = Arrays.asList(new String(headersBytes).split("\r\n"));

        Map<String, String> headers = new HashMap<>();
        for (String headerLine : headerList) {
            var i = headerLine.indexOf(":");
            var headerName = headerLine.substring(0, i);
            var headerValue = headerLine.substring(i + 2);
            headers.put(headerName, headerValue);
        }

        // для GET тела нет
        if (!method.equals(GET)) {
            stream.skip(headersDelimiter.length);
            // вычитываем Content-Length, чтобы прочитать body
            final var contentLength = Optional.ofNullable(headers.get("Content-Length"));
            if (contentLength.isPresent()) {
                final var length = Integer.parseInt(contentLength.get());
                final var bodyBytes = in.readNBytes(length);
                final var body = new String(bodyBytes);
                System.out.println(body);
            }
        }

        return new Request(method, path, headers, in);
    }

    // from google guava with modifications
    private static int indexOf(byte[] array, byte[] target, int start, int max) {
        outer:
        for (int i = start; i < max - target.length + 1; i++) {
            for (int j = 0; j < target.length; j++) {
                if (array[i + j] != target[j]) {
                    continue outer;
                }
            }
            return i;
        }
        return -1;
    }

    @Override
    public String toString() {
        return "Request{" +
                "method='" + method + '\'' +
                ", path='" + path + '\'' +
                ", headers=" + headers +
                '}';
    }
}
