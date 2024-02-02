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
        if (!isSubtype(visit(node.exp), ckvisit(node.returnType)))
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
     * Return the lowest common ancestor of the two types.
     *
     * @param node the IfNode node to visit
     * @return the lowest common ancestor of the two types
     * @throws TypeException if the condition is not correct or the branches are not compatible
     */
    @Override
    public TypeNode visitNode(IfNode node) throws TypeException {
        if (print) printNode(node);
        if (!(isSubtype(visit(node.condition), new BoolTypeNode())))
            throw new TypeException("Non boolean condition in if", node.getLine());
        TypeNode thenBranch = visit(node.thenBranch);
        TypeNode elseBranch = visit(node.elseBranch);
        final TypeNode returnType = lowestCommonAncestor(thenBranch, elseBranch);
        if (returnType == null) {
            throw new TypeException("Incompatible types in then-else branches", node.getLine());
        }
        return returnType;
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
        if (typeNode instanceof MethodTypeNode methodTypeNode) {
            typeNode = methodTypeNode.arrowTypeNode;
        }
        if (!(typeNode instanceof ArrowTypeNode arrowTypeNode)) {
            throw new TypeException("Invocation of a non-function " + node.id, node.getLine());
        }
        if (!(arrowTypeNode.parameters.size() == node.arguments.size())) {
            throw new TypeException("Wrong number of parameters in the invocation of " + node.id, node.getLine());
        }
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
        if (t instanceof ArrowTypeNode || t instanceof MethodTypeNode)
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
        if (print) printNode(node, node.value.toString());
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
        if (print) printNode(node, node.value.toString());
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

    // OBJECT ORIENTED EXTENSION

    /**
     * Visit a ClassNode node and check its type.
     * If the class has a super class, add it as super type in TypeRels Map.
     * Visit all methods in the class.
     * <p>
     * If the class has a super class, visit all fields and methods
     * in the class and check that their types are subtypes of the
     * types of the fields and methods in the super class.
     * Return null.
     *
     * @param node the ClassNode node to visit
     * @return null
     * @throws TypeException if the class has a super class and the super class is not defined
     */
    @Override
    public TypeNode visitNode(final ClassNode node) throws TypeException {
        if (print) printNode(node, node.classId);
        final boolean isSubClass = node.superClassId.isPresent();
        final String superId = isSubClass ? node.superClassId.get() : null;

        // if class has a super class, add it as super type in TypeRels Map
        if (isSubClass) {
            superType.put(node.classId, superId);
        }

        // visit all methods
        for (final MethodNode method : node.allMethods) {
            try {
                visit(method);
            } catch (TypeException e) {
                System.out.println("Type checking error in a class declaration: " + e.text);
            }
        }

        if (!isSubClass || node.superEntry == null) {
            return null;
        }

        // get the types of the class and the super class
        final ClassTypeNode classType = (ClassTypeNode) node.getType();
        final ClassTypeNode parentClassType = (ClassTypeNode) node.superEntry.type;

        // visit all fields in the class and check that their types are subtypes of the types of the fields in the super class
        for (final FieldNode field : node.allFields) {
            int position = -field.offset - 1;
            final boolean isOverriding = position < parentClassType.allFields.size();
            if (isOverriding && !isSubtype(classType.allFields.get(position), parentClassType.allFields.get(position))) {
                throw new TypeException("Wrong type for field " + field.id, field.getLine());
            }
        }

        // visit all methods in the class and check that their types are subtypes of the types of the methods in the super class
        for (final MethodNode method : node.allMethods) {
            int position = method.offset;
            final boolean isOverriding = position < parentClassType.allFields.size();
            if (isOverriding && !isSubtype(classType.allMethods.get(position), parentClassType.allMethods.get(position))) {
                throw new TypeException("Wrong type for method " + method.id, method.getLine());
            }
        }
        return null;
    }

    /**
     * Visit a MethodNode node and check its type.
     * Visit all declarations.
     * Visit the expression and check that its type is a subtype of the return type.
     *
     * @param node the MethodNode node to visit
     * @return null
     * @throws TypeException if the type of the expression is not a subtype of the return type
     */
    @Override
    public TypeNode visitNode(final MethodNode node) throws TypeException {
        if (print) printNode(node, node.id);
        for (final DecNode declaration : node.declarations) {
            try {
                visit(declaration);
            } catch (TypeException e) {
                System.out.println("Type checking error in a method declaration: " + e.text);
            }
        }
        // visit expression and check if it is a subtype of the return type
        if (!isSubtype(visit(node.exp), ckvisit(node.returnType))) {
            throw new TypeException("Wrong return type for method " + node.id, node.getLine());
        }
        return null;
    }

    /**
     * Visit an EmptyNode node and return an EmptyTypeNode.
     *
     * @param node the EmptyNode node to visit.
     * @return an EmptyTypeNode.
     */
    @Override
    public TypeNode visitNode(final EmptyNode node) {
        if (print) printNode(node);
        return new EmptyTypeNode();
    }

    /**
     * Visit a ClassCallNode node and check its type.
     * Visit the method entry and check that its type is a method type.
     * Check that the number of parameters is correct and that their types are correct.
     * Return the type of the method.
     *
     * @param node the ClassCallNode node to visit
     * @return the type of the method
     * @throws TypeException if the type of the method is not a method type
     *                       or if the number of parameters is not correct
     *                       or if the types of the parameters are not correct
     */
    @Override
    public TypeNode visitNode(final ClassCallNode node) throws TypeException {
        if (print) printNode(node, node.objectId);
        TypeNode methodType = visit(node.methodEntry);
        // visit method, if it is a method type, get the functional type
        if (methodType instanceof MethodTypeNode methodTypeNode) {
            methodType = methodTypeNode.arrowTypeNode;
        }
        // if it is not an arrow type, throw an exception
        if (!(methodType instanceof ArrowTypeNode arrowTypeNode)) {
            throw new TypeException("Invocation of a non-function " + node.methodId, node.getLine());
        }
        // check if the number of parameters is correct
        if (arrowTypeNode.parameters.size() != node.args.size()) {
            throw new TypeException("Wrong number of parameters in the invocation of method " + node.methodId, node.getLine());
        }
        // check if the types of the parameters are correct
        for (int i = 0; i < node.args.size(); i++) {
            if (!(isSubtype(visit(node.args.get(i)), arrowTypeNode.parameters.get(i)))) {
                throw new TypeException("Wrong type for " + (i + 1) + "-th parameter in the invocation of method " + node.methodId, node.getLine());
            }
        }
        return arrowTypeNode.returnType;
    }

    /**
     * Visit a NewNode node and check its type.
     * Visit the class entry and check that it is a class type.
     * Check that the number of parameters is correct and that their types are correct.
     *
     * @param node the NewNode node to visit
     * @return the class type
     * @throws TypeException if the class entry is not a class type or if the number of parameters is wrong or if their types are wrong
     */
    @Override
    public TypeNode visitNode(final NewNode node) throws TypeException {
        if (print) printNode(node, node.classId);
        final TypeNode typeNode = visit(node.entry);
        // if the class entry is not a class type, throw an exception
        if (!(typeNode instanceof ClassTypeNode classTypeNode)) {
            throw new TypeException("Invocation of a non-constructor " + node.classId, node.getLine());
        }
        // check if the number of parameters is correct
        if (classTypeNode.allFields.size() != node.arguments.size()) {
            throw new TypeException("Wrong number of parameters in the invocation of constructor " + node.classId, node.getLine());
        }
        // check if the types of the parameters are correct
        for (int i = 0; i < node.arguments.size(); i++) {
            if (!(isSubtype(visit(node.arguments.get(i)), classTypeNode.allFields.get(i)))) {
                throw new TypeException("Wrong type for " + (i + 1) + "-th parameter in the invocation of constructor " + node.classId, node.getLine());
            }
        }
        return new RefTypeNode(node.classId);
    }

    /**
     * Visit a ClassTypeNode node.
     * Visit all fields and methods.
     * Return null.
     *
     * @param node the ClassTypeNode node to visit.
     * @return null.
     * @throws TypeException if there is a type error.
     */
    @Override
    public TypeNode visitNode(final ClassTypeNode node) throws TypeException {
        if (print) printNode(node);
        // Visit all fields and methods
        for (final TypeNode field : node.allFields) visit(field);
        for (final ArrowTypeNode method : node.allMethods) visit(method);
        return null;
    }

    /**
     * Visit a MethodTypeNode node.
     * Visit all parameters and the return type.
     * Return null.
     *
     * @param node the MethodTypeNode node to visit.
     * @return null.
     * @throws TypeException if there is a type error.
     */
    @Override
    public TypeNode visitNode(final MethodTypeNode node) throws TypeException {
        if (print) printNode(node);
        // Visit all parameters and the return type
        for (final TypeNode parameter : node.arrowTypeNode.parameters) visit(parameter);
        visit(node.arrowTypeNode.returnType, "->");
        return null;
    }

    /**
     * Visit a RefTypeNode node.
     *
     * @param node the RefTypeNode node to visit.
     * @return null.
     */
    @Override
    public TypeNode visitNode(final RefTypeNode node) {
        if (print) printNode(node);
        return null;
    }

    /**
     * Visit an EmptyTypeNode node.
     *
     * @param node the EmptyTypeNode node to visit.
     * @return null.
     */
    @Override
    public TypeNode visitNode(final EmptyTypeNode node) {
        if (print) printNode(node);
        return null;
    }
}