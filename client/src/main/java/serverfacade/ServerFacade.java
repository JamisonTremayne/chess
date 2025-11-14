package serverfacade;

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
        HttpRequest request = buildRequest("POST", "/user", userData);
        HttpResponse<String> response = sendRequest(request);
        return handleResponse(response, LoginResponse.class);
    }

    public LoginResponse login(LoginRequest loginRequest) throws RequestException {
        HttpRequest request = buildRequest("POST", "/session", loginRequest);
        HttpResponse<String> response = sendRequest(request);
        return handleResponse(response, LoginResponse.class);
    }

    public void logout(LogoutRequest logoutRequest) throws RequestException {
        HttpRequest request = buildRequest("DELETE", "/session", null,
                "authorization", logoutRequest.authToken());
        HttpResponse<String> response = sendRequest(request);
        handleResponse(response, null);
    }

    public ListGamesResponse listGames(ListGamesRequest listGamesRequest) throws RequestException {
        HttpRequest request = buildRequest("GET", "/game", null,
                "authorization", listGamesRequest.authToken());
        HttpResponse<String> response = sendRequest(request);
        return handleResponse(response, ListGamesResponse.class);
    }

    public CreateGameResponse createGame(CreateGameRequest createGameRequest) throws RequestException {
        CreateGameRequestBody body = new CreateGameRequestBody(createGameRequest.gameName());
        HttpRequest request = buildRequest("POST", "/game", body,
                "authorization", createGameRequest.authToken());
        HttpResponse<String> response = sendRequest(request);
        return handleResponse(response, CreateGameResponse.class);
    }

    public void joinGame(JoinGameRequest joinGameRequest) throws RequestException {
        JoinGameRequestBody body = new JoinGameRequestBody(joinGameRequest.playerColor(), joinGameRequest.gameID());
        HttpRequest request = buildRequest("PUT", "/game", body,
                "authorization", joinGameRequest.authToken());
        HttpResponse<String> response = sendRequest(request);
        handleResponse(response, null);
    }

    public void clear() throws RequestException {
        HttpRequest request = buildRequest("DELETE", "/db", null);
        HttpResponse<String> response = sendRequest(request);
        handleResponse(response, null);
    }

    private HttpRequest buildRequest(String method, String path, Object body) {
        Builder request = HttpRequest.newBuilder()
                .uri(URI.create(serverUrl + path))
                .method(method, makeRequestBody(body));
        return request.build();
    }

    private HttpRequest buildRequest(String method, String path, Object body, String headerName, String header) {
        Builder request = HttpRequest.newBuilder()
                .uri(URI.create(serverUrl + path))
                .method(method, makeRequestBody(body))
                .header(headerName, header);
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
            throw new RequestException("Cannot connect to the server. " +
                    "Please make sure the server is running before continuing.", RequestException.Code.ServerError);
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
