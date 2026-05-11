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
        var key = "charset=";
        var url = anno.value();
        try (var client = HttpClient.newHttpClient()) {
            var request = HttpRequest.newBuilder().uri(URI.create(url)).GET().build();
            var response = client.send(request, HttpResponse.BodyHandlers.ofByteArray());
            if (response.statusCode() >= 400) throw new IOException("HTTP Error: " + response.statusCode());
            var contentType = response.headers().firstValue("Content-Type").orElse("charset=UTF-8");
            var idx = contentType.indexOf(key);
            Charset charset;
            if (idx >= 0) {
                 var charsetName = contentType.substring(idx + key.length());
                 charset = Charset.forName(charsetName);
            } else {
                charset = CharsetSpec.from(anno.fallbackEncoding());
            }
            var cb = charset.newDecoder().onUnmappableCharacter(CodingErrorAction.REPORT).decode(ByteBuffer.wrap(response.body()));
            return new String(cb.array());
        } catch (InterruptedException e) {
            throw new IOException("Download was interrupted.", e);
        }
    }
}
