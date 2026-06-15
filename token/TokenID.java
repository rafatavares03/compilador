package token;

import recursos.Tipos;

public class TokenID {
    private final Token token;
    private final Tipos tipo;
    private String valor;
    private boolean usada;

    public TokenID(Token token,  Tipos tipo) {
        this.token = token;
        this.tipo = tipo;
        this.valor = null;
        this.usada = false;
    }

    public TokenID(Token token, Tipos tipo, String valor) {
        this.token = token;
        this.tipo = tipo;
        this.valor = valor;
        this.usada = false;
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

    public void setUsada(boolean usada) {
        this.usada = usada;
    }

    public boolean getUsada() {
        return usada;
    }
}
