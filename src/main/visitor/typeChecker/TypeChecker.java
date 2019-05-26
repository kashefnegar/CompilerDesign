package main.visitor.typeChecker;

import main.ast.node.*;
import main.ast.node.declaration.*;
import main.ast.node.expression.*;
import main.ast.node.expression.Value.*;
import main.ast.node.statement.*;
import main.ast.node.expression.BinOp.*;

import main.symbolTable.*;
import main.symbolTable.symbolTableVariable.*;
import main.symbolTable.itemException.*;

import java.util.ArrayList;

import javax.lang.model.util.ElementScanner6;

import main.ast.Type.*;
import main.ast.Type.PrimitiveType.*;
import main.ast.Type.ArrayType.*;
import main.ast.Type.UserDefinedType.UserDefinedType;


import main.visitor.VisitorImpl;


public class TypeChecker extends VisitorImpl {

    private ArrayList<String> typeErrors;

    public TypeChecker()
    {
        typeErrors = new ArrayList<String>();
    }

    public int numOfErrors()
    {
        return typeErrors.size();
    }

    @Override
    public void visit(Node node) {
    }

    @Override
    public void visit(Program program){
        if( program == null )
            return;
        
        SymbolTable.push(SymbolTable.root);

        this.visit(program.getMainClass());
        for (ClassDeclaration classDeclaration : program.getClasses())
            this.visit(classDeclaration);
        
        SymbolTable.pop();

        if(numOfErrors() != 0)
        {
            for(String e : typeErrors)
            {
                System.out.println(e);
            }
        }
    }

    @Override
    public void visit(ClassDeclaration classDeclaration) {
        if( classDeclaration == null )
            return;
        
        try
        {
            ClassSymbolTableItem classSymItem = (ClassSymbolTableItem)SymbolTable.top.get(ClassSymbolTableItem.CLASS + classDeclaration.getName().getName());
            SymbolTable.push(classSymItem.getClassSym());
            //check if the parent exists
            for( VarDeclaration varDeclaration: classDeclaration.getVarDeclarations() )
                this.visit( varDeclaration );  
            for( MethodDeclaration methodDeclaration: classDeclaration.getMethodDeclarations() )
                this.visit( methodDeclaration ); 
            
            SymbolTable.pop();
        }
        catch(ItemNotFoundException infe)
        {
            System.out.println("Error in typechecker classDeclaration visit class symbol table not found");
        }
    }

    @Override
    public void visit(MethodDeclaration methodDeclaration) {
        if( methodDeclaration == null )
            return;
        
        try
        {
            SymbolTableMethodItem methodItem = (SymbolTableMethodItem) SymbolTable.top.get(SymbolTableMethodItem.METHOD + methodDeclaration.getName().getName());
            SymbolTable.push(methodItem.getMethodSymbolTable());

            for( VarDeclaration argDeclaration: methodDeclaration.getArgs() )
                visit( argDeclaration );
            
            for( VarDeclaration localVariable: methodDeclaration.getLocalVars() )
                this.visit( localVariable );
            for( Statement statement : methodDeclaration.getBody() )
                visitStatement( statement );
            
            visitExpr( methodDeclaration.getReturnValue() );

            Type retType = methodDeclaration.getReturnValue().getType();
            Type actRetType = methodDeclaration.getActualReturnType();
            //sub type
            if(!(retType.getClass().equals(actRetType.getClass())) && !(retType instanceof NoType))
            {
                typeErrors.add("Line:" + methodDeclaration.getReturnValue().getLineNum() + ":" + methodDeclaration.getName().getName() + " return type must be " + actRetType.toString());
            }
            SymbolTable.pop();
        }
        catch(ItemNotFoundException infe)
        {
            System.out.println("Error in typechecker visit methoddeclaration method item not found");
        }
    }

