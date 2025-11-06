import com.google.gson.Gson;
import datamodel.*;
import exception.RequestException;
import response.*;
import request.*;

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

    public LoginResponse register(UserData userData) throws RequestException {
        HttpRequest request = buildRequest("POST", "user", userData);
        HttpResponse<String> response = sendRequest(request);
        return handleResponse(response, LoginResponse.class);
    }

    public LoginResponse login(LoginRequest loginRequest) throws RequestException {
        HttpRequest request = buildRequest("POST", "session", loginRequest);
        HttpResponse<String> response = sendRequest(request);
        return handleResponse(response, LoginResponse.class);
    }

    public void logout(LogoutRequest logoutRequest) throws RequestException {
        HttpRequest request = buildRequest("DELETE", "session", logoutRequest);
        HttpResponse<String> response = sendRequest(request);
        handleResponse(response, null);
    }

    public ListGamesResponse listGames(ListGamesRequest listGamesRequest) throws RequestException {
        HttpRequest request = buildRequest("GET", "game", listGamesRequest);
        HttpResponse<String> response = sendRequest(request);
        return handleResponse(response, ListGamesResponse.class);
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
