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
        if(token.valor().equals("if")) {
            arvore.addChild(condicao());
            S1(arvore);
            return;
        }

        if(token.tipo() == Recursos.TIPO) {
            arvore.addChild(declaracao());
            if(!listaDeTokens.getFirst().valor().equals(";")) {
                throw new RuntimeException("FALTOU ; DEPOIS DA DECLARACAO");
            }
            consumirToken(arvore);
            S1(arvore);
            return;
        }

        if(a() || token.valor().equals("!") || token.valor().equals("(")) {
            arvore.addChild(atribuicao());
            if(listaDeTokens.isEmpty() || !listaDeTokens.getFirst().valor().equals(";")) {
                throw new RuntimeException("FALTOU ; APÓS EXPRESSÃO OU ATRIBUIÇÃO");
            }
            consumirToken(arvore);
            S1(arvore);
            return;
        }

        if(token.valor().equals("while") || token.valor().equals(("for"))) {
            arvore.addChild(repeticao());
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

    public Node condicao() {
        if(!listaDeTokens.getFirst().valor().equals("if")) {
            throw new RuntimeException("Condição iniciada incorretamente");
        }
        Node NO = new Node(Sentencas.CONDICAO);
        consumirToken(NO);
        if(!listaDeTokens.getFirst().valor().equals("(")) {
            throw new RuntimeException("FALTOU O () NO IF");
        }
        consumirToken(NO);
        Node resultado = expressao();
        if(resultado != null) {
            NO.addChild(resultado);
        }
        if(!listaDeTokens.getFirst().valor().equals(")")) {
            throw new RuntimeException("PARENTESES NÃO FECHADO");
        }
        consumirToken(NO);
        bloco(NO);
        C1(NO);
        return NO;
    }

    private void C1(Node condicao) {
        if(listaDeTokens.isEmpty()) return;
        if(listaDeTokens.getFirst().valor().equals("}")) return;
        if(listaDeTokens.getFirst().valor().equals("else")) {
            consumirToken(condicao);
            C2(condicao);
        }
    }

    private void C2(Node condicao) {
        Token token = listaDeTokens.getFirst();
        if(token.valor().equals("if")) {
            Node blocoIf = condicao();
            if(blocoIf != null) {
                condicao.addChild(blocoIf);
            }
            return;
        }

        if(token.valor().equals("{")) {
            bloco(condicao);
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

    public Node declaracao() {
        Node NO = new Node(Sentencas.DECLARACAO);
        if(!(listaDeTokens.getFirst().tipo() == Recursos.TIPO)) {
            throw new RuntimeException("DECLARACAO SEM TIPO");
        }
        consumirToken(NO);
        if(!(listaDeTokens.getFirst().tipo() == Recursos.IDENTIFICADOR)) {
            throw new RuntimeException("FALTA O NOME DO IDENTIFICADOR");
        }
        consumirToken(NO);
        Node atribuicao = D2();
        if(atribuicao != null) {
            NO.addChild(atribuicao);
        }
        D1(NO);
        return NO;
    }

    private void D1(Node declaracao) {
        if(listaDeTokens.isEmpty()) return;
        if(listaDeTokens.getFirst().valor().equals(";")) return;
        if(listaDeTokens.getFirst().valor().equals(",")) {
            consumirToken(declaracao);
            if(listaDeTokens.getFirst().tipo() != Recursos.IDENTIFICADOR) {
                throw new RuntimeException("IDENTIFICADOR ESPERADO");
            }
            consumirToken(declaracao);
            Node atribuicao = D2();
            if(atribuicao != null) {
                declaracao.addChild(atribuicao);
            }
            D1(declaracao);
        }
    }

    private Node D2() {
        if(listaDeTokens.isEmpty()) return null;
        Token token = listaDeTokens.getFirst();
        if(token.valor().equals(",") || token.valor().equals(";")){
            return null;
        }
        if(operadorDeAtribuicao()) {
            Node operador = OP();
            Node conteudo = D3();
            if(operador == null|| conteudo == null) {
                throw new RuntimeException("ERRO NA DECLARAÇÃO!");
            }
            operador.addChild(conteudo);
            return operador;
        }
        return null;
    }

    private Node D3() {
        Token token = listaDeTokens.getFirst();
        if(a() || token.valor().equals("!") || token.valor().equals("(")) {
            return expressaoLogica();
        }
        return null;
    }

    private Node OP() {
        if(operadorDeAtribuicao()) {
            return new Node(listaDeTokens.removeFirst());
        }
        return null;
    }

    public Node COMP() {
        if(!operadorDeComparacao()) return null;
        return new Node(listaDeTokens.removeFirst());
    }

    public Node atribuicao() {
        if(a() || listaDeTokens.getFirst().valor().equals("(") || listaDeTokens.getFirst().valor().equals("!")) {
            Node NO = new Node(Sentencas.ATRIBUICAO);
            Node resultado = expressao();
            NO.addChild(resultado);
            ATR1(NO);
            return NO;
        }
        return null;
    }

    public void ATR1(Node atribuicao) {
        if(listaDeTokens.isEmpty()) return;
        Token token = listaDeTokens.getFirst();
        if(token.valor().equals(")") || token.valor().equals(";") || token.valor().equals(",")) return;
        Node operador = OP();
        if(operador != null) {
            Node filho = atribuicao.getChildNodes().get(0);
            atribuicao.getChildNodes().set(0, filho.getChildNodes().get(0));
            atribuicao.addChild(operador);
            atribuicao.addChild(expressao().getChildNodes().get(0));
        }
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
        if(token.valor().equals(",") || token.valor().equals(";") || token.valor().equals(")") || operadorDeAtribuicao()) return esquerda;
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
           token.valor().equals(",") ||
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
            token.valor().equals(",") ||
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
            token.valor().equals(",") ||
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
                token.valor().equals(",") ||
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

    public Node repeticao() {
        Node NO = new Node(Sentencas.REPETICAO);
        if(listaDeTokens.getFirst().valor().equals("while")) {
            consumirToken(NO);
            if(!listaDeTokens.getFirst().valor().equals("(")) {
                throw new RuntimeException("FALTOU O () NO WHILE!");
            }
            consumirToken(NO);
            Node resultado = expressao();
            if(resultado != null) {
                NO.addChild(resultado);
            }
            if(listaDeTokens.isEmpty() || !listaDeTokens.getFirst().valor().equals(")")) {
                throw new RuntimeException("PARÊNTESES NÃO FECHADO");
            }
            consumirToken(NO);
            bloco(NO);
            return NO;
        }

        if(listaDeTokens.getFirst().valor().equals("for")) {
            consumirToken(NO);
            if(!listaDeTokens.getFirst().valor().equals("(")) {
                throw new RuntimeException("FALTOU O () NO FOR!");
            }
            consumirToken(NO);
            R1(NO);
            if(listaDeTokens.isEmpty() || !listaDeTokens.getFirst().valor().equals(";")) {
                throw new RuntimeException("FALTOU ; DEPOIS DA DECLARAÇÃO DO FOR!");
            }
            consumirToken(NO);
            R2(NO);
            if(listaDeTokens.isEmpty() || !listaDeTokens.getFirst().valor().equals(";")) {
                throw new RuntimeException("FALTOU ; DEPOIS DA DECLARAÇÃO DO FOR!");
            }
            consumirToken(NO);
            R2(NO);
            R3(NO);
            if(listaDeTokens.isEmpty() || !listaDeTokens.getFirst().valor().equals(")")) {
                throw new RuntimeException("FALTOU ; DEPOIS DA DECLARAÇÃO DO FOR!");
            }
            consumirToken(NO);
            bloco(NO);
            return NO;
        }

        return null;
    }

    private void R1(Node repeticao) {
        if(listaDeTokens.getFirst().valor().equals(";")) {
            return;
        }

        if(listaDeTokens.getFirst().tipo() == Recursos.TIPO) {
            repeticao.addChild(declaracao());
            return;
        }
    }

    private void R2(Node repeticao) {
        Token token = listaDeTokens.getFirst();
        if(token.valor().equals(";") || token.valor().equals(")")) return;
        if(a() || token.valor().equals("!") || token.valor().equals("(")) {
            repeticao.addChild(expressao());
        }
    }

    private void R3(Node repeticao) {
        if(listaDeTokens.isEmpty()) return;
       Token token = listaDeTokens.getFirst();
        if(token.valor().equals(";") || token.valor().equals(")")) return;
       if(token.valor().equals(",")) {
           consumirToken(repeticao);
           R2(repeticao);
           R3(repeticao);
       }
    }

}
