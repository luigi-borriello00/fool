package compiler;

import java.util.*;

import compiler.AST.*;
import compiler.exc.*;
import compiler.lib.*;

public class SymbolTableASTVisitor extends BaseASTVisitor<Void, VoidException> {

    // class table per preservare le dichiarazioni interne delle classi (campi e metodi)
    private Map<String, Map<String, STentry>> classTable = new HashMap<>();
    //symbol table per gestire le dichiarazioni di funzioni e variabili
    private List<Map<String, STentry>> symTable = new ArrayList<>();
    private int nestingLevel = 0; // current nesting level
    private int decOffset = -2; // counter for offset of local declarations at current nesting level
    int stErrors = 0;

    SymbolTableASTVisitor() {
    }

    SymbolTableASTVisitor(boolean debug) {
        super(debug);
    } // enables print for debugging

    private STentry stLookup(String id) {
        int j = nestingLevel;
        STentry entry = null;
        while (j >= 0 && entry == null)
            entry = symTable.get(j--).get(id);
        return entry;
    }

    @Override
    public Void visitNode(ProgLetInNode n) {
        if (print) printNode(n);
        Map<String, STentry> hm = new HashMap<>();
        symTable.add(hm);
        for (Node dec : n.declist) visit(dec);
        visit(n.exp);
        symTable.remove(0);
        return null;
    }

    @Override
    public Void visitNode(ProgNode n) {
        if (print) printNode(n);
        visit(n.exp);
        return null;
    }

    @Override
    public Void visitNode(FunNode n) {
        if (print) printNode(n);
        Map<String, STentry> hm = symTable.get(nestingLevel);
        List<TypeNode> parTypes = new ArrayList<>();
        for (ParNode par : n.parlist) parTypes.add(par.getType());
        STentry entry = new STentry(nestingLevel, new ArrowTypeNode(parTypes, n.retType), decOffset--);
        //inserimento di ID nella symtable
        if (hm.put(n.id, entry) != null) {
            System.out.println("Fun id " + n.id + " at line " + n.getLine() + " already declared");
            stErrors++;
        }
        //creare una nuova hashmap per la symTable
        nestingLevel++;
        Map<String, STentry> hmn = new HashMap<>();
        symTable.add(hmn);
        int prevNLDecOffset = decOffset; // stores counter for offset of declarations at previous nesting level
        decOffset = -2;

        int parOffset = 1;
        for (ParNode par : n.parlist)
            if (hmn.put(par.id, new STentry(nestingLevel, par.getType(), parOffset++)) != null) {
                System.out.println("Par id " + par.id + " at line " + n.getLine() + " already declared");
                stErrors++;
            }
        for (Node dec : n.declist) visit(dec);
        visit(n.exp);
        //rimuovere la hashmap corrente poiche' esco dallo scope
        symTable.remove(nestingLevel--);
        decOffset = prevNLDecOffset; // restores counter for offset of declarations at previous nesting level
        return null;
    }

    @Override
    public Void visitNode(VarNode n) {
        if (print) printNode(n);
        visit(n.exp);
        Map<String, STentry> hm = symTable.get(nestingLevel);
        STentry entry = new STentry(nestingLevel, n.getType(), decOffset--);
        //inserimento di ID nella symtable
        if (hm.put(n.id, entry) != null) {
            System.out.println("Var id " + n.id + " at line " + n.getLine() + " already declared");
            stErrors++;
        }
        return null;
    }

    @Override
    public Void visitNode(PrintNode n) {
        if (print) printNode(n);
        visit(n.exp);
        return null;
    }

    @Override
    public Void visitNode(IfNode n) {
        if (print) printNode(n);
        visit(n.cond);
        visit(n.th);
        visit(n.el);
        return null;
    }

    @Override
    public Void visitNode(EqualNode n) {
        if (print) printNode(n);
        visit(n.left);
        visit(n.right);
        return null;
    }

    @Override
    public Void visitNode(TimesNode n) {
        if (print) printNode(n);
        visit(n.left);
        visit(n.right);
        return null;
    }

    @Override
    public Void visitNode(PlusNode n) {
        if (print) printNode(n);
        visit(n.left);
        visit(n.right);
        return null;
    }

    @Override
    public Void visitNode(GreaterEqualNode n) throws VoidException {
        if (print) printNode(n);
        visit(n.left);
        visit(n.right);
        return null;
    }

