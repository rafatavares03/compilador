package frontend;

import tipos.*;
import token.Lexema;

import java.io.File;
import java.io.FileNotFoundException;
import java.util.HashMap;
import java.util.Map;

public class AL {
    FileScanner fileScanner;
    HashMap<Tipos,Tipo> tiposHashMap;

    private HashMap<Tipos,Tipo> gerarHashDeTipo(FileScanner fileScanner) {
        HashMap<Tipos, Tipo> hash = new HashMap<>();
        hash.put(Tipos.COMENTARIO, new Comentario(fileScanner));
        hash.put(Tipos.IDENTIFICADOR, new Identificador(fileScanner));
        hash.put(Tipos.LITERAL, new Literal(fileScanner));
        hash.put(Tipos.NUMERICO, new Numerico(fileScanner));
        hash.put(Tipos.OPERADOR, new Operador(fileScanner));
        hash.put(Tipos.SEPARADOR, new Separador(fileScanner));
        return hash;
    }

    public boolean identificaTipo(String character) {
        boolean identificado = false;

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
                    System.out.println("/" + " " + fileScanner.getLine() + " " + Tipos.OPERADOR);
                    identificaTipo(character.substring(1));
                }
            }
            return true;
        }

        for(Map.Entry<Tipos, Tipo> tipo : tiposHashMap.entrySet()) {
            if(tipo.getValue().matches(character)) {
                Lexema lexema = tipo.getValue().handleToken(character);
                if(!lexema.getToken().isEmpty()) {
                    System.out.println(lexema.getToken() + " " + fileScanner.getLine() + " " + tipo.getKey());
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

    public void executarAnaliseLexica(File codigoFonte) {
        this.fileScanner = null;
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
            this.fileScanner.close();
        }
    }
}
