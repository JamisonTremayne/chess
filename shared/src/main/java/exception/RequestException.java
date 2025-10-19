package exception;

import com.google.gson.Gson;
import java.util.Map;

public class RequestException extends Exception {

    public enum Code {
        BadRequestError,
        UnauthorizedError,
        AlreadyTakenError,
        ServerError
    }

    private final Code code;

    public RequestException(String message, Code code) {
        super(message);
        this.code = code;
    }

    public String toJson() {
        return new Gson().toJson(Map.of("message", getMessage(), "status", code));
    }

    public int toHttpStatusCode() {
        return switch (code) {
            case ServerError -> 500;
            case BadRequestError -> 400;
            case UnauthorizedError -> 401;
            case AlreadyTakenError -> 403;
        };
    }
}
