package ninja.javahacker.test.annotimpler.sql.factories;

import module java.base;

public class SimpleHttpServer implements AutoCloseable {

    private final Object lock;
    private final int port;
    private final ServerSocket serverSocket;
    private final Thread serverThread;
    private final Limits limits;
    private volatile boolean stopped;
    private final RequestHandler handler;

    private static record Limits(
            int maxSingleHeaderSize,
            int maxHeaderTotalSize,
            int maxHeaderCount)
    {
    }

    private SimpleHttpServer(int port, RequestHandler handler) throws IOException {
        this.lock = new Object();
        this.port = port;
        this.handler = handler;
        this.limits = new Limits(8 * 1024, 48 * 1024, 100);
        this.serverSocket = new ServerSocket(port);
        this.serverThread = new Thread(this::serve);
    }

    @Override
    public void close() throws IOException {
        synchronized (lock) {
            if (stopped) return;
            stopped = true;
            this.serverSocket.close();
        }
    }

    private void start() {
        this.serverThread.start();
    }

    private void serve() {
        if (this.serverThread != Thread.currentThread()) throw new AssertionError();
        try (var executor = Executors.newVirtualThreadPerTaskExecutor()) {
            System.out.println("Server started at http://localhost:" + port);

            while (true) {
                var client = serverSocket.accept();
                executor.submit(() -> receiveRequest(client));
            }
        } catch (IOException e) {
            System.err.println("Error handling server: " + e.getMessage());
        }
    }

    public static SimpleHttpServer start(int port, RequestHandler handler) throws IOException {
        var s = new SimpleHttpServer(port, handler);
        s.start();
        return s;
    }

    private static record HeaderLine(byte[] content, boolean completed, int size) {
        public String asString() {
            return new String(content, StandardCharsets.UTF_8);
        }

        private static HeaderLine read(InputStream in, int max) throws IOException {
            var b = new byte[max];
            int k;
            int end = 0;
            var slashR = false;
            var completed = false;

            for (k = 0; k < max; k++) {
                var a = in.read();
                if (a < 0) {
                    completed = true;
                    break;
                }
                b[k] = (byte) a;
                if (a == 10) {
                    end = slashR ? 2 : 1;
                    completed = true;
                    break;
                }
                slashR = (a == 13);
            }

            var c = new byte[k - end];
            System.arraycopy(b, 0, c, 0, k);
            return new HeaderLine(c, completed, k);
        }
    }

    private static record HeaderSet(List<HeaderLine> lines) {
        public boolean completed() {
            return lines.get(lines.size() - 1).completed();
        }

        private static HeaderSet read(InputStream in, Limits limits) throws IOException {
            var headers = new ArrayList<HeaderLine>(limits.maxHeaderCount());
            var soFar = 0;
            for (var i = 0; i < limits.maxHeaderCount(); i++) {
                var next = HeaderLine.read(in, Math.min(limits.maxSingleHeaderSize(), limits.maxHeaderTotalSize() - soFar));
                soFar += next.size();
                headers.add(next);
                if (next.content().length == 0) break;
            }
            return new HeaderSet(List.copyOf(headers));
        }
    }

    private static record Header(String name, String value) {
    }

    private static class MalformedHeaderException extends Exception {

        private static final long serialVersionUID = 1L;

        public MalformedHeaderException() {
        }
    }

    private static record HttpHeaders(String verb, String resource, String version, List<Header> headers) {
        private static HttpHeaders parse(HeaderSet set) throws MalformedHeaderException {
            if (!set.completed()) throw new MalformedHeaderException();
            var first = set.lines().get(0);
            var parts = first.asString().split(" ");
            if (parts.length != 3) throw new MalformedHeaderException();
            var headers = new ArrayList<Header>(set.lines().size() - 2);
            for (int i = 1; i < set.lines().size() - 1; i++) {
                var h = set.lines().get(i);
                var hparts = h.asString().split(": ", 2);
                if (hparts.length != 2) throw new MalformedHeaderException();
                var hname = hparts[0];
                if (hname.isEmpty() || hname.startsWith(" ") || hname.endsWith(" ")) throw new MalformedHeaderException();
                headers.add(new Header(hparts[0], hparts[1]));
            }
            return new HttpHeaders(parts[0], parts[1], parts[2], List.copyOf(headers));
        }
    }

    @FunctionalInterface
    public static interface RequestHandler {
        public void handle(Socket client, HttpHeaders headers, InputStream in, OutputStream out);
    }

    private void receiveRequest(Socket client) {
        try (
                client;
                var in = new BufferedInputStream(client.getInputStream());
                var out = client.getOutputStream()
        ) {
            try {
                // Read the request line (e.g., "GET / HTTP/1.1").
                var rawHeaders = HeaderSet.read(in, limits);
                if (!rawHeaders.completed()) {
                    output431(out);
                    return;
                }

                var formattedHeaders = HttpHeaders.parse(rawHeaders);
                handler.handle(client, formattedHeaders, in, out);

                // For this example, we serve a fixed file "index.html".
                /*var file = new File("index.html");
                if (!file.exists() || file.isDirectory()) {
                    output404(out);
                    return;
                }
                var content = Files.readAllBytes(file.toPath());
                output(out, "text/html; charset=utf-8", content);*/
            } catch (MalformedHeaderException e) {
                output400(out);
            } finally {
                out.flush();
            }
        } catch (IOException e) {
            System.err.println("Error handling client: " + e.getMessage());
        }
    }

    private static void output(OutputStream out, String contentType, byte[] content) throws IOException {
        var head = """
                   HTTP/1.1 200 OK
                   Content-Type: $X
                   Content-Length: $Y
                   $Z
                   """;
        var header = head.replace("$Z", "").replace("$Y", "" + content.length).replace("$X", contentType).replace("\n", "\r\n");
        out.write(header.getBytes(StandardCharsets.UTF_8));
        out.write(content);
    }

    private static void output404(OutputStream out) throws IOException {
        var head = """
                   HTTP/1.1 404 Not Found
                   $Z
                   File Not Found""";
        var header = head.replace("$Z", "").replace("\n", "\r\n");
        out.write(header.getBytes(StandardCharsets.UTF_8));
    }

    private static void output431(OutputStream out) throws IOException {
        var head = """
                   HTTP/1.1 431 Request Header Fields Too Large
                   $Z
                   Request Header Fields Too Large""";
        var header = head.replace("$Z", "").replace("\n", "\r\n");
        out.write(header.getBytes(StandardCharsets.UTF_8));
    }

    private static void output400(OutputStream out) throws IOException {
        var head = """
                   HTTP/1.1 400 Bad Request
                   $Z
                   Bad Reques""";
        var header = head.replace("$Z", "").replace("\n", "\r\n");
        out.write(header.getBytes(StandardCharsets.UTF_8));
    }
}
