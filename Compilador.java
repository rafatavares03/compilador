import frontend.AL;
import token.Token;

import java.io.File;
import java.util.Deque;

public class Compilador {

    public static void main(String[] args) {
        if(args.length == 0) {
            throw new RuntimeException("O caminho de um código fonte deve ser especificado.");
        }
        File codigoFonte = new File(args[0]);
        AL analisadorLexico = new AL();
        //AnalisadorLexico analisadorLexico = new AnalisadorLexico();
        Deque<Token> tokens = analisadorLexico.executarAnaliseLexica(codigoFonte);
        System.out.printf("%-5s %-40s %-20s %-5s %-5s%n", "ID", "TOKEN", "CLASSE", "LINHA", "COLUNA");
        for(Token token : tokens) {
            System.out.printf("%-5d %-40s %-20s %-5d %-5d%n",
                    token.getId(),
                    token.getValor(),
                    token.getTipo(),
                    token.getLinha(),
                    token.getColuna()
            );
        }
    }
}
