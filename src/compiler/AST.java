package compiler;

import java.util.*;

import compiler.lib.*;

/**
 * // * This class implements the AST node.
 */
public class AST {

    /**
     * This is the root of the AST.
     */
    public static class ProgLetInNode extends Node {
        final List<DecNode> declist;
        final Node exp;

        ProgLetInNode(List<DecNode> d, Node e) {
            declist = Collections.unmodifiableList(d);
            exp = e;
        }

        @Override
        public <S, E extends Exception> S accept(BaseASTVisitor<S, E> visitor) throws E {
            return visitor.visitNode(this);
        }
    }

    /**
     * This is the root of the AST.
     * It contains the main expression.
     */
    public static class ProgNode extends Node {
        final Node exp;

        ProgNode(Node e) {
            exp = e;
        }

        @Override
        public <S, E extends Exception> S accept(BaseASTVisitor<S, E> visitor) throws E {
            return visitor.visitNode(this);
        }
    }

    /* Declaration nodes */

    /**
     * Function declaration node.
     * It contains the function name, the return type, the list of parameters, the list of local declarations and the body expression.
     */
    public static class FunNode extends DecNode {
        final String id;
        final TypeNode retType;
        final List<ParNode> parList;
        final List<DecNode> decList;
        final Node exp;

        FunNode(String id, TypeNode retType, List<ParNode> parList, List<DecNode> decList, Node exp) {
            this.id = id;
            this.retType = retType;
            this.parList = Collections.unmodifiableList(parList);
            this.decList = Collections.unmodifiableList(decList);
            this.exp = exp;
        }

        void setType(TypeNode t) {
            type = t;
        }

        @Override
        public <S, E extends Exception> S accept(BaseASTVisitor<S, E> visitor) throws E {
            return visitor.visitNode(this);
        }
    }

    /**
     * Parameter declaration node.
     * It contains the parameter name and type.
     */
    public static class ParNode extends DecNode {
        final String id;

        ParNode(String id, TypeNode type) {
            this.id = id;
            this.type = type;
        }

        @Override
        public <S, E extends Exception> S accept(BaseASTVisitor<S, E> visitor) throws E {
            return visitor.visitNode(this);
        }
    }

    /**
     * Variable declaration node.
     * It contains the variable name, type and initial value.
     */
    public static class VarNode extends DecNode {
        final String id;
        final Node exp;

        VarNode(String id, TypeNode type, Node exp) {
            this.id = id;
            this.type = type;
            this.exp = exp;
        }

        @Override
        public <S, E extends Exception> S accept(BaseASTVisitor<S, E> visitor) throws E {
            return visitor.visitNode(this);
        }
    }

    public static class PrintNode extends Node {
        final Node exp;

        PrintNode(Node e) {
            exp = e;
        }

        @Override
        public <S, E extends Exception> S accept(BaseASTVisitor<S, E> visitor) throws E {
            return visitor.visitNode(this);
        }
    }


    /**
     * If-then-else node.
     * It contains the condition, the then branch and the else branch.
     */
    public static class IfNode extends Node {
        final Node condition;
        final Node thenBranch;
        final Node elseBranch;

        IfNode(Node c, Node t, Node e) {
            condition = c;
            thenBranch = t;
            elseBranch = e;
        }

        @Override
        public <S, E extends Exception> S accept(BaseASTVisitor<S, E> visitor) throws E {
            return visitor.visitNode(this);
        }
    }

    /**
     * Equal node.
     * It contains the two expressions.
     * It is used for both equality and assignment.
     */
    public static class EqualNode extends Node {
        final Node left;
        final Node right;

        EqualNode(Node left, Node right) {
            this.left = left;
            this.right = right;
        }

        @Override
        public <S, E extends Exception> S accept(BaseASTVisitor<S, E> visitor) throws E {
            return visitor.visitNode(this);
        }
    }

    /**
     * Greater equal node.
     * It contains the two expressions.
     */
    public static class GreaterEqualNode extends Node {
        final Node left;
        final Node right;

        GreaterEqualNode(Node left, Node right) {
            this.left = left;
            this.right = right;
        }

        @Override
        public <S, E extends Exception> S accept(BaseASTVisitor<S, E> visitor) throws E {
            return visitor.visitNode(this);
        }
    }

    /**
     * Less equal node.
     * It contains the two expressions.
     */
    public static class LessEqualNode extends Node {
        final Node left;
        final Node right;

        LessEqualNode(Node left, Node right) {
            this.left = left;
            this.right = right;
        }

        @Override
        public <S, E extends Exception> S accept(BaseASTVisitor<S, E> visitor) throws E {
            return visitor.visitNode(this);
        }
    }

