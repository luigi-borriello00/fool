package compiler;

import compiler.AST.*;
import compiler.lib.*;
import compiler.exc.*;
import svm.ExecuteVM;

import java.util.ArrayList;
import java.util.List;

import static compiler.lib.FOOLlib.*;

public class CodeGenerationASTVisitor extends BaseASTVisitor<String, VoidException> {

    // Define constants for the code generation
    public static final String PUSH = "push ";
    public static final String POP = "pop";
    public static final String HALT = "halt";
    public static final String BRANCH_EQUAL = "beq ";
    public static final String BRANCH = "b ";
    public static final String BRANCH_LESS_EQ = "bleq ";
    public static final String MULTIPLY = "mult";
    public static final String DIV = "div";
    public static final String ADD = "add";
    public static final String SUB = "sub";
    public static final String PRINT = "print";
    public static final String JS = "js";
    /**
     * Push the value of RA on the stack.
     */
    public static final String LOAD_RA = "lra";
    /**
     * Store the value on the top of the stack in RA.
     */
    public static final String STORE_TM = "stm";
    /**
     * Load the value of TM on the stack.
     */
    public static final String LOAD_TM = "ltm";

    /**
     * Copy the value of SP in FP.
     */
    public static final String COPY_FRAME_POINTER = "cfp";
    /**
     * Push the value of FP on the stack.
     */
    public static final String LOAD_FRAME_POINTER = "lfp";
    /**
     * Pop the value on the top of the stack and store it in FP.
     */
    public static final String STORE_FRAME_POINTER = "sfp";
    /**
     * Push the value on the top of the stack and store it in HP.
     */
    public static final String LOAD_HEAP_POINTER = "lhp";
    /**
     * Pop the value on the top of the stack and store it in HP.
     */
    public static final String STORE_HEAP_POINTER = "shp";
    /**
     * Pop the value on the top of the stack and store it in RA.
     */
    public static final String STORE_RETURN_ADDRESS = "sra";
    /**
     * Pop the address on the top of the stack and
     * pop the value to store at that address.
     */
    public static final String STORE_WORD = "sw";
    /**
     * Pop the address on the top of the stack and
     * push the value stored at that address.
     */
    public static final String LOAD_WORD = "lw";


    /**
     * The dispatch tables of the classes.
     * <p>
     * Each dispatch table is a list of labels, one for each method of the class.
     */
    private final List<List<String>> dispatchTables = new ArrayList<>();

    CodeGenerationASTVisitor() {
    }

    CodeGenerationASTVisitor(boolean debug) {
        super(false, debug);
    } //enables print for debugging

    @Override
    public String visitNode(ProgLetInNode node) {
        if (print) printNode(node);
        String declCode = null;
        for (Node dec : node.declarations) declCode = nlJoin(declCode, visit(dec));
        return nlJoin(
                PUSH + 0,
                declCode, // generate code for declarations (allocation)
                visit(node.exp),
                HALT,
                getCode()
        );
    }

    @Override
    public String visitNode(ProgNode node) {
        if (print) printNode(node);
        return nlJoin(
                visit(node.exp),
                HALT
        );
    }

    @Override
    public String visitNode(FunNode node) {
        if (print) printNode(node, node.id);
        String declCode = null, popDecl = null, popParl = null;
        for (Node dec : node.declarations) {
            declCode = nlJoin(declCode, visit(dec));
            popDecl = nlJoin(popDecl, POP);
        }
        for (Node parameter : node.parameters) popParl = nlJoin(popParl, POP);
        String funl = freshFunLabel();
        putCode(
                nlJoin(
                        funl + ":",
                        COPY_FRAME_POINTER, // set $fp to $sp value
                        LOAD_RA, // load $ra value
                        declCode, // generate code for local declarations (they use the new $fp!!!)
                        visit(node.exp), // generate code for function body expression
                        STORE_TM, // set $tm to popped value (function result)
                        popDecl, // remove local declarations from stack
                        STORE_RETURN_ADDRESS, // set $ra to popped value
                        POP, // remove Access Link from stack
                        popParl, // remove parameters from stack
                        STORE_FRAME_POINTER, // set $fp to popped value (Control Link)
                        LOAD_TM, // load $tm value (function result)
                        LOAD_RA, // load $ra value
                        JS  // jump to popped address
                )
        );
        return "push " + funl;
    }

