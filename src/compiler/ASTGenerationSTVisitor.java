package compiler;

import java.util.*;
import java.util.stream.Collectors;

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

    // OBJECT ORIENTED EXTENSION

    /**
     * Visit the Cldec context.
     * It returns the ClassNode built with the class id, the super id, the fields and the methods.
     *
     * @param context the parse tree
     * @return the ClassNode built with the class id, the super id, the fields and the methods
     */
    @Override
    public Node visitCldec(final CldecContext context) {
        if (print) printVarAndProdName(context);
        if (context.ID().size() == 0) return null; // Incomplete ST
        final String classId = context.ID(0).getText();
        // Get the super id if present
        final Optional<String> superId = context.EXTENDS() == null ?
                Optional.empty() : Optional.of(context.ID(1).getText());
        final int idPadding = superId.isPresent() ? 2 : 1;

        // Get the fields
        final List<FieldNode> fields = new ArrayList<>();
        for (int i = idPadding; i < context.ID().size(); i++) {
            final String id = context.ID(i).getText();
            final TypeNode type = (TypeNode) visit(context.type(i - idPadding));
            final FieldNode f = new FieldNode(id, type);
            f.setLine(context.ID(i).getSymbol().getLine());
            fields.add(f);
        }
        // Get the methods
        final List<MethodNode> methods = context.methdec().stream()
                .map(x -> (MethodNode) visit(x))
                .collect(Collectors.toList());
        final ClassNode classNode = new ClassNode(classId, superId, fields, methods);
        classNode.setLine(context.ID(0).getSymbol().getLine());
        return classNode;
    }

    /**
     * Visit the Methdec context.
     * It returns the MethodNode built with the method id, the return type, the parameters, the declarations and the body.
     *
     * @param context the parse tree
     * @return the MethodNode built with the method id, the return type, the parameters, the declarations and the body
     */
    @Override
    public Node visitMethdec(final MethdecContext context) {
        if (print) printVarAndProdName(context);
        if (context.ID().size() == 0) return null; // Incomplete ST
        final String methodId = context.ID(0).getText();
        final TypeNode returnType = (TypeNode) visit(context.type(0));

        final int idPadding = 1;
        final List<ParNode> parameters = new ArrayList<>();
        for (int i = idPadding; i < context.ID().size(); i++) {
            final String id = context.ID(i).getText();
            final TypeNode type = (TypeNode) visit(context.type(i));
            final ParNode p = new ParNode(id, type);
            p.setLine(context.ID(i).getSymbol().getLine());
            parameters.add(p);
        }

        // Get the declarations
        final List<DecNode> declarations = context.dec().stream()
                .map(x -> (DecNode) visit(x))
                .collect(Collectors.toList());
        final Node exp = visit(context.exp());
        final MethodNode methodNode = new MethodNode(methodId, returnType, parameters, declarations, exp);
        methodNode.setLine(context.ID(0).getSymbol().getLine());
        return methodNode;
    }

    /**
     * Visit the Null context.
     * It returns the EmptyNode.
     *
     * @param context the parse tree
     * @return the EmptyNode
     */
    @Override
    public Node visitNull(final NullContext context) {
        if (print) printVarAndProdName(context);
        return new EmptyNode();
    }

    /**
     * Visit the DotCall context.
     * It returns the ClassCallNode built with the object id, the method id and the arguments.
     *
     * @param context the parse tree
     * @return the ClassCallNode built with the object id, the method id and the arguments
     */
    @Override
    public Node visitDotCall(final DotCallContext context) {
        if (print) printVarAndProdName(context);
        if (context.ID().size() != 2) return null; // Incomplete ST
        final String objectId = context.ID(0).getText();
        final String methodId = context.ID(1).getText();
        // Visit the arguments
        final List<Node> arguments = context.exp().stream()
                .map(this::visit)
                .collect(Collectors.toList());

        // Build the ClassCallNode
        final ClassCallNode classCallNode = new ClassCallNode(objectId, methodId, arguments);
        classCallNode.setLine(context.ID(0).getSymbol().getLine());
        return classCallNode;
    }

    /**
     * Visit the New context.
     * It returns the NewNode built with the class id and the arguments.
     *
     * @param context the parse tree
     * @return the NewNode built with the class id and the arguments
     */
    @Override
    public Node visitNew(final NewContext context) {
        if (print) printVarAndProdName(context);
        if (context.ID() == null) return null; // Incomplete ST
        final String classId = context.ID().getText();
        final List<Node> arguments = context.exp().stream()
                .map(this::visit)
                .toList();
        // Build the NewNode
        final NewNode newNode = new NewNode(classId, arguments);
        newNode.setLine(context.ID().getSymbol().getLine());
        return newNode;
    }

    /* *******************
     *********************
     * OOP Type Nodes
     *********************
     ******************* */

    /**
     * Visit the IdType context.
     * It returns the RefTypeNode built with the id.
     *
     * @param context the parse tree
     * @return the RefTypeNode built
     */
    @Override
    public Node visitIdType(final IdTypeContext context) {
        if (print) printVarAndProdName(context);
        final String id = context.ID().getText();
        final RefTypeNode node = new RefTypeNode(id);
        node.setLine(context.ID().getSymbol().getLine());
        return node;
    }
}
