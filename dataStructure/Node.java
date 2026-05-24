package dataStructure;

import recursos.Sentencas;
import token.Token;

import java.util.ArrayList;

public class Node {
    private final Token token;
    private final Sentencas type;
    private final ArrayList<Node> childNodes = new ArrayList<>();

    public Node(Token token) {
        this.token = token;
        this.type = Sentencas.TOKEN;
    }

    public Node(Sentencas type) {
        this.token = null;
        this.type = type;
    }

    public boolean addChild(Node child) {
        return this.childNodes.add(child);
    }

    public ArrayList<Node> getChildNodes() {
        return this.childNodes;
    }

    public Token getToken(){
        return this.token;
    }

    public Sentencas getType() {
        return this.type;
    }
}
