package token;

import tipos.Tipos;

public class Token {
    private final String valor;
    private final Tipos tipo;
    private final int id;
    private final int linha;
    private final int coluna;

    public Token(String valor, Tipos tipo, int id, int linha, int coluna) {
        this.valor = valor;
        this. tipo = tipo;
        this. id = id;
        this.linha = linha;
        this.coluna = coluna;
    }
}
