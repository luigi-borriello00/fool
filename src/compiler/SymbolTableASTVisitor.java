package compiler;

import java.util.*;

import compiler.AST.*;
import compiler.exc.*;
import compiler.lib.*;

public class SymbolTableASTVisitor extends BaseASTVisitor<Void, VoidException> {

    private List<Map<String, STentry>> symbolTable = new ArrayList<>();
    private int nestingLevel = 0; // current nesting level
    private int decOffset = -2; // counter for offset of local declarations at current nesting level
    int stErrors = 0;

    SymbolTableASTVisitor() {
    }

    SymbolTableASTVisitor(boolean debug) {
        super(debug);
    } // enables print for debugging

    /**
     * Do a lookup in the symbol table for the given id.
     * The lookup starts from the current nesting level and goes up
     * to the global level.
     * If the id is found, the method returns the STentry object
     * associated with the id.
     * Otherwise, it returns null.
     *
     * @param id the id to search for
     * @return the STentry object associated with the id, or null if the id is not found
     */
    private STentry stLookup(String id) {
        int j = nestingLevel;
        STentry entry = null;
        while (j >= 0 && entry == null)
            entry = symbolTable.get(j--).get(id);
        return entry;
    }

    /**
     * Visit a ProgLetInNode.
     * A new scope is created and then the declarations are visited.
     * Then the expression is visited.
     * The scope is then removed.
     *
     * @param node the ProgLetInNode to visit
     * @return null
     */
    @Override
    public Void visitNode(ProgLetInNode node) {
        if (print) printNode(node);
        this.symbolTable.add(new HashMap<>());
        node.declarations.forEach(this::visit);
        visit(node.exp);
        symbolTable.remove(0);
        return null;
    }

    /**
     * Visit a ProgNode.
     * The expression is visited.
     *
     * @param node the ProgNode to visit
     * @return null
     */
    @Override
    public Void visitNode(ProgNode node) {
        if (print) printNode(node);
        visit(node.exp);
        return null;
    }

    /**
     * Visit a FunNode.
     * Create the STentry for the function and add it to the current symbol table.
     * Create a new scope for the function parameters and add them to the new symbol table.
     * Then visit each function declarations and body and remove the scope.
     *
     * @param node the FunNode to visit
     * @return null
     */
    @Override
    public Void visitNode(FunNode node) {
        if (print) printNode(node);
        Map<String, STentry> currentSymbolTable = symbolTable.get(nestingLevel);
        List<TypeNode> parametersType = new ArrayList<>();
        node.parameters.forEach(par -> parametersType.add(par.getType()));
        final ArrowTypeNode arrowTypeNode = new ArrowTypeNode(parametersType, node.retType);
        node.setType(arrowTypeNode);
        STentry entry = new STentry(nestingLevel, arrowTypeNode, decOffset--);
        //inserimento di ID nella symtable
        if (currentSymbolTable.put(node.id, entry) != null) {
            System.out.println("Fun id " + node.id + " at line " + node.getLine() + " already declared");
            stErrors++;
        }
        //creare una nuova hashmap per la symTable
        nestingLevel++;
        Map<String, STentry> newSymbolTable = new HashMap<>();
        symbolTable.add(newSymbolTable);
        int prevNLDecOffset = decOffset; // stores counter for offset of declarations at previous nesting level
        decOffset = -2;

        int parOffset = 1;
        for (ParNode par : node.parameters) {
            final STentry parEntry = new STentry(nestingLevel, par.getType(), parOffset++);
            if (newSymbolTable.put(par.id, parEntry) != null) {
                System.out.println("Par id " + par.id + " at line " + node.getLine() + " already declared");
                stErrors++;
            }
        }
        node.declarations.forEach(this::visit);
        visit(node.exp);
        //rimuovere la hashmap corrente poiche' esco dallo scope
        symbolTable.remove(nestingLevel--);
        decOffset = prevNLDecOffset; // restores counter for offset of declarations at previous nesting level
        return null;
    }

    /**
     * Visit a VarNode.
     * Create the STentry for the variable and add it to the current symbol table.
     *
     * @param node the VarNode to visit
     * @return null
     */
    @Override
    public Void visitNode(VarNode node) {
        if (print) printNode(node);
        visit(node.exp);
        Map<String, STentry> currentSymbolTable = symbolTable.get(nestingLevel);
        STentry entry = new STentry(nestingLevel, node.getType(), decOffset--);
        //inserimento di ID nella symtable
        if (currentSymbolTable.put(node.id, entry) != null) {
            System.out.println("Var id " + node.id + " at line " + node.getLine() + " already declared");
            stErrors++;
        }
        return null;
    }

