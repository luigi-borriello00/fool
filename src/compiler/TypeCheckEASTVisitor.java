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
    public TypeNode visitNode(ProgLetInNode node) throws TypeException {
        if (print) printNode(node);
        for (Node dec : node.declarations)
            try {
                visit(dec);
            } catch (IncomplException e) {
            } catch (TypeException e) {
                System.out.println("Type checking error in a declaration: " + e.text);
            }
        return visit(node.exp);
    }

    @Override
    public TypeNode visitNode(ProgNode node) throws TypeException {
        if (print) printNode(node);
        return visit(node.exp);
    }

    @Override
    public TypeNode visitNode(FunNode node) throws TypeException {
        if (print) printNode(node, node.id);
        for (Node dec : node.declarations)
            try {
                visit(dec);
            } catch (IncomplException e) {
            } catch (TypeException e) {
                System.out.println("Type checking error in a declaration: " + e.text);
            }
        if (!isSubtype(visit(node.exp), ckvisit(node.retType)))
            throw new TypeException("Wrong return type for function " + node.id, node.getLine());
        return null;
    }

    @Override
    public TypeNode visitNode(VarNode node) throws TypeException {
        if (print) printNode(node, node.id);
        if (!isSubtype(visit(node.exp), ckvisit(node.getType())))
            throw new TypeException("Incompatible value for variable " + node.id, node.getLine());
        return null;
    }

    @Override
    public TypeNode visitNode(PrintNode node) throws TypeException {
        if (print) printNode(node);
        return visit(node.exp);
    }

    @Override
    public TypeNode visitNode(IfNode node) throws TypeException {
        if (print) printNode(node);
        if (!(isSubtype(visit(node.condition), new BoolTypeNode())))
            throw new TypeException("Non boolean condition in if", node.getLine());
        TypeNode t = visit(node.thenBranch);
        TypeNode e = visit(node.elseBranch);
        if (isSubtype(t, e)) return e;
        if (isSubtype(e, t)) return t;
        throw new TypeException("Incompatible types in then-else branches", node.getLine());
    }

    @Override
    public TypeNode visitNode(EqualNode node) throws TypeException {
        if (print) printNode(node);
        TypeNode l = visit(node.left);
        TypeNode r = visit(node.right);
        if (!(isSubtype(l, r) || isSubtype(r, l)))
            throw new TypeException("Incompatible types in equal", node.getLine());
        return new BoolTypeNode();
    }

    @Override
    public TypeNode visitNode(TimesNode node) throws TypeException {
        if (print) printNode(node);
        if (!(isSubtype(visit(node.left), new IntTypeNode())
                && isSubtype(visit(node.right), new IntTypeNode())))
            throw new TypeException("Non integers in multiplication", node.getLine());
        return new IntTypeNode();
    }

    @Override
    public TypeNode visitNode(PlusNode node) throws TypeException {
        if (print) printNode(node);
        if (!(isSubtype(visit(node.left), new IntTypeNode())
                && isSubtype(visit(node.right), new IntTypeNode())))
            throw new TypeException("Non integers in sum", node.getLine());
        return new IntTypeNode();
    }

    @Override
    public TypeNode visitNode(CallNode node) throws TypeException {
        if (print) printNode(node, node.id);
        TypeNode t = visit(node.entry);
        if (!(t instanceof ArrowTypeNode))
            throw new TypeException("Invocation of a non-function " + node.id, node.getLine());
        ArrowTypeNode at = (ArrowTypeNode) t;
        if (!(at.parameters.size() == node.arguments.size()))
            throw new TypeException("Wrong number of parameters in the invocation of " + node.id, node.getLine());
        for (int i = 0; i < node.arguments.size(); i++)
            if (!(isSubtype(visit(node.arguments.get(i)), at.parameters.get(i))))
                throw new TypeException("Wrong type for " + (i + 1) + "-th parameter in the invocation of " + node.id, node.getLine());
        return at.returnType;
    }

    @Override
    public TypeNode visitNode(IdNode node) throws TypeException {
        if (print) printNode(node, node.id);
        TypeNode t = visit(node.entry);
        if (t instanceof ArrowTypeNode)
            throw new TypeException("Wrong usage of function identifier " + node.id, node.getLine());
        return t;
    }

    @Override
    public TypeNode visitNode(BoolNode node) {
        if (print) printNode(node, node.val.toString());
        return new BoolTypeNode();
    }

    @Override
    public TypeNode visitNode(IntNode node) {
        if (print) printNode(node, node.val.toString());
        return new IntTypeNode();
    }

