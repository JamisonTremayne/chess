package server;

import com.google.gson.Gson;
import io.javalin.*;
import io.javalin.http.Context;

import java.util.Map;
import java.util.UUID;

public class Server {

    private final Javalin server;

    public Server() {
        server = Javalin.create(config -> config.staticFiles.add("web"));

        server.delete("db", ctx -> ctx.result("{}"));
        server.post("user", this::register);
    }

    public int run(int desiredPort) {
        server.start(desiredPort);
        return server.port();
    }

    public void stop() {
        server.stop();
    }

    public static String generateToken() {
        return UUID.randomUUID().toString();
    }

    private void register(Context ctx) {
        String requestJson = ctx.body();
        Gson serializer = new Gson();
        var req = serializer.fromJson(requestJson, Map.class);

        var res = Map.of("username", req.get("username"), "authToken", generateToken());
        ctx.result(serializer.toJson(res));
    }
}
