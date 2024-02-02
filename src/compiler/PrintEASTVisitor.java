package compiler;

import compiler.AST.*;
import compiler.exc.VoidException;
import compiler.lib.BaseEASTVisitor;

/**
 * This class implements a visitor that prints the E-AST.
 * It is used for debugging purposes.
 */
public class PrintEASTVisitor extends BaseEASTVisitor<Void, VoidException> {

    PrintEASTVisitor() {
        super(false, true);
    }

    @Override
    public Void visitNode(final ProgLetInNode node) {
        printNode(node);
        node.declarations.forEach(this::visit);
        visit(node.exp);
        return null;
    }

    @Override
    public Void visitNode(final ProgNode node) {
        printNode(node);
        visit(node.exp);
        return null;
    }

    @Override
    public Void visitNode(final FunNode node) {
        printNode(node, node.id);
        visit(node.returnType);
        node.parameters.forEach(this::visit);
        node.declarations.forEach(this::visit);
        visit(node.exp);
        return null;
    }

    @Override
    public Void visitNode(final ParNode node) {
        printNode(node, node.id);
        visit(node.getType());
        return null;
    }

    @Override
    public Void visitNode(final VarNode node) {
        printNode(node, node.id);
        visit(node.getType());
        visit(node.exp);
        return null;
    }

    @Override
    public Void visitNode(final PrintNode node) {
        printNode(node);
        visit(node.exp);
        return null;
    }

    @Override
    public Void visitNode(final IfNode node) {
        printNode(node);
        visit(node.condition);
        visit(node.thenBranch);
        visit(node.elseBranch);
        return null;
    }

    @Override
    public Void visitNode(final EqualNode node) {
        printNode(node);
        visit(node.left);
        visit(node.right);
        return null;
    }

    @Override
    public Void visitNode(final GreaterEqualNode node) throws VoidException {
        printNode(node);
        visit(node.left);
        visit(node.right);
        return null;
    }

    @Override
    public Void visitNode(final LessEqualNode node) throws VoidException {
        printNode(node);
        visit(node.left);
        visit(node.right);
        return null;
    }

    @Override
    public Void visitNode(final NotNode node) throws VoidException {
        printNode(node);
        visit(node.exp);
        return null;
    }

    @Override
    public Void visitNode(final OrNode node) throws VoidException {
        printNode(node);
        visit(node.left);
        visit(node.right);
        return null;
    }

    @Override
    public Void visitNode(final AndNode node) throws VoidException {
        printNode(node);
        visit(node.left);
        visit(node.right);
        return null;
    }

    @Override
    public Void visitNode(final TimesNode node) {
        printNode(node);
        visit(node.left);
        visit(node.right);
        return null;
    }

    @Override
    public Void visitNode(final DivNode node) {
        printNode(node);
        visit(node.left);
        visit(node.right);
        return null;
    }

    @Override
    public Void visitNode(final PlusNode node) {
        printNode(node);
        visit(node.left);
        visit(node.right);
        return null;
    }

    @Override
    public Void visitNode(final MinusNode node) {
        printNode(node);
        visit(node.left);
        visit(node.right);
        return null;
    }

    @Override
    public Void visitNode(final CallNode node) {
        printNode(node, node.id + " with nesting level: " + node.nestingLevel);
        visit(node.entry);
        node.arguments.forEach(this::visit);
        return null;
    }

    @Override
    public Void visitNode(final IdNode node) {
        printNode(node, node.id + " with nesting level: " + node.nestingLevel);
        visit(node.entry);
        return null;
    }

    @Override
    public Void visitNode(final BoolNode node) {
        printNode(node, String.valueOf(node.value));
        return null;
    }

    @Override
    public Void visitNode(final IntNode node) {
        printNode(node, node.value.toString());
        return null;
    }

    @Override
    public Void visitNode(final ArrowTypeNode node) {
        printNode(node);
        node.parameters.forEach(this::visit);
        visit(node.returnType, "->");
        return null;
    }

    @Override
    public Void visitNode(final BoolTypeNode node) {
        printNode(node);
        return null;
    }

    @Override
    public Void visitNode(final IntTypeNode node) {
        printNode(node);
        return null;
    }

    @Override
    public Void visitSTentry(final STentry entry) {
        printSTentry("nestlev " + entry.nl);
        printSTentry("type");
        visit(entry.type);
        printSTentry("offset " + entry.offset);
        return null;
    }


    // OBJECT-ORIENTED EXTENSION


    @Override
    public Void visitNode(final ClassNode node) throws VoidException {
        printNode(node, node.classId + (node.superClassId.isPresent() ? " extends: " + node.superClassId : ""));
        node.fields.forEach(this::visit);
        node.methods.forEach(this::visit);
        return null;
    }

    @Override
    public Void visitNode(final FieldNode node) throws VoidException {
        printNode(node, node.id);
        visit(node.getType());
        return null;
    }

    @Override
    public Void visitNode(final MethodNode node) throws VoidException {
        printNode(node, node.id);
        visit(node.returnType);
        node.parameters.forEach(this::visit);
        node.declarations.forEach(this::visit);
        visit(node.exp);
        return null;
    }

    @Override
    public Void visitNode(final ClassCallNode node) throws VoidException {
        printNode(node, node.objectId + "." + node.methodId + " with nesting level: " + node.nestingLevel);
        visit(node.entry);
        visit(node.methodEntry);
        node.args.forEach(this::visit);
        return null;
    }

    @Override
    public Void visitNode(final NewNode node) throws VoidException {
        printNode(node, node.classId + " with nesting level: " + node.entry.nl);
        visit(node.entry);
        node.arguments.forEach(this::visit);
        return null;
    }

    @Override
    public Void visitNode(final EmptyNode node) throws VoidException {
        printNode(node);
        return null;
    }

    @Override
    public Void visitNode(final ClassTypeNode node) throws VoidException {
        printNode(node);
        node.fields.forEach(this::visit);
        node.methods.forEach(this::visit);
        return null;
    }

    @Override
    public Void visitNode(final MethodTypeNode node) throws VoidException {
        printNode(node);
        node.arrowTypeNode.parameters.forEach(this::visit);
        visit(node.arrowTypeNode.returnType, "->"); //marks return type
        return null;
    }

    @Override
    public Void visitNode(final RefTypeNode node) throws VoidException {
        printNode(node, node.refClassId);
        return null;
    }
}