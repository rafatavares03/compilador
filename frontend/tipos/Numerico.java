package frontend.tipos;

import frontend.FileScanner;

import java.util.regex.Pattern;

public class Numerico extends Tipo{
    public Numerico(FileScanner fileScanner) {
        super.pattern = Pattern.compile("[0-9]+(\\.[0-9]+)?");
        super.fileScanner = fileScanner;
    }

    @Override
    public String handleToken(String character) {
        StringBuilder stringBuilder = new StringBuilder(character);
        int charByte;
        while((charByte = this.fileScanner.readCharacter()) != -1) {
            String aux = String.valueOf((char)charByte);
            if(Pattern.matches("\\.", aux)) {
                charByte = this.fileScanner.readCharacter();
                aux += String.valueOf((char)charByte);
                if(!matches(stringBuilder.toString().concat(aux))) {
                    System.out.println(stringBuilder.toString() + " " + fileScanner.getLine() + " " + (fileScanner.getColumn() - stringBuilder.toString().length()));
                    return aux;
                }
                stringBuilder.append(aux);
                continue;
            }

            if(!matches(aux)) {
                break;
            }

            stringBuilder.append(aux);
        }
        character = stringBuilder.toString();
        System.out.println(character + " " + fileScanner.getLine() + " " + (fileScanner.getColumn() - character.length()));
        return String.valueOf((char)charByte);
    }
}
