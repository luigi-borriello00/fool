package compiler;

import compiler.AST.*;
import compiler.lib.*;
import compiler.exc.*;

public class PrintEASTVisitor extends BaseEASTVisitor<Void, VoidException> {

    PrintEASTVisitor() {
        super(false, true);
    }

    @Override
    public Void visitNode(ProgLetInNode n) {
        printNode(n);
        for (Node dec : n.declist) visit(dec);
        visit(n.exp);
        return null;
    }

    @Override
    public Void visitNode(ProgNode n) {
        printNode(n);
        visit(n.exp);
        return null;
    }

    @Override
    public Void visitNode(FunNode n) {
        printNode(n, n.id);
        visit(n.retType);
        for (ParNode par : n.parlist) visit(par);
        for (Node dec : n.declist) visit(dec);
        visit(n.exp);
        return null;
    }

    @Override
    public Void visitNode(ParNode n) {
        printNode(n, n.id);
        visit(n.getType());
        return null;
    }

    @Override
    public Void visitNode(VarNode n) {
        printNode(n, n.id);
        visit(n.getType());
        visit(n.exp);
        return null;
    }

    @Override
    public Void visitNode(PrintNode n) {
        printNode(n);
        visit(n.exp);
        return null;
    }

    @Override
    public Void visitNode(IfNode n) {
        printNode(n);
        visit(n.cond);
        visit(n.th);
        visit(n.el);
        return null;
    }

    @Override
    public Void visitNode(EqualNode n) {
        printNode(n);
        visit(n.left);
        visit(n.right);
        return null;
    }

    @Override
    public Void visitNode(TimesNode n) {
        printNode(n);
        visit(n.left);
        visit(n.right);
        return null;
    }

    @Override
    public Void visitNode(PlusNode n) {
        printNode(n);
        visit(n.left);
        visit(n.right);
        return null;
    }

    @Override
    public Void visitNode(CallNode n) {
        printNode(n, n.id + " at nestinglevel " + n.nl);
        visit(n.entry);
        for (Node arg : n.arglist) visit(arg);
        return null;
    }

    @Override
    public Void visitNode(IdNode n) {
        printNode(n, n.id + " at nestinglevel " + n.nl);
        visit(n.entry);
        return null;
    }

    @Override
    public Void visitNode(BoolNode n) {
        printNode(n, n.val.toString());
        return null;
    }

    @Override
    public Void visitNode(IntNode n) {
        printNode(n, n.val.toString());
        return null;
    }

    @Override
    public Void visitNode(ArrowTypeNode n) {
        printNode(n);
        for (Node par : n.parlist) visit(par);
        visit(n.ret, "->"); //marks return type
        return null;
    }

    @Override
    public Void visitNode(BoolTypeNode n) {
        printNode(n);
        return null;
    }

    @Override
    public Void visitNode(IntTypeNode n) {
        printNode(n);
        return null;
    }

    @Override
    public Void visitNode(GreaterEqualNode n) throws VoidException {
        printNode(n);
        visit(n.left);
        visit(n.right);
        return null;
    }

    @Override
    public Void visitNode(LessEqualNode n) throws VoidException {
        printNode(n);
        visit(n.left);
        visit(n.right);
        return null;
    }

    @Override
    public Void visitNode(NotNode n) throws VoidException {
        printNode(n);
        visit(n.node);
        return null;
    }

    @Override
    public Void visitNode(MinusNode n) throws VoidException {
        printNode(n);
        visit(n.left);
        visit(n.right);
        return null;
    }

    @Override
    public Void visitNode(OrNode n) throws VoidException {
        printNode(n);
        visit(n.left);
        visit(n.right);
        return null;
    }

    @Override
    public Void visitNode(DivNode n) throws VoidException {
        printNode(n);
        visit(n.left);
        visit(n.right);
        return null;
    }

    @Override
    public Void visitNode(AndNode n) throws VoidException {
        printNode(n);
        visit(n.left);
        visit(n.right);
        return null;
    }

    @Override
    public Void visitSTentry(STentry entry) {
        printSTentry("nestlev " + entry.nl);
        printSTentry("type");
        visit(entry.type);
        printSTentry("offset " + entry.offset);
        return null;
    }

    @Override
    public Void visitNode(ClassNode n) throws VoidException {
        printNode(n, n.id);
        for (FieldNode f : n.fields) visit(f);
        for (MethodNode m : n.methods) visit(m);
        return null;
    }

    @Override
    public Void visitNode(FieldNode node) throws VoidException {
        printNode(node, node.id);
        visit(node.getType());
        return null;
    }

    @Override
    public Void visitNode(MethodNode n) throws VoidException {
        printNode(n, n.id);
        visit(n.retType);
        for (ParNode par : n.parlist) visit(par);
        for (DecNode dec : n.declist) visit(dec);
        visit(n.exp);
        return null;
    }

    @Override
    public Void visitNode(ClassCallNode node) throws VoidException {
        printNode(node, node.id + "." + node.methodId + " at nestinglevel " + node.nl);
        visit(node.entry);
        visit(node.methodEntry);
        for (Node arg : node.arglist) visit(arg);
        return null;
    }

    @Override
    public Void visitNode(NewNode n) throws VoidException {
        printNode(n, n.id + " at nestinglevel " + n.entry.nl);
        visit(n.entry);
        for (Node arg : n.arglist) visit(arg);
        return null;
    }

    @Override
    public Void visitNode(EmptyNode n) throws VoidException {
        printNode(n);
        return null;
    }

    @Override
    public Void visitNode(ClassTypeNode n) throws VoidException {
        printNode(n);
        for (TypeNode f : n.fields) visit(f);
        for (ArrowTypeNode m : n.methods) visit(m);
        return null;
    }

    @Override
    public Void visitNode(MethodTypeNode n) throws VoidException {
        printNode(n);
        for (TypeNode p : n.fun.parlist) visit(p);
        visit(n.fun.ret, "->"); //marks return type
        return null;
    }

    @Override
    public Void visitNode(RefTypeNode n) throws VoidException {
        printNode(n, n.id);
        return null;
    }

}
