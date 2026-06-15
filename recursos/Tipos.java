package recursos;

public enum Tipos {
    INT,
    STRING,
    FLOAT,
    BOOLEAN,
    NULL,
    ERRO;

    public static Tipos fromString(String valor) {
        for(Tipos tipo: values()) {
            if(tipo.name().equalsIgnoreCase(valor)) {
                return tipo;
            }
        }
        throw new RuntimeException("Tipo inválido: " + valor);
    }
}
