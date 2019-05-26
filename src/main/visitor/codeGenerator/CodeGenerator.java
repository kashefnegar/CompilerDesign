package main.visitor.codeGenerator;

import main.ast.node.*;
import main.ast.node.declaration.*;
import main.ast.node.expression.*;
import main.ast.node.expression.Value.*;
import main.ast.node.statement.*;
import main.ast.node.expression.BinOp.*;

import main.symbolTable.*;
import main.symbolTable.symbolTableVariable.*;
import main.symbolTable.itemException.*;

import main.ast.Type.*;
import main.ast.Type.PrimitiveType.*;
import main.ast.Type.ArrayType.*;
import main.ast.Type.UserDefinedType.UserDefinedType;

import main.visitor.*;

import java.util.*;
import java.io.*;


public class CodeGenerator extends VisitorImpl {

    public File dir;
    public String mainClassName;

    @Override
    public void visit(Node node) {}

    @Override
    public void visit(Program program){
        if( program == null )
            return;
            
        dir = new File(".\\output");
        dir.mkdirs();

        System.out.println("program");
        mainClassName = program.getMainClass().getName().getName();
        this.visit(program.getMainClass());
        for (ClassDeclaration classDeclaration : program.getClasses())
            this.visit(classDeclaration);
    }

    @Override
    public void visit(ClassDeclaration classDeclaration) {
        if( classDeclaration == null )
            return;

        File file = new File(dir, classDeclaration.getName().getName() + ".j");

        System.out.println(classDeclaration.getName().getName() + ".j");
        try{
            file.createNewFile();
            FileWriter writer = new FileWriter(file);
        System.out.println("classDeclaration");

            try
            {
                ClassSymbolTableItem classSymItem = (ClassSymbolTableItem)SymbolTable.top.get(ClassSymbolTableItem.CLASS + classDeclaration.getName().getName());
                SymbolTable.push(classSymItem.getClassSym());
                SymbolTable.top.writer = writer;

                String classCode = 
                    ".class public " + classDeclaration.getName().getName() + "\n";

                String parentCode = ".super ";
                if(classDeclaration.getParentName() != null){
                    parentCode += classDeclaration.getParentName().getName() + "\n";  //??
                }
                else if(classDeclaration.getName().getName().equals("Object")){
                    parentCode += "java/lang/Object\n";
                }
                else{
                    parentCode += "java/lang/Object\n";//??
                }

                String constructor = 
                ".method public <init>()V\n" + 
                    "aload_0\n" +
                    "invokenonvirtual java/lang/Object/<init>()V\n" + 
                    "return\n" + 
                ".end method\n";
                
                SymbolTable.top.writer.write(classCode + parentCode + constructor);

                for( VarDeclaration varDeclaration: classDeclaration.getVarDeclarations() )
                    this.visit( varDeclaration );

                for( MethodDeclaration methodDeclaration: classDeclaration.getMethodDeclarations() ){
                    if(methodDeclaration instanceof MainMethodDeclaration){
                        visit((MainMethodDeclaration) methodDeclaration);
                    }
                    else{
                        this.visit( methodDeclaration );
                    }
                }
                
                SymbolTable.pop();
            }
            catch(ItemNotFoundException infe)
            {
                System.out.println("Error in typechecker classDeclaration visit class symbol table not found");
            }
            
            writer.close();
        } catch(IOException e) {
            System.out.println("File already exists.");
        }
    }
    
