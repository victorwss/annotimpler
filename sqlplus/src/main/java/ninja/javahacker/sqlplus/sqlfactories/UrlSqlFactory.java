package ninja.javahacker.sqlplus.sqlfactories;

import java.io.IOException;
import java.lang.reflect.Method;
import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.sql.SQLException;
import lombok.NonNull;
import ninja.javahacker.sqlplus.SqlFromUrl;
import ninja.javahacker.sqlplus.meta.SqlFactory;
import ninja.javahacker.sqlplus.meta.SqlSupplier;

public enum UrlSqlFactory implements SqlFactory {
    INSTANCE;

    @Override
    public SqlSupplier prepare(@NonNull Class<?> iface, @NonNull Method m) {
        var anno = m.getAnnotation(SqlFromUrl.class);
        if (anno == null) throw new UnsupportedOperationException();
        var value = anno.value();
        return () -> {
            try {
                return download(value);
            } catch (IOException e) {
                throw new SQLException(e);
            }
        };
    }

    private static String download(@NonNull String url) throws IOException {
        try (var client = HttpClient.newHttpClient()) {
            var request = HttpRequest.newBuilder().uri(URI.create(url)).GET().build();
            var response = client.send(request, HttpResponse.BodyHandlers.ofString());
            if (response.statusCode() >= 200 && response.statusCode() < 300) return response.body();
            throw new IOException("HTTP Error: " + response.statusCode());
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
            throw new IOException("Download was interrupted.", e);
        }
    }
}