    /**
     * Visit a PrintNode.
     * Visit the expression.
     *
     * @param node the PrintNode to visit
     * @return null
     */
    @Override
    public Void visitNode(PrintNode node) {
        if (print) printNode(node);
        visit(node.exp);
        return null;
    }

    /**
     * Visit a IfNode.
     * Visit the condition, then branch and else branch.
     *
     * @param node the IfNode to visit
     * @return null
     */
    @Override
    public Void visitNode(IfNode node) {
        if (print) printNode(node);
        visit(node.condition);
        visit(node.thenBranch);
        visit(node.elseBranch);
        return null;
    }

    /**
     * Visit an EqualNode.
     * Visit the left and right expression.
     *
     * @param node the EqualNode to visit
     * @return null
     */
    @Override
    public Void visitNode(EqualNode node) {
        if (print) printNode(node);
        visit(node.left);
        visit(node.right);
        return null;
    }

    /**
     * Visit a TimesNode.
     * Visit the left and right expression.
     *
     * @param node the TimesNode to visit
     * @return null
     */
    @Override
    public Void visitNode(TimesNode node) {
        if (print) printNode(node);
        visit(node.left);
        visit(node.right);
        return null;
    }

    /**
     * Visit a PlusNode.
     * Visit the left and right expression.
     *
     * @param node the PlusNode to visit
     * @return null
     */
    @Override
    public Void visitNode(PlusNode node) {
        if (print) printNode(node);
        visit(node.left);
        visit(node.right);
        return null;
    }

    /**
     * Visit a CallNode.
     * Lookup the function in the symbol table and set the entry and nesting level.
     * Visit the arguments.
     *
     * @param node the CallNode to visit
     * @return null
     */
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

    /**
     * Visit a IdNode.
     * Lookup the variable or parameter in the symbol table and set the entry and nesting level.
     *
     * @param node the IdNode to visit
     * @return null
     */
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

    /**
     * Visit a BoolNode.
     * Visit the expression.
     *
     * @param node the BoolNode to visit
     * @return null
     */
    @Override
    public Void visitNode(BoolNode node) {
        if (print) printNode(node, node.val.toString());
        return null;
    }

    /**
     * Visit an IntNode.
     * Visit the expression.
     *
     * @param node the IntNode to visit
     * @return null
     */
    @Override
    public Void visitNode(IntNode node) {
        if (print) printNode(node, node.val.toString());
        return null;
    }

    // OPERATOR EXTENSION

    /**
     * Visit a GreaterEqualNode.
     * Visit the left and right expression.
     *
     * @param node the GreaterEqualNode to visit
     * @return null
     */
    @Override
    public Void visitNode(GreaterEqualNode node) {
        if (print) printNode(node);
        visit(node.left);
        visit(node.right);
        return null;
    }

    /**
     * Visit a LessEqualNode.
     * Visit the left and right expression.
     *
     * @param node the LessEqualNode to visit
     * @return null
     */
    @Override
    public Void visitNode(LessEqualNode node) {
        if (print) printNode(node);
        visit(node.left);
        visit(node.right);
        return null;
    }

    /**
     * Visit a NotNode.
     * Visit the expression.
     *
     * @param node the NotNode to visit
     * @return null
     */
    @Override
    public Void visitNode(NotNode node) {
        if (print) printNode(node);
        visit(node.exp);
        return null;
    }

    /**
     * Visit a MinusNode.
     * Visit the left and right expression.
     *
     * @param node the MinusNode to visit
     * @return null
     */
    @Override
    public Void visitNode(MinusNode node) {
        if (print) printNode(node);
        visit(node.left);
        visit(node.right);
        return null;
    }

    /**
     * Visit an OrNode.
     * Visit the left and right expression.
     *
     * @param node the OrNode to visit
     * @return null
     */
    @Override
    public Void visitNode(OrNode node) {
        if (print) printNode(node);
        visit(node.left);
        visit(node.right);
        return null;
    }

    /**
     * Visit an AndNode.
     * Visit the left and right expression.
     *
     * @param node the AndNode to visit
     * @return null
     */
    @Override
    public Void visitNode(AndNode node) {
        if (print) printNode(node);
        visit(node.left);
        visit(node.right);
        return null;
    }

    /**
     * Visit a DivNode.
     * Visit the left and right expression.
     *
     * @param node the DivNode to visit
     * @return null
     */
    @Override
    public Void visitNode(DivNode node) {
        if (print) printNode(node);
        visit(node.left);
        visit(node.right);
        return null;
    }


}
