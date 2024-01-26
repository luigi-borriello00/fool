package compiler;

import java.util.*;

import org.antlr.v4.runtime.ParserRuleContext;
import org.antlr.v4.runtime.tree.ParseTree;

import compiler.AST.*;
import compiler.FOOLParser.*;
import compiler.lib.*;

import static compiler.lib.FOOLlib.*;

public class ASTGenerationSTVisitor extends FOOLBaseVisitor<Node> {

    String indent;
    public boolean print;

    ASTGenerationSTVisitor() {
    }

    ASTGenerationSTVisitor(boolean debug) {
        print = debug;
    }

    /**
     * Print the name of the variable and the name of the productionode.
     *
     * @param context the current context
     */
    private void printVarAndProdName(ParserRuleContext context) {
        String prefix = "";
        Class<?> ctxClass = context.getClass(), parentClass = ctxClass.getSuperclass();
        if (!parentClass.equals(ParserRuleContext.class)) // parentClass is the var context (and not ctxClass itself)
            prefix = lowerizeFirstChar(extractCtxName(parentClass.getName())) + ": production #";
        System.out.println(indent + prefix + lowerizeFirstChar(extractCtxName(ctxClass.getName())));
    }

    /**
     * Increase the indentation and visit the parse tree.
     *
     * @param tree the parse tree
     * @return the result of the visit
     */
    @Override
    public Node visit(ParseTree tree) {
        if (tree == null) return null;
        String temp = indent;
        indent = (indent == null) ? "" : indent + "  ";
        Node result = super.visit(tree);
        indent = temp;
        return result;
    }

    /**
     * Visit the Prog context.
     * It visits the progbody context and returns the result of the visit.
     *
     * @param context the parse tree
     * @return the result of the visit
     */
    @Override
    public Node visitProg(ProgContext context) {
        if (print) printVarAndProdName(context);
        return visit(context.progbody());
    }

    /**
     * Visit the LetInProg context.
     * It returns the ProgLetInNode built with the results of the visits.
     *
     * @param context the parse tree
     * @return the ProgLetInNode built with the results of the visits
     */
    @Override
    public Node visitLetInProg(LetInProgContext context) {
        if (print) printVarAndProdName(context);
        List<DecNode> declist = new ArrayList<>();
        for (DecContext dec : context.dec()) declist.add((DecNode) visit(dec));
        return new ProgLetInNode(declist, visit(context.exp()));
    }

    /**
     * Visit the NoDecProg context.
     * It returns the ProgNode built with the result of the visit.
     *
     * @param context the parse tree
     * @return the ProgNode built with the result of the visit
     */
    @Override
    public Node visitNoDecProg(NoDecProgContext context) {
        if (print) printVarAndProdName(context);
        return new ProgNode(visit(context.exp()));
    }

    /**
     * Visit the TimesDiv context.
     * It returns the TimesNode or the DivNode built with the results of the visits.
     *
     * @param context the parse tree
     * @return the TimesNode or the DivNode built with the results of the visits
     */
    @Override
    public Node visitTimesDiv(TimesDivContext context) {
        if (context.TIMES() == null) {
            if (print) printVarAndProdName(context);
            DivNode node = new DivNode(visit(context.exp(0)), visit(context.exp(1)));
            node.setLine(context.DIV().getSymbol().getLine());
            return node;
        } else {
            if (print) printVarAndProdName(context);
            TimesNode node = new TimesNode(visit(context.exp(0)), visit(context.exp(1)));
            node.setLine(context.TIMES().getSymbol().getLine());
            return node;
        }
    }

    /**
     * Visit the PlusMinus context.
     * It returns the PlusNode or the MinusNode built with the results of the visits.
     *
     * @param context the parse tree
     * @return the PlusNode or the MinusNode built with the results of the visits
     */
    @Override
    public Node visitPlusMinus(PlusMinusContext context) {
        if (context.PLUS() == null) {
            if (print) printVarAndProdName(context);
            Node node = new MinusNode(visit(context.exp(0)), visit(context.exp(1)));
            node.setLine(context.MINUS().getSymbol().getLine());
            return node;
        } else {
            if (print) printVarAndProdName(context);
            Node node = new PlusNode(visit(context.exp(0)), visit(context.exp(1)));
            node.setLine(context.PLUS().getSymbol().getLine());
            return node;
        }
    }

    /**
     * Visit the Comp context.
     * It returns the EqualNode, the GreaterEqualNode or the LessEqualNode built with the results of the visits.
     *
     * @param context the parse tree
     * @return the EqualNode, the GreaterEqualNode or the LessEqualNode built with the results of the visits
     */
    @Override
    public Node visitComp(CompContext context) {
        if (print) printVarAndProdName(context);
        if (context.EQ() != null) {
            // It's an EqualNode
            final Node node = new EqualNode(visit(context.exp(0)), visit(context.exp(1)));
            node.setLine(context.EQ().getSymbol().getLine());
            return node;
        } else if (context.GE() != null) {
            // It's a GreaterEqualNode
            final Node node = new GreaterEqualNode(visit(context.exp(0)), visit(context.exp(1)));
            node.setLine(context.GE().getSymbol().getLine());
            return node;
        } else {
            // It's a LessEqualNode
            final Node node = new LessEqualNode(visit(context.exp(0)), visit(context.exp(1)));
            node.setLine(context.LE().getSymbol().getLine());
            return node;
        }
    }

