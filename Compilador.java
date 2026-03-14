import frontend.AnalisadorLexico;

public class Compilador {
    private final static AnalisadorLexico analisadorLexico = new AnalisadorLexico();

    public static void main(String[] args) {
        if(args.length == 0) {
            throw new RuntimeException("O caminho de um código fonte deve ser especificado.");
        }
        analisadorLexico.analisarCodigoFonte(args[0]);
    }
}
