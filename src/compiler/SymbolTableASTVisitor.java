package compiler;

import java.util.*;

import compiler.AST.*;
import compiler.exc.*;
import compiler.lib.*;

public class SymbolTableASTVisitor extends BaseASTVisitor<Void, VoidException> {

    private final List<Map<String, STentry>> symbolTable = new ArrayList<>();
    /**
     * Class table, used for keep track of virtual tables.
     * Is used to preserve the internal declarations of a class (fields and methods) once the visitor has concluded the declaration of a class
     */
    private final Map<String, Map<String, STentry>> classTable = new HashMap<>();
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

    // OBJECT ORIENTED EXTENSION

    /**
     * Visit a ClassNode.
     * Check if the super class is declared and set the super entry.
     * Create a new ClassTypeNode and set the super class fields and methods if present.
     * Create the STentry and add it to the global symbol table.
     * Create the Virtual Table inheriting the super class methods if present.
     * Add the Virtual Table to the class table.
     * Add the new symbol table for methods and fields.
     * For each field, visit it and create the STentry, also enriching the classTypeNode.
     * For each method, enrich the classTypeNode and visit it.
     * Remove the symbol table for methods and fields and restore the nesting level.
     *
     * @param node the ClassNode to visit
     * @return null
     */
    @Override
    public Void visitNode(final ClassNode node) {
        if (print) printNode(node);
        ClassTypeNode newClassTypeNode = new ClassTypeNode();
        final boolean isSubClass = node.superClassId.isPresent();
        final String superId = isSubClass ? node.superClassId.get() : null;

        if (isSubClass) {
            // Check if the super class is declared
            if (classTable.containsKey(superId)) {
                // Get the super class entry
                final STentry superSTEntry = symbolTable.get(0).get(superId);
                final ClassTypeNode superTypeNode = (ClassTypeNode) superSTEntry.type;
                // Set the super class fields and methods
                newClassTypeNode = new ClassTypeNode(superTypeNode);
                node.superEntry = superSTEntry;
            } else {
                System.out.println("Class " + superId + " at line " + node.getLine() + " not declared");
                stErrors++;
            }
        }
        final ClassTypeNode classTypeNode = newClassTypeNode;
        node.setType(classTypeNode);

        // Add the class id to the global scope table checking for duplicates
        final STentry entry = new STentry(0, classTypeNode, decOffset--);
        final Map<String, STentry> globalScopeTable = symbolTable.get(0);
        if (globalScopeTable.put(node.classId, entry) != null) {
            System.out.println("Class id " + node.classId + " at line " + node.getLine() + " already declared");
            stErrors++;
        }

        // Add the class to the class table
        final Map<String, STentry> virtualTable = new HashMap<>();
        if (isSubClass) {
            final Map<String, STentry> superClassVirtualTable = classTable.get(superId);
            // we copy the superclass virtual table not the reference
            virtualTable.putAll(superClassVirtualTable);
        }
        // Add the virtual table to the class table
        classTable.put(node.classId, virtualTable);
        // Add the symbol table to the symbol table list
        symbolTable.add(virtualTable);
        // Setting the field offset
        nestingLevel++;
        int fieldOffset = -1;
        if (isSubClass) {
            final ClassTypeNode superTypeNode = (ClassTypeNode) symbolTable.get(0).get(superId).type;
            fieldOffset = -superTypeNode.fields.size() - 1;
        }

        /*
         * Visit each field, create the STentry and add it to the virtual table.
         */
        final Set<String> visitedClassNames = new HashSet<>();
        for (final FieldNode field : node.fields) {
            // check for duplicate fields
            if (visitedClassNames.contains(field.id)) {
                System.out.println(
                        "Field with id " + field.id + " on line " + field.getLine() + " was already declared"
                );
                stErrors++;
            } else {
                visitedClassNames.add(field.id);
            }
            visit(field);

            // The field offset grows from the end of the list
            STentry fieldEntry = new STentry(nestingLevel, field.getType(), fieldOffset--);
            final boolean isFieldOverridden = isSubClass && virtualTable.containsKey(field.id);
            // Handle the override of a field
            if (isFieldOverridden) {
                final STentry overriddenFieldEntry = virtualTable.get(field.id);
                final boolean isOverridingAMethod = overriddenFieldEntry.type instanceof MethodTypeNode;
                if (isOverridingAMethod) {
                    System.out.println("Cannot override method " + field.id + " with a field");
                    stErrors++;
                } else {
                    // if the field is overridden, the offset is the same in the heap
                    fieldEntry = new STentry(nestingLevel, field.getType(), overriddenFieldEntry.offset);
                    classTypeNode.fields.set(-fieldEntry.offset - 1, fieldEntry.type);
                }
            } else {
                classTypeNode.fields.add(-fieldEntry.offset - 1, fieldEntry.type);
            }
            // Add the field to the virtual table
            virtualTable.put(field.id, fieldEntry);
            field.offset = fieldEntry.offset;
        }

        /*
         * Visit each method, enrich the classTypeNode and visit it.
         */
        int prevDecOffset = decOffset;
        decOffset = 0;
        if (isSubClass) {
            final ClassTypeNode superTypeNode = (ClassTypeNode) symbolTable.get(0).get(superId).type;
            decOffset = superTypeNode.methods.size();
        }

        for (final MethodNode method : node.methods) {
            if (visitedClassNames.contains(method.id)) {
                System.out.println(
                        "Method with id " + method.id + " on line " + method.getLine() + " was already declared"
                );
                stErrors++;
            } else {
                visitedClassNames.add(method.id);
            }
            visit(method);
            final MethodTypeNode methodTypeNode = (MethodTypeNode) symbolTable.get(nestingLevel).get(method.id).type;
            classTypeNode.methods.add(
                    method.offset,
                    methodTypeNode.arrowTypeNode
            );
        }

        // Remove the class from the symbol table
        symbolTable.remove(nestingLevel--);
        decOffset = prevDecOffset;
        return null;
    }

