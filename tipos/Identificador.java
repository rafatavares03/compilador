package tipos;

import frontend.FileScanner;
import token.Lexema;

import java.util.regex.Pattern;

public class Identificador extends Tipo{
    protected Pattern validCharacter = Pattern.compile("\\w");

    public Identificador(FileScanner fileScanner) {
        super.pattern = Pattern.compile("[_a-zA-Z]");
        super.fileScanner = fileScanner;
    }

    protected boolean isValidCharacter(String character) {
        return validCharacter.matcher(character).matches();
    }

    @Override
    public Lexema handleToken(String character) {
        Lexema lexema = new Lexema();
        StringBuilder stringBuilder = new StringBuilder(character);
        int charByte;
        while((charByte = this.fileScanner.readCharacter()) != -1) {
            String aux = String.valueOf((char)charByte);
            if(!isValidCharacter(aux)) {
                break;
            }
            stringBuilder.append(aux);
        }
        lexema.setToken(stringBuilder.toString());
        if(charByte != -1) lexema.setNextChar(String.valueOf((char)charByte));
        return lexema;
    }
}
