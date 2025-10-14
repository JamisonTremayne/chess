package server;

import com.google.gson.Gson;
import dataaccess.DataAccess;
import dataaccess.MemoryDataAccess;
import datamodel.*;
import io.javalin.*;
import io.javalin.http.Context;
import service.UserService;

import java.util.Map;
import java.util.UUID;

public class Server {

    private final Javalin server;
    private final UserService userService;

    public Server() {
        DataAccess dataAccess = new MemoryDataAccess();
        server = Javalin.create(config -> config.staticFiles.add("web"));

        server.delete("db", ctx -> ctx.result("{}"));
        server.post("user", this::register);

        userService = new UserService(dataAccess);
    }

    public int run(int desiredPort) {
        server.start(desiredPort);
        return server.port();
    }

    public void stop() {
        server.stop();
    }

    private void register(Context ctx) {
        String requestJson = ctx.body();
        Gson serializer = new Gson();
        var req = serializer.fromJson(requestJson, Map.class);

        var res = Map.of("username", req.get("username"), "authToken", "z");
        ctx.result(serializer.toJson(res));
    }

    private void login(Context ctx) {
        Gson serializer = new Gson();
        try {
            String requestJson = ctx.body();
            var user = serializer.fromJson(requestJson, UserData.class);

            AuthData authData = userService.register(user);

            ctx.result(serializer.toJson(authData));
        } catch (Exception ex) {
            String msg = String.format("{ \"message\"");
            ctx.status(403).result(msg);
        }
    }
}
