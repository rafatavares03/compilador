package tipos;

import frontend.FileScanner;
import token.Lexema;

import java.util.regex.Pattern;

public class Operador extends Tipo{
    public Operador(FileScanner fileScanner) {
        super.pattern = Pattern.compile("\\+\\+?|--?|&&?|\\|\\|?|[*/]|(=[=+-]?)|[<>!]=?");;
        super.fileScanner = fileScanner;
    }

    @Override
    public Lexema handleToken(String character) {
        Lexema lexema = new Lexema();
        if (character.length() < 2) {
            int charByte;
            if ((charByte = this.fileScanner.readCharacter()) != -1) {
                character += (char) charByte;
                if (matches(character)) {
                    lexema.setToken(character);
                } else {
                    lexema.setToken(character);
                    lexema.setNextChar(character.substring(1));
                }
            }
        } else {
            lexema.setToken(character);
        }
        return lexema;
    }
}