    /**
     * Visit a FieldNode.
     *
     * @param node the FieldNode to visit
     * @return null
     */
    @Override
    public Void visitNode(final FieldNode node) {
        if (print) printNode(node);
        return null;
    }

    /**
     * Visit a MethodNode.
     * Create the MethodTypeNode and the STentry adding it to the symbol table.
     * If the method is overriding another method, check if the overriding is correct.
     * Create the new SymbolTable for the method scope.
     * For each parameter, create the STentry and add it to the symbol table.
     * Visit the declarations and the expression.
     * Finally, remove the method scope from the symbol table.
     *
     * @param node the MethodNode to visit
     * @return null
     */
    @Override
    public Void visitNode(final MethodNode node) {
        if (print) printNode(node);
        // obtain the current symbol table, who is the virtual table of the class of the method
        final Map<String, STentry> currentSymbolTable = symbolTable.get(nestingLevel);
        final List<TypeNode> parameters = node.parameters.stream()
                .map(ParNode::getType)
                .toList();
        final boolean isOverriding = currentSymbolTable.containsKey(node.id);
        final TypeNode methodType = new MethodTypeNode(new ArrowTypeNode(parameters, node.retType));
        STentry methodEntry = new STentry(nestingLevel, methodType, decOffset++);
        if (isOverriding) {
            final var overriddenMethodEntry = currentSymbolTable.get(node.id);
            final boolean isOverridingAMethod = overriddenMethodEntry != null && overriddenMethodEntry.type instanceof MethodTypeNode;
            if (isOverridingAMethod) {
                methodEntry = new STentry(nestingLevel, methodType, overriddenMethodEntry.offset);
                decOffset--;
            } else {
                System.out.println("Cannot override a class field with a method: " + node.id);
                stErrors++;
            }
        }

        node.offset = methodEntry.offset;
        currentSymbolTable.put(node.id, methodEntry);

        // Create a new table for the method.
        nestingLevel++;
        final Map<String, STentry> methodTable = new HashMap<>();
        symbolTable.add(methodTable);

        // Set the declaration offset
        int prevDecOffset = decOffset;
        decOffset = -2;
        int parameterOffset = 1;

        // check for duplicate parameters
        for (final ParNode parameter : node.parameters) {
            final STentry parameterEntry = new STentry(nestingLevel, parameter.getType(), parameterOffset++);
            if (methodTable.put(parameter.id, parameterEntry) != null) {
                System.out.println("Par id " + parameter.id + " at line " + node.getLine() + " already declared");
                stErrors++;
            }
        }
        // Now we are inside the method scope, so we can visit the declarations and the expression.
        node.declarations.forEach(this::visit);
        visit(node.exp);

        // Remove the current nesting level symbolTable.
        symbolTable.remove(nestingLevel--);
        decOffset = prevDecOffset;
        return null;
    }