    @Override
    public Void visitNode(LessEqualNode n) throws VoidException {
        if (print) printNode(n);
        visit(n.left);
        visit(n.right);
        return null;
    }

    @Override
    public Void visitNode(NotNode n) throws VoidException {
        if (print) printNode(n);
        visit(n.node);
        return null;
    }

    @Override
    public Void visitNode(MinusNode n) throws VoidException {
        if (print) printNode(n);
        visit(n.left);
        visit(n.right);
        return null;
    }

    @Override
    public Void visitNode(OrNode n) throws VoidException {
        if (print) printNode(n);
        visit(n.left);
        visit(n.right);
        return null;
    }

    @Override
    public Void visitNode(DivNode n) throws VoidException {
        if (print) printNode(n);
        visit(n.left);
        visit(n.right);
        return null;
    }

    @Override
    public Void visitNode(AndNode n) throws VoidException {
        if (print) printNode(n);
        visit(n.left);
        visit(n.right);
        return null;
    }

    @Override
    public Void visitNode(CallNode n) {
        if (print) printNode(n);
        STentry entry = stLookup(n.id);
        if (entry == null) {
            System.out.println("Fun id " + n.id + " at line " + n.getLine() + " not declared");
            stErrors++;
        } else {
            n.entry = entry;
            n.nl = nestingLevel;
        }
        for (Node arg : n.arglist) visit(arg);
        return null;
    }

    @Override
    public Void visitNode(IdNode n) {
        if (print) printNode(n);
        STentry entry = stLookup(n.id);
        if (entry == null) {
            System.out.println("Var or Par id " + n.id + " at line " + n.getLine() + " not declared");
            stErrors++;
        } else {
            n.entry = entry;
            n.nl = nestingLevel;
        }
        return null;
    }

    @Override
    public Void visitNode(BoolNode n) {
        if (print) printNode(n, n.val.toString());
        return null;
    }

    @Override
    public Void visitNode(IntNode n) {
        if (print) printNode(n, n.val.toString());
        return null;
    }

    @Override
    public Void visitNode(ClassNode n) {
        if (print) printNode(n);
        final Map<String, STentry> globalST = symTable.get(0);
        ClassTypeNode ctn = new ClassTypeNode(new ArrayList<>(), new ArrayList<>());
        //creo un nuovo classTypeNode con liste vuote di campi e metodi, che sarà il tipo della classe
        if (n.superID != null) {
            if (!classTable.containsKey(n.superID)) {
                System.out.println("Superclass id " + n.superID + " at line " + n.getLine() + " not declared");
                stErrors++;
            } else {
                STentry superEntry = globalST.get(n.superID);
                ClassTypeNode parent = (ClassTypeNode) superEntry.type;
                ctn.fields.addAll(parent.fields);
                ctn.methods.addAll(parent.methods);
                n.superEntry = superEntry;
            }
        }

        //creo la nuova entry per la classe
        STentry entry = new STentry(0, ctn, decOffset--);

        if (globalST.put(n.id, entry) != null) {
            System.out.println("Class id " + n.id + " at line " + n.getLine() + " already declared");
            stErrors++;
        }

        Map<String, STentry> virtualTable = new HashMap<>();
        if (n.superID != null) {
            virtualTable.putAll(classTable.get(n.superID));
        }
        // Add the class to the class table
        classTable.put(n.id, virtualTable);
        // Add the class to the symbol table
        symTable.add(virtualTable);

        // Increase nesting level
        nestingLevel++;

        // Visit fields of the class
        int fieldOffset = - ctn.fields.size() - 1;

        final Set<String> fieldNames = new HashSet<>();

        for (FieldNode field : n.fields) {
            // Add the field to the class table
            if (fieldNames.contains(field.id)) {
                System.out.println("Field id " + field.id + " at line " + n.getLine() + " already declared");
                stErrors++;
            } else {
                fieldNames.add(field.id);
            }
            visit(field);

            STentry fieldEntry = null;

            if (virtualTable.containsKey(field.id)) {
                STentry parent = virtualTable.get(field.id);
                if (parent.type instanceof MethodTypeNode) {
                    System.out.println("Field id " + field.id + " at line " + n.getLine() + " already declared as method");
                    stErrors++;
                } else {
                    fieldEntry = new STentry(nestingLevel, field.getType(), parent.offset);
                }
            } else {
                fieldEntry = new STentry(nestingLevel, field.getType(), fieldOffset--);
            }

            if(fieldEntry != null) {
                ctn.fields.add(-fieldEntry.offset - 1, fieldEntry.type);
                virtualTable.put(field.id, fieldEntry);
            }
        }

        int precOffset = decOffset;
        decOffset = ctn.methods.size();
        List<String> methodNames = new ArrayList<>();
        for (MethodNode method : n.methods) {
            if (methodNames.contains(method.id)) {
                System.out.println("Method id " + method.id + " at line " + n.getLine() + " already declared");
                stErrors++;
            } else {
                methodNames.add(method.id);
            }
            visit(method);

            final MethodTypeNode methodTypeNode = (MethodTypeNode) symTable.get(nestingLevel).get(method.id).type;
            ctn.methods.add(method.offset, methodTypeNode.fun);
        }
        decOffset = precOffset;
        symTable.remove(nestingLevel--);
        return null;
    }

