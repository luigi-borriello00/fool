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

    /**
     * Visit a ProgLetInNode and check its declarations types
     * Visit the declaration and return its type
     *
     * @param node ProgLetInNode
     * @return null
     * @throws TypeException
     */
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

    /**
     * Visit a ProgNode and check its expression type
     * Visit the expression and return its type
     *
     * @param node ProgNode
     * @return null
     * @throws TypeException
     */
    @Override
    public TypeNode visitNode(ProgNode node) throws TypeException {
        if (print) printNode(node);
        return visit(node.exp);
    }

    /**
     * Visit a FunNode node and check its type.
     * For each declaration, visit it.
     * Then visit the expression and check that its type is a subtype of the return type.
     * If not, throws an exception.
     *
     * @param node the FunNode node to visit
     * @return null
     * @throws TypeException if a declaration is not correct or the return type is not correct
     */
    @Override
    public TypeNode visitNode(FunNode node) throws TypeException {
        if (print) printNode(node, node.id);
        for (Node dec : node.declarations)
            try {
                visit(dec);
            } catch (IncomplException ignored) {
            } catch (TypeException e) {
                System.out.println("Type checking error in a declaration: " + e.text);
            }
        if (!isSubtype(visit(node.exp), ckvisit(node.retType)))
            throw new TypeException("Wrong return type for function " + node.id, node.getLine());
        return null;
    }

    /**
     * Visit a VarNode node and check its type.
     * Visit the expression and check that its type is a subtype of the variable type.
     * If not, throws an exception.
     *
     * @param node the VarNode node to visit
     * @return null
     * @throws TypeException if the expression is not correct
     */
    @Override
    public TypeNode visitNode(VarNode node) throws TypeException {
        if (print) printNode(node, node.id);
        if (!isSubtype(visit(node.exp), ckvisit(node.getType())))
            throw new TypeException("Incompatible value for variable " + node.id, node.getLine());
        return null;
    }

    /**
     * Visit a PrintNode node and check its type.
     *
     * @param node the PrintNode node to visit
     * @return null
     * @throws TypeException if the expression is not correct
     */
    @Override
    public TypeNode visitNode(PrintNode node) throws TypeException {
        if (print) printNode(node);
        return visit(node.exp);
    }

    /**
     * Visit a IfNode node and check its type.
     * Visit the condition and check that its type is a subtype of BoolTypeNode.
     * If not, throws an exception.
     * Then visit the then branch and the else branch and check that their types are compatible.
     * If not, throws an exception.
     *
     * @param node the IfNode node to visit
     * @return null
     * @throws TypeException if the condition is not correct or the branches are not compatible
     */
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

    /**
     * Visit a EqualNode node and check its type.
     * Visit the left and right expressions and check that their types are compatible.
     * If not, throws an exception.
     *
     * @param node the EqualNode node to visit
     * @return BoolTypeNode
     * @throws TypeException if the expressions are not compatible
     */
    @Override
    public TypeNode visitNode(EqualNode node) throws TypeException {
        if (print) printNode(node);
        TypeNode l = visit(node.left);
        TypeNode r = visit(node.right);
        if (!(isSubtype(l, r) || isSubtype(r, l)))
            throw new TypeException("Incompatible types in equal", node.getLine());
        return new BoolTypeNode();
    }

    /**
     * Visit a TimesNode node and check its type.
     * Visit the left and right expressions and check that their types are compatible (both IntTypeNode).
     * If not, throws an exception.
     *
     * @param node the TimesNode node to visit
     * @return IntTypeNode
     * @throws TypeException if the expressions are not compatible
     */
    @Override
    public TypeNode visitNode(TimesNode node) throws TypeException {
        if (print) printNode(node);
        if (!(isSubtype(visit(node.left), new IntTypeNode())
                && isSubtype(visit(node.right), new IntTypeNode())))
            throw new TypeException("Non integers in multiplication", node.getLine());
        return new IntTypeNode();
    }

    /**
     * Visit a PlusNode node and check its type.
     * Visit the left and right expressions and check that their types are compatible (both IntTypeNode).
     * If not, throws an exception.
     *
     * @param node the PlusNode node to visit
     * @return IntTypeNode
     * @throws TypeException if the expressions are not compatible
     */
    @Override
    public TypeNode visitNode(PlusNode node) throws TypeException {
        if (print) printNode(node);
        if (!(isSubtype(visit(node.left), new IntTypeNode())
                && isSubtype(visit(node.right), new IntTypeNode())))
            throw new TypeException("Non integers in sum", node.getLine());
        return new IntTypeNode();
    }

    /**
     * Visit a CallNode node and check its type.
     * Visit the function entry and check that its type is an ArrowTypeNode.
     * If not, throws an exception.
     * Then visit the arguments and check that their types are compatible with the function parameters.
     * If not, throws an exception.
     *
     * @param node the CallNode node to visit
     * @return the return type of the function
     * @throws TypeException if the function entry is not correct or the arguments are not compatible
     */
    @Override
    public TypeNode visitNode(CallNode node) throws TypeException {
        if (print) printNode(node, node.id);
        TypeNode typeNode = visit(node.entry);
        if (!(typeNode instanceof ArrowTypeNode))
            throw new TypeException("Invocation of a non-function " + node.id, node.getLine());
        ArrowTypeNode arrowTypeNode = (ArrowTypeNode) typeNode;
        if (!(arrowTypeNode.parameters.size() == node.arguments.size()))
            throw new TypeException("Wrong number of parameters in the invocation of " + node.id, node.getLine());
        for (int i = 0; i < node.arguments.size(); i++)
            if (!(isSubtype(visit(node.arguments.get(i)), arrowTypeNode.parameters.get(i))))
                throw new TypeException("Wrong type for " + (i + 1) + "-th parameter in the invocation of " + node.id, node.getLine());
        return arrowTypeNode.returnType;
    }

    /**
     * Visit a IdNode node and check its type.
     * Visit the function entry and check that its type is not an ArrowTypeNode.
     * If not, throws an exception.
     *
     * @param node the IdNode node to visit
     * @return the type of the identifier
     * @throws TypeException if the function entry is not correct
     */
    @Override
    public TypeNode visitNode(IdNode node) throws TypeException {
        if (print) printNode(node, node.id);
        TypeNode t = visit(node.entry);
        if (t instanceof ArrowTypeNode)
            throw new TypeException("Wrong usage of function identifier " + node.id, node.getLine());
        return t;
    }

    /**
     * Visit a BoolNode node and check its type.
     *
     * @param node the BoolNode node to visit
     * @return BoolTypeNode
     */
    @Override
    public TypeNode visitNode(BoolNode node) {
        if (print) printNode(node, node.val.toString());
        return new BoolTypeNode();
    }

    /**
     * Visit a IntNode node and check its type.
     *
     * @param node the IntNode node to visit
     * @return IntTypeNode
     */
    @Override
    public TypeNode visitNode(IntNode node) {
        if (print) printNode(node, node.val.toString());
        return new IntTypeNode();
    }

