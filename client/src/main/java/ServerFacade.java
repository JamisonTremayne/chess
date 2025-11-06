import com.google.gson.Gson;
import datamodel.UserData;
import exception.RequestException;

import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpRequest.*;
import java.net.http.HttpResponse;
import java.net.http.HttpResponse.*;

public class ServerFacade {

    private final HttpClient client = HttpClient.newHttpClient();
    private final String serverUrl;

    public ServerFacade(String url) {
        serverUrl = url;
    }

    public UserData register(UserData userData) {
        HttpRequest request = buildRequest("POST", "user", userData);

        return null;
    }

    private HttpRequest buildRequest(String method, String path, Object body) {
        Builder request = HttpRequest.newBuilder()
                .uri(URI.create(serverUrl + path))
                .method(method, makeRequestBody(body));
        if (body != null) {
            request.setHeader("Content-Type", "application/json");
        }
        return request.build();
    }

    private BodyPublisher makeRequestBody(Object request) {
        if (request != null) {
            return BodyPublishers.ofString(new Gson().toJson(request));
        } else {
            return BodyPublishers.noBody();
        }
    }

    private HttpResponse<String> sendRequest(HttpRequest request) throws RequestException {
        try {
            return client.send(request, BodyHandlers.ofString());
        } catch (Exception ex) {
            throw new RequestException(ex.getMessage(), RequestException.Code.ServerError);
        }
    }

    private <T> T handleResponse(HttpResponse<String> response, Class<T> responseClass) throws RequestException {
        var status = response.statusCode();
        if (!isSuccessful(status)) {
            var body = response.body();
            if (body != null) {
                throw RequestException.fromJson(body);
            }

            throw new RequestException("other failure: " + status, RequestException.fromHttpStatusCode(status));
        }

        if (responseClass != null) {
            return new Gson().fromJson(response.body(), responseClass);
        }

        return null;
    }

    private boolean isSuccessful(int status) {
        return status / 100 == 2;
    }
}
