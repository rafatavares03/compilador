package frontend;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
import java.util.regex.Pattern;

public class AnalisadorLexico {
    private int linha;
    private int coluna;
    private final File codigoFonte;
    private final FileReader fileReader;
    Pattern separador = Pattern.compile("\r?\n|[\t (){};\\[\\]]");
    Pattern literal = Pattern.compile("\'|\"");
    Pattern comentario = Pattern.compile("//|/\\*");

    public AnalisadorLexico(File codigoFonte) {
        this.codigoFonte = codigoFonte;
        try {
            this.fileReader = new FileReader(codigoFonte);
        } catch (FileNotFoundException e) {
            throw new RuntimeException(e);
        }
    }

    boolean ehSeparador(String str) {
        return separador.matcher(str).matches();
    }

    boolean ehLiteral(String str) {
        return literal.matcher(str).matches();
    }

    boolean ehComentario(String str) {
        return comentario.matcher(str).matches();
    }

    public void separadorHandler(String str) {
        if(Pattern.matches("[(){};\\[\\]]", str)) {
            System.out.println(str + " " + this.linha + " " + this.coluna);
        }
        if(Pattern.matches("\r?\n", str)) {
            novaLinha();
        }
    }

    public void literalHandler(String str) {
        if(Pattern.matches("\'", str)) {
            int charByte = lerNovoCaractere();
            str += String.valueOf((char)charByte);
            if(!Pattern.matches("\'\'", str)){
                charByte = lerNovoCaractere();
                str += String.valueOf((char)charByte);
            }
        } else if(Pattern.matches("\"", str)) {
            int charByte;
            StringBuilder strBuilder = new StringBuilder(str);
            while((charByte = lerNovoCaractere()) != -1) {
                String caractere = String.valueOf((char)charByte);

                if(caractere.charAt(0) == '\\') {
                    charByte = lerNovoCaractere();
                    String aux = String.valueOf((char)charByte);
                    if(Pattern.matches("\"", aux)) {
                        strBuilder.append(aux);
                    } else {
                        strBuilder.append(caractere);
                        strBuilder.append(aux);
                    }
                } else {
                    strBuilder.append(caractere);
                }

                if(Pattern.matches("\"", caractere)) {
                    break;
                }
            }
            str = strBuilder.toString();
        }
        System.out.println(str + " " + linha + " " + (coluna - str.length() + 1));
    }

    public void comentarioHandler(String str) {
        int charByte;
        if(Pattern.matches("//", str)) {
            while((charByte = lerNovoCaractere()) != -1) {
                if((char)charByte == '\n') {
                    break;
                }
            }
            novaLinha();
        }
        if(Pattern.matches("/\\*", str)) {
            while((charByte = lerNovoCaractere()) != -1) {
                if((char)charByte == '\n') {
                    novaLinha();
                }
                if((char)charByte == '*') {
                    charByte = lerNovoCaractere();
                    if((char)charByte == '/') {
                        break;
                    }
                }
            }
        }
    }

    public void executarAnaliseLexica() {
        this.linha = 1;
        this.coluna = 0;
        int charByte;
        while((charByte = lerNovoCaractere()) != -1) {
            String character = String.valueOf((char)charByte);

            if(Pattern.matches("\r", character)) {
                if((charByte = lerNovoCaractere()) != -1) {
                    character += String.valueOf((char)charByte);
                }
            }

            if(Pattern.matches("/", character)) {
                if((charByte = lerNovoCaractere()) != -1) {
                    character += String.valueOf((char)charByte);
                }
                if(ehComentario(character)) {
                    comentarioHandler(character);
                }
            }

            if(ehSeparador(character)) {
                separadorHandler(character);
            }

            if(ehLiteral(character)) {
                literalHandler(character);
            }
        }
    }

    private void novaLinha() {
        this.linha++;
        this.coluna = 0;
    }

    private int lerNovoCaractere() {
        int charByte = -1;
        try{
            charByte = fileReader.read();
            this.coluna++;
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
        return charByte;
    }

}
