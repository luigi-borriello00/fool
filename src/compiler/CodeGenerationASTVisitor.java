package compiler;

import compiler.AST.*;
import compiler.lib.*;
import compiler.exc.*;

import static compiler.lib.FOOLlib.*;

public class CodeGenerationASTVisitor extends BaseASTVisitor<String, VoidException> {

    // Stack Machine Code Instructions
    public static final String COPY_FP = "cfp";
    public static final String LOAD_RA = "lra";
    public static final String SET_TM = "stm";
    public static final String PUSH = "push ";
    public static final String HALT = "halt";
    public static final String SET_RA = "sra";
    public static final String POP = "pop";
    public static final String SET_FP = "sfp";
    public static final String LOAD_TM = "ltm";
    public static final String JS = "js";
    public static final String BRANCH_EQUAL = "beq ";
    public static final String BRANCH = "b ";
    public static final String MULTIPLY = "mult";
    public static final String ADD = "add";
    public static final String PRINT = "print";
    public static final String LOAD_FP = "lfp";
    public static final String LOAD_W = "lw";
    public static final String SUB = "sub";
    public static final String BRANCH_LESS_EQ = "bleq ";
    public static final String DIV = "div";

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
                        COPY_FP, // set $fp to $sp value
                        LOAD_RA, // load $ra value
                        declCode, // generate code for local declarations (they use the new $fp!!!)
                        visit(node.exp), // generate code for function body expression
                        SET_TM, // set $tm to popped value (function result)
                        popDecl, // remove local declarations from stack
                        SET_RA, // set $ra to popped value
                        POP, // remove Access Link from stack
                        popParl, // remove parameters from stack
                        SET_FP, // set $fp to popped value (Control Link)
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
        for (int i = 0; i < node.nestingLevel - node.entry.nl; i++) getAR = nlJoin(getAR, LOAD_W);
        return nlJoin(
                LOAD_FP, // load Control Link (pointer to frame of function "id" caller)
                argCode, // generate code for argument expressions in reversed order
                LOAD_FP, getAR, // retrieve address of frame containing "id" declaration
                // by following the static chain (of Access Links)
                SET_TM, // set $tm to popped value (with the aim of duplicating top of stack)
                LOAD_TM, // load Access Link (pointer to frame of function "id" declaration)
                LOAD_TM, // duplicate top of stack
                PUSH + node.entry.offset, ADD, // compute address of "id" declaration
                LOAD_W, // load address of "id" function
                JS  // jump to popped address (saving address of subsequent instruction in $ra)
        );
    }

    @Override
    public String visitNode(IdNode node) {
        if (print) printNode(node, node.id);
        String getAR = null;
        for (int i = 0; i < node.nl - node.entry.nl; i++) getAR = nlJoin(getAR, LOAD_W);
        return nlJoin(
                LOAD_FP, getAR, // retrieve address of frame containing "id" declaration
                // by following the static chain (of Access Links)
                PUSH + node.entry.offset, ADD, // compute address of "id" declaration
                LOAD_W // load value of "id" variable
        );
    }

    @Override
    public String visitNode(BoolNode node) {
        if (print) printNode(node, node.val.toString());
        return PUSH + (node.val ? 1 : 0);
    }

    @Override
    public String visitNode(IntNode node) {
        if (print) printNode(node, node.val.toString());
        return PUSH + node.val;
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
}