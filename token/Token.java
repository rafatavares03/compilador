package token;

import recursos.Recursos;

public record Token(String valor, Recursos tipo, int id, int linha, int coluna) {
}
