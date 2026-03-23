package frontend.tipos;

import frontend.FileScanner;

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
    public String handleToken(String character) {
        StringBuilder stringBuilder = new StringBuilder(character);
        int charByte;
        while((charByte = this.fileScanner.readCharacter()) != -1) {
            String aux = String.valueOf((char)charByte);
            if((char)charByte == '\r') {
                charByte = this.fileScanner.readCharacter();
                aux += String.valueOf((char)charByte);
            }
            if(!isValidCharacter(aux)) {
                System.out.println(stringBuilder.toString() + " " + fileScanner.getLine() + " " + (fileScanner.getColumn() - stringBuilder.toString().length()));
                return aux;
            }
            stringBuilder.append(aux);
        }
        return "";
    }
}