    @Override
    public void visit(MainMethodDeclaration methodDeclaration)
    {
        if( methodDeclaration == null )
        return;
    
        try
        {
            SymbolTableMethodItem methodItem = (SymbolTableMethodItem) SymbolTable.top.get(SymbolTableMethodItem.METHOD + methodDeclaration.getName().getName());
            SymbolTable.push(methodItem.getMethodSymbolTable());

            for( Statement statement : methodDeclaration.getBody() )
                visitStatement( statement );
            
            visitExpr( methodDeclaration.getReturnValue() );

            Type retType = methodDeclaration.getReturnValue().getType();
            Type actRetType = methodDeclaration.getActualReturnType();
            //sub type
            if(!(retType.getClass().equals(actRetType.getClass())) && !(retType instanceof NoType))
            {
                typeErrors.add("Line:" + methodDeclaration.getReturnValue().getLineNum() + ":" + methodDeclaration.getName().getName() + " return type must be " + actRetType.toString());
            }
            SymbolTable.pop();
        }
        catch(ItemNotFoundException infe)
        {
            System.out.println("Error in typechecker visit methoddeclaration method item not found");
        }
    }
    private void checkVarType(VarDeclaration vd)
    {
        Type vType = vd.getType();
        if(vType instanceof UserDefinedType)
        {
            try
            {
                ClassSymbolTableItem classItem = (ClassSymbolTableItem)SymbolTable.root.get(ClassSymbolTableItem.CLASS + ((UserDefinedType)vType).getName().getName());
                ((UserDefinedType)vType).setClassDeclaration(classItem.getClassDeclaration());
            }
            catch(ItemNotFoundException infe)
            {
                typeErrors.add("Line:" + vd.getIdentifier().getLineNum() + ":" + ((UserDefinedType)vType).getName().getName() + " type does not exist");
                vd.setType(new NoType());
            }
        }
    }
    
    @Override
    public void visit(VarDeclaration varDeclaration) {
        if( varDeclaration == null )
            return;

        checkVarType(varDeclaration);
        visitExpr( varDeclaration.getIdentifier() );
    }

    @Override
    public void visit(ArrayCall arrayCall) {
        if( arrayCall == null )
            return;

        
        visitExpr( arrayCall.getInstance() );
        arrayCall.setLineNum(arrayCall.getInstance().getLineNum());
        Type instType = arrayCall.getInstance().getType();
        if(!(instType instanceof ArrayType)
        && !(instType instanceof NoType))
        {
            typeErrors.add("Line:" + arrayCall.getLineNum() + ":the instance is not an array");
        }
        //anyway it is an int
        arrayCall.setType(new IntType());
        visitExpr( arrayCall.getIndex() );
    }

    private void checkBooleanBinaryOperandType(BinaryExpression binExpr, Type lType, Type rType, Type operandType)
    {
        Boolean checked = false;

        if(operandType instanceof BooleanType
        && (lType instanceof BooleanType || lType instanceof NoType)
        && (rType instanceof BooleanType || rType instanceof NoType)
        )
        {
            checked = true;
        }
        else if(operandType instanceof IntType
        && (lType instanceof IntType || lType instanceof NoType)
        && (rType instanceof IntType || rType instanceof NoType)
        )
        {
            checked = true;
        }
        else if( operandType instanceof NoType
        && (lType.toString().equals(rType.toString())
            || lType instanceof NoType
            || rType instanceof NoType
           )
        )
        {
            checked = true;
        }

        if(checked)
        {
            binExpr.setType(new BooleanType());
        }
        else 
        {
            binExpr.setType(new NoType());
            typeErrors.add(
                    "Line:" + binExpr.getLineNum() + ":unsupported operand type for " + binExpr.getBinaryOperator().toString());
        }
    }

    private void checkIntBinaryOperandType(BinaryExpression binExpr, Type lType, Type rType, Type operandType)
    {
        if((lType instanceof IntType || lType instanceof NoType)
        && (rType instanceof IntType || rType instanceof NoType)
        )
        {
            binExpr.setType(new IntType());
            return;
        }

        binExpr.setType(new NoType());
        typeErrors.add("Line:" + binExpr.getLineNum() + ":unsupported operand type for " + binExpr.getBinaryOperator().toString());
    }

