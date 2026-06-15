package frontend;

import dataStructure.NodeSemantico;
import exception.InvalidOperationException;
import exception.SyntaticException;
import recursos.Recursos;
import recursos.Sentencas;
import recursos.Tipo;
import recursos.Tipos;
import token.Token;
import dataStructure.Node;
import token.TokenID;

import java.util.Deque;
import java.util.HashMap;
import java.util.Stack;
import java.util.regex.Pattern;

public class AnalisadorSintatico {
    private Deque<Token> listaDeTokens;
    private Stack<HashMap<String, TokenID>> escopos;
    private boolean erroSintatico = false;

    public Node executarAnalise(Deque<Token>tokens) {
        if(tokens.isEmpty()) {
            return null;
        }
        this.listaDeTokens = tokens;
        escopos = new Stack<>();
        escopos.push(new HashMap<>());
        Node arvoreSintatica = new Node(Sentencas.PROGRAMA);
        sentenca(arvoreSintatica);
        while(erroSintatico && !listaDeTokens.isEmpty()) {
            listaDeTokens.removeFirst();
            sentenca(arvoreSintatica);
        }
        if(!erroSintatico){
            //arvoreSintatica.print("");
            printTabelaDeSimbolos();
        }
        return arvoreSintatica;
    }

    private void printTabelaDeSimbolos() {
        for(int i = 0; i < escopos.size(); i++) {
            System.out.println("Escopo " + i);
            for(TokenID simbolo : escopos.get(i).values()) {
                System.out.println(
                        "  " + simbolo.getToken().valor() + " " + simbolo.getTipo() + " " + simbolo.getValor() + " " + simbolo.getToken().linha() + " " + simbolo.getToken().coluna()
                );
            }
        }
    }

    private TokenID buscarIdentificador(String nome) {
        for(int i = escopos.size() - 1; i >= 0; i--) {
            HashMap<String, TokenID> escopo = escopos.get(i);
            if(escopo.containsKey(nome)) {
                return escopo.get(nome);
            }
        }
        return null;
    }

    private void consumirToken(Node raiz) {
        if(listaDeTokens.isEmpty()) {
            throw new SyntaticException("Lista de tokens está vazia, não possível adicionar novos tokens à árvore sintática.", raiz.getToken());
        }
        raiz.addChild(new Node(listaDeTokens.removeFirst()));
    }

    private Tipos getTipo(Token token) {
        if(Pattern.compile("[0-9]+(\\.[0-9]+)").matcher(token.valor()).matches()) {
            return Tipos.FLOAT;
        }

        if(Pattern.compile("[0-9]+").matcher(token.valor()).matches()) {
            return Tipos.INT;
        }

        if(token.valor().contains("\"") || token.valor().contains("\'")) {
            return Tipos.STRING;
        }

        if(Pattern.compile("true|false").matcher(token.valor()).matches()) {
            return Tipos.BOOLEAN;
        }

        return Tipos.NULL;
    }

    private Tipos verificaSoma(NodeSemantico t1, NodeSemantico t2) {
        if(t1.getTipo() == Tipos.INT && t2.getTipo() == Tipos.INT) {
            return Tipos.INT;
        }

        if(t1.getTipo() == Tipos.INT && t2.getTipo() == Tipos.FLOAT ||
            t1.getTipo() == Tipos.FLOAT && t2.getTipo() == Tipos.INT ||
                t1.getTipo() == Tipos.FLOAT && t2.getTipo() == Tipos.FLOAT
        ) {
            return Tipos.FLOAT;
        }

        if(t1.getTipo() == Tipos.STRING && t2.getTipo() == Tipos.STRING ||
            t1.getTipo() == Tipos.STRING && t2.getTipo() == Tipos.INT ||
                t1.getTipo() == Tipos.INT && t2.getTipo() == Tipos.STRING ||
                t1.getTipo() == Tipos.FLOAT && t2.getTipo() == Tipos.STRING ||
                t1.getTipo() == Tipos.STRING && t2.getTipo() == Tipos.FLOAT
        ) {
            return Tipos.STRING;
        }

        return Tipos.ERRO;
    }

