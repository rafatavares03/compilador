package tipos;

import frontend.FileScanner;
import token.Lexema;

import java.util.regex.Pattern;

public class Literal extends Tipo{
    private final Pattern validLiteral = Pattern.compile("'([^\\\\']|\\\\.)?'|\".*\"");

    public Literal(FileScanner fileScanner) {
        super.pattern = Pattern.compile("['\"]");
        super.fileScanner = fileScanner;
    }

    private boolean isValid(String literal) {
        return validLiteral.matcher(literal).matches();
    }

    private String charLiteralHandler(String character) {
        int charByte = this.fileScanner.readCharacter();
        character += String.valueOf((char)charByte);
        if(!Pattern.matches("\'\'", character)){
            charByte = this.fileScanner.readCharacter();
            character += String.valueOf((char)charByte);
            if(character.charAt(1) == '\\') {
                charByte = this.fileScanner.readCharacter();
                character += String.valueOf((char)charByte);               
            }
        }
        return character;
    }

    private String stringLiteralHandler(String character) {
        int charByte;
        StringBuilder strBuilder = new StringBuilder(character);
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

            if(Pattern.matches("\"|\n", caractere)) {
                break;
            }
        }
        return strBuilder.toString();
    }

    @Override
    public Lexema handleToken(String character) {
        String literal = (Pattern.matches("\'", character)) ? charLiteralHandler(character) : stringLiteralHandler(character);
        Lexema lexema = new Lexema(literal, "");
        if(!isValid(literal)) {
            lexema.setInvalid();
            lexema.setErrorMsg("ERRO: literal não encerrado ou inválido");
        }
        return lexema;
    }
}