    @Override
    public String visitNode(VarNode node) {
        if (print) printNode(node, node.id);
        return visit(node.exp);
    }

    @Override
    public String visitNode(PrintNode node) {
        if (print) printNode(node);
        return nlJoin(
                visit(node.exp),
                PRINT
        );
    }

    @Override
    public String visitNode(IfNode node) {
        if (print) printNode(node);
        String thenLabel = freshLabel();
        String elseLabel = freshLabel();
        return nlJoin(
                visit(node.condition),
                PUSH + 1,
                BRANCH_EQUAL + thenLabel,
                visit(node.elseBranch),
                BRANCH + elseLabel,
                thenLabel + ":",
                visit(node.thenBranch),
                elseLabel + ":"
        );
    }

    @Override
    public String visitNode(EqualNode node) {
        if (print) printNode(node);
        String trueLabel = freshLabel();
        String falseLabel = freshLabel();
        return nlJoin(
                visit(node.left),
                visit(node.right),
                BRANCH_EQUAL + trueLabel,
                PUSH + 0,
                BRANCH + falseLabel,
                trueLabel + ":",
                PUSH + 1,
                falseLabel + ":"
        );
    }

    @Override
    public String visitNode(TimesNode node) {
        if (print) printNode(node);
        return nlJoin(
                visit(node.left),
                visit(node.right),
                MULTIPLY
        );
    }

    @Override
    public String visitNode(PlusNode node) {
        if (print) printNode(node);
        return nlJoin(
                visit(node.left),
                visit(node.right),
                ADD
        );
    }

    @Override
    public String visitNode(CallNode node) {
        if (print) printNode(node, node.id);
        String argCode = null, getAR = null;
        for (int i = node.arguments.size() - 1; i >= 0; i--) argCode = nlJoin(argCode, visit(node.arguments.get(i)));
        for (int i = 0; i < node.nestingLevel - node.entry.nl; i++) getAR = nlJoin(getAR, LOAD_WORD);
        return nlJoin(
                LOAD_FRAME_POINTER, // load Control Link (pointer to frame of function "id" caller)
                argCode, // generate code for argument expressions in reversed order
                LOAD_FRAME_POINTER, getAR, // retrieve address of frame containing "id" declaration
                // by following the static chain (of Access Links)
                STORE_TM, // set $tm to popped value (with the aim of duplicating top of stack)
                LOAD_TM, // load Access Link (pointer to frame of function "id" declaration)
                LOAD_TM, // duplicate top of stack
                // load address of dispatch table if method
                (node.entry.type instanceof MethodTypeNode) ? LOAD_WORD : "",
                PUSH + node.entry.offset, ADD, // compute address of "id" declaration
                LOAD_WORD, // load address of "id" function
                JS  // jump to popped address (saving address of subsequent instruction in $ra)
        );
    }

    @Override
    public String visitNode(IdNode node) {
        if (print) printNode(node, node.id);
        String getAR = null;
        for (int i = 0; i < node.nestingLevel - node.entry.nl; i++) getAR = nlJoin(getAR, LOAD_WORD);
        return nlJoin(
                LOAD_FRAME_POINTER, getAR, // retrieve address of frame containing "id" declaration
                // by following the static chain (of Access Links)
                PUSH + node.entry.offset, ADD, // compute address of "id" declaration
                LOAD_WORD // load value of "id" variable
        );
    }

    @Override
    public String visitNode(BoolNode node) {
        if (print) printNode(node, node.value.toString());
        return PUSH + (node.value ? 1 : 0);
    }

