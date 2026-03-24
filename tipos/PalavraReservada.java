package tipos;

import frontend.FileScanner;
import java.util.regex.Pattern;

public class PalavraReservada extends Tipo{
    public PalavraReservada(FileScanner fileScanner) {
        super.pattern = Pattern.compile("if|else|while|for|int|boolean|string|break|continue|return|null");
        super.fileScanner = fileScanner;
    }
}
