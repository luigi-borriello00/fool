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
        final List<DecNode> declarations;
        final Node exp;

        ProgLetInNode(List<DecNode> d, Node e) {
            declarations = Collections.unmodifiableList(d);
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
        final TypeNode returnType;
        final List<ParNode> parameters;
        final List<DecNode> declarations;
        final Node exp;

        FunNode(String id, TypeNode retType, List<ParNode> parList, List<DecNode> decList, Node exp) {
            this.id = id;
            this.returnType = retType;
            this.parameters = Collections.unmodifiableList(parList);
            this.declarations = Collections.unmodifiableList(decList);
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
     * Identifier node.
     * It contains the identifier name, the symbol table entry and the nesting level.
     */
    public static class IdNode extends Node {
        final String id;
        STentry entry;
        int nestingLevel;

        IdNode(String i) {
            id = i;
        }

        @Override
        public <S, E extends Exception> S accept(BaseASTVisitor<S, E> visitor) throws E {
            return visitor.visitNode(this);
        }
    }

    /**
     * Boolean node.
     * It contains the boolean value.
     */
    public static class BoolNode extends Node {
        final Boolean value;

        BoolNode(boolean n) {
            value = n;
        }

        @Override
        public <S, E extends Exception> S accept(BaseASTVisitor<S, E> visitor) throws E {
            return visitor.visitNode(this);
        }
    }

    /**
     * Integer node.
     * It contains the integer value.
     */
    public static class IntNode extends Node {
        final Integer value;

        IntNode(Integer n) {
            value = n;
        }

        @Override
        public <S, E extends Exception> S accept(BaseASTVisitor<S, E> visitor) throws E {
            return visitor.visitNode(this);
        }
    }

    /* Type nodes */

    /**
     * Arrow type node.
     * It contains the list of parameter types and the return type.
     */
    public static class ArrowTypeNode extends TypeNode {
        final List<TypeNode> parameters;
        final TypeNode returnType;

        ArrowTypeNode(List<TypeNode> parTypeList, TypeNode retType) {
            this.parameters = Collections.unmodifiableList(parTypeList);
            this.returnType = retType;
        }

        @Override
        public <S, E extends Exception> S accept(BaseASTVisitor<S, E> visitor) throws E {
            return visitor.visitNode(this);
        }
    }

    /**
     * Boolean type node.
     */
    public static class BoolTypeNode extends TypeNode {

        @Override
        public <S, E extends Exception> S accept(BaseASTVisitor<S, E> visitor) throws E {
            return visitor.visitNode(this);
        }
    }

    /**
     * Integer type node.
     */
    public static class IntTypeNode extends TypeNode {

        @Override
        public <S, E extends Exception> S accept(BaseASTVisitor<S, E> visitor) throws E {
            return visitor.visitNode(this);
        }
    }

    /* Operation nodes */

    /**
     * Function call node.
     * It contains the function name, the list of arguments, the symbol table entry and the nesting level.
     */
    public static class CallNode extends Node {
        final String id;
        final List<Node> arguments;
        STentry entry;
        int nestingLevel;

        CallNode(String id, List<Node> arguments) {
            this.id = id;
            this.arguments = Collections.unmodifiableList(arguments);
        }

        @Override
        public <S, E extends Exception> S accept(BaseASTVisitor<S, E> visitor) throws E {
            return visitor.visitNode(this);
        }
    }

    /**
     * Print node.
     * It contains the expression to print.
     */
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

    // OPERATORS EXTENSION

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


    // Object oriented extension

    /**
     * This is a Field declaration node.
     * It contains the field name and the offset.
     */
    public static class FieldNode extends DecNode {
        final String id;
        int offset;

        FieldNode(String id, TypeNode type) {
            this.id = id;
            this.type = type;
        }

        @Override
        public <S, E extends Exception> S accept(BaseASTVisitor<S, E> visitor) throws E {
            return visitor.visitNode(this);
        }
    }

    /**
     * This is a Method declaration node.
     * It contains the method name, the return type, the list of parameters, the list of local declarations and the body expression.
     * It also contains the offset and the label.
     */
    public static class MethodNode extends DecNode {
        final String id;
        final TypeNode returnType;
        final List<ParNode> parameters;
        final List<DecNode> declarations;
        final Node exp;
        int offset = 0;

        String label;

        MethodNode(String id, TypeNode returnType, List<ParNode> parList, List<DecNode> decList, Node exp) {
            this.id = id;
            this.returnType = returnType;
            this.parameters = Collections.unmodifiableList(parList);
            this.declarations = Collections.unmodifiableList(decList);
            this.exp = exp;
        }

        @Override
        public <S, E extends Exception> S accept(BaseASTVisitor<S, E> visitor) throws E {
            return visitor.visitNode(this);
        }
    }

    /**
     * The node for the declaration of a class.
     * It contains the id of the class, the id of the super class,
     * the list of fields and the list of methods.
     * It also contains the type of the class and the entry of the super class.
     */
    public static class ClassNode extends DecNode {
        final String classId;
        final Optional<String> superClassId;
        final List<FieldNode> allFields;
        final List<MethodNode> allMethods;
        STentry superEntry;

        public ClassNode(final String classId, final Optional<String> superId, final List<FieldNode> allFields, final List<MethodNode> allMethods) {
            this.classId = classId;
            this.superClassId = superId;
            this.allFields = Collections.unmodifiableList(allFields);
            this.allMethods = Collections.unmodifiableList(allMethods);
        }

        void setType(final ClassTypeNode type) {
            this.type = type;
        }

        @Override
        public <S, E extends Exception> S accept(BaseASTVisitor<S, E> visitor) throws E {
            return visitor.visitNode(this);
        }
    }

    /**
     * The node for the null value.
     */
    public static class EmptyNode extends Node {
        @Override
        public <S, E extends Exception> S accept(BaseASTVisitor<S, E> visitor) throws E {
            return visitor.visitNode(this);
        }

    }

    /**
     * The node for the method call.
     * It contains the id of the object, the id of the method,
     * the list of arguments and the entry in the symbol table.
     * It also contains the entry of the method in the symbol table.
     * It also contains the nesting level.
     */
    public static class ClassCallNode extends Node {
        final String objectId;
        final String methodId;
        final List<Node> args;
        int nestingLevel = 0;
        STentry entry;
        STentry methodEntry;

        public ClassCallNode(final String objectId, final String methodId, final List<Node> args) {
            this.objectId = objectId;
            this.methodId = methodId;
            this.args = Collections.unmodifiableList(args);
        }

        @Override
        public <S, E extends Exception> S accept(BaseASTVisitor<S, E> visitor) throws E {
            return visitor.visitNode(this);
        }
    }

    /**
     * The node for the new expression.
     * It contains the id of the class and the list of arguments.
     * It also contains the entry in the symbol table.
     */
    public static class NewNode extends Node {
        final String classId;
        final List<Node> arguments;
        STentry entry;

        public NewNode(final String classId, final List<Node> args) {
            this.classId = classId;
            this.arguments = Collections.unmodifiableList(args);
        }

        @Override
        public <S, E extends Exception> S accept(BaseASTVisitor<S, E> visitor) throws E {
            return visitor.visitNode(this);
        }
    }

    /**
     * This is the node for the class type.
     * It contains the list of fields and the list of methods.
     */
    public static class ClassTypeNode extends TypeNode {
        final List<TypeNode> allFields;
        final List<ArrowTypeNode> allMethods;

        public ClassTypeNode(final List<TypeNode> allFields, final List<ArrowTypeNode> allMethods) {
            this.allFields = new ArrayList<>(allFields);
            this.allMethods = new ArrayList<>(allMethods);
        }

        public ClassTypeNode(final ClassTypeNode parent) {
            this(parent.allFields, parent.allMethods);
        }

        public ClassTypeNode() {
            this(new ArrayList<>(), new ArrayList<>());
        }

        @Override
        public <S, E extends Exception> S accept(BaseASTVisitor<S, E> visitor) throws E {
            return visitor.visitNode(this);
        }

    }

    /**
     * This is the node for the method type.
     * It contains the arrow type of the method.
     */
    public static class MethodTypeNode extends TypeNode {
        final ArrowTypeNode arrowTypeNode;

        public MethodTypeNode(final ArrowTypeNode functionalType) {
            this.arrowTypeNode = functionalType;
        }

        @Override
        public <S, E extends Exception> S accept(BaseASTVisitor<S, E> visitor) throws E {
            return visitor.visitNode(this);
        }
    }

    /**
     * The node for object reference type.
     * It contains the id of the class.
     */
    public static class RefTypeNode extends TypeNode {

        final String refClassId;

        public RefTypeNode(final String classId) {
            this.refClassId = classId;
        }


        @Override
        public <S, E extends Exception> S accept(BaseASTVisitor<S, E> visitor) throws E {
            return visitor.visitNode(this);
        }
    }

    /**
     * This is the node for the empty type.
     */
    public static class EmptyTypeNode extends TypeNode {

        @Override
        public <S, E extends Exception> S accept(BaseASTVisitor<S, E> visitor) throws E {
            return visitor.visitNode(this);
        }
    }
}