    private void setBinaryExpressionType(BinaryExpression binaryExpression)
    {
        Type lType = binaryExpression.getLeft().getType();
        Type rType = binaryExpression.getRight().getType();
        BinaryOperator operator = binaryExpression.getBinaryOperator();
        
        if(BooleanBinaryOperator.contains(operator.toString()))
        {
            if(BooleanBinaryOperatorBooleanOperand.contains(operator.toString()))
            {
                checkBooleanBinaryOperandType(binaryExpression, lType, rType, new BooleanType());
            }
            else if(BooleanBinaryOperatorIntOperand.contains(operator.toString()))
            {
                checkBooleanBinaryOperandType(binaryExpression, lType, rType, new IntType());
            }
            else if(BooleanBinaryOperatorGeneralOperand.contains(operator.toString()))
            {
                checkBooleanBinaryOperandType(binaryExpression, lType, rType, new NoType());
            }
        }
        else if(IntBinaryOperator.contains(operator.toString()))
        {
            checkIntBinaryOperandType(binaryExpression, lType, rType, new IntType());
        }
        else if(operator.toString().equals(BinaryOperator.assign.toString()))
        {
            if(!lType.toString().equals(rType.toString())
            && !(lType instanceof NoType)
            && !(rType instanceof NoType)
            )
            {
                typeErrors.add("Line:" + binaryExpression.getLineNum() + ":unsupported operand type for " + binaryExpression.getBinaryOperator().toString());
                binaryExpression.setType(new NoType());
            }
            else
            {
                binaryExpression.setType(lType);
            }
        }
    }

    @Override
    public void visit(BinaryExpression binaryExpression) {
        if( binaryExpression == null )
            return;
        Expression lOperand = binaryExpression.getLeft();
        Expression rOperand = binaryExpression.getRight();

        visitExpr(lOperand);
        visitExpr(rOperand);

        setBinaryExpressionType(binaryExpression);
    }

    @Override
    public void visit(Identifier identifier) {
        if( identifier == null )
            return;
        
        try
        {
            SymbolTableVariableItemBase varItem = 
            (SymbolTableVariableItemBase)SymbolTable.top.get(SymbolTableVariableItemBase.VARIABLE + identifier.getName());
            identifier.setType(varItem.getType());
        }
        catch(ItemNotFoundException infeVar)
        {
            identifier.setType(new NoType());
            typeErrors.add(
                "Line:" + identifier.getLineNum() + ":variable " + identifier.getName() + " is not declared");
            try
            {
                SymbolTable.top.put(
                    new SymbolTableLocalVariableItem(
                        identifier.getName(), identifier.getType(), SymbolTable.top.getItemCount()));
            }
            catch(ItemAlreadyExistsException iaee)
            {
                System.out.println("Error in putting undeclared variable in symbol table");
            }
        }
    }

    private boolean lengthExpressionCheck(Expression expression){
        if(expression.getType() instanceof ArrayType
        || expression.getType() instanceof NoType){
            return true;
        }
        else{
            return false;
        }
    }
    private void lengthExpressionCheckType(Expression expression)
    {
        boolean check;
        check = lengthExpressionCheck(expression);
        if(!check){
            typeErrors.add("Line:" + expression.getLineNum() + ":length is not defined for " + expression.getType());
        }
    }

    @Override
    public void visit(Length length) {
        if( length == null )
            return;
        visitExpr( length.getExpression() );
        lengthExpressionCheckType(length.getExpression());
        length.setType(new IntType());
    }

