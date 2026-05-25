package exception;

import token.Token;

public class SyntaticException extends RuntimeException {
    private final Token token;
    public SyntaticException(String message, Token token) {
        super(message);
        this.token = token;
    }

    public Token getToken() {
        return this.token;
    }
}
