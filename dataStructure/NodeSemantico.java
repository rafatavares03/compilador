package dataStructure;

import recursos.Tipos;

public class NodeSemantico {
    final private Node node;
    final private Tipos tipo;
    private String valor;

    public NodeSemantico(Node node, Tipos tipo) {
        this.node = node;
        this.tipo = tipo;
        this.valor = null;
    }

    public NodeSemantico(Node node, Tipos tipo, String valor) {
        this.node = node;
        this.tipo = tipo;
        this.valor = valor;
    }

    public Node getNode() {
        return node;
    }

    public Tipos getTipo() {
        return tipo;
    }

    public void setValor(String valor) {
        this.valor = valor;
    }

    public String getValor() {
        return valor;
    }

    public String toString() {
        if(this.node != null && this.node.getToken() != null) return this.node.getToken().valor() + " " + this.tipo + " " + this.valor;
        return this.tipo + " " + this.valor;
    }
}
