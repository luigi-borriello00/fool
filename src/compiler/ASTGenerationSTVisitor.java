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

	ASTGenerationSTVisitor() {}
    ASTGenerationSTVisitor(boolean debug) { print=debug; }
        
    private void printVarAndProdName(ParserRuleContext ctx) {
        String prefix="";        
    	Class<?> ctxClass=ctx.getClass(), parentClass=ctxClass.getSuperclass();
        if (!parentClass.equals(ParserRuleContext.class)) // parentClass is the var context (and not ctxClass itself)
        	prefix=lowerizeFirstChar(extractCtxName(parentClass.getName()))+": production #";
    	System.out.println(indent+prefix+lowerizeFirstChar(extractCtxName(ctxClass.getName())));                               	
    }
        
    @Override
	public Node visit(ParseTree t) {
    	if (t==null) return null;
        String temp=indent;
        indent=(indent==null)?"":indent+"  ";
        Node result = super.visit(t);
        indent=temp;
        return result; 
	}

	@Override
	public Node visitProg(ProgContext c) {
		if (print) printVarAndProdName(c);
		return visit(c.progbody());
	}

	@Override
	public Node visitLetInProg(LetInProgContext c) {
		if (print) printVarAndProdName(c);
		List<DecNode> classDeclist = new ArrayList<>();
		for (CldecContext clDec : c.cldec()) classDeclist.add((DecNode) visit(clDec));
		List<DecNode> declist = new ArrayList<>();
		for (DecContext dec : c.dec()) declist.add((DecNode) visit(dec));
		List<DecNode> allDeclist = new ArrayList<>();
		allDeclist.addAll(classDeclist);
		allDeclist.addAll(declist);
		return new ProgLetInNode(allDeclist, visit(c.exp()));
	}

	@Override
	public Node visitNoDecProg(NoDecProgContext c) {
		if (print) printVarAndProdName(c);
		return new ProgNode(visit(c.exp()));
	}

	@Override
	public Node visitTimesDiv(TimesDivContext ctx) {
		if (print) printVarAndProdName(ctx);
		boolean times= ctx.TIMES( ) != null;
		Node n = null;
		if ( times ) {
			n = new TimesNode(visit(ctx.exp(0)), visit(ctx.exp(1)));
			n.setLine(ctx.TIMES().getSymbol().getLine()); // setLine added
		} else { //deve essere ctx.DIV( ) != null
			n = new DivNode(visit(ctx.exp(0)), visit(ctx.exp(1)));
			n.setLine(ctx.DIV().getSymbol().getLine()); // setLine added
		}
		return n;
	}

	@Override
	public Node visitPlusMinus(PlusMinusContext ctx) {
		if (print) printVarAndProdName(ctx);
		boolean plus= ctx.PLUS( ) != null;
		Node n = null;
		if ( plus ) {
			n = new PlusNode(visit(ctx.exp(0)), visit(ctx.exp(1)));
			n.setLine(ctx.PLUS().getSymbol().getLine()); // setLine added
		} else { //deve essere ctx.DIV( ) != null
			n = new MinusNode(visit(ctx.exp(0)), visit(ctx.exp(1)));
			n.setLine(ctx.MINUS().getSymbol().getLine()); // setLine added
		}
		return n;
	}

	@Override
	public Node visitAndOr(AndOrContext ctx) {
		if (print) printVarAndProdName(ctx);
		boolean andop= ctx.AND( ) != null;
		Node n = null;
		if ( andop ) {
			n = new AndNode(visit(ctx.exp(0)), visit(ctx.exp(1)));
			n.setLine(ctx.AND().getSymbol().getLine()); // setLine added
		} else { //deve essere ctx.DIV( ) != null
			n = new OrNode(visit(ctx.exp(0)), visit(ctx.exp(1)));
			n.setLine(ctx.OR().getSymbol().getLine()); // setLine added
		}
		return n;
	}

	@Override
	public Node visitNot(NotContext ctx) {
		if (print) printVarAndProdName(ctx);
		Node n = new NotNode(visit(ctx.exp()));
		n.setLine(ctx.NOT().getSymbol().getLine());
		return n;
	}
	@Override
	public Node visitComp(CompContext ctx) {
		if (print) printVarAndProdName(ctx);
		boolean EQ= ctx.EQ() != null;
		Node n = null;
		if(EQ){
			n = new EqualNode(visit(ctx.exp(0)), visit(ctx.exp(1)));
			n.setLine(ctx.EQ().getSymbol().getLine());
		}else{
			boolean GEQ= ctx.GE() != null;
			if(GEQ){
				n = new GreaterEqualNode(visit(ctx.exp(0)), visit(ctx.exp(1)));
				n.setLine(ctx.GE().getSymbol().getLine());
			}else{
				n = new LessEqualNode(visit(ctx.exp(0)), visit(ctx.exp(1)));
				n.setLine(ctx.LE().getSymbol().getLine());
			}
		}
		//todo check else? is a lexer issue i think
		return n;
	}

	@Override
	public Node visitVardec(VardecContext c) {
		if (print) printVarAndProdName(c);
		Node n = null;
		if (c.ID()!=null) { //non-incomplete ST
			n = new VarNode(c.ID().getText(), (TypeNode) visit(c.type()), visit(c.exp()));
			n.setLine(c.VAR().getSymbol().getLine());
		}
        return n;
	}

	@Override
	public Node visitFundec(FundecContext c) {
		if (print) printVarAndProdName(c);
		List<ParNode> parList = new ArrayList<>();
		for (int i = 1; i < c.ID().size(); i++) { 
			ParNode p = new ParNode(c.ID(i).getText(),(TypeNode) visit(c.type(i)));
			p.setLine(c.ID(i).getSymbol().getLine());
			parList.add(p);
		}
		List<DecNode> decList = new ArrayList<>();
		for (DecContext dec : c.dec()) decList.add((DecNode) visit(dec));
		Node n = null;
		if (c.ID().size()>0) { //non-incomplete ST
			n = new FunNode(c.ID(0).getText(),(TypeNode)visit(c.type(0)),parList,decList,visit(c.exp()));
			n.setLine(c.FUN().getSymbol().getLine());
		}
        return n;
	}

	@Override
	public Node visitIntType(IntTypeContext c) {
		if (print) printVarAndProdName(c);
		return new IntTypeNode();
	}

	@Override
	public Node visitBoolType(BoolTypeContext c) {
		if (print) printVarAndProdName(c);
		return new BoolTypeNode();
	}

	@Override
	public Node visitInteger(IntegerContext c) {
		if (print) printVarAndProdName(c);
		int v = Integer.parseInt(c.NUM().getText());
		return new IntNode(c.MINUS()==null?v:-v);
	}

	@Override
	public Node visitTrue(TrueContext c) {
		if (print) printVarAndProdName(c);
		return new BoolNode(true);
	}

	@Override
	public Node visitFalse(FalseContext c) {
		if (print) printVarAndProdName(c);
		return new BoolNode(false);
	}

	@Override
	public Node visitIf(IfContext c) {
		if (print) printVarAndProdName(c);
		Node ifNode = visit(c.exp(0));
		Node thenNode = visit(c.exp(1));
		Node elseNode = visit(c.exp(2));
		Node n = new IfNode(ifNode, thenNode, elseNode);
		n.setLine(c.IF().getSymbol().getLine());			
        return n;		
	}

	@Override
	public Node visitPrint(PrintContext c) {
		if (print) printVarAndProdName(c);
		return new PrintNode(visit(c.exp()));
	}

	@Override
	public Node visitPars(ParsContext c) {
		if (print) printVarAndProdName(c);
		return visit(c.exp());
	}

	@Override
	public Node visitId(IdContext c) {
		if (print) printVarAndProdName(c);
		Node n = new IdNode(c.ID().getText());
		n.setLine(c.ID().getSymbol().getLine());
		return n;
	}

	@Override
	public Node visitCall(CallContext c) {
		if (print) printVarAndProdName(c);		
		List<Node> arglist = new ArrayList<>();
		for (ExpContext arg : c.exp()) arglist.add(visit(arg));
		Node n = new CallNode(c.ID().getText(), arglist);
		n.setLine(c.ID().getSymbol().getLine());
		return n;
	}


	// OBJECT ORIENTED EXTENSION


	@Override
	public Node visitCldec(CldecContext ctx) {
		if (print) printVarAndProdName(ctx);
		if(ctx.ID().size() == 0) return null;

		final String clId = ctx.ID(0).getText();

		final List<FieldNode> fieldList = new ArrayList<>();
		for(int i = 1; i < ctx.ID().size(); i++) {
			final String fieldId = ctx.ID(i).getText();
			final TypeNode fieldType = (TypeNode) visit(ctx.type(i-1));
			FieldNode f = new FieldNode(fieldId, fieldType);
			f.setLine(ctx.ID(i).getSymbol().getLine());
			fieldList.add(f);
		}

		final List<MethodNode> methodList = new ArrayList<>();
		for (MethdecContext dec : ctx.methdec()) {
			MethodNode m = (MethodNode) visit(dec);
			methodList.add(m);
		}

		final ClassNode c = new ClassNode(clId, fieldList, methodList);
		c.setLine(ctx.ID(0).getSymbol().getLine());
		return c;
	}

	@Override
	public Node visitMethdec(MethdecContext ctx) {
		if (print) printVarAndProdName(ctx);
		if(ctx.ID().size() == 0) return null;

		final String methId = ctx.ID(0).getText();
		final TypeNode methType = (TypeNode) visit(ctx.type(0));

		final List<ParNode> params = new ArrayList<>();
		for(int i = 1; i < ctx.ID().size(); i++) {
			final String id = ctx.ID(i).getText();
			final TypeNode type = (TypeNode) visit(ctx.type(i));
			ParNode p = new ParNode(id, type);
			p.setLine(ctx.ID(i).getSymbol().getLine());
			params.add(p);
		}

		final List<DecNode> declarations = new ArrayList<>();
		for (DecContext dec : ctx.dec()) {
			DecNode m = (DecNode) visit(dec);
			declarations.add(m);
		}

		Node exp = visit(ctx.exp());
		final MethodNode m = new MethodNode(methId, methType, params, declarations, exp);
		m.setLine(ctx.ID(0).getSymbol().getLine());
		return m;
	}

	@Override
	public Node visitNew(NewContext ctx) {
		if (print) printVarAndProdName(ctx);
		if(ctx.ID() == null) return null;

		final String classId = ctx.ID().getText();
		final List<Node> args = new ArrayList<>();
		for (ExpContext arg : ctx.exp()) {
			args.add(visit(arg));
		}

		final NewNode n = new NewNode(classId, args);
		n.setLine(ctx.ID().getSymbol().getLine());
		return n;
	}

	@Override
	public Node visitNull(NullContext ctx) {
		if (print) printVarAndProdName(ctx);
		return new EmptyNode();
	}

	@Override
	public Node visitDotCall(DotCallContext ctx) {
		if (print) printVarAndProdName(ctx);
		if(ctx.ID().size() != 2) return null;

		final String classId = ctx.ID(0).getText();
		final String methodId = ctx.ID(1).getText();
		final List<Node> args = new ArrayList<>();
		for (ExpContext arg : ctx.exp()) {
			args.add(visit(arg));
		}

		final ClassCallNode n = new ClassCallNode(classId, methodId, args);
		n.setLine(ctx.ID(0).getSymbol().getLine());
		return n;
	}

	@Override
	public Node visitIdType(IdTypeContext ctx) {
		if (print) printVarAndProdName(ctx);

		final String id = ctx.ID().getText();
		final RefTypeNode n = new RefTypeNode(id);
		n.setLine(ctx.ID().getSymbol().getLine());
		return n;
	}
}
