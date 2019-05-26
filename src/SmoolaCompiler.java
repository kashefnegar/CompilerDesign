import main.compileError.CompileErrorException;
import main.symbolTable.*;
import main.ast.node.Program;
import main.visitor.typeChecker.TypeChecker;
import main.visitor.astPrinter.ASTPrinter;
import main.visitor.nameAnalyzer.NameAnalyser;
import main.visitor.codeGenerator.*;
import org.antlr.v4.runtime.CharStream;
import org.antlr.v4.runtime.CommonTokenStream;

public class SmoolaCompiler {
    private static final String COMPILATION_TERMINATED_ERROR = "compilation terminated with some errors";
    public void compile( CharStream reader )
    {
        SmoolaLexer lexer = new SmoolaLexer(reader);   // SmoolaLexer in your project
        CommonTokenStream tokens = new CommonTokenStream(lexer);
        SmoolaParser parser = new SmoolaParser(tokens);   // SmoolaParser in your project
        SymbolTable.push( new SymbolTable( null) );
        SymbolTable.root = SymbolTable.top;
        try {
            Program program = parser.program().returnedProgram; // program is the name of the start rule
            NameAnalyser nameAnalyser = new NameAnalyser();
            nameAnalyser.visit( program );
            if( nameAnalyser.numOfErrors() != 0 ) {
                throw new CompileErrorException();
            }
            TypeChecker typeChecker = new TypeChecker();
            typeChecker.visit( program );
            if( typeChecker.numOfErrors() != 0)
            {
                throw new CompileErrorException();
            }

            CodeGenerator codeGenerator = new CodeGenerator();
            codeGenerator.visit(program);
            
            ASTPrinter printer = new ASTPrinter();
            printer.visit( program );
        }
        catch( Exception compileError )
        {
//            compileError.printStackTrace();
//            System.out.println( "----" + SmoolaCompiler.COMPILATION_TERMINATED_ERROR );
        }
    }
}
