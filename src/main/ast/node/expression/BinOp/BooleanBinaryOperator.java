package main.ast.node.expression.BinOp;

public enum BooleanBinaryOperator
{
    and, or, eq, lt, gt, neq;

    public static boolean contains(String s)
    {
        for(BooleanBinaryOperator op:values())
            if (op.name().equals(s)) 
                return true;
        return false;
    } 
}