    private Tipos verificaOperacaoAritmetica(NodeSemantico t1, NodeSemantico t2) {
        if(t1.getTipo() == Tipos.INT && t2.getTipo() == Tipos.INT) {
            return Tipos.INT;
        }

        if(t1.getTipo() == Tipos.INT && t2.getTipo() == Tipos.FLOAT ||
                t1.getTipo() == Tipos.FLOAT && t2.getTipo() == Tipos.INT ||
                t1.getTipo() == Tipos.FLOAT && t2.getTipo() == Tipos.FLOAT
        ) {
            return Tipos.FLOAT;
        }

        return Tipos.ERRO;
    }

    private Tipos verificaOperacaoLogica(NodeSemantico t1, NodeSemantico t2) {
        if(t1.getTipo() == Tipos.BOOLEAN && t2.getTipo() == Tipos.BOOLEAN) {
            return Tipos.BOOLEAN;
        }

        return Tipos.ERRO;
    }

    private Tipos verificaOperacaoRelacional(NodeSemantico t1, NodeSemantico t2) {
        if(t1.getTipo() == Tipos.INT || t1.getTipo() == Tipos.FLOAT &&
                t2.getTipo() == Tipos.INT || t2.getTipo() == Tipos.FLOAT) {
            return Tipos.BOOLEAN;
        }

        return Tipos.ERRO;
    }