    /**
     * Visit an EmptyNode.
     * Visit the expression.
     *
     * @param node the EmptyNode to visit
     * @return null
     */
    @Override
    public Void visitNode(EmptyNode node) {
        if (print) printNode(node);
        return null;
    }

    /**
     * Visit a NewNode.
     * Check if the class id was declared doing a lookup in the class table, if not, print an error.
     * If the class id was declared, set the entry of the node.
     * Finally, visit the arguments.
     *
     * @param node the NewNode to visit
     * @return null
     */
    @Override
    public Void visitNode(NewNode node) {
        if (print) printNode(node);
        if (!classTable.containsKey(node.classId)) {
            System.out.println("Class " + node.classId + " at line " + node.getLine() + " not declared");
            stErrors++;
        }
        // Attach the class entry to the node
        node.entry = symbolTable.get(0).get(node.classId);
        node.arguments.forEach(this::visit);
        return null;
    }


    /**
     * Visit a ClassCallNode.
     * Check if the object id was declared doing a lookup in the symbol table, if not, print an error.
     * If the object id was declared, check if the type is a RefTypeNode, if not, print an error.
     * If the type is a RefTypeNode, check if the method id is in the virtual table, if not, print an error.
     * If the method id is in the virtual table, set the entry and the nesting level of the node.
     * Finally, visit the arguments.
     *
     * @param node the ClassCallNode to visit
     * @return null
     */
    @Override
    public Void visitNode(final ClassCallNode node) {
        if (print) printNode(node);
        // Search for the object id in the symbol table
        final STentry classCallEntry = stLookup(node.objectId);
        if (classCallEntry == null) {
            System.out.println("Object id " + node.objectId + " was not declared");
            stErrors++;
        } else if (classCallEntry.type instanceof final RefTypeNode refTypeNode) {
            node.entry = classCallEntry;
            node.nestingLevel = nestingLevel;
            final Map<String, STentry> virtualTable = classTable.get(refTypeNode.refClassId);
            // Check if the method id is in the virtual table
            if (virtualTable.containsKey(node.methodId)) {
                node.methodEntry = virtualTable.get(node.methodId);
            } else {
                System.out.println(
                        "Object id " + node.objectId + " at line " + node.getLine() + " has no method " + node.methodId
                );
                stErrors++;
            }
        } else {
            System.out.println("Object id " + node.objectId + " at line " + node.getLine() + " is not a RefType");
            stErrors++;
        }
        node.args.forEach(this::visit);
        return null;
    }

    /**
     * Visit a ClassTypeNode.
     *
     * @param node the ClassTypeNode to visit
     * @return null
     */
    @Override
    public Void visitNode(final ClassTypeNode node) {
        if (print) printNode(node);
        return null;
    }

    /**
     * Visit a MethodTypeNode.
     *
     * @param node the MethodTypeNode to visit
     * @return null
     */
    @Override
    public Void visitNode(final MethodTypeNode node) {
        if (print) printNode(node);
        return null;
    }

    /**
     * Visit a RefTypeNode.
     * Check if the type id was declared doing a lookup in the class table.
     * If the type id was not declared, print an error.
     *
     * @param node the RefTypeNode to visit
     * @return null
     */
    @Override
    public Void visitNode(final RefTypeNode node) {
        if (print) printNode(node);
        if (!this.classTable.containsKey(node.refClassId)) {
            System.out.println("Class with id: " + node.refClassId + " on line: " + node.getLine() + " was not declared");
            stErrors++;
        }
        return null;
    }

    /**
     * Visit an EmptyTypeNode.
     *
     * @param node the EmptyTypeNode to visit
     * @return null
     */
    @Override
    public Void visitNode(final EmptyTypeNode node) {
        if (print) printNode(node);
        return null;
    }
}
