package frontend.tipos;
import frontend.FileScanner;

import java.util.regex.Pattern;

public class Comentario extends Tipo{
    public Comentario(FileScanner fileScanner) {
        super.pattern = Pattern.compile("//|/\\*");
        super.fileScanner = fileScanner;
    }

    private void lineComment() {
        int charByte;
        while((charByte = this.fileScanner.readCharacter()) != -1) {
            if((char)charByte == '\n') {
                break;
            }
        }
        this.fileScanner.newLine();
    }

    private void blockComment() {
        int charByte;
        while((charByte = this.fileScanner.readCharacter()) != -1) {
            if((char)charByte == '\n') {
                this.fileScanner.newLine();
            }
            if((char)charByte == '*') {
                charByte = this.fileScanner.readCharacter();
                if((char)charByte == '/') {
                    break;
                }
            }
        }
    }

    public String handleToken(String charactere) {
        if(Pattern.matches("//", charactere)) {
            lineComment();
        } else {
            blockComment();
        }
        return "";
    }
}