// gestione tipi incompleti	(se lo sono lancia eccezione)

    @Override
    public TypeNode visitNode(ArrowTypeNode node) throws TypeException {
        if (print) printNode(node);
        for (Node par : node.parameters) visit(par);
        visit(node.returnType, "->"); //marks return type
        return null;
    }

    @Override
    public TypeNode visitNode(BoolTypeNode node) {
        if (print) printNode(node);
        return null;
    }

    @Override
    public TypeNode visitNode(IntTypeNode node) {
        if (print) printNode(node);
        return null;
    }

// STentry (ritorna campo type)

    @Override
    public TypeNode visitSTentry(STentry entry) throws TypeException {
        if (print) printSTentry("type");
        return ckvisit(entry.type);
    }

    // OPERATOR EXTENSION

    @Override
    public TypeNode visitNode(GreaterEqualNode node) throws TypeException {
        if (print) printNode(node);
        if (!(isSubtype(visit(node.left), new IntTypeNode())
                && isSubtype(visit(node.right), new IntTypeNode())))
            throw new TypeException("Non integers in greater-equal", node.getLine());
        return new BoolTypeNode();
    }

    @Override
    public TypeNode visitNode(LessEqualNode node) throws TypeException {
        if (print) printNode(node);
        if (!(isSubtype(visit(node.left), new IntTypeNode())
                && isSubtype(visit(node.right), new IntTypeNode())))
            throw new TypeException("Non integers in less-equal", node.getLine());
        return new BoolTypeNode();
    }

    @Override
    public TypeNode visitNode(NotNode node) throws TypeException {
        if (print) printNode(node);
        if (!(isSubtype(visit(node.exp), new BoolTypeNode())))
            throw new TypeException("Non boolean in not", node.getLine());
        return new BoolTypeNode();
    }

    @Override
    public TypeNode visitNode(OrNode node) throws TypeException {
        if (print) printNode(node);
        if (!(isSubtype(visit(node.left), new BoolTypeNode())
                && isSubtype(visit(node.right), new BoolTypeNode())))
            throw new TypeException("Non boolean in or", node.getLine());
        return new BoolTypeNode();
    }

    @Override
    public TypeNode visitNode(MinusNode node) throws TypeException {
        if (print) printNode(node);
        if (!(isSubtype(visit(node.left), new IntTypeNode())
                && isSubtype(visit(node.right), new IntTypeNode())))
            throw new TypeException("Non integers in minus", node.getLine());
        return new IntTypeNode();
    }

    @Override
    public TypeNode visitNode(DivNode node) throws TypeException {
        if (print) printNode(node);
        if (!(isSubtype(visit(node.left), new IntTypeNode())
                && isSubtype(visit(node.right), new IntTypeNode())))
            throw new TypeException("Non integers in division", node.getLine());
        return new IntTypeNode();
    }

    @Override
    public TypeNode visitNode(AndNode node) throws TypeException {
        if (print) printNode(node);
        if (!(isSubtype(visit(node.left), new BoolTypeNode())
                && isSubtype(visit(node.right), new BoolTypeNode())))
            throw new TypeException("Non boolean in and", node.getLine());
        return new BoolTypeNode();
    }


}