package compiler;

import compiler.AST.*;
import compiler.exc.*;
import compiler.lib.*;

import static compiler.TypeRels.*;

//visitNode(n) fa il type checking di un Node n e ritorna:
//- per una espressione, il suo tipo (oggetto BoolTypeNode o IntTypeNode)
//- per una dichiarazione, "null"; controlla la correttezza interna della dichiarazione
//(- per un tipo: "null"; controlla che il tipo non sia incompleto) 
//
//visitSTentry(s) ritorna, per una STentry s, il tipo contenuto al suo interno
public class TypeCheckEASTVisitor extends BaseEASTVisitor<TypeNode, TypeException> {

    private RefTypeNode at;

    TypeCheckEASTVisitor() {
        super(true);
    } // enables incomplete tree exceptions

    TypeCheckEASTVisitor(boolean debug) {
        super(true, debug);
    } // enables print for debugging

    //checks that a type object is visitable (not incomplete)
    private TypeNode ckvisit(TypeNode t) throws TypeException {
        visit(t);
        return t;
    }

    @Override
    public TypeNode visitNode(ProgLetInNode n) throws TypeException {
        if (print) printNode(n);
        for (Node dec : n.declist)
            try {
                visit(dec);
            } catch (IncomplException e) {
            } catch (TypeException e) {
                System.out.println("Type checking error in a declaration: " + e.text);
            }
        return visit(n.exp);
    }

    @Override
    public TypeNode visitNode(ProgNode n) throws TypeException {
        if (print) printNode(n);
        return visit(n.exp);
    }

    @Override
    public TypeNode visitNode(FunNode n) throws TypeException {
        if (print) printNode(n, n.id);
        for (Node dec : n.declist)
            try {
                visit(dec);
            } catch (IncomplException e) {
            } catch (TypeException e) {
                System.out.println("Type checking error in a declaration: " + e.text);
            }
        if (!isSubtype(visit(n.exp), ckvisit(n.retType)))
            throw new TypeException("Wrong return type for function " + n.id, n.getLine());
        return null;
    }

    @Override
    public TypeNode visitNode(VarNode n) throws TypeException {
        if (print) printNode(n, n.id);
        if (!isSubtype(visit(n.exp), ckvisit(n.getType())))
            throw new TypeException("Incompatible value for variable " + n.id, n.getLine());
        return null;
    }

    @Override
    public TypeNode visitNode(PrintNode n) throws TypeException {
        if (print) printNode(n);
        return visit(n.exp);
    }

    @Override
    public TypeNode visitNode(IfNode n) throws TypeException {
        if (print) printNode(n);
        if (!(isSubtype(visit(n.cond), new BoolTypeNode())))
            throw new TypeException("Non boolean condition in if", n.getLine());
        TypeNode t = visit(n.th);
        TypeNode e = visit(n.el);
        if (isSubtype(t, e)) return e;
        if (isSubtype(e, t)) return t;
        throw new TypeException("Incompatible types in then-else branches", n.getLine());
    }

    @Override
    public TypeNode visitNode(EqualNode n) throws TypeException {
        if (print) printNode(n);
        TypeNode l = visit(n.left);
        TypeNode r = visit(n.right);
        if (!(isSubtype(l, r) || isSubtype(r, l)))
            throw new TypeException("Incompatible types in equal", n.getLine());
        return new BoolTypeNode();
    }

    @Override
    public TypeNode visitNode(TimesNode n) throws TypeException {
        if (print) printNode(n);
        if (!(isSubtype(visit(n.left), new IntTypeNode())
                && isSubtype(visit(n.right), new IntTypeNode())))
            throw new TypeException("Non integers in multiplication", n.getLine());
        return new IntTypeNode();
    }

    @Override
    public TypeNode visitNode(PlusNode n) throws TypeException {
        if (print) printNode(n);
        if (!(isSubtype(visit(n.left), new IntTypeNode())
                && isSubtype(visit(n.right), new IntTypeNode())))
            throw new TypeException("Non integers in sum", n.getLine());
        return new IntTypeNode();
    }