    private Tipos verificaAtribuicao(Tipos tipo, Tipos conteudo) {
        if(tipo == Tipos.FLOAT) {
            if(conteudo == Tipos.FLOAT || conteudo == Tipos.INT) {
                return Tipos.FLOAT;
            }
        }

        if(tipo == conteudo) {
            return tipo;
        }

        return Tipos.ERRO;
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
        try{
            if(listaDeTokens.isEmpty()) return;
            Token token = listaDeTokens.getFirst();
            if(token.valor().equals("if")) {
                arvore.addChild(condicao());
                S1(arvore);
                return;
            }

            if(token.tipo() == Recursos.TIPO) {
                arvore.addChild(declaracao());
                if(listaDeTokens.isEmpty() || !listaDeTokens.getFirst().valor().equals(";")) {
                    throw new SyntaticException("Esperado o caractere \";\" após a declaração.", arvore.getLastDescendant().getToken());
                }
                consumirToken(arvore);
                S1(arvore);
                return;
            }

            if(a() || token.valor().equals("!") || token.valor().equals("(")) {
                NodeSemantico atr = atribuicao();
                if(atr != null && atr.getNode() != null) {
                    arvore.addChild(atr.getNode());
                }
                if(listaDeTokens.isEmpty() || !listaDeTokens.getFirst().valor().equals(";")) {
                    throw new SyntaticException("Esperado o caractere \";\" após a " + ((atr.getNode().getType() == Sentencas.ATRIBUICAO) ? "atribuição." : "expressão."), arvore.getLastDescendant().getToken());
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

            if(listaDeTokens.getFirst().valor().equals("}")) {
                throw new SyntaticException("Bloco vazio.", listaDeTokens.getFirst());
            } else {
                throw new SyntaticException("Símbolo não esperado.", listaDeTokens.getFirst());
            }

        } catch (SyntaticException e) {
            erroSintatico = true;
            System.out.println("ERRO: " + e.getMessage());
            if(e.getToken() != null) System.out.println("\t" + e.getToken().linha() + ":" + e.getToken().coluna() + " - Proxímo a \"" + e.getToken().valor() + "\".");
            sincronizar(e.getToken());
        } catch (InvalidOperationException e) {
            erroSintatico = true;
            NodeSemantico t1 = e.getT1();
            NodeSemantico t2 = e.getT2();
            if(e.getMessage().contains("Atribuição")) {
                System.out.println("ERRO: " + e.getMessage() + " Não é possível atribuir " + t2.getTipo() + " a uma variável do tipo " + t1.getTipo() + ".");
            } else {
                System.out.println("ERRO: " + e.getMessage() + t1.getNode().getToken().valor() + " é do tipo " + t1.getTipo() + ", enquanto " + t2.getNode().getToken().valor() + " é do tipo" + t2.getTipo() + ".");
            }
            if(t2.getNode() != null && t2.getNode().getToken() != null) System.out.println("\t" + t2.getNode().getToken().linha() + ":" + t2.getNode().getToken().coluna() + " - Proxímo a \"" + t2.getNode().getToken().valor() + "\".");
            sincronizar(t2.getNode().getToken());
        }
    }

    private void sincronizar(Token exceptionToken) {
        while(!listaDeTokens.isEmpty()) {
            String token = listaDeTokens.getFirst().valor();
            if(exceptionToken.tipo() == Recursos.IDENTIFICADOR && listaDeTokens.getFirst() == exceptionToken) {
                listaDeTokens.removeFirst();
            }
            if(a() || listaDeTokens.getFirst().tipo() == Recursos.TIPO || token.equals("if") || token.equals("for") || token.equals("while")) {
                break;
            }
            listaDeTokens.removeFirst();
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
            throw new SyntaticException("Condição iniciada incorretamente.", listaDeTokens.getFirst());
        }
        Node NO = new Node(Sentencas.CONDICAO);
        consumirToken(NO);
        if(!listaDeTokens.getFirst().valor().equals("(")) {
            throw new SyntaticException("Esperado o caractere \"(\". A condição é na forma de if(<expressão>).", NO.getLastDescendant().getToken());
        }
        consumirToken(NO);
        NodeSemantico resultado = expressao();
        if(resultado != null && resultado.getNode() != null) {
            NO.addChild(resultado.getNode());
        }
        if(!listaDeTokens.getFirst().valor().equals(")")) {
            throw new SyntaticException("O parênteses da expressão condicional não foi encerrado.", NO.getChildNodes().getFirst().getToken());
        }
        consumirToken(NO);
        bloco(NO);
        if(resultado.getTipo() != Tipos.BOOLEAN) {
            throw new SyntaticException("A expressão da condição não é do tipo BOOLEAN", NO.getChildNodes().getFirst().getToken());
        }
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
        escopos.push(new HashMap<>());
        try {
            if(listaDeTokens.isEmpty() || !listaDeTokens.getFirst().valor().equals("{")) {
                throw new SyntaticException("Bloco não inicializado, esperado \"{\".", arvore.getLastDescendant().getToken());
            }

            consumirToken(arvore);
            sentenca(arvore);
            if(listaDeTokens.isEmpty() || !listaDeTokens.getFirst().valor().equals("}")) {
                throw new SyntaticException("Bloco não encerrado, esperado \"}\".", arvore.getLastDescendant().getToken());
            }
            consumirToken(arvore);
        } catch (SyntaticException e) {
            erroSintatico = true;
            System.out.println("ERRO: " + e.getMessage());
        } catch (Exception e) {
            throw new RuntimeException(e);
        } finally {
            escopos.pop();
        }
    }

    public Node declaracao() {
        Node NO = new Node(Sentencas.DECLARACAO);
        Token type = listaDeTokens.getFirst();
        if(!(type.tipo() == Recursos.TIPO)) {
            throw new SyntaticException("Não é possível declarar uma variável sem tipo especificado.", listaDeTokens.getFirst());
        }
        consumirToken(NO);
        Token identificador = listaDeTokens.getFirst();
        if(!(identificador.tipo() == Recursos.IDENTIFICADOR)) {
            throw new SyntaticException("Identificador não especificado na declaração.", listaDeTokens.getFirst());
        }
        HashMap<String, TokenID> escopoAtual = escopos.peek();
        TokenID id = escopoAtual.get(identificador.valor());
        if(id != null) {
            throw new SyntaticException("Identificador \"" + identificador.valor() + "\" já foi declarado na linha " + id.getToken().linha() + ".", identificador);
        }
        consumirToken(NO);
        NodeSemantico atr = D2(Tipos.fromString(type.valor()));
        Node atribuicao = (atr != null) ? atr.getNode() : null;
        String conteudo = (atr != null) ? atr.getValor() : null;
        if(atribuicao != null) {
            NO.addChild(atribuicao);
        }
        escopoAtual.put(identificador.valor(), new TokenID(identificador, Tipos.fromString(type.valor()), conteudo));
        D1(NO, Tipos.fromString(type.valor()));
        return NO;
    }

    private void D1(Node declaracao, Tipos tipo) {
        if(listaDeTokens.isEmpty()) return;
        if(listaDeTokens.getFirst().valor().equals(";")) return;
        if(listaDeTokens.getFirst().valor().equals(",")) {
            consumirToken(declaracao);
            if(listaDeTokens.getFirst().tipo() != Recursos.IDENTIFICADOR) {
                throw new SyntaticException("Token inválido, esperado um identificador.", listaDeTokens.getFirst());
            }
            Token identificador = listaDeTokens.getFirst();
            HashMap<String, TokenID> escopoAtual = escopos.peek();
            consumirToken(declaracao);
            NodeSemantico atr = D2(tipo);
            Node atribuicao = (atr != null) ? atr.getNode() : null;
            String conteudo = (atr != null) ? atr.getValor() : null;
            if(atribuicao != null) {
                declaracao.addChild(atribuicao);
            }
            escopoAtual.put(identificador.valor(), new TokenID(identificador, tipo, conteudo));
            D1(declaracao, tipo);
        }
    }

    private NodeSemantico D2(Tipos tipo) {
        if(listaDeTokens.isEmpty()) return null;
        Token token = listaDeTokens.getFirst();
        if(token.valor().equals(",") || token.valor().equals(";")){
            return null;
        }
        if(operadorDeAtribuicao()) {
            Node operador = OP();
            NodeSemantico conteudo = D3();
            if(operador == null) {
                throw new SyntaticException("Erro ao processar operador de atribuição", listaDeTokens.getFirst());
            }
            if(conteudo == null || conteudo.getNode() == null) {
                throw new SyntaticException("Token inválido, esperado um valor de variável ou expressão", listaDeTokens.getFirst());
            }
            Tipos opTipo;
            if(operador.getToken().valor().length() > 1) {
                String operacao = operador.getToken().valor().substring(1,1);
                if(operacao.equals("+")) {
                    opTipo = verificaSoma(new NodeSemantico(null, tipo), conteudo);
                } else {
                    opTipo = verificaOperacaoAritmetica(new NodeSemantico(null, tipo), conteudo);
                }
            } else {
                opTipo = conteudo.getTipo();
            }
            Tipos resultado = verificaAtribuicao(tipo, opTipo);
            if(resultado == Tipos.ERRO) {
                throw new InvalidOperationException("Atribuição inválida.", operador.getToken().valor(), new NodeSemantico(null, tipo), conteudo);
            }
            operador.addChild(conteudo.getNode());
            return new NodeSemantico(operador, tipo, conteudo.getValor());
        }
        return null;
    }

    private NodeSemantico D3() {
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

    public NodeSemantico atribuicao() {
        if(a() || listaDeTokens.getFirst().valor().equals("(") || listaDeTokens.getFirst().valor().equals("!")) {
            NodeSemantico esquerda = expressao();
            if(esquerda == null) {
                return null;
            }
            if(listaDeTokens.isEmpty() || !operadorDeAtribuicao(listaDeTokens.getFirst().valor())) {
                return esquerda;
            }

            Node NO = new Node(Sentencas.ATRIBUICAO);
            Node primeiroFilho = esquerda.getNode().getChildNodes().getFirst();
            NO.addChild(primeiroFilho);
            Token identificador = primeiroFilho.getToken();
            TokenID id = buscarIdentificador(identificador.valor());
            if(id == null) {
                throw new SyntaticException("Identificador não declarado: " + identificador.valor(), identificador);
            }
            ATR1(NO);
            return new NodeSemantico(NO, id.getTipo());
        }
        return null;
    }

    public void ATR1(Node atribuicao) {
        if(listaDeTokens.isEmpty()) return;
        Token token = listaDeTokens.getFirst();
        if(token.valor().equals(")") || token.valor().equals(";") || token.valor().equals(",")) return;
        Node operador = OP();
        if(operador != null) {
            atribuicao.addChild(operador);
            NodeSemantico exp = expressao();
            if(exp != null && exp.getNode() != null && !exp.getNode().getChildNodes().isEmpty()) {
                operador.addChild(exp.getNode().getChildNodes().getFirst());
                Token identificador = atribuicao.getChildNodes().getFirst().getToken();
                TokenID id = buscarIdentificador(identificador.valor());
                if(id != null) {
                    id.setValor(exp.getValor());
                }
            } else {
                throw new SyntaticException("Atribuição vazia, esperado uma expressão.", operador.getToken());
            }
        }
    }

    public NodeSemantico expressao() {
        if(a() || listaDeTokens.getFirst().valor().equals("(") || listaDeTokens.getFirst().valor().equals("!")) {
            Node NO = new Node(Sentencas.EXPRESSAO);
            NodeSemantico resultado = expressaoLogica();
            NO.addChild(resultado.getNode());
            return new NodeSemantico(NO, resultado.getTipo(), resultado.getValor());
        }
        return null;
    }

    public NodeSemantico expressaoLogica() {
        if(listaDeTokens.isEmpty()) {
            throw new SyntaticException("Fim de arquivo, esperado expressão.", null);
        }
        Token token = listaDeTokens.getFirst();
        if(a() || token.valor().equals("(") || token.valor().equals("!")) {
            NodeSemantico esquerda = TL();
            return EL1(esquerda);
        }
        return null;
    }


    public NodeSemantico EL1(NodeSemantico esquerda) {
        if(listaDeTokens.isEmpty()) return esquerda;
        Token token = listaDeTokens.getFirst();
        if(token.valor().equals(",") || token.valor().equals(";") || token.valor().equals(")") || operadorDeAtribuicao()) return esquerda;
        if(token.valor().equals("||")) {
            Node OR = new Node(listaDeTokens.removeFirst());
            OR.addChild(esquerda.getNode());
            NodeSemantico direita = TL();
            if(direita == null) {
                throw new SyntaticException("Expressão inválida, esperado termo após o operador \"||\"", OR.getLastDescendant().getToken());
            }
            OR.addChild(direita.getNode());
            Tipos opTipo = verificaOperacaoLogica(esquerda, direita);
            if(opTipo == Tipos.ERRO) {
                throw new InvalidOperationException("Operação inválida.", OR.getToken().valor(), esquerda, direita);
            }
            String opValue = (direita.getValor() != null) ? esquerda.getValor() + OR.getToken().valor() + direita.getValor() : null;
            return T1(new NodeSemantico(OR, opTipo, opValue));
        }
        return esquerda;
    }

    public NodeSemantico TL() {
        Token token = listaDeTokens.getFirst();
        if(a() || token.valor().equals("(") || token.valor().equals("!")) {
            NodeSemantico esquerda = expressaoRelacional();
            return TL1(esquerda);
        }
        return null;
    }

    public NodeSemantico TL1(NodeSemantico esquerda) {
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
            AND.addChild(esquerda.getNode());
            NodeSemantico direita = expressaoRelacional();
            if(direita == null) {
                throw new SyntaticException("Expressão inválida, esperado termo após o operador \"&&\"", AND.getLastDescendant().getToken());
            }
            AND.addChild(direita.getNode());
            Tipos opTipo = verificaOperacaoLogica(esquerda, direita);
            if(opTipo == Tipos.ERRO) {
                throw new InvalidOperationException("Operação inválida.", AND.getToken().valor(), esquerda, direita);
            }
            String opValue = (direita.getValor() != null) ? esquerda.getValor() + AND.getToken().valor() + direita.getValor() : null;
            return TL1(new NodeSemantico(AND, opTipo, opValue));
        }
        return esquerda;
    }

    public NodeSemantico expressaoRelacional() {
        Token token = listaDeTokens.getFirst();
        if(a() || token.valor().equals("(") || token.valor().equals("!")){
            NodeSemantico esquerda = expressaoAritmetica();
            return ER1(esquerda);
        }
        return null;
    }

    public NodeSemantico ER1(NodeSemantico esquerda) {
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
            operador.addChild(esquerda.getNode());
            NodeSemantico direita = expressaoAritmetica();
            if(direita == null) {
                throw new SyntaticException("Expressão inválida, esperado termo após o operador \"" + operador.getToken().valor() + "\"", operador.getLastDescendant().getToken());
            }
            operador.addChild(direita.getNode());
            Tipos opTipo = verificaOperacaoRelacional(esquerda, direita);
            if(opTipo == Tipos.ERRO) {
                throw new InvalidOperationException("Operação inválida.", operador.getToken().valor(), esquerda, direita);
            }
            String opValue = (direita.getValor() != null) ? esquerda.getValor() + operador.getToken().valor() + direita.getValor() : null;
            return ER1(new NodeSemantico(operador, opTipo, opValue));
        }
        return esquerda;
    }

    public NodeSemantico expressaoAritmetica() {
        Token token = listaDeTokens.getFirst();
        if(a() || token.valor().equals("(") || token.valor().equals("!")) {
            NodeSemantico esquerda = T();
            return EA1(esquerda);
        }
        return null;
    }

    public NodeSemantico EA1(NodeSemantico esquerda) {
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
            operador.addChild(esquerda.getNode());
            NodeSemantico direita = T();
            if(direita == null) {
                String op = (operador.getToken() == null) ? "." : " \"" + operador.getToken().valor() + "\"";
                throw new SyntaticException("Expressão inválida, esperado termo após o operador" + op, operador.getLastDescendant().getToken());
            }
            operador.addChild(direita.getNode());
            Tipos opTipo;
            if(token.valor().equals("+")) {
                opTipo = verificaSoma(esquerda,direita);
            } else {
                opTipo = verificaOperacaoAritmetica(direita, esquerda);
            }
            if(opTipo == Tipos.ERRO) {
                throw new InvalidOperationException("Operação inválida.", operador.getToken().valor(), esquerda, direita);
            }
            String opValue = (direita.getValor() != null) ? esquerda.getValor() + operador.getToken().valor() + direita.getValor() : null;
            return EA1(new NodeSemantico(operador, opTipo, opValue));
        }
        return esquerda;
    }

