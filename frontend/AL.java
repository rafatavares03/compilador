package frontend;

import tipos.*;
import token.Lexema;
import token.Token;

import java.io.File;
import java.io.FileNotFoundException;
import java.util.ArrayDeque;
import java.util.Deque;
import java.util.HashMap;
import java.util.Map;

public class AL {
    FileScanner fileScanner;
    HashMap<Tipos,Tipo> tiposHashMap;
    Deque<Token> tokens;

    private HashMap<Tipos,Tipo> gerarHashDeTipo(FileScanner fileScanner) {
        HashMap<Tipos, Tipo> hash = new HashMap<>();
        hash.put(Tipos.COMENTARIO, new Comentario(fileScanner));
        hash.put(Tipos.IDENTIFICADOR, new Identificador(fileScanner));
        hash.put(Tipos.LITERAL, new Literal(fileScanner));
        hash.put(Tipos.NUMERICO, new Numerico(fileScanner));
        hash.put(Tipos.OPERADOR, new Operador(fileScanner));
        hash.put(Tipos.SEPARADOR, new Separador(fileScanner));
        hash.put(Tipos.PALAVRA_RESERVADA, new PalavraReservada(fileScanner));
        return hash;
    }

    public boolean identificaTipo(String character) {
        boolean identificado = false;
        int pivo = fileScanner.getColumn() - character.length() + 1;

        if(character.equals("\r")) {
            int charByte = fileScanner.readCharacter();
            if(charByte != -1) {
                character += String.valueOf((char)charByte);
            }
        }

        if(character.equals("/")) {
            int aux = fileScanner.readCharacter();
            if(aux != -1) {
                character += (char)aux;
            }
            if(!identificaTipo(character)) {
                if(!tiposHashMap.get(Tipos.OPERADOR).matches(character.substring(1))) {
                    Token token = new Token("/", Tipos.OPERADOR, this.tokens.size(), fileScanner.getLine(), pivo);
                    identificaTipo(character.substring(1));
                }
            }
            return true;
        }

        for(Map.Entry<Tipos, Tipo> tipo : tiposHashMap.entrySet()) {
            if(tipo.getValue().matches(character)) {
                Lexema lexema = tipo.getValue().handleToken(character);
                if(!lexema.getToken().isEmpty()) {
                    Token token;
                    if(tipo.getKey() == Tipos.IDENTIFICADOR && tiposHashMap.get(Tipos.PALAVRA_RESERVADA).matches(lexema.getToken())) {
                        token = new Token(lexema.getToken(), Tipos.PALAVRA_RESERVADA, this.tokens.size(), fileScanner.getLine(), pivo);
                    } else {
                        token = new Token(lexema.getToken(), tipo.getKey(), this.tokens.size(), fileScanner.getLine(), pivo);
                    }
                    tokens.addLast(token);
                }
                if(!lexema.getNextChar().isEmpty()) {
                    identificaTipo(lexema.getNextChar());
                }
                identificado = true;
                break;
            }
        }
        return identificado;
    }

    public Deque<Token> executarAnaliseLexica(File codigoFonte) {
        this.fileScanner = null;
        this.tokens = new ArrayDeque<Token>();
        try {
            fileScanner = new FileScanner(codigoFonte);
            tiposHashMap = gerarHashDeTipo(fileScanner);
            int charByte;
            while((charByte = fileScanner.readCharacter()) != -1) {
                String character = String.valueOf((char)charByte);
                identificaTipo(character);
            }
        } catch (FileNotFoundException e) {
            throw new RuntimeException(e);
        } finally {
            if(this.fileScanner != null) this.fileScanner.close();
        }
        return this.tokens;
    }
}
