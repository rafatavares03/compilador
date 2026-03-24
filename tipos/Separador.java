package tipos;

import frontend.FileScanner;
import token.Lexema;

import java.util.regex.Pattern;

public class Separador extends Tipo{
    public Separador(FileScanner fileScanner) {
        super.pattern = Pattern.compile("\r?\n|[\t (){};,\\[\\]]");
        super.fileScanner = fileScanner;
    }

    @Override
    public Lexema handleToken(String character) {
        Lexema lexema = new Lexema();
        if(Pattern.matches("[(){};,\\[\\]]", character)) {
            lexema.setToken(character);
        }
        if(Pattern.matches("\r?\n", character)) {
            this.fileScanner.newLine();
        }
        return lexema;
    }
}