    @Override
    public String visitNode(IntNode node) {
        if (print) printNode(node, node.value.toString());
        return PUSH + node.value;
    }

    // OPERATOR EXTENSIONS

    @Override
    public String visitNode(GreaterEqualNode node) {
        if (print) printNode(node);
        final String falseLabel = freshLabel();
        final String endLabel = freshLabel();
        return nlJoin(
                visit(node.left),        // generate code for left expression
                visit(node.right),              // generate code for right expression
                PUSH + 1,                       // push 1
                SUB,                            // subtract 1 from right value
                BRANCH_LESS_EQ + falseLabel, // if left value is not less or equal than right value, jump to false label
                PUSH + 1,                       // push 1 (the result)
                BRANCH + endLabel,              // jump to end label
                falseLabel + ":",               // false label
                PUSH + 0,                       // push 0 (the result)
                endLabel + ":"                  // end label
        );
    }

    @Override
    public String visitNode(LessEqualNode node) {
        if (print) printNode(node);
        final String trueLabel = freshLabel();
        final String endLabel = freshLabel();
        return nlJoin(
                visit(node.left),        // generate code for left expression
                visit(node.right),              // generate code for right expression
                BRANCH_LESS_EQ + trueLabel,  // if left value is less or equal than right value, jump to true label
                PUSH + 0,                       // push 0 (the result)
                BRANCH + endLabel,              // jump to end label
                trueLabel + ":",                // true label
                PUSH + 1,                       // push 1 (the result)
                endLabel + ":"                  // end label
        );
    }

    @Override
    public String visitNode(NotNode node) {
        if (print) printNode(node);
        final String itWasFalseLabel = freshLabel();
        final String endLabel = freshLabel();
        return nlJoin(
                visit(node.exp),          // generate code for expression
                PUSH + 0,                       // push 0
                BRANCH_EQUAL + itWasFalseLabel, // if value is 0, jump to itWasFalseLabel
                PUSH + 0,                       // push 0 (the result)
                BRANCH + endLabel,              // jump to end label
                itWasFalseLabel + ":",          // itWasFalseLabel
                PUSH + 1,                       // push 1 (the result)
                endLabel + ":"                  // end label
        );
    }

    @Override
    public String visitNode(OrNode node) {
        if (print) printNode(node);
        final String trueLabel = freshLabel();
        final String endLabel = freshLabel();
        return nlJoin(
                visit(node.left),    // generate code for left expression
                PUSH + 1,                   // push 1
                BRANCH_EQUAL + trueLabel,   // if value is 1, jump to true label
                visit(node.right),          // generate code for right expression
                PUSH + 1,                   // push 1
                BRANCH_EQUAL + trueLabel,   // if value is 1, jump to true label
                PUSH + 0,                   // push 0 (the result)
                BRANCH + endLabel,          // jump to end label
                trueLabel + ":",            // true label
                PUSH + 1,                   // push 1 (the result)
                endLabel + ":"              // end label
        );
    }

    @Override
    public String visitNode(AndNode node) {
        if (print) printNode(node);
        final String falseLabel = freshLabel();
        final String endLabel = freshLabel();
        return nlJoin(
                visit(node.left),    // generate code for left expression
                PUSH + 0,                   // push 0
                BRANCH_EQUAL + falseLabel,  // if value is 0, jump to false label
                visit(node.right),          // generate code for right expression
                PUSH + 0,                   // push 0
                BRANCH_EQUAL + falseLabel,  // if value is 0, jump to false label
                PUSH + 1,                   // push 1 (the result)
                BRANCH + endLabel,          // jump to end label
                falseLabel + ":",           // false label
                PUSH + 0,                   // push 0 (the result)
                endLabel + ":"              // end label
        );
    }

    @Override
    public String visitNode(MinusNode node) {
        if (print) printNode(node);
        return nlJoin(
                visit(node.left),    // generate code for left expression
                visit(node.right),          // generate code for right expression
                SUB                   // subtract right value from left value
        );
    }