    /**
     * Visit the AndOr context.
     * It returns the AndNode or the OrNode built with the results of the visits.
     *
     * @param context the parse tree
     * @return the AndNode or the OrNode built with the results of the visits
     */
    @Override
    public Node visitAndOr(AndOrContext context) {
        if (print) printVarAndProdName(context);
        if (context.AND() != null) {
            // It's an AndNode
            final Node node = new AndNode(visit(context.exp(0)), visit(context.exp(1)));
            node.setLine(context.AND().getSymbol().getLine());
            return node;
        } else {
            // It's an OrNode
            final Node node = new OrNode(visit(context.exp(0)), visit(context.exp(1)));
            node.setLine(context.OR().getSymbol().getLine());
            return node;
        }
    }

    /**
     * Visit the vardec context.
     * It returns the VarNode built with the results of the visits.
     *
     * @param context the parse tree
     * @return the VarNode built with the results of the visits
     */
    @Override
    public Node visitVardec(VardecContext context) {
        if (print) printVarAndProdName(context);
        Node node = null;
        if (context.ID() != null) { //non-incomplete ST
            node = new VarNode(context.ID().getText(), (TypeNode) visit(context.type()), visit(context.exp()));
            node.setLine(context.VAR().getSymbol().getLine());
        }
        return node;
    }

    /**
     * Visit the fundec context.
     * It returns the FunNode built with the results of the visits.
     *
     * @param context the parse tree
     * @return the FunNode built with the results of the visits
     */
    @Override
    public Node visitFundec(FundecContext context) {
        if (print) printVarAndProdName(context);
        List<ParNode> parList = new ArrayList<>();
        for (int i = 1; i < context.ID().size(); i++) {
            ParNode p = new ParNode(context.ID(i).getText(), (TypeNode) visit(context.type(i)));
            p.setLine(context.ID(i).getSymbol().getLine());
            parList.add(p);
        }
        List<DecNode> decList = new ArrayList<>();
        for (DecContext dec : context.dec()) decList.add((DecNode) visit(dec));
        Node node = null;
        if (context.ID().size() > 0) { //non-incomplete ST
            node = new FunNode(context.ID(0).getText(), (TypeNode) visit(context.type(0)), parList, decList, visit(context.exp()));
            node.setLine(context.FUN().getSymbol().getLine());
        }
        return node;
    }

    /* Type Nodes */

    /**
     * Visit the IntType context.
     * It returns the IntTypeNode.
     *
     * @param context the parse tree
     * @return the IntTypeNode
     */
    @Override
    public Node visitIntType(IntTypeContext context) {
        if (print) printVarAndProdName(context);
        return new IntTypeNode();
    }

    /**
     * Visit the BoolType context.
     * It returns the BoolTypeNode.
     *
     * @param context the parse tree
     * @return the BoolTypeNode
     */
    @Override
    public Node visitBoolType(BoolTypeContext context) {
        if (print) printVarAndProdName(context);
        return new BoolTypeNode();
    }

    /**
     * Visit the Integer context.
     *
     * @param context the parse tree
     * @return the IntNode
     */
    @Override
    public Node visitInteger(IntegerContext context) {
        if (print) printVarAndProdName(context);
        int v = Integer.parseInt(context.NUM().getText());
        return new IntNode(context.MINUS() == null ? v : -v);
    }

    /**
     * Visit the True context.
     * It returns the BoolNode.
     *
     * @param context the parse tree
     * @return the BoolNode
     */
    @Override
    public Node visitTrue(TrueContext context) {
        if (print) printVarAndProdName(context);
        return new BoolNode(true);
    }

    /**
     * Visit the False context.
     * It returns the BoolNode.
     *
     * @param context the parse tree
     * @return the BoolNode
     */
    @Override
    public Node visitFalse(FalseContext context) {
        if (print) printVarAndProdName(context);
        return new BoolNode(false);
    }

    /**
     * Visit the If context.
     * It returns the IfNode built with the results of the visits.
     *
     * @param context the parse tree
     * @return the IfNode built with the results of the visits
     */
    @Override
    public Node visitIf(IfContext context) {
        if (print) printVarAndProdName(context);
        Node ifNode = visit(context.exp(0));
        Node thenNode = visit(context.exp(1));
        Node elseNode = visit(context.exp(2));
        Node node = new IfNode(ifNode, thenNode, elseNode);
        node.setLine(context.IF().getSymbol().getLine());
        return node;
    }

    /**
     * Visit the Print context.
     * It returns the PrintNode built with the result of the visit.
     *
     * @param context the parse tree
     * @return the PrintNode built with the result of the visit
     */
    @Override
    public Node visitPrint(PrintContext context) {
        if (print) printVarAndProdName(context);
        return new PrintNode(visit(context.exp()));
    }

    /**
     * Visit the Pars context.
     * It returns the result of the visit.
     *
     * @param context the parse tree
     * @return the result of the visit
     */
    @Override
    public Node visitPars(ParsContext context) {
        if (print) printVarAndProdName(context);
        return visit(context.exp());
    }

    /**
     * Visit the Id context.
     * It returns the IdNode built with the result of the visit.
     *
     * @param context the parse tree
     * @return the IdNode built with the result of the visit
     */
    @Override
    public Node visitId(IdContext context) {
        if (print) printVarAndProdName(context);
        Node node = new IdNode(context.ID().getText());
        node.setLine(context.ID().getSymbol().getLine());
        return node;
    }

    /**
     * Visit the Call context.
     * It returns the CallNode built with the results of the visits.
     *
     * @param context the parse tree
     * @return the CallNode built with the results of the visits
     */
    @Override
    public Node visitCall(CallContext context) {
        if (print) printVarAndProdName(context);
        List<Node> arglist = new ArrayList<>();
        for (ExpContext arg : context.exp()) arglist.add(visit(arg));
        Node node = new CallNode(context.ID().getText(), arglist);
        node.setLine(context.ID().getSymbol().getLine());
        return node;
    }
}
