package token;

import recursos.Tipos;

public class TokenID {
    private final Token token;
    private final Tipos tipo;
    private String valor;

    public TokenID(Token token,  Tipos tipo) {
        this.token = token;
        this.tipo = tipo;
        this.valor = null;
    }

    public TokenID(Token token, Tipos tipo, String valor) {
        this.token = token;
        this.tipo = tipo;
        this.valor = valor;
    }

    public Token getToken() {
        return token;
    }

    public Tipos getTipo() {
        return tipo;
    }

    public String getValor() {
        return valor;
    }

    public void setValor(String valor) {
        this.valor = valor;
    }
}