    private String getJasminType(Type t) {
        if (t instanceof IntType) {
            return "I";
        } else if (t instanceof StringType) {
            return "Ljava/lang/String;";
        } else if (t instanceof BooleanType) {
            return "Z";
        } else if (t instanceof ArrayType) {
            return "[I";
        } else if (t instanceof UserDefinedType) {
            return "Ljava/lang/" + ((UserDefinedType) t).getName() + ";";
        } else {
            System.out.println("The type was not of any instance!");
            return "V";
        }
    }
    private String getReturnCode(Type t){
        if (t instanceof IntType){
            return "ireturn\n";
        }else if(t instanceof StringType){
            return "areturn\n";
        }
        else if(t instanceof BooleanType){
            return "ireturn\n";
        }
        else if(t instanceof ArrayType){
            return "areturn\n";
        }
        else if (t instanceof UserDefinedType) {
            return "areturn\n";
        }
        else{
            return "return\n";
        }
    }
    @Override
    public void visit(MethodDeclaration methodDeclaration) {
        int localCount = 0;
        int argCount = 0;
        if( methodDeclaration == null )
            return;
        String methodCode = ".method public " + methodDeclaration.getName().getName() + "(" ;
        String localCode = "";
        for( VarDeclaration argDeclaration: methodDeclaration.getArgs() ){
            methodCode += getJasminType(argDeclaration.getType());
            argCount += 1;
        }
            
        methodCode += ")" + getJasminType(methodDeclaration.getActualReturnType()) + "\n";   
        
        methodCode += ".limit locals ";

        String startLabel = "L"+ methodDeclaration.getName().getName() +  methodDeclaration.getName().getLineNum() + "_start";
        String endLabel = "L" + methodDeclaration.getName().getName() + methodDeclaration.getName().getLineNum() + "_end";

        for( VarDeclaration localVariable: methodDeclaration.getLocalVars() ){
            localCount += 1;
            localCode += ".var " + (argCount + localCount) + " is " + localVariable.getIdentifier().getName() + getJasminType(localVariable.getType()) 
                        + " from L" + startLabel + " to L"
                        + endLabel + "\n" ;
        }

        methodCode += (localCount + argCount + 1) + "\n";
        methodCode += ".limit stack 16\n";
        
        try {
            SymbolTable.top.writer.write(methodCode + localCode + startLabel + ":\n");
        } catch (IOException e) {
            System.out.println("ioexception");
        }

        for( Statement statement : methodDeclaration.getBody() )//
            visitStatement( statement );
          
        visitExpr( methodDeclaration.getReturnValue() );//

        String returnCode = getReturnCode(methodDeclaration.getActualReturnType());

        try {
            SymbolTable.top.writer.write(endLabel + ":\n" + returnCode + ".end method\n");
        } catch (IOException e) {
            System.out.println("ioexception");
        }
    }

    @Override
    public void visit(MainMethodDeclaration methodDeclaration) {
        if( methodDeclaration == null )
            return;

        String methodCode = ".method public static main([Ljava/lang/String;)V\n"
                          + ".limit locals 1\n"
                          + ".limit stack 4\n"
                          + "new " + mainClassName + "\n"
                          + "dup\n"
                          + "invokespecial " + mainClassName + "/<init>()V\n"
                          + "invokevirtual " + mainClassName + "/"
                          + methodDeclaration.getName().getName()
                          + "()" + getJasminType(methodDeclaration.getActualReturnType()) + "\n"
                          + "return\n.end method\n"; 
        
        try {
            SymbolTable.top.writer.write(methodCode);
        } catch (IOException e) {
            System.out.println("ioexception");
        }

        visit((MethodDeclaration) methodDeclaration);
    }

    

    @Override
    public void visit(VarDeclaration varDeclaration) {
        if( varDeclaration == null )
            return;

        String fieldCode = ".field public " + varDeclaration.getIdentifier().getName() 
            + " " + getJasminType(varDeclaration.getType()) + "\n";
        try{
            SymbolTable.top.writer.write(fieldCode);
        }
        catch(IOException e){
            System.out.println("ioexception");
        }
    }

