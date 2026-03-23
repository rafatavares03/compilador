package frontend;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;

public class FileScanner {
    private int line;
    private int column;
    private final FileReader fileReader;

    public FileScanner(File file) throws FileNotFoundException {
        this.line = 1;
        this.column = 0;
        this.fileReader = new FileReader(file);
    }

    public void newLine() {
        this.line++;
        this.column = 0;
    }

    public int readCharacter() {
        int charByte = -1;
        try{
            charByte = fileReader.read();
            this.column++;
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
        return charByte;
    }

    public void close() {
        try{
            this.fileReader.close();
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    public int getLine() {
        return this.line;
    }

    public int getColumn() {
        return this.column;
    }
}
