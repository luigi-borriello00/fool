package compiler.lib;

import compiler.AST.*;
import compiler.exc.*;

import static compiler.lib.FOOLlib.*;

public class BaseASTVisitor<S, E extends Exception> {

    private boolean incomplExc; // enables throwing IncomplException
    protected boolean print;    // enables printing
    protected String indent;

    protected BaseASTVisitor() {
    }

    protected BaseASTVisitor(boolean ie) {
        incomplExc = ie;
    }

    protected BaseASTVisitor(boolean ie, boolean p) {
        incomplExc = ie;
        print = p;
    }

    protected void printNode(Node n) {
        System.out.println(indent + extractNodeName(n.getClass().getName()));
    }

    protected void printNode(Node n, String s) {
        System.out.println(indent + extractNodeName(n.getClass().getName()) + ": " + s);
    }

    public S visit(Visitable v) throws E {
        return visit(v, "");                //performs unmarked visit
    }

    public S visit(Visitable v, String mark) throws E {   //when printing marks this visit with string mark
        if (v == null)
            if (incomplExc) throw new IncomplException();
            else
                return null;
        if (print) {
            String temp = indent;
            indent = (indent == null) ? "" : indent + "  ";
            indent += mark; //inserts mark
            try {
                S result = visitByAcc(v);
                return result;
            } finally {
                indent = temp;
            }
        } else
            return visitByAcc(v);
    }

    S visitByAcc(Visitable v) throws E {
        return v.accept(this);
    }

    public S visitNode(ProgLetInNode node) throws E {
        throw new UnimplException();
    }

    public S visitNode(ProgNode node) throws E {
        throw new UnimplException();
    }

    public S visitNode(FunNode node) throws E {
        throw new UnimplException();
    }

    public S visitNode(ParNode node) throws E {
        throw new UnimplException();
    }

    public S visitNode(VarNode node) throws E {
        throw new UnimplException();
    }

    public S visitNode(PrintNode node) throws E {
        throw new UnimplException();
    }

    public S visitNode(IfNode node) throws E {
        throw new UnimplException();
    }

    public S visitNode(EqualNode node) throws E {
        throw new UnimplException();
    }

    public S visitNode(TimesNode node) throws E {
        throw new UnimplException();
    }

    public S visitNode(PlusNode node) throws E {
        throw new UnimplException();
    }

    public S visitNode(CallNode node) throws E {
        throw new UnimplException();
    }

    public S visitNode(IdNode node) throws E {
        throw new UnimplException();
    }

    public S visitNode(BoolNode node) throws E {
        throw new UnimplException();
    }

    public S visitNode(IntNode node) throws E {
        throw new UnimplException();
    }

    public S visitNode(ArrowTypeNode node) throws E {
        throw new UnimplException();
    }

    public S visitNode(BoolTypeNode node) throws E {
        throw new UnimplException();
    }

    public S visitNode(IntTypeNode node) throws E {
        throw new UnimplException();
    }

    // OPERATOR EXTENSION

    public S visitNode(GreaterEqualNode node) throws E {
        throw new UnimplException();
    }

    public S visitNode(LessEqualNode node) throws E {
        throw new UnimplException();
    }

    public S visitNode(NotNode node) throws E {
        throw new UnimplException();
    }

    public S visitNode(MinusNode node) throws E {
        throw new UnimplException();
    }

    public S visitNode(OrNode node) throws E {
        throw new UnimplException();
    }

    public S visitNode(DivNode node) throws E {
        throw new UnimplException();
    }

    public S visitNode(AndNode node) throws E {
        throw new UnimplException();
    }

    // OBJECT-ORIENTED EXTENSION

//	public S visitNode(ClassNode n) throws E {throw new UnimplException();}
//	public S visitNode(FieldNode node) throws E {throw new UnimplException();}
//	public S visitNode(MethodNode n) throws E {throw new UnimplException();}
//	public S visitNode(ClassCallNode node) throws E {throw new UnimplException();}
//	public S visitNode(NewNode n) throws E {throw new UnimplException();}
//	public S visitNode(EmptyNode n) throws E {throw new UnimplException();}
//
//	public S visitNode(ClassTypeNode n) throws E {throw new UnimplException();}
//	public S visitNode(MethodTypeNode n) throws E {throw new UnimplException();}
//	public S visitNode(RefTypeNode n) throws E {throw new UnimplException();}
//	public S visitNode(EmptyTypeNode n) throws E {throw new UnimplException();}

}