    @Override
    public void visit(MethodCall methodCall) {
        if( methodCall == null )
            return;
        Expression inst = methodCall.getInstance();
        visitExpr(inst);
        methodCall.setLineNum(methodCall.getMethodName().getLineNum());
        MethodDeclaration methDec;
        
        if(inst.getType() instanceof UserDefinedType)
        {
            
            try
            {
                ClassSymbolTableItem classItem = (ClassSymbolTableItem)SymbolTable.root.get(ClassSymbolTableItem.CLASS + ((UserDefinedType)inst.getType()).getName().getName());
                SymbolTable classSym = classItem.getClassSym();
                
                try
                {
                    SymbolTableMethodItem methItem = (SymbolTableMethodItem)classSym.get(SymbolTableMethodItem.METHOD + methodCall.getMethodName().getName());
                    methDec = methItem.getMethodDeclaration();
                    for (Expression argument : methodCall.getArgs())
                        visitExpr(argument);
                    
                    ArrayList<VarDeclaration> methArgDec = methDec.getArgs();
                    ArrayList<Expression> args = methodCall.getArgs();
                    if(methArgDec.size() < args.size())
                    {
                        typeErrors.add("Line:" + methodCall.getLineNum() + ":too many arguments for method " + methodCall.getMethodName().getName());
                        methodCall.setType(new NoType());
                    }
                    else if(methArgDec.size() > args.size())
                    {
                        typeErrors.add("Line:" + methodCall.getLineNum() + ":too few arguments for method " + methodCall.getMethodName().getName());
                        methodCall.setType(new NoType());
                    }
                    else
                    {
                        Boolean checked = true;
                        for(int i = 0; i < methArgDec.size(); i++)
                        {
                            //subtype

                            if(!methArgDec.get(i).getType().toString().equals(args.get(i).getType().toString())
                            && !(args.get(i).getType() instanceof NoType))
                            {
                                checked = false;
                                typeErrors.add("Line:" + methodCall.getMethodName().getLineNum() + ":expression " + i + " must be of type " + methArgDec.get(i).getType().toString());
                            }
                        }
                        if(checked){
                            methodCall.setType(methDec.getActualReturnType());
                        }
                        else
                        {
                            methodCall.setType(new NoType());
                        }
                    }
                }
                catch(ItemNotFoundException infe)
                {
                    typeErrors.add("Line:" + methodCall.getMethodName().getLineNum() + ":there is no method named " 
                    + methodCall.getMethodName().getName() + " in class "
                    + ((UserDefinedType)inst.getType()).getName().getName());
                    methodCall.setType(new NoType());
                }
            }
            catch(ItemNotFoundException infe)
            {
                System.out.println("Error while getting class symbol table in method call");
            }
        }
    }

    @Override
    public void visit(NewArray newArray) {
        if( newArray == null )
            return;
        newArray.setType(new ArrayType());
        visitExpr( newArray.getExpression() );
    }

    @Override
    public void visit(NewClass newClass) {
        if( newClass == null )
            return;
        
        newClass.setLineNum(newClass.getClassName().getLineNum());
        try
        {
            ClassSymbolTableItem classItem = 
            (ClassSymbolTableItem)SymbolTable.top.get(ClassSymbolTableItem.CLASS + newClass.getClassName().getName());
        
            UserDefinedType type = new UserDefinedType();
            type.setName(classItem.getClassDeclaration().getName());
            type.setClassDeclaration(classItem.getClassDeclaration());
            newClass.setType(type);
        }
        catch(ItemNotFoundException infeClass)
        {
            newClass.setType(new NoType());
            typeErrors.add("Line:" + newClass.getLineNum() + ":class " + newClass.getClassName().getName() + " is not declared");
        }
    }

    @Override
    public void visit(This instance) {
        if( instance == null )
            return;
        
        try
        {
            SymbolTableFieldVariableItem thisItem = (SymbolTableFieldVariableItem)SymbolTable.top.get(SymbolTableVariableItemBase.VARIABLE + "this");
            instance.setType(thisItem.getType());
        }
        catch(ItemNotFoundException infe)
        {
            System.out.println("Error in finding This in symbolTable");
        }
    }

