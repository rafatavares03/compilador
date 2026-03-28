package token;

import tipos.Tipos;

public record Token(String valor, Tipos tipo, int id, int linha, int coluna) {
}
