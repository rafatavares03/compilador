package frontend.tipos;

import java.util.regex.Pattern;
import frontend.FileScanner;

public abstract class Tipo {
    protected Pattern pattern;
    protected FileScanner fileScanner;

    public boolean matches(String token) {
        return pattern.matcher(token).matches();
    }
    public abstract String handleToken(String character);
}