    private void setUnaryExpressionType(UnaryExpression ue)
    {
        Type vType = ue.getValue().getType();
        if(ue.getUnaryOperator() == UnaryOperator.minus)
        {
            if(vType instanceof IntType || vType instanceof NoType)
            {
                ue.setType(new IntType());
            }
            else
            {
                ue.setType(new NoType());
                typeErrors.add("Line:" + ue.getLineNum() + ":unsupported operand type for " + ue.getUnaryOperator().toString());
            }
        }
        else if(ue.getUnaryOperator() == UnaryOperator.not)
        {
            if(vType instanceof BooleanType || vType instanceof NoType)
            {
                ue.setType(new BooleanType());
            }
            else
            {
                ue.setType(new NoType());
                typeErrors.add("Line:" + ue.getLineNum() + ":unsupported operand type for " + ue.getUnaryOperator().toString());
            }
        }
    }

    @Override
    public void visit(UnaryExpression unaryExpression) {
        if( unaryExpression == null )
            return;
        try {
            visitExpr(unaryExpression.getValue());
            setUnaryExpressionType(unaryExpression);
        }
        catch( NullPointerException npe )
        {
            System.out.println( "unary value is null" );
        }
    }

    @Override
    public void visit(BooleanValue value) {
        //TODO: implement appropriate visit functionality
        if( value == null )
            return;
        value.setType(new BooleanType());
    }

    @Override
    public void visit(IntValue value) {
        if( value == null )
            return;
        value.setType(new IntType());
    }

    @Override
    public void visit(StringValue value) {
        if( value == null )
            return;
        value.setType(new StringType());
    }

    @Override
    public void visit(Assign assign) {
        if( assign == null )
            return;
        
        Expression lExpr = assign.getlValue();
        visitExpr(lExpr);
        assign.setLineNum(lExpr.getLineNum());
        Expression rValExpr = assign.getrValue();
        if (rValExpr != null){
            visitExpr(rValExpr);
            if(!(lExpr instanceof Identifier))
            {
                typeErrors.add("Line:" + assign.getLineNum() + ":left side of assignment must be a valid lvalue");
            }
        }
    }

    @Override
    public void visit(Block block) {
        if( block == null )
            return;
        
        for( Statement blockStat : block.getBody() )
            this.visitStatement( blockStat );
    }

    @Override
    public void visit(Conditional conditional) {
        if( conditional == null )
            return;
        
        visitExpr( conditional.getExpression() );
        Type exprType = conditional.getExpression().getType();
        if(!(exprType instanceof BooleanType)
        && !(exprType instanceof NoType))
        {
            typeErrors.add("Line:" + conditional.getExpression().getLineNum() + ":condition type must be boolean");
        }

        visitStatement( conditional.getConsequenceBody() );
        visitStatement( conditional.getAlternativeBody() );
    }

    @Override
    public void visit(While loop) {
        if( loop == null )
            return;
        visitExpr( loop.getCondition() );
        Type exprType = loop.getCondition().getType();
        if(!(exprType instanceof BooleanType)
        && !(exprType instanceof NoType))
        {
            typeErrors.add("Line:" + loop.getCondition().getLineNum() + ":condition type must be boolean");
        }
        visitStatement( loop.getBody() );

    }

    private boolean writeCheckArg(Expression arg){
        if( arg.getType() instanceof StringType 
        || arg.getType() instanceof IntType
        || arg.getType() instanceof ArrayType
        || arg.getType() instanceof NoType
        ){
            return true;
        }
        return false;
    }
    private void writeCheckArgType(Expression arg){
        if(!writeCheckArg(arg)){
            typeErrors.add("Line:" + arg.getLineNum() + ":unsupported type for writeln");
        }
    }
    @Override
    public void visit(Write write) {
        if( write == null )
            return;
        visitExpr( write.getArg() );
        writeCheckArgType(write.getArg());
    }
}

