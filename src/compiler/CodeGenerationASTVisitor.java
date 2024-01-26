package compiler;

import compiler.AST.*;
import compiler.lib.*;
import compiler.exc.*;

import static compiler.lib.FOOLlib.*;

public class CodeGenerationASTVisitor extends BaseASTVisitor<String, VoidException> {

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
                "push 0",
                declCode, // generate code for declarations (allocation)
                visit(node.exp),
                "halt",
                getCode()
        );
    }

    @Override
    public String visitNode(ProgNode node) {
        if (print) printNode(node);
        return nlJoin(
                visit(node.exp),
                "halt"
        );
    }

    @Override
    public String visitNode(FunNode node) {
        if (print) printNode(node, node.id);
        String declCode = null, popDecl = null, popParl = null;
        for (Node dec : node.declarations) {
            declCode = nlJoin(declCode, visit(dec));
            popDecl = nlJoin(popDecl, "pop");
        }
        for (Node parameter : node.parameters) popParl = nlJoin(popParl, "pop");
        String funl = freshFunLabel();
        putCode(
                nlJoin(
                        funl + ":",
                        "cfp", // set $fp to $sp value
                        "lra", // load $ra value
                        declCode, // generate code for local declarations (they use the new $fp!!!)
                        visit(node.exp), // generate code for function body expression
                        "stm", // set $tm to popped value (function result)
                        popDecl, // remove local declarations from stack
                        "sra", // set $ra to popped value
                        "pop", // remove Access Link from stack
                        popParl, // remove parameters from stack
                        "sfp", // set $fp to popped value (Control Link)
                        "ltm", // load $tm value (function result)
                        "lra", // load $ra value
                        "js"  // jump to to popped address
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
                "print"
        );
    }

    @Override
    public String visitNode(IfNode node) {
        if (print) printNode(node);
        String thenLabel = freshLabel();
        String elseLabel = freshLabel();
        return nlJoin(
                visit(node.condition),
                "push 1",
                "beq " + thenLabel,
                visit(node.elseBranch),
                "b " + elseLabel,
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
                "beq " + trueLabel,
                "push 0",
                "b " + falseLabel,
                trueLabel + ":",
                "push 1",
                falseLabel + ":"
        );
    }

    @Override
    public String visitNode(TimesNode node) {
        if (print) printNode(node);
        return nlJoin(
                visit(node.left),
                visit(node.right),
                "mult"
        );
    }

    @Override
    public String visitNode(PlusNode node) {
        if (print) printNode(node);
        return nlJoin(
                visit(node.left),
                visit(node.right),
                "add"
        );
    }

    @Override
    public String visitNode(CallNode node) {
        if (print) printNode(node, node.id);
        String argCode = null, getAR = null;
        for (int i = node.arguments.size() - 1; i >= 0; i--) argCode = nlJoin(argCode, visit(node.arguments.get(i)));
        for (int i = 0; i < node.nestingLevel - node.entry.nestingLevel; i++) getAR = nlJoin(getAR, "lw");
        return nlJoin(
                "lfp", // load Control Link (pointer to frame of function "id" caller)
                argCode, // generate code for argument expressions in reversed order
                "lfp", getAR, // retrieve address of frame containing "id" declaration
                // by following the static chain (of Access Links)
                "stm", // set $tm to popped value (with the aim of duplicating top of stack)
                "ltm", // load Access Link (pointer to frame of function "id" declaration)
                "ltm", // duplicate top of stack
                "push " + node.entry.offset, "add", // compute address of "id" declaration
                "lw", // load address of "id" function
                "js"  // jump to popped address (saving address of subsequent instruction in $ra)
        );
    }

    @Override
    public String visitNode(IdNode node) {
        if (print) printNode(node, node.id);
        String getAR = null;
        for (int i = 0; i < node.nl - node.entry.nestingLevel; i++) getAR = nlJoin(getAR, "lw");
        return nlJoin(
                "lfp", getAR, // retrieve address of frame containing "id" declaration
                // by following the static chain (of Access Links)
                "push " + node.entry.offset, "add", // compute address of "id" declaration
                "lw" // load value of "id" variable
        );
    }

    @Override
    public String visitNode(BoolNode node) {
        if (print) printNode(node, node.val.toString());
        return "push " + (node.val ? 1 : 0);
    }

    @Override
    public String visitNode(IntNode node) {
        if (print) printNode(node, node.val.toString());
        return "push " + node.val;
    }

    @Override
    public String visitNode(GreaterEqualNode node) {
        if (print) printNode(node);
        final String falseLabel = freshLabel();
        final String endLabel = freshLabel();
        return nlJoin(
                visit(node.left),        // generate code for left expression
                visit(node.right),              // generate code for right expression
                "push " + 1,                       // push 1
                "sub",                            // subtract 1 from right value
                "bleq " + falseLabel, // if left value is not less or equal than right value, jump to false label
                "push " + 1,                       // push 1 (the result)
                "b " + endLabel,              // jump to end label
                falseLabel + ":",               // false label
                "push " + 0,                       // push 0 (the result)
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
                "bleq " + trueLabel,  // if left value is less or equal than right value, jump to true label
                "push " + 0,                       // push 0 (the result)
                "b " + endLabel,              // jump to end label
                trueLabel + ":",                // true label
                "push " + 1,                       // push 1 (the result)
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
                "push " + 0,                       // push 0
                "beq " + itWasFalseLabel, // if value is 0, jump to itWasFalseLabel
                "push " + 0,                       // push 0 (the result)
                "b " + endLabel,              // jump to end label
                itWasFalseLabel + ":",          // itWasFalseLabel
                "push " + 1,                       // push 1 (the result)
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
                "push " + 1,                   // push 1
                "beq " + trueLabel,   // if value is 1, jump to true label
                visit(node.right),          // generate code for right expression
                "push " + 1,                   // push 1
                "beq " + trueLabel,   // if value is 1, jump to true label
                "push " + 0,                   // push 0 (the result)
                "b " + endLabel,          // jump to end label
                trueLabel + ":",            // true label
                "push " + 1,                   // push 1 (the result)
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
                "push" + 0,                   // push 0
                "beq" + falseLabel,  // if value is 0, jump to false label
                visit(node.right),          // generate code for right expression
                "push" + 0,                   // push 0
                "beq" + falseLabel,  // if value is 0, jump to false label
                "push" + 1,                   // push 1 (the result)
                "b" + endLabel,          // jump to end label
                falseLabel + ":",           // false label
                "push" + 0,                   // push 0 (the result)
                endLabel + ":"              // end label
        );
    }

    @Override
    public String visitNode(MinusNode node) {
        if (print) printNode(node);
        return nlJoin(
                visit(node.left),    // generate code for left expression
                visit(node.right),          // generate code for right expression
                "sub"                   // subtract right value from left value
        );
    }

    @Override
    public String visitNode(DivNode node) {
        if (print) printNode(node);
        return nlJoin(
                visit(node.left),    // generate code for left expression
                visit(node.right),          // generate code for right expression
                "div"                   // divide left value by right value
        );
    }
}