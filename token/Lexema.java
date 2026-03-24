package token;

public class Lexema {
    private String token;
    private String nextChar;

    public Lexema() {
        this.token = "";
        this.nextChar = "";
    }

    public Lexema(String token, String nextChar) {
        this.token = token;
        this.nextChar = nextChar;
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
}
