package frontend;

import recursos.Recursos;
import recursos.Sentencas;
import token.Token;
import dataStructure.Node;

import java.util.Comparator;
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

    public Node sentenca(Node arvore) {
        Token token = listaDeTokens.getFirst();
        Node folha = null;
        if(token.valor().equals("if")) {
            folha = new Node(Sentencas.CONDICAO);
            condicao(folha);
        }

        if(token.tipo() == Recursos.TIPO) {
            System.out.println(token.valor());
            System.out.println(this.listaDeTokens.getFirst());
            folha = new Node(Sentencas.DECLARACAO);
            declaracao(folha);
            arvore.addChild(new Node(this.listaDeTokens.removeFirst()));
        }

        if(a() || token.valor().equals("!") || token.valor().equals("(")) {
            folha = new Node(Sentencas.EXPRESSAO);
            expressao(folha);
        }

        if(token.valor().equals("while") || token.valor().equals(("for"))) {
            folha = new Node(Sentencas.REPETICAO);
            repeticao(folha);
        }
        if(folha != null) {
            arvore.addChild(folha);
        }
        S1(arvore);

        return null;
    }

    public Node S1(Node arvore) {
        if(listaDeTokens.isEmpty() || listaDeTokens.getFirst().valor().equals("}")) {
            return null;
        }
        sentenca(arvore);
        S1(arvore);
        return arvore;
    }

    public Node condicao(Node arvore) {
        System.out.println("IF");
        return null;
    }

    public Node declaracao(Node arvore) {
        System.out.println(listaDeTokens.getFirst().valor());
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
        if(a()) {
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
        if(a() || listaDeTokens.getFirst().equals("(") || listaDeTokens.getFirst().equals("!")) {
            atribuicao(arvore);
        }
        return null;
    }

    public Node atribuicao(Node arvore) {
        if(a() || listaDeTokens.getFirst().equals("(") || listaDeTokens.getFirst().equals("!")) {
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
        if(a()) {
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
        if(a()) {
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
        if(a()){
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
        if(a()) {
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
        if(a()) {
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
        }

        if(token.valor().equals("(")) {
            arvore.addChild(new Node(listaDeTokens.removeFirst()));
            expressao(arvore);
            arvore.addChild(new Node(listaDeTokens.removeFirst()));
        }

        if(token.tipo() == Recursos.IDENTIFICADOR || token.tipo() == Recursos.NUMERICO) {
            arvore.addChild(new Node(listaDeTokens.removeFirst()));
            P(arvore);
        }

        arvore.addChild(new Node(listaDeTokens.removeFirst()));
    }

    private void P(Node arvore) {
        if(listaDeTokens.isEmpty()) return;
        Token token = listaDeTokens.getFirst();
        if(token.equals("++") || token.equals("--")) {
            arvore.addChild(new Node(listaDeTokens.removeFirst()));
        }
    }

    public Node repeticao(Node arvore) {
        System.out.println("REPETIÇÃO");
        return null;
    }

}