// gestione tipi incompleti	(se lo sono lancia eccezione)

    /**
     * Visit a ArrowTypeNode node.
     * Visit the parameters and the return type.
     * Return null.
     *
     * @param node the ArrowTypeNode node to visit
     * @return null
     */
    @Override
    public TypeNode visitNode(ArrowTypeNode node) throws TypeException {
        if (print) printNode(node);
        for (Node par : node.parameters) visit(par);
        visit(node.returnType, "->"); //marks return type
        return null;
    }

    /**
     * Visit a BoolTypeNode node.
     * Return null.
     *
     * @param node the BoolTypeNode node to visit
     * @return null
     */
    @Override
    public TypeNode visitNode(BoolTypeNode node) {
        if (print) printNode(node);
        return null;
    }

    /**
     * Visit a IntTypeNode node.
     * Return null.
     *
     * @param node the IntTypeNode node to visit
     * @return null
     */
    @Override
    public TypeNode visitNode(IntTypeNode node) {
        if (print) printNode(node);
        return null;
    }

// STentry (ritorna campo type)

    /**
     * Visit a STentry node.
     * Return the type of the entry.
     *
     * @param entry the STentry node to visit
     * @return the type of the entry
     * @throws TypeException if the type is not correct
     */
    @Override
    public TypeNode visitSTentry(STentry entry) throws TypeException {
        if (print) printSTentry("type");
        return ckvisit(entry.type);
    }

    // OPERATOR EXTENSION

    /**
     * Visit a GreaterNode node and check its type.
     * Visit the left and right expressions and check that their types are compatible (both IntTypeNode).
     * If not, throws an exception.
     *
     * @param node the GreaterNode node to visit
     * @return BoolTypeNode
     * @throws TypeException if the expressions are not compatible
     */
    @Override
    public TypeNode visitNode(GreaterEqualNode node) throws TypeException {
        if (print) printNode(node);
        if (!(isSubtype(visit(node.left), new IntTypeNode())
                && isSubtype(visit(node.right), new IntTypeNode())))
            throw new TypeException("Non integers in greater-equal", node.getLine());
        return new BoolTypeNode();
    }

    /**
     * Visit a LessEqualNode node and check its type.
     * Visit the left and right expressions and check that their types are compatible (both IntTypeNode).
     * If not, throws an exception.
     *
     * @param node the LessEqualNode node to visit
     * @return BoolTypeNode
     * @throws TypeException if the expressions are not compatible
     */
    @Override
    public TypeNode visitNode(LessEqualNode node) throws TypeException {
        if (print) printNode(node);
        if (!(isSubtype(visit(node.left), new IntTypeNode())
                && isSubtype(visit(node.right), new IntTypeNode())))
            throw new TypeException("Non integers in less-equal", node.getLine());
        return new BoolTypeNode();
    }

    /**
     * Visit a NotNode node and check its type.
     * Visit the expression and check that its type is a subtype of BoolTypeNode.
     * If not, throws an exception.
     * Then return BoolTypeNode.
     *
     * @param node the NotNode node to visit
     * @return BoolTypeNode
     * @throws TypeException if the expression is not correct
     */
    @Override
    public TypeNode visitNode(NotNode node) throws TypeException {
        if (print) printNode(node);
        if (!(isSubtype(visit(node.exp), new BoolTypeNode())))
            throw new TypeException("Non boolean in not", node.getLine());
        return new BoolTypeNode();
    }

    /**
     * Visit a OrNode node and check its type.
     * Visit the left and right expressions and check that their types are compatible (both BoolTypeNode).
     * If not, throws an exception.
     * Then return BoolTypeNode.
     *
     * @param node the OrNode node to visit
     * @return BoolTypeNode
     * @throws TypeException if the expression is not correct
     */
    @Override
    public TypeNode visitNode(OrNode node) throws TypeException {
        if (print) printNode(node);
        if (!(isSubtype(visit(node.left), new BoolTypeNode())
                && isSubtype(visit(node.right), new BoolTypeNode())))
            throw new TypeException("Non boolean in or", node.getLine());
        return new BoolTypeNode();
    }

    /**
     * Visit a AndNode node and check its type.
     * Visit the left and right expressions and check that their types are compatible (both BoolTypeNode).
     * If not, throws an exception.
     * Then return BoolTypeNode.
     *
     * @param node the AndNode node to visit
     * @return BoolTypeNode
     * @throws TypeException if the expression is not correct
     */
    @Override
    public TypeNode visitNode(AndNode node) throws TypeException {
        if (print) printNode(node);
        if (!(isSubtype(visit(node.left), new BoolTypeNode())
                && isSubtype(visit(node.right), new BoolTypeNode())))
            throw new TypeException("Non boolean in and", node.getLine());
        return new BoolTypeNode();
    }

    /**
     * Visit a MinusNode node and check its type.
     * Visit the left and right expressions and check that their types are compatible (both IntTypeNode).
     * If not, throws an exception.
     *
     * @param node the MinusNode node to visit
     * @return IntTypeNode
     * @throws TypeException if the expressions are not compatible
     */
    @Override
    public TypeNode visitNode(MinusNode node) throws TypeException {
        if (print) printNode(node);
        if (!(isSubtype(visit(node.left), new IntTypeNode())
                && isSubtype(visit(node.right), new IntTypeNode())))
            throw new TypeException("Non integers in minus", node.getLine());
        return new IntTypeNode();
    }

    /**
     * Visit a DivNode node and check its type.
     * Visit the left and right expressions and check that their types are compatible (both IntTypeNode).
     * If not, throws an exception.
     *
     * @param node the DivNode node to visit
     * @return IntTypeNode
     * @throws TypeException if the expressions are not compatible
     */
    @Override
    public TypeNode visitNode(DivNode node) throws TypeException {
        if (print) printNode(node);
        if (!(isSubtype(visit(node.left), new IntTypeNode())
                && isSubtype(visit(node.right), new IntTypeNode())))
            throw new TypeException("Non integers in division", node.getLine());
        return new IntTypeNode();
    }


}