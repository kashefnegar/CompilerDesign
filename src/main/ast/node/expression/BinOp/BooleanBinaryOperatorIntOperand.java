package main.ast.node.expression.BinOp;

public enum BooleanBinaryOperatorIntOperand
{
    lt, gt;

    public static boolean contains(String s)
    {
        for(BooleanBinaryOperatorIntOperand op:values())
            if (op.name().equals(s)) 
                return true;
        return false;
    } 
}