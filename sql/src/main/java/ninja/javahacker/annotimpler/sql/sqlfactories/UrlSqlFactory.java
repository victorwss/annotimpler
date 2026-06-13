package ninja.javahacker.annotimpler.sql.sqlfactories;

import edu.umd.cs.findbugs.annotations.SuppressFBWarnings;
import lombok.NonNull;

import module java.base;
import module java.net.http;
import module ninja.javahacker.annotimpler.sql;

/// Singleton [SqlFactory] that downloads the SQL string from an HTTP/HTTPS URL, as specified by a
/// [SqlFromUrl]-annotated method.
/// The download strategy (eager, lazy, etc.) is controlled by [SqlFromUrl#policy()].
/// Character encoding is detected from the HTTP `Content-Type` response header when
/// [SqlFromUrl#getEncodingFromHeaders()] is `true`; otherwise the [SqlFromUrl#fallbackEncoding()] is used.
@SuppressFBWarnings("ENMI_ONE_ENUM_VALUE")
public enum UrlSqlFactory implements SqlFactory {

    /// The sole instance of this factory.
    INSTANCE;

    /// Returns a [SqlSupplier] that downloads the SQL from the URL specified in the [SqlFromUrl] annotation
    /// on `m`, according to the configured policy.
    ///
    /// @param m The method carrying the [SqlFromUrl] annotation.
    /// @return A [SqlSupplier] that supplies the downloaded SQL string.
    /// @throws BadImplementationException If the URL is invalid or unreachable (for eager read policies).
    /// @throws UnsupportedOperationException If `m` has no [SqlFromUrl] annotation.
    /// @throws IllegalArgumentException If `m` is `null`.
    @Override
    public SqlSupplier prepare(@NonNull Method m) throws BadImplementationException {
        var anno = m.getAnnotation(SqlFromUrl.class);
        if (anno == null) throw new UnsupportedOperationException();
        return anno.policy().prepare(UrlSqlFactory::download, anno);
    }

    private static String download(@NonNull SqlFromUrl anno) throws IOException {
        checkNotNull(anno);
        var key = "charset=";
        var url = anno.value();
        try (var client = HttpClient.newHttpClient()) {
            HttpRequest request;
            try {
                request = HttpRequest.newBuilder().uri(URI.create(url)).GET().build();
            } catch (IllegalArgumentException e) {
                throw new IOException(e);
            }
            HttpResponse<byte[]> response;
            try {
                response = client.send(request, HttpResponse.BodyHandlers.ofByteArray());
            } catch (IOException e) {
                if (e.getMessage() == null) throw new IOException("Download failed. Server didn't answer.", e.getCause());
                throw e;
            }
            var status = response.statusCode();
            if (isNotFound(status)) throw new FileNotFoundException(url);
            if (!isStatusOk(response.statusCode())) throw new IOException("HTTP Error: " + response.statusCode());
            var contentType = response.headers().firstValue("Content-Type").orElse("charset=UTF-8");
            var idx = contentType.indexOf(key);
            CharsetSpec spec;
            if (idx >= 0 && anno.getEncodingFromHeaders()) {
                 var charsetName = contentType.substring(idx + key.length());
                 var charset = Charset.forName(charsetName);
                 spec = () -> charset;
            } else {
                spec = CharsetSpec.instance(anno.fallbackEncoding());
            }
            return spec.decode(response.body());
        } catch (InterruptedException e) {
            throw new IOException("Download was interrupted.", e);
        }
    }

    private static boolean isStatusOk(int status) {
        return status / 100 == 2;
    }

    private static boolean isNotFound(int status) {
        return status == 404;
    }

    @Generated
    private static void checkNotNull(Object obj) {
        if (obj == null) throw new AssertionError();
    }
}
