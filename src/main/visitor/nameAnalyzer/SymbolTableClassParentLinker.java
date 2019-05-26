package main.visitor.nameAnalyzer;

import main.ast.node.Program;
import main.ast.node.declaration.ClassDeclaration;
import main.symbolTable.*;
import main.symbolTable.itemException.ItemNotFoundException;
import main.ast.Type.UserDefinedType.*;

import java.util.HashSet;
import java.util.Set;

public class SymbolTableClassParentLinker {

    public void findClassesParents( Program program )
    {
        Set<String> visitedClasses = new HashSet<>();
        for(ClassDeclaration classDeclaration: program.getClasses() )
        {
            linkClassWithItsParents( classDeclaration , visitedClasses );
        }
    }
    private void linkClassWithItsParents( ClassDeclaration classDeclaration , Set<String> visitedClasses )
    {
        SymbolTable currentSymTable, prevSymTable = null;
        ClassSymbolTableItem classItem;
        String name = classDeclaration.getName().getName();
        try {
            do {
                classItem = ((ClassSymbolTableItem) SymbolTable.root.getInCurrentScope(ClassSymbolTableItem.CLASS + name));
                currentSymTable = classItem.getClassSym();
                if (prevSymTable != null)
                    prevSymTable.setPreSymbolTable(currentSymTable);
                if (name != null && visitedClasses.contains(name))
                    throw new InheritanceCycleException(name, classItem.getParentName());   //why not throw cycle exception?
                visitedClasses.add(name);
                name = classItem.getParentName();
                prevSymTable = currentSymTable;
            } while (name != null);
        }
        catch( ItemNotFoundException itemNotFound )
        {
            //if the parent does not exists we get an error here
            System.out.println( "parent name does not exist" );
        }
        catch(InheritanceCycleException cycle)
        {
            System.out.println("inheritence cycle detected for class " + cycle.getName()+ " with parent " + cycle.getParent());
        }
    }
}
