package recursos;

import frontend.FileScanner;

import java.util.regex.Pattern;

public class Booleano extends Recurso{
    public Booleano(FileScanner fileScanner) {
        super.pattern = Pattern.compile("true|false");
        super.fileScanner = fileScanner;
    }
}
