package main.ast.node.expression.BinOp;

public enum IntBinaryOperator
{
    add, sub, mult, div, assign;

    public static boolean contains(String s)
    {
        for(IntBinaryOperator op:values())
            if (op.name().equals(s)) 
                return true;
        return false;
    } 
}