package compiler;

import compiler.AST.*;
import compiler.lib.*;
import compiler.exc.*;

public class PrintEASTVisitor extends BaseEASTVisitor<Void, VoidException> {

    PrintEASTVisitor() {
        super(false, true);
    }

    @Override
    public Void visitNode(ProgLetInNode node) {
        printNode(node);
        for (Node dec : node.declarations) visit(dec);
        visit(node.exp);
        return null;
    }

    @Override
    public Void visitNode(ProgNode node) {
        printNode(node);
        visit(node.exp);
        return null;
    }

    @Override
    public Void visitNode(FunNode node) {
        printNode(node, node.id);
        visit(node.retType);
        for (ParNode par : node.parameters) visit(par);
        for (Node dec : node.declarations) visit(dec);
        visit(node.exp);
        return null;
    }

    @Override
    public Void visitNode(ParNode node) {
        printNode(node, node.id);
        visit(node.getType());
        return null;
    }

    @Override
    public Void visitNode(VarNode node) {
        printNode(node, node.id);
        visit(node.getType());
        visit(node.exp);
        return null;
    }

    @Override
    public Void visitNode(PrintNode node) {
        printNode(node);
        visit(node.exp);
        return null;
    }

    @Override
    public Void visitNode(IfNode node) {
        printNode(node);
        visit(node.condition);
        visit(node.thenBranch);
        visit(node.elseBranch);
        return null;
    }

    @Override
    public Void visitNode(EqualNode node) {
        printNode(node);
        visit(node.left);
        visit(node.right);
        return null;
    }


    @Override
    public Void visitNode(TimesNode node) {
        printNode(node);
        visit(node.left);
        visit(node.right);
        return null;
    }

    @Override
    public Void visitNode(PlusNode node) {
        printNode(node);
        visit(node.left);
        visit(node.right);
        return null;
    }

    @Override
    public Void visitNode(CallNode node) {
        printNode(node, node.id + " at nestinglevel " + node.nestingLevel);
        visit(node.entry);
        for (Node arg : node.arguments) visit(arg);
        return null;
    }

    @Override
    public Void visitNode(IdNode node) {
        printNode(node, node.id + " at nestinglevel " + node.nl);
        visit(node.entry);
        return null;
    }

    @Override
    public Void visitNode(BoolNode node) {
        printNode(node, node.val.toString());
        return null;
    }

    @Override
    public Void visitNode(IntNode node) {
        printNode(node, node.val.toString());
        return null;
    }

    @Override
    public Void visitNode(ArrowTypeNode node) {
        printNode(node);
        for (Node par : node.parameters) visit(par);
        visit(node.returnType, "->"); //marks return type
        return null;
    }

    @Override
    public Void visitNode(BoolTypeNode node) {
        printNode(node);
        return null;
    }

    @Override
    public Void visitNode(IntTypeNode node) {
        printNode(node);
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

    // OPERATOR EXTENSION

    @Override
    public Void visitNode(GreaterEqualNode node) {
        printNode(node);
        visit(node.left);
        visit(node.right);
        return null;
    }

    @Override
    public Void visitNode(OrNode node) {
        printNode(node);
        visit(node.left);
        visit(node.right);
        return null;
    }

    @Override
    public Void visitNode(NotNode node) {
        printNode(node);
        visit(node.exp);
        return null;
    }

    @Override
    public Void visitNode(LessEqualNode node) {
        printNode(node);
        visit(node.left);
        visit(node.right);
        return null;
    }

    @Override
    public Void visitNode(MinusNode node) {
        printNode(node);
        visit(node.left);
        visit(node.right);
        return null;
    }

    @Override
    public Void visitNode(DivNode node) {
        printNode(node);
        visit(node.left);
        visit(node.right);
        return null;
    }

    @Override
    public Void visitNode(AndNode node) {
        printNode(node);
        visit(node.left);
        visit(node.right);
        return null;
    }

    @Override
    public Void visitNode(EmptyNode node) {
        printNode(node);
        return null;
    }

    @Override
    public Void visitNode(ClassNode node) {
        printNode(node, node.classId);
        for (Node dec : node.fields) visit(dec);
        for (Node dec : node.methods) visit(dec);
        return null;
    }

    @Override
    public Void visitNode(FieldNode node) {
        printNode(node, node.id);
        visit(node.getType());
        return null;
    }

    @Override
    public Void visitNode(MethodNode node) {
        printNode(node, node.id);
        visit(node.retType);
        for (ParNode par : node.parameters) visit(par);
        for (Node dec : node.declarations) visit(dec);
        visit(node.exp);
        return null;
    }

    @Override
    public Void visitNode(ClassCallNode node) {
        printNode(node, node.objectId + " at nestinglevel " + node.nestingLevel);
        visit(node.entry);
        for (Node arg : node.args) visit(arg);
        return null;
    }

    @Override
    public Void visitNode(NewNode node) {
        printNode(node, node.classId);
        visit(node.entry);
        return null;
    }

    @Override
    public Void visitNode(ClassTypeNode node) {
        for (Node par : node.fields) visit(par);
        for (Node par : node.methods) visit(par);
        return null;
    }

    @Override
    public Void visitNode(MethodTypeNode node) {
        for (TypeNode par : node.arrowTypeNode.parameters) visit(par);
        visit(node.arrowTypeNode.returnType, "->");
        return null;
    }

    @Override
    public Void visitNode(RefTypeNode node) {
        printNode(node, node.refClassId);
        return null;
    }

    @Override
    public Void visitNode(EmptyTypeNode node) {
        printNode(node);
        return null;
    }


}
