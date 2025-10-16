package server;

import com.google.gson.Gson;
import dataaccess.DataAccess;
import dataaccess.MemoryDataAccess;
import datamodel.*;
import exception.RequestException;
import io.javalin.*;
import io.javalin.http.Context;
import request.CreateGameRequest;
import request.LoginRequest;
import request.LogoutRequest;
import service.*;

public class Server {

    private final Javalin server;
    private final UserService userService;
    private final GameService gameService;

    public Server() {
        DataAccess dataAccess = new MemoryDataAccess();
        server = Javalin.create(config -> config.staticFiles.add("web"));

        server.delete("db", this::clear);
        server.post("user", this::register);
        server.post("session", this::login);
        server.delete("session", this::logout);
        server.post("game", this::createGame);

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

    private void register(Context ctx) {
        Gson serializer = new Gson();
        try {
            String requestJson = ctx.body();
            var user = serializer.fromJson(requestJson, UserData.class);
            AuthData authData = userService.register(user);
            ctx.result(serializer.toJson(authData));
        } catch (RequestException ex) {
            ctx.status(ex.toHttpStatusCode()).result(ex.toJson());
        }
    }

    private void login(Context ctx) {
        Gson serializer = new Gson();
        try {
            String requestJson = ctx.body();
            LoginRequest loginRequest = serializer.fromJson(requestJson, LoginRequest.class);
            AuthData authData = userService.login(loginRequest);
            ctx.result(serializer.toJson(authData));
        } catch (RequestException ex) {
            ctx.status(ex.toHttpStatusCode()).result(ex.toJson());
        }
    }

    private void logout(Context ctx) {
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

    private void createGame(Context ctx) {
        Gson serializer = new Gson();
        try {
            String jsonBody = ctx.body();
            String jsonHead = ctx.header("authorization");
            CreateGameRequest createGameRequest = serializer.fromJson(jsonBody + jsonHead, CreateGameRequest.class);
            int gameID = gameService.createGame(createGameRequest);
            ctx.result(serializer.toJson(gameID));
        } catch (RequestException ex) {
            ctx.status(ex.toHttpStatusCode()).result(ex.toJson());
        }
    }
}