    @Override
    public Void visitNode(FieldNode n) {
        if (print) printNode(n);
        return null;
    }

    @Override
    public Void visitNode(MethodNode n) {
        if (print) printNode(n);
        Map<String, STentry> virtualTable = symTable.get(nestingLevel);
        List<TypeNode> parTypes = new ArrayList<>();
        for (ParNode par : n.parlist) parTypes.add(par.getType());
        STentry entry = null;
        MethodTypeNode methodTypeNode = new MethodTypeNode(new ArrowTypeNode(parTypes, n.retType));
        if(virtualTable.containsKey(n.id)){
            if(virtualTable.get(n.id).type instanceof MethodTypeNode){
                entry = new STentry(nestingLevel, methodTypeNode, virtualTable.get(n.id).offset);;
            } else {
                System.out.println("Method id " + n.id + " at line " + n.getLine() + " already declared as field");
                stErrors++;
            }
        } else {
            entry = new STentry(nestingLevel, methodTypeNode, decOffset++);
        }
        if (entry != null) {
            n.offset = entry.offset;
            //inserimento di ID nella symtable
            virtualTable.put(n.id, entry);
        }

        //creare una nuova hashmap per la symTable
        nestingLevel++;
        Map<String, STentry> methodScopeTable = new HashMap<>();
        symTable.add(methodScopeTable);
        int prevNLDecOffset = decOffset; // stores counter for offset of declarations at previous nesting level
        decOffset = -2;
        int parOffset = 1;
        for (ParNode par : n.parlist)
            if (methodScopeTable.put(par.id, new STentry(nestingLevel, par.getType(), parOffset++)) != null) {
                System.out.println("Par id " + par.id + " at line " + n.getLine() + " already declared");
                stErrors++;
            }
        for (Node dec : n.declist) visit(dec);
        visit(n.exp);
        //rimuovere la hashmap corrente poiche' esco dallo scope
        symTable.remove(nestingLevel--);
        decOffset = prevNLDecOffset; // restores counter for offset of declarations at previous nesting level
        return null;
    }

    @Override
    public Void visitNode(ClassCallNode node) throws VoidException {
        if (print) printNode(node);
        STentry entry = stLookup(node.id);
        if (entry == null) {
            System.out.println("Var or Par id " + node.id + " at line " + node.getLine() + " not declared");
            stErrors++;
        } else if (entry.type instanceof RefTypeNode) {
            node.entry = entry;
            node.nl = nestingLevel;
            Map<String, STentry> virtualTable = classTable.get(((RefTypeNode) entry.type).id);
            if (virtualTable.containsKey(node.methodId)) {
                node.methodEntry = virtualTable.get(node.methodId);
            } else {
                System.out.println("Object id " + node.id + " at line " + node.getLine() + " has no method " + node.methodId);
                stErrors++;
            }
        } else {
            System.out.println("Object id " + node.id + " at line " + node.getLine() + " is not a class");
            stErrors++;
        }

        node.arglist.forEach(this::visit);
        return null;
    }

    @Override
    public Void visitNode(NewNode node) throws VoidException {
        if (print) printNode(node);
        if (!classTable.containsKey(node.id)) {
            System.out.println("Class id " + node.id + " at line " + node.getLine() + " not declared");
            stErrors++;
        }
        node.entry = symTable.get(0).get(node.id);
        node.arglist.forEach(this::visit);
        return null;
    }

    @Override
    public Void visitNode(EmptyNode n) {
        if (print) printNode(n);
        return null;
    }
}

