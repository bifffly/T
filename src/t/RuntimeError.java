package t;

public class RuntimeError extends RuntimeException {
    final Token token;

    public RuntimeError(Token token, String message) {
        this(token, "Error", message);
    }

    public RuntimeError(Token token, String type, String message) {
        super(type + ": " + message);
        this.token = token;
    }
}
