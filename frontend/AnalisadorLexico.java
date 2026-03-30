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
import java.util.regex.Pattern;

public class AnalisadorLexico {
    private FileScanner fileScanner;
    private HashMap<Tipos,Tipo> tiposHashMap;
    private final Tipo palavrasReservadas = new PalavraReservada(this.fileScanner);
    private Deque<Token> tokens;
    private boolean erroDetectado = false;

    private HashMap<Tipos,Tipo> gerarHashDeTipo(FileScanner fileScanner) {
        HashMap<Tipos, Tipo> hash = new HashMap<>();
        hash.put(Tipos.SEPARADOR, new Separador(fileScanner));
        hash.put(Tipos.NUMERICO, new Numerico(fileScanner));
        hash.put(Tipos.COMENTARIO, new Comentario(fileScanner));
        hash.put(Tipos.LITERAL, new Literal(fileScanner));
        hash.put(Tipos.OPERADOR, new Operador(fileScanner));
        hash.put(Tipos.IDENTIFICADOR, new Identificador(fileScanner));
        return hash;
    }

    private void errorHandler(String character, String erroMsg, int linha, int coluna) {
        this.erroDetectado = true;
        StringBuilder stringBuilder = new StringBuilder(character);
        int charByte;
        while((charByte = fileScanner.readCharacter()) != -1) {
            String aux = String.valueOf((char)charByte);
            if(Pattern.matches(" |\r?\n", aux)) {
                break;
            }
            if(!aux.equals("\r")) stringBuilder.append(aux);
        }
        String str = stringBuilder.toString();
        if(erroMsg.isEmpty()) {
            System.out.println("ERRO: " + ((str.length() > 1) ? "sequência de caracteres inválida " : "caractere inválido "));
            System.out.println("\t\t" + str);
        } else {
            System.out.println(erroMsg);
            System.out.println("\t\t" + str);
        }
        System.out.println("\t\tdetectado na linha " + linha + " coluna " + coluna);
    }

    private void criaToken(String valor, Tipos tipo, int id, int linha, int coluna) {
        Token token = new Token(valor, tipo, id, linha, coluna);
        tokens.addLast(token);
    }

    public void identificaTipo(String character) {
        int pivo = fileScanner.getColumn() - character.length() + 1;
        int pivoLinha = fileScanner.getLine();

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
            if(tiposHashMap.get(Tipos.COMENTARIO).matches(character)) {
                Lexema lexema = tiposHashMap.get(Tipos.COMENTARIO).handleToken(character);
                if(!lexema.isValid()) {
                    errorHandler(character, lexema.getErrorMsg(), pivoLinha, pivo);
                    return;
                }
            } else if(!tiposHashMap.get(Tipos.OPERADOR).matches(character.substring(1)) &&
                        !tiposHashMap.get(Tipos.LITERAL).matches(character.substring(1))) {
                criaToken("/", Tipos.OPERADOR, this.tokens.size(), fileScanner.getLine(), pivo);
                identificaTipo(character.substring(1));
            } else {
                errorHandler(character, "", pivoLinha, pivo);
            }
            return;
        }

        for(Map.Entry<Tipos, Tipo> tipo : tiposHashMap.entrySet()) {
            if(tipo.getValue().matches(character)) {
                Lexema lexema = tipo.getValue().handleToken(character);
                if(!lexema.isValid()) {
                    errorHandler( lexema.getToken(), lexema.getErrorMsg(), pivoLinha, pivo);
                    return;
                }
                if(!lexema.getToken().isEmpty()) {
                    if(tipo.getKey() == Tipos.IDENTIFICADOR && palavrasReservadas.matches(lexema.getToken())) {
                        criaToken(lexema.getToken(), Tipos.PALAVRA_RESERVADA, this.tokens.size(), fileScanner.getLine(), pivo);
                    } else {
                        criaToken(lexema.getToken(), tipo.getKey(), this.tokens.size(), fileScanner.getLine(), pivo);
                    }
                }
                if(!lexema.getNextChar().isEmpty()) {
                    identificaTipo(lexema.getNextChar());
                }
                return;
            }
        }
        errorHandler(character, "", pivoLinha, pivo);
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

    public boolean temErroLexico() {
        return this.erroDetectado;
    }
}