    private NodeSemantico T() {
        Token token = listaDeTokens.getFirst();
        if(a() || token.valor().equals("(") || token.valor().equals("!")) {
            NodeSemantico esquerda = F();
            return T1(esquerda);
        }
        return null;
    }

    public NodeSemantico T1(NodeSemantico esquerda) {
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
            operador.addChild(esquerda.getNode());
            NodeSemantico direita = F();
            operador.addChild(direita.getNode());
            Tipos opTipo = verificaOperacaoAritmetica(esquerda, direita);
            if(opTipo == Tipos.ERRO) {
                throw new InvalidOperationException("Operação inválida.", operador.getToken().valor(), esquerda, direita);
            }
            String opValue = (direita.getValor() != null) ? esquerda.getValor() + operador.getToken().valor() + direita.getValor() : null;
            return T1(new NodeSemantico(operador, opTipo, opValue));
        }

        return esquerda;
    }

    private NodeSemantico F() {
        Token token = listaDeTokens.getFirst();
        if(token.valor().equals("!")) {
            Node negacao = new Node(listaDeTokens.removeFirst());
            NodeSemantico NS = F();
            Node filho = NS.getNode();
            negacao.addChild(filho);
            return new NodeSemantico(negacao, Tipos.BOOLEAN);
        }

        if(token.valor().equals("(")) {
            Node expressao = new Node(Sentencas.EXPRESSAO);
            expressao.addChild(new Node(listaDeTokens.removeFirst()));
            NodeSemantico conteudo = expressaoLogica();
            expressao.addChild(conteudo.getNode());
            expressao.addChild(new Node(listaDeTokens.removeFirst()));
            return new NodeSemantico(expressao, conteudo.getTipo());
        }

        if(token.tipo() == Recursos.IDENTIFICADOR || token.tipo() == Recursos.NUMERICO) {
            TokenID id = buscarIdentificador(token.valor());
            if(token.tipo() == Recursos.IDENTIFICADOR && id == null) {
                throw new SyntaticException("Identificador não declarado: " + token.valor(), token);
            }
            Node NO = new Node(listaDeTokens.removeFirst());
            Node iterador = P();
            if(iterador != null) {
                NO.addChild(iterador);
            }
            Tipos nodeTipo = (token.tipo() == Recursos.IDENTIFICADOR) ? id.getTipo() : getTipo(token);
            String nodeValue = (token.tipo() == Recursos.IDENTIFICADOR) ? id.getValor() : token.valor();
            NodeSemantico NS = new NodeSemantico(NO, nodeTipo, nodeValue);
            return NS;
        }

        Tipos nodeTipo = getTipo(token);
        String nodeValue = null;
        if(nodeTipo != Tipos.NULL) {
            nodeValue = token.valor();
        }
        return new NodeSemantico(new Node(listaDeTokens.removeFirst()), nodeTipo, nodeValue);
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
                throw new SyntaticException("Esperado o caractere \"(\". A repetição é na forma de while(<expressão>).", NO.getLastDescendant().getToken());
            }
            consumirToken(NO);
            NodeSemantico resultado = expressao();
            if(resultado != null) {
                NO.addChild(resultado.getNode());
            }
            if(listaDeTokens.isEmpty() || !listaDeTokens.getFirst().valor().equals(")")) {
                throw new SyntaticException("O parênteses do while não foi encerrado.", NO.getChildNodes().getFirst().getToken());
            }
            consumirToken(NO);
            bloco(NO);
            return NO;
        }

        if(listaDeTokens.getFirst().valor().equals("for")) {
            escopos.push(new HashMap<>());
            try {
                consumirToken(NO);
                if(listaDeTokens.isEmpty() || !listaDeTokens.getFirst().valor().equals("(")) {
                    throw new SyntaticException("Esperado o caractere \"(\". A repetição é na forma de for(<declaração>;<expressão>;<expressão>). A declaração e expressão podem ser vazias, porém o formato deve se mantido.", NO.getLastDescendant().getToken());
                }
                consumirToken(NO);
                R1(NO);
                if(listaDeTokens.isEmpty() || !listaDeTokens.getFirst().valor().equals(";")) {
                    throw new SyntaticException("Esperado \";\"", listaDeTokens.getFirst());
                }
                consumirToken(NO);
                R2(NO);
                if(listaDeTokens.isEmpty() || !listaDeTokens.getFirst().valor().equals(";")) {
                    throw new SyntaticException("Esperado \";\"", listaDeTokens.getFirst());
                }
                consumirToken(NO);
                R2(NO);
                R3(NO);
                if(listaDeTokens.isEmpty() || !listaDeTokens.getFirst().valor().equals(")")) {
                    throw new SyntaticException("O parênteses do for não foi encerrado.", NO.getChildNodes().getFirst().getToken());
                }
                consumirToken(NO);
                bloco(NO);
                return NO;
            } catch (SyntaticException e) {
                erroSintatico = true;
                System.out.println("ERRO: " + e.getMessage());
            } finally {
                escopos.pop();
            }
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
            repeticao.addChild(expressao().getNode());
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
