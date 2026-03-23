package frontend;

import java.io.File;
import java.util.regex.Pattern;

public class AnalisadorLexico {
    private FileScanner fileScanner;
    private final Pattern separador = Pattern.compile("\r?\n|[\t (){};,\\[\\]]");
    private final Pattern literal = Pattern.compile("['\"]");
    private final Pattern comentario = Pattern.compile("//|/\\*");
    private final Pattern identificador = Pattern.compile("[_a-zA-Z]");
    private final Pattern operador = Pattern.compile("\\+\\+?|--?|&&?|\\|\\|?|[*/]|(=[=+-]?)|[<>!]=?");
    private final Pattern numerico = Pattern.compile("[0-9]+(\\.[0-9]+)?");
    private final Pattern palavrasReservadas = Pattern.compile("if|else|while|break|continue|return|null");

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
            System.out.println(str + " " + this.fileScanner.getLine() + " " + this.fileScanner.getColumn());
        }
        if(Pattern.matches("\r?\n", str)) {
            this.fileScanner.newLine();
        }
    }

    public void literalHandler(String str) {
        if(Pattern.matches("\'", str)) {
            int charByte = this.fileScanner.readCharacter();
            str += String.valueOf((char)charByte);
            if(!Pattern.matches("\'\'", str)){
                charByte = this.fileScanner.readCharacter();
                str += String.valueOf((char)charByte);
            }
        } else if(Pattern.matches("\"", str)) {
            int charByte;
            StringBuilder strBuilder = new StringBuilder(str);
            while((charByte = this.fileScanner.readCharacter()) != -1) {
                String caractere = String.valueOf((char)charByte);

                if(caractere.charAt(0) == '\\') {
                    charByte = this.fileScanner.readCharacter();
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
        System.out.println(str + " " + fileScanner.getLine() + " " + (fileScanner.getColumn() - str.length() + 1));
    }

    public void comentarioHandler(String str) {
        int charByte;
        if(Pattern.matches("//", str)) {
            while((charByte = this.fileScanner.readCharacter()) != -1) {
                if((char)charByte == '\n') {
                    break;
                }
            }
            this.fileScanner.newLine();
        }
        if(Pattern.matches("/\\*", str)) {
            while((charByte = this.fileScanner.readCharacter()) != -1) {
                if((char)charByte == '\n') {
                    this.fileScanner.newLine();
                }
                if((char)charByte == '*') {
                    charByte = this.fileScanner.readCharacter();
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
        while((charByte = this.fileScanner.readCharacter()) != -1) {
            String character = String.valueOf((char)charByte);

            if((char)charByte == '\r') {
                charByte = this.fileScanner.readCharacter();
                character += String.valueOf((char)charByte);
            }
            if(ehSeparador(character) || ehOperador(character)) {
                str = stringBuilder.toString();
                System.out.println(str + " " + fileScanner.getLine() + " " + (fileScanner.getColumn() - str.length()));
                identificarTipo(character);
                break;
            }

            stringBuilder.append(character);
        }
    }

    public void operadorHandler(String str) {
        if(str.length() < 2) {
            int charByte;
            if((charByte = this.fileScanner.readCharacter()) != -1) {
                str += (char)charByte;
                if(ehOperador(str)) {
                    System.out.println(str + " " + fileScanner.getLine() + (fileScanner.getColumn() - str.length()));
                } else {
                    System.out.println(str.charAt(0) + " " + fileScanner.getLine() + " " + (fileScanner.getColumn() - str.length() - 1));
                    if(ehSeparador(str.substring(1))) {
                        separadorHandler(str.substring(1));
                    }
                }
            }
        } else {
            System.out.println(str + " " + fileScanner.getLine() + " " + (fileScanner.getColumn() - str.length()));
        }
    }

    public void numericoHandler(String str) {
        StringBuilder stringBuilder = new StringBuilder(str);
        int charByte;
        while((charByte = this.fileScanner.readCharacter()) != -1) {
            String character = String.valueOf((char)charByte);
            if(Pattern.matches("\\.", character)) {
                charByte = this.fileScanner.readCharacter();
                character += String.valueOf((char)charByte);
                if(!ehNumerico(stringBuilder.toString().concat(character))) {
                    System.out.println(stringBuilder.toString() + " " + fileScanner.getLine() + " " + (fileScanner.getColumn() - stringBuilder.toString().length()));
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
        System.out.println(str + " " + fileScanner.getLine() + " " + (fileScanner.getColumn() - str.length()));
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

    public void executarAnaliseLexica(File codigoFonte) {
        this.fileScanner = null;
        try {
            this.fileScanner = new FileScanner(codigoFonte);
            int charByte;
            while((charByte = this.fileScanner.readCharacter()) != -1) {
                String character = String.valueOf((char)charByte);

                if(Pattern.matches("\r", character)) {
                    if((charByte = this.fileScanner.readCharacter()) != -1) {
                        character += String.valueOf((char)charByte);
                    }
                }

                if(Pattern.matches("/", character)) {
                    if((charByte = this.fileScanner.readCharacter()) != -1) {
                        character += String.valueOf((char)charByte);
                    }
                }

                identificarTipo(character);
            }
        } catch (Exception e) {
            throw new RuntimeException(e);
        } finally {
            if(fileScanner != null) {
                fileScanner.close();
            }
        }
    }

}
