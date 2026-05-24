package frontend;

import recursos.Recursos;
import recursos.Sentencas;
import token.Token;
import dataStructure.Node;

import java.util.Deque;

public class AnalisadorSintatico {
    private Deque<Token> listaDeTokens;
    public boolean executarAnalise(Deque<Token>tokens) {
        Node arvoreSintatica = new Node(Sentencas.PROGRAMA);
        if(tokens.isEmpty()) {
            return false;
        }
        this.listaDeTokens = tokens;
        sentenca();
        return true;
    }

    public Node sentenca() {
        Token token = listaDeTokens.getFirst();
        if(token.valor().equals("if")) {
            condicao();
        }

        if(token.tipo() == Recursos.TIPO) {
            declaracao();
        }

        if(token.tipo() == Recursos.IDENTIFICADOR ||
                token.tipo() == Recursos.LITERAL ||
                token.tipo() == Recursos.NUMERICO ||
                token.valor().equals("!") ||
                token.valor().equals("(") ||
                token.valor().equals("true") ||
                token.valor().equals("false")
        ) {
            atribuicao();
        }

        if(token.valor().equals("while") || token.valor().equals(("for"))) {
            repeticao();
        }

        S1();

        return null;
    }

    public Node S1() {
        if(listaDeTokens.isEmpty() || listaDeTokens.getFirst().valor().equals("}")) {
            return null;
        }
        Node arvore = sentenca();
        Node child = S1();
        if(arvore != null && child != null) {
            arvore.addChild(child);
        }
        return arvore;
    }

    public Node condicao() {
        System.out.println("IF");
        return null;
    }

    public Node declaracao() {
        System.out.println(listaDeTokens.getFirst().valor());
        Node arvore = new Node(Sentencas.DECLARACAO);
        if(!(listaDeTokens.getFirst().tipo() == Recursos.TIPO)) {
            throw new RuntimeException("DECLARACAO SEM TIPO");
        }
        arvore.addChild(new Node(listaDeTokens.removeFirst()));
        if(!(listaDeTokens.getFirst().tipo() == Recursos.IDENTIFICADOR)) {
            throw new RuntimeException("FALTA O NOME DO IDENTIFICADOR");
        }
        arvore.addChild(new Node(listaDeTokens.removeFirst()));
        D2();
        return null;
    }

    public Node D2() {
        if(listaDeTokens.getFirst().valor().equals(",") || listaDeTokens.getFirst().valor().equals(";")){
            return null;
        }
        Node arvore = null;
        if(listaDeTokens.getFirst().tipo() == Recursos.OPERADOR) {
            System.out.println(listaDeTokens.getFirst().valor());
            Node child = OP();
            if(child != null) {
                arvore = new Node(Sentencas.DECLARACAO);
                arvore.addChild(child);
            }
        }
        return arvore;
    }

    public Node OP() {
        Node arvore = null;
        if(listaDeTokens.getFirst().valor().equals("=") ||
            listaDeTokens.getFirst().valor().equals("+=") ||
            listaDeTokens.getFirst().valor().equals("-=")
        ) {
            arvore = new Node(listaDeTokens.removeFirst());
        }
        return arvore;
    }

    public Node expressao() {
        if(listaDeTokens.getFirst().tipo() == Recursos.NUMERICO ||
            listaDeTokens.getFirst().tipo() == Recursos.LITERAL ||
            listaDeTokens.getFirst().tipo() == Recursos.IDENTIFICADOR ||
            listaDeTokens.getFirst().tipo() == Recursos.BOOLEANO ||
            listaDeTokens.getFirst().equals("(") ||
            listaDeTokens.getFirst().equals("!")
        ) {
            return null;
        }
        return null;
    }

    public Node repeticao() {
        System.out.println("REPETIÇÃO");
        return null;
    }

    public Node atribuicao() {
        System.out.println("ATRIBUIÇÃO");
        return null;
    }
}