    @Override
    public TypeNode visitNode(GreaterEqualNode n) throws TypeException {
        if (print) printNode(n);
        TypeNode l = visit(n.left);
        TypeNode r = visit(n.right);
        if (!(isSubtype(l, r) || isSubtype(r, l)))
            throw new TypeException("Incompatible types in greater equal", n.getLine());
        return new BoolTypeNode();
    }

    @Override
    public TypeNode visitNode(LessEqualNode n) throws TypeException {
        if (print) printNode(n);
        TypeNode l = visit(n.left);
        TypeNode r = visit(n.right);
        if (!(isSubtype(l, r) || isSubtype(r, l)))
            throw new TypeException("Incompatible types in less equal", n.getLine());
        return new BoolTypeNode();
    }

    @Override
    public TypeNode visitNode(NotNode n) throws TypeException {
        if (print) printNode(n);
        if (!isSubtype(visit(n.node), new BoolTypeNode()))
            throw new TypeException("Non bool in not", n.getLine());
        return new BoolTypeNode();
    }

    @Override
    public TypeNode visitNode(MinusNode n) throws TypeException {
        if (print) printNode(n);
        if (!(isSubtype(visit(n.left), new IntTypeNode())
                && isSubtype(visit(n.right), new IntTypeNode())))
            throw new TypeException("Non integers in sub", n.getLine());
        return new IntTypeNode();
    }

    @Override
    public TypeNode visitNode(OrNode n) throws TypeException {
        if (print) printNode(n);
        if (!(isSubtype(visit(n.left), new BoolTypeNode())
                && isSubtype(visit(n.right), new BoolTypeNode())))
            throw new TypeException("Non bool in or", n.getLine());
        return new BoolTypeNode();
    }

    @Override
    public TypeNode visitNode(DivNode n) throws TypeException {
        if (print) printNode(n);
        if (!(isSubtype(visit(n.left), new IntTypeNode())
                && isSubtype(visit(n.right), new IntTypeNode())))
            throw new TypeException("Non integers in div", n.getLine());
        return new IntTypeNode();
    }

    @Override
    public TypeNode visitNode(AndNode n) throws TypeException {
        if (print) printNode(n);
        if (!(isSubtype(visit(n.left), new BoolTypeNode())
                && isSubtype(visit(n.right), new BoolTypeNode())))
            throw new TypeException("Non bool in and", n.getLine());
        return new BoolTypeNode();
    }

    @Override
    public TypeNode visitNode(CallNode n) throws TypeException {
        if (print) printNode(n, n.id);
        TypeNode t = visit(n.entry);
        if(t instanceof MethodTypeNode){
            t = ((MethodTypeNode) t).fun;
        }
        if (!(t instanceof ArrowTypeNode))
            throw new TypeException("Invocation of a non-function " + n.id, n.getLine());
        ArrowTypeNode at = (ArrowTypeNode) t;
        if (!(at.parlist.size() == n.arglist.size()))
            throw new TypeException("Wrong number of parameters in the invocation of " + n.id, n.getLine());
        for (int i = 0; i < n.arglist.size(); i++)
            if (!(isSubtype(visit(n.arglist.get(i)), at.parlist.get(i))))
                throw new TypeException("Wrong type for " + (i + 1) + "-th parameter in the invocation of " + n.id, n.getLine());
        return at.ret;
    }

    @Override
    public TypeNode visitNode(IdNode n) throws TypeException {
        if (print) printNode(n, n.id);
        TypeNode t = visit(n.entry);
        if (t instanceof ArrowTypeNode || t instanceof MethodTypeNode) {   // da valutare methodtypeNode
            throw new TypeException("Wrong usage of function/method identifier " + n.id, n.getLine());
        }
        if (t instanceof ClassTypeNode) {
            throw new TypeException("Wrong usage of class identifier " + n.id, n.getLine());
        }
        return t;
    }

    @Override
    public TypeNode visitNode(BoolNode n) {
        if (print) printNode(n, n.val.toString());
        return new BoolTypeNode();
    }

    @Override
    public TypeNode visitNode(IntNode n) {
        if (print) printNode(n, n.val.toString());
        return new IntTypeNode();
    }

// gestione tipi incompleti	(se lo sono lancia eccezione)