    @Override
    public void visit(ArrayCall arrayCall) {
        if( arrayCall == null )
            return;
        String arrayCallCode = "iaload\n";
        visitExpr( arrayCall.getInstance() );
        visitExpr( arrayCall.getIndex() );
        try {
            SymbolTable.top.writer.write(arrayCallCode);
        } catch (IOException e) {
            System.out.println("ioexception");
        }
    }
    private String getBinaryOperatorCode(Type t , BinaryOperator b , Integer line){
        if(b == BinaryOperator.add){
            return "iadd\n";
        }
        else if(b == BinaryOperator.sub){
            return "isub\n";
        }
        else if(b == BinaryOperator.mult){
            return "imult\n";
        }
        else if(b == BinaryOperator.div){
            return "idiv\n";
        }
        else if (b == BinaryOperator.eq) {
            if(t instanceof IntType || t instanceof BooleanType){
                return "if_icmpne PushZero" + line + "\niconst_1\ngoto CmpEnd" + line + "\nPushZero" + line
                        + ":\niconst_0\nCmpEnd" + line + ":\n";
            }
            else{
                return "invokevirtual java/lang/Object/equals(Ljava/lang/Object/equals(Ljava/lang/Object;)Z\n";//???
            }
            
        }
        else if (b == BinaryOperator.lt) {
            return "if_icmpge PushZero" + line + "\niconst_1\ngoto CmpEnd" + line + "\nPushZero" + line
                    + ":\niconst_0\nCmpEnd" + line + ":\n";
        }
        else if (b == BinaryOperator.gt) {
            return "if_icmple PushZero" + line + "\niconst_1\ngoto CmpEnd" + line + "\nPushZero" + line
                    + ":\niconst_0\nCmpEnd" + line + ":\n";
        }
        else if (b == BinaryOperator.neq) {
            if (t instanceof IntType || t instanceof BooleanType){
                return "if_icmpeq PushZero" + line + "\niconst_1\ngoto CmpEnd" + line + "\nPushZero" + line
                        + ":\niconst_0\nCmpEnd" + line + ":\n";
            }
            else{
                return "invokevirtual java/lang/Object/equals(Ljava/lang/Object/equals(Ljava/lang/Object;)Z\n";//???
            }
            
        }
        else{
            return "";
        }
    }
    private String getShortCircuitCode(BinaryOperator b, Integer line , Integer col){
        if (b == BinaryOperator.or) {
            return "ifne PushOne" + line + col + "\n";
        }
        else if(b == BinaryOperator.and){
            return "ifeq PushZero" + line + col + "\n";
        }
        else{
            return "";
        }
    }
    @Override
    public void visit(BinaryExpression binaryExpression) {
        if( binaryExpression == null )
            return;
        
        Expression lOperand = binaryExpression.getLeft();
        Expression rOperand = binaryExpression.getRight();
        String BinExpCode = "";
        BinaryOperator opt = binaryExpression.getBinaryOperator();
        if(opt == BinaryOperator.or || opt == BinaryOperator.and){
            visitExpr(lOperand);

            BinExpCode = getShortCircuitCode(opt, binaryExpression.getLineNum() , binaryExpression.getColNum());
            try {
                SymbolTable.top.writer.write(BinExpCode);
            } catch (IOException e) {
                System.out.println("ioexception");
            }
    
            visitExpr(rOperand);
            Integer line = binaryExpression.getLineNum() ;
            Integer col = binaryExpression.getColNum() ;
            BinExpCode = "ifeq PushZero" + line + col + "\n" + "PushZero" + line + col + ":\niconst_1\ngoto End" + line + col + "\nPushZero" + line + col + ":\niconst_0\n"
                        + "End" + line + col + ":\n";

            try {
                SymbolTable.top.writer.write(BinExpCode);
            } catch (IOException e) {
                System.out.println("ioexception");
            }
        }
        else{
            visitExpr(lOperand);
            visitExpr(rOperand);
            BinExpCode = getBinaryOperatorCode(lOperand.getType() , opt , binaryExpression.getLineNum());
            try {
                SymbolTable.top.writer.write(BinExpCode);
            } catch (IOException e) {
                System.out.println("ioexception");
            }
        }
    }

    @Override
    public void visit(Identifier identifier) {
        if( identifier == null )
            return;
    }

    @Override
    public void visit(Length length) {
        if( length == null )
            return;
        visitExpr(length.getExpression());//#
        try {
            SymbolTable.top.writer.write("arraylength\n");
        } catch (IOException e) {
            System.out.println("ioexception");
        }
        
    }

    @Override
    public void visit(MethodCall methodCall) {////////
        if( methodCall == null )
            return;
        visitExpr(methodCall.getInstance());

        String methodCallCode = "invokevirtual " 
            + ((UserDefinedType)methodCall.getInstance().getType()).getClassDeclaration().getName().getName()
            + "/" + methodCall.getMethodName().getName() + "(";

        for (Expression argument : methodCall.getArgs()){
            visitExpr(argument);//#
            methodCallCode += getJasminType(argument.getType());
        }

        methodCallCode += ")" + getJasminType(methodCall.getType()) + "\n";

        try {
            SymbolTable.top.writer.write(methodCallCode);
        } catch (IOException e) {
            System.out.println("ioexception");
        }
    }

    @Override
    public void visit(NewArray newArray) {
        if( newArray == null )
            return;
        visitExpr(newArray.getExpression());//# bipush
        String newArrayCode = "newarray int\n";
        try {
            SymbolTable.top.writer.write(newArrayCode);
        } catch (IOException e) {
            System.out.println("ioexception");
        }
        
    }

    @Override
    public void visit(NewClass newClass) {
        if( newClass == null )
            return;
        
        try {
            SymbolTable.top.writer.write(
                "new " + newClass.getClassName().getName() + "\n" +
                "dup\n" +
                "invokespecial " + newClass.getClass().getName() + "/<init>()V\n");//????
        } catch (IOException e) {
            System.out.println("ioexception");
        }
    }

