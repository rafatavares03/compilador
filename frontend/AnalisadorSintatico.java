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
            arvore.addChild(new Node(this.listaDeTokens.removeFirst()));
            S1(arvore);
            return;
        }

        if(a() || token.valor().equals("!") || token.valor().equals("(")) {
            folha = new Node(Sentencas.EXPRESSAO);
            arvore.addChild(folha);
            expressao(folha);
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
            arvore.addChild(new Node(listaDeTokens.removeFirst()));
            if(!listaDeTokens.getFirst().valor().equals("(")) {
                throw new RuntimeException("FALTOU O () NO IF");
            }
            arvore.addChild(new Node(listaDeTokens.removeFirst()));
            expressao(arvore);
            if(!listaDeTokens.getFirst().valor().equals(")")) {
                throw new RuntimeException("PARENTESES NÃO FECHADO");
            }
            arvore.addChild(new Node(listaDeTokens.removeFirst()));
            bloco(arvore);
            C1(arvore);
        }
    }

    private void C1(Node arvore) {
        if(listaDeTokens.isEmpty()) return;
        if(listaDeTokens.getFirst().valor().equals("}")) return;
        if(listaDeTokens.getFirst().valor().equals("else")) {
            arvore.addChild(new Node(listaDeTokens.removeFirst()));
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
            arvore.addChild(new Node(listaDeTokens.removeFirst()));
            sentenca(arvore);
            if(listaDeTokens.isEmpty() || !listaDeTokens.getFirst().valor().equals("}")) {
                throw new RuntimeException("Bloco não fechado!");
            }
            arvore.addChild(new Node(listaDeTokens.removeFirst()));
        }
    }

    public Node declaracao(Node arvore) {
        if(!(listaDeTokens.getFirst().tipo() == Recursos.TIPO)) {
            throw new RuntimeException("DECLARACAO SEM TIPO");
        }
        arvore.addChild(new Node(listaDeTokens.removeFirst()));
        if(!(listaDeTokens.getFirst().tipo() == Recursos.IDENTIFICADOR)) {
            throw new RuntimeException("FALTA O NOME DO IDENTIFICADOR");
        }
        arvore.addChild(new Node(listaDeTokens.removeFirst()));
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
            expressaoLogica(arvore);
        }
    }

    private void OP(Node arvore) {
        if(operadorDeAtribuicao()) {
            arvore.addChild(new Node(listaDeTokens.removeFirst()));
        }
    }

    public void COMP(Node arvore) {
        if(operadorDeComparacao()) {
            arvore.addChild(new Node(listaDeTokens.removeFirst()));
        }
    }

    public Node expressao(Node arvore) {
        if(a() || listaDeTokens.getFirst().valor().equals("(") || listaDeTokens.getFirst().valor().equals("!")) {
            atribuicao(arvore);
        }
        return null;
    }

    public Node atribuicao(Node arvore) {
        if(a() || listaDeTokens.getFirst().valor().equals("(") || listaDeTokens.getFirst().valor().equals("!")) {
            expressaoLogica(arvore);
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

    public void expressaoLogica(Node arvore) {
        Token token = listaDeTokens.getFirst();
        if(a() || token.valor().equals("(") || token.valor().equals("!")) {
            TL(arvore);
            EL1(arvore);
        }
    }


    public void EL1(Node arvore) {
        if(listaDeTokens.isEmpty()) return;
        Token token = listaDeTokens.getFirst();
        if(token.valor().equals(";") || token.valor().equals(")") || operadorDeAtribuicao()) return;
        if(token.valor().equals("||")) {
            arvore.addChild(new Node(listaDeTokens.removeFirst()));
            TL(arvore);
            EL1(arvore);
        }
    }

    public void TL(Node arvore) {
        Token token = listaDeTokens.getFirst();
        if(a() || token.valor().equals("(") || token.valor().equals("!")) {
            expressaoRelacional(arvore);
            TL1(arvore);
        }
    }

    public void TL1(Node arvore) {
        if(listaDeTokens.isEmpty()) return;
        Token token = listaDeTokens.getFirst();
        if(token.valor().equals(")") ||
           token.valor().equals("||") ||
           token.valor().equals(";") ||
           operadorDeAtribuicao()){
            return;
        }
        if(token.valor().equals("&&")) {
            arvore.addChild(new Node(listaDeTokens.removeFirst()));
            expressaoRelacional(arvore);
            TL1(arvore);
        }
    }

    public void expressaoRelacional(Node arvore) {
        Token token = listaDeTokens.getFirst();
        if(a() || token.valor().equals("(") || token.valor().equals("!")){
            expressaoAritmetica(arvore);
            ER1(arvore);
        }
    }

    public void ER1(Node arvore) {
        if(listaDeTokens.isEmpty()) return;
        Token token = listaDeTokens.getFirst();
        if(token.valor().equals("&&") ||
            token.valor().equals("||") ||
            token.valor().equals(";") ||
            operadorDeAtribuicao()
        ) {
            return;
        }
        if(operadorDeComparacao()) {
            COMP(arvore);
            expressaoAritmetica(arvore);
        }
    }

    public void expressaoAritmetica(Node arvore) {
        Token token = listaDeTokens.getFirst();
        if(a() || token.valor().equals("(") || token.valor().equals("!")) {
            T(arvore);
            EA1(arvore);
        }
    }

    public void EA1(Node arvore) {
        if(listaDeTokens.isEmpty()) return;
        Token token = listaDeTokens.getFirst();
        if(token.valor().equals(")") ||
            token.valor().equals("&&") ||
            token.valor().equals("||") ||
            token.valor().equals(";") ||
            operadorDeAtribuicao() ||
            operadorDeComparacao()
        ) {
            return;
        }
        if(token.valor().equals("+") || token.valor().equals("-")) {
            arvore.addChild(new Node(listaDeTokens.removeFirst()));
            T(arvore);
            EA1(arvore);
        }
    }

    private void T(Node arvore) {
        Token token = listaDeTokens.getFirst();
        if(a() || token.valor().equals("(") || token.valor().equals("!")) {
            F(arvore);
            T1(arvore);
        }
    }

    public void T1(Node arvore) {
        if(listaDeTokens.isEmpty()) return;
        Token token = listaDeTokens.getFirst();
        if(token.valor().equals(")") ||
                token.valor().equals("&&") ||
                token.valor().equals("||") ||
                token.valor().equals(";") ||
                operadorDeAtribuicao() ||
                operadorDeComparacao()
        ) {
            return;
        }
        if(token.valor().equals("*") || token.valor().equals("/")) {
            arvore.addChild(new Node(listaDeTokens.removeFirst()));
            F(arvore);
            T1(arvore);
        }
    }

    private void F(Node arvore) {
        Token token = listaDeTokens.getFirst();
        if(token.valor().equals("!")) {
            arvore.addChild(new Node(listaDeTokens.removeFirst()));
            F(arvore);
            return;
        }

        if(token.valor().equals("(")) {
            arvore.addChild(new Node(listaDeTokens.removeFirst()));
            expressaoLogica(arvore);
            arvore.addChild(new Node(listaDeTokens.removeFirst()));
            return;
        }

        if(token.tipo() == Recursos.IDENTIFICADOR || token.tipo() == Recursos.NUMERICO) {
            arvore.addChild(new Node(listaDeTokens.removeFirst()));
            P(arvore);
        } else {
            arvore.addChild(new Node(listaDeTokens.removeFirst()));
        }

    }

    private void P(Node arvore) {
        if(listaDeTokens.isEmpty()) return;
        Token token = listaDeTokens.getFirst();
        if(token.equals("++") || token.equals("--")) {
            arvore.addChild(new Node(listaDeTokens.removeFirst()));
        }
    }

    public Node repeticao(Node arvore) {
        return null;
    }

}
