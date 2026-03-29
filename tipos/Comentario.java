package tipos;
import frontend.FileScanner;
import token.Lexema;

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

    private void blockComment(Lexema lexema) {
        int charByte;
        while((charByte = this.fileScanner.readCharacter()) != -1) {
            if((char)charByte == '\n') {
                this.fileScanner.newLine();
            }
            if((char)charByte == '*') {
                charByte = this.fileScanner.readCharacter();
                if((char)charByte == '/') {
                    return;
                }
            }
        }
        lexema.setInvalid();
        lexema.setErrorMsg("ERRO: comentário de bloco não encerrado.");
    }

    public Lexema handleToken(String charactere) {
        Lexema lexema = new Lexema();
        if(Pattern.matches("//", charactere)) {
            lineComment();
        } else {
            blockComment(lexema);
        }
        return lexema;
    }
}
