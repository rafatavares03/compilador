package tipos;

import frontend.FileScanner;
import token.Lexema;

import java.util.regex.Pattern;

public class Literal extends Tipo{
    public Literal(FileScanner fileScanner) {
        super.pattern = Pattern.compile("['\"]");
        super.fileScanner = fileScanner;
    }

    private String charLiteralHandler(String character) {
        int charByte = this.fileScanner.readCharacter();
        character += String.valueOf((char)charByte);
        if(!Pattern.matches("\'\'", character)){
            charByte = this.fileScanner.readCharacter();
            character += String.valueOf((char)charByte);
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

            if(Pattern.matches("\"", caractere)) {
                break;
            }
        }
        return strBuilder.toString();
    }

    @Override
    public Lexema handleToken(String character) {
        String literal = (Pattern.matches("\'", character)) ? charLiteralHandler(character) : stringLiteralHandler(character);
        return new Lexema(literal, "");
    }
}
