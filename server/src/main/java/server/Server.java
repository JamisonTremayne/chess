package server;

import com.google.gson.Gson;
import dataaccess.DataAccess;
import dataaccess.MemoryDataAccess;
import datamodel.*;
import exception.RequestException;
import io.javalin.*;
import io.javalin.http.Context;
import service.UserService;

import java.util.Map;

public class Server {

    private final Javalin server;
    private final UserService userService;

    public Server() {
        DataAccess dataAccess = new MemoryDataAccess();
        server = Javalin.create(config -> config.staticFiles.add("web"));

        server.delete("db", this::clear);
        server.post("user", this::register);
        server.post("session", this::login);
        server.delete("session", this::logout);

        userService = new UserService(dataAccess);
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
            var userInfo = serializer.fromJson(requestJson, UserData.class);
            AuthData authData = userService.login(userInfo);
            ctx.result(serializer.toJson(authData));
        } catch (RequestException ex) {
            ctx.status(ex.toHttpStatusCode()).result(ex.toJson());
        }
    }

    private void logout(Context ctx) {
        Gson serializer = new Gson();
        try {
            String requestJson = ctx.header("authorization");
            String authToken = serializer.fromJson(requestJson, String.class);
            userService.logout(authToken);
            ctx.result("{}");
        } catch (RequestException ex) {
            ctx.status(ex.toHttpStatusCode()).result(ex.toJson());
        }
    }
}