    /**
     * Not equal node.
     * It contains the expression to negate.
     */
    public static class NotNode extends Node {
        final Node exp;

        NotNode(Node exp) {
            this.exp = exp;
        }

        @Override
        public <S, E extends Exception> S accept(BaseASTVisitor<S, E> visitor) throws E {
            return visitor.visitNode(this);
        }
    }

    /**
     * Or node.
     * It contains the two expressions.
     */
    public static class OrNode extends Node {
        final Node left;
        final Node right;

        OrNode(Node left, Node right) {
            this.left = left;
            this.right = right;
        }

        @Override
        public <S, E extends Exception> S accept(BaseASTVisitor<S, E> visitor) throws E {
            return visitor.visitNode(this);
        }
    }

    /**
     * And node.
     * It contains the two expressions.
     */
    public static class AndNode extends Node {
        final Node left;
        final Node right;

        AndNode(Node left, Node right) {
            this.left = left;
            this.right = right;
        }

        @Override
        public <S, E extends Exception> S accept(BaseASTVisitor<S, E> visitor) throws E {
            return visitor.visitNode(this);
        }
    }

    /**
     * Times node.
     * It contains the two expressions.
     */
    public static class TimesNode extends Node {
        final Node left;
        final Node right;

        TimesNode(Node l, Node r) {
            left = l;
            right = r;
        }

        @Override
        public <S, E extends Exception> S accept(BaseASTVisitor<S, E> visitor) throws E {
            return visitor.visitNode(this);
        }
    }

    /**
     * Division node.
     * It contains the two expressions.
     */
    public static class DivNode extends Node {
        final Node left;
        final Node right;

        DivNode(Node l, Node r) {
            left = l;
            right = r;
        }

        @Override
        public <S, E extends Exception> S accept(BaseASTVisitor<S, E> visitor) throws E {
            return visitor.visitNode(this);
        }
    }

    /**
     * Plus node.
     * It contains the two expressions.
     */
    public static class PlusNode extends Node {
        final Node left;
        final Node right;

        PlusNode(Node l, Node r) {
            left = l;
            right = r;
        }

        @Override
        public <S, E extends Exception> S accept(BaseASTVisitor<S, E> visitor) throws E {
            return visitor.visitNode(this);
        }
    }

    /**
     * Minus node.
     * It contains the two expressions.
     */
    public static class MinusNode extends Node {
        final Node left;
        final Node right;

        MinusNode(Node l, Node r) {
            left = l;
            right = r;
        }

        @Override
        public <S, E extends Exception> S accept(BaseASTVisitor<S, E> visitor) throws E {
            return visitor.visitNode(this);
        }
    }

    /**
     *
     */
    public static class CallNode extends Node {
        final String id;
        final List<Node> arglist;
        STentry entry;
        int nl;

        CallNode(String i, List<Node> p) {
            id = i;
            arglist = Collections.unmodifiableList(p);
        }

        @Override
        public <S, E extends Exception> S accept(BaseASTVisitor<S, E> visitor) throws E {
            return visitor.visitNode(this);
        }
    }

    public static class IdNode extends Node {
        final String id;
        STentry entry;
        int nl;

        IdNode(String i) {
            id = i;
        }

        @Override
        public <S, E extends Exception> S accept(BaseASTVisitor<S, E> visitor) throws E {
            return visitor.visitNode(this);
        }
    }

    public static class BoolNode extends Node {
        final Boolean val;

        BoolNode(boolean n) {
            val = n;
        }

        @Override
        public <S, E extends Exception> S accept(BaseASTVisitor<S, E> visitor) throws E {
            return visitor.visitNode(this);
        }
    }

    public static class IntNode extends Node {
        final Integer val;

        IntNode(Integer n) {
            val = n;
        }

        @Override
        public <S, E extends Exception> S accept(BaseASTVisitor<S, E> visitor) throws E {
            return visitor.visitNode(this);
        }
    }

    public static class ArrowTypeNode extends TypeNode {
        final List<TypeNode> parlist;
        final TypeNode ret;

        ArrowTypeNode(List<TypeNode> p, TypeNode r) {
            parlist = Collections.unmodifiableList(p);
            ret = r;
        }

        @Override
        public <S, E extends Exception> S accept(BaseASTVisitor<S, E> visitor) throws E {
            return visitor.visitNode(this);
        }
    }

    public static class BoolTypeNode extends TypeNode {

        @Override
        public <S, E extends Exception> S accept(BaseASTVisitor<S, E> visitor) throws E {
            return visitor.visitNode(this);
        }
    }

    public static class IntTypeNode extends TypeNode {

        @Override
        public <S, E extends Exception> S accept(BaseASTVisitor<S, E> visitor) throws E {
            return visitor.visitNode(this);
        }
    }

}