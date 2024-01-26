package compiler;

import compiler.lib.*;

public class STentry implements Visitable {
    final int nestingLevel;
    final TypeNode type;
    final int offset;

    public STentry(int nestingLevel, TypeNode typeNode, int offset) {
        this.nestingLevel = nestingLevel;
        this.type = typeNode;
        this.offset = offset;
    }

    @Override
    public <S, E extends Exception> S accept(BaseASTVisitor<S, E> visitor) throws E {
        return ((BaseEASTVisitor<S, E>) visitor).visitSTentry(this);
    }
}
