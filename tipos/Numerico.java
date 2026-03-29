package tipos;

import frontend.FileScanner;
import token.Lexema;

import java.util.regex.Pattern;

public class Numerico extends Tipo{
    public Numerico(FileScanner fileScanner) {
        super.pattern = Pattern.compile("[0-9]+(\\.[0-9]+)?");
        super.fileScanner = fileScanner;
    }

    @Override
    public Lexema handleToken(String character) {
        StringBuilder stringBuilder = new StringBuilder(character);
        Lexema lexema = new Lexema();
        int charByte;
        while((charByte = this.fileScanner.readCharacter()) != -1) {
            String aux = String.valueOf((char)charByte);
            if(Pattern.matches("\\.", aux)) {
                if(stringBuilder.toString().indexOf('.') >= 0) {
                    lexema.setToken(stringBuilder.toString().concat(aux));
                    lexema.setInvalid();
                    return lexema;
                }
                charByte = this.fileScanner.readCharacter();
                aux += String.valueOf((char)charByte);
                if(!matches(stringBuilder.toString().concat(aux))) {
                    lexema.setToken(stringBuilder.toString());
                    lexema.setNextChar(aux);
                    return lexema;
                }
                stringBuilder.append(aux);
                continue;
            }

            if(!matches(aux)) {
                break;
            }

            stringBuilder.append(aux);
        }

        lexema.setToken(stringBuilder.toString());
        lexema.setNextChar(String.valueOf((char)charByte));
        return lexema;
    }
}
