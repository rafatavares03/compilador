package token;

public class Lexema {
    private String token;
    private String nextChar;
    private boolean valid;
    private String errorMsg;

    public Lexema() {
        this.token = "";
        this.nextChar = "";
        this.valid = true;
        this.errorMsg = "";
    }

    public Lexema(String token, String nextChar) {
        this.token = token;
        this.nextChar = nextChar;
        this.valid = true;
        this.errorMsg = "";
    }

    public String getToken() {
        return token;
    }

    public void setToken(String token) {
        this.token = token;
    }

    public String getNextChar() {
        return nextChar;
    }

    public void setNextChar(String nextChar) {
        this.nextChar = nextChar;
    }

    public boolean isValid() {
        return valid;
    }

    public void setInvalid() {
        this.valid = false;
    }

    public String getErrorMsg() {
        return this.errorMsg;
    }

    public void setErrorMsg(String message) {
        this.errorMsg = message;
    }
}
