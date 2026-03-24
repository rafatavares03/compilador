package tipos;

import java.util.regex.Pattern;
import frontend.FileScanner;
import token.Lexema;

public abstract class Tipo {
    protected Pattern pattern;
    protected FileScanner fileScanner;

    public boolean matches(String token) {
        return pattern.matcher(token).matches();
    }
    public Lexema handleToken(String character) {
        return new Lexema();
    };
}
