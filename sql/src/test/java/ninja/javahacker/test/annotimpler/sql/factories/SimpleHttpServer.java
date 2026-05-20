package ninja.javahacker.test.annotimpler.sql.factories;

import module java.base;

public class SimpleHttpServer implements AutoCloseable {

    private final Object lock;
    private final int port;
    private volatile Thread serverThread;
    private final Limits limits;
    private final RequestHandler handler;
    private final ServerSocket serverSocket;
    private volatile boolean stopped;

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
        this.serverThread = null;
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
        synchronized (lock) {
            this.serverThread = Thread.startVirtualThread(this::serve);
        }
    }

    public static SimpleHttpServer start(int port, RequestHandler handler) throws IOException {
        var s = new SimpleHttpServer(port, handler);
        s.start();
        return s;
    }

    private void serve() {
        synchronized (lock) {
            if (this.serverThread != Thread.currentThread()) throw new AssertionError();
        }
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

    private static record HeaderLine(byte[] content, boolean completed, int size) {
        public String asString() {
            return new String(content, StandardCharsets.UTF_8);
        }

        private static HeaderLine read(Input in, int max) throws IOException {
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

        private static HeaderSet read(Input in, Limits limits) throws IOException {
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

    public static record HttpRequestHeaders(String verb, String resource, String version, List<Header> headers) {
        private static HttpRequestHeaders parse(HeaderSet set) throws MalformedHeaderException {
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
            return new HttpRequestHeaders(parts[0], parts[1], parts[2], List.copyOf(headers));
        }
    }

    @FunctionalInterface
    public static interface RequestHandler {
        public void handle(Socket client, HttpRequestHeaders headers, Input in, Output out) throws IOException;
    }

    @FunctionalInterface
    public static interface Input {
        public int read() throws IOException;

        public default byte[] read(int length) throws IOException {
            var b = new byte[length];
            int i;
            for (i = 0; i < length; i++) {
                var r = read();
                if (r < 0) break;
                b[i] = (byte) r;
            }
            if (i == length) return b;
            var b2 = new byte[i];
            System.arraycopy(b, 0, b2, 0, i);
            return b2;
        }

        public static Input from(InputStream in) {
            return () -> in.read();
        }
    }

    @FunctionalInterface
    public static interface Output {
        public void write(byte value) throws IOException;

        public default void write(byte[] values) throws IOException {
            for (var e : values) {
                write(e);
            }
        }

        public static Output from(OutputStream out) {
            return b -> out.write(b);
        }
    }

    private void receiveRequest(Socket client) {
        System.out.println("Received connection.");
        try (
                client;
                var in = new BufferedInputStream(client.getInputStream());
                var out = new BufferedOutputStream(client.getOutputStream())
        ) {
            var iout = Output.from(out);
            var iin = Input.from(in);
            try {
                handleRequest(client, iin, iout);
            } finally {
                out.flush();
            }
        } catch (IOException e) {
            System.err.println("Error handling client: " + e.getMessage());
        }
        System.out.println("Finished connection.");
    }

    private void handleRequest(Socket client, Input in, Output out) throws IOException {
        var rawHeaders = HeaderSet.read(in, limits);
        if (!rawHeaders.completed()) {
            output431(out);
            System.out.println("Oops - 431.");
            return;
        }

        HttpRequestHeaders formattedHeaders;
        try {
            formattedHeaders = HttpRequestHeaders.parse(rawHeaders);
        } catch (MalformedHeaderException e) {
            output400(out);
            System.out.println("Oops - 400.");
            return;
        }
        System.out.println("Headers ok. " + formattedHeaders.verb() + " - " + formattedHeaders.resource());

        try {
            handler.handle(client, formattedHeaders, in, out);
        } catch (Throwable e) {
            e.printStackTrace(System.err);
            System.out.println("Oops - 500.");
            output500(out);
            return;
        }
    }

    public static void output400(Output out) throws IOException {
        var head = """
                   HTTP/1.1 400 Bad Request
                   $Z
                   Bad Request""";
        var header = head.replace("$Z", "").replace("\n", "\r\n");
        out.write(header.getBytes(StandardCharsets.UTF_8));
    }

    public static void output404(Output out) throws IOException {
        var head = """
                   HTTP/1.1 404 Not Found
                   $Z
                   File Not Found""";
        var header = head.replace("$Z", "").replace("\n", "\r\n");
        out.write(header.getBytes(StandardCharsets.UTF_8));
    }

    public static void output431(Output out) throws IOException {
        var head = """
                   HTTP/1.1 431 Request Header Fields Too Large
                   $Z
                   Request Header Fields Too Large""";
        var header = head.replace("$Z", "").replace("\n", "\r\n");
        out.write(header.getBytes(StandardCharsets.UTF_8));
    }

    public static void output500(Output out) throws IOException {
        var head = """
                   HTTP/1.1 500 Internal Server Error
                   $Z
                   Internal Server Error""";
        var header = head.replace("$Z", "").replace("\n", "\r\n");
        out.write(header.getBytes(StandardCharsets.UTF_8));
    }

    public static record Content(String contentType, byte[] content) {
        public void output(Output out) throws IOException {
            var head = """
                       HTTP/1.1 200 OK
                       Content-Type: $X
                       Content-Length: $Y
                       $Z
                       $Z""";
            var header = head.replace("$Z", "").replace("$Y", "" + content.length).replace("$X", contentType).replace("\n", "\r\n");
            out.write(header.getBytes(StandardCharsets.UTF_8));
            out.write(content);
        }
    }

    public static RequestHandler staticFiles(Map<String, ? extends Supplier<Content>> files) {
        return (client, headers, in, out) -> {
            var name = headers.resource();
            var sup = files.get(name);
            if (sup == null) sup = () -> null;
            var c = sup.get();
            if (c == null) {
                System.out.println("Oops - 404.");
                output404(out);
                return;
            }
            System.out.println("Ok - 200");
            c.output(out);
        };
    }

    /*
        Still lacking:
        * Response headers
        * Date headers
        * keep-alive
        * cache control
        * content negotiation
        * conditional get
        * chunking
        * status codes
        * percent encoding
        * query string
        * form data
        * multipart upload
        * gzip
        * TLS
        * redirect
        * CORS
        * QUIC
        * HTTP 2
        * HTTP 3
        * Special handling for ETag, Cookies, User-Agent, referer, etc.
        * Probably a lot more...
    */
}
