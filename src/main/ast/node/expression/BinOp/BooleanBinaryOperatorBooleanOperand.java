package main.ast.node.expression.BinOp;

public enum BooleanBinaryOperatorBooleanOperand
{
    and, or;

    public static boolean contains(String s)
    {
        for(BooleanBinaryOperatorBooleanOperand op:values())
            if (op.name().equals(s)) 
                return true;
        return false;
    } 
}