package compiler;

import java.util.*;

import compiler.AST.*;
import compiler.exc.*;
import compiler.lib.*;

public class SymbolTableASTVisitor extends BaseASTVisitor<Void, VoidException> {

    private List<Map<String, STentry>> symTable = new ArrayList<>();
    private int nestingLevel = 0; // current nesting level
    private int decOffset = -2; // counter for offset of local declarations at current nesting level
    int stErrors = 0;

    SymbolTableASTVisitor() {
    }

    SymbolTableASTVisitor(boolean debug) {
        super(debug);
    } // enables print for debugging

    private STentry stLookup(String id) {
        int j = nestingLevel;
        STentry entry = null;
        while (j >= 0 && entry == null)
            entry = symTable.get(j--).get(id);
        return entry;
    }

    @Override
    public Void visitNode(ProgLetInNode node) {
        if (print) printNode(node);
        Map<String, STentry> hm = new HashMap<>();
        symTable.add(hm);
        for (Node dec : node.declarations) visit(dec);
        visit(node.exp);
        symTable.remove(0);
        return null;
    }

    @Override
    public Void visitNode(ProgNode node) {
        if (print) printNode(node);
        visit(node.exp);
        return null;
    }

    @Override
    public Void visitNode(FunNode node) {
        if (print) printNode(node);
        Map<String, STentry> hm = symTable.get(nestingLevel);
        List<TypeNode> parTypes = new ArrayList<>();
        for (ParNode par : node.parameters) parTypes.add(par.getType());
        STentry entry = new STentry(nestingLevel, new ArrowTypeNode(parTypes, node.retType), decOffset--);
        //inserimento di ID nella symtable
        if (hm.put(node.id, entry) != null) {
            System.out.println("Fun id " + node.id + " at line " + node.getLine() + " already declared");
            stErrors++;
        }
        //creare una nuova hashmap per la symTable
        nestingLevel++;
        Map<String, STentry> hmn = new HashMap<>();
        symTable.add(hmn);
        int prevNLDecOffset = decOffset; // stores counter for offset of declarations at previous nesting level
        decOffset = -2;

        int parOffset = 1;
        for (ParNode par : node.parameters)
            if (hmn.put(par.id, new STentry(nestingLevel, par.getType(), parOffset++)) != null) {
                System.out.println("Par id " + par.id + " at line " + node.getLine() + " already declared");
                stErrors++;
            }
        for (Node dec : node.declarations) visit(dec);
        visit(node.exp);
        //rimuovere la hashmap corrente poiche' esco dallo scope
        symTable.remove(nestingLevel--);
        decOffset = prevNLDecOffset; // restores counter for offset of declarations at previous nesting level
        return null;
    }

    @Override
    public Void visitNode(VarNode node) {
        if (print) printNode(node);
        visit(node.exp);
        Map<String, STentry> hm = symTable.get(nestingLevel);
        STentry entry = new STentry(nestingLevel, node.getType(), decOffset--);
        //inserimento di ID nella symtable
        if (hm.put(node.id, entry) != null) {
            System.out.println("Var id " + node.id + " at line " + node.getLine() + " already declared");
            stErrors++;
        }
        return null;
    }

    @Override
    public Void visitNode(PrintNode node) {
        if (print) printNode(node);
        visit(node.exp);
        return null;
    }

    @Override
    public Void visitNode(IfNode node) {
        if (print) printNode(node);
        visit(node.condition);
        visit(node.thenBranch);
        visit(node.elseBranch);
        return null;
    }

    @Override
    public Void visitNode(EqualNode node) {
        if (print) printNode(node);
        visit(node.left);
        visit(node.right);
        return null;
    }

    @Override
    public Void visitNode(TimesNode node) {
        if (print) printNode(node);
        visit(node.left);
        visit(node.right);
        return null;
    }

    @Override
    public Void visitNode(PlusNode node) {
        if (print) printNode(node);
        visit(node.left);
        visit(node.right);
        return null;
    }

    @Override
    public Void visitNode(CallNode node) {
        if (print) printNode(node);
        STentry entry = stLookup(node.id);
        if (entry == null) {
            System.out.println("Fun id " + node.id + " at line " + node.getLine() + " not declared");
            stErrors++;
        } else {
            node.entry = entry;
            node.nestingLevel = nestingLevel;
        }
        for (Node arg : node.arguments) visit(arg);
        return null;
    }

    @Override
    public Void visitNode(IdNode node) {
        if (print) printNode(node);
        STentry entry = stLookup(node.id);
        if (entry == null) {
            System.out.println("Var or Par id " + node.id + " at line " + node.getLine() + " not declared");
            stErrors++;
        } else {
            node.entry = entry;
            node.nl = nestingLevel;
        }
        return null;
    }

    @Override
    public Void visitNode(BoolNode node) {
        if (print) printNode(node, node.val.toString());
        return null;
    }

    @Override
    public Void visitNode(IntNode node) {
        if (print) printNode(node, node.val.toString());
        return null;
    }

    // OPERATOR EXTENSION
    @Override
    public Void visitNode(GreaterEqualNode node) {
        if (print) printNode(node);
        visit(node.left);
        visit(node.right);
        return null;
    }

    @Override
    public Void visitNode(LessEqualNode node) {
        if (print) printNode(node);
        visit(node.left);
        visit(node.right);
        return null;
    }

    @Override
    public Void visitNode(NotNode node) {
        if (print) printNode(node);
        visit(node.exp);
        return null;
    }

    @Override
    public Void visitNode(MinusNode node) {
        if (print) printNode(node);
        visit(node.left);
        visit(node.right);
        return null;
    }

    @Override
    public Void visitNode(OrNode node) {
        if (print) printNode(node);
        visit(node.left);
        visit(node.right);
        return null;
    }

    @Override
    public Void visitNode(AndNode node) {
        if (print) printNode(node);
        visit(node.left);
        visit(node.right);
        return null;
    }

    @Override
    public Void visitNode(DivNode node) {
        if (print) printNode(node);
        visit(node.left);
        visit(node.right);
        return null;
    }


}