    @Override
    public String visitNode(DivNode node) {
        if (print) printNode(node);
        return nlJoin(
                visit(node.left),    // generate code for left expression
                visit(node.right),          // generate code for right expression
                DIV                   // divide left value by right value
        );
    }

    /**
     * Generate code for the ClassNode node.
     *
     * @param node the ClassNode node
     * @return the code generated for the ClassNode node
     */
    @Override
    public String visitNode(final ClassNode node) {
        if (print) printNode(node);
        // Build the in-memory dispatch table
        final List<String> dispatchTable = new ArrayList<>();
        dispatchTables.add(dispatchTable);
        final boolean isSubclass = node.superEntry != null;
        // Add the dispatch table of the superclass
        if (isSubclass) {
            // Get the dispatch table of the superclass
            final List<String> superDispatchTable = dispatchTables.get(-node.superEntry.offset - 2);
            dispatchTable.addAll(superDispatchTable);
        }
        // Add a label for each method of the class
        for (final MethodNode methodEntry : node.allMethods) {
            visit(methodEntry);
            final boolean isOverriding = methodEntry.offset < dispatchTable.size();
            // Update the dispatch table
            if (isOverriding) {
                dispatchTable.set(methodEntry.offset, methodEntry.label);
            } else {
                dispatchTable.add(methodEntry.label);
            }
        }
        // Add the dispatch table to the heap
        String dispatchTableHeapCode = "";
        for (final String label : dispatchTable) {
            // Store method label in heap, increment heap pointer, store heap pointer
            dispatchTableHeapCode = nlJoin(
                    dispatchTableHeapCode,
                    // Store method label in heap
                    PUSH + label,       // push method label
                    LOAD_HEAP_POINTER,  // push heap pointer
                    STORE_WORD,         // store method label in heap
                    // Increment heap pointer
                    LOAD_HEAP_POINTER,  // push heap pointer
                    PUSH + 1,           // push 1
                    ADD,                // heap pointer + 1
                    STORE_HEAP_POINTER            // store heap pointer
            );
        }

        return nlJoin(
                LOAD_HEAP_POINTER,      // push heap pointer, the address of the dispatch table
                dispatchTableHeapCode   // generated code for creating the dispatch table in the heap
        );

    }

    /**
     * Generate code for the EmptyNode node.
     *
     * @param node the EmptyNode node
     * @return the code generated for the EmptyNode node
     */
    @Override
    public String visitNode(final EmptyNode node) {
        if (print) printNode(node);
        return PUSH + "-1";
    }

    /**
     * Generate code for the MethodNode node.
     *
     * @param node the MethodNode node
     * @return the code generated for the MethodNode node
     */
    @Override
    public String visitNode(final MethodNode node) {
        if (print) printNode(node);
        String declarationsCode = "";
        for (final DecNode declaration : node.declarations) {
            declarationsCode = nlJoin(
                    declarationsCode,
                    visit(declaration)
            );
        }
        String popDeclarationsCode = "";
        for (final DecNode declaration : node.declarations) {
            popDeclarationsCode = nlJoin(
                    popDeclarationsCode,
                    POP
            );
        }
        String popParametersCode = "";
        for (final ParNode parameter : node.parameters) {
            popParametersCode = nlJoin(
                    popParametersCode,
                    POP
            );
        }
        final String methodLabel = freshFunLabel();
        node.label = methodLabel; // set the label of the method
        // Generate code for the method body
        putCode(
                nlJoin(
                        methodLabel + ":",   // method label
                        // Set up the stack frame with FP, RA, and declarations
                        COPY_FRAME_POINTER,                    // copy $sp to $fp, the new frame pointer
                        LOAD_RA,                    // push return address
                        declarationsCode,           // generate code for declarations
                        // Generate code for the body and store the result in $tm
                        visit(node.exp),            // generate code for the expression
                        STORE_TM,                   // set $tm to popped value (function result)
                        // Frame cleanup
                        popDeclarationsCode,        // pop declarations
                        STORE_RETURN_ADDRESS,
                        // pop return address to $ra (for return)
                        POP,                        // pop $fp
                        popParametersCode,          // pop parameters
                        STORE_FRAME_POINTER,                   // pop $fp (restore old frame pointer)
                        // Return
                        LOAD_TM,                    // push function result
                        LOAD_RA,                    // push return address
                        JS                  // jump to return address
                )
        );

        return null;
    }

