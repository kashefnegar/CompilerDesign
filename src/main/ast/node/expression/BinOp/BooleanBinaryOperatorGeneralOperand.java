package main.ast.node.expression.BinOp;

public enum BooleanBinaryOperatorGeneralOperand
{
    eq, neq;

    public static boolean contains(String s)
    {
        for(BooleanBinaryOperatorGeneralOperand op:values())
            if (op.name().equals(s)) 
                return true;
        return false;
    } 
}
