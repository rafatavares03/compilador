package exception;

import dataStructure.NodeSemantico;
import token.Token;

public class InvalidOperationException extends RuntimeException {
    private final String operator;
    private final NodeSemantico t1;
    private final NodeSemantico t2;
    public InvalidOperationException(String message, String operator, NodeSemantico t1, NodeSemantico t2) {
        super(message);
        this.operator = operator;
        this.t1 = t1;
        this.t2 = t2;
    }

    public String getOperator() {
        return operator;
    }

    public NodeSemantico getT1() {
        return t1;
    }

    public NodeSemantico getT2() {
        return t2;
    }
}
