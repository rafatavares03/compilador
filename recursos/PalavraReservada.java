package recursos;

import frontend.FileScanner;
import java.util.regex.Pattern;

public class PalavraReservada extends Recurso {
    public PalavraReservada(FileScanner fileScanner) {
        super.pattern = Pattern.compile("if|else|while|for|break|continue");
        super.fileScanner = fileScanner;
    }
}