    @Override
    public void visit(This instance) {
        if( instance == null )
            return;
        try {
            SymbolTable.top.writer.write("aload_0\n");
        } catch (IOException e) {
            System.out.println("ioexception");
        }
        
    }
    private String getUnaryCode(UnaryOperator opt , Integer line , Integer col){
        if(opt == UnaryOperator.minus){
            return "ineg\n";
        }
        else{
            return "ifne PushZero" + line + col + "\niconst_1\ngoto UnaryEnd" + line + col 
                        + "\nPushZero" + line + col + ":\niconst_0\nUnaryEnd" + line + col + ":\n";
        }
    }
    @Override
    public void visit(UnaryExpression unaryExpression) {
        if( unaryExpression == null )
            return;
        String unaryExpCode = "";
        Integer line = unaryExpression.getLineNum();
        Integer col = unaryExpression.getColNum();
        
        try {
            visitExpr(unaryExpression.getValue());
            unaryExpCode = getUnaryCode(unaryExpression.getUnaryOperator(), line, col);

            try {
                SymbolTable.top.writer.write(unaryExpCode);
            } catch (IOException e) {
                System.out.println("ioexception");
            }
        }
        catch( NullPointerException npe )
        {
            System.out.println( "unary value is null" );
        }
    }

    @Override
    public void visit(BooleanValue value) {
        if( value == null )
            return;
        String booleanCode = "iconst_";
        if(value.isConstant() == true){
            booleanCode += "1\n";
        }
        else{
            booleanCode += "0\n";
        }
        try {
            SymbolTable.top.writer.write(booleanCode);
        } catch (IOException e) {
            System.out.println("ioexception");
        }
    }

    @Override
    public void visit(IntValue value) {
        if( value == null )
            return;
        try {
            SymbolTable.top.writer.write("bipush " + value.getConstant() + "\n");
        } catch (IOException e) {
            System.out.println("ioexception");
        }   
    }

    @Override
    public void visit(StringValue value) {
        if( value == null )
            return;
        try {
            SymbolTable.top.writer.write("ldc " + value.getConstant() + "\n");
        } catch (IOException e) {
            System.out.println("ioexception");
        }
    }

    @Override
    public void visit(Assign assign) {//#
        if( assign == null )
            return;
        Expression lExpr = assign.getlValue();
        visitExpr(lExpr);
        Expression rValExpr = assign.getrValue();
        if (rValExpr != null)
            visitExpr(rValExpr);

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
        String condCode = "ifeq Else" + conditional.getLineNum() + "\n";
        try {
            SymbolTable.top.writer.write(condCode);
        } catch (IOException e) {
            System.out.println("ioexception");
        }
        visitStatement( conditional.getConsequenceBody() );
        condCode = "Else" + conditional.getLineNum() + ":\n";
        try {
            SymbolTable.top.writer.write(condCode);
        } catch (IOException e) {
            System.out.println("ioexception");
        }
        visitStatement( conditional.getAlternativeBody() );
    }

    @Override
    public void visit(While loop) {
        if( loop == null )
            return;
        String whileCode = "WhileStart" + loop.getLineNum() +":\n";
        try {
            SymbolTable.top.writer.write(whileCode);
        } catch (IOException e) {
            System.out.println("ioexception");
        }
        visitExpr( loop.getCondition() );
        whileCode = "ifeq WhileEnd" + loop.getLineNum() + " \n";
        try {
            SymbolTable.top.writer.write(whileCode);
        } catch (IOException e) {
            System.out.println("ioexception");
        }
        visitStatement(loop.getBody());
        whileCode = "goto WhileStart" + loop.getLineNum() + "\n" + "WhileEnd" + loop.getLineNum() + ":\n";
        try {
            SymbolTable.top.writer.write(whileCode);
        } catch (IOException e) {
            System.out.println("ioexception");
        }
    }

    @Override
    public void visit(Write write) {
        if( write == null )
            return;

        try {
            SymbolTable.top.writer.write("getstatic java/lang/System/out Ljava/io/PrintStream;\n");
        } catch (IOException e) {
            System.out.println("ioexception");
        }
        visitExpr(write.getArg());// #

        String writeCode = "invokevirtual java/io/PrintStream/println(" + getJasminType(write.getArg().getType()) + ")V\n" ;//????
        
        try {
            SymbolTable.top.writer.write(writeCode);
        } catch (IOException e) {
            System.out.println("ioexception");
        }
    }
}

