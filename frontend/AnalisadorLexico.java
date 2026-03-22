package frontend;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
import java.util.regex.Pattern;

public class AnalisadorLexico {
    private int linha;
    private int coluna;
    private final FileReader fileReader;
    Pattern separador = Pattern.compile("\r?\n|[\t (){};\\[\\]]");
    Pattern literal = Pattern.compile("['\"]");
    Pattern comentario = Pattern.compile("//|/\\*");
    Pattern identificador = Pattern.compile("[_a-zA-Z]");
    Pattern operador = Pattern.compile("\\+\\+?|--?|&&?|\\|\\|?|[*/]|(=[=+-]?)|[<>!]=?");
    Pattern numerico = Pattern.compile("[0-9]+(\\.[0-9]+)?");

    public AnalisadorLexico(File codigoFonte) {
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

    boolean ehIdentificador(String str) {
        return identificador.matcher(str).matches();
    }

    boolean ehOperador(String str) {
        return operador.matcher(str).matches();
    }

    boolean ehNumerico(String str) {
        return numerico.matcher(str).matches();
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

    public void identificadorHandler(String str) {
        StringBuilder stringBuilder = new StringBuilder(str);
        int charByte;
        while((charByte = lerNovoCaractere()) != -1) {
            String character = String.valueOf((char)charByte);

            if((char)charByte == '\r') {
                charByte = lerNovoCaractere();
                character += String.valueOf((char)charByte);
            }
            if(ehSeparador(character) || ehOperador(character)) {
                str = stringBuilder.toString();
                System.out.println(str + " " + linha + " " + (coluna - str.length()));
                identificarTipo(character);
                break;
            }

            stringBuilder.append(character);
        }
    }

    public void operadorHandler(String str) {
        if(str.length() < 2) {
            int charByte;
            if((charByte = lerNovoCaractere()) != -1) {
                str += (char)charByte;
                if(ehOperador(str)) {
                    System.out.println(str + " " + linha + (coluna - str.length()));
                } else {
                    System.out.println(str.charAt(0) + " " + linha + " " + (coluna - str.length() - 1));
                    if(ehSeparador(str.substring(1))) {
                        separadorHandler(str.substring(1));
                    }
                }
            }
        } else {
            System.out.println(str + " " + linha + " " + (coluna - str.length()));
        }
    }

    public void numericoHandler(String str) {
        StringBuilder stringBuilder = new StringBuilder(str);
        int charByte;
        while((charByte = lerNovoCaractere()) != -1) {
            String character = String.valueOf((char)charByte);
            if(Pattern.matches("\\.", character)) {
                charByte = lerNovoCaractere();
                character += String.valueOf((char)charByte);
                if(!ehNumerico(stringBuilder.toString().concat(character))) {
                    System.out.println(stringBuilder.toString() + " " + linha + " " + (coluna - stringBuilder.toString().length()));
                    identificarTipo(character);
                    return;
                }
                stringBuilder.append(character);
                continue;
            }

            if(!ehNumerico(character)) {
                break;
            }

            stringBuilder.append(character);
        }
        str = stringBuilder.toString();
        System.out.println(str + " " + linha + " " + (coluna - str.length()));
        identificarTipo(String.valueOf((char)charByte));
    }

    private void identificarTipo(String str) {
        if(ehComentario(str)) {
            comentarioHandler(str);
        }

        if(ehSeparador(str)) {
            separadorHandler(str);
        }

        if(ehLiteral(str)) {
            literalHandler(str);
        }

        if(ehIdentificador(str)) {
            identificadorHandler(str);
        }

        if(ehOperador(str)) {
            operadorHandler(str);
        }

        if(ehNumerico(str)) {
            numericoHandler(str);
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
            }

            identificarTipo(character);
        }
        try{
            this.fileReader.close();
        } catch (IOException e) {
            throw new RuntimeException(e);
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
