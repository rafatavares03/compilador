package recursos;

import frontend.FileScanner;

import java.util.regex.Pattern;

public class Tipo extends Recurso{
    public Tipo(FileScanner fileScanner) {
        super.pattern = Pattern.compile("int|string|boolean|float|null");
        this.fileScanner = fileScanner;
    }
}
