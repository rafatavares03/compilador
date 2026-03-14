package frontend;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;

public class AnalisadorLexico {
    public void analisarCodigoFonte(String codigoFontePath) {
        try {
            File codigoFonte = new File(codigoFontePath);
            FileReader fileReader = new FileReader(codigoFonte);

            int linha = 1;
            int coluna = 0;
            int rebatedor = 0;
            int charByte = fileReader.read();
            String token = "";

            while(charByte != -1) {
                char character = (char)charByte;
                rebatedor++;

                if(character == '\r') {
                    charByte = fileReader.read();
                    continue;
                }

                if(character == ' ') {
                    System.out.println(token + " " + linha + " " + coluna);
                    token = "";
                } else if(character == '\n') {
                    System.out.println(token + " " + linha + " " + coluna);
                    linha++;
                    rebatedor = 0;
                    coluna = 0;
                    token = "";
                } else {
                    if(token.isEmpty()) {
                        coluna = rebatedor;
                    }
                    token += character;
                }
                charByte = fileReader.read();
            }
            if(!token.isEmpty()) {
                System.out.println(token + " " + linha + " " + coluna);
            }
        } catch(FileNotFoundException exception) {
            throw new RuntimeException("Arquivo inexistênte ou não é possível ler.");
        } catch(IOException exception) {
            throw new RuntimeException("Ocorreu um erro ao ler o arquivo." + exception);
        }
    }
}
