package frontend.tipos;

import frontend.FileScanner;

import java.util.regex.Pattern;

public class Separador extends Tipo{
    public Separador(FileScanner fileScanner) {
        super.pattern = Pattern.compile("\r?\n|[\t (){};,\\[\\]]");
        super.fileScanner = fileScanner;
    }

    @Override
    public String handleToken(String character) {
        if(Pattern.matches("[(){};\\[\\]]", character)) {
            System.out.println(character + " " + this.fileScanner.getLine() + " " + this.fileScanner.getColumn());
        }
        if(Pattern.matches("\r?\n", character)) {
            this.fileScanner.newLine();
        }
        return "";
    }
}