    /**
     * Generate code for the ClassCallNode node.
     *
     * @param node the ClassCallNode node
     * @return the code generated for the ClassCallNode node
     */
    @Override
    public String visitNode(final ClassCallNode node) {
        if (print) printNode(node);
        String argumentsCode = "";
        for (int i = node.arguments.size() - 1; i >= 0; i--) {
            argumentsCode = nlJoin(
                    argumentsCode,
                    visit(node.arguments.get(i))
            );
        }
        String getARCode = "";
        for (int i = 0; i < node.nestingLevel - node.entry.nl; i++) {
            getARCode = nlJoin(
                    getARCode,
                    LOAD_WORD
            );
        }
        return nlJoin(
                LOAD_FRAME_POINTER,     // push $fp on the stack
                argumentsCode,      // generate arguments
                // Get the address of the object
                LOAD_FRAME_POINTER, getARCode,         // get AR
                PUSH + node.entry.offset,   // push class offset on the stack
                ADD,                        // add class offset to $ar
                // Go to the object address in the heap
                LOAD_WORD,                  // load object address
                // Duplicate class address to set the access link
                STORE_TM,     // set $tm to popped value (class address)
                LOAD_TM,      // push class address on the stack
                LOAD_TM,      // duplicate class address
                // Get the address of the method
                LOAD_WORD,    // load dispatch table address
                PUSH + node.methodEntry.offset, // push method offset on the stack
                ADD,          // add method offset to dispatch table address
                LOAD_WORD,    // load method address
                // Call the method
                JS // jump to method address which is on the top of the stack
        );

    }

    /**
     * Generate code for the NewNode node.
     *
     * @param node the NewNode node
     * @return the code generated for the NewNode node
     */
    @Override
    public String visitNode(final NewNode node) {
        if (print) printNode(node);
        // Generate code for the arguments
        String argumentsCode = "";
        for (final Node argument : node.arguments) {
            argumentsCode = nlJoin(
                    argumentsCode,
                    visit(argument)
            );
        }
        // We have the n° arguments on the stack
        String moveArgsToHeap = "";
        for (final Node argument : node.arguments) {
            moveArgsToHeap = nlJoin(
                    moveArgsToHeap,
                    // Store argument on the heap
                    LOAD_HEAP_POINTER,    // push $hp on the stack
                    STORE_WORD,           // store argument on the heap
                    // Update $hp = $hp + 1
                    LOAD_HEAP_POINTER,    // push $hp on the stack
                    PUSH + 1,             // push 1 on the stack
                    ADD,                  // add 1 to $hp
                    STORE_HEAP_POINTER             // store $hp
            );
        }

        return nlJoin(

                // Set up arguments on the stack and move them on the heap
                argumentsCode,      // generate arguments
                moveArgsToHeap,  // move arguments on the heap
                // Load the address of the dispatch table in the heap
                PUSH + (ExecuteVM.MEMSIZE + node.entry.offset), // push class address on the stack
                LOAD_WORD,          // load dispatch table address
                LOAD_HEAP_POINTER,  // push $hp on the stack
                STORE_WORD,         // store dispatch table address on the heap
                // Put the result on the stack (object address)
                LOAD_HEAP_POINTER,  // push $hp on the stack (object address)

                // Update $hp = $hp + 1
                LOAD_HEAP_POINTER,  // push $hp on the stack
                PUSH + 1,           // push 1 on the stack
                ADD,                // add 1 to $hp
                STORE_HEAP_POINTER            // store $hp
        );

    }
}