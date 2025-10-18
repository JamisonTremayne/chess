package server;

import com.google.gson.Gson;
import dataaccess.*;
import datamodel.*;
import exception.RequestException;
import io.javalin.*;
import io.javalin.http.Context;
import request.*;
import response.*;
import service.*;

public class Server {

    private final Javalin server;
    private final UserService userService;
    private final GameService gameService;

    public Server() {
        DataAccess dataAccess = new MemoryDataAccess();
        server = Javalin.create(config -> config.staticFiles.add("web"));

        server.delete("db", this::clear);
        server.post("user", this::registerHandler);
        server.post("session", this::loginHandler);
        server.delete("session", this::logoutHandler);
        server.post("game", this::createGameHandler);

        userService = new UserService(dataAccess);
        gameService = new GameService(dataAccess);
    }

    public int run(int desiredPort) {
        server.start(desiredPort);
        return server.port();
    }

    public void stop() {
        server.stop();
    }

    private void clear(Context ctx) {
        userService.clear();
        ctx.result("{}");
    }

    private void registerHandler(Context ctx) {
        Gson serializer = new Gson();
        try {
            String requestJson = ctx.body();
            var user = serializer.fromJson(requestJson, UserData.class);
            LoginResponse loginResponse = userService.register(user);
            ctx.result(serializer.toJson(loginResponse));
        } catch (RequestException ex) {
            ctx.status(ex.toHttpStatusCode()).result(ex.toJson());
        }
    }

    private void loginHandler(Context ctx) {
        Gson serializer = new Gson();
        try {
            String requestJson = ctx.body();
            LoginRequest loginRequest = serializer.fromJson(requestJson, LoginRequest.class);
            LoginResponse loginResponse = userService.login(loginRequest);
            ctx.result(serializer.toJson(loginResponse));
        } catch (RequestException ex) {
            ctx.status(ex.toHttpStatusCode()).result(ex.toJson());
        }
    }

    private void logoutHandler(Context ctx) {
        Gson serializer = new Gson();
        try {
            String requestJson = ctx.header("authorization");
            LogoutRequest logoutRequest = serializer.fromJson(requestJson, LogoutRequest.class);
            userService.logout(logoutRequest);
            ctx.result("{}");
        } catch (RequestException ex) {
            ctx.status(ex.toHttpStatusCode()).result(ex.toJson());
        }
    }

    private void createGameHandler(Context ctx) {
        Gson serializer = new Gson();
        try {
            String jsonBody = ctx.body();
            var tempBody = serializer.fromJson(jsonBody, CreateGameRequestBody.class);
            String authToken = ctx.header("authorization");
            CreateGameRequest createGameRequest = new CreateGameRequest(tempBody.gameName(), authToken);
            CreateGameResponse response = gameService.createGame(createGameRequest);
            ctx.result(serializer.toJson(response));
        } catch (RequestException ex) {
            ctx.status(ex.toHttpStatusCode()).result(ex.toJson());
        }
    }
}