    @Override
    public TypeNode visitNode(ArrowTypeNode n) throws TypeException {
        if (print) printNode(n);
        for (Node par : n.parlist) visit(par);
        visit(n.ret, "->"); //marks return type
        return null;
    }

    @Override
    public TypeNode visitNode(BoolTypeNode n) {
        if (print) printNode(n);
        return null;
    }

    @Override
    public TypeNode visitNode(IntTypeNode n) {
        if (print) printNode(n);
        return null;
    }

// STentry (ritorna campo type)

    @Override
    public TypeNode visitSTentry(STentry entry) throws TypeException {
        if (print) printSTentry("type");
        return ckvisit(entry.type);
    }

    @Override
    public TypeNode visitNode(ClassNode n) throws TypeException {
        if (print) printNode(n, n.id);

        for (MethodNode m : n.methods) {
            try {
                visit(m);
            } catch (TypeException e) {
                System.out.println("Type checking error in a class declaration: " + e.text);
            }
        }
        return null;
    }

    @Override
    public TypeNode visitNode(MethodNode n) throws TypeException {
        if (print) printNode(n, n.id);
        for (Node dec : n.declist)
            try {
                visit(dec);
            } catch (IncomplException e) {
            } catch (TypeException e) {
                System.out.println("Type checking error in a method declaration: " + e.text);
            }
        if (!isSubtype(visit(n.exp), ckvisit(n.retType)))
            throw new TypeException("Wrong return type for method " + n.id, n.getLine());
        return null;
    }

    @Override
    public TypeNode visitNode(ClassCallNode n) throws TypeException {
        if (print) printNode(n, n.id);
        TypeNode t = visit(n.methodEntry);
        if(t instanceof MethodTypeNode){
            t = ((MethodTypeNode) t).fun;
        }
        if (!(t instanceof ArrowTypeNode))
            throw new TypeException("Invocation of a non-method " + n.id, n.getLine());
        ArrowTypeNode at = (ArrowTypeNode) t;
        if (!(at.parlist.size() == n.arglist.size()))
            throw new TypeException("Wrong number of parameters in the invocation of method " + n.id, n.getLine());
        for (int i = 0; i < n.arglist.size(); i++)
            if (!(isSubtype(visit(n.arglist.get(i)), at.parlist.get(i))))
                throw new TypeException("Wrong type for " + (i + 1) + "-th parameter in the invocation of method " + n.id, n.getLine());
        return at.ret;
    }

    @Override
    public TypeNode visitNode(NewNode n) throws TypeException {
        if (print) printNode(n, n.id);
        TypeNode t = visit(n.entry);
        if (!(t instanceof ClassTypeNode))
            throw new TypeException("Invocation of a non-construction " + n.id, n.getLine());
        ClassTypeNode ct = (ClassTypeNode) t;
        if (!(ct.fields.size() == n.arglist.size()))
            throw new TypeException("Wrong number of parameters in the invocation of constructor of class " + n.id, n.getLine());
        for (int i = 0; i < n.arglist.size(); i++)
            if (!(isSubtype(visit(n.arglist.get(i)), ct.fields.get(i))))
                throw new TypeException("Wrong type for " + (i + 1) + "-th parameter in the invocation of constructor " + n.id, n.getLine());
        return new RefTypeNode(n.id);
    }

    @Override
    public TypeNode visitNode(EmptyNode n) throws TypeException {
        if (print) printNode(n);
        return new EmptyTypeNode();
    }

    @Override
    public TypeNode visitNode(RefTypeNode n) throws TypeException {
        if (print) printNode(n, n.id);
        return null;
    }

    @Override
    public TypeNode visitNode(ClassTypeNode n) throws TypeException {
        if (print) printNode(n);
        for (TypeNode f : n.fields) visit(f);
        for (ArrowTypeNode m : n.methods) visit(m);
        return null;
    }

    @Override
    public TypeNode visitNode(MethodTypeNode n) throws TypeException {
        if (print) printNode(n);
        for (TypeNode p : n.fun.parlist) visit(p);
        visit(n.fun.ret, "->"); //marks return type
        return null;
    }

    @Override
    public TypeNode visitNode(EmptyTypeNode n) throws TypeException {
        if (print) printNode(n);
        return null;
    }
}