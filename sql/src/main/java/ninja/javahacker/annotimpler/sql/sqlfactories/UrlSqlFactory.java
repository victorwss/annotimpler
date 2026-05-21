package ninja.javahacker.annotimpler.sql.sqlfactories;

import lombok.NonNull;

import module java.base;
import module java.net.http;
import module ninja.javahacker.annotimpler.sql;

public enum UrlSqlFactory implements SqlFactory {
    INSTANCE;

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
            var request = HttpRequest.newBuilder().uri(URI.create(url)).GET().build();
            var response = client.send(request, HttpResponse.BodyHandlers.ofByteArray());
            if (!isStatusOk(response.statusCode())) throw new IOException("HTTP Error: " + response.statusCode());
            var contentType = response.headers().firstValue("Content-Type").orElse("charset=UTF-8");
            var idx = contentType.indexOf(key);
            Charset charset;
            if (idx >= 0) {
                 var charsetName = contentType.substring(idx + key.length());
                 charset = Charset.forName(charsetName);
            } else {
                charset = CharsetSpec.from(anno.fallbackEncoding());
            }
            return BytesToStringSupport.make(response.body(), charset);
        } catch (InterruptedException e) {
            throw new IOException("Download was interrupted.", e);
        }
    }

    private static boolean isStatusOk(int status) {
        return status / 100 == 2;
    }

    @Generated
    private static void checkNotNull(Object obj) {
        if (obj == null) throw new AssertionError();
    }
}
