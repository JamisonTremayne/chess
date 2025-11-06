package exception;

import com.google.gson.Gson;

import java.util.HashMap;
import java.util.Map;

public class RequestException extends Exception {

    public enum Code {
        BadRequestError,
        UnauthorizedError,
        AlreadyTakenError,
        ServerError,
        DataAccessError
    }

    private final Code code;

    public RequestException(String message, Code code) {
        super(message);
        this.code = code;
    }

    public static RequestException fromJson(String json) {
        var map = new Gson().fromJson(json, HashMap.class);
        var status = Code.valueOf(map.get("status").toString());
        String message = map.get("message").toString();
        return new RequestException(message, status);
    }

    public String toJson() {
        return new Gson().toJson(Map.of("message", getMessage(), "status", code));
    }


    public static Code fromHttpStatusCode(int httpStatusCode) {
        return switch (httpStatusCode) {
            case 500 -> Code.ServerError;
            case 400 -> Code.BadRequestError;
            case 401 -> Code.UnauthorizedError;
            case 403 -> Code.AlreadyTakenError;
            default -> throw new IllegalArgumentException("Unknown HTTP status code: " + httpStatusCode);
        };
    }

    public int toHttpStatusCode() {
        return switch (code) {
            case ServerError, DataAccessError -> 500;
            case BadRequestError -> 400;
            case UnauthorizedError -> 401;
            case AlreadyTakenError -> 403;
        };
    }
}
