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
        sentenca(arvoreSintatica);
        arvoreSintatica.print("");
        return true;
    }

    private void consumirToken(Node raiz) {
        if(listaDeTokens.isEmpty()) {
            throw new RuntimeException("LISTA VAZIA!");
        }
        raiz.addChild(new Node(listaDeTokens.removeFirst()));
    }

    private boolean a() {
        if(listaDeTokens.isEmpty()) return false;
        Recursos tokenTipo = listaDeTokens.getFirst().tipo();
        return tokenTipo == Recursos.NUMERICO ||
                tokenTipo == Recursos.LITERAL ||
                tokenTipo == Recursos.IDENTIFICADOR ||
                tokenTipo == Recursos.BOOLEANO;
    }

    private boolean operadorDeAtribuicao() {
        String tokenValor = listaDeTokens.getFirst().valor();
        return tokenValor.equals("=") ||
                tokenValor.equals("+=") ||
                tokenValor.equals("-=");
    }

    private boolean operadorDeAtribuicao(String operador) {
        return operador.equals("=") ||
                operador.equals("+=") ||
                operador.equals("-=");
    }

    private boolean operadorDeComparacao() {
        String tokenValor = listaDeTokens.getFirst().valor();
        return tokenValor.equals("<") ||
                tokenValor.equals("<=") ||
                tokenValor.equals(">") ||
                tokenValor.equals(">=") ||
                tokenValor.equals("==") ||
                tokenValor.equals("!=");
    }

    public void sentenca(Node arvore) {
        Token token = listaDeTokens.getFirst();
        Node folha = null;
        if(token.valor().equals("if")) {
            folha = new Node(Sentencas.CONDICAO);
            arvore.addChild(folha);
            condicao(folha);
            S1(arvore);
            return;
        }

        if(token.tipo() == Recursos.TIPO) {
            folha = new Node(Sentencas.DECLARACAO);
            arvore.addChild(folha);
            declaracao(folha);
            consumirToken(arvore);
            S1(arvore);
            return;
        }

        if(a() || token.valor().equals("!") || token.valor().equals("(")) {
            folha = new Node(Sentencas.ATRIBUICAO);
            arvore.addChild(folha);
            atribuicao(folha);
            /*
            if(folha.getChildNodes().get(1).getToken() != null &&
               folha.getChildNodes().size() >= 2 &&
               operadorDeAtribuicao(folha.getChildNodes().get(1).getToken().valor())) {
                folha.setType(Sentencas.ATRIBUICAO);
            }
            */
            if(listaDeTokens.isEmpty() || !listaDeTokens.getFirst().valor().equals(";")) {
                throw new RuntimeException("FALTOU ; APÓS EXPRESSÃO OU ATRIBUIÇÃO");
            }
            consumirToken(arvore);
            S1(arvore);
            return;
        }

        if(token.valor().equals("while") || token.valor().equals(("for"))) {
            folha = new Node(Sentencas.REPETICAO);
            arvore.addChild(folha);
            repeticao(folha);
            S1(arvore);
            return;
        }
    }

    public void S1(Node arvore) {
        if(listaDeTokens.isEmpty() || listaDeTokens.getFirst().valor().equals("}")) {
            return;
        }
        sentenca(arvore);
        S1(arvore);
    }

    public void condicao(Node arvore) {
        if(listaDeTokens.getFirst().valor().equals("if")) {
            consumirToken(arvore);
            if(!listaDeTokens.getFirst().valor().equals("(")) {
                throw new RuntimeException("FALTOU O () NO IF");
            }
            consumirToken(arvore);
            arvore.addChild(expressao());
            if(!listaDeTokens.getFirst().valor().equals(")")) {
                throw new RuntimeException("PARENTESES NÃO FECHADO");
            }
            consumirToken(arvore);
            bloco(arvore);
            C1(arvore);
        }
    }

    private void C1(Node arvore) {
        if(listaDeTokens.isEmpty()) return;
        if(listaDeTokens.getFirst().valor().equals("}")) return;
        if(listaDeTokens.getFirst().valor().equals("else")) {
            consumirToken(arvore);
            C2(arvore);
        }
    }

    private void C2(Node arvore) {
        Token token = listaDeTokens.getFirst();
        if(token.valor().equals("if")) {
            condicao(arvore);
            return;
        }

        if(token.valor().equals("{")) {
            bloco(arvore);
            return;
        }
    }

    private void bloco(Node arvore) {
        Token token = listaDeTokens.getFirst();
        if(token.valor().equals("{")) {
            consumirToken(arvore);
            sentenca(arvore);
            if(listaDeTokens.isEmpty() || !listaDeTokens.getFirst().valor().equals("}")) {
                throw new RuntimeException("Bloco não fechado!");
            }
            consumirToken(arvore);
        }
    }

    public Node declaracao(Node arvore) {
        if(!(listaDeTokens.getFirst().tipo() == Recursos.TIPO)) {
            throw new RuntimeException("DECLARACAO SEM TIPO");
        }
        consumirToken(arvore);
        if(!(listaDeTokens.getFirst().tipo() == Recursos.IDENTIFICADOR)) {
            throw new RuntimeException("FALTA O NOME DO IDENTIFICADOR");
        }
        consumirToken(arvore);
        D2(arvore);
        return null;
    }

    private void D2(Node arvore) {
        if(listaDeTokens.isEmpty()) return;
        Token token = listaDeTokens.getFirst();
        if(token.valor().equals(",") || token.valor().equals(";")){
            return;
        }
        if(operadorDeAtribuicao()) {
            OP(arvore);
            D3(arvore);
        }
    }

    private void D3(Node arvore) {
        Token token = listaDeTokens.getFirst();
        if(a() || token.valor().equals("!") || token.valor().equals("(")) {
            arvore.addChild(expressaoLogica());
        }
    }

    private void OP(Node arvore) {
        if(operadorDeAtribuicao()) {
            consumirToken(arvore);
        }
    }

    public Node COMP() {
        if(!operadorDeComparacao()) return null;
        return new Node(listaDeTokens.removeFirst());
    }

    public Node expressao() {
        if(a() || listaDeTokens.getFirst().valor().equals("(") || listaDeTokens.getFirst().valor().equals("!")) {
            Node NO = new Node(Sentencas.EXPRESSAO);
            Node resultado = expressaoLogica();
            NO.addChild(resultado);
            return NO;
        }
        return null;
    }

    public Node atribuicao(Node arvore) {
        Node NO = new Node(Sentencas.ATRIBUICAO);
        if(a() || listaDeTokens.getFirst().valor().equals("(") || listaDeTokens.getFirst().valor().equals("!")) {
            arvore.addChild(NO);
            arvore.addChild(expressao());
            ATR1(arvore);
        }
        return null;
    }

    public void ATR1(Node arvore) {
        if(listaDeTokens.isEmpty()) return;
        Token token = listaDeTokens.getFirst();
        if(token.equals(")") || token.equals(";")) return;
        OP(arvore);
        atribuicao(arvore);
    }

    public Node expressaoLogica() {
        Token token = listaDeTokens.getFirst();
        if(a() || token.valor().equals("(") || token.valor().equals("!")) {
            Node esquerda = TL();
            return EL1(esquerda);
        }
        return null;
    }


    public Node EL1(Node esquerda) {
        if(listaDeTokens.isEmpty()) return esquerda;
        Token token = listaDeTokens.getFirst();
        if(token.valor().equals(";") || token.valor().equals(")") || operadorDeAtribuicao()) return esquerda;
        if(token.valor().equals("||")) {
            Node OR = new Node(listaDeTokens.removeFirst());
            OR.addChild(esquerda);
            Node direita = TL();
            OR.addChild(direita);
            return EL1(OR);
        }
        return esquerda;
    }

    public Node TL() {
        Token token = listaDeTokens.getFirst();
        if(a() || token.valor().equals("(") || token.valor().equals("!")) {
            Node esquerda = expressaoRelacional();
            return TL1(esquerda);
        }
        return null;
    }

    public Node TL1(Node esquerda) {
        if(listaDeTokens.isEmpty()) return esquerda;
        Token token = listaDeTokens.getFirst();
        if(token.valor().equals(")") ||
           token.valor().equals("||") ||
           token.valor().equals(";") ||
           operadorDeAtribuicao()){
            return esquerda;
        }
        if(token.valor().equals("&&")) {
            Node AND = new Node(listaDeTokens.removeFirst());
            AND.addChild(esquerda);
            Node direita = expressaoRelacional();
            AND.addChild(direita);
            return TL1(AND);
        }
        return esquerda;
    }

    public Node expressaoRelacional() {
        Token token = listaDeTokens.getFirst();
        if(a() || token.valor().equals("(") || token.valor().equals("!")){
            Node esquerda = expressaoAritmetica();
            return ER1(esquerda);
        }
        return null;
    }

    public Node ER1(Node esquerda) {
        if(listaDeTokens.isEmpty()) return esquerda;
        Token token = listaDeTokens.getFirst();
        if(token.valor().equals("&&") ||
            token.valor().equals("||") ||
            token.valor().equals(";") ||
            operadorDeAtribuicao()
        ) {
            return esquerda;
        }
        if(operadorDeComparacao()) {
            Node operador = COMP();
            operador.addChild(esquerda);
            Node direita = expressaoAritmetica();
            operador.addChild(direita);
            return ER1(operador);
        }
        return esquerda;
    }

    public Node expressaoAritmetica() {
        Node NO = new Node(Sentencas.EXPRESSAO_ARITMETICA);
        Token token = listaDeTokens.getFirst();
        if(a() || token.valor().equals("(") || token.valor().equals("!")) {
            Node esquerda = T();
            return EA1(esquerda);
        }
        return null;
    }

    public Node EA1(Node esquerda) {
        if(listaDeTokens.isEmpty()) return esquerda;
        Token token = listaDeTokens.getFirst();
        if(token.valor().equals(")") ||
            token.valor().equals("&&") ||
            token.valor().equals("||") ||
            token.valor().equals(";") ||
            operadorDeAtribuicao() ||
            operadorDeComparacao()
        ) {
            return esquerda;
        }
        if(token.valor().equals("+") || token.valor().equals("-")) {
            Node operador = new Node(listaDeTokens.removeFirst());
            operador.addChild(esquerda);
            Node direita = T();
            operador.addChild(direita);
            return EA1(operador);
        }
        return esquerda;
    }

    private Node T() {
        Token token = listaDeTokens.getFirst();
        if(a() || token.valor().equals("(") || token.valor().equals("!")) {
            Node esquerda = F();
            return T1(esquerda);
        }
        return null;
    }

    public Node T1(Node esquerda) {
        if(listaDeTokens.isEmpty()) return esquerda;
        Token token = listaDeTokens.getFirst();
        if(token.valor().equals(")") ||
                token.valor().equals("&&") ||
                token.valor().equals("||") ||
                token.valor().equals(";") ||
                operadorDeAtribuicao() ||
                operadorDeComparacao()
        ) {
            return esquerda;
        }
        if(token.valor().equals("*") || token.valor().equals("/")) {
            Node operador = new Node(listaDeTokens.removeFirst());
            operador.addChild(esquerda);
            Node direita = F();
            operador.addChild(direita);
            return T1(operador);
        }

        return esquerda;
    }

    private Node F() {
        Token token = listaDeTokens.getFirst();
        if(token.valor().equals("!")) {
            Node negacao = new Node(listaDeTokens.removeFirst());
            Node filho = F();
            negacao.addChild(filho);
            return negacao;
        }

        if(token.valor().equals("(")) {
            Node expressao = new Node(Sentencas.EXPRESSAO);
            expressao.addChild(new Node(listaDeTokens.removeFirst()));
            Node conteudo = expressaoLogica();
            expressao.addChild(conteudo);
            expressao.addChild(new Node(listaDeTokens.removeFirst()));
            return expressao;
        }

        if(token.tipo() == Recursos.IDENTIFICADOR || token.tipo() == Recursos.NUMERICO) {
            Node NO = new Node(listaDeTokens.removeFirst());
            Node iterador = P();
            if(iterador != null) {
                NO.addChild(iterador);
            }
            return NO;
        }

        return new Node(listaDeTokens.removeFirst());
    }

    private Node P() {
        if(listaDeTokens.isEmpty()) return null;
        Token token = listaDeTokens.getFirst();
        if(token.valor().equals("++") || token.valor().equals("--")) {
            return new Node(listaDeTokens.removeFirst());
        }
        return null;
    }

    public void repeticao(Node arvore) {
        if(listaDeTokens.getFirst().valor().equals("while")) {
            consumirToken(arvore);
            if(!listaDeTokens.getFirst().valor().equals("(")) {
                throw new RuntimeException("FALTOU O () NO WHILE!");
            }
            consumirToken(arvore);
            arvore.addChild(expressao());
            if(listaDeTokens.isEmpty() || !listaDeTokens.getFirst().valor().equals(")")) {
                throw new RuntimeException("PARÊNTESES NÃO FECHADO");
            }
            consumirToken(arvore);
            bloco(arvore);
            return;
        }

        if(listaDeTokens.getFirst().valor().equals("for")) {
            consumirToken(arvore);
            if(!listaDeTokens.getFirst().valor().equals("(")) {
                throw new RuntimeException("FALTOU O () NO FOR!");
            }
            consumirToken(arvore);
            R1(arvore);
            if(listaDeTokens.isEmpty() || !listaDeTokens.getFirst().valor().equals(";")) {
                throw new RuntimeException("FALTOU ; DEPOIS DA DECLARAÇÃO DO FOR!");
            }
            consumirToken(arvore);
            R2(arvore);
            if(listaDeTokens.isEmpty() || !listaDeTokens.getFirst().valor().equals(";")) {
                throw new RuntimeException("FALTOU ; DEPOIS DA DECLARAÇÃO DO FOR!");
            }
            consumirToken(arvore);
            R2(arvore);
            if(listaDeTokens.isEmpty() || !listaDeTokens.getFirst().valor().equals(")")) {
                throw new RuntimeException("FALTOU ; DEPOIS DA DECLARAÇÃO DO FOR!");
            }
            consumirToken(arvore);
            bloco(arvore);
            return;
        }
    }

    private void R1(Node arvore) {
        if(listaDeTokens.getFirst().valor().equals(";")) {
            return;
        }

        if(listaDeTokens.getFirst().tipo() == Recursos.TIPO) {
            declaracao(arvore);
            return;
        }
    }

    private void R2(Node arvore) {
        Token token = listaDeTokens.getFirst();
        if(token.valor().equals(";") || token.valor().equals(")")) return;
        if(a() || token.valor().equals("!") || token.valor().equals("(")) {
            arvore.addChild(expressao());
        }
    }

}
