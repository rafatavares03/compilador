import frontend.AL;
import frontend.AnalisadorLexico;

import java.io.File;

public class Compilador {

    public static void main(String[] args) {
        if(args.length == 0) {
            throw new RuntimeException("O caminho de um código fonte deve ser especificado.");
        }
        File codigoFonte = new File(args[0]);
        AL analisadorLexico = new AL();
        //AnalisadorLexico analisadorLexico = new AnalisadorLexico();
        analisadorLexico.executarAnaliseLexica(codigoFonte);
    }
